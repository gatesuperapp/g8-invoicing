package com.a4a.g8invoicing.ui.viewmodels

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.DeliveryNoteLocalDataSourceInterface
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.data.calculateDocumentPrices
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class DeliveryNoteAddEditViewModel(
    private val documentDataSource: DeliveryNoteLocalDataSourceInterface,
    private val documentProductDataSource: ProductLocalDataSourceInterface,
    private val itemId: String?,
) : ViewModel() {
    private var fetchJob: Job? = null
    private var createNewJob: Job? = null
    private var saveJob: Job? = null
    private var updateJob: Job? = null
    private var deleteJob: Job? = null
    private var autoSaveJob: Job? = null

    private val _documentUiState = MutableStateFlow(DeliveryNoteState())
    val deliveryNoteUiState: StateFlow<DeliveryNoteState> = _documentUiState

    init {
        autoSaveInLocalDb()
        try {
            itemId?.let {
                fetchDeliveryNoteFromLocalDb(it.toLong())
            } ?: viewModelScope.launch {
                createNewDeliveryNoteInVM()?.let {
                    fetchDeliveryNoteFromLocalDb(it)
                }
            }
        } catch (e: Exception) {
            // Error handling
        }
    }

    @OptIn(FlowPreview::class)
    private fun autoSaveInLocalDb() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            _documentUiState.debounce(300)
                .collect { updateDeliveryNoteInLocalDb() }
        }
    }

    private fun fetchDeliveryNoteFromLocalDb(id: Long) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                documentDataSource.fetch(id)?.let {
                    _documentUiState.value = it
                }
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    private suspend fun createNewDeliveryNoteInVM(): Long? {
        var deliveryNoteId: Long? = null
        val createNewJob = viewModelScope.launch {
            try {
                deliveryNoteId = documentDataSource.createNew()
            } catch (e: Exception) {
                // Error handling
            }
        }
        createNewJob.join()
        return deliveryNoteId
    }

    fun updateUiState(screenElement: ScreenElement, value: Any) {
        _documentUiState.value =
            updateDeliveryNoteUiState(_documentUiState.value, screenElement, value)
    }

    private fun updateDeliveryNoteInLocalDb() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            try {
                documentDataSource.update(deliveryNoteUiState.value)
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            updateDeliveryNoteInLocalDb()
        }
    }

    suspend fun saveDocumentProductInLocalDbAndGetId(documentProduct: DocumentProductState): Int? {
        val currentDocumentId = _documentUiState.value.documentId?.toLong()
        if (currentDocumentId == null) {
            return null
        }

        return try {
            documentDataSource.saveDocumentProductInDbAndLinkToDocument(
                documentProduct = documentProduct,
                documentId = currentDocumentId
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
                        orderedProducts = productsWithUpdatedSortOrder
                    )
                }
            } catch (e: Exception) {
                // Error handling
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
                        documentProductId.toLong()
                    )
                }
                documentProductDataSource.deleteDocumentProducts(listOf(documentProductId.toLong()))
            } catch (e: Exception) {
                // Error handling
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
            // Error handling
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
            // Error handling
        }
    }

    fun saveDocumentClientOrIssuerInLocalDb(documentClientOrIssuer: ClientOrIssuerState) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            try {
                documentDataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(
                    documentClientOrIssuer = documentClientOrIssuer,
                    documentId = _documentUiState.value.documentId?.toLong()
                )
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    fun removeDocumentClientOrIssuerFromLocalDb(type: ClientOrIssuerType) {
        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            try {
                _documentUiState.value.documentId?.let {
                    documentDataSource.deleteDocumentClientOrIssuer(
                        it.toLong(),
                        type
                    )
                }

                _documentUiState.value.documentId?.let {
                    fetchDeliveryNoteFromLocalDb(it.toLong())
                }
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    fun removeDocumentClientOrIssuerFromUiState(type: ClientOrIssuerType) {
        if (type == ClientOrIssuerType.DOCUMENT_CLIENT)
            _documentUiState.value = _documentUiState.value.copy(
                documentClient = null
            )
        else _documentUiState.value = _documentUiState.value.copy(
            documentIssuer = null
        )
    }

    fun saveDocumentClientOrIssuerInUiState(documentClientOrIssuer: ClientOrIssuerState) {
        if (documentClientOrIssuer.type == ClientOrIssuerType.DOCUMENT_CLIENT)
            _documentUiState.value = _documentUiState.value.copy(
                documentClient = documentClientOrIssuer
            )
        else _documentUiState.value = _documentUiState.value.copy(
            documentIssuer = documentClientOrIssuer
        )
    }

    fun updateTextFieldCursorOfDeliveryNoteState(pageElement: ScreenElement) {
        val text = when (pageElement) {
            ScreenElement.DOCUMENT_NUMBER -> deliveryNoteUiState.value.documentNumber.text
            ScreenElement.DOCUMENT_REFERENCE -> deliveryNoteUiState.value.reference?.text
            else -> null
        }

        _documentUiState.value = updateDeliveryNoteUiState(
            _documentUiState.value, pageElement, TextFieldValue(
                text = text ?: "",
                selection = TextRange(text?.length ?: 0)
            )
        )
    }
}

fun updateDeliveryNoteUiState(
    deliveryNote: DeliveryNoteState,
    element: ScreenElement,
    value: Any,
): DeliveryNoteState {
    var doc = deliveryNote
    when (element) {
        ScreenElement.DOCUMENT_NUMBER -> {
            doc = doc.copy(documentNumber = value as TextFieldValue)
        }

        ScreenElement.DOCUMENT_DATE -> {
            doc = doc.copy(documentDate = value as String)
        }

        ScreenElement.DOCUMENT_CLIENT -> {
            doc = doc.copy(documentClient = value as ClientOrIssuerState)
        }

        ScreenElement.DOCUMENT_ISSUER -> {
            doc = doc.copy(documentIssuer = value as ClientOrIssuerState)
        }

        ScreenElement.DOCUMENT_REFERENCE -> {
            doc = doc.copy(reference = value as TextFieldValue)
        }

        ScreenElement.DOCUMENT_FREE_FIELD -> {
            doc = doc.copy(freeField = value as TextFieldValue)
        }

        ScreenElement.DOCUMENT_PRODUCT -> {
            updateDocumentProductList(
                value as DocumentProductState,
                doc
            )?.let {
                doc = doc.copy(documentProducts = it)
                doc = doc.copy(documentTotalPrices = calculateDocumentPrices(it))
            }
        }

        ScreenElement.DOCUMENT_CURRENCY -> {
            doc = doc.copy(currency = value as TextFieldValue)
        }

        ScreenElement.DOCUMENT_FOOTER -> {
            doc = doc.copy(footerText = value as TextFieldValue)
        }

        else -> {}
    }
    return doc
}
