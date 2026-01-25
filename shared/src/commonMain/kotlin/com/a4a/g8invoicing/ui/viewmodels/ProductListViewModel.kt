package com.a4a.g8invoicing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.ui.states.ProductsUiState
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.product_duplicate_suffix
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

class ProductListViewModel(
    private val productDataSource: ProductLocalDataSourceInterface,
) : ViewModel() {

    private val _productsUiState = MutableStateFlow(ProductsUiState())
    val productsUiState: StateFlow<ProductsUiState> = _productsUiState.asStateFlow()

    private var fetchJob: Job? = null
    private var deleteJob: Job? = null
    private var saveJob: Job? = null
    private var duplicateJob: Job? = null

    init {
        fetchProducts()
    }

    private fun fetchProducts() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                productDataSource.fetchAllProducts().collect { products ->
                    _productsUiState.update {
                        it.copy(
                            products = products.sortedBy { it.name.text })
                    }
                }
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    fun deleteProducts(selectedProducts: List<ProductState>) {
        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            try {
                selectedProducts.forEach { selectedProduct ->
                    selectedProduct.id?.let {
                        productDataSource.deleteProduct(it.toLong())
                    }
                }
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    fun duplicateProducts(selectedProducts: List<ProductState>) {
        duplicateJob?.cancel()
        duplicateJob = viewModelScope.launch {
            try {
                val suffix = getString(Res.string.product_duplicate_suffix)
                productDataSource.duplicateProducts(selectedProducts, suffix)
            } catch (e: Exception) {
                // Error handling
            }
        }
    }
}
