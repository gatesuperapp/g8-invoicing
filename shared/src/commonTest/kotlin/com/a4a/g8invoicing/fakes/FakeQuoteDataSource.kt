package com.a4a.g8invoicing.fakes

import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.data.QuoteLocalDataSourceInterface
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.util.DateUtils
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.QuoteState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeQuoteDataSource : QuoteLocalDataSourceInterface {

    private val quotes = mutableListOf<QuoteState>()
    private val quotesFlow = MutableStateFlow<List<QuoteState>>(emptyList())
    private var nextQuoteId = 1
    private var nextProductId = 1

    fun getQuotes(): List<QuoteState> = quotes.toList()
    fun getQuoteCount(): Int = quotes.size
    fun clear() {
        quotes.clear()
        quotesFlow.value = emptyList()
        nextQuoteId = 1
        nextProductId = 1
    }

    override suspend fun fetch(id: Long): QuoteState? {
        return quotes.find { it.documentId == id.toInt() }
    }

    override fun fetchAll(): Flow<List<QuoteState>> {
        return quotesFlow
    }

    override suspend fun createNew(): Long {
        val todayFormatted = DateUtils.getCurrentDateFormatted()
        val newQuote = QuoteState(
            documentId = nextQuoteId,
            documentNumber = TextFieldValue("D-${nextQuoteId.toString().padStart(3, '0')}"),
            documentDate = todayFormatted,
            documentTag = DocumentTag.DRAFT,
            createdDate = DateUtils.getCurrentTimestamp()
        )
        quotes.add(newQuote)
        quotesFlow.value = quotes.toList()
        return nextQuoteId++.toLong()
    }

    override suspend fun saveDocumentProductInDbAndLinkToDocument(
        documentProduct: DocumentProductState,
        documentId: Long
    ): Int {
        val quote = quotes.find { it.documentId == documentId.toInt() }
        quote?.let {
            val productId = nextProductId++
            val product = documentProduct.copy(id = productId)
            val currentProducts = it.documentProducts?.toMutableList() ?: mutableListOf()
            currentProducts.add(product)
            val index = quotes.indexOf(it)
            quotes[index] = it.copy(documentProducts = currentProducts)
            quotesFlow.value = quotes.toList()
            return productId
        }
        return -1
    }

    override suspend fun deleteDocumentProduct(documentId: Long, documentProductId: Long) {
        val quote = quotes.find { it.documentId == documentId.toInt() }
        quote?.let {
            val updatedProducts = it.documentProducts?.filterNot { product ->
                product.id == documentProductId.toInt()
            }
            val index = quotes.indexOf(it)
            quotes[index] = it.copy(documentProducts = updatedProducts)
            quotesFlow.value = quotes.toList()
        }
    }

    override suspend fun saveDocumentClientOrIssuerInDbAndLinkToDocument(
        documentClientOrIssuer: ClientOrIssuerState,
        documentId: Long?
    ) {
        val quote = quotes.find { it.documentId == documentId?.toInt() }
        quote?.let {
            val index = quotes.indexOf(it)
            if (documentClientOrIssuer.type == ClientOrIssuerType.CLIENT) {
                quotes[index] = it.copy(documentClient = documentClientOrIssuer)
            } else {
                quotes[index] = it.copy(documentIssuer = documentClientOrIssuer)
            }
            quotesFlow.value = quotes.toList()
        }
    }

    override suspend fun deleteDocumentClientOrIssuer(documentId: Long, type: ClientOrIssuerType) {
        val quote = quotes.find { it.documentId == documentId.toInt() }
        quote?.let {
            val index = quotes.indexOf(it)
            if (type == ClientOrIssuerType.CLIENT) {
                quotes[index] = it.copy(documentClient = null)
            } else {
                quotes[index] = it.copy(documentIssuer = null)
            }
            quotesFlow.value = quotes.toList()
        }
    }

    override suspend fun duplicate(documents: List<QuoteState>) {
        documents.forEach { doc ->
            val newId = nextQuoteId++
            val duplicate = doc.copy(
                documentId = newId,
                documentNumber = TextFieldValue("D-${newId.toString().padStart(3, '0')}"),
                documentTag = DocumentTag.DRAFT
            )
            quotes.add(duplicate)
        }
        quotesFlow.value = quotes.toList()
    }

    override suspend fun update(document: QuoteState) {
        val index = quotes.indexOfFirst { it.documentId == document.documentId }
        if (index >= 0) {
            quotes[index] = document
            quotesFlow.value = quotes.toList()
        }
    }

    override suspend fun delete(documents: List<QuoteState>) {
        val idsToDelete = documents.mapNotNull { it.documentId }
        quotes.removeAll { it.documentId in idsToDelete }
        quotesFlow.value = quotes.toList()
    }

    override suspend fun updateDocumentProductsOrderInDb(
        documentId: Long,
        orderedProducts: List<DocumentProductState>
    ) {
        val index = quotes.indexOfFirst { it.documentId == documentId.toInt() }
        if (index >= 0) {
            val reorderedProducts = orderedProducts.mapIndexed { i, product ->
                product.copy(sortOrder = i)
            }
            quotes[index] = quotes[index].copy(documentProducts = reorderedProducts)
            quotesFlow.value = quotes.toList()
        }
    }
}
