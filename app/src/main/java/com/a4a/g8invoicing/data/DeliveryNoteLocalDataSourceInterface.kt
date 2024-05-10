package com.a4a.g8invoicing.data

import com.a4a.g8invoicing.ui.states.DeliveryNoteState
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
    suspend fun duplicateDeliveryNote(deliveryNote: DeliveryNoteState): Long?
    suspend fun updateDeliveryNote(deliveryNote: DeliveryNoteState)
    suspend fun deleteDeliveryNote(id: Long)
    suspend fun deleteDeliveryNoteProduct(id:Long, documentProductId: Long)
    suspend fun addDeliveryNoteProduct(deliveryNoteId: Long, documentProductId: Long)
}
