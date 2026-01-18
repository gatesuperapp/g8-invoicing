package com.a4a.g8invoicing.data

import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.models.TagUpdateOrCreationCase
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import kotlinx.coroutines.flow.Flow


interface InvoiceLocalDataSourceInterface {
    suspend fun fetch(id: Long): InvoiceState?
    fun fetchAll(): Flow<List<InvoiceState>>?
    suspend fun createNew(): Long?
    suspend fun saveDocumentProductInDbAndLinkToDocument(
        documentProduct: DocumentProductState,
        documentId: Long,
        deliveryNoteDate: String? = null,
        deliveryNoteNumber: String? = null,
    ): Int?

    suspend fun deleteDocumentProduct(id: Long, documentProductId: Long)
    suspend fun saveDocumentClientOrIssuerInDbAndLinkToDocument(
        documentClientOrIssuer: ClientOrIssuerState,
        id: Long?,
    )

    suspend fun deleteDocumentClientOrIssuer(id: Long, type: ClientOrIssuerType)
    suspend fun duplicate(documents: List<InvoiceState>)
    suspend fun convertDeliveryNotesToInvoice(deliveryNotes: List<DeliveryNoteState>)
    suspend fun update(document: InvoiceState)
    suspend fun delete(documents: List<InvoiceState>)
    suspend fun setTag(documents: List<InvoiceState>, tag: DocumentTag, tagUpdateCase: TagUpdateOrCreationCase)
    suspend fun deleteTag(invoiceId: Long)
    suspend fun markAsPaid(documents: List<InvoiceState>, tag: DocumentTag)
    suspend fun updateDocumentProductsOrderInDb(documentId: Long, orderedProducts: List<DocumentProductState>)
}
