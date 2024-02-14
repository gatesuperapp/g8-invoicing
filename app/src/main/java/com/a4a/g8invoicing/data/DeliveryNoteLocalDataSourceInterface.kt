package com.a4a.g8invoicing.data

import g8invoicing.DeliveryNote
import kotlinx.coroutines.flow.Flow

/**
 * Interface for DeliveryNoteLocalDataSourceImpl
 * Makes db implementation swappable
 *
 */


interface DeliveryNoteLocalDataSourceInterface {
    fun fetchDeliveryNote(id: Long): DeliveryNoteEditable?
    fun fetchAllDeliveryNotes(): Flow<List<DeliveryNoteEditable>>
    suspend fun saveDeliveryNote(deliveryNote: DeliveryNoteEditable)
    suspend fun duplicateDeliveryNote(deliveryNote: DeliveryNoteEditable)
    suspend fun updateDeliveryNote(deliveryNote: DeliveryNoteEditable)
    suspend fun deleteDeliveryNote(id: Long)
    suspend fun deleteDeliveryNoteProduct(documentProductId: Long)
    suspend fun addDeliveryNoteProduct(deliveryNoteId: Long, productId: Long)
}
