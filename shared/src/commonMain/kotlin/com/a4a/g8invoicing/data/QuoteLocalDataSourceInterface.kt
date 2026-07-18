package com.a4a.g8invoicing.data

import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.ui.states.QuoteState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import kotlinx.coroutines.flow.Flow

/**
 * Interface for QuoteLocalDataSourceImpl
 * Makes db implementation swappable
 *
 */


interface QuoteLocalDataSourceInterface {
    // fun fetchQuoteFlow(id: Long): Flow<QuoteState?>
    suspend fun fetch(id: Long): QuoteState?
    fun fetchAll(): Flow<List<QuoteState>>?
    suspend fun createNew(): Long?
    suspend fun saveDocumentProductInDbAndLinkToDocument(
        documentProduct: DocumentProductState,
        documentId: Long
    ): Int?

    suspend fun deleteDocumentProduct(documentId: Long, documentProductId: Long)
    suspend fun saveDocumentClientOrIssuerInDbAndLinkToDocument(
        documentClientOrIssuer: ClientOrIssuerState,
        documentId: Long?,
    )

    suspend fun deleteDocumentClientOrIssuer(documentId: Long, type: ClientOrIssuerType)
    suspend fun duplicate(documents: List<QuoteState>)
    suspend fun update(document: QuoteState)
    suspend fun delete(documents: List<QuoteState>)
    suspend fun updateDocumentProductsOrderInDb(documentId: Long, orderedProducts: List<DocumentProductState>)

}
