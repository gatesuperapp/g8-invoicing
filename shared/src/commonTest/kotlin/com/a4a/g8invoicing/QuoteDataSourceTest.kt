package com.a4a.g8invoicing

import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.util.DateUtils
import com.a4a.g8invoicing.fakes.FakeQuoteDataSource
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

class QuoteDataSourceTest {

    private lateinit var dataSource: FakeQuoteDataSource

    @BeforeTest
    fun setup() {
        dataSource = FakeQuoteDataSource()
    }

    // ============= CREATION TESTS =============

    @Test
    fun createQuote_createsEmptyDocument() = runTest {
        val quoteId = dataSource.createNew()

        assertNotNull(quoteId)
        assertEquals(1, dataSource.getQuoteCount())
        val quote = dataSource.fetch(quoteId)
        assertNotNull(quote)
        assertEquals(DocumentTag.DRAFT, quote.documentTag)
    }

    @Test
    fun createQuote_setsDateToToday() = runTest {
        val quoteId = dataSource.createNew()

        val quote = dataSource.fetch(quoteId)
        val todayFormatted = DateUtils.getCurrentDateFormatted()

        assertNotNull(quote)
        assertEquals(todayFormatted, quote.documentDate)
    }

    @Test
    fun createQuote_setsCreatedDate() = runTest {
        val quoteId = dataSource.createNew()

        val quote = dataSource.fetch(quoteId)

        assertNotNull(quote?.createdDate)
        assertTrue(quote?.createdDate?.isNotBlank() == true)
    }

    @Test
    fun createMultipleQuotes_assignsUniqueIds() = runTest {
        val id1 = dataSource.createNew()
        val id2 = dataSource.createNew()
        val id3 = dataSource.createNew()

        assertEquals(3, dataSource.getQuoteCount())
        assertTrue(id1 != id2 && id2 != id3)
    }

    @Test
    fun createQuote_generatesSequentialNumbers() = runTest {
        dataSource.createNew()
        dataSource.createNew()
        dataSource.createNew()

        val quotes = dataSource.getQuotes()
        assertEquals("D-001", quotes[0].documentNumber.text)
        assertEquals("D-002", quotes[1].documentNumber.text)
        assertEquals("D-003", quotes[2].documentNumber.text)
    }

    // ============= PRODUCT TESTS =============

    @Test
    fun addProductToQuote_savesCorrectly() = runTest {
        val quoteId = dataSource.createNew()
        val product = DocumentProductState(
            name = TextFieldValue("Produit Test"),
            priceWithoutTax = BigDecimal.fromDouble(100.0),
            quantity = BigDecimal.ONE
        )

        val productId = dataSource.saveDocumentProductInDbAndLinkToDocument(
            documentProduct = product,
            documentId = quoteId
        )

        assertTrue(productId!! > 0)
        val quote = dataSource.fetch(quoteId)
        assertEquals(1, quote?.documentProducts?.size)
    }

    @Test
    fun addMultipleProductsToQuote() = runTest {
        val quoteId = dataSource.createNew()
        val product1 = DocumentProductState(name = TextFieldValue("Produit 1"))
        val product2 = DocumentProductState(name = TextFieldValue("Produit 2"))
        val product3 = DocumentProductState(name = TextFieldValue("Produit 3"))

        dataSource.saveDocumentProductInDbAndLinkToDocument(product1, quoteId)
        dataSource.saveDocumentProductInDbAndLinkToDocument(product2, quoteId)
        dataSource.saveDocumentProductInDbAndLinkToDocument(product3, quoteId)

        val quote = dataSource.fetch(quoteId)
        assertEquals(3, quote?.documentProducts?.size)
    }

    @Test
    fun removeProductFromQuote() = runTest {
        val quoteId = dataSource.createNew()
        val product = DocumentProductState(name = TextFieldValue("À Retirer"))
        val productId = dataSource.saveDocumentProductInDbAndLinkToDocument(product, quoteId)

        dataSource.deleteDocumentProduct(quoteId, productId!!.toLong())

        val quote = dataSource.fetch(quoteId)
        assertTrue(quote?.documentProducts?.isEmpty() ?: true)
    }

    // ============= CLIENT/ISSUER TESTS =============

    @Test
    fun addClientToQuote() = runTest {
        val quoteId = dataSource.createNew()
        val client = ClientOrIssuerState(
            name = TextFieldValue("Client Test"),
            type = ClientOrIssuerType.CLIENT
        )

        dataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(client, quoteId)

        val quote = dataSource.fetch(quoteId)
        assertNotNull(quote?.documentClient)
        assertEquals("Client Test", quote?.documentClient?.name?.text)
    }

    @Test
    fun addIssuerToQuote() = runTest {
        val quoteId = dataSource.createNew()
        val issuer = ClientOrIssuerState(
            name = TextFieldValue("Mon Entreprise"),
            type = ClientOrIssuerType.ISSUER
        )

        dataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(issuer, quoteId)

        val quote = dataSource.fetch(quoteId)
        assertNotNull(quote?.documentIssuer)
    }

    @Test
    fun removeClientFromQuote() = runTest {
        val quoteId = dataSource.createNew()
        val client = ClientOrIssuerState(
            name = TextFieldValue("À Retirer"),
            type = ClientOrIssuerType.CLIENT
        )
        dataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(client, quoteId)

        dataSource.deleteDocumentClientOrIssuer(quoteId, ClientOrIssuerType.CLIENT)

        val quote = dataSource.fetch(quoteId)
        assertNull(quote?.documentClient)
    }

    // ============= UPDATE TESTS =============

    @Test
    fun updateQuote_changesNumber() = runTest {
        val quoteId = dataSource.createNew()
        val quote = dataSource.fetch(quoteId)!!

        val updated = quote.copy(documentNumber = TextFieldValue("D-2024-001"))
        dataSource.update(updated)

        val fetched = dataSource.fetch(quoteId)
        assertEquals("D-2024-001", fetched?.documentNumber?.text)
    }

    @Test
    fun updateQuote_changesDate() = runTest {
        val quoteId = dataSource.createNew()
        val quote = dataSource.fetch(quoteId)!!

        val updated = quote.copy(documentDate = "15/01/2024")
        dataSource.update(updated)

        val fetched = dataSource.fetch(quoteId)
        assertEquals("15/01/2024", fetched?.documentDate)
    }

    // ============= DELETE TESTS =============

    @Test
    fun deleteQuote_removesFromList() = runTest {
        val quoteId = dataSource.createNew()
        val quote = dataSource.fetch(quoteId)!!

        dataSource.delete(listOf(quote))

        assertEquals(0, dataSource.getQuoteCount())
    }

    @Test
    fun deleteMultipleQuotes() = runTest {
        dataSource.createNew()
        dataSource.createNew()
        dataSource.createNew()

        val toDelete = dataSource.getQuotes().take(2)
        dataSource.delete(toDelete)

        assertEquals(1, dataSource.getQuoteCount())
    }

    // ============= DUPLICATE TESTS =============

    @Test
    fun duplicateQuote_copiesAllData() = runTest {
        val quoteId = dataSource.createNew()
        val client = ClientOrIssuerState(name = TextFieldValue("Client"), type = ClientOrIssuerType.CLIENT)
        dataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(client, quoteId)
        val product = DocumentProductState(name = TextFieldValue("Produit"))
        dataSource.saveDocumentProductInDbAndLinkToDocument(product, quoteId)

        val quote = dataSource.fetch(quoteId)!!
        dataSource.duplicate(listOf(quote))

        assertEquals(2, dataSource.getQuoteCount())
        val duplicated = dataSource.getQuotes().last()
        assertNotNull(duplicated.documentClient)
        assertNotNull(duplicated.documentProducts)
    }

    @Test
    fun duplicateQuote_incrementsNumber() = runTest {
        val quoteId = dataSource.createNew()
        val quote = dataSource.fetch(quoteId)!!

        dataSource.duplicate(listOf(quote))

        val duplicated = dataSource.getQuotes().last()
        assertEquals("D-002", duplicated.documentNumber.text)
    }

    @Test
    fun duplicateQuote_setsTagToDraft() = runTest {
        val quoteId = dataSource.createNew()
        val quote = dataSource.fetch(quoteId)!!

        dataSource.duplicate(listOf(quote))

        val duplicated = dataSource.getQuotes().last()
        assertEquals(DocumentTag.DRAFT, duplicated.documentTag)
    }

    // ============= PRODUCT ORDER TESTS =============

    @Test
    fun reorderProducts_movesCorrectly() = runTest {
        val quoteId = dataSource.createNew()
        val p1 = DocumentProductState(name = TextFieldValue("Premier"))
        val p2 = DocumentProductState(name = TextFieldValue("Deuxième"))
        val p3 = DocumentProductState(name = TextFieldValue("Troisième"))
        dataSource.saveDocumentProductInDbAndLinkToDocument(p1, quoteId)
        dataSource.saveDocumentProductInDbAndLinkToDocument(p2, quoteId)
        dataSource.saveDocumentProductInDbAndLinkToDocument(p3, quoteId)

        val quote = dataSource.fetch(quoteId)!!
        val reordered = quote.documentProducts!!.reversed()
        dataSource.updateDocumentProductsOrderInDb(quoteId, reordered)

        val updated = dataSource.fetch(quoteId)
        assertEquals("Troisième", updated?.documentProducts?.first()?.name?.text)
    }

    @Test
    fun reorderProducts_updatesOrder() = runTest {
        val quoteId = dataSource.createNew()
        val p1 = DocumentProductState(name = TextFieldValue("A"))
        val p2 = DocumentProductState(name = TextFieldValue("B"))
        dataSource.saveDocumentProductInDbAndLinkToDocument(p1, quoteId)
        dataSource.saveDocumentProductInDbAndLinkToDocument(p2, quoteId)

        val quote = dataSource.fetch(quoteId)!!
        dataSource.updateDocumentProductsOrderInDb(quoteId, quote.documentProducts!!)

        val updated = dataSource.fetch(quoteId)
        assertEquals(0, updated?.documentProducts?.get(0)?.sortOrder)
        assertEquals(1, updated?.documentProducts?.get(1)?.sortOrder)
    }

    // ============= FLOW TESTS =============

    @Test
    fun flowUpdates_onCreateQuote() = runTest {
        val initialQuotes = dataSource.fetchAll()?.first()
        assertTrue(initialQuotes?.isEmpty() ?: true)

        dataSource.createNew()

        val updatedQuotes = dataSource.fetchAll()?.first()
        assertEquals(1, updatedQuotes?.size)
    }

    @Test
    fun flowUpdates_onDeleteQuote() = runTest {
        val quoteId = dataSource.createNew()
        val quote = dataSource.fetch(quoteId)!!

        dataSource.delete(listOf(quote))

        val quotes = dataSource.fetchAll()?.first()
        assertTrue(quotes?.isEmpty() ?: true)
    }
}
