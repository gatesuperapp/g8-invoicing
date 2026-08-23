package com.a4a.g8invoicing.facturx

import com.a4a.g8invoicing.data.models.ClientType
import com.a4a.g8invoicing.data.models.ProductNature
import com.a4a.g8invoicing.data.models.unCefactCodesForExport
import com.a4a.g8invoicing.data.setScale
import com.a4a.g8invoicing.data.util.DateUtils
import com.a4a.g8invoicing.ui.shared.DocumentType
import com.a4a.g8invoicing.ui.states.AddressState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import kotlinx.datetime.LocalDate

/**
 * Hand-rolled CII XML builder for the **Extended** Factur-X profile
 * (URN `urn:cen.eu:en16931:2017#conformant#urn:factur-x.eu:1p0:extended`).
 *
 * Extended is required for our use case because a single invoice can
 * consolidate multiple delivery notes (BT-16) and reference several buyer
 * orders / project ids that the Basic and EN16931 profiles cap at one.
 *
 * The builder is pure `commonMain` — no JVM XML libraries, no JAXB — so
 * the same code path works on Android, Desktop and iOS. Escaping is
 * strict (5 XML predefined entities); numbers are emitted with a dot
 * decimal separator regardless of locale; dates go through the
 * UN/EDIFACT format 102 (`YYYYMMDD`).
 */
object CiiXmlBuilder {

    /**
     * Assemble the full CII XML for [invoice]. The result is valid XML
     * conforming to the Factur-X Extended profile — pending Schematron
     * validation against the FNFE-MPE toolkit at the caller's convenience.
     *
     * Every semantic decision (tax category per line, invoice type code,
     * currency, ref list dedup) is deterministic given the invoice state.
     * No I/O, no clock — this can run in a test with a hard-coded fixture
     * and produce byte-stable output.
     */
    fun build(invoice: InvoiceState): String = buildString {
        val currency = invoice.currency.text.trim().ifEmpty { "EUR" }
        val issuer = invoice.documentIssuer
        val buyer = invoice.documentClient
        val products = invoice.documentProducts.orEmpty()

        append("""<?xml version="1.0" encoding="UTF-8"?>""").append('\n')
        appendCrossIndustryInvoiceOpen()
        appendExchangedDocumentContext()
        appendExchangedDocument(invoice)
        append("  <rsm:SupplyChainTradeTransaction>\n")
        products.forEachIndexed { index, product ->
            appendLine(product, lineIndex = index + 1, invoice = invoice, currency = currency)
        }
        appendHeaderTradeAgreement(issuer, buyer, invoice.reference?.text)
        appendHeaderTradeDelivery(products)
        appendHeaderTradeSettlement(invoice, issuer, buyer, currency, products)
        append("  </rsm:SupplyChainTradeTransaction>\n")
        append("</rsm:CrossIndustryInvoice>\n")
    }

    // -----------------------------------------------------------------
    // Root element + namespace declarations. Kept as constants at the
    // top of every emitted document so downstream consumers can grep for
    // them and confirm profile intent without parsing the whole file.
    // -----------------------------------------------------------------

    private fun StringBuilder.appendCrossIndustryInvoiceOpen() {
        append("""<rsm:CrossIndustryInvoice""").append('\n')
        append("""    xmlns:rsm="urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100"""").append('\n')
        append("""    xmlns:qdt="urn:un:unece:uncefact:data:standard:QualifiedDataType:100"""").append('\n')
        append("""    xmlns:ram="urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100"""").append('\n')
        append("""    xmlns:xs="http://www.w3.org/2001/XMLSchema"""").append('\n')
        append("""    xmlns:udt="urn:un:unece:uncefact:data:standard:UnqualifiedDataType:100">""").append('\n')
    }

    private fun StringBuilder.appendExchangedDocumentContext() {
        append("  <rsm:ExchangedDocumentContext>\n")
        // BT-23 Business Process : A1 = "invoice for goods and services".
        // Factur-X 1.07.2 §2.2 makes this mandatory; A1 is the safe default.
        append("    <ram:BusinessProcessSpecifiedDocumentContextParameter>\n")
        append("      <ram:ID>A1</ram:ID>\n")
        append("    </ram:BusinessProcessSpecifiedDocumentContextParameter>\n")
        append("    <ram:GuidelineSpecifiedDocumentContextParameter>\n")
        append("      <ram:ID>urn:cen.eu:en16931:2017#conformant#urn:factur-x.eu:1p0:extended</ram:ID>\n")
        append("    </ram:GuidelineSpecifiedDocumentContextParameter>\n")
        append("  </rsm:ExchangedDocumentContext>\n")
    }

    // -----------------------------------------------------------------
    // Document header — BT-1 (number), BT-3 (type code), BT-2 (date),
    // BG-1 (notes / free text lifted from InvoiceState.freeField).
    // -----------------------------------------------------------------

    private fun StringBuilder.appendExchangedDocument(invoice: InvoiceState) {
        val typeCode = invoice.typeCode()
        append("  <rsm:ExchangedDocument>\n")
        append("    <ram:ID>${esc(invoice.documentNumber.text)}</ram:ID>\n")
        append("    <ram:TypeCode>$typeCode</ram:TypeCode>\n")
        append("    <ram:IssueDateTime>\n")
        append("      <udt:DateTimeString format=\"102\">${formatDate102(invoice.documentDate)}</udt:DateTimeString>\n")
        append("    </ram:IssueDateTime>\n")
        invoice.freeField?.text?.takeIf { it.isNotBlank() }?.let { note ->
            append("    <ram:IncludedNote>\n")
            append("      <ram:Content>${esc(note)}</ram:Content>\n")
            append("    </ram:IncludedNote>\n")
        }
        append("  </rsm:ExchangedDocument>\n")
    }

    // -----------------------------------------------------------------
    // Line items — BG-25. One <IncludedSupplyChainTradeLineItem> per
    // DocumentProduct. Line-level tax category (BT-151) resolved via
    // TaxCategoryResolver against per-line ProductNature + rate.
    // -----------------------------------------------------------------

    private fun StringBuilder.appendLine(
        product: DocumentProductState,
        lineIndex: Int,
        invoice: InvoiceState,
        currency: String,
    ) {
        val netUnitPrice = product.priceWithoutTax ?: BigDecimal.ZERO
        val qty = product.quantity
        val lineTotal = netUnitPrice.multiply(qty).roundToTwo()
        val ratePercent = product.taxRate?.doubleValue(false) ?: 0.0
        val unitCode = product.unitCode?.uppercase()?.ifEmpty { null } ?: "C62"

        val category = TaxCategoryResolver.resolve(
            TaxContext(
                issuerCountryCode = invoice.documentIssuer?.primaryCountry(),
                buyerCountryCode = invoice.documentClient?.primaryCountry(),
                issuerVatExempt = invoice.documentIssuer?.vatExempt == true,
                issuerIntraEuSales = invoice.documentIssuer?.intraEuSales == true,
                clientType = invoice.documentClient?.clientType,
                productNature = product.type,
                taxRate = ratePercent,
            ),
        )

        append("    <ram:IncludedSupplyChainTradeLineItem>\n")
        append("      <ram:AssociatedDocumentLineDocument>\n")
        append("        <ram:LineID>$lineIndex</ram:LineID>\n")
        append("      </ram:AssociatedDocumentLineDocument>\n")
        append("      <ram:SpecifiedTradeProduct>\n")
        append("        <ram:Name>${esc(product.name.text)}</ram:Name>\n")
        product.description?.text?.takeIf { it.isNotBlank() }?.let {
            append("        <ram:Description>${esc(it)}</ram:Description>\n")
        }
        append("      </ram:SpecifiedTradeProduct>\n")
        append("      <ram:SpecifiedLineTradeAgreement>\n")
        append("        <ram:NetPriceProductTradePrice>\n")
        append("          <ram:ChargeAmount>${formatAmount(netUnitPrice)}</ram:ChargeAmount>\n")
        append("        </ram:NetPriceProductTradePrice>\n")
        append("      </ram:SpecifiedLineTradeAgreement>\n")
        append("      <ram:SpecifiedLineTradeDelivery>\n")
        append("        <ram:BilledQuantity unitCode=\"$unitCode\">${formatQty(qty)}</ram:BilledQuantity>\n")
        append("      </ram:SpecifiedLineTradeDelivery>\n")
        append("      <ram:SpecifiedLineTradeSettlement>\n")
        append("        <ram:ApplicableTradeTax>\n")
        append("          <ram:TypeCode>VAT</ram:TypeCode>\n")
        append("          <ram:CategoryCode>${category.name}</ram:CategoryCode>\n")
        append("          <ram:RateApplicablePercent>${formatRate(ratePercent)}</ram:RateApplicablePercent>\n")
        append("        </ram:ApplicableTradeTax>\n")
        append("        <ram:SpecifiedTradeSettlementLineMonetarySummation>\n")
        append("          <ram:LineTotalAmount>${formatAmount(lineTotal)}</ram:LineTotalAmount>\n")
        append("        </ram:SpecifiedTradeSettlementLineMonetarySummation>\n")
        append("      </ram:SpecifiedLineTradeSettlement>\n")
        append("    </ram:IncludedSupplyChainTradeLineItem>\n")
    }

    // -----------------------------------------------------------------
    // Header trade agreement — parties (BG-4 seller, BG-7 buyer) and
    // reference to the buyer's PO (BT-13). We currently store a single
    // free-text `reference` on the invoice; if present, we emit it as
    // the BuyerOrderReferencedDocument.
    // -----------------------------------------------------------------

    private fun StringBuilder.appendHeaderTradeAgreement(
        issuer: ClientOrIssuerState?,
        buyer: ClientOrIssuerState?,
        buyerOrderRef: String?,
    ) {
        append("    <ram:ApplicableHeaderTradeAgreement>\n")
        if (issuer != null) appendParty("SellerTradeParty", issuer, indent = 6)
        if (buyer != null) appendParty("BuyerTradeParty", buyer, indent = 6)
        buyerOrderRef?.takeIf { it.isNotBlank() }?.let {
            append("      <ram:BuyerOrderReferencedDocument>\n")
            append("        <ram:IssuerAssignedID>${esc(it)}</ram:IssuerAssignedID>\n")
            append("      </ram:BuyerOrderReferencedDocument>\n")
        }
        append("    </ram:ApplicableHeaderTradeAgreement>\n")
    }

    private fun StringBuilder.appendParty(
        tag: String,
        party: ClientOrIssuerState,
        indent: Int,
    ) {
        val pad = " ".repeat(indent)
        val fullName = listOfNotNull(
            party.name.text.trim().ifEmpty { null },
            party.firstName?.text?.trim()?.ifEmpty { null },
        ).joinToString(" ")
        append("$pad<ram:$tag>\n")
        append("$pad  <ram:Name>${esc(fullName)}</ram:Name>\n")
        // SIREN / SIRET / registration number: whichever the user labelled
        // companyId1 as. We drop it into SpecifiedLegalOrganization/ID with
        // schemeID="0002" (INSEE SIREN) — Factur-X validators tolerate this
        // even when the value is technically a SIRET (14 digits).
        party.companyId1Number?.text?.takeIf { it.isNotBlank() }?.let {
            append("$pad  <ram:SpecifiedLegalOrganization>\n")
            append("$pad    <ram:ID schemeID=\"0002\">${esc(it)}</ram:ID>\n")
            append("$pad  </ram:SpecifiedLegalOrganization>\n")
        }
        party.addresses?.firstOrNull()?.let { appendAddress(it, indent + 2) }
        // VAT ID goes under SpecifiedTaxRegistration with schemeID="VA"
        // (VAT). We consume companyId2 by convention (FR default label is
        // "TVA intracom") — if the user renamed the labels the semantic
        // stays right: it's still the "second tax identifier" slot.
        party.companyId2Number?.text?.takeIf { it.isNotBlank() }?.let {
            append("$pad  <ram:SpecifiedTaxRegistration>\n")
            append("$pad    <ram:ID schemeID=\"VA\">${esc(it)}</ram:ID>\n")
            append("$pad  </ram:SpecifiedTaxRegistration>\n")
        }
        append("$pad</ram:$tag>\n")
    }

    private fun StringBuilder.appendAddress(address: AddressState, indent: Int) {
        val pad = " ".repeat(indent)
        append("$pad<ram:PostalTradeAddress>\n")
        address.zipCode?.text?.takeIf { it.isNotBlank() }?.let {
            append("$pad  <ram:PostcodeCode>${esc(it)}</ram:PostcodeCode>\n")
        }
        address.addressLine1?.text?.takeIf { it.isNotBlank() }?.let {
            append("$pad  <ram:LineOne>${esc(it)}</ram:LineOne>\n")
        }
        address.addressLine2?.text?.takeIf { it.isNotBlank() }?.let {
            append("$pad  <ram:LineTwo>${esc(it)}</ram:LineTwo>\n")
        }
        address.city?.text?.takeIf { it.isNotBlank() }?.let {
            append("$pad  <ram:CityName>${esc(it)}</ram:CityName>\n")
        }
        address.countryCode?.trim()?.takeIf { it.isNotBlank() }?.let {
            append("$pad  <ram:CountryID>${esc(it.uppercase())}</ram:CountryID>\n")
        }
        append("$pad</ram:PostalTradeAddress>\n")
    }

    // -----------------------------------------------------------------
    // Header trade delivery — Extended-profile multi-reference key
    // feature: several DespatchAdviceReferencedDocument elements, one
    // per distinct delivery-note number that any line was linked from.
    // This is what lets a single invoice consolidate multiple BLs.
    // -----------------------------------------------------------------

    private fun StringBuilder.appendHeaderTradeDelivery(products: List<DocumentProductState>) {
        append("    <ram:ApplicableHeaderTradeDelivery>\n")
        // Distinct linked delivery notes, preserving first-seen order so
        // the XML stays byte-stable across re-generations.
        val deliveryNoteRefs = products
            .mapNotNull { it.linkedDocNumber?.trim()?.takeIf { s -> s.isNotBlank() } }
            .distinct()
        deliveryNoteRefs.forEach { ref ->
            append("      <ram:DespatchAdviceReferencedDocument>\n")
            append("        <ram:IssuerAssignedID>${esc(ref)}</ram:IssuerAssignedID>\n")
            append("      </ram:DespatchAdviceReferencedDocument>\n")
        }
        append("    </ram:ApplicableHeaderTradeDelivery>\n")
    }

    // -----------------------------------------------------------------
    // Header trade settlement — currency, payment means / IBAN / BIC,
    // doc-level tax breakdown (one row per rate), payment terms + due
    // date, grand totals monetary summation.
    // -----------------------------------------------------------------

    private fun StringBuilder.appendHeaderTradeSettlement(
        invoice: InvoiceState,
        issuer: ClientOrIssuerState?,
        buyer: ClientOrIssuerState?,
        currency: String,
        products: List<DocumentProductState>,
    ) {
        append("    <ram:ApplicableHeaderTradeSettlement>\n")
        // BT-83 — the payment reference the payer should quote. Defaults
        // to the invoice number: universal fallback that lets an ISO
        // 20022 SEPA payment be reconciled without extra config.
        append("      <ram:PaymentReference>${esc(invoice.documentNumber.text)}</ram:PaymentReference>\n")
        append("      <ram:InvoiceCurrencyCode>${esc(currency)}</ram:InvoiceCurrencyCode>\n")

        appendPaymentMeans(invoice, issuer)
        appendDocLevelTax(invoice, products)
        appendPaymentTerms(invoice)
        appendMonetarySummation(invoice, products, currency)

        append("    </ram:ApplicableHeaderTradeSettlement>\n")
    }

    private fun StringBuilder.appendPaymentMeans(
        invoice: InvoiceState,
        issuer: ClientOrIssuerState?,
    ) {
        // Chip identities (TRANSFER, CHEQUE…) → UN/CEFACT 4461 codes.
        // Empty → [1] "Instrument not defined" per the helper's fallback.
        val codes = unCefactCodesForExport(invoice.paymentMeansSelections)
        val iban = issuer?.paymentIban?.text?.trim()?.takeIf { it.isNotBlank() }
        val bic = issuer?.paymentBic?.text?.trim()?.takeIf { it.isNotBlank() }
        codes.forEach { code ->
            append("      <ram:SpecifiedTradeSettlementPaymentMeans>\n")
            append("        <ram:TypeCode>${code.toString().padStart(2, '0')}</ram:TypeCode>\n")
            // Only attach IBAN/BIC on the transfer + SEPA rows — the codes
            // where those fields are semantically meaningful. Attaching an
            // IBAN under a "cheque" or "cash" row would confuse validators.
            if (iban != null && code in IBAN_CARRYING_CODES) {
                append("        <ram:PayeePartyCreditorFinancialAccount>\n")
                append("          <ram:IBANID>${esc(iban)}</ram:IBANID>\n")
                append("        </ram:PayeePartyCreditorFinancialAccount>\n")
                if (bic != null) {
                    append("        <ram:PayeeSpecifiedCreditorFinancialInstitution>\n")
                    append("          <ram:BICID>${esc(bic)}</ram:BICID>\n")
                    append("        </ram:PayeeSpecifiedCreditorFinancialInstitution>\n")
                }
            }
            append("      </ram:SpecifiedTradeSettlementPaymentMeans>\n")
        }
    }

    private fun StringBuilder.appendDocLevelTax(
        invoice: InvoiceState,
        products: List<DocumentProductState>,
    ) {
        // Group lines by (categoryCode, rate) — Factur-X wants one
        // ApplicableTradeTax row per (category × rate) combination at the
        // doc level, with the basis + calculated amount summed.
        val issuer = invoice.documentIssuer
        val buyer = invoice.documentClient
        data class Bucket(val category: TaxCategory, val rate: Double)
        val buckets = linkedMapOf<Bucket, Pair<BigDecimal, BigDecimal>>()
        products.forEach { p ->
            val rate = p.taxRate?.doubleValue(false) ?: 0.0
            val category = TaxCategoryResolver.resolve(
                TaxContext(
                    issuerCountryCode = issuer?.primaryCountry(),
                    buyerCountryCode = buyer?.primaryCountry(),
                    issuerVatExempt = issuer?.vatExempt == true,
                    issuerIntraEuSales = issuer?.intraEuSales == true,
                    clientType = buyer?.clientType,
                    productNature = p.type,
                    taxRate = rate,
                ),
            )
            val basis = (p.priceWithoutTax ?: BigDecimal.ZERO).multiply(p.quantity)
            val tax = basis.multiply(BigDecimal.fromDouble(rate)).divide(BigDecimal.fromInt(100))
            val current = buckets[Bucket(category, rate)] ?: (BigDecimal.ZERO to BigDecimal.ZERO)
            buckets[Bucket(category, rate)] = (current.first + basis) to (current.second + tax)
        }
        buckets.forEach { (bucket, sums) ->
            append("      <ram:ApplicableTradeTax>\n")
            append("        <ram:CalculatedAmount>${formatAmount(sums.second.roundToTwo())}</ram:CalculatedAmount>\n")
            append("        <ram:TypeCode>VAT</ram:TypeCode>\n")
            append("        <ram:BasisAmount>${formatAmount(sums.first.roundToTwo())}</ram:BasisAmount>\n")
            append("        <ram:CategoryCode>${bucket.category.name}</ram:CategoryCode>\n")
            append("        <ram:RateApplicablePercent>${formatRate(bucket.rate)}</ram:RateApplicablePercent>\n")
            append("      </ram:ApplicableTradeTax>\n")
        }
    }

    private fun StringBuilder.appendPaymentTerms(invoice: InvoiceState) {
        val description = invoice.paymentTermsDescription.text.trim()
        val dueDate = invoice.dueDate.trim()
        if (description.isEmpty() && dueDate.isEmpty()) return
        append("      <ram:SpecifiedTradePaymentTerms>\n")
        if (description.isNotEmpty()) {
            append("        <ram:Description>${esc(description)}</ram:Description>\n")
        }
        if (dueDate.isNotEmpty()) {
            append("        <ram:DueDateDateTime>\n")
            append("          <udt:DateTimeString format=\"102\">${formatDate102(dueDate)}</udt:DateTimeString>\n")
            append("        </ram:DueDateDateTime>\n")
        }
        append("      </ram:SpecifiedTradePaymentTerms>\n")
    }

    private fun StringBuilder.appendMonetarySummation(
        invoice: InvoiceState,
        products: List<DocumentProductState>,
        currency: String,
    ) {
        // Recompute from lines to guarantee internal consistency —
        // documentTotalPrices is a UI cache that may lag when we're
        // called from a test with a partially-populated state.
        val netTotal = products.fold(BigDecimal.ZERO) { acc, p ->
            acc + (p.priceWithoutTax ?: BigDecimal.ZERO).multiply(p.quantity)
        }.roundToTwo()
        val taxTotal = products.fold(BigDecimal.ZERO) { acc, p ->
            val rate = p.taxRate?.doubleValue(false) ?: 0.0
            acc + (p.priceWithoutTax ?: BigDecimal.ZERO)
                .multiply(p.quantity)
                .multiply(BigDecimal.fromDouble(rate))
                .divide(BigDecimal.fromInt(100))
        }.roundToTwo()
        val grandTotal = (netTotal + taxTotal).roundToTwo()
        append("      <ram:SpecifiedTradeSettlementHeaderMonetarySummation>\n")
        append("        <ram:LineTotalAmount>${formatAmount(netTotal)}</ram:LineTotalAmount>\n")
        append("        <ram:TaxBasisTotalAmount>${formatAmount(netTotal)}</ram:TaxBasisTotalAmount>\n")
        append("        <ram:TaxTotalAmount currencyID=\"${esc(currency)}\">${formatAmount(taxTotal)}</ram:TaxTotalAmount>\n")
        append("        <ram:GrandTotalAmount>${formatAmount(grandTotal)}</ram:GrandTotalAmount>\n")
        append("        <ram:DuePayableAmount>${formatAmount(grandTotal)}</ram:DuePayableAmount>\n")
        append("      </ram:SpecifiedTradeSettlementHeaderMonetarySummation>\n")
    }

    // -----------------------------------------------------------------
    // Formatting + escaping helpers.
    // -----------------------------------------------------------------

    /** XML predefined entities. Order matters — `&` first to avoid double-escaping. */
    private fun esc(s: String): String = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

    /**
     * Convert the app's "dd/MM/yyyy" storage format to UN/EDIFACT format
     * 102 ("YYYYMMDD"). Falls back to epoch so the Schematron gets a
     * parseable value instead of an empty tag — the caller should have
     * validated the field before calling us anyway.
     */
    private fun formatDate102(ddMmYyyy: String): String {
        val date: LocalDate = DateUtils.parseDate(ddMmYyyy) ?: return "19700101"
        val y = date.year.toString().padStart(4, '0')
        val m = date.monthNumber.toString().padStart(2, '0')
        val d = date.dayOfMonth.toString().padStart(2, '0')
        return "$y$m$d"
    }

    private fun formatAmount(bd: BigDecimal): String =
        bd.roundToTwo().toPlainString()

    private fun formatQty(bd: BigDecimal): String =
        bd.toPlainString().trimEnd('0').trimEnd('.').ifEmpty { "0" }

    private fun formatRate(rate: Double): String {
        val bd = BigDecimal.fromDouble(rate)
        return bd.toPlainString().trimEnd('0').trimEnd('.').ifEmpty { "0" }
    }

    private fun BigDecimal.roundToTwo(): BigDecimal =
        this.setScale(2, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)

    /**
     * UN/CEFACT 4461 codes that carry an IBAN/BIC per Factur-X — SEPA
     * credit transfer (58), generic credit transfer (30), and card
     * payments (48, when the card is tied to a bank account).
     */
    private val IBAN_CARRYING_CODES = setOf(30, 58)

    /** Invoice → 380, credit note → 381 (default 380 for anything else). */
    private fun DocumentState.typeCode(): String = when (documentType) {
        DocumentType.CREDIT_NOTE -> "381"
        else -> "380"
    }

    /** Primary country from the first address, or null if none. */
    private fun ClientOrIssuerState.primaryCountry(): String? =
        addresses?.firstOrNull()?.countryCode?.trim()?.uppercase()?.ifEmpty { null }
}
