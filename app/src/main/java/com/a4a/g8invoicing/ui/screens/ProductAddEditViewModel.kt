package com.a4a.g8invoicing.ui.screens

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.data.ProductTaxLocalDataSourceInterface
import com.a4a.g8invoicing.ui.shared.ScreenElement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject


@HiltViewModel
class ProductAddEditViewModel @Inject constructor(
    private val dataSource: ProductLocalDataSourceInterface,
    private val taxDataSource: ProductTaxLocalDataSourceInterface,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private var saveJob: Job? = null
    private var fetchTaxRatesJob: Job? = null
    private var taxRates: List<BigDecimal> = listOf()
    private val id: String? = savedStateHandle["itemId"]
    private val type: String? = savedStateHandle["type"]

    private val _productUiState = mutableStateOf(ProductState())
    val productUiState: State<ProductState> = _productUiState

    private val _documentProductUiState = mutableStateOf(DocumentProductState())
    val documentProductUiState: State<DocumentProductState> = _documentProductUiState

    init {
        if (type == ProductType.PRODUCT.name.lowercase()) {
            id?.let {
                fetchProductFromLocalDb(it.toLong())
            }
        } else {
            id?.let {
                fetchDocumentProductFromLocalDb(it.toLong())
            }
        }
    }

    private fun fetchProductFromLocalDb(id: Long) {
        val product = dataSource.fetchProduct(id)

        _productUiState.value = _productUiState.value.copy(
            productId = product?.productId,
            name = product?.name ?: TextFieldValue(),
            description = product?.description,
            finalPrice = product?.finalPrice,
            taxRate = product?.taxRate,
            priceWithoutTax = product?.priceWithoutTax,
            unit = product?.unit
        )
    }

    private fun fetchDocumentProductFromLocalDb(id: Long) {
        val documentProduct = dataSource.fetchDocumentProduct(id)

        _documentProductUiState.value = _documentProductUiState.value.copy(
            productId = documentProduct?.productId,
            name = documentProduct?.name ?: TextFieldValue(""),
            description = documentProduct?.description,
            finalPrice = documentProduct?.finalPrice ?: BigDecimal(0),
            priceWithoutTax = documentProduct?.priceWithoutTax ?: BigDecimal(0),
            taxRate = documentProduct?.taxRate ?: BigDecimal(0),
            unit = documentProduct?.unit
        )
    }

    // When user chooses a new tax rate
    fun updateTaxRate(taxRate: BigDecimal?) {
        _productUiState.value = _productUiState.value.copy(
            taxRate = taxRate,
            priceWithoutTax = _productUiState.value.finalPrice?.let {
                taxRate?.let { tax ->
                    it - it * tax / BigDecimal(100)
                }
            }
        )
    }

    fun fetchTaxRatesFromLocalDb(): List<BigDecimal> {
        fetchTaxRatesJob?.cancel()
        fetchTaxRatesJob = viewModelScope.launch {
            try {
                taxRates = taxDataSource.fetchProductTaxes()
            } catch (e: Exception) {
                println("Fetching tax rates failed with exception: ${e.localizedMessage}")
            }
        }
        return taxRates
    }

    fun saveProductInLocalDb(type: ProductType) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            try {
                if (type == ProductType.PRODUCT) {
                    dataSource.saveProduct(productUiState.value)
                } else {
                    dataSource.saveDocumentProduct(documentProductUiState.value)
                }
            } catch (e: Exception) {
                println("Saving products failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun updateProductInLocalDb(type: ProductType) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            try {
                if (type == ProductType.PRODUCT) {
                    dataSource.updateProduct(productUiState.value)
                } else {
                    dataSource.updateDocumentProduct(documentProductUiState.value)
                }
            } catch (e: Exception) {
                println("Saving products failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun updateProductState(pageElement: ScreenElement, value: Any, productType: ProductType) {
        if (productType == ProductType.PRODUCT) {
            _productUiState.value = updateProductUiState(_productUiState.value, pageElement, value)
        } else {
            _documentProductUiState.value = updateDocumentProductUiState(_documentProductUiState.value, pageElement, value)
        }
    }

    fun updateCursorOfProductState(pageElement: ScreenElement) {
        val text = when (pageElement) {
            ScreenElement.PRODUCT_NAME -> productUiState.value.name.text
            ScreenElement.PRODUCT_DESCRIPTION -> productUiState.value.description?.text
            else -> ""
        }
        _productUiState.value = updateProductUiState(
            _productUiState.value, pageElement, TextFieldValue(
                text = text ?: "",
                selection = TextRange(text?.length ?: 0)
            )
        )
    }
}

private fun updateProductUiState(
    product: ProductState,
    element: ScreenElement,
    value: Any,
): ProductState {
    var product = product
    when (element) {
        ScreenElement.PRODUCT_NAME -> {
            product = product.copy(name = value as TextFieldValue)
        }

        ScreenElement.PRODUCT_DESCRIPTION -> {
            product = product.copy(description = value as TextFieldValue)
        }

        ScreenElement.PRODUCT_UNIT -> {
            product = product.copy(unit = value as TextFieldValue)
        }

        else -> null
    }
    return product
}



enum class ProductType {
    PRODUCT, DOCUMENT_PRODUCT
}

private fun updateDocumentProductUiState(
    documentProduct: DocumentProductState,
    element: ScreenElement,
    value: Any,
): DocumentProductState {
    var product = documentProduct
    when (element) {
        ScreenElement.DOCUMENT_PRODUCT_NAME -> {
            product = product.copy(name = value as TextFieldValue)
        }

        ScreenElement.DOCUMENT_PRODUCT_DESCRIPTION -> {
            product = product.copy(description = value as TextFieldValue)
        }

        ScreenElement.DOCUMENT_PRODUCT_UNIT -> {
            product = product.copy(unit = value as TextFieldValue)
        }

        ScreenElement.DOCUMENT_PRODUCT_QUANTITY -> {
            val quantity = value as String
            product = product.copy(quantity = BigDecimal(quantity.toDoubleOrNull() ?: 0.0))
        }

        else -> null
    }
    return product
}