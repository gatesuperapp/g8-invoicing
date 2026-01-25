package com.a4a.g8invoicing.fakes

import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.ProductPrice
import com.a4a.g8invoicing.ui.states.ProductState
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeProductDataSource : ProductLocalDataSourceInterface {

    private val products = mutableListOf<ProductState>()
    private val productsFlow = MutableStateFlow<List<ProductState>>(emptyList())
    private var nextId = 1

    // For testing: access internal state
    fun getProducts(): List<ProductState> = products.toList()
    fun getProductCount(): Int = products.size
    fun clear() {
        products.clear()
        productsFlow.value = emptyList()
        nextId = 1
    }

    override suspend fun fetchProduct(id: Long): ProductState? {
        return products.find { it.id == id.toInt() }
    }

    override fun fetchAllProducts(): Flow<List<ProductState>> {
        return productsFlow
    }

    override suspend fun saveProduct(product: ProductState) {
        val newProduct = product.copy(id = nextId++)
        products.add(newProduct)
        productsFlow.value = products.toList()
    }

    override suspend fun duplicateProducts(products: List<ProductState>, duplicateNameSuffix: String) {
        products.forEach { product ->
            val duplicate = product.copy(
                id = nextId++,
                name = TextFieldValue("${product.name.text}$duplicateNameSuffix")
            )
            this.products.add(duplicate)
        }
        productsFlow.value = this.products.toList()
    }

    override suspend fun updateProduct(product: ProductState) {
        val index = products.indexOfFirst { it.id == product.id }
        if (index >= 0) {
            products[index] = product
            productsFlow.value = products.toList()
        }
    }

    override suspend fun updateDocumentProduct(documentProduct: DocumentProductState) {
        // For document products, handled separately
    }

    override suspend fun deleteProduct(id: Long) {
        products.removeAll { it.id == id.toInt() }
        productsFlow.value = products.toList()
    }

    override suspend fun deleteDocumentProducts(ids: List<Long>) {
        // Document products handled separately
    }

    override suspend fun deleteAdditionalPrice(productId: Long, priceWithoutTax: BigDecimal) {
        val product = products.find { it.id == productId.toInt() }
        product?.let {
            val updatedPrices = it.additionalPrices?.filterNot { price ->
                price.priceWithoutTax == priceWithoutTax
            }
            val index = products.indexOf(it)
            products[index] = it.copy(additionalPrices = updatedPrices)
            productsFlow.value = products.toList()
        }
    }

    override suspend fun removeClientFromAdditionalPrice(productId: Long, clientId: Long) {
        val product = products.find { it.id == productId.toInt() }
        product?.let { prod ->
            val updatedPrices = prod.additionalPrices?.map { price ->
                price.copy(clients = price.clients.filterNot { it.id == clientId.toInt() })
            }
            val index = products.indexOf(prod)
            products[index] = prod.copy(additionalPrices = updatedPrices)
            productsFlow.value = products.toList()
        }
    }

    // Helper to add additional price to a product
    fun addAdditionalPriceToProduct(productId: Int, price: ProductPrice) {
        val product = products.find { it.id == productId }
        product?.let {
            val currentPrices = it.additionalPrices?.toMutableList() ?: mutableListOf()
            currentPrices.add(price)
            val index = products.indexOf(it)
            products[index] = it.copy(additionalPrices = currentPrices)
            productsFlow.value = products.toList()
        }
    }
}
