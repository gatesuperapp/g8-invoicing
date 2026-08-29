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
    object IssuerAddress : CiiValidationIssue()

    object ClientMissing : CiiValidationIssue()
    object ClientName : CiiValidationIssue()
    object ClientTypeUnspecified : CiiValidationIssue()
    object ClientSiren : CiiValidationIssue()

    /** Business client's SIREN present but not exactly 9 digits. */
    object ClientSirenFormat : CiiValidationIssue()

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
            // any invoice carrying VAT lines.
            if (!issuer.vatExempt && issuer.companyId2Number?.text.isNullOrBlank()) {
                issues += CiiValidationIssue.IssuerVat
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
                    if (clientSiren.isBlank()) {
                        issues += CiiValidationIssue.ClientSiren
                    } else if (!isValidSiren(clientSiren)) {
                        issues += CiiValidationIssue.ClientSirenFormat
                    }
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
