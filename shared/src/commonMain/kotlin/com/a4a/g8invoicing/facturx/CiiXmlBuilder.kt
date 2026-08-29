package com.a4a.g8invoicing.facturx

import com.a4a.g8invoicing.data.models.ClientType
import com.a4a.g8invoicing.data.models.ProductNature
import com.a4a.g8invoicing.data.models.flattenPaymentLabel
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
     * [paymentMeansLabels] is the chipId → localized label map used to
     * flatten [InvoiceState.paymentMeansSegments] into the `<ram:Information>`
     * (BT-82) free-text field. Callers on Android / Desktop pass the same
     * map they hand to PdfGenerator (built from
     * `PaymentMeans.entries.associate { chipId to stringResource(labelRes) }`).
     * Left null (as in tests) → the payment-means portion of BT-82 is
     * skipped.
     *
     * [bankInfoText] is the already-flattened bank-details block — what the
     * user sees at the bottom of the invoice when they edit the "coordonnées
     * bancaires" text (IBAN + BIC + any custom prose). Callers resolve the
     * IBAN/BIC identifier labels + IBAN/BIC values themselves and pass the
     * result of [flattenPaymentBank] here. Concatenated after the payment-
     * means list in the same `<ram:Information>` block so a CII-only reader
     * sees the full payment instructions.
     *
     * Every semantic decision (tax category per line, invoice type code,
     * currency, ref list dedup) is deterministic given the invoice state.
     * No I/O, no clock — this can run in a test with a hard-coded fixture
     * and produce byte-stable output.
     */
    fun build(
        invoice: InvoiceState,
        paymentMeansLabels: Map<String, String>? = null,
        bankInfoText: String? = null,
    ): String = buildString {
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
        appendHeaderTradeDelivery(products, invoice)
        appendHeaderTradeSettlement(
            invoice,
            issuer,
            buyer,
            currency,
            products,
            paymentMeansLabels,
            bankInfoText,
        )
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
        // BT-23 Business Process. The FR national Schematron (BR-FR-08)
        // restricts this to B1-B9 / S1-S9 / M1-M9 and rejects the older
        // "A1" default. S1 = simple standard invoice, which is what a
        // hand-created invoice out of the picker maps to.
        append("    <ram:BusinessProcessSpecifiedDocumentContextParameter>\n")
        append("      <ram:ID>S1</ram:ID>\n")
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
        // FR national Schematron (BR-FR-05) requires three legal-mention
        // notes on every invoice, keyed by SubjectCode:
        //   PMT — frais de recouvrement (recovery fees)
        //   PMD — pénalités de retard (late-payment penalties)
        //   AAB — escompte pour paiement anticipé (early-payment discount)
        // Each maps to a dedicated field on InvoiceState — the 3-row picker
        // lets the user edit them independently. When one is blank we fall
        // back to LEGAL_MENTION_FALLBACK so the note is never emitted empty.
        val subjectMap: List<Pair<String, String>> = listOf(
            "PMT" to invoice.paymentTermsRecoveryFees.text.trim().ifEmpty { LEGAL_MENTION_FALLBACK },
            "PMD" to invoice.paymentTermsLateFees.text.trim().ifEmpty { LEGAL_MENTION_FALLBACK },
            "AAB" to invoice.paymentTermsDiscount.text.trim().ifEmpty { LEGAL_MENTION_FALLBACK },
        )
        subjectMap.forEach { (subjectCode, content) ->
            append("    <ram:IncludedNote>\n")
            append("      <ram:Content>${esc(content)}</ram:Content>\n")
            append("      <ram:SubjectCode>$subjectCode</ram:SubjectCode>\n")
            append("    </ram:IncludedNote>\n")
        }
        append("  </rsm:ExchangedDocument>\n")
    }

    /**
     * Last-resort text when one of the 3 payment-terms fields is blank —
     * so <ram:Content> never ships empty and each SubjectCode still fires
     * BR-FR-05 as present. The createNew() seed populates each field from
     * `payment_terms_*_default`, so this only bites for legacy invoices.
     */
    private const val LEGAL_MENTION_FALLBACK =
        "Non renseigné."

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
        val rateBd = product.taxRate ?: BigDecimal.ZERO
        val unitCode = product.unitCode?.uppercase()?.ifEmpty { null } ?: "C62"

        val category = TaxCategoryResolver.resolve(
            TaxContext(
                issuerCountryCode = invoice.documentIssuer?.primaryCountry(),
                buyerCountryCode = invoice.documentClient?.primaryCountry(),
                issuerVatExempt = invoice.documentIssuer?.vatExempt == true,
                issuerIntraEuSales = invoice.documentIssuer?.intraEuSales == true,
                clientType = invoice.documentClient?.clientType,
                productNature = product.type,
                taxRate = rateBd.doubleValue(false),
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
        // BT-X-116 per-line delivery-note reference (Extended profile).
        // Header-level cardinality is 0..1 in the CII schema, so multi-BL
        // has to be attributed line-by-line. Each product ligne carries its
        // source BL number via linkedDocNumber (populated when the invoice
        // was built from delivery notes) — we surface it here so a reader
        // can trace each line back to the exact bon de livraison.
        product.linkedDocNumber?.trim()?.takeIf { it.isNotBlank() }?.let { ref ->
            append("        <ram:DeliveryNoteReferencedDocument>\n")
            append("          <ram:IssuerAssignedID>${esc(ref)}</ram:IssuerAssignedID>\n")
            product.linkedDate?.trim()?.takeIf { it.isNotBlank() }?.let { date ->
                append("          <ram:FormattedIssueDateTime>\n")
                append("            <qdt:DateTimeString format=\"102\">${formatDate102(date)}</qdt:DateTimeString>\n")
                append("          </ram:FormattedIssueDateTime>\n")
            }
            append("        </ram:DeliveryNoteReferencedDocument>\n")
        }
        append("      </ram:SpecifiedLineTradeDelivery>\n")
        append("      <ram:SpecifiedLineTradeSettlement>\n")
        append("        <ram:ApplicableTradeTax>\n")
        append("          <ram:TypeCode>VAT</ram:TypeCode>\n")
        append("          <ram:CategoryCode>${category.name}</ram:CategoryCode>\n")
        append("          <ram:RateApplicablePercent>${formatRateBd(rateBd)}</ram:RateApplicablePercent>\n")
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
        val partyCountry = party.primaryCountry()
        // BT-30 (SpecifiedLegalOrganization/ID) is emitted under schemeID
        // "0002" (INSEE SIREN). BR-FR-10 + BR-FR-32 require exactly 9
        // digits under that scheme, so if the user entered a SIRET (14
        // digits — SIREN + 5-digit NIC) we extract the leading SIREN
        // portion. User's data stays intact in companyId1Number for
        // display; only the XML emit is normalised. Skipped entirely for
        // a party with no companyId1Number (typically a B2C particulier).
        val siren = extractSiren(party.companyId1Number?.text)
        val email = party.emails
            ?.firstOrNull()
            ?.email?.text?.trim()
            ?.takeIf { it.isNotBlank() }

        append("$pad<ram:$tag>\n")
        append("$pad  <ram:Name>${esc(fullName)}</ram:Name>\n")
        if (siren != null) {
            append("$pad  <ram:SpecifiedLegalOrganization>\n")
            append("$pad    <ram:ID schemeID=\"0002\">${esc(siren)}</ram:ID>\n")
            append("$pad  </ram:SpecifiedLegalOrganization>\n")
        }
        party.addresses?.firstOrNull()?.let { appendAddress(it, indent + 2) }
        // BT-34 (seller) / BT-49 (buyer) electronic address for e-invoice
        // routing. FR national profile (BR-FR-13 / BR-FR-12) makes it
        // mandatory. Peppol EAS mapping we support:
        //   * schemeID "0002" (INSEE SIREN) — business parties. Chorus
        //     Pro / PPF route on this in France.
        //   * schemeID "EM" (email) — the natural electronic address for
        //     a particulier client, or a business without a SIREN. Not
        //     PPF-routable but perfectly valid CII / EN 16931 for direct
        //     delivery through any other channel.
        // When neither is available the pre-flight validator surfaces
        // "add an email" (INDIVIDUAL) or "SIREN missing" (PROFESSIONAL),
        // so the block is genuinely skipped only when the user has
        // explicitly acknowledged a stripped-down invoice.
        when {
            siren != null -> {
                append("$pad  <ram:URIUniversalCommunication>\n")
                append("$pad    <ram:URIID schemeID=\"0002\">${esc(siren)}</ram:URIID>\n")
                append("$pad  </ram:URIUniversalCommunication>\n")
            }
            email != null -> {
                append("$pad  <ram:URIUniversalCommunication>\n")
                append("$pad    <ram:URIID schemeID=\"EM\">${esc(email)}</ram:URIID>\n")
                append("$pad  </ram:URIUniversalCommunication>\n")
            }
        }
        // VAT ID goes under SpecifiedTaxRegistration with schemeID="VA"
        // (VAT). We consume companyId2 by convention (FR default label is
        // "TVA intracom") — if the user renamed the labels the semantic
        // stays right: it's still the "second tax identifier" slot.
        // BR-CO-09: the value must carry an ISO 3166-1 alpha-2 country
        // prefix (FR12345678900). We only auto-prepend when the raw value
        // isn't already prefixed with a valid 2-letter code, so a user who
        // typed the full VAT stays intact.
        party.companyId2Number?.text?.trim()?.takeIf { it.isNotBlank() }?.let { rawVat ->
            val prefixed = normalizeVatId(rawVat, partyCountry)
            append("$pad  <ram:SpecifiedTaxRegistration>\n")
            append("$pad    <ram:ID schemeID=\"VA\">${esc(prefixed)}</ram:ID>\n")
            append("$pad  </ram:SpecifiedTaxRegistration>\n")
        }
        append("$pad</ram:$tag>\n")
    }

    /**
     * Peel the SIREN out of whatever the user typed in companyId1Number.
     * Strips separators (spaces, dots, dashes) — a "123 456 789" entry
     * becomes "123456789". Then:
     *   • exactly 9 digits → return as-is (canonical SIREN)
     *   • exactly 14 digits → return the leading 9 (SIRET without NIC)
     *   • anything else → null (validator will surface the SIREN issue)
     */
    private fun extractSiren(raw: String?): String? {
        val digits = raw?.trim()?.filter { it.isDigit() } ?: return null
        return when (digits.length) {
            9 -> digits
            14 -> digits.substring(0, 9)
            else -> null
        }
    }

    /**
     * BR-CO-09 requires the VAT identifier to start with an ISO 3166-1
     * alpha-2 country code. If the user typed "12345678900", we prepend
     * their party's country ("FR" fallback); if they typed "FR12345678900"
     * or "EL12345…", we keep it as-is. Whitespace + non-alphanumeric noise
     * is stripped either way.
     */
    private fun normalizeVatId(raw: String, partyCountry: String?): String {
        val cleaned = raw.filter { it.isLetterOrDigit() }
        if (cleaned.isEmpty()) return raw
        val first = cleaned.substring(0, minOf(2, cleaned.length))
        val startsWithCountry = first.length == 2 && first.all { it.isLetter() }
        return if (startsWithCountry) cleaned.uppercase()
        else (partyCountry ?: "FR") + cleaned.uppercase()
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
    // Header trade delivery. Per-BL references are emitted line-by-line
    // via <ram:DeliveryNoteReferencedDocument> inside SpecifiedLineTrade­-
    // Delivery (see appendLine) — that's the Extended-profile pattern
    // for multi-BL invoices, since the header-level references have
    // cardinality 0..1 in the CII schema. At the header level we only
    // carry BT-72 (actual delivery date), defaulted to the issue date,
    // so the element isn't empty (PEPPOL-EN16931-R008).
    // -----------------------------------------------------------------

    private fun StringBuilder.appendHeaderTradeDelivery(
        products: List<DocumentProductState>,
        invoice: InvoiceState,
    ) {
        append("    <ram:ApplicableHeaderTradeDelivery>\n")
        append("      <ram:ActualDeliverySupplyChainEvent>\n")
        append("        <ram:OccurrenceDateTime>\n")
        append("          <udt:DateTimeString format=\"102\">${formatDate102(invoice.documentDate)}</udt:DateTimeString>\n")
        append("        </ram:OccurrenceDateTime>\n")
        append("      </ram:ActualDeliverySupplyChainEvent>\n")
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
        paymentMeansLabels: Map<String, String>?,
        bankInfoText: String?,
    ) {
        append("    <ram:ApplicableHeaderTradeSettlement>\n")
        // BT-83 — the payment reference the payer should quote. Defaults
        // to the invoice number: universal fallback that lets an ISO
        // 20022 SEPA payment be reconciled without extra config.
        append("      <ram:PaymentReference>${esc(invoice.documentNumber.text)}</ram:PaymentReference>\n")
        append("      <ram:InvoiceCurrencyCode>${esc(currency)}</ram:InvoiceCurrencyCode>\n")

        appendPaymentMeans(invoice, issuer, paymentMeansLabels, bankInfoText)
        appendDocLevelTax(invoice, products)
        appendPaymentTerms(invoice)
        appendMonetarySummation(invoice, products, currency)

        append("    </ram:ApplicableHeaderTradeSettlement>\n")
    }

    private fun StringBuilder.appendPaymentMeans(
        invoice: InvoiceState,
        issuer: ClientOrIssuerState?,
        paymentMeansLabels: Map<String, String>?,
        bankInfoText: String?,
    ) {
        // Chip identities (TRANSFER, CHEQUE…) → UN/CEFACT 4461 codes.
        // Empty → [1] "Instrument not defined" per the helper's fallback.
        val codes = unCefactCodesForExport(invoice.paymentMeansSelections)
        val iban = issuer?.paymentIban?.text?.trim()?.takeIf { it.isNotBlank() }
        val bic = issuer?.paymentBic?.text?.trim()?.takeIf { it.isNotBlank() }
        // CII-SR-467: every SpecifiedTradeSettlementPaymentMeans block must
        // share the same TypeCode. That mirrors the EN 16931 semantic BG-16
        // 0..1 cardinality — one payment METHOD per invoice, even though
        // several account blocks are legal (multi-IBAN for the same
        // method). If the user has picked several chips, we fall back to
        // code 1 ("Instrument not defined") and put the localised list of
        // accepted methods in BT-82 Information so a CII-only reader still
        // sees the intent.
        val effectiveCode: Int = codes.singleOrNull() ?: 1
        val attachIban = iban != null && effectiveCode in IBAN_CARRYING_CODES
        // BT-82 Information — free-text payment instructions. We merge two
        // things the user can edit on the PDF into a single block, since
        // CII allows only one Information child per SpecifiedTrade­Settlement­
        // PaymentMeans (0..1):
        //   1. the accepted-methods list (flattened paymentMeansSegments)
        //   2. the bank-details prose (flattened paymentBankSegments —
        //      passed in pre-flattened by the caller since it needs
        //      locale-resolved IBAN/BIC labels).
        // Empty lines get stripped so a doc with only one of the two still
        // renders cleanly.
        val meansText = paymentMeansLabels
            ?.let { flattenPaymentLabel(invoice.paymentMeansSegments, it) }
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        val bankText = bankInfoText?.trim()?.takeIf { it.isNotEmpty() }
        val information = listOfNotNull(meansText, bankText).joinToString("\n").ifEmpty { null }

        append("      <ram:SpecifiedTradeSettlementPaymentMeans>\n")
        // UN/CEFACT 4461 codelist expects the integer value without zero
        // padding — "1" not "01". FX-SCH-A-001008 rejects the padded form
        // because the enumeration in FACTUR-X_EXTENDED_codedb.xml lists
        // codes as bare integers.
        append("        <ram:TypeCode>$effectiveCode</ram:TypeCode>\n")
        if (information != null) {
            append("        <ram:Information>${esc(information)}</ram:Information>\n")
        }
        if (attachIban) {
            append("        <ram:PayeePartyCreditorFinancialAccount>\n")
            append("          <ram:IBANID>${esc(iban!!)}</ram:IBANID>\n")
            append("        </ram:PayeePartyCreditorFinancialAccount>\n")
            if (bic != null) {
                append("        <ram:PayeeSpecifiedCreditorFinancialInstitution>\n")
                append("          <ram:BICID>${esc(bic)}</ram:BICID>\n")
                append("        </ram:PayeeSpecifiedCreditorFinancialInstitution>\n")
            }
        }
        append("      </ram:SpecifiedTradeSettlementPaymentMeans>\n")
    }

    /**
     * Bucket key + rounded totals for one row of the doc-level tax
     * breakdown. Materialized once so [appendDocLevelTax] and
     * [appendMonetarySummation] emit numerically identical figures — that
     * removes BR-CO-16 drift between the sum of BT-117 rows and the
     * header BT-110.
     */
    private data class TaxBucketRow(
        val category: TaxCategory,
        val rate: BigDecimal,
        val basis: BigDecimal,
        val calculated: BigDecimal,
    )

    private fun aggregateTaxBuckets(
        invoice: InvoiceState,
        products: List<DocumentProductState>,
    ): List<TaxBucketRow> {
        val issuer = invoice.documentIssuer
        val buyer = invoice.documentClient
        data class Key(val category: TaxCategory, val rate: BigDecimal)
        val raw = linkedMapOf<Key, BigDecimal>()
        products.forEach { p ->
            // Keep the rate as BigDecimal for the sum + emit path — the
            // Double conversion is only needed to feed TaxCategoryResolver
            // (which does coarse comparisons like `> 0.0` for category
            // choice, so Double precision drift is harmless there).
            val rateBd = p.taxRate ?: BigDecimal.ZERO
            val category = TaxCategoryResolver.resolve(
                TaxContext(
                    issuerCountryCode = issuer?.primaryCountry(),
                    buyerCountryCode = buyer?.primaryCountry(),
                    issuerVatExempt = issuer?.vatExempt == true,
                    issuerIntraEuSales = issuer?.intraEuSales == true,
                    clientType = buyer?.clientType,
                    productNature = p.type,
                    taxRate = rateBd.doubleValue(false),
                ),
            )
            val basis = (p.priceWithoutTax ?: BigDecimal.ZERO).multiply(p.quantity)
            val key = Key(category, rateBd)
            raw[key] = (raw[key] ?: BigDecimal.ZERO) + basis
        }
        return raw.map { (key, basisSum) ->
            val basisRounded = basisSum.roundToTwo()
            // BR-FXEXT-S-09b recomputes as `round(BasisAmount * rate / 100)`
            // with a 0.01 × line-count tolerance. We match the formula
            // exactly: pure BigDecimal all the way (no Double intermediate),
            // multiply first (product is always terminating), divide by 100
            // last (shifts decimal place, no precision loss), then round.
            val calculated = basisRounded
                .multiply(key.rate)
                .divide(BigDecimal.fromInt(100))
                .roundToTwo()
            TaxBucketRow(key.category, key.rate, basisRounded, calculated)
        }
    }

    private fun StringBuilder.appendDocLevelTax(
        invoice: InvoiceState,
        products: List<DocumentProductState>,
    ) {
        val issuerCountry = invoice.documentIssuer?.primaryCountry()
        val buyerCountry = invoice.documentClient?.primaryCountry()
        val userExemptionText = invoice.vatExemptionText?.text?.trim()?.ifEmpty { null }
        aggregateTaxBuckets(invoice, products).forEach { row ->
            // BR-E-10 / BR-AE-10 / BR-IC-10 / BR-G-10: an ApplicableTradeTax
            // with any non-standard VAT category MUST carry either BT-120
            // (ExemptionReason free text) or BT-121 (ExemptionReasonCode).
            // We prefer the coded form — language- and country-neutral,
            // deductible from the category alone for AE / K / G. For E the
            // motif depends on the operation (293 B franchise vs 261-4
            // formation vs 275 exports), so we read the user-set text from
            // the invoice; the preflight validator blocks export when E is
            // used with no text.
            val reason = exemptionReasonFor(
                category = row.category,
                issuerCountry = issuerCountry,
                buyerCountry = buyerCountry,
                userExemptionText = userExemptionText,
            )
            append("      <ram:ApplicableTradeTax>\n")
            append("        <ram:CalculatedAmount>${formatAmount(row.calculated)}</ram:CalculatedAmount>\n")
            append("        <ram:TypeCode>VAT</ram:TypeCode>\n")
            // CII schema order: ExemptionReason (BT-120) after TypeCode,
            // before BasisAmount.
            if (reason.text != null) {
                append("        <ram:ExemptionReason>${esc(reason.text)}</ram:ExemptionReason>\n")
            }
            append("        <ram:BasisAmount>${formatAmount(row.basis)}</ram:BasisAmount>\n")
            append("        <ram:CategoryCode>${row.category.name}</ram:CategoryCode>\n")
            // ExemptionReasonCode (BT-121) after CategoryCode, before Rate.
            if (reason.code != null) {
                append("        <ram:ExemptionReasonCode>${esc(reason.code)}</ram:ExemptionReasonCode>\n")
            }
            append("        <ram:RateApplicablePercent>${formatRateBd(row.rate)}</ram:RateApplicablePercent>\n")
            append("      </ram:ApplicableTradeTax>\n")
        }
    }

    /** BT-120 text and/or BT-121 code emitted next to the CategoryCode. */
    private data class ExemptionReason(val code: String?, val text: String?)

    // Categories AE / K / G map directly to VATEX-EU-* codes derivable from
    // the category alone — no user input needed and the code is neutral to
    // language and reader country. Category E stays fundamentally user-set:
    // the motif tracks the operation, not the issuer's country, so we read
    // it from InvoiceState.vatExemptionText (fed by CreditNoteState too).
    // Standard rate (S) and zero-rated (Z) don't require an exemption reason.
    private fun exemptionReasonFor(
        category: TaxCategory,
        issuerCountry: String?,
        buyerCountry: String?,
        userExemptionText: String?,
    ): ExemptionReason = when (category) {
        TaxCategory.AE -> {
            // Autoliquidation domestique FR (art. 283-2 CGI) has its own
            // national code when both parties are French; otherwise the
            // generic EU code covers all cross-border B2B services.
            val bothFr = issuerCountry?.uppercase() == "FR" &&
                buyerCountry?.uppercase() == "FR"
            ExemptionReason(if (bothFr) "VATEX-FR-AE" else "VATEX-EU-AE", null)
        }
        TaxCategory.K -> ExemptionReason("VATEX-EU-IC", null)
        TaxCategory.G -> ExemptionReason("VATEX-EU-G", null)
        TaxCategory.E -> ExemptionReason(null, userExemptionText)
        TaxCategory.S, TaxCategory.Z -> ExemptionReason(null, null)
    }

    private fun StringBuilder.appendPaymentTerms(invoice: InvoiceState) {
        // BT-20 Description = human-readable single-block version of the
        // payment conditions. Join the 3 sub-mentions with "\n" so a CII
        // reader that doesn't parse BR-FR-05 subject codes still gets the
        // full text under BT-20 (redundant with IncludedNote, but that's
        // how BT-20 vs BG-1 split works in EN 16931).
        val description = listOf(
            invoice.paymentTermsRecoveryFees.text.trim(),
            invoice.paymentTermsLateFees.text.trim(),
            invoice.paymentTermsDiscount.text.trim(),
        ).filter { it.isNotEmpty() }.joinToString(" ")
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
        // LineTotal + TaxBasisTotal + TaxTotal + Grand — all derived from
        // the same TaxBucketRow list the doc-level tax breakdown uses, so
        // BT-110 = sum(BT-117) exactly (BR-CO-16). LineTotalAmount stays
        // computed off the raw per-line totals since the FR Schematron
        // (BR-CO-10) checks that against sum(BT-131).
        val netTotal = products.fold(BigDecimal.ZERO) { acc, p ->
            acc + (p.priceWithoutTax ?: BigDecimal.ZERO).multiply(p.quantity)
        }.roundToTwo()
        val buckets = aggregateTaxBuckets(invoice, products)
        val taxTotal = buckets.fold(BigDecimal.ZERO) { acc, row -> acc + row.calculated }
        val taxBasisTotal = buckets.fold(BigDecimal.ZERO) { acc, row -> acc + row.basis }
        val grandTotal = (taxBasisTotal + taxTotal).roundToTwo()
        append("      <ram:SpecifiedTradeSettlementHeaderMonetarySummation>\n")
        append("        <ram:LineTotalAmount>${formatAmount(netTotal)}</ram:LineTotalAmount>\n")
        append("        <ram:TaxBasisTotalAmount>${formatAmount(taxBasisTotal)}</ram:TaxBasisTotalAmount>\n")
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

    /**
     * VAT rate as plain decimal with trailing zeros trimmed ("20", "5.5",
     * "2.1"). Kept BigDecimal-native so 0.9 / 2.1 don't drift through a
     * Double conversion — that drift is what BR-FXEXT-S-09b caught on the
     * previous iteration.
     */
    private fun formatRateBd(rate: BigDecimal): String {
        val plain = rate.toPlainString()
        return if (plain.contains('.')) {
            plain.trimEnd('0').trimEnd('.').ifEmpty { "0" }
        } else plain
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
