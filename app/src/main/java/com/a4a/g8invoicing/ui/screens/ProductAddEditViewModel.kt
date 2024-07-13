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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
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

    private val _documentProductUiState = MutableStateFlow(DocumentProductState())
    val documentProductUiState: StateFlow<DocumentProductState> = _documentProductUiState

/*    private val _documentProductUiState = mutableStateOf(DocumentProductState())
    val documentProductUiState: State<DocumentProductState> = _documentProductUiState*/

    init {
        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            _documentProductUiState.debounce(300)
                .collect { updateInLocalDb(ProductType.DOCUMENT_PRODUCT) }
        }
        // We initialize only if coming from the navigation (NavGraph)
        // Not if calling from a document (to open the bottom sheet form)
        if (type == ProductType.PRODUCT.name.lowercase()) {
            id?.let {
                fetchProductFromLocalDb(it.toLong())
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        GlobalScope.launch {
            updateInLocalDb(ProductType.DOCUMENT_PRODUCT)
        }
    }


    // Used when sliding the bottom form from documents
    // Editing a document product
    fun setDocumentProductUiState(documentProduct: DocumentProductState) {
        _documentProductUiState.value = documentProduct
    }

    // Used when sliding the bottom form from documents
    // Choosing a product
    fun setDocumentProductUiStateWithProduct(product: ProductState) {
        _documentProductUiState.value = DocumentProductState(
            id = null,
            name = product.name,
            description = product.description,
            priceWithTax = product.priceWithTax,
            taxRate = product.taxRate,
            quantity = BigDecimal(1),
            unit = product.unit,
            productId = product.productId
        )
    }

    fun setProductUiState() {
        _productUiState.value = ProductState(
            productId = null,
            name = _documentProductUiState.value.name,
            description = _documentProductUiState.value.description,
            priceWithTax = _documentProductUiState.value.priceWithTax,
            taxRate = _documentProductUiState.value.taxRate,
            unit = _documentProductUiState.value.unit
        )
    }

    fun clearProductUiState() { // Used when sliding the form in documents
        _productUiState.value = ProductState()
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
            priceWithTax = product?.priceWithTax,
            taxRate = product?.taxRate,
            unit = product?.unit
        )
    }


    // When user chooses a new tax rate
    fun updateTaxRate(taxRate: BigDecimal?, type: ProductType) {
        if (type == ProductType.PRODUCT) {
            _productUiState.value = _productUiState.value.copy(
                taxRate = taxRate
            )
        } else {
            _documentProductUiState.value = _documentProductUiState.value.copy(
                taxRate = taxRate
            )
        }
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

    fun saveProductInLocalDb() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            try {
                dataSource.saveProduct(productUiState.value)
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
            _documentProductUiState.value =
                updateDocumentProductUiState(_documentProductUiState.value, pageElement, value)
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
            ScreenElement.DOCUMENT_PRODUCT_NAME -> documentProductUiState.value.name.text
            ScreenElement.DOCUMENT_PRODUCT_QUANTITY -> documentProductUiState.value.quantity
            ScreenElement.DOCUMENT_PRODUCT_DESCRIPTION -> documentProductUiState.value.description?.text
            ScreenElement.DOCUMENT_PRODUCT_UNIT -> documentProductUiState.value.unit?.text
            else -> ""
        }
        _documentProductUiState.value = updateDocumentProductUiState(
            _documentProductUiState.value,
            pageElement,
            TextFieldValue(
                text = input.toString(),
                selection = TextRange(input?.toString()?.length ?: 0)
            )
        )
    }
}

private fun updateProductUiState(
    product: ProductState,
    element: ScreenElement,
    value: Any
): ProductState {
    var product = product
    when (element) {
        ScreenElement.PRODUCT_NAME -> {
            product = product.copy(name = value as TextFieldValue)
        }

        ScreenElement.PRODUCT_DESCRIPTION -> {
            product = product.copy(description = value as TextFieldValue)
        }

        ScreenElement.PRODUCT_FINAL_PRICE -> {
            product =
                product.copy(priceWithTax = (value as String).toBigDecimalOrNull() ?: BigDecimal(0))
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
    var documentProduct = documentProduct
    when (element) {
        ScreenElement.DOCUMENT_PRODUCT_NAME -> {
            documentProduct = documentProduct.copy(name = value as TextFieldValue)
        }

        ScreenElement.DOCUMENT_PRODUCT_QUANTITY -> {
            documentProduct =
                documentProduct.copy(
                    quantity = (value as String).toBigDecimalOrNull() ?: BigDecimal(0)
                )
        }

        ScreenElement.DOCUMENT_PRODUCT_DESCRIPTION -> {
            documentProduct = documentProduct.copy(description = value as TextFieldValue)
        }

        ScreenElement.DOCUMENT_PRODUCT_FINAL_PRICE -> {
            documentProduct = documentProduct.copy(
                priceWithTax = (value as String).toBigDecimalOrNull() ?: BigDecimal(0)
            )
        }

        ScreenElement.DOCUMENT_PRODUCT_UNIT -> {
            documentProduct = documentProduct.copy(unit = value as TextFieldValue)
        }

        else -> null
    }
    return documentProduct
}