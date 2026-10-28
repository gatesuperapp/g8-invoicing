package com.a4a.g8invoicing.ui.viewmodels

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.data.SalesOrderLocalDataSourceInterface
import com.a4a.g8invoicing.data.calculateDocumentPrices
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.SalesOrderState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class SalesOrderAddEditViewModel(
    private val documentDataSource: SalesOrderLocalDataSourceInterface,
    private val documentProductDataSource: ProductLocalDataSourceInterface,
    private val itemId: String?,
) : ViewModel() {
    private var fetchJob: Job? = null
    private var saveJob: Job? = null
    private var updateJob: Job? = null
    private var deleteJob: Job? = null
    private var autoSaveJob: Job? = null

    private val _documentUiState = MutableStateFlow(SalesOrderState())
    val salesOrderUiState: StateFlow<SalesOrderState> = _documentUiState

    init {
        autoSaveInLocalDb()
        try {
            itemId?.let {
                fetchSalesOrderFromLocalDb(it.toLong())
            } ?: viewModelScope.launch {
                createNewSalesOrderInVM()?.let {
                    fetchSalesOrderFromLocalDb(it)
                }
            }
        } catch (e: Exception) {
        }
    }

    @OptIn(FlowPreview::class)
    private fun autoSaveInLocalDb() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            _documentUiState.debounce(300)
                .collect { updateSalesOrderInLocalDb() }
        }
    }

    private fun fetchSalesOrderFromLocalDb(id: Long) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                documentDataSource.fetch(id)?.let {
                    _documentUiState.value = it
                }
            } catch (e: Exception) {
            }
        }
    }

    fun reloadDocument() {
        _documentUiState.value.documentId?.let {
            fetchSalesOrderFromLocalDb(it.toLong())
        }
    }

    private suspend fun createNewSalesOrderInVM(): Long? {
        var newId: Long? = null
        val job = viewModelScope.launch {
            try {
                newId = documentDataSource.createNew()
            } catch (e: Exception) {
            }
        }
        job.join()
        return newId
    }

    fun updateUiState(screenElement: ScreenElement, value: Any) {
        _documentUiState.value =
            updateSalesOrderUiState(_documentUiState.value, screenElement, value)
    }

    private fun updateSalesOrderInLocalDb() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            try {
                documentDataSource.update(salesOrderUiState.value)
            } catch (e: Exception) {
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            updateSalesOrderInLocalDb()
        }
    }

    fun setDocumentFont(fontId: String?) {
        _documentUiState.value = _documentUiState.value.copy(fontFamily = fontId)
        updateSalesOrderInLocalDb()
    }

    suspend fun saveDocumentProductInLocalDbAndGetId(documentProduct: DocumentProductState): Int? {
        val currentDocumentId = _documentUiState.value.documentId?.toLong() ?: return null
        return try {
            documentDataSource.saveDocumentProductInDbAndLinkToDocument(
                documentProduct = documentProduct,
                documentId = currentDocumentId,
            )
        } catch (e: Exception) {
            null
        }
    }

    fun updateDocumentProductsOrderInUiStateAndDb(updatedProducts: List<DocumentProductState>) {
        viewModelScope.launch {
            try {
                val productsWithUpdatedSortOrder = updatedProducts.mapIndexed { index, product ->
                    product.copy(sortOrder = index)
                }

                _documentUiState.value = _documentUiState.value.copy(
                    documentProducts = productsWithUpdatedSortOrder
                )

                val documentId = _documentUiState.value.documentId?.toLong()
                if (documentId != null) {
                    documentDataSource.updateDocumentProductsOrderInDb(
                        documentId = documentId,
                        orderedProducts = productsWithUpdatedSortOrder,
                    )
                }
            } catch (e: Exception) {
            }
        }
    }

    fun removeDocumentProductFromLocalDb(documentProductId: Int) {
        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            try {
                _documentUiState.value.documentId?.let {
                    documentDataSource.deleteDocumentProduct(
                        it.toLong(),
                        documentProductId.toLong(),
                    )
                }
                documentProductDataSource.deleteDocumentProducts(listOf(documentProductId.toLong()))
            } catch (e: Exception) {
            }
        }
    }

    fun removeDocumentProductFromUiState(documentProductId: Int) {
        try {
            val list = _documentUiState.value.documentProducts
                ?.filterNot { it.id == documentProductId }?.toMutableList()
            _documentUiState.value = _documentUiState.value.copy(
                documentProducts = list
            )
            _documentUiState.value.documentProducts?.let {
                _documentUiState.value =
                    _documentUiState.value.copy(documentTotalPrices = calculateDocumentPrices(it))
            }
        } catch (e: Exception) {
        }
    }

    fun saveDocumentProductInUiState(documentProduct: DocumentProductState) {
        try {
            val list = _documentUiState.value.documentProducts
            var maxId = 1
            if (!list.isNullOrEmpty()) {
                maxId = list.mapNotNull { it.id }.max()
            }
            if (documentProduct.id == null) {
                documentProduct.id = maxId + 1
            }
            val newList: List<DocumentProductState> = (list ?: emptyList()) + documentProduct
            _documentUiState.value = _documentUiState.value.copy(
                documentProducts = newList
            )
            _documentUiState.value.documentProducts?.let {
                _documentUiState.value =
                    _documentUiState.value.copy(documentTotalPrices = calculateDocumentPrices(it))
            }
        } catch (e: Exception) {
        }
    }

    fun saveDocumentClientOrIssuerInLocalDb(documentClientOrIssuer: ClientOrIssuerState) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            try {
                val documentType = when (documentClientOrIssuer.type) {
                    ClientOrIssuerType.CLIENT, ClientOrIssuerType.DOCUMENT_CLIENT ->
                        ClientOrIssuerType.DOCUMENT_CLIENT
                    ClientOrIssuerType.ISSUER, ClientOrIssuerType.DOCUMENT_ISSUER ->
                        ClientOrIssuerType.DOCUMENT_ISSUER
                    else -> null
                }
                _documentUiState.value.documentId?.let { docId ->
                    documentType?.let {
                        documentDataSource.deleteDocumentClientOrIssuer(docId.toLong(), it)
                    }
                }
                documentDataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(
                    documentClientOrIssuer = documentClientOrIssuer,
                    documentId = _documentUiState.value.documentId?.toLong(),
                )
                _documentUiState.value.documentId?.let {
                    fetchSalesOrderFromLocalDb(it.toLong())
                }
            } catch (e: Exception) {
            }
        }
    }

    fun removeDocumentClientOrIssuerFromLocalDb(type: ClientOrIssuerType) {
        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            try {
                _documentUiState.value.documentId?.let {
                    documentDataSource.deleteDocumentClientOrIssuer(it.toLong(), type)
                }
                _documentUiState.value.documentId?.let {
                    fetchSalesOrderFromLocalDb(it.toLong())
                }
            } catch (e: Exception) {
            }
        }
    }

    fun removeDocumentClientOrIssuerFromUiState(type: ClientOrIssuerType) {
        if (type == ClientOrIssuerType.DOCUMENT_CLIENT)
            _documentUiState.value = _documentUiState.value.copy(documentClient = null)
        else _documentUiState.value = _documentUiState.value.copy(documentIssuer = null)
    }

    fun saveDocumentClientOrIssuerInUiState(documentClientOrIssuer: ClientOrIssuerState) {
        if (documentClientOrIssuer.type == ClientOrIssuerType.DOCUMENT_CLIENT)
            _documentUiState.value = _documentUiState.value.copy(documentClient = documentClientOrIssuer)
        else _documentUiState.value = _documentUiState.value.copy(documentIssuer = documentClientOrIssuer)
    }

    fun updateTextFieldCursorOfSalesOrderState(pageElement: ScreenElement) {
        val text = when (pageElement) {
            ScreenElement.DOCUMENT_NUMBER -> salesOrderUiState.value.documentNumber.text
            ScreenElement.DOCUMENT_REFERENCE -> salesOrderUiState.value.reference?.text
            else -> null
        }
        _documentUiState.value = updateSalesOrderUiState(
            _documentUiState.value, pageElement, TextFieldValue(
                text = text ?: "",
                selection = TextRange(text?.length ?: 0),
            )
        )
    }
}

fun updateSalesOrderUiState(
    salesOrder: SalesOrderState,
    element: ScreenElement,
    value: Any,
): SalesOrderState {
    var doc = salesOrder
    when (element) {
        ScreenElement.DOCUMENT_NUMBER -> doc = doc.copy(documentNumber = value as TextFieldValue)
        ScreenElement.DOCUMENT_DATE -> doc = doc.copy(documentDate = value as String)
        ScreenElement.DOCUMENT_CLIENT -> doc = doc.copy(documentClient = value as ClientOrIssuerState)
        ScreenElement.DOCUMENT_ISSUER -> doc = doc.copy(documentIssuer = value as ClientOrIssuerState)
        ScreenElement.DOCUMENT_REFERENCE -> doc = doc.copy(reference = value as TextFieldValue)
        ScreenElement.DOCUMENT_FREE_FIELD -> doc = doc.copy(freeField = value as TextFieldValue)
        ScreenElement.DOCUMENT_PRODUCT -> {
            updateDocumentProductList(value as DocumentProductState, doc)?.let {
                doc = doc.copy(documentProducts = it)
                doc = doc.copy(documentTotalPrices = calculateDocumentPrices(it))
            }
        }
        ScreenElement.DOCUMENT_CURRENCY -> doc = doc.copy(currency = value as TextFieldValue)
        ScreenElement.DOCUMENT_FOOTER -> doc = doc.copy(footerText = value as TextFieldValue)
        else -> {}
    }
    return doc
}
