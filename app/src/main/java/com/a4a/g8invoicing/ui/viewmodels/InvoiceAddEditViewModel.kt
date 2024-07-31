package com.a4a.g8invoicing.ui.viewmodels

import android.content.ContentValues
import android.util.Log
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.data.InvoiceLocalDataSourceInterface
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.data.calculateDocumentPrices
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.DocumentClientOrIssuerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvoiceAddEditViewModel @Inject constructor(
    private val documentDataSource: InvoiceLocalDataSourceInterface,
    private val documentProductDataSource: ProductLocalDataSourceInterface,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private var fetchJob: Job? = null
    private var saveJob: Job? = null
    private var updateJob: Job? = null
    private var deleteJob: Job? = null

    // Getting the argument in "InvoiceAddEdit?itemId={itemId}" with savedStateHandle
    private var id: String? = savedStateHandle["itemId"]

    private val _documentUiState = MutableStateFlow(InvoiceState())
    val documentUiState: StateFlow<InvoiceState> = _documentUiState

    init {
        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            _documentUiState.debounce(300)
                .collect { updateInvoiceInLocalDb() }
        }

        viewModelScope.launch(context = Dispatchers.Default) {
            try {
                id?.let {
                    fetchInvoiceFromLocalDb(it.toLong())
                } ?: documentDataSource.createNew()?.let {
                    fetchInvoiceFromLocalDb(it)
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }


    private fun fetchInvoiceFromLocalDb(id: Long) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                documentDataSource.fetch(id)?.let {
                    _documentUiState.value = it
                }
            } catch (e: Exception) {
                println("Fetching deliveryNote failed with exception: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun updateInvoiceInLocalDb() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            try {
                id?.let {
                    documentDataSource.update(documentUiState.value)
                }
            } catch (e: Exception) {
                println("Saving deliveryNote failed with exception: ${e.localizedMessage}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        GlobalScope.launch {
            updateInvoiceInLocalDb()
        }
    }

    fun saveDocumentProductInLocalDb(documentProduct: DocumentProductState) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            try {

                documentDataSource.saveDocumentProductInDbAndLinkToDocument(
                    documentProduct = documentProduct,
                    id = _documentUiState.value.documentId?.toLong()
                )
            } catch (e: Exception) {
                println("Saving documentProduct failed with exception: ${e.localizedMessage}")
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

                // If several pages, decrement page of next element
                val numberOfPages = _documentUiState.value.documentProducts?.last()?.page
                numberOfPages?.let {numberOfPages ->
                    if(numberOfPages > 1) {
                        for(i in 2..numberOfPages) {
                            _documentUiState.value.documentProducts
                                ?.first { it.page == i }?.id?.let {
                                    updateInvoiceStateWithDecrementedValue(it)
                                }
                        }
                    }
                }
            } catch (e: Exception) {
                println("Deleting delivery note product failed with exception: ${e.localizedMessage}")
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

            // If several pages, decrement page of next element
            val numberOfPages = _documentUiState.value.documentProducts?.last()?.page
            numberOfPages?.let {numberOfPages ->
                if(numberOfPages > 1) {
                    for(i in 2..numberOfPages) {
                        _documentUiState.value.documentProducts
                            ?.first { it.page == i }?.id?.let {
                                updateInvoiceStateWithDecrementedValue(it)
                            }
                    }
                }
            }

            // Recalculate the prices
            _documentUiState.value.documentProducts?.let {
                _documentUiState.value =
                    _documentUiState.value.copy(documentPrices = calculateDocumentPrices(it))
            }


        } catch (e: Exception) {
            println("Deleting delivery note product failed with exception: ${e.localizedMessage}")
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
            println("Saving delivery note product failed with exception: ${e.localizedMessage}")
        }
    }

    fun saveDocumentClientOrIssuerInLocalDb(documentClientOrIssuer: DocumentClientOrIssuerState) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            try {
                documentDataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(
                    documentClientOrIssuer = documentClientOrIssuer,
                    id = _documentUiState.value.documentId?.toLong()
                )

            } catch (e: Exception) {
                println("Saving documentProduct failed with exception: ${e.localizedMessage}")
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
                    fetchInvoiceFromLocalDb(it.toLong())
                }

            } catch (e: Exception) {
                println("Deleting invoice client or issuer failed with exception: ${e.localizedMessage}")
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

    fun saveDocumentClientOrIssuerInUiState(documentClientOrIssuer: DocumentClientOrIssuerState) {
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
            updateInvoiceUiState(_documentUiState.value, screenElement, value)
    }

    fun updateStateWithIncrementedValue(documentProductId: Any) {
        val documentProduct =
            _documentUiState.value.documentProducts?.first { it.id == documentProductId }
        documentProduct?.let { docProduct ->
            val updatedDocumentProduct = docProduct.copy(page = docProduct.page + 1)
            val list =
                _documentUiState.value.documentProducts?.filterNot { it.id == documentProductId }
                    ?.toMutableList()
            val updatedList = (list ?: emptyList()) + updatedDocumentProduct
            _documentUiState.value =
                _documentUiState.value.copy(documentProducts = updatedList)
        }
    }

    private fun updateInvoiceStateWithDecrementedValue(documentProductId: Any) {
        val documentProduct =
            _documentUiState.value.documentProducts?.first { it.id == documentProductId }
        documentProduct?.let { docProduct ->
            val updatedDocumentProduct = docProduct.copy(page = docProduct.page - 1)
            val list =
                _documentUiState.value.documentProducts?.filterNot { it.id == documentProductId }
                    ?.toMutableList()
            val updatedList = (list ?: emptyList()) + updatedDocumentProduct
            _documentUiState.value =
                _documentUiState.value.copy(documentProducts = updatedList)
        }
    }

    fun updateTextFieldCursorOfInvoiceState(pageElement: ScreenElement) {
        val text = when (pageElement) {
            ScreenElement.DOCUMENT_NUMBER -> documentUiState.value.documentNumber.text
            ScreenElement.DOCUMENT_ORDER_NUMBER -> documentUiState.value.orderNumber.text
            else -> null
        }

        _documentUiState.value = updateInvoiceUiState(
            _documentUiState.value, pageElement, TextFieldValue(
                text = text ?: "",
                selection = TextRange(text?.length ?: 0)
            )
        )
    }

}

fun updateInvoiceUiState(
    document: InvoiceState,
    element: ScreenElement,
    value: Any,
): InvoiceState {
    var note = document
    when (element) {
        ScreenElement.DOCUMENT_NUMBER -> {
            note = note.copy(documentNumber = value as TextFieldValue)
        }

        ScreenElement.DOCUMENT_DATE -> {
            note = note.copy(documentDate = value as String)
        }

        ScreenElement.DOCUMENT_CLIENT -> {
            note = note.copy(documentClient = value as DocumentClientOrIssuerState)
        }

        ScreenElement.DOCUMENT_ISSUER -> {
            note = note.copy(documentIssuer = value as DocumentClientOrIssuerState)
        }

        ScreenElement.DOCUMENT_ORDER_NUMBER -> {
            note = note.copy(orderNumber = value as TextFieldValue)
        }

        ScreenElement.DOCUMENT_PRODUCT -> {
            val updatedDocumentProduct = value as DocumentProductState
            val list = note.documentProducts?.filterNot { it.id == value.id }?.toMutableList()
            val updatedList = (list ?: emptyList()) + updatedDocumentProduct

            note = note.copy(documentProducts = updatedList)
        }

        ScreenElement.DOCUMENT_CURRENCY -> {
            note = note.copy(currency = value as TextFieldValue)
        }

        else -> {}
    }
    return note
}




