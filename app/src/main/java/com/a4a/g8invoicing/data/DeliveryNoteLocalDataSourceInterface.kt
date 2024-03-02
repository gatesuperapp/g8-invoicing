package com.a4a.g8invoicing.data

import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import kotlinx.coroutines.flow.Flow

/**
 * Interface for DeliveryNoteLocalDataSourceImpl
 * Makes db implementation swappable
 *
 */


interface DeliveryNoteLocalDataSourceInterface {
    fun fetchDeliveryNote(id: Long): Flow<DeliveryNoteState?>
    fun fetchAllDeliveryNotes(): Flow<List<DeliveryNoteState>>
    suspend fun saveDeliveryNote(deliveryNote: DeliveryNoteState)
    suspend fun duplicateDeliveryNote(deliveryNote: DeliveryNoteState)
    suspend fun updateDeliveryNote(deliveryNote: DeliveryNoteState)
    suspend fun deleteDeliveryNote(id: Long)
    suspend fun deleteDeliveryNoteProduct(documentProductId: Long)
    suspend fun addDeliveryNoteProduct(deliveryNoteId: Long, productId: Long)
}
