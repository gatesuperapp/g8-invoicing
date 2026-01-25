package com.a4a.g8invoicing

import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.models.TagUpdateOrCreationCase
import com.a4a.g8invoicing.data.util.DateUtils
import com.a4a.g8invoicing.fakes.FakeDeliveryNoteDataSource
import com.a4a.g8invoicing.fakes.FakeInvoiceDataSource
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
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

class InvoiceDataSourceTest {

    private lateinit var dataSource: FakeInvoiceDataSource

    @BeforeTest
    fun setup() {
        dataSource = FakeInvoiceDataSource()
    }

    // ============= CREATION TESTS =============

    @Test
    fun createInvoice_createsEmptyDocument() = runTest {
        val invoiceId = dataSource.createNew()

        assertNotNull(invoiceId)
        assertEquals(1, dataSource.getInvoiceCount())
        val invoice = dataSource.fetch(invoiceId)
        assertNotNull(invoice)
        assertEquals(DocumentTag.DRAFT, invoice.documentTag)
    }

    @Test
    fun createMultipleInvoices_assignsUniqueIds() = runTest {
        val id1 = dataSource.createNew()
        val id2 = dataSource.createNew()
        val id3 = dataSource.createNew()

        assertEquals(3, dataSource.getInvoiceCount())
        assertTrue(id1 != id2 && id2 != id3)
    }

    @Test
    fun createInvoice_generatesSequentialNumbers() = runTest {
        dataSource.createNew()
        dataSource.createNew()
        dataSource.createNew()

        val invoices = dataSource.getInvoices()
        assertEquals("FA-001", invoices[0].documentNumber.text)
        assertEquals("FA-002", invoices[1].documentNumber.text)
        assertEquals("FA-003", invoices[2].documentNumber.text)
    }

    // ============= PRODUCT TESTS =============

    @Test
    fun addProductToInvoice_savesCorrectly() = runTest {
        val invoiceId = dataSource.createNew()
        val product = DocumentProductState(
            name = TextFieldValue("Produit Test"),
            priceWithoutTax = BigDecimal.fromDouble(100.0),
            quantity = BigDecimal.ONE
        )

        val productId = dataSource.saveDocumentProductInDbAndLinkToDocument(
            documentProduct = product,
            documentId = invoiceId
        )

        assertTrue(productId > 0)
        val invoice = dataSource.fetch(invoiceId)
        assertEquals(1, invoice?.documentProducts?.size)
        assertEquals("Produit Test", invoice?.documentProducts?.first()?.name?.text)
    }

    @Test
    fun addMultipleProductsToInvoice() = runTest {
        val invoiceId = dataSource.createNew()
        val product1 = DocumentProductState(name = TextFieldValue("Produit 1"))
        val product2 = DocumentProductState(name = TextFieldValue("Produit 2"))
        val product3 = DocumentProductState(name = TextFieldValue("Produit 3"))

        dataSource.saveDocumentProductInDbAndLinkToDocument(product1, invoiceId)
        dataSource.saveDocumentProductInDbAndLinkToDocument(product2, invoiceId)
        dataSource.saveDocumentProductInDbAndLinkToDocument(product3, invoiceId)

        val invoice = dataSource.fetch(invoiceId)
        assertEquals(3, invoice?.documentProducts?.size)
    }

    @Test
    fun removeProductFromInvoice() = runTest {
        val invoiceId = dataSource.createNew()
        val product = DocumentProductState(name = TextFieldValue("À Retirer"))
        val productId = dataSource.saveDocumentProductInDbAndLinkToDocument(product, invoiceId)

        dataSource.deleteDocumentProduct(invoiceId, productId.toLong())

        val invoice = dataSource.fetch(invoiceId)
        assertTrue(invoice?.documentProducts?.isEmpty() ?: true)
    }

    @Test
    fun removeProductFromInvoice_leavesOthers() = runTest {
        val invoiceId = dataSource.createNew()
        val product1 = DocumentProductState(name = TextFieldValue("Produit 1"))
        val product2 = DocumentProductState(name = TextFieldValue("Produit 2"))
        val id1 = dataSource.saveDocumentProductInDbAndLinkToDocument(product1, invoiceId)
        dataSource.saveDocumentProductInDbAndLinkToDocument(product2, invoiceId)

        dataSource.deleteDocumentProduct(invoiceId, id1.toLong())

        val invoice = dataSource.fetch(invoiceId)
        assertEquals(1, invoice?.documentProducts?.size)
        assertEquals("Produit 2", invoice?.documentProducts?.first()?.name?.text)
    }

    // ============= CLIENT/ISSUER TESTS =============

    @Test
    fun addClientToInvoice() = runTest {
        val invoiceId = dataSource.createNew()
        val client = ClientOrIssuerState(
            name = TextFieldValue("Client Test"),
            type = ClientOrIssuerType.CLIENT
        )

        dataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(client, invoiceId)

        val invoice = dataSource.fetch(invoiceId)
        assertNotNull(invoice?.documentClient)
        assertEquals("Client Test", invoice?.documentClient?.name?.text)
    }

    @Test
    fun addIssuerToInvoice() = runTest {
        val invoiceId = dataSource.createNew()
        val issuer = ClientOrIssuerState(
            name = TextFieldValue("Mon Entreprise"),
            type = ClientOrIssuerType.ISSUER
        )

        dataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(issuer, invoiceId)

        val invoice = dataSource.fetch(invoiceId)
        assertNotNull(invoice?.documentIssuer)
        assertEquals("Mon Entreprise", invoice?.documentIssuer?.name?.text)
    }

    @Test
    fun addBothClientAndIssuerToInvoice() = runTest {
        val invoiceId = dataSource.createNew()
        val client = ClientOrIssuerState(
            name = TextFieldValue("Client"),
            type = ClientOrIssuerType.CLIENT
        )
        val issuer = ClientOrIssuerState(
            name = TextFieldValue("Emetteur"),
            type = ClientOrIssuerType.ISSUER
        )

        dataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(client, invoiceId)
        dataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(issuer, invoiceId)

        val invoice = dataSource.fetch(invoiceId)
        assertNotNull(invoice?.documentClient)
        assertNotNull(invoice?.documentIssuer)
    }

    @Test
    fun removeClientFromInvoice() = runTest {
        val invoiceId = dataSource.createNew()
        val client = ClientOrIssuerState(
            name = TextFieldValue("À Retirer"),
            type = ClientOrIssuerType.CLIENT
        )
        dataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(client, invoiceId)

        dataSource.deleteDocumentClientOrIssuer(invoiceId, ClientOrIssuerType.CLIENT)

        val invoice = dataSource.fetch(invoiceId)
        assertNull(invoice?.documentClient)
    }

    // ============= UPDATE TESTS =============

    @Test
    fun updateInvoice_changesNumber() = runTest {
        val invoiceId = dataSource.createNew()
        val invoice = dataSource.fetch(invoiceId)!!

        val updated = invoice.copy(documentNumber = TextFieldValue("FA-2024-001"))
        dataSource.update(updated)

        val fetched = dataSource.fetch(invoiceId)
        assertEquals("FA-2024-001", fetched?.documentNumber?.text)
    }

    @Test
    fun updateInvoice_changesDate() = runTest {
        val invoiceId = dataSource.createNew()
        val invoice = dataSource.fetch(invoiceId)!!

        val updated = invoice.copy(documentDate = "15/01/2024")
        dataSource.update(updated)

        val fetched = dataSource.fetch(invoiceId)
        assertEquals("15/01/2024", fetched?.documentDate)
    }

    @Test
    fun updateInvoice_changesDueDate() = runTest {
        val invoiceId = dataSource.createNew()
        val invoice = dataSource.fetch(invoiceId)!!

        val updated = invoice.copy(dueDate = "15/02/2024")
        dataSource.update(updated)

        val fetched = dataSource.fetch(invoiceId)
        assertEquals("15/02/2024", fetched?.dueDate)
    }

    // ============= DELETE TESTS =============

    @Test
    fun deleteInvoice_removesFromList() = runTest {
        val invoiceId = dataSource.createNew()
        val invoice = dataSource.fetch(invoiceId)!!

        dataSource.delete(listOf(invoice))

        assertEquals(0, dataSource.getInvoiceCount())
    }

    @Test
    fun deleteMultipleInvoices() = runTest {
        dataSource.createNew()
        dataSource.createNew()
        dataSource.createNew()

        val toDelete = dataSource.getInvoices().take(2)
        dataSource.delete(toDelete)

        assertEquals(1, dataSource.getInvoiceCount())
    }

    // ============= DUPLICATE TESTS =============

    @Test
    fun duplicateInvoice_copiesAllData() = runTest {
        val invoiceId = dataSource.createNew()
        val client = ClientOrIssuerState(name = TextFieldValue("Client"), type = ClientOrIssuerType.CLIENT)
        dataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(client, invoiceId)
        val product = DocumentProductState(name = TextFieldValue("Produit"))
        dataSource.saveDocumentProductInDbAndLinkToDocument(product, invoiceId)

        val invoice = dataSource.fetch(invoiceId)!!
        dataSource.duplicate(listOf(invoice))

        assertEquals(2, dataSource.getInvoiceCount())
        val duplicated = dataSource.getInvoices().last()
        assertNotNull(duplicated.documentClient)
        assertNotNull(duplicated.documentProducts)
    }

    @Test
    fun duplicateInvoice_incrementsNumber() = runTest {
        val invoiceId = dataSource.createNew()
        val invoice = dataSource.fetch(invoiceId)!!

        dataSource.duplicate(listOf(invoice))

        val duplicated = dataSource.getInvoices().last()
        assertEquals("FA-002", duplicated.documentNumber.text)
    }

    @Test
    fun duplicateInvoice_setsTagToDraft() = runTest {
        val invoiceId = dataSource.createNew()
        val invoice = dataSource.fetch(invoiceId)!!
        dataSource.setTag(listOf(invoice), DocumentTag.SENT, TagUpdateOrCreationCase.UPDATED_BY_USER)

        dataSource.duplicate(listOf(invoice))

        val duplicated = dataSource.getInvoices().last()
        assertEquals(DocumentTag.DRAFT, duplicated.documentTag)
    }

    // ============= TAG TESTS =============

    @Test
    fun setTag_paid() = runTest {
        val invoiceId = dataSource.createNew()
        val invoice = dataSource.fetch(invoiceId)!!

        dataSource.setTag(listOf(invoice), DocumentTag.PAID, TagUpdateOrCreationCase.UPDATED_BY_USER)

        val updated = dataSource.fetch(invoiceId)
        assertEquals(DocumentTag.PAID, updated?.documentTag)
    }

    @Test
    fun setTag_sent() = runTest {
        val invoiceId = dataSource.createNew()
        val invoice = dataSource.fetch(invoiceId)!!

        dataSource.setTag(listOf(invoice), DocumentTag.SENT, TagUpdateOrCreationCase.UPDATED_BY_USER)

        val updated = dataSource.fetch(invoiceId)
        assertEquals(DocumentTag.SENT, updated?.documentTag)
    }

    @Test
    fun setTag_reminder() = runTest {
        val invoiceId = dataSource.createNew()
        val invoice = dataSource.fetch(invoiceId)!!

        dataSource.setTag(listOf(invoice), DocumentTag.REMINDED, TagUpdateOrCreationCase.UPDATED_BY_USER)

        val updated = dataSource.fetch(invoiceId)
        assertEquals(DocumentTag.REMINDED, updated?.documentTag)
    }

    @Test
    fun removeTag() = runTest {
        val invoiceId = dataSource.createNew()
        val invoice = dataSource.fetch(invoiceId)!!
        dataSource.setTag(listOf(invoice), DocumentTag.SENT, TagUpdateOrCreationCase.UPDATED_BY_USER)

        dataSource.deleteTag(invoiceId)

        val updated = dataSource.fetch(invoiceId)
        assertEquals(DocumentTag.DRAFT, updated?.documentTag)
    }

    @Test
    fun markAsPaid_setsCorrectTag() = runTest {
        val invoiceId = dataSource.createNew()
        val invoice = dataSource.fetch(invoiceId)!!

        dataSource.markAsPaid(listOf(invoice), DocumentTag.PAID)

        val updated = dataSource.fetch(invoiceId)
        assertEquals(DocumentTag.PAID, updated?.documentTag)
        assertEquals(1, updated?.paymentStatus)
    }

    // ============= PRODUCT ORDER TESTS =============

    @Test
    fun reorderProducts_movesCorrectly() = runTest {
        val invoiceId = dataSource.createNew()
        val p1 = DocumentProductState(name = TextFieldValue("Premier"))
        val p2 = DocumentProductState(name = TextFieldValue("Deuxième"))
        val p3 = DocumentProductState(name = TextFieldValue("Troisième"))
        dataSource.saveDocumentProductInDbAndLinkToDocument(p1, invoiceId)
        dataSource.saveDocumentProductInDbAndLinkToDocument(p2, invoiceId)
        dataSource.saveDocumentProductInDbAndLinkToDocument(p3, invoiceId)

        val invoice = dataSource.fetch(invoiceId)!!
        val reordered = invoice.documentProducts!!.reversed()
        dataSource.updateDocumentProductsOrderInDb(invoiceId, reordered)

        val updated = dataSource.fetch(invoiceId)
        assertEquals("Troisième", updated?.documentProducts?.first()?.name?.text)
    }

    @Test
    fun reorderProducts_updatesOrder() = runTest {
        val invoiceId = dataSource.createNew()
        val p1 = DocumentProductState(name = TextFieldValue("A"))
        val p2 = DocumentProductState(name = TextFieldValue("B"))
        dataSource.saveDocumentProductInDbAndLinkToDocument(p1, invoiceId)
        dataSource.saveDocumentProductInDbAndLinkToDocument(p2, invoiceId)

        val invoice = dataSource.fetch(invoiceId)!!
        dataSource.updateDocumentProductsOrderInDb(invoiceId, invoice.documentProducts!!)

        val updated = dataSource.fetch(invoiceId)
        assertEquals(0, updated?.documentProducts?.get(0)?.sortOrder)
        assertEquals(1, updated?.documentProducts?.get(1)?.sortOrder)
    }

    // ============= FLOW TESTS =============

    @Test
    fun flowUpdates_onCreateInvoice() = runTest {
        val initialInvoices = dataSource.fetchAll()?.first()
        assertTrue(initialInvoices?.isEmpty() ?: true)

        dataSource.createNew()

        val updatedInvoices = dataSource.fetchAll()?.first()
        assertEquals(1, updatedInvoices?.size)
    }

    @Test
    fun flowUpdates_onDeleteInvoice() = runTest {
        val invoiceId = dataSource.createNew()
        val invoice = dataSource.fetch(invoiceId)!!

        dataSource.delete(listOf(invoice))

        val invoices = dataSource.fetchAll()?.first()
        assertTrue(invoices?.isEmpty() ?: true)
    }

    // ============= DEFAULT DATE TESTS =============

    @Test
    fun createInvoice_setsDateToToday() = runTest {
        val invoiceId = dataSource.createNew()

        val invoice = dataSource.fetch(invoiceId)
        val todayFormatted = DateUtils.getCurrentDateFormatted()

        assertNotNull(invoice)
        assertEquals(todayFormatted, invoice.documentDate)
    }

    @Test
    fun createInvoice_setsDueDateTo30DaysFromNow() = runTest {
        val invoiceId = dataSource.createNew()

        val invoice = dataSource.fetch(invoiceId)
        val dueDateFormatted = DateUtils.getDatePlusDaysFormatted(30)

        assertNotNull(invoice)
        assertEquals(dueDateFormatted, invoice.dueDate)
    }

    @Test
    fun createInvoice_setsCreatedDate() = runTest {
        val invoiceId = dataSource.createNew()

        val invoice = dataSource.fetch(invoiceId)

        assertNotNull(invoice?.createdDate)
        assertTrue(invoice?.createdDate?.isNotBlank() == true)
    }

    // ============= FOOTER TESTS =============

    @Test
    fun createInvoice_setsDefaultFooter() = runTest {
        val invoiceId = dataSource.createNew()

        val invoice = dataSource.fetch(invoiceId)

        assertNotNull(invoice?.footerText)
        assertTrue(invoice?.footerText?.text?.isNotBlank() == true)
        assertEquals("Merci pour votre confiance.", invoice?.footerText?.text)
    }

    @Test
    fun createInvoice_customFooterCanBeSet() = runTest {
        dataSource.defaultFooterText = "Paiement à 30 jours net."
        val invoiceId = dataSource.createNew()

        val invoice = dataSource.fetch(invoiceId)

        assertEquals("Paiement à 30 jours net.", invoice?.footerText?.text)
    }

    // ============= DELIVERY NOTE CONVERSION TESTS =============

    @Test
    fun convertDeliveryNotesToInvoice_createsNewInvoice() = runTest {
        val deliveryNote = DeliveryNoteState(
            documentId = 1,
            documentNumber = TextFieldValue("BL-001"),
            documentDate = "10/01/2024"
        )

        dataSource.convertDeliveryNotesToInvoice(listOf(deliveryNote))

        assertEquals(1, dataSource.getInvoiceCount())
        val invoice = dataSource.getInvoices().first()
        assertEquals(DocumentTag.DRAFT, invoice.documentTag)
    }

    @Test
    fun convertDeliveryNotesToInvoice_setsDateToToday() = runTest {
        val deliveryNote = DeliveryNoteState(
            documentId = 1,
            documentNumber = TextFieldValue("BL-001"),
            documentDate = "10/01/2024"
        )

        dataSource.convertDeliveryNotesToInvoice(listOf(deliveryNote))

        val invoice = dataSource.getInvoices().first()
        val todayFormatted = DateUtils.getCurrentDateFormatted()
        assertEquals(todayFormatted, invoice.documentDate)
    }

    @Test
    fun convertDeliveryNotesToInvoice_copiesClientAndIssuer() = runTest {
        val client = ClientOrIssuerState(
            name = TextFieldValue("Client BL"),
            type = ClientOrIssuerType.CLIENT
        )
        val issuer = ClientOrIssuerState(
            name = TextFieldValue("Emetteur BL"),
            type = ClientOrIssuerType.ISSUER
        )
        val deliveryNote = DeliveryNoteState(
            documentId = 1,
            documentNumber = TextFieldValue("BL-001"),
            documentClient = client,
            documentIssuer = issuer
        )

        dataSource.convertDeliveryNotesToInvoice(listOf(deliveryNote))

        val invoice = dataSource.getInvoices().first()
        assertEquals("Client BL", invoice.documentClient?.name?.text)
        assertEquals("Emetteur BL", invoice.documentIssuer?.name?.text)
    }

    @Test
    fun convertDeliveryNotesToInvoice_productsLinkedToDeliveryNote() = runTest {
        val product = DocumentProductState(
            name = TextFieldValue("Produit Livré"),
            priceWithoutTax = BigDecimal.fromDouble(100.0)
        )
        val deliveryNote = DeliveryNoteState(
            documentId = 1,
            documentNumber = TextFieldValue("BL-001"),
            documentDate = "10/01/2024",
            documentProducts = listOf(product)
        )

        dataSource.convertDeliveryNotesToInvoice(listOf(deliveryNote))

        val invoice = dataSource.getInvoices().first()
        assertEquals(1, invoice.documentProducts?.size)
        val invoiceProduct = invoice.documentProducts?.first()
        assertEquals("Produit Livré", invoiceProduct?.name?.text)
        assertEquals("10/01/2024", invoiceProduct?.linkedDate)
        assertEquals("BL-001", invoiceProduct?.linkedDocNumber)
    }

    @Test
    fun convertMultipleDeliveryNotesToInvoice_combinesProducts() = runTest {
        val product1 = DocumentProductState(name = TextFieldValue("Produit BL1"))
        val product2 = DocumentProductState(name = TextFieldValue("Produit BL2"))
        val deliveryNote1 = DeliveryNoteState(
            documentId = 1,
            documentNumber = TextFieldValue("BL-001"),
            documentDate = "10/01/2024",
            documentProducts = listOf(product1)
        )
        val deliveryNote2 = DeliveryNoteState(
            documentId = 2,
            documentNumber = TextFieldValue("BL-002"),
            documentDate = "12/01/2024",
            documentProducts = listOf(product2)
        )

        dataSource.convertDeliveryNotesToInvoice(listOf(deliveryNote1, deliveryNote2))

        val invoice = dataSource.getInvoices().first()
        assertEquals(2, invoice.documentProducts?.size)

        val products = invoice.documentProducts!!
        assertEquals("Produit BL1", products[0].name.text)
        assertEquals("BL-001", products[0].linkedDocNumber)
        assertEquals("10/01/2024", products[0].linkedDate)

        assertEquals("Produit BL2", products[1].name.text)
        assertEquals("BL-002", products[1].linkedDocNumber)
        assertEquals("12/01/2024", products[1].linkedDate)
    }

    @Test
    fun convertMultipleDeliveryNotesToInvoice_eachProductLinkedToItsDeliveryNote() = runTest {
        val product1a = DocumentProductState(name = TextFieldValue("Produit 1A"))
        val product1b = DocumentProductState(name = TextFieldValue("Produit 1B"))
        val product2a = DocumentProductState(name = TextFieldValue("Produit 2A"))
        val deliveryNote1 = DeliveryNoteState(
            documentId = 1,
            documentNumber = TextFieldValue("BL-001"),
            documentDate = "05/01/2024",
            documentProducts = listOf(product1a, product1b)
        )
        val deliveryNote2 = DeliveryNoteState(
            documentId = 2,
            documentNumber = TextFieldValue("BL-002"),
            documentDate = "15/01/2024",
            documentProducts = listOf(product2a)
        )

        dataSource.convertDeliveryNotesToInvoice(listOf(deliveryNote1, deliveryNote2))

        val invoice = dataSource.getInvoices().first()
        assertEquals(3, invoice.documentProducts?.size)

        val products = invoice.documentProducts!!
        // Products from BL-001
        assertEquals("BL-001", products[0].linkedDocNumber)
        assertEquals("05/01/2024", products[0].linkedDate)
        assertEquals("BL-001", products[1].linkedDocNumber)
        assertEquals("05/01/2024", products[1].linkedDate)
        // Product from BL-002
        assertEquals("BL-002", products[2].linkedDocNumber)
        assertEquals("15/01/2024", products[2].linkedDate)
    }

    @Test
    fun convertDeliveryNotesToInvoice_setsDefaultFooter() = runTest {
        val deliveryNote = DeliveryNoteState(
            documentId = 1,
            documentNumber = TextFieldValue("BL-001")
        )

        dataSource.convertDeliveryNotesToInvoice(listOf(deliveryNote))

        val invoice = dataSource.getInvoices().first()
        assertEquals("Merci pour votre confiance.", invoice.footerText.text)
    }

    @Test
    fun addProductWithDeliveryNoteInfo_savesLinkedInfo() = runTest {
        val invoiceId = dataSource.createNew()
        val product = DocumentProductState(name = TextFieldValue("Produit"))

        dataSource.saveDocumentProductInDbAndLinkToDocument(
            documentProduct = product,
            documentId = invoiceId,
            deliveryNoteDate = "08/01/2024",
            deliveryNoteNumber = "BL-005"
        )

        val invoice = dataSource.fetch(invoiceId)
        val savedProduct = invoice?.documentProducts?.first()
        assertEquals("08/01/2024", savedProduct?.linkedDate)
        assertEquals("BL-005", savedProduct?.linkedDocNumber)
    }

    // ============= CORRECTIVE INVOICE TESTS (Facture Rectificative) =============

    @Test
    fun createCorrectiveInvoice_createsNewInvoice() = runTest {
        val invoiceId = dataSource.createNew()
        val invoice = dataSource.fetch(invoiceId)!!

        val correctiveId = dataSource.createCorrectiveInvoice(invoice)

        assertEquals(2, dataSource.getInvoiceCount())
        assertTrue(correctiveId > 0)
    }

    @Test
    fun createCorrectiveInvoice_hasCancelAndReplaceText() = runTest {
        val invoiceId = dataSource.createNew()
        val invoice = dataSource.fetch(invoiceId)!!

        val correctiveId = dataSource.createCorrectiveInvoice(invoice)

        val corrective = dataSource.fetch(correctiveId)
        assertNotNull(corrective?.freeField)
        assertTrue(corrective?.freeField?.text?.contains("Annule et remplace") == true)
        assertTrue(corrective?.freeField?.text?.contains("FA-001") == true)
    }

    @Test
    fun createCorrectiveInvoice_originalMarkedAsCancelled() = runTest {
        val invoiceId = dataSource.createNew()
        val invoice = dataSource.fetch(invoiceId)!!

        dataSource.createCorrectiveInvoice(invoice)

        val original = dataSource.fetch(invoiceId)
        assertEquals(DocumentTag.CANCELLED, original?.documentTag)
    }

    @Test
    fun createCorrectiveInvoice_correctiveIsDraft() = runTest {
        val invoiceId = dataSource.createNew()
        val invoice = dataSource.fetch(invoiceId)!!
        dataSource.setTag(listOf(invoice), DocumentTag.SENT, TagUpdateOrCreationCase.UPDATED_BY_USER)

        val correctiveId = dataSource.createCorrectiveInvoice(invoice)

        val corrective = dataSource.fetch(correctiveId)
        assertEquals(DocumentTag.DRAFT, corrective?.documentTag)
    }

    @Test
    fun createCorrectiveInvoice_copiesClientAndIssuer() = runTest {
        val invoiceId = dataSource.createNew()
        val client = ClientOrIssuerState(name = TextFieldValue("Client Original"), type = ClientOrIssuerType.CLIENT)
        val issuer = ClientOrIssuerState(name = TextFieldValue("Emetteur Original"), type = ClientOrIssuerType.ISSUER)
        dataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(client, invoiceId)
        dataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(issuer, invoiceId)
        val invoice = dataSource.fetch(invoiceId)!!

        val correctiveId = dataSource.createCorrectiveInvoice(invoice)

        val corrective = dataSource.fetch(correctiveId)
        assertEquals("Client Original", corrective?.documentClient?.name?.text)
        assertEquals("Emetteur Original", corrective?.documentIssuer?.name?.text)
    }

    @Test
    fun createCorrectiveInvoice_copiesProducts() = runTest {
        val invoiceId = dataSource.createNew()
        val product = DocumentProductState(
            name = TextFieldValue("Produit Original"),
            priceWithoutTax = BigDecimal.fromDouble(150.0)
        )
        dataSource.saveDocumentProductInDbAndLinkToDocument(product, invoiceId)
        val invoice = dataSource.fetch(invoiceId)!!

        val correctiveId = dataSource.createCorrectiveInvoice(invoice)

        val corrective = dataSource.fetch(correctiveId)
        assertEquals(1, corrective?.documentProducts?.size)
        assertEquals("Produit Original", corrective?.documentProducts?.first()?.name?.text)
    }

    @Test
    fun createCorrectiveInvoice_incrementsNumber() = runTest {
        val invoiceId = dataSource.createNew()
        val invoice = dataSource.fetch(invoiceId)!!

        val correctiveId = dataSource.createCorrectiveInvoice(invoice)

        val corrective = dataSource.fetch(correctiveId)
        assertEquals("FA-002", corrective?.documentNumber?.text)
    }

    @Test
    fun createCorrectiveInvoice_customCancelAndReplaceText() = runTest {
        val invoiceId = dataSource.createNew()
        val invoice = dataSource.fetch(invoiceId)!!

        val correctiveId = dataSource.createCorrectiveInvoice(
            originalInvoice = invoice,
            cancelAndReplaceText = "Rectifie et annule"
        )

        val corrective = dataSource.fetch(correctiveId)
        assertTrue(corrective?.freeField?.text?.contains("Rectifie et annule") == true)
    }

    @Test
    fun createCorrectiveInvoice_paidInvoiceCanBeCorrected() = runTest {
        val invoiceId = dataSource.createNew()
        val invoice = dataSource.fetch(invoiceId)!!
        dataSource.markAsPaid(listOf(invoice), DocumentTag.PAID)

        val correctiveId = dataSource.createCorrectiveInvoice(invoice)

        assertTrue(correctiveId > 0)
        val original = dataSource.fetch(invoiceId)
        assertEquals(DocumentTag.CANCELLED, original?.documentTag)
    }

    @Test
    fun setTag_cancelled() = runTest {
        val invoiceId = dataSource.createNew()
        val invoice = dataSource.fetch(invoiceId)!!

        dataSource.setTag(listOf(invoice), DocumentTag.CANCELLED, TagUpdateOrCreationCase.AUTOMATICALLY_CANCELLED)

        val updated = dataSource.fetch(invoiceId)
        assertEquals(DocumentTag.CANCELLED, updated?.documentTag)
    }
}
