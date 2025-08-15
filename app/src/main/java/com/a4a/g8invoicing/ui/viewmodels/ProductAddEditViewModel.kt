package com.a4a.g8invoicing.ui.viewmodels

import androidx.compose.animation.core.copy
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.data.ProductTaxLocalDataSourceInterface
import com.a4a.g8invoicing.ui.shared.FormInputsValidator
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.calculatePriceWithTax
import com.a4a.g8invoicing.ui.states.ProductPrice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import javax.inject.Inject


@HiltViewModel
class ProductAddEditViewModel @Inject constructor(
    private val dataSource: ProductLocalDataSourceInterface,
    private val taxDataSource: ProductTaxLocalDataSourceInterface,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private var saveJob: Job? = null
    private var updateJob: Job? = null
    private var autoSaveJob: Job? = null
    private var fetchTaxRatesJob: Job? = null
    private var taxRates: List<BigDecimal> = listOf()
    private val id: String? = savedStateHandle["itemId"]
    private val type: String? = savedStateHandle["type"]

    private val _productUiState = mutableStateOf(ProductState())
    val productUiState: State<ProductState> = _productUiState

    // État de chargement
    private val _isLoading = MutableStateFlow(true) // Initialisé à true
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _documentProductUiState = MutableStateFlow(DocumentProductState())
    val documentProductUiState: StateFlow<DocumentProductState> = _documentProductUiState

    init {
        // We initialize only if coming from the navigation (NavGraph)
        // Not if calling from a document (to open the bottom sheet form)
        if (type == ProductType.PRODUCT.name.lowercase()) {
            id?.let { productId ->
                viewModelScope.launch {
                    _isLoading.value = true // Démarre le chargement
                    try {
                        val productData = dataSource.fetchProduct(productId.toLong())
                        productData?.let { product ->
                            _productUiState.value = _productUiState.value.copy(
                                id = product.id,
                                name = product.name,
                                description = product.description,
                                defaultPriceWithoutTax = product.defaultPriceWithoutTax,
                                defaultPriceWithTax = product.defaultPriceWithTax,
                                taxRate = product.taxRate,
                                unit = product.unit
                            )
                        }
                    } finally {
                        _isLoading.value =
                            false // Termine le chargement, que le produit soit trouvé ou non
                    }
                }
            } ?: run {
                // Si pas d'ID (création de produit), pas de chargement initial depuis la DB nécessaire pour *ce* produit
                _isLoading.value = false
            }
        } else {
            // Si ce n'est pas de type PRODUCT, on ne gère pas le chargement ici
            // (ou alors il faudrait une logique spécifique pour DocumentProduct si nécessaire)
            _isLoading.value = false
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
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
            priceWithoutTax = product.defaultPriceWithoutTax,
            priceWithTax = product.defaultPriceWithTax,
            taxRate = product.taxRate,
            quantity = BigDecimal(1),
            unit = product.unit,
            productId = product.id,
        )
    }

    fun setProductUiState() {
        _productUiState.value = ProductState(
            id = null,
            name = _documentProductUiState.value.name,
            description = _documentProductUiState.value.description,
            defaultPriceWithoutTax = _documentProductUiState.value.priceWithoutTax,
            defaultPriceWithTax = _documentProductUiState.value.priceWithTax,
            taxRate = _documentProductUiState.value.taxRate,
            unit = _documentProductUiState.value.unit
        )
    }

    fun clearProductUiState() { // Used when sliding the form in documents
        _productUiState.value = ProductState()
        _documentProductUiState.value = DocumentProductState()
    }

    fun clearProductNameAndDescription() { // Used when sliding the form in documents
        _productUiState.value = _productUiState.value.copy(
            name = TextFieldValue(),
            description = TextFieldValue(),
            defaultPriceWithoutTax = null
        )

        _documentProductUiState.value = _documentProductUiState.value.copy(
            name = TextFieldValue(),
            description = TextFieldValue(),
            priceWithoutTax = null
        )
    }


    // When user chooses a new tax rate
    fun updateTaxRate(taxRate: BigDecimal?, type: ProductType) {
        if (type == ProductType.PRODUCT) {
            _productUiState.value = _productUiState.value.copy(
                taxRate = taxRate,
                defaultPriceWithTax = _productUiState.value.defaultPriceWithoutTax?.let { priceWithoutTax ->
                    taxRate?.let { tax ->
                        calculatePriceWithTax(priceWithoutTax, tax)
                    } ?: BigDecimal(0)
                }
            )
        } else {
            _documentProductUiState.value = _documentProductUiState.value.copy(
                taxRate = taxRate,
                priceWithTax = _documentProductUiState.value.priceWithoutTax?.let { priceWithoutTax ->
                    taxRate?.let { tax ->
                        calculatePriceWithTax(priceWithoutTax, tax)
                    } ?: BigDecimal(0)
                }
            )
        }
    }

    fun fetchTaxRatesFromLocalDb(): List<BigDecimal> {
        fetchTaxRatesJob?.cancel()
        fetchTaxRatesJob = viewModelScope.launch {
            try {
                taxRates = taxDataSource.fetchProductTaxes()
            } catch (e: Exception) {
                //println("Fetching tax rates failed with exception: ${e.localizedMessage}")
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
                //println("Saving products failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun updateInLocalDb(type: ProductType) {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            try {
                if (type == ProductType.PRODUCT) {
                    dataSource.updateProduct(productUiState.value)
                } else {
                    dataSource.updateDocumentProduct(documentProductUiState.value)
                }
            } catch (e: Exception) {
                //println("Saving products failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun updateProductState(pageElement: ScreenElement, value: Any, productType: ProductType, idStr: String? = null) {
        if (productType == ProductType.PRODUCT) {
            _productUiState.value = updateProductUiState(_productUiState.value, pageElement, value, idStr)
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
            ScreenElement.PRODUCT_DESCRIPTION -> productUiState.value.description?.text ?: ""
            else -> ""
        }
        _productUiState.value = updateProductUiState(
            _productUiState.value, pageElement, TextFieldValue(
                text = text,
                selection = TextRange(text.length)
            )
        )
    }

    private fun updateCursorOfDocumentProductState(pageElement: ScreenElement) {
        val input = when (pageElement) {
            ScreenElement.DOCUMENT_PRODUCT_NAME -> documentProductUiState.value.name.text
            ScreenElement.DOCUMENT_PRODUCT_QUANTITY -> documentProductUiState.value.quantity
            ScreenElement.DOCUMENT_PRODUCT_DESCRIPTION -> documentProductUiState.value.description?.text
                ?: ""

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

    fun validateInputs(type: ProductType): Boolean {
        val listOfErrors: MutableList<Pair<ScreenElement, String?>> = mutableListOf()
        when (type) {
            ProductType.PRODUCT -> {
                FormInputsValidator.validateName(_productUiState.value.name.text)?.let {
                    listOfErrors.add(Pair(ScreenElement.PRODUCT_NAME, it))
                }
                _productUiState.value = _productUiState.value.copy(
                    errors = listOfErrors
                )
            }

            ProductType.DOCUMENT_PRODUCT -> {
                FormInputsValidator.validateName(_documentProductUiState.value.name.text)?.let {
                    listOfErrors.add(Pair(ScreenElement.DOCUMENT_PRODUCT_NAME, it))
                }
                _documentProductUiState.value = _documentProductUiState.value.copy(
                    errors = listOfErrors
                )
            }
        }

        return listOfErrors.isEmpty()
    }

    fun clearValidateInputErrors(type: ProductType) {
        when (type) {
            ProductType.PRODUCT -> productUiState.value.errors.clear()
            ProductType.DOCUMENT_PRODUCT -> documentProductUiState.value.errors.clear()
        }
    }

    // Ajouter ou supprimer des prix additionnels
    fun addPrice() {
        val currentProduct = _productUiState.value // Supposons que vous avez un _uiState
        val newPrices =
            currentProduct.additionalPrices?.plus(ProductPrice()) ?: listOf(ProductPrice()) // Ajoute un nouveau prix vide
        _productUiState.value = _productUiState.value.copy(
            additionalPrices = newPrices
        )
    }

    fun deletePrice(priceId: String) {
        val currentProduct = _productUiState.value
        val updatedPrices = currentProduct.additionalPrices?.filterNot { it.idStr == priceId }
        val finalPrices = if (updatedPrices.isNullOrEmpty()) null else updatedPrices
        _productUiState.value = _productUiState.value.copy(
            additionalPrices = finalPrices
        )
    }

}

private fun updateProductUiState(
    currentProductState: ProductState,
    element: ScreenElement,
    value: Any,
    priceId: String? = null
): ProductState {
    var updatedProductState = currentProductState
    when (element) {
        ScreenElement.PRODUCT_NAME -> {
            updatedProductState = updatedProductState.copy(name = value as TextFieldValue)
        }

        ScreenElement.PRODUCT_DESCRIPTION -> {
            updatedProductState = updatedProductState.copy(description = value as TextFieldValue)
        }

        ScreenElement.PRODUCT_DEFAULT_PRICE_WITHOUT_TAX -> {
            val priceWithoutTax = value as String
            updatedProductState = if (priceWithoutTax.isNotEmpty()) {
                updatedProductState.copy(
                    defaultPriceWithoutTax = priceWithoutTax.replace(",", ".")
                        .toBigDecimalOrNull()
                        ?: BigDecimal(0)
                )
            } else updatedProductState.copy(
                defaultPriceWithoutTax = null
            )
        }

        ScreenElement.PRODUCT_DEFAULT_PRICE_WITH_TAX -> {
            val priceWithTax = value as String
            updatedProductState = if (priceWithTax.isNotEmpty()) {
                updatedProductState.copy(
                    defaultPriceWithTax = priceWithTax.replace(",", ".")
                        .toBigDecimalOrNull()
                        ?: BigDecimal(0)
                )
            } else updatedProductState.copy(
                defaultPriceWithTax = null
            )
        }

        ScreenElement.PRODUCT_OTHER_PRICE_WITHOUT_TAX -> {
            if (priceId != null) {
                val priceWithoutTaxStr = value as String
                val newPrices = updatedProductState.additionalPrices?.map { price ->
                    if (price.idStr == priceId) {
                        price.copy(
                            priceWithoutTax = if (priceWithoutTaxStr.isNotEmpty()) {
                                priceWithoutTaxStr.replace(",", ".").toBigDecimalOrNull() ?: BigDecimal(0)
                            } else {
                                null
                            }
                        )
                    } else {
                        price
                    }
                }
                updatedProductState = updatedProductState.copy(additionalPrices = newPrices)
            }
        }
        ScreenElement.PRODUCT_OTHER_PRICE_WITH_TAX -> {
            if (priceId != null) {
                val priceWithTaxStr = value as String
                val newPrices = updatedProductState.additionalPrices?.map { price ->
                    if (price.idStr == priceId) {
                        price.copy(
                            priceWithTax = if (priceWithTaxStr.isNotEmpty()) {
                                priceWithTaxStr.replace(",", ".").toBigDecimalOrNull() ?: BigDecimal(0)
                            } else {
                                null
                            }
                        )
                    } else {
                        price
                    }
                }
                updatedProductState = updatedProductState.copy(additionalPrices = newPrices)
            }
        }

        ScreenElement.PRODUCT_UNIT -> {
            updatedProductState = updatedProductState.copy(unit = value as TextFieldValue)
        }

        else -> null
    }
    return updatedProductState
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

        ScreenElement.DOCUMENT_PRODUCT_PRICE_WITHOUT_TAX -> {
            val priceWithoutTax = value as String
            documentProduct = if (priceWithoutTax.isNotEmpty()) {
                documentProduct.copy(
                    priceWithoutTax = priceWithoutTax.replace(",", ".")
                        .toBigDecimalOrNull()
                        ?: BigDecimal(0)
                )
            } else documentProduct.copy(
                priceWithoutTax = null
            )
        }

        ScreenElement.DOCUMENT_PRODUCT_PRICE_WITH_TAX -> {
            val priceWithTax = value as String
            documentProduct = if (priceWithTax.isNotEmpty()) {
                documentProduct.copy(
                    priceWithTax = priceWithTax.replace(",", ".")
                        .toBigDecimalOrNull()
                        ?: BigDecimal(0)
                )
            } else documentProduct.copy(
                priceWithTax = null
            )
        }

        ScreenElement.DOCUMENT_PRODUCT_UNIT -> {
            documentProduct = documentProduct.copy(unit = value as TextFieldValue)
        }

        else -> null
    }
    return documentProduct
}