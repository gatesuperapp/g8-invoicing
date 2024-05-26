package com.a4a.g8invoicing.ui.screens

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.ui.states.ProductsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
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
                println("Fetching products failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun deleteProducts(selectedProducts: List<ProductState>) {
        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            try {
                selectedProducts.forEach { selectedProduct ->
                    selectedProduct.productId?.let {
                        productDataSource.deleteProduct(it.toLong())
                    }
                }
            } catch (e: Exception) {
                println("Deleting products failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun duplicateProducts(selectedProducts: List<ProductState>) {
        duplicateJob?.cancel()
        duplicateJob = viewModelScope.launch {
            try {
                productDataSource.duplicateProducts(selectedProducts)
            } catch (e: Exception) {
                println("Duplicating products failed with exception: ${e.localizedMessage}")
            }
        }
    }
}
