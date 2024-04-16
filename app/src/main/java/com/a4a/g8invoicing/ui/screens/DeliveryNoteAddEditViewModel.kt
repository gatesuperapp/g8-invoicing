package com.a4a.g8invoicing.ui.screens

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSourceInterface
import com.a4a.g8invoicing.data.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.data.DeliveryNoteLocalDataSourceInterface
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.data.calculateDocumentPrices
import com.a4a.g8invoicing.ui.shared.ScreenElement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeliveryNoteAddEditViewModel @Inject constructor(
    private val deliveryNoteDataSource: DeliveryNoteLocalDataSourceInterface,
    private val clientOrIssuerDataSource: ClientOrIssuerLocalDataSourceInterface,
    private val documentProductDataSource: ProductLocalDataSourceInterface,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private var fetchJob: Job? = null
    private var saveJob: Job? = null
    private var updateJob: Job? = null
    private var deleteJob: Job? = null

    // Getting the argument in "DeliveryNoteAddEdit?itemId={itemId}" with savedStateHandle
    private val id: String? = savedStateHandle["itemId"]
    private val _deliveryNoteUiState = mutableStateOf(DeliveryNoteState())
    val deliveryNoteUiState: State<DeliveryNoteState> = _deliveryNoteUiState

    init {
        id?.let {
            fetchDeliveryNoteFromLocalDb(it.toLong())
        }
    }

    private fun fetchDeliveryNoteFromLocalDb(id: Long) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                deliveryNoteDataSource.fetchDeliveryNoteFlow(id).collect {
                    it?.let {
                        _deliveryNoteUiState.value = it
                    }
                }
            } catch (e: Exception) {
                println("Fetching deliveryNotes failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun saveOrUpdateDeliveryNoteInLocalDb() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            try {
                id?.let {
                    deliveryNoteDataSource.updateDeliveryNote(deliveryNoteUiState.value)
                } ?: deliveryNoteDataSource.saveDeliveryNote(deliveryNoteUiState.value)

            } catch (e: Exception) {
                println("Saving deliveryNotes failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun saveDocumentProductInLocalDbAndInDeliveryNote(documentProduct: DocumentProductState) {
        _deliveryNoteUiState.value.deliveryNoteId?.let {
            saveDocumentProductInDbAndLinkToDeliveryNote(
                documentProduct = documentProduct,
                deliveryNoteDataSource = deliveryNoteDataSource,
                documentProductDataSource = documentProductDataSource,
                viewModelScope = viewModelScope,
                deliveryNoteId = it.toLong()
            )
        }
    }

    fun removeDocumentProductFromDeliveryNote(documentProductId: Int) {
        // Update the Ui State
        val newList = _deliveryNoteUiState.value.documentProducts?.toMutableList()
            ?.filter { it.id != documentProductId }
        newList?.let {
            updateDeliveryNoteState(ScreenElement.DOCUMENT_PRODUCTS, it)
        }

        // Update the db
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            try {
                documentProductDataSource.deleteDocumentProduct(documentProductId.toLong())
                deliveryNoteUiState.value.deliveryNoteId?.toLong()?.let {
                    deliveryNoteDataSource.deleteDeliveryNoteProduct(documentProductId.toLong())
                }
            } catch (e: Exception) {
                println("Deleting delivery note product failed with exception: ${e.localizedMessage}")
            }
        }
    }


    fun updateDeliveryNoteState(pageElement: ScreenElement, value: Any) {
        _deliveryNoteUiState.value =
            updateDeliveryNoteUiState(_deliveryNoteUiState.value, pageElement, value)
    }

    fun updateTextFieldCursorOfDeliveryNoteState(pageElement: ScreenElement) {
        val text = when (pageElement) {
            ScreenElement.DOCUMENT_NUMBER -> deliveryNoteUiState.value.number?.text
            ScreenElement.DOCUMENT_ORDER_NUMBER -> deliveryNoteUiState.value.orderNumber?.text
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
            note = note.copy(number = value as TextFieldValue)
        }

        ScreenElement.DOCUMENT_DATE -> {
            note = note.copy(deliveryDate = value as String)
        }

        ScreenElement.DOCUMENT_CLIENT -> {
            note = note.copy(client = value as ClientOrIssuerState)
        }

        ScreenElement.DOCUMENT_ISSUER -> {
            note = note.copy(issuer = value as ClientOrIssuerState)
        }

        ScreenElement.DOCUMENT_ORDER_NUMBER -> {
            note = note.copy(orderNumber = value as TextFieldValue)
        }

        ScreenElement.DOCUMENT_PRODUCTS -> {
            note = note.copy(documentProducts = value as List<DocumentProductState>)
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


fun saveDocumentProductInDbAndLinkToDeliveryNote(
    documentProduct: DocumentProductState,
    deliveryNoteDataSource: DeliveryNoteLocalDataSourceInterface,
    documentProductDataSource: ProductLocalDataSourceInterface,
    viewModelScope: CoroutineScope,
    deliveryNoteId: Long,
) {
    viewModelScope.launch {
        try {
            val documentProductId =
                documentProductDataSource.saveDocumentProduct(documentProduct)
            documentProductId?.let { id ->
                deliveryNoteDataSource.addDeliveryNoteProduct(
                    deliveryNoteId,
                    id.toLong()
                )
            }
        } catch (e: Exception) {
            println("Saving delivery note product failed with exception: ${e.localizedMessage}")
        }
    }
}
