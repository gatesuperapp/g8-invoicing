package com.a4a.g8invoicing.data

import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.ProductState
import kotlinx.coroutines.flow.Flow

/**
 * Interface for ProductLocalDataSourceImpl
 * Makes db implementation swappable
 *
 */

interface ProductLocalDataSourceInterface {
    fun fetchProduct(id: Long): ProductState?
    fun fetchDocumentProduct(id: Long): DocumentProductState?
    fun fetchAllProducts(): Flow<List<ProductState>>
    suspend fun saveProduct(product: ProductState)
    suspend fun saveDocumentProduct(documentProduct: DocumentProductState): Int?
    suspend fun duplicateProduct(product: ProductState)
    suspend fun updateProduct(product: ProductState)
    suspend fun updateDocumentProduct(documentProduct: DocumentProductState)
    fun checkIfEmpty(): Int
    suspend fun deleteProduct(id: Long)
    suspend fun deleteDocumentProduct(id: Long)
}
