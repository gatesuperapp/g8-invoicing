package com.a4a.g8invoicing.ui.viewmodels

import android.content.ContentValues
import android.util.Log
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.ui.states.CreditNoteState
import com.a4a.g8invoicing.data.CreditNoteLocalDataSourceInterface
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.data.calculateDocumentPrices
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class CreditNoteAddEditViewModel @Inject constructor(
    private val documentDataSource: CreditNoteLocalDataSourceInterface,
    private val documentProductDataSource: ProductLocalDataSourceInterface,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private var fetchJob: Job? = null
    private var saveJob: Job? = null
    private var updateJob: Job? = null
    private var deleteJob: Job? = null
    private var autoSaveJob: Job? = null

    // Getting the argument in "CreditNoteAddEdit?itemId={itemId}" with savedStateHandle
    private var id: String? = savedStateHandle["itemId"]

    private val _documentUiState = MutableStateFlow(CreditNoteState())
    val documentUiState: StateFlow<CreditNoteState> = _documentUiState

    init {
        autoSaveInLocalDb()
        try {
            id?.let {
                fetchCreditNoteFromLocalDb(it.toLong())
            } ?: viewModelScope.launch(context = Dispatchers.Default) {
                createNewCreditNote()?.let {
                    fetchCreditNoteFromLocalDb(it)
                }
            }
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

    private fun autoSaveInLocalDb() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            @OptIn(FlowPreview::class)
            _documentUiState.debounce(300)
                .collect { updateCreditNoteInLocalDb() }
        }
    }

    private fun fetchCreditNoteFromLocalDb(id: Long) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                documentDataSource.fetch(id)?.let {
                    _documentUiState.value = it
                }
            } catch (e: Exception) {
                //println("Fetching deliveryNote failed with exception: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun createNewCreditNote(): Long? {
        var documentId: Long? = null
        val createNewJob = viewModelScope.launch {
            try {
                documentId = documentDataSource.createNew()
            } catch (e: Exception) {
                //println("Fetching deliveryNote failed with exception: ${e.localizedMessage}")
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
                //println("Saving deliveryNote failed with exception: ${e.localizedMessage}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        GlobalScope.launch {
            updateCreditNoteInLocalDb()
        }
    }

    fun saveDocumentProductInLocalDbAndWaitForTheId(documentProduct: DocumentProductState): Int? {
        var documentProductId: Int? = null
        runBlocking {  // launch a new coroutine and keep a reference to its Job
            try {
                documentProductId = documentDataSource.saveDocumentProductInDbAndLinkToDocument(
                    documentProduct = documentProduct,
                    id = _documentUiState.value.documentId?.toLong()
                )
            } catch (e: Exception) {
                //println("Saving documentProduct failed with exception: ${e.localizedMessage}")
            }
        }
        return documentProductId
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
                //println("Deleting delivery note product failed with exception: ${e.localizedMessage}")
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

            // Recalculate the prices
            _documentUiState.value.documentProducts?.let {
                _documentUiState.value =
                    _documentUiState.value.copy(documentPrices = calculateDocumentPrices(it))
            }


        } catch (e: Exception) {
            //println("Deleting delivery note product failed with exception: ${e.localizedMessage}")
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
            // Recalculate the prices
            _documentUiState.value.documentProducts?.let {
                _documentUiState.value =
                    _documentUiState.value.copy(documentPrices = calculateDocumentPrices(it))
            }
        } catch (e: Exception) {
            //println("Saving delivery note product failed with exception: ${e.localizedMessage}")
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
                //println("Saving documentProduct failed with exception: ${e.localizedMessage}")
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

                // useless??
                _documentUiState.value.documentId?.let {
                    fetchCreditNoteFromLocalDb(it.toLong())
                }

            } catch (e: Exception) {
                //println("Deleting CreditNote client or issuer failed with exception: ${e.localizedMessage}")
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
                doc = doc.copy(documentPrices = calculateDocumentPrices(it))
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




