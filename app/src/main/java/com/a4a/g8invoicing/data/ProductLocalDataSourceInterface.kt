package com.a4a.g8invoicing.data

import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.ProductState
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

/**
 * Interface for ProductLocalDataSourceImpl
 * Makes db implementation swappable
 *
 */

interface ProductLocalDataSourceInterface {
    suspend fun fetchProduct(id: Long): ProductState?
    fun fetchAllProducts(): Flow<List<ProductState>>
    suspend fun saveProduct(product: ProductState)
    suspend fun duplicateProducts(products: List<ProductState>, duplicateNameSuffix: String)
    suspend fun updateProduct(product: ProductState)
    suspend fun updateDocumentProduct(documentProduct: DocumentProductState)
    suspend fun deleteProduct(id: Long)
    suspend fun deleteDocumentProducts(ids: List<Long>)
    suspend fun deleteAdditionalPrice(productId: Long, priceWithoutTax: BigDecimal)
    suspend fun removeClientFromAdditionalPrice(productId: Long, clientId: Long)
}
