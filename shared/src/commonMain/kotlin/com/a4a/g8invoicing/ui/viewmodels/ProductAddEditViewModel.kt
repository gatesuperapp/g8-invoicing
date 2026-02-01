package com.a4a.g8invoicing.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSourceInterface
import com.a4a.g8invoicing.data.models.PersonType
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.data.ProductTaxLocalDataSourceInterface
import com.a4a.g8invoicing.ui.shared.FormInputsValidator
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.data.util.calculatePriceWithTax
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.ClientRef
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.ProductPrice
import com.a4a.g8invoicing.ui.states.ProductState
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.ionspin.kotlin.bignum.decimal.BigDecimal

class ProductAddEditViewModel(
    private val dataSource: ProductLocalDataSourceInterface,
    private val taxDataSource: ProductTaxLocalDataSourceInterface,
    private val clientOrIssuerDataSource: ClientOrIssuerLocalDataSourceInterface,
    private val itemId: String?,
    private val type: String?,
) : ViewModel() {

    private var saveJob: Job? = null
    private var updateJob: Job? = null
    private var autoSaveJob: Job? = null
    private var fetchTaxRatesJob: Job? = null
    private var taxRates: List<BigDecimal> = listOf()

    private val _productUiState = mutableStateOf(ProductState())
    val productUiState: State<ProductState> = _productUiState

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _documentProductUiState = MutableStateFlow(DocumentProductState())
    val documentProductUiState: StateFlow<DocumentProductState> = _documentProductUiState

    private val _availableClients = MutableStateFlow<List<ClientOrIssuerState>>(emptyList())
    val availableClients: StateFlow<List<ClientOrIssuerState>> = _availableClients.asStateFlow()

    private val _clientSelectionDialogState = MutableStateFlow<ClientSelectionDialogState?>(null)
    val clientSelectionDialogState: StateFlow<ClientSelectionDialogState?> =
        _clientSelectionDialogState.asStateFlow()

    init {
        if (type == ProductType.PRODUCT.name.lowercase()) {
            itemId?.let { productId ->
                viewModelScope.launch {
                    _isLoading.value = true
                    try {
                        dataSource.fetchProduct(productId.toLong())?.let {
                            _productUiState.value = it
                        }
                    } finally {
                        _isLoading.value = false
                    }
                }
            } ?: run {
                _isLoading.value = false
            }
        } else {
            _isLoading.value = false
        }

        viewModelScope.launch {
            clientOrIssuerDataSource.fetchAll(type = PersonType.CLIENT).collect { clients ->
                _availableClients.value = clients
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            updateInLocalDb(ProductType.DOCUMENT_PRODUCT)
        }
    }

    fun setDocumentProductUiState(documentProduct: DocumentProductState) {
        _documentProductUiState.value = documentProduct
    }

    fun setDocumentProductUiStateWithProduct(product: ProductState, clientId: Int? = null) {
        val (priceWithoutTax, priceWithTax) = if (clientId != null) {
            val clientPrice = product.additionalPrices?.find { price ->
                price.clients.any { it.id == clientId }
            }
            if (clientPrice != null) {
                Pair(clientPrice.priceWithoutTax, clientPrice.priceWithTax)
            } else {
                Pair(product.defaultPriceWithoutTax, product.defaultPriceWithTax)
            }
        } else {
            Pair(product.defaultPriceWithoutTax, product.defaultPriceWithTax)
        }

        _documentProductUiState.value = DocumentProductState(
            id = null,
            name = product.name,
            description = product.description,
            priceWithoutTax = priceWithoutTax,
            priceWithTax = priceWithTax,
            taxRate = product.taxRate,
            quantity = BigDecimal.ONE,
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

    fun clearProductUiState() {
        _productUiState.value = ProductState()
        _documentProductUiState.value = DocumentProductState()
    }

    fun clearProductNameAndDescription() {
        _productUiState.value = _productUiState.value.copy(
            name = TextFieldValue(),
            description = TextFieldValue(),
            defaultPriceWithoutTax = null,
            defaultPriceWithTax = null
        )

        _documentProductUiState.value = _documentProductUiState.value.copy(
            name = TextFieldValue(),
            description = TextFieldValue(),
            priceWithoutTax = null,
            priceWithTax = null,
            quantity = BigDecimal.ONE
            // unit is intentionally NOT reset - it's preserved between product creations
        )
    }

    fun updateTaxRate(taxRate: BigDecimal?, type: ProductType) {
        if (type == ProductType.PRODUCT) {
            val updatedAdditionalPrices = _productUiState.value.additionalPrices?.map { price ->
                price.copy(
                    priceWithTax = price.priceWithoutTax?.let { ht ->
                        taxRate?.let { tax -> calculatePriceWithTax(ht, tax) }
                    }
                )
            }

            _productUiState.value = _productUiState.value.copy(
                taxRate = taxRate,
                defaultPriceWithTax = _productUiState.value.defaultPriceWithoutTax?.let { priceWithoutTax ->
                    taxRate?.let { tax ->
                        calculatePriceWithTax(priceWithoutTax, tax)
                    } ?: BigDecimal.ZERO
                },
                additionalPrices = updatedAdditionalPrices
            )
        } else {
            _documentProductUiState.value = _documentProductUiState.value.copy(
                taxRate = taxRate,
                priceWithTax = _documentProductUiState.value.priceWithoutTax?.let { priceWithoutTax ->
                    taxRate?.let { tax ->
                        calculatePriceWithTax(priceWithoutTax, tax)
                    } ?: BigDecimal.ZERO
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
                // Error handling
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
                // Error handling
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
                // Error handling
            }
        }
    }

    fun updateProductState(
        pageElement: ScreenElement,
        value: Any,
        productType: ProductType,
        idStr: String? = null,
    ) {
        if (productType == ProductType.PRODUCT) {
            _productUiState.value =
                updateProductUiState(_productUiState.value, pageElement, value, idStr)
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
            ScreenElement.DOCUMENT_PRODUCT_DESCRIPTION -> documentProductUiState.value.description?.text ?: ""
            ScreenElement.DOCUMENT_PRODUCT_UNIT -> documentProductUiState.value.unit?.text ?: ""
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
                _productUiState.value = _productUiState.value.copy(errors = listOfErrors)
            }

            ProductType.DOCUMENT_PRODUCT -> {
                FormInputsValidator.validateName(_documentProductUiState.value.name.text)?.let {
                    listOfErrors.add(Pair(ScreenElement.DOCUMENT_PRODUCT_NAME, it))
                }
                _documentProductUiState.value = _documentProductUiState.value.copy(errors = listOfErrors)
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

    fun addPrice() {
        val currentProduct = _productUiState.value
        val newPrices = currentProduct.additionalPrices?.plus(ProductPrice()) ?: listOf(ProductPrice())
        _productUiState.value = _productUiState.value.copy(additionalPrices = newPrices)
    }

    fun deletePrice(priceId: String) {
        val currentProduct = _productUiState.value
        val updatedPrices = currentProduct.additionalPrices?.filterNot { it.idStr == priceId }
        val finalPrices = if (updatedPrices.isNullOrEmpty()) null else updatedPrices
        _productUiState.value = _productUiState.value.copy(additionalPrices = finalPrices)
    }

    fun openClientSelectionDialog(priceId: String) {
        val price = _productUiState.value.additionalPrices?.find { it.idStr == priceId } ?: return

        val allClients = _availableClients.value.mapNotNull {
            val id = it.id ?: return@mapNotNull null
            ClientRef(id = id, name = it.name.text)
        }
        _clientSelectionDialogState.value = ClientSelectionDialogState(
            priceId = priceId,
            selectedClients = price.clients,
            availableClients = allClients
        )
    }

    fun closeClientSelectionDialog() {
        _clientSelectionDialogState.value = null
    }

    fun toggleClientSelection(client: ClientRef) {
        val state = _clientSelectionDialogState.value ?: return

        val updated = if (state.selectedClients.any { it.id == client.id }) {
            state.selectedClients.filterNot { it.id == client.id }
        } else {
            state.selectedClients + client
        }

        _clientSelectionDialogState.value = state.copy(selectedClients = updated)
    }

    fun confirmClientSelection() {
        val state = _clientSelectionDialogState.value ?: return

        _productUiState.value = updateProductUiState(
            _productUiState.value,
            ScreenElement.PRODUCT_OTHER_PRICE_CLIENTS, state.selectedClients, state.priceId
        )

        _clientSelectionDialogState.value = null
    }

    fun removeClientFromPrice(priceId: String, clientId: Int) {
        val updatedPrices = _productUiState.value.additionalPrices?.map { price ->
            if (price.idStr == priceId) {
                price.copy(clients = price.clients.filterNot { it.id == clientId })
            } else price
        }

        _productUiState.value = _productUiState.value.copy(additionalPrices = updatedPrices)
    }
}

private fun updateProductUiState(
    currentProductState: ProductState,
    element: ScreenElement,
    value: Any,
    priceId: String? = null,
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
                        .toDoubleOrNull()?.let { BigDecimal.fromDouble(it) }
                        ?: BigDecimal.ZERO
                )
            } else updatedProductState.copy(defaultPriceWithoutTax = null)
        }

        ScreenElement.PRODUCT_DEFAULT_PRICE_WITH_TAX -> {
            val priceWithTax = value as String
            updatedProductState = if (priceWithTax.isNotEmpty()) {
                updatedProductState.copy(
                    defaultPriceWithTax = priceWithTax.replace(",", ".")
                        .toDoubleOrNull()?.let { BigDecimal.fromDouble(it) }
                        ?: BigDecimal.ZERO
                )
            } else updatedProductState.copy(defaultPriceWithTax = null)
        }

        ScreenElement.PRODUCT_OTHER_PRICE_WITHOUT_TAX -> {
            if (priceId != null) {
                val priceWithoutTaxStr = value as String

                val newPrices = updatedProductState.additionalPrices?.map { price ->
                    if (price.idStr == priceId) {
                        val ht = priceWithoutTaxStr
                            .replace(",", ".")
                            .toDoubleOrNull()?.let { BigDecimal.fromDouble(it) }

                        val ttc = ht?.let {
                            updatedProductState.taxRate?.let { tax ->
                                calculatePriceWithTax(it, tax)
                            }
                        }

                        price.copy(priceWithoutTax = ht, priceWithTax = ttc)
                    } else price
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
                                priceWithTaxStr.replace(",", ".").toDoubleOrNull()?.let { BigDecimal.fromDouble(it) }
                                    ?: BigDecimal.ZERO
                            } else null
                        )
                    } else price
                }
                updatedProductState = updatedProductState.copy(additionalPrices = newPrices)
            }
        }

        ScreenElement.PRODUCT_UNIT -> {
            updatedProductState = updatedProductState.copy(unit = value as TextFieldValue)
        }

        ScreenElement.PRODUCT_OTHER_PRICE_CLIENTS -> {
            val newPrices = updatedProductState.additionalPrices?.map { price ->
                if (price.idStr == priceId) {
                    price.copy(clients = value as List<ClientRef>)
                } else price
            }
            updatedProductState = updatedProductState.copy(additionalPrices = newPrices)
        }

        else -> {}
    }
    return updatedProductState
}

private fun updateDocumentProductUiState(
    documentProduct: DocumentProductState,
    element: ScreenElement,
    value: Any,
): DocumentProductState {
    var updated = documentProduct
    when (element) {
        ScreenElement.DOCUMENT_PRODUCT_NAME -> {
            updated = updated.copy(name = value as TextFieldValue)
        }

        ScreenElement.DOCUMENT_PRODUCT_QUANTITY -> {
            updated = updated.copy(
                quantity = (value as String).replace(",", ".").toDoubleOrNull()?.let { BigDecimal.fromDouble(it) } ?: BigDecimal.ZERO
            )
        }

        ScreenElement.DOCUMENT_PRODUCT_DESCRIPTION -> {
            updated = updated.copy(description = value as TextFieldValue)
        }

        ScreenElement.DOCUMENT_PRODUCT_PRICE_WITHOUT_TAX -> {
            val priceWithoutTax = value as String
            updated = if (priceWithoutTax.isNotEmpty()) {
                updated.copy(
                    priceWithoutTax = priceWithoutTax.replace(",", ".")
                        .toDoubleOrNull()?.let { BigDecimal.fromDouble(it) }
                        ?: BigDecimal.ZERO
                )
            } else updated.copy(priceWithoutTax = null)
        }

        ScreenElement.DOCUMENT_PRODUCT_PRICE_WITH_TAX -> {
            val priceWithTax = value as String
            updated = if (priceWithTax.isNotEmpty()) {
                updated.copy(
                    priceWithTax = priceWithTax.replace(",", ".")
                        .toDoubleOrNull()?.let { BigDecimal.fromDouble(it) }
                        ?: BigDecimal.ZERO
                )
            } else updated.copy(priceWithTax = null)
        }

        ScreenElement.DOCUMENT_PRODUCT_UNIT -> {
            updated = updated.copy(unit = value as TextFieldValue)
        }

        else -> {}
    }
    return updated
}

data class ClientSelectionDialogState(
    val priceId: String,
    val selectedClients: List<ClientRef>,
    val availableClients: List<ClientRef>,
)
