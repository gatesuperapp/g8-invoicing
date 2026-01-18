package com.a4a.g8invoicing.data

import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import kotlinx.coroutines.flow.Flow

/**
 * Interface for DeliveryNoteLocalDataSourceImpl
 * Makes db implementation swappable
 *
 */


interface DeliveryNoteLocalDataSourceInterface {
    // fun fetchDeliveryNoteFlow(id: Long): Flow<DeliveryNoteState?>
    suspend fun fetch(id: Long): DeliveryNoteState?
    fun fetchAll(): Flow<List<DeliveryNoteState>>?
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
    suspend fun duplicate(documents: List<DeliveryNoteState>)
    suspend fun update(document: DeliveryNoteState)
    suspend fun delete(documents: List<DeliveryNoteState>)
    suspend fun updateDocumentProductsOrderInDb(documentId: Long, orderedProducts: List<DocumentProductState>)

}
