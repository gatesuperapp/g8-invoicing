package com.a4a.g8invoicing.data

import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.models.TagUpdateOrCreationCase
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.states.SalesOrderState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import kotlinx.coroutines.flow.Flow

interface SalesOrderLocalDataSourceInterface {
    suspend fun fetch(id: Long): SalesOrderState?
    fun fetchAll(): Flow<List<SalesOrderState>>?
    suspend fun createNew(): Long?
    suspend fun saveDocumentProductInDbAndLinkToDocument(
        documentProduct: DocumentProductState,
        documentId: Long,
    ): Int?

    suspend fun deleteDocumentProduct(documentId: Long, documentProductId: Long)
    suspend fun saveDocumentClientOrIssuerInDbAndLinkToDocument(
        documentClientOrIssuer: ClientOrIssuerState,
        documentId: Long?,
    )

    suspend fun deleteDocumentClientOrIssuer(documentId: Long, type: ClientOrIssuerType)
    suspend fun duplicate(documents: List<SalesOrderState>)
    suspend fun update(document: SalesOrderState)
    suspend fun delete(documents: List<SalesOrderState>)
    suspend fun updateDocumentProductsOrderInDb(
        documentId: Long,
        orderedProducts: List<DocumentProductState>,
    )

    suspend fun setTag(
        documents: List<SalesOrderState>,
        tag: DocumentTag,
        tagUpdateCase: TagUpdateOrCreationCase,
    )
}
