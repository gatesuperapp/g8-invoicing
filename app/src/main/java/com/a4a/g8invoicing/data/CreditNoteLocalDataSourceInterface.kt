package com.a4a.g8invoicing.data

import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType
import com.a4a.g8invoicing.ui.states.CreditNoteState
import com.a4a.g8invoicing.ui.states.DocumentClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import kotlinx.coroutines.flow.Flow

interface CreditNoteLocalDataSourceInterface {
    fun fetch(id: Long): CreditNoteState?
    fun fetchAll(): Flow<List<CreditNoteState>>?
    suspend fun createNew(): Long?
    suspend fun saveDocumentProductInDbAndLinkToDocument(documentProduct: DocumentProductState, id: Long?,
                                                         deliveryNoteDate: String? = null,
                                                         deliveryNoteNumber: String? = null)
    suspend fun deleteDocumentProduct(id:Long, documentProductId: Long)
    suspend fun saveDocumentClientOrIssuerInDbAndLinkToDocument(documentClientOrIssuer: DocumentClientOrIssuerState,  id: Long?)
    suspend fun deleteDocumentClientOrIssuer(id:Long, type: ClientOrIssuerType,)
    suspend fun duplicate(documents: List<CreditNoteState>)
    suspend fun convertDeliveryNotesToCreditNote(deliveryNotes: List<DeliveryNoteState>)
    suspend fun update(document: CreditNoteState)
    suspend fun delete(documents: List<CreditNoteState>)
    suspend fun markAsPaid(documents: List<CreditNoteState>, isPaid: Boolean = true)
}
