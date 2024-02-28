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
        // We initialize only if coming from the navigation (NavGraph)
        // Not if calling from a document (to open the bottom sheet form),
        if (type == ProductType.PRODUCT.name.lowercase()) {
            id?.let {
                fetchProductFromLocalDb(it.toLong())
            }
        } else if(type == ProductType.DOCUMENT_PRODUCT.name.lowercase()) {
            id?.let {
                fetchDocumentProductFromLocalDb(it.toLong())
            }
        }
    }

    fun setDocumentProductUiState(product: ProductState) { // Used when sliding the form in documents
        _documentProductUiState.value = DocumentProductState(
            id = null,
            name = product.name,
            description = product.description,
            finalPrice = product.finalPrice ?: BigDecimal(0),
            priceWithoutTax = product.priceWithoutTax ?: BigDecimal(0),
            taxRate = product.taxRate ?: BigDecimal(0),
            quantity = BigDecimal(1),
            unit = product.unit,
            productId = product.productId
        )
    }

    fun clearDocumentProductUiState() { // Used when sliding the form in documents
        _documentProductUiState.value = DocumentProductState()
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

    fun saveInLocalDb(type: ProductType) {
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

    fun updateInLocalDb(type: ProductType) {
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

    fun updateCursor(pageElement: ScreenElement, productType: ProductType) {
        if (productType == ProductType.PRODUCT) {
            updateCursorOfProductState(pageElement)
        } else {
            updateCursorOfDocumentProductState(pageElement)
        }
    }
    private fun updateCursorOfProductState(pageElement: ScreenElement) {
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
    private fun updateCursorOfDocumentProductState(pageElement: ScreenElement) {
        val input = when (pageElement) {
            ScreenElement.DOCUMENT_PRODUCT_NAME -> documentProductUiState.value.name
            ScreenElement.DOCUMENT_PRODUCT_QUANTITY -> documentProductUiState.value.quantity
            ScreenElement.DOCUMENT_PRODUCT_DESCRIPTION -> documentProductUiState.value.description
            ScreenElement.DOCUMENT_PRODUCT_UNIT -> documentProductUiState.value.unit
            else -> ""
        }
        _documentProductUiState.value = updateDocumentProductUiState(
            _documentProductUiState.value, pageElement, TextFieldValue(
                text = input.toString(),
                selection = TextRange(input?.toString()?.length ?: 0)
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

        ScreenElement.DOCUMENT_PRODUCT_QUANTITY -> {
            val quantity = value as String
            product = product.copy(quantity = BigDecimal(quantity.toDoubleOrNull() ?: 0.0))
        }

        ScreenElement.DOCUMENT_PRODUCT_DESCRIPTION -> {
            product = product.copy(description = value as TextFieldValue)
        }

        ScreenElement.DOCUMENT_PRODUCT_UNIT -> {
            product = product.copy(unit = value as TextFieldValue)
        }

        else -> null
    }
    return product
}