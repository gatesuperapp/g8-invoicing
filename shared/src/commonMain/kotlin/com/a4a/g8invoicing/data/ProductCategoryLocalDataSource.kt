package com.a4a.g8invoicing.data

import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.data.util.DispatcherProvider
import com.a4a.g8invoicing.ui.states.ProductCategoryState
import g8invoicing.ProductCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ProductCategoryLocalDataSource(
    db: Database,
) : ProductCategoryLocalDataSourceInterface {
    private val productCategoryQueries = db.productCategoryQueries
    private val productQueries = db.productQueries

    override fun fetchAllCategoriesFlow(): Flow<List<ProductCategoryState>> =
        productCategoryQueries.getAllCategories().asFlow().map { query ->
            query.executeAsList().map { it.toState() }
        }

    override suspend fun fetchAllCategories(): List<ProductCategoryState> =
        withContext(DispatcherProvider.IO) {
            productCategoryQueries.getAllCategories().executeAsList().map { it.toState() }
        }

    override suspend fun fetchCategory(id: Long): ProductCategoryState? =
        withContext(DispatcherProvider.IO) {
            productCategoryQueries.getCategory(id).executeAsOneOrNull()?.toState()
        }

    override suspend fun saveCategory(category: ProductCategoryState): Long? =
        withContext(DispatcherProvider.IO) {
            runCatching {
                productCategoryQueries.transaction {
                    productCategoryQueries.saveCategory(
                        name = category.name,
                        color = category.color,
                        sort_order = category.sortOrder.toLong(),
                    )
                }
                productCategoryQueries.lastInsertRowId().executeAsOne()
            }.getOrNull()
        }

    override suspend fun updateCategory(category: ProductCategoryState) {
        val id = category.id?.toLong() ?: return
        withContext(DispatcherProvider.IO) {
            productCategoryQueries.updateCategory(
                id = id,
                name = category.name,
                color = category.color,
            )
        }
    }

    override suspend fun deleteCategory(id: Long) {
        withContext(DispatcherProvider.IO) {
            // FK on Product.product_category_id is ON DELETE SET NULL, so any
            // product that pointed at this row loses its category without the
            // row itself vanishing — matches tax-rate deletion semantics.
            productCategoryQueries.deleteCategory(id)
        }
    }

    override suspend fun assignCategoryToProducts(productIds: List<Long>, categoryId: Long?) {
        if (productIds.isEmpty()) return
        withContext(DispatcherProvider.IO) {
            productQueries.transaction {
                productIds.forEach { id ->
                    productQueries.updateProductCategoryId(
                        product_category_id = categoryId,
                        id = id,
                    )
                }
            }
        }
    }

    private fun ProductCategory.toState() = ProductCategoryState(
        id = product_category_id.toInt(),
        name = name,
        color = color,
        sortOrder = sort_order.toInt(),
    )
}
