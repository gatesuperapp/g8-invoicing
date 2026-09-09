package com.a4a.g8invoicing.data

import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.models.TagUpdateOrCreationCase
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.states.QuoteState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.RetentionState
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
    suspend fun setTag(
        documents: List<QuoteState>,
        tag: DocumentTag,
        tagUpdateCase: TagUpdateOrCreationCase,
    )

    // Retention CRUD — mirrors InvoiceLocalDataSourceInterface. Used by the
    // ViewModel when the user toggles the retention switch on the doc's issuer
    // (clear on OFF, seed defaults on ON) so the change lands in DB before the
    // subsequent reloadDocument reads state back.
    suspend fun deleteAllRetentions(quoteId: Long)
    suspend fun saveRetentions(quoteId: Long, retentions: List<RetentionState>)

    // BT-120 seed on EDIT_ISSUER vatExempt flip — mirrors Invoice /
    // CreditNote so a devis whose issuer just went from taxed to franchise
    // gets the country default persisted before the subsequent reload.
    suspend fun updateVatExemptionText(quoteId: Long, text: String?)
}
