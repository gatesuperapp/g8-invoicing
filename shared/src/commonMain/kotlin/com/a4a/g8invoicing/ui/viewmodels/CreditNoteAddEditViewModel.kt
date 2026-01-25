package com.a4a.g8invoicing.ui.viewmodels

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.CreditNoteLocalDataSourceInterface
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.data.calculateDocumentPrices
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.CreditNoteState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreditNoteAddEditViewModel(
    private val documentDataSource: CreditNoteLocalDataSourceInterface,
    private val documentProductDataSource: ProductLocalDataSourceInterface,
    private val itemId: String?,
) : ViewModel() {
    private var fetchJob: Job? = null
    private var saveJob: Job? = null
    private var updateJob: Job? = null
    private var deleteJob: Job? = null
    private var autoSaveJob: Job? = null

    private val _documentUiState = MutableStateFlow(CreditNoteState())
    val documentUiState: StateFlow<CreditNoteState> = _documentUiState

    init {
        autoSaveInLocalDb()
        try {
            itemId?.let {
                fetchCreditNoteFromLocalDb(it.toLong())
            } ?: viewModelScope.launch {
                val newId = createNewCreditNote()
                newId?.let {
                    fetchCreditNoteFromLocalDb(it)
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
                .collect { updateCreditNoteInLocalDb() }
        }
    }

    private fun fetchCreditNoteFromLocalDb(id: Long) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                val fetched = documentDataSource.fetch(id)
                fetched?.let {
                    _documentUiState.value = it
                }
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    private suspend fun createNewCreditNote(): Long? {
        var documentId: Long? = null
        val createNewJob = viewModelScope.launch {
            try {
                documentId = documentDataSource.createNew()
            } catch (e: Exception) {
                // Error handling
            }
        }
        createNewJob.join()
        return documentId
    }

    private suspend fun updateCreditNoteInLocalDb() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            try {
                documentDataSource.update(documentUiState.value)
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            updateCreditNoteInLocalDb()
        }
    }

    suspend fun saveDocumentProductInLocalDbAndGetId(documentProduct: DocumentProductState): Int? {
        val currentDocumentId = _documentUiState.value.documentId?.toLong()
            ?: return null

        return try {
            documentDataSource.saveDocumentProductInDbAndLinkToDocument(
                documentProduct = documentProduct,
                documentId = currentDocumentId
            )
        } catch (e: Exception) {
            null
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
        if (documentProduct.id == null) {
            println("Warning: Attempting to save DocumentProduct in db.")
        }

        val currentList = _documentUiState.value.documentProducts ?: emptyList()
        val newList = ArrayList(currentList)

        newList.add(documentProduct)

        _documentUiState.update { currentState ->
            currentState.copy(
                documentProducts = newList.toList(),
                documentTotalPrices = calculateDocumentPrices(newList.toList())
            )
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

    fun saveDocumentClientOrIssuerInLocalDb(documentClientOrIssuer: ClientOrIssuerState) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            try {
                documentDataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(
                    documentClientOrIssuer = documentClientOrIssuer,
                    id = _documentUiState.value.documentId?.toLong()
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
                    fetchCreditNoteFromLocalDb(it.toLong())
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

    fun updateUiState(screenElement: ScreenElement, value: Any) {
        _documentUiState.value =
            updateCreditNoteUiState(_documentUiState.value, screenElement, value)
    }

    fun updateTextFieldCursorOfCreditNoteState(pageElement: ScreenElement) {
        val text = when (pageElement) {
            ScreenElement.DOCUMENT_NUMBER -> documentUiState.value.documentNumber.text
            ScreenElement.DOCUMENT_REFERENCE -> documentUiState.value.reference?.text
            else -> null
        }

        _documentUiState.value = updateCreditNoteUiState(
            _documentUiState.value, pageElement, TextFieldValue(
                text = text ?: "",
                selection = TextRange(text?.length ?: 0)
            )
        )
    }
}

fun updateCreditNoteUiState(
    document: CreditNoteState,
    element: ScreenElement,
    value: Any,
): CreditNoteState {
    var doc = document
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

        ScreenElement.DOCUMENT_DUE_DATE -> {
            doc = doc.copy(dueDate = value as String)
        }

        ScreenElement.DOCUMENT_FOOTER -> {
            doc = doc.copy(footerText = value as TextFieldValue)
        }

        else -> {}
    }
    return doc
}
