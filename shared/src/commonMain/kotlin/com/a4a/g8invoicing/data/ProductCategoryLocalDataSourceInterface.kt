package com.a4a.g8invoicing.data

import com.a4a.g8invoicing.ui.states.ProductCategoryState
import kotlinx.coroutines.flow.Flow

/**
 * CRUD for ProductCategory (release 2.1 premium module).
 */
interface ProductCategoryLocalDataSourceInterface {
    fun fetchAllCategoriesFlow(): Flow<List<ProductCategoryState>>
    suspend fun fetchAllCategories(): List<ProductCategoryState>
    suspend fun fetchCategory(id: Long): ProductCategoryState?
    suspend fun saveCategory(category: ProductCategoryState): Long?
    suspend fun updateCategory(category: ProductCategoryState)
    suspend fun deleteCategory(id: Long)
    /** Bulk-assign a (nullable) category to the given product ids. */
    suspend fun assignCategoryToProducts(productIds: List<Long>, categoryId: Long?)
}
