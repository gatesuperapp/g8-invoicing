package com.a4a.g8invoicing.data

import com.a4a.g8invoicing.data.models.ProductNature
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.ProductState
import kotlinx.coroutines.flow.Flow
import com.ionspin.kotlin.bignum.decimal.BigDecimal

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
    suspend fun clearTaxRateFromProducts(taxId: Long)
    /** Last 5 distinct UNECE unit codes ever used on Products, most-recent first.
     * Feeds the "Récentes" section of the UnitCodePicker bottom sheet. */
    suspend fun fetchLast5UnitCodes(): List<String>
    /** Last non-null ProductType used on a Product, for sticky-default on new products. */
    suspend fun fetchLastUsedProductType(): ProductNature?
    /** Most recently created Product (highest id). Used to pre-fill unit / unitCode /
     * taxRate on a fresh "new product" flow — the user's mental model is that these
     * settings carry over from one product to the next. */
    suspend fun fetchLastCreatedProduct(): ProductState?
    /** Bulk-set Product.type on every row in the Product table. Used by the 1.8
     * onboarding "Do you sell only services / only goods / a mix?" answer to
     * preseed the type on all existing products (which had null type pre-1.8). */
    suspend fun updateAllProductTypes(newType: ProductNature)
}
