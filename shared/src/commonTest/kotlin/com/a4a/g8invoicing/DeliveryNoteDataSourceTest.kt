package com.a4a.g8invoicing

import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.util.DateUtils
import com.a4a.g8invoicing.fakes.FakeDeliveryNoteDataSource
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeliveryNoteDataSourceTest {

    private lateinit var dataSource: FakeDeliveryNoteDataSource

    @BeforeTest
    fun setup() {
        dataSource = FakeDeliveryNoteDataSource()
    }

    // ============= CREATION TESTS =============

    @Test
    fun createDeliveryNote_createsEmptyDocument() = runTest {
        val deliveryNoteId = dataSource.createNew()

        assertNotNull(deliveryNoteId)
        assertEquals(1, dataSource.getDeliveryNoteCount())
        val deliveryNote = dataSource.fetch(deliveryNoteId)
        assertNotNull(deliveryNote)
        assertEquals(DocumentTag.DRAFT, deliveryNote.documentTag)
    }

    @Test
    fun createDeliveryNote_setsDateToToday() = runTest {
        val deliveryNoteId = dataSource.createNew()

        val deliveryNote = dataSource.fetch(deliveryNoteId)
        val todayFormatted = DateUtils.getCurrentDateFormatted()

        assertNotNull(deliveryNote)
        assertEquals(todayFormatted, deliveryNote.documentDate)
    }

    @Test
    fun createDeliveryNote_setsCreatedDate() = runTest {
        val deliveryNoteId = dataSource.createNew()

        val deliveryNote = dataSource.fetch(deliveryNoteId)

        assertNotNull(deliveryNote?.createdDate)
        assertTrue(deliveryNote?.createdDate?.isNotBlank() == true)
    }

    @Test
    fun createMultipleDeliveryNotes_assignsUniqueIds() = runTest {
        val id1 = dataSource.createNew()
        val id2 = dataSource.createNew()
        val id3 = dataSource.createNew()

        assertEquals(3, dataSource.getDeliveryNoteCount())
        assertTrue(id1 != id2 && id2 != id3)
    }

    @Test
    fun createDeliveryNote_generatesSequentialNumbers() = runTest {
        dataSource.createNew()
        dataSource.createNew()
        dataSource.createNew()

        val deliveryNotes = dataSource.getDeliveryNotes()
        assertEquals("BL-001", deliveryNotes[0].documentNumber.text)
        assertEquals("BL-002", deliveryNotes[1].documentNumber.text)
        assertEquals("BL-003", deliveryNotes[2].documentNumber.text)
    }

    // ============= PRODUCT TESTS =============

    @Test
    fun addProductToDeliveryNote_savesCorrectly() = runTest {
        val deliveryNoteId = dataSource.createNew()
        val product = DocumentProductState(
            name = TextFieldValue("Produit Test"),
            priceWithoutTax = BigDecimal.fromDouble(100.0),
            quantity = BigDecimal.ONE
        )

        val productId = dataSource.saveDocumentProductInDbAndLinkToDocument(
            documentProduct = product,
            documentId = deliveryNoteId
        )

        assertTrue(productId!! > 0)
        val deliveryNote = dataSource.fetch(deliveryNoteId)
        assertEquals(1, deliveryNote?.documentProducts?.size)
    }

    @Test
    fun addMultipleProductsToDeliveryNote() = runTest {
        val deliveryNoteId = dataSource.createNew()
        val product1 = DocumentProductState(name = TextFieldValue("Produit 1"))
        val product2 = DocumentProductState(name = TextFieldValue("Produit 2"))
        val product3 = DocumentProductState(name = TextFieldValue("Produit 3"))

        dataSource.saveDocumentProductInDbAndLinkToDocument(product1, deliveryNoteId)
        dataSource.saveDocumentProductInDbAndLinkToDocument(product2, deliveryNoteId)
        dataSource.saveDocumentProductInDbAndLinkToDocument(product3, deliveryNoteId)

        val deliveryNote = dataSource.fetch(deliveryNoteId)
        assertEquals(3, deliveryNote?.documentProducts?.size)
    }

    @Test
    fun removeProductFromDeliveryNote() = runTest {
        val deliveryNoteId = dataSource.createNew()
        val product = DocumentProductState(name = TextFieldValue("À Retirer"))
        val productId = dataSource.saveDocumentProductInDbAndLinkToDocument(product, deliveryNoteId)

        dataSource.deleteDocumentProduct(deliveryNoteId, productId!!.toLong())

        val deliveryNote = dataSource.fetch(deliveryNoteId)
        assertTrue(deliveryNote?.documentProducts?.isEmpty() ?: true)
    }

    // ============= CLIENT/ISSUER TESTS =============

    @Test
    fun addClientToDeliveryNote() = runTest {
        val deliveryNoteId = dataSource.createNew()
        val client = ClientOrIssuerState(
            name = TextFieldValue("Client Test"),
            type = ClientOrIssuerType.CLIENT
        )

        dataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(client, deliveryNoteId)

        val deliveryNote = dataSource.fetch(deliveryNoteId)
        assertNotNull(deliveryNote?.documentClient)
        assertEquals("Client Test", deliveryNote?.documentClient?.name?.text)
    }

    @Test
    fun addIssuerToDeliveryNote() = runTest {
        val deliveryNoteId = dataSource.createNew()
        val issuer = ClientOrIssuerState(
            name = TextFieldValue("Mon Entreprise"),
            type = ClientOrIssuerType.ISSUER
        )

        dataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(issuer, deliveryNoteId)

        val deliveryNote = dataSource.fetch(deliveryNoteId)
        assertNotNull(deliveryNote?.documentIssuer)
    }

    @Test
    fun removeClientFromDeliveryNote() = runTest {
        val deliveryNoteId = dataSource.createNew()
        val client = ClientOrIssuerState(
            name = TextFieldValue("À Retirer"),
            type = ClientOrIssuerType.CLIENT
        )
        dataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(client, deliveryNoteId)

        dataSource.deleteDocumentClientOrIssuer(deliveryNoteId, ClientOrIssuerType.CLIENT)

        val deliveryNote = dataSource.fetch(deliveryNoteId)
        assertNull(deliveryNote?.documentClient)
    }

    // ============= UPDATE TESTS =============

    @Test
    fun updateDeliveryNote_changesNumber() = runTest {
        val deliveryNoteId = dataSource.createNew()
        val deliveryNote = dataSource.fetch(deliveryNoteId)!!

        val updated = deliveryNote.copy(documentNumber = TextFieldValue("BL-2024-001"))
        dataSource.update(updated)

        val fetched = dataSource.fetch(deliveryNoteId)
        assertEquals("BL-2024-001", fetched?.documentNumber?.text)
    }

    @Test
    fun updateDeliveryNote_changesDate() = runTest {
        val deliveryNoteId = dataSource.createNew()
        val deliveryNote = dataSource.fetch(deliveryNoteId)!!

        val updated = deliveryNote.copy(documentDate = "15/01/2024")
        dataSource.update(updated)

        val fetched = dataSource.fetch(deliveryNoteId)
        assertEquals("15/01/2024", fetched?.documentDate)
    }

    // ============= DELETE TESTS =============

    @Test
    fun deleteDeliveryNote_removesFromList() = runTest {
        val deliveryNoteId = dataSource.createNew()
        val deliveryNote = dataSource.fetch(deliveryNoteId)!!

        dataSource.delete(listOf(deliveryNote))

        assertEquals(0, dataSource.getDeliveryNoteCount())
    }

    @Test
    fun deleteMultipleDeliveryNotes() = runTest {
        dataSource.createNew()
        dataSource.createNew()
        dataSource.createNew()

        val toDelete = dataSource.getDeliveryNotes().take(2)
        dataSource.delete(toDelete)

        assertEquals(1, dataSource.getDeliveryNoteCount())
    }

    // ============= DUPLICATE TESTS =============

    @Test
    fun duplicateDeliveryNote_copiesAllData() = runTest {
        val deliveryNoteId = dataSource.createNew()
        val client = ClientOrIssuerState(name = TextFieldValue("Client"), type = ClientOrIssuerType.CLIENT)
        dataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(client, deliveryNoteId)
        val product = DocumentProductState(name = TextFieldValue("Produit"))
        dataSource.saveDocumentProductInDbAndLinkToDocument(product, deliveryNoteId)

        val deliveryNote = dataSource.fetch(deliveryNoteId)!!
        dataSource.duplicate(listOf(deliveryNote))

        assertEquals(2, dataSource.getDeliveryNoteCount())
        val duplicated = dataSource.getDeliveryNotes().last()
        assertNotNull(duplicated.documentClient)
        assertNotNull(duplicated.documentProducts)
    }

    @Test
    fun duplicateDeliveryNote_incrementsNumber() = runTest {
        val deliveryNoteId = dataSource.createNew()
        val deliveryNote = dataSource.fetch(deliveryNoteId)!!

        dataSource.duplicate(listOf(deliveryNote))

        val duplicated = dataSource.getDeliveryNotes().last()
        assertEquals("BL-002", duplicated.documentNumber.text)
    }

    @Test
    fun duplicateDeliveryNote_setsTagToDraft() = runTest {
        val deliveryNoteId = dataSource.createNew()
        val deliveryNote = dataSource.fetch(deliveryNoteId)!!

        dataSource.duplicate(listOf(deliveryNote))

        val duplicated = dataSource.getDeliveryNotes().last()
        assertEquals(DocumentTag.DRAFT, duplicated.documentTag)
    }

    // ============= PRODUCT ORDER TESTS =============

    @Test
    fun reorderProducts_movesCorrectly() = runTest {
        val deliveryNoteId = dataSource.createNew()
        val p1 = DocumentProductState(name = TextFieldValue("Premier"))
        val p2 = DocumentProductState(name = TextFieldValue("Deuxième"))
        val p3 = DocumentProductState(name = TextFieldValue("Troisième"))
        dataSource.saveDocumentProductInDbAndLinkToDocument(p1, deliveryNoteId)
        dataSource.saveDocumentProductInDbAndLinkToDocument(p2, deliveryNoteId)
        dataSource.saveDocumentProductInDbAndLinkToDocument(p3, deliveryNoteId)

        val deliveryNote = dataSource.fetch(deliveryNoteId)!!
        val reordered = deliveryNote.documentProducts!!.reversed()
        dataSource.updateDocumentProductsOrderInDb(deliveryNoteId, reordered)

        val updated = dataSource.fetch(deliveryNoteId)
        assertEquals("Troisième", updated?.documentProducts?.first()?.name?.text)
    }

    @Test
    fun reorderProducts_updatesOrder() = runTest {
        val deliveryNoteId = dataSource.createNew()
        val p1 = DocumentProductState(name = TextFieldValue("A"))
        val p2 = DocumentProductState(name = TextFieldValue("B"))
        dataSource.saveDocumentProductInDbAndLinkToDocument(p1, deliveryNoteId)
        dataSource.saveDocumentProductInDbAndLinkToDocument(p2, deliveryNoteId)

        val deliveryNote = dataSource.fetch(deliveryNoteId)!!
        dataSource.updateDocumentProductsOrderInDb(deliveryNoteId, deliveryNote.documentProducts!!)

        val updated = dataSource.fetch(deliveryNoteId)
        assertEquals(0, updated?.documentProducts?.get(0)?.sortOrder)
        assertEquals(1, updated?.documentProducts?.get(1)?.sortOrder)
    }

    // ============= FLOW TESTS =============

    @Test
    fun flowUpdates_onCreateDeliveryNote() = runTest {
        val initialDeliveryNotes = dataSource.fetchAll()?.first()
        assertTrue(initialDeliveryNotes?.isEmpty() ?: true)

        dataSource.createNew()

        val updatedDeliveryNotes = dataSource.fetchAll()?.first()
        assertEquals(1, updatedDeliveryNotes?.size)
    }

    @Test
    fun flowUpdates_onDeleteDeliveryNote() = runTest {
        val deliveryNoteId = dataSource.createNew()
        val deliveryNote = dataSource.fetch(deliveryNoteId)!!

        dataSource.delete(listOf(deliveryNote))

        val deliveryNotes = dataSource.fetchAll()?.first()
        assertTrue(deliveryNotes?.isEmpty() ?: true)
    }
}
