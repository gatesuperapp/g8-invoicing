package com.a4a.g8invoicing

import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.util.DateUtils
import com.a4a.g8invoicing.fakes.FakeCreditNoteDataSource
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CreditNoteDataSourceTest {

    private lateinit var dataSource: FakeCreditNoteDataSource

    @BeforeTest
    fun setup() {
        dataSource = FakeCreditNoteDataSource()
    }

    // ============= CREATION TESTS =============

    @Test
    fun createCreditNote_createsEmptyDocument() = runTest {
        val creditNoteId = dataSource.createNew()

        assertNotNull(creditNoteId)
        assertEquals(1, dataSource.getCreditNoteCount())
        val creditNote = dataSource.fetch(creditNoteId)
        assertNotNull(creditNote)
        assertEquals(DocumentTag.DRAFT, creditNote.documentTag)
    }

    @Test
    fun createCreditNote_setsDateToToday() = runTest {
        val creditNoteId = dataSource.createNew()

        val creditNote = dataSource.fetch(creditNoteId)
        val todayFormatted = DateUtils.getCurrentDateFormatted()

        assertNotNull(creditNote)
        assertEquals(todayFormatted, creditNote.documentDate)
    }

    @Test
    fun createCreditNote_setsCreatedDate() = runTest {
        val creditNoteId = dataSource.createNew()

        val creditNote = dataSource.fetch(creditNoteId)

        assertNotNull(creditNote?.createdDate)
        assertTrue(creditNote?.createdDate?.isNotBlank() == true)
    }

    @Test
    fun createMultipleCreditNotes_assignsUniqueIds() = runTest {
        val id1 = dataSource.createNew()
        val id2 = dataSource.createNew()
        val id3 = dataSource.createNew()

        assertEquals(3, dataSource.getCreditNoteCount())
        assertTrue(id1 != id2 && id2 != id3)
    }

    @Test
    fun createCreditNote_generatesSequentialNumbers() = runTest {
        dataSource.createNew()
        dataSource.createNew()
        dataSource.createNew()

        val creditNotes = dataSource.getCreditNotes()
        assertEquals("AV-001", creditNotes[0].documentNumber.text)
        assertEquals("AV-002", creditNotes[1].documentNumber.text)
        assertEquals("AV-003", creditNotes[2].documentNumber.text)
    }

    // ============= PRODUCT TESTS =============

    @Test
    fun addProductToCreditNote_savesCorrectly() = runTest {
        val creditNoteId = dataSource.createNew()
        val product = DocumentProductState(
            name = TextFieldValue("Produit Test"),
            priceWithoutTax = BigDecimal.fromDouble(100.0),
            quantity = BigDecimal.ONE
        )

        val productId = dataSource.saveDocumentProductInDbAndLinkToDocument(
            documentProduct = product,
            documentId = creditNoteId
        )

        assertTrue(productId!! > 0)
        val creditNote = dataSource.fetch(creditNoteId)
        assertEquals(1, creditNote?.documentProducts?.size)
    }

    @Test
    fun addProductToCreditNote_withLinkedDeliveryNoteInfo() = runTest {
        val creditNoteId = dataSource.createNew()
        val product = DocumentProductState(name = TextFieldValue("Produit"))

        dataSource.saveDocumentProductInDbAndLinkToDocument(
            documentProduct = product,
            documentId = creditNoteId,
            deliveryNoteDate = "10/01/2024",
            deliveryNoteNumber = "BL-001"
        )

        val creditNote = dataSource.fetch(creditNoteId)
        val savedProduct = creditNote?.documentProducts?.first()
        assertEquals("10/01/2024", savedProduct?.linkedDate)
        assertEquals("BL-001", savedProduct?.linkedDocNumber)
    }

    @Test
    fun addMultipleProductsToCreditNote() = runTest {
        val creditNoteId = dataSource.createNew()
        val product1 = DocumentProductState(name = TextFieldValue("Produit 1"))
        val product2 = DocumentProductState(name = TextFieldValue("Produit 2"))
        val product3 = DocumentProductState(name = TextFieldValue("Produit 3"))

        dataSource.saveDocumentProductInDbAndLinkToDocument(product1, creditNoteId)
        dataSource.saveDocumentProductInDbAndLinkToDocument(product2, creditNoteId)
        dataSource.saveDocumentProductInDbAndLinkToDocument(product3, creditNoteId)

        val creditNote = dataSource.fetch(creditNoteId)
        assertEquals(3, creditNote?.documentProducts?.size)
    }

    @Test
    fun removeProductFromCreditNote() = runTest {
        val creditNoteId = dataSource.createNew()
        val product = DocumentProductState(name = TextFieldValue("À Retirer"))
        val productId = dataSource.saveDocumentProductInDbAndLinkToDocument(product, creditNoteId)

        dataSource.deleteDocumentProduct(creditNoteId, productId!!.toLong())

        val creditNote = dataSource.fetch(creditNoteId)
        assertTrue(creditNote?.documentProducts?.isEmpty() ?: true)
    }

    // ============= CLIENT/ISSUER TESTS =============

    @Test
    fun addClientToCreditNote() = runTest {
        val creditNoteId = dataSource.createNew()
        val client = ClientOrIssuerState(
            name = TextFieldValue("Client Test"),
            type = ClientOrIssuerType.CLIENT
        )

        dataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(client, creditNoteId)

        val creditNote = dataSource.fetch(creditNoteId)
        assertNotNull(creditNote?.documentClient)
        assertEquals("Client Test", creditNote?.documentClient?.name?.text)
    }

    @Test
    fun addIssuerToCreditNote() = runTest {
        val creditNoteId = dataSource.createNew()
        val issuer = ClientOrIssuerState(
            name = TextFieldValue("Mon Entreprise"),
            type = ClientOrIssuerType.ISSUER
        )

        dataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(issuer, creditNoteId)

        val creditNote = dataSource.fetch(creditNoteId)
        assertNotNull(creditNote?.documentIssuer)
    }

    @Test
    fun removeClientFromCreditNote() = runTest {
        val creditNoteId = dataSource.createNew()
        val client = ClientOrIssuerState(
            name = TextFieldValue("À Retirer"),
            type = ClientOrIssuerType.CLIENT
        )
        dataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(client, creditNoteId)

        dataSource.deleteDocumentClientOrIssuer(creditNoteId, ClientOrIssuerType.CLIENT)

        val creditNote = dataSource.fetch(creditNoteId)
        assertNull(creditNote?.documentClient)
    }

    // ============= CONVERT FROM INVOICE TESTS =============

    @Test
    fun convertInvoiceToCreditNote_createsNewCreditNote() = runTest {
        val invoice = InvoiceState(
            documentId = 1,
            documentNumber = TextFieldValue("FA-001"),
            documentDate = "15/01/2024"
        )

        dataSource.convertInvoiceToCreditNote(listOf(invoice))

        assertEquals(1, dataSource.getCreditNoteCount())
        val creditNote = dataSource.getCreditNotes().first()
        assertEquals(DocumentTag.DRAFT, creditNote.documentTag)
    }

    @Test
    fun convertInvoiceToCreditNote_setsDateToToday() = runTest {
        val invoice = InvoiceState(
            documentId = 1,
            documentNumber = TextFieldValue("FA-001"),
            documentDate = "15/01/2024"
        )

        dataSource.convertInvoiceToCreditNote(listOf(invoice))

        val creditNote = dataSource.getCreditNotes().first()
        val todayFormatted = DateUtils.getCurrentDateFormatted()
        assertEquals(todayFormatted, creditNote.documentDate)
    }

    @Test
    fun convertInvoiceToCreditNote_copiesClientAndIssuer() = runTest {
        val client = ClientOrIssuerState(
            name = TextFieldValue("Client Facture"),
            type = ClientOrIssuerType.CLIENT
        )
        val issuer = ClientOrIssuerState(
            name = TextFieldValue("Emetteur Facture"),
            type = ClientOrIssuerType.ISSUER
        )
        val invoice = InvoiceState(
            documentId = 1,
            documentNumber = TextFieldValue("FA-001"),
            documentClient = client,
            documentIssuer = issuer
        )

        dataSource.convertInvoiceToCreditNote(listOf(invoice))

        val creditNote = dataSource.getCreditNotes().first()
        assertEquals("Client Facture", creditNote.documentClient?.name?.text)
        assertEquals("Emetteur Facture", creditNote.documentIssuer?.name?.text)
    }

    @Test
    fun convertInvoiceToCreditNote_copiesProductsWithLinkedInfo() = runTest {
        val product = DocumentProductState(
            name = TextFieldValue("Produit Facturé"),
            priceWithoutTax = BigDecimal.fromDouble(100.0)
        )
        val invoice = InvoiceState(
            documentId = 1,
            documentNumber = TextFieldValue("FA-001"),
            documentDate = "15/01/2024",
            documentProducts = listOf(product)
        )

        dataSource.convertInvoiceToCreditNote(listOf(invoice))

        val creditNote = dataSource.getCreditNotes().first()
        assertEquals(1, creditNote.documentProducts?.size)
        val creditProduct = creditNote.documentProducts?.first()
        assertEquals("Produit Facturé", creditProduct?.name?.text)
        assertEquals("15/01/2024", creditProduct?.linkedDate)
        assertEquals("FA-001", creditProduct?.linkedDocNumber)
    }

    @Test
    fun convertInvoiceToCreditNote_linksToOriginalInvoice() = runTest {
        val invoice = InvoiceState(
            documentId = 1,
            documentNumber = TextFieldValue("FA-001"),
            documentDate = "15/01/2024"
        )

        dataSource.convertInvoiceToCreditNote(listOf(invoice))

        val creditNote = dataSource.getCreditNotes().first()
        assertNotNull(creditNote.linkedInvoice)
        assertEquals("FA-001", creditNote.linkedInvoice?.documentNumber?.text)
    }

    // ============= UPDATE TESTS =============

    @Test
    fun updateCreditNote_changesNumber() = runTest {
        val creditNoteId = dataSource.createNew()
        val creditNote = dataSource.fetch(creditNoteId)!!

        val updated = creditNote.copy(documentNumber = TextFieldValue("AV-2024-001"))
        dataSource.update(updated)

        val fetched = dataSource.fetch(creditNoteId)
        assertEquals("AV-2024-001", fetched?.documentNumber?.text)
    }

    @Test
    fun updateCreditNote_changesDate() = runTest {
        val creditNoteId = dataSource.createNew()
        val creditNote = dataSource.fetch(creditNoteId)!!

        val updated = creditNote.copy(documentDate = "20/01/2024")
        dataSource.update(updated)

        val fetched = dataSource.fetch(creditNoteId)
        assertEquals("20/01/2024", fetched?.documentDate)
    }

    // ============= DELETE TESTS =============

    @Test
    fun deleteCreditNote_removesFromList() = runTest {
        val creditNoteId = dataSource.createNew()
        val creditNote = dataSource.fetch(creditNoteId)!!

        dataSource.delete(listOf(creditNote))

        assertEquals(0, dataSource.getCreditNoteCount())
    }

    @Test
    fun deleteMultipleCreditNotes() = runTest {
        dataSource.createNew()
        dataSource.createNew()
        dataSource.createNew()

        val toDelete = dataSource.getCreditNotes().take(2)
        dataSource.delete(toDelete)

        assertEquals(1, dataSource.getCreditNoteCount())
    }

    // ============= DUPLICATE TESTS =============

    @Test
    fun duplicateCreditNote_copiesAllData() = runTest {
        val creditNoteId = dataSource.createNew()
        val client = ClientOrIssuerState(name = TextFieldValue("Client"), type = ClientOrIssuerType.CLIENT)
        dataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(client, creditNoteId)
        val product = DocumentProductState(name = TextFieldValue("Produit"))
        dataSource.saveDocumentProductInDbAndLinkToDocument(product, creditNoteId)

        val creditNote = dataSource.fetch(creditNoteId)!!
        dataSource.duplicate(listOf(creditNote))

        assertEquals(2, dataSource.getCreditNoteCount())
        val duplicated = dataSource.getCreditNotes().last()
        assertNotNull(duplicated.documentClient)
        assertNotNull(duplicated.documentProducts)
    }

    @Test
    fun duplicateCreditNote_incrementsNumber() = runTest {
        val creditNoteId = dataSource.createNew()
        val creditNote = dataSource.fetch(creditNoteId)!!

        dataSource.duplicate(listOf(creditNote))

        val duplicated = dataSource.getCreditNotes().last()
        assertEquals("AV-002", duplicated.documentNumber.text)
    }

    @Test
    fun duplicateCreditNote_setsTagToDraft() = runTest {
        val creditNoteId = dataSource.createNew()
        val creditNote = dataSource.fetch(creditNoteId)!!

        dataSource.duplicate(listOf(creditNote))

        val duplicated = dataSource.getCreditNotes().last()
        assertEquals(DocumentTag.DRAFT, duplicated.documentTag)
    }

    // ============= PRODUCT ORDER TESTS =============

    @Test
    fun reorderProducts_movesCorrectly() = runTest {
        val creditNoteId = dataSource.createNew()
        val p1 = DocumentProductState(name = TextFieldValue("Premier"))
        val p2 = DocumentProductState(name = TextFieldValue("Deuxième"))
        val p3 = DocumentProductState(name = TextFieldValue("Troisième"))
        dataSource.saveDocumentProductInDbAndLinkToDocument(p1, creditNoteId)
        dataSource.saveDocumentProductInDbAndLinkToDocument(p2, creditNoteId)
        dataSource.saveDocumentProductInDbAndLinkToDocument(p3, creditNoteId)

        val creditNote = dataSource.fetch(creditNoteId)!!
        val reordered = creditNote.documentProducts!!.reversed()
        dataSource.updateDocumentProductsOrderInDb(creditNoteId, reordered)

        val updated = dataSource.fetch(creditNoteId)
        assertEquals("Troisième", updated?.documentProducts?.first()?.name?.text)
    }

    // ============= FLOW TESTS =============

    @Test
    fun flowUpdates_onCreateCreditNote() = runTest {
        val initialCreditNotes = dataSource.fetchAll()?.first()
        assertTrue(initialCreditNotes?.isEmpty() ?: true)

        dataSource.createNew()

        val updatedCreditNotes = dataSource.fetchAll()?.first()
        assertEquals(1, updatedCreditNotes?.size)
    }

    @Test
    fun flowUpdates_onDeleteCreditNote() = runTest {
        val creditNoteId = dataSource.createNew()
        val creditNote = dataSource.fetch(creditNoteId)!!

        dataSource.delete(listOf(creditNote))

        val creditNotes = dataSource.fetchAll()?.first()
        assertTrue(creditNotes?.isEmpty() ?: true)
    }
}
