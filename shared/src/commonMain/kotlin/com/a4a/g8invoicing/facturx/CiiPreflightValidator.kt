package com.a4a.g8invoicing.facturx

import com.a4a.g8invoicing.data.models.ClientType
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.InvoiceState

/**
 * Pre-flight check for the mandatory fields an EN 16931 CII XML export
 * needs. Runs before the export screen fires so the user gets a clear
 * "these fields are missing" list instead of a technically-valid but
 * business-invalid XML that a downstream platform would reject.
 *
 * Only what a downstream e-invoicing platform will actually refuse —
 * optional-but-recommended fields (BT-3 already fixed at 380, currency,
 * payment terms text, etc.) are not checked. FR-national Schematron
 * rules (BR-FR-*) are also enforced here since Chorus Pro / DGFiP is
 * the primary landing zone for exports out of the FR user base.
 */
sealed class CiiValidationIssue {
    object IssuerMissing : CiiValidationIssue()
    object IssuerName : CiiValidationIssue()
    object IssuerSiren : CiiValidationIssue()

    /** SIREN present but not exactly 9 digits (BR-FR-10 / BR-FR-32). */
    object IssuerSirenFormat : CiiValidationIssue()
    object IssuerVat : CiiValidationIssue()

    /** Issuer VAT number present but doesn't match the EU format for the
     *  issuer's country (FR must be FR + 2 alphanum + 9 digits, DE = DE +
     *  9 digits, etc.). Only fired when the country is in the EU whitelist. */
    object IssuerVatFormat : CiiValidationIssue()
    object IssuerAddress : CiiValidationIssue()

    object ClientMissing : CiiValidationIssue()
    object ClientName : CiiValidationIssue()
    object ClientTypeUnspecified : CiiValidationIssue()
    object ClientSiren : CiiValidationIssue()

    /** Client SIREN present but not exactly 9 digits. Fires regardless of
     *  clientType — a stray value on an INDIVIDUAL still ends up printed on
     *  the PDF but dropped from the XML, and that PDF/XML delta is what
     *  causes the downstream inconsistency the guard is meant to catch. */
    object ClientSirenFormat : CiiValidationIssue()

    /** Client VAT number present but bad format for the client's country
     *  (see IssuerVatFormat). Only checked on PROFESSIONAL clients — an
     *  INDIVIDUAL never carries a VAT number. */
    object ClientVatFormat : CiiValidationIssue()

    /**
     * Individual (B2C) client has no email → BT-49 (buyer electronic
     * address) would be empty. For a particulier, email is the natural
     * routing identifier since they have no SIREN by definition. Without
     * either, BR-FR-12 flags the invoice as un-routable.
     */
    object ClientEmail : CiiValidationIssue()
    object ClientAddress : CiiValidationIssue()

    object ProductsEmpty : CiiValidationIssue()

    /** [lineNumber] is 1-based for display. */
    data class LineName(val lineNumber: Int) : CiiValidationIssue()
    data class LinePrice(val lineNumber: Int) : CiiValidationIssue()
    data class LineTaxRate(val lineNumber: Int) : CiiValidationIssue()

    /**
     * VAT rate not in the FR-allowed set (BR-FR-16). Only fired when the
     * issuer's country is FR — a Belgian issuer using a 21 % rate is
     * legitimate and must not be flagged.
     */
    data class LineTaxRateInvalid(val lineNumber: Int, val rate: String) : CiiValidationIssue()

    object InvoiceDueDate : CiiValidationIssue()

    /**
     * Issuer is on franchise en base (vatExempt=true → every line ends up in
     * tax category E) but the invoice's BT-120 exemption reason text is empty.
     * BR-E-10 requires either BT-120 or BT-121 in every VAT breakdown group,
     * and we can't emit BT-121 for E (the code varies with the operation,
     * not the country). Blocks export until the user sets the text in the
     * text menu.
     */
    object VatExemptionTextMissing : CiiValidationIssue()
}

object CiiPreflightValidator {

    /**
     * FR VAT rates accepted by BR-FR-16. Covers metropolitan (0, 2.1,
     * 5.5, 8.5, 10, 13, 20) and DOM (0.9, 1.05, 1.75, 2.1, 8.5, 13, 20).
     * Kept as strings because BigDecimal equality is exact-representation
     * sensitive and the incoming rate can be "5.5" or "5.50".
     */
    private val FR_ALLOWED_VAT_RATES = setOf(
        "0", "0.9", "1.05", "1.75", "2.1", "5.5", "8.5", "10", "13", "20",
    )

    fun validate(invoice: InvoiceState): List<CiiValidationIssue> {
        val issues = mutableListOf<CiiValidationIssue>()

        val issuer = invoice.documentIssuer
        if (issuer == null) {
            issues += CiiValidationIssue.IssuerMissing
        } else {
            if (issuer.name.text.isBlank()) issues += CiiValidationIssue.IssuerName
            val issuerSiren = issuer.companyId1Number?.text?.trim().orEmpty()
            if (issuerSiren.isBlank()) {
                issues += CiiValidationIssue.IssuerSiren
            } else if (!isValidSiren(issuerSiren)) {
                issues += CiiValidationIssue.IssuerSirenFormat
            }
            // VAT number is skipped only when the issuer is explicitly on
            // the "franchise en base" scheme — otherwise it's mandatory for
            // any invoice carrying VAT lines. When present, the format has
            // to match the EU pattern for the issuer's country (a "FR12345"
            // won't be accepted by any downstream platform).
            if (!issuer.vatExempt) {
                val issuerVat = issuer.companyId2Number?.text?.trim().orEmpty()
                val issuerCountry = issuer.addresses?.firstOrNull()?.countryCode
                if (issuerVat.isBlank()) {
                    issues += CiiValidationIssue.IssuerVat
                } else if (!isValidEuVatNumber(issuerCountry, issuerVat)) {
                    issues += CiiValidationIssue.IssuerVatFormat
                }
            }
            if (!hasCompletePostalAddress(issuer)) issues += CiiValidationIssue.IssuerAddress
        }

        val client = invoice.documentClient
        if (client == null) {
            issues += CiiValidationIssue.ClientMissing
        } else {
            if (client.name.text.isBlank()) issues += CiiValidationIssue.ClientName
            when (client.clientType) {
                null -> issues += CiiValidationIssue.ClientTypeUnspecified
                ClientType.PROFESSIONAL -> {
                    val clientSiren = client.companyId1Number?.text?.trim().orEmpty()
                    if (clientSiren.isBlank()) issues += CiiValidationIssue.ClientSiren
                }
                ClientType.INDIVIDUAL -> {
                    // Particulier — no SIREN by definition, so BT-49 falls
                    // back to email. Without one the URIUniversalCommunication
                    // block is skipped entirely and the FR Schematron flags
                    // an empty electronic address.
                    if (client.emails.orEmpty().none { it.email.text.isNotBlank() }) {
                        issues += CiiValidationIssue.ClientEmail
                    }
                }
            }

            // Format checks run whenever the field is populated, regardless
            // of clientType — a bogus SIREN typed on an INDIVIDUAL still
            // lands on the PDF but gets dropped from the XML, and we want
            // that discrepancy caught at the Oups modal, not silently.
            val clientSirenRaw = client.companyId1Number?.text?.trim().orEmpty()
            if (clientSirenRaw.isNotBlank() && !isValidSiren(clientSirenRaw)) {
                issues += CiiValidationIssue.ClientSirenFormat
            }
            if (client.clientType == ClientType.PROFESSIONAL) {
                val clientVat = client.companyId2Number?.text?.trim().orEmpty()
                if (clientVat.isNotBlank()) {
                    val clientCountry = client.addresses?.firstOrNull()?.countryCode
                    if (!isValidEuVatNumber(clientCountry, clientVat)) {
                        issues += CiiValidationIssue.ClientVatFormat
                    }
                }
            }

            if (!hasCompletePostalAddress(client)) issues += CiiValidationIssue.ClientAddress
        }

        val products = invoice.documentProducts.orEmpty()
        if (products.isEmpty()) {
            issues += CiiValidationIssue.ProductsEmpty
        } else {
            val issuerCountry = issuer?.addresses?.firstOrNull()?.countryCode?.uppercase()
            products.forEachIndexed { index, product ->
                val lineNumber = index + 1
                if (product.name.text.isBlank()) issues += CiiValidationIssue.LineName(lineNumber)
                if (product.priceWithoutTax == null) issues += CiiValidationIssue.LinePrice(lineNumber)
                // Tax rate is only skippable when the issuer is on
                // franchise en base — that maps to category E across every
                // line. Otherwise each line must carry an explicit rate so
                // TaxCategoryResolver has something to work with.
                val rate = product.taxRate
                if (rate == null && issuer?.vatExempt != true) {
                    issues += CiiValidationIssue.LineTaxRate(lineNumber)
                } else if (rate != null && issuerCountry == "FR") {
                    val display = normalizeRateForCompare(rate.toPlainString())
                    if (display !in FR_ALLOWED_VAT_RATES) {
                        issues += CiiValidationIssue.LineTaxRateInvalid(lineNumber, display)
                    }
                }
            }
        }

        if (invoice.dueDate.isBlank()) issues += CiiValidationIssue.InvoiceDueDate

        // Franchise en base → every line resolves to tax category E, which
        // requires an exemption reason per BR-E-10. Text is user-owned (not
        // deductible from the country), so block export until it's set.
        if (issuer?.vatExempt == true &&
            invoice.vatExemptionText?.text?.trim().isNullOrEmpty()) {
            issues += CiiValidationIssue.VatExemptionTextMissing
        }

        return issues
    }

    private fun hasCompletePostalAddress(party: ClientOrIssuerState): Boolean {
        val address = party.addresses?.firstOrNull() ?: return false
        return !address.addressLine1?.text.isNullOrBlank() &&
            !address.zipCode?.text.isNullOrBlank() &&
            !address.city?.text.isNullOrBlank()
    }

    /**
     * FR SIREN = exactly 9 digits. We strip separators the user commonly
     * types (spaces, dots, dashes) before checking — a "123 456 789"
     * entry must pass. SIRET (14 digits) is accepted only in the form
     * SIREN + NIC where the leading 9 chars parse as a SIREN.
     */
    private fun isValidSiren(raw: String): Boolean {
        val digits = raw.filter { it.isDigit() }
        return digits.length == 9 || digits.length == 14
    }

    /**
     * Structural VAT-number check per EU member state, plus Northern Ireland
     * (XI). Whitespace, dots and dashes are stripped before matching so a
     * user typing "FR 12 345678901" still passes. The map is intentionally
     * limited to structural regex — VIES online verification is out of scope
     * for a pre-export sanity check.
     *
     * Country codes are ISO 3166-1 alpha-2 (matching AddressState.countryCode)
     * with a Greek nuance: EL is the EU VAT prefix, GR the ISO country code
     * — both map to the same rule so an address country of GR still matches.
     * Unknown / non-EU countries pass through unchecked (returns true).
     */
    private fun isValidEuVatNumber(country: String?, vat: String): Boolean {
        val cc = country?.uppercase() ?: return true
        val normalized = vat.filter { !it.isWhitespace() && it != '.' && it != '-' }.uppercase()
        val pattern = EU_VAT_PATTERNS[cc] ?: return true
        return pattern.matches(normalized)
    }

    private val EU_VAT_PATTERNS = mapOf(
        "AT" to Regex("^ATU[0-9]{8}$"),
        "BE" to Regex("^BE[01][0-9]{9}$"),
        "BG" to Regex("^BG[0-9]{9,10}$"),
        "CY" to Regex("^CY[0-9]{8}[A-Z]$"),
        "CZ" to Regex("^CZ[0-9]{8,10}$"),
        "DE" to Regex("^DE[0-9]{9}$"),
        "DK" to Regex("^DK[0-9]{8}$"),
        "EE" to Regex("^EE[0-9]{9}$"),
        "EL" to Regex("^EL[0-9]{9}$"),
        // Greece uses EL as VAT prefix but GR as ISO country code — same rule.
        "GR" to Regex("^EL[0-9]{9}$"),
        "ES" to Regex("^ES[A-Z0-9][0-9]{7}[A-Z0-9]$"),
        "FI" to Regex("^FI[0-9]{8}$"),
        "FR" to Regex("^FR[A-Z0-9]{2}[0-9]{9}$"),
        "HR" to Regex("^HR[0-9]{11}$"),
        "HU" to Regex("^HU[0-9]{8}$"),
        "IE" to Regex("^IE([0-9]{7}[A-Z]{1,2}|[0-9][A-Z*+][0-9]{5}[A-Z])$"),
        "IT" to Regex("^IT[0-9]{11}$"),
        "LT" to Regex("^LT([0-9]{9}|[0-9]{12})$"),
        "LU" to Regex("^LU[0-9]{8}$"),
        "LV" to Regex("^LV[0-9]{11}$"),
        "MT" to Regex("^MT[0-9]{8}$"),
        "NL" to Regex("^NL[0-9]{9}B[0-9]{2}$"),
        "PL" to Regex("^PL[0-9]{10}$"),
        "PT" to Regex("^PT[0-9]{9}$"),
        "RO" to Regex("^RO[0-9]{2,10}$"),
        "SE" to Regex("^SE[0-9]{12}$"),
        "SI" to Regex("^SI[0-9]{8}$"),
        "SK" to Regex("^SK[0-9]{10}$"),
        // Northern Ireland — separate VAT prefix from GB following Brexit.
        "XI" to Regex("^XI([0-9]{9}|[0-9]{12}|GD[0-9]{3}|HA[0-9]{3})$"),
    )

    /**
     * Strip a trailing ".0", "0", or trailing zeros after a decimal point
     * so "5.50" and "5.5" collapse to the same lookup key. Preserves
     * meaningful precision ("2.1" stays "2.1", "0.9" stays "0.9").
     */
    private fun normalizeRateForCompare(raw: String): String {
        if (!raw.contains('.')) return raw
        val trimmed = raw.trimEnd('0').trimEnd('.')
        return trimmed.ifEmpty { "0" }
    }
}
