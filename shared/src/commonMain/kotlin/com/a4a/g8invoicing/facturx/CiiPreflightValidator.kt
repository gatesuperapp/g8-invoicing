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
    object IssuerAddress : CiiValidationIssue()

    object ClientMissing : CiiValidationIssue()
    object ClientName : CiiValidationIssue()
    object ClientTypeUnspecified : CiiValidationIssue()

    /**
     * A company-id slot carries a label whose keyword (SIRET, TVA, EIN, CUIT…)
     * flags an expected format, but the value doesn't match that format. The
     * builder skips the slot from the XML — this warning tells the user to
     * either fix the value or rename the label. [fieldLabel] is the user-typed
     * label as-is; [expectedFormat] is a plain-language format hint (e.g.
     * "14 chiffres", "code pays ISO + 8 à 12 caractères").
     *
     * Supersedes the older IssuerSiren / IssuerSirenFormat / IssuerVat /
     * IssuerVatFormat + client mirrors — the classification-based check
     * covers both "missing" (no slot maps to that kind) and "wrong format"
     * (label recognised but value doesn't fit) cases uniformly.
     */
    data class IssuerCompanyIdLabelMismatch(
        val fieldLabel: String,
        val expectedFormat: String,
    ) : CiiValidationIssue()

    data class ClientCompanyIdLabelMismatch(
        val fieldLabel: String,
        val expectedFormat: String,
    ) : CiiValidationIssue()

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
            // Label-first classification across the 3 company-id slots.
            // Every "wrong format for this label" surfaces here — covers the
            // old SIREN/VAT format checks uniformly (with the label the user
            // actually typed, not a hardcoded "SIREN of the issuer" wording).
            // Empty slots stay silent — the export path lets the invoice go
            // through with just BT-29 fallback (see appendParty in the
            // builder). A completely tax-id-less issuer is only really
            // blocked downstream by BR-CO-26, which the receiver catches.
            collectLabelMismatches(issuer) { label, expectedFormat ->
                issues += CiiValidationIssue.IssuerCompanyIdLabelMismatch(label, expectedFormat)
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
                    collectLabelMismatches(client) { label, expectedFormat ->
                        issues += CiiValidationIssue.ClientCompanyIdLabelMismatch(label, expectedFormat)
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

    /**
     * Run [classifyCompanyId] on the party's 3 company-id slots and invoke
     * [onMismatch] once per [CompanyIdClassification.LabelMismatch]. The
     * builder skips mismatched slots from the XML — we surface a modale
     * Oups warning so the user fixes the value or renames the label.
     * Empty slots and clean matches are silent.
     */
    private fun collectLabelMismatches(
        party: ClientOrIssuerState,
        onMismatch: (fieldLabel: String, expectedFormat: String) -> Unit,
    ) {
        val country = party.addresses?.firstOrNull()?.countryCode?.trim()?.uppercase()?.ifEmpty { null }
        val slots = listOf(
            party.companyId1Label?.text to party.companyId1Number?.text,
            party.companyId2Label?.text to party.companyId2Number?.text,
            party.companyId3Label?.text to party.companyId3Number?.text,
        )
        slots.forEachIndexed { index, (label, value) ->
            // A FR issuer that has never renamed the label sub-field shows
            // the resource default ("N° SIRET" / "N° TVA" / "N° RCS") in the
            // UI while the state's label stays null. Without this fallback
            // the classifier's label-first branch would skip, regex-fallback
            // would silently drop garbage input, and the export would sneak
            // through — matching the "4 letters in the SIRET field, no
            // warning" bug. Only wired for FR (the country whose default
            // labels we hardcode); other countries continue to rely on the
            // regex fallback with their own patterns.
            val effectiveLabel = label?.takeIf { it.isNotBlank() }
                ?: DEFAULT_FR_SLOT_HINTS.getOrNull(index)?.takeIf { country == "FR" }
            val result = classifyCompanyId(effectiveLabel, value, country)
            if (result is CompanyIdClassification.LabelMismatch) {
                onMismatch(result.fieldLabel, result.expected.expectedFormat)
            }
        }
    }

    /** Default keyword for each of the 3 company-id slots on a FR issuer —
     *  matches the resource text shown in the form when the user hasn't
     *  renamed the field. Used as a fallback hint by [collectLabelMismatches]
     *  so untouched-label slots still fire LabelMismatch on garbage input. */
    private val DEFAULT_FR_SLOT_HINTS: List<String> = listOf("SIRET", "TVA", "RCS")

    private fun hasCompletePostalAddress(party: ClientOrIssuerState): Boolean {
        val address = party.addresses?.firstOrNull() ?: return false
        return !address.addressLine1?.text.isNullOrBlank() &&
            !address.zipCode?.text.isNullOrBlank() &&
            !address.city?.text.isNullOrBlank()
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
