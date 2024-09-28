package com.a4a.g8invoicing.data

import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.CreditNoteState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType
import dagger.Binds
import kotlinx.coroutines.flow.Flow

interface CreditNoteLocalDataSourceInterface {
    fun fetch(id: Long): CreditNoteState?
    fun fetchAll(): Flow<List<CreditNoteState>>?
    suspend fun createNew(): Long?
    suspend fun saveDocumentProductInDbAndLinkToDocument(documentProduct: DocumentProductState, id: Long?,
                                                         deliveryNoteDate: String? = null,
                                                         deliveryNoteNumber: String? = null): Int?
    suspend fun deleteDocumentProduct(id:Long, documentProductId: Long)
    suspend fun saveDocumentClientOrIssuerInDbAndLinkToDocument(documentClientOrIssuer: ClientOrIssuerState, id: Long?)
    suspend fun deleteDocumentClientOrIssuer(id:Long, type: ClientOrIssuerType,)
    suspend fun duplicate(documents: List<CreditNoteState>)
    suspend fun convertInvoiceToCreditNote(documents: List<InvoiceState>)
    suspend fun update(document: CreditNoteState)
    suspend fun delete(documents: List<CreditNoteState>)
}
