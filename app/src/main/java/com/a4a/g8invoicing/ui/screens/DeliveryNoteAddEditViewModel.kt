package com.a4a.g8invoicing.ui.screens

import android.content.ContentValues
import android.util.Log
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSourceInterface
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.data.DeliveryNoteLocalDataSourceInterface
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
class DeliveryNoteAddEditViewModel @Inject constructor(
    private val deliveryNoteDataSource: DeliveryNoteLocalDataSourceInterface,
    private val documentClientOrIssuerDataSource: ClientOrIssuerLocalDataSourceInterface,
    private val documentProductDataSource: ProductLocalDataSourceInterface,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private var fetchJob: Job? = null
    private var saveJob: Job? = null
    private var updateJob: Job? = null
    private var deleteJob: Job? = null

    // Getting the argument in "DeliveryNoteAddEdit?itemId={itemId}" with savedStateHandle
    private var id: String? = savedStateHandle["itemId"]

    private val _deliveryNoteUiState = MutableStateFlow(DeliveryNoteState())
    val deliveryNoteUiState: StateFlow<DeliveryNoteState> = _deliveryNoteUiState

    init {
        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            _deliveryNoteUiState.debounce(300)
                .collect { updateDeliveryNoteInLocalDb() }
        }

        viewModelScope.launch(context = Dispatchers.Default) {
            try {
                id?.let {
                    fetchDeliveryNoteFromLocalDb(it.toLong())
                } ?: deliveryNoteDataSource.createNewDeliveryNote()?.let {
                    fetchDeliveryNoteFromLocalDb(it)
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }


    private fun fetchDeliveryNoteFromLocalDb(id: Long) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                deliveryNoteDataSource.fetchDeliveryNote(id)?.let {
                    _deliveryNoteUiState.value = it
                }
            } catch (e: Exception) {
                println("Fetching deliveryNote failed with exception: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun updateDeliveryNoteInLocalDb() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            try {
                id?.let {
                    deliveryNoteDataSource.updateDeliveryNote(deliveryNoteUiState.value)
                }
            } catch (e: Exception) {
                println("Saving deliveryNote failed with exception: ${e.localizedMessage}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        GlobalScope.launch {
            updateDeliveryNoteInLocalDb()
        }
    }

    fun saveDocumentProductInLocalDb(documentProduct: DocumentProductState) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            try {

                deliveryNoteDataSource.saveDocumentProductInDbAndLinkToDocument(
                    documentProduct = documentProduct,
                    deliveryNoteId = _deliveryNoteUiState.value.documentId?.toLong()
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
                _deliveryNoteUiState.value.documentId?.let {
                    deliveryNoteDataSource.deleteDeliveryNoteProduct(
                        it.toLong(),
                        documentProductId.toLong()
                    )
                }
                documentProductDataSource.deleteDocumentProducts(listOf(documentProductId.toLong()))
            } catch (e: Exception) {
                println("Deleting delivery note product failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun removeDocumentProductFromUiState(documentProductId: Int) {
        try {
            val list = _deliveryNoteUiState.value.documentProducts
                ?.filterNot { it.id == documentProductId }?.toMutableList()
            _deliveryNoteUiState.value = _deliveryNoteUiState.value.copy(
                documentProducts = list
            )
        } catch (e: Exception) {
            println("Deleting delivery note product failed with exception: ${e.localizedMessage}")
        }
    }

    fun saveDocumentProductInDeliveryNoteUiState(documentProduct: DocumentProductState) {
        try {
            val list = _deliveryNoteUiState.value.documentProducts
            var maxId = 1
            var newList: List<DocumentProductState> = listOf()

            if (!list.isNullOrEmpty()) {
                maxId = list.map { it.id }.filterNotNull().max()
            }

            if (documentProduct.id == null) {
                documentProduct.id = maxId + 1
            }

            newList = (list ?: emptyList()) + documentProduct

            _deliveryNoteUiState.value = _deliveryNoteUiState.value.copy(
                documentProducts = newList
            )
        } catch (e: Exception) {
            println("Saving delivery note product failed with exception: ${e.localizedMessage}")
        }
    }

    fun saveDocumentClientOrIssuerInLocalDb(documentClientOrIssuer: DocumentClientOrIssuerState) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            try {
                deliveryNoteDataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(
                    documentClientOrIssuer = documentClientOrIssuer,
                    deliveryNoteId = _deliveryNoteUiState.value.documentId?.toLong()
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
                _deliveryNoteUiState.value.documentId?.let {
                    deliveryNoteDataSource.deleteDocumentClientOrIssuer(
                        it.toLong(),
                        type
                    )
                }

                // useless??
                _deliveryNoteUiState.value.documentId?.let {
                    fetchDeliveryNoteFromLocalDb(it.toLong())
                }

            } catch (e: Exception) {
                println("Deleting delivery note client or issuer failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun removeDocumentClientOrIssuerFromUiState(type: ClientOrIssuerType) {
        if (type == ClientOrIssuerType.DOCUMENT_CLIENT)
            _deliveryNoteUiState.value = _deliveryNoteUiState.value.copy(
                documentClient = null
            )
        else _deliveryNoteUiState.value = _deliveryNoteUiState.value.copy(
            documentIssuer = null
        )
    }

    fun saveDocumentClientOrIssuerInDeliveryNoteUiState(documentClientOrIssuer: DocumentClientOrIssuerState) {
        if (documentClientOrIssuer.type == ClientOrIssuerType.DOCUMENT_CLIENT)
            _deliveryNoteUiState.value = _deliveryNoteUiState.value.copy(
                documentClient = documentClientOrIssuer
            )
        else _deliveryNoteUiState.value = _deliveryNoteUiState.value.copy(
            documentIssuer = documentClientOrIssuer
        )
    }

    fun updateDeliveryNoteState(screenElement: ScreenElement, value: Any) {
        _deliveryNoteUiState.value =
            updateDeliveryNoteUiState(_deliveryNoteUiState.value, screenElement, value)
    }

    fun updateTextFieldCursorOfDeliveryNoteState(pageElement: ScreenElement) {
        val text = when (pageElement) {
            ScreenElement.DOCUMENT_NUMBER -> deliveryNoteUiState.value.documentNumber.text
            ScreenElement.DOCUMENT_ORDER_NUMBER -> deliveryNoteUiState.value.orderNumber.text
            else -> null
        }

        _deliveryNoteUiState.value = updateDeliveryNoteUiState(
            _deliveryNoteUiState.value, pageElement, TextFieldValue(
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
    var note = deliveryNote
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
            // Recalculate the prices
            note.documentProducts?.let {
                note = note.copy(documentPrices = calculateDocumentPrices(it))
            }
        }

        ScreenElement.DOCUMENT_CURRENCY -> {
            note = note.copy(currency = value as TextFieldValue)
        }

        else -> {}
    }
    return note
}




