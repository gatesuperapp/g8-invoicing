package com.a4a.g8invoicing.facturx

import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.models.ClientType
import com.a4a.g8invoicing.data.models.ProductNature
import com.a4a.g8invoicing.ui.states.AddressState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Behavioural spec for [CiiXmlBuilder]. We assert **presence** of the
 * key BT/BG elements rather than byte-for-byte equality to a golden
 * file — the latter would break every time we tweak whitespace or
 * attribute order without a semantic reason.
 */
class CiiXmlBuilderTest {

    private fun issuerFr() = ClientOrIssuerState(
        type = ClientOrIssuerType.DOCUMENT_ISSUER,
        name = TextFieldValue("SARL Truc"),
        addresses = listOf(
            AddressState(
                addressLine1 = TextFieldValue("12 rue Test"),
                city = TextFieldValue("Paris"),
                zipCode = TextFieldValue("75001"),
                countryCode = "FR",
            ),
        ),
        companyId1Number = TextFieldValue("551234567"), // SIREN
        companyId2Number = TextFieldValue("FR40551234567"), // TVA intracom
        paymentIban = TextFieldValue("FR7612345678901234567890189"),
        paymentBic = TextFieldValue("BNPAFRPPXXX"),
    )

    private fun clientFrPro() = ClientOrIssuerState(
        type = ClientOrIssuerType.DOCUMENT_CLIENT,
        name = TextFieldValue("Acme SAS"),
        addresses = listOf(
            AddressState(
                addressLine1 = TextFieldValue("34 avenue Client"),
                city = TextFieldValue("Lyon"),
                zipCode = TextFieldValue("69001"),
                countryCode = "FR",
            ),
        ),
        companyId1Number = TextFieldValue("999888777"),
        clientType = ClientType.PROFESSIONAL,
    )

    private fun goodsLine() = DocumentProductState(
        name = TextFieldValue("Widget"),
        description = TextFieldValue("Un widget d'exemple"),
        priceWithoutTax = BigDecimal.parseString("100"),
        taxRate = BigDecimal.parseString("20"),
        quantity = BigDecimal.parseString("2"),
        unitCode = "C62",
        type = ProductNature.GOODS,
    )

    // -----------------------------------------------------------------

    @Test
    fun `builds well-formed XML declaration + extended profile URN`() {
        val xml = CiiXmlBuilder.build(
            InvoiceState(
                documentNumber = TextFieldValue("F-2026-0001"),
                documentDate = "23/08/2026",
                dueDate = "22/09/2026",
                currency = TextFieldValue("EUR"),
                documentIssuer = issuerFr(),
                documentClient = clientFrPro(),
                documentProducts = listOf(goodsLine()),
            ),
        )
        // Header + root element + Extended profile URN — the three
        // signals that mark a Factur-X Extended-conformant CII document.
        assertTrue(xml.startsWith("""<?xml version="1.0" encoding="UTF-8"?>"""))
        assertTrue(xml.contains("<rsm:CrossIndustryInvoice"))
        assertTrue(
            xml.contains("urn:cen.eu:en16931:2017#conformant#urn:factur-x.eu:1p0:extended"),
            "Extended profile URN missing",
        )
        // Business process A1 = invoice for goods and services.
        assertTrue(xml.contains("<ram:ID>A1</ram:ID>"))
    }

    @Test
    fun `emits BT-1 number + BT-3 type code 380 + BT-2 date in format 102`() {
        val xml = CiiXmlBuilder.build(
            InvoiceState(
                documentNumber = TextFieldValue("F-2026-0001"),
                documentDate = "23/08/2026",
                currency = TextFieldValue("EUR"),
                documentIssuer = issuerFr(),
                documentClient = clientFrPro(),
                documentProducts = listOf(goodsLine()),
            ),
        )
        assertTrue(xml.contains("<ram:ID>F-2026-0001</ram:ID>"))
        assertTrue(xml.contains("<ram:TypeCode>380</ram:TypeCode>"))
        assertTrue(
            xml.contains("""<udt:DateTimeString format="102">20260823</udt:DateTimeString>"""),
            "BT-2 date not in format 102",
        )
    }

    @Test
    fun `renders seller and buyer parties with SIREN and VAT number`() {
        val xml = CiiXmlBuilder.build(
            InvoiceState(
                documentNumber = TextFieldValue("F-1"),
                documentDate = "23/08/2026",
                currency = TextFieldValue("EUR"),
                documentIssuer = issuerFr(),
                documentClient = clientFrPro(),
                documentProducts = listOf(goodsLine()),
            ),
        )
        assertTrue(xml.contains("<ram:SellerTradeParty>"))
        assertTrue(xml.contains("<ram:Name>SARL Truc</ram:Name>"))
        assertTrue(xml.contains("""<ram:ID schemeID="0002">551234567</ram:ID>"""))
        assertTrue(xml.contains("""<ram:ID schemeID="VA">FR40551234567</ram:ID>"""))
        assertTrue(xml.contains("<ram:BuyerTradeParty>"))
        assertTrue(xml.contains("<ram:Name>Acme SAS</ram:Name>"))
        assertTrue(xml.contains("<ram:CityName>Paris</ram:CityName>"))
        assertTrue(xml.contains("<ram:CityName>Lyon</ram:CityName>"))
        assertTrue(xml.contains("<ram:CountryID>FR</ram:CountryID>"))
    }

    @Test
    fun `line-level tax category resolves to S for national FR B2B`() {
        val xml = CiiXmlBuilder.build(
            InvoiceState(
                documentNumber = TextFieldValue("F-1"),
                documentDate = "23/08/2026",
                currency = TextFieldValue("EUR"),
                documentIssuer = issuerFr(),
                documentClient = clientFrPro(),
                documentProducts = listOf(goodsLine()),
            ),
        )
        assertTrue(xml.contains("<ram:CategoryCode>S</ram:CategoryCode>"))
        assertTrue(xml.contains("<ram:RateApplicablePercent>20</ram:RateApplicablePercent>"))
    }

    @Test
    fun `line-level tax category resolves to K for intra-EU B2B goods`() {
        val issuer = issuerFr().copy(intraEuSales = true)
        val buyerDe = clientFrPro().copy(
            addresses = listOf(
                AddressState(
                    city = TextFieldValue("Berlin"),
                    zipCode = TextFieldValue("10115"),
                    countryCode = "DE",
                ),
            ),
        )
        val xml = CiiXmlBuilder.build(
            InvoiceState(
                documentNumber = TextFieldValue("F-1"),
                documentDate = "23/08/2026",
                currency = TextFieldValue("EUR"),
                documentIssuer = issuer,
                documentClient = buyerDe,
                documentProducts = listOf(goodsLine()),
            ),
        )
        assertTrue(xml.contains("<ram:CategoryCode>K</ram:CategoryCode>"))
    }

    @Test
    fun `multiple distinct delivery-note refs surface as DespatchAdvice entries`() {
        // Extended profile-only feature: several DespatchAdviceReferencedDocument
        // elements. This is the reason we chose Extended over Basic.
        val lines = listOf(
            goodsLine().copy(linkedDocNumber = "BL-001"),
            goodsLine().copy(linkedDocNumber = "BL-002"),
            goodsLine().copy(linkedDocNumber = "BL-001"), // duplicate → dedup
            goodsLine().copy(linkedDocNumber = "BL-003"),
        )
        val xml = CiiXmlBuilder.build(
            InvoiceState(
                documentNumber = TextFieldValue("F-1"),
                documentDate = "23/08/2026",
                currency = TextFieldValue("EUR"),
                documentIssuer = issuerFr(),
                documentClient = clientFrPro(),
                documentProducts = lines,
            ),
        )
        assertTrue(xml.contains("<ram:IssuerAssignedID>BL-001</ram:IssuerAssignedID>"))
        assertTrue(xml.contains("<ram:IssuerAssignedID>BL-002</ram:IssuerAssignedID>"))
        assertTrue(xml.contains("<ram:IssuerAssignedID>BL-003</ram:IssuerAssignedID>"))
        // 3 refs, not 4 — dedup preserved.
        val count = "<ram:DespatchAdviceReferencedDocument>".toRegex().findAll(xml).count()
        assertTrue(count == 3, "expected 3 dedup'd DespatchAdvice entries, got $count")
    }

    @Test
    fun `IBAN attached only to transfer + SEPA payment-means rows`() {
        val xml = CiiXmlBuilder.build(
            InvoiceState(
                documentNumber = TextFieldValue("F-1"),
                documentDate = "23/08/2026",
                currency = TextFieldValue("EUR"),
                documentIssuer = issuerFr(),
                documentClient = clientFrPro(),
                documentProducts = listOf(goodsLine()),
                paymentMeansSelections = setOf("TRANSFER", "CHEQUE"),
            ),
        )
        assertTrue(xml.contains("<ram:TypeCode>30</ram:TypeCode>"), "TRANSFER=30 missing")
        assertTrue(xml.contains("<ram:TypeCode>42</ram:TypeCode>"), "CHEQUE=42 missing")
        // IBAN appears only under the TRANSFER row — presence assertion
        // + one-occurrence check (the block only holds one IBANID line).
        val ibanOccurrences = "<ram:IBANID>".toRegex().findAll(xml).count()
        assertTrue(
            ibanOccurrences == 1,
            "IBAN should appear exactly once (under TRANSFER), got $ibanOccurrences",
        )
    }

    @Test
    fun `doc-level tax breakdown groups lines by rate`() {
        val lines = listOf(
            goodsLine().copy(taxRate = BigDecimal.parseString("20")),
            goodsLine().copy(taxRate = BigDecimal.parseString("20")),
            goodsLine().copy(taxRate = BigDecimal.parseString("5.5")),
        )
        val xml = CiiXmlBuilder.build(
            InvoiceState(
                documentNumber = TextFieldValue("F-1"),
                documentDate = "23/08/2026",
                currency = TextFieldValue("EUR"),
                documentIssuer = issuerFr(),
                documentClient = clientFrPro(),
                documentProducts = lines,
            ),
        )
        // Two doc-level ApplicableTradeTax rows (one per rate bucket),
        // not three — the two 20% lines merge.
        val docTaxRows = "<ram:BasisAmount>".toRegex().findAll(xml).count()
        assertTrue(
            docTaxRows == 2,
            "expected 2 tax breakdown rows (20% + 5.5%), got $docTaxRows",
        )
    }

    @Test
    fun `payment terms and due date surface when present`() {
        val xml = CiiXmlBuilder.build(
            InvoiceState(
                documentNumber = TextFieldValue("F-1"),
                documentDate = "23/08/2026",
                dueDate = "22/09/2026",
                currency = TextFieldValue("EUR"),
                documentIssuer = issuerFr(),
                documentClient = clientFrPro(),
                documentProducts = listOf(goodsLine()),
                paymentTermsDescription = TextFieldValue("Paiement à 30 jours"),
            ),
        )
        assertTrue(xml.contains("<ram:Description>Paiement à 30 jours</ram:Description>"))
        assertTrue(
            xml.contains("""<udt:DateTimeString format="102">20260922</udt:DateTimeString>"""),
            "BT-9 due date in format 102 missing",
        )
    }

    @Test
    fun `payment terms omitted entirely when description and due date are both blank`() {
        val xml = CiiXmlBuilder.build(
            InvoiceState(
                documentNumber = TextFieldValue("F-1"),
                documentDate = "23/08/2026",
                dueDate = "",
                currency = TextFieldValue("EUR"),
                documentIssuer = issuerFr(),
                documentClient = clientFrPro(),
                documentProducts = listOf(goodsLine()),
                paymentTermsDescription = TextFieldValue(""),
            ),
        )
        assertFalse(xml.contains("<ram:SpecifiedTradePaymentTerms>"))
    }

    @Test
    fun `XML escapes ampersand and special characters in party names`() {
        val issuer = issuerFr().copy(name = TextFieldValue("Truc & Muche <SARL>"))
        val xml = CiiXmlBuilder.build(
            InvoiceState(
                documentNumber = TextFieldValue("F-1"),
                documentDate = "23/08/2026",
                currency = TextFieldValue("EUR"),
                documentIssuer = issuer,
                documentClient = clientFrPro(),
                documentProducts = listOf(goodsLine()),
            ),
        )
        assertTrue(xml.contains("Truc &amp; Muche &lt;SARL&gt;"))
        assertFalse(xml.contains("Truc & Muche <SARL>"))
    }

    @Test
    fun `monetary summation recomputes from lines regardless of cached totals`() {
        // Two lines × 100 EUR × qty 2 = 400 net, 20% VAT = 80, grand 480.
        val xml = CiiXmlBuilder.build(
            InvoiceState(
                documentNumber = TextFieldValue("F-1"),
                documentDate = "23/08/2026",
                currency = TextFieldValue("EUR"),
                documentIssuer = issuerFr(),
                documentClient = clientFrPro(),
                documentProducts = listOf(goodsLine(), goodsLine()),
            ),
        )
        // Amounts may or may not carry trailing ".00" depending on the
        // ionspin BigDecimal representation — regex tolerates both.
        val netLine = Regex("""<ram:LineTotalAmount>400(?:\.0+)?</ram:LineTotalAmount>""")
        val taxLine = Regex("""<ram:TaxTotalAmount currencyID="EUR">80(?:\.0+)?</ram:TaxTotalAmount>""")
        val grandLine = Regex("""<ram:GrandTotalAmount>480(?:\.0+)?</ram:GrandTotalAmount>""")
        val dueLine = Regex("""<ram:DuePayableAmount>480(?:\.0+)?</ram:DuePayableAmount>""")
        assertTrue(netLine.containsMatchIn(xml), "expected net line total 400, got:\n$xml")
        assertTrue(taxLine.containsMatchIn(xml), "expected tax total 80")
        assertTrue(grandLine.containsMatchIn(xml), "expected grand total 480")
        assertTrue(dueLine.containsMatchIn(xml), "expected due payable 480")
    }
}
