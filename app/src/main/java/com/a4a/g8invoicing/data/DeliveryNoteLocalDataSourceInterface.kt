package com.a4a.g8invoicing.data

import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import kotlinx.coroutines.flow.Flow

/**
 * Interface for DeliveryNoteLocalDataSourceImpl
 * Makes db implementation swappable
 *
 */


interface DeliveryNoteLocalDataSourceInterface {
    fun fetchDeliveryNoteFlow(id: Long): Flow<DeliveryNoteState?>
    fun fetchDeliveryNote(id: Long): DeliveryNoteState?
    fun fetchAllDeliveryNotes(): Flow<List<DeliveryNoteState>>
    fun saveDeliveryNote(): Long?
    suspend fun saveDocumentProductInDbAndLinkToDeliveryNote(documentProduct: DocumentProductState,  deliveryNoteId: Long?)
    suspend fun duplicateDeliveryNotes(deliveryNotes: List<DeliveryNoteState>): Long?
    suspend fun updateDeliveryNote(deliveryNote: DeliveryNoteState)
    suspend fun deleteDeliveryNotes(deliveryNotes: List<DeliveryNoteState>)
    suspend fun deleteDeliveryNoteProduct(deliveryNoteId:Long, documentProductId: Long)
    suspend fun addDeliveryNoteProduct(deliveryNoteId: Long, documentProductId: Long)
}
