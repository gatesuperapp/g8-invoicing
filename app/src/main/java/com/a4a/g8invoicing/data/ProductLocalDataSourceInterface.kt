package com.a4a.g8invoicing.data

import kotlinx.coroutines.flow.Flow

/**
 * Interface for ProductLocalDataSourceImpl
 * Makes db implementation swappable
 *
 */

interface ProductLocalDataSourceInterface {
    fun fetchProduct(id: Long): ProductEditable?
    fun fetchDocumentProduct(id: Long): DocumentProductEditable?
    fun fetchAllProducts(): Flow<List<ProductEditable>>
    suspend fun saveProduct(product: ProductEditable)
    suspend fun saveDocumentProduct(documentProduct: DocumentProductEditable)
    suspend fun duplicateProduct(product: ProductEditable)
    suspend fun updateProduct(product: ProductEditable)
    suspend fun updateDocumentProduct(documentProduct: DocumentProductEditable)
    fun checkIfEmpty(): Int
    suspend fun deleteProduct(id: Long)
    suspend fun deleteDocumentProduct(id: Long)
}
