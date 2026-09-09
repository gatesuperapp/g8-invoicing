package com.a4a.g8invoicing.facturx

import com.a4a.g8invoicing.data.models.CountryCodes
import com.a4a.g8invoicing.data.models.defaultPaymentBankSegments
import com.a4a.g8invoicing.data.models.flattenPaymentBank
import com.a4a.g8invoicing.ui.states.InvoiceState

/**
 * Build the free-form bank identification text that
 * [CiiXmlBuilder] drops into `<ram:Information>`. Same identifier-label
 * selection (IBAN vs generic account label based on the payment country)
 * and same default-segments fallback as the PDF footer / CII platform
 * screen — kept in one place so the Factur-X and CII XML paths share
 * identical output.
 *
 * Returns null when the block is hidden, empty, or the issuer has no
 * IBAN/BIC so callers don't emit an empty `Information` block.
 */
fun buildBankInfoText(
    invoice: InvoiceState,
    ibanLabel: String,
    genericLabel: String,
): String? {
    if (invoice.paymentBankHidden) return null
    val issuer = invoice.documentIssuer ?: return null
    val iban = issuer.paymentIban?.text?.trim().orEmpty()
    val bic = issuer.paymentBic?.text?.trim().orEmpty()
    if (iban.isEmpty() && bic.isEmpty()) return null
    val identifierLabel = if (
        CountryCodes.isIbanCountry(issuer.paymentCountry) || issuer.paymentCountry == null
    ) ibanLabel else genericLabel
    val segments = invoice.paymentBankSegments.ifEmpty { defaultPaymentBankSegments() }
    return flattenPaymentBank(segments, identifierLabel, iban, bic)
        .takeIf { it.isNotEmpty() }
}
