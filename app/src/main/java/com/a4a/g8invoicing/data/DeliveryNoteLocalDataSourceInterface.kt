package com.a4a.g8invoicing.data

import com.a4a.g8invoicing.ui.states.DeliveryNote
import com.a4a.g8invoicing.ui.states.DocumentProductState
import kotlinx.coroutines.flow.Flow

/**
 * Interface for DeliveryNoteLocalDataSourceImpl
 * Makes db implementation swappable
 *
 */


interface DeliveryNoteLocalDataSourceInterface {
    fun fetchDeliveryNoteFlow(id: Long): Flow<DeliveryNote?>
    fun fetchDeliveryNote(id: Long): DeliveryNote?
    fun fetchAllDeliveryNotes(): Flow<List<DeliveryNote>>
    fun saveDeliveryNote(): Long?
    suspend fun saveDocumentProductInDbAndLinkToDeliveryNote(documentProduct: DocumentProductState,  deliveryNoteId: Long?)
    suspend fun duplicateDeliveryNotes(deliveryNotes: List<DeliveryNote>): Long?
    suspend fun updateDeliveryNote(deliveryNote: DeliveryNote)
    suspend fun deleteDeliveryNote(id: Long)
    suspend fun deleteDeliveryNoteProduct(id:Long, documentProductId: Long)
    suspend fun addDeliveryNoteProduct(deliveryNoteId: Long, documentProductId: Long)
}
