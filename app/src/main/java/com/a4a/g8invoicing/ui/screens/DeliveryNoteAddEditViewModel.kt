package com.a4a.g8invoicing.ui.screens

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.ClientOrIssuerEditable
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.data.DeliveryNoteLocalDataSourceInterface
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.ui.shared.ScreenElement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeliveryNoteAddEditViewModel @Inject constructor(
    private val deliveryNoteDataSource: DeliveryNoteLocalDataSourceInterface,
    private val documentProductDataSource: ProductLocalDataSourceInterface,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
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
        deliveryNoteDataSource.fetchDeliveryNote(id)?.let {
            _deliveryNoteUiState.value = it
        }
    }

    fun saveDeliveryNoteInLocalDb() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            try {
                deliveryNoteDataSource.saveDeliveryNote(deliveryNoteUiState.value)
                deliveryNoteUiState.value.documentProducts?.forEach {
                    documentProductDataSource.saveDocumentProduct(it)
                }
            } catch (e: Exception) {
                println("Saving deliveryNotes failed with exception: ${e.localizedMessage}")
            }
        }
    }


    fun updateDeliveryNoteInLocalDb() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            try {
                deliveryNoteDataSource.updateDeliveryNote(deliveryNoteUiState.value)

            } catch (e: Exception) {
                println("Saving deliveryNotes failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun removeDocumentProductFromDeliveryNote(documentProductId: Int) {
        val newList = _deliveryNoteUiState.value.documentProducts?.toMutableList()
            ?.filter { it.id != documentProductId }
        newList?.let {
            updateDeliveryNote(ScreenElement.DOCUMENT_PRODUCTS, it)
        }

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

    fun addDocumentProductToDeliveryNote(documentProduct: DocumentProductState) {
       viewModelScope.launch {
            try {
                documentProductDataSource.saveDocumentProduct(documentProduct)
                deliveryNoteUiState.value.deliveryNoteId?.toLong()?.let {deliveryNoteId ->
                    documentProduct.id?.toLong()?.let {documentProductId ->
                        deliveryNoteDataSource.addDeliveryNoteProduct(
                            deliveryNoteId,
                            documentProductId
                        )
                    }
                }
            } catch (e: Exception) {
                println("Deleting delivery note product failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun updateDeliveryNote(pageElement: ScreenElement, value: Any) {
        _deliveryNoteUiState.value = updateDeliveryNoteUiState(_deliveryNoteUiState.value, pageElement, value)
        updateDeliveryNoteInLocalDb()
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

    fun updateProductState(pageElement: ScreenElement, value: Any, documentProductId: Int) {
        /*val documentProductToUpdate =  _deliveryNoteUiState.value.documentProducts?.first { it.id == documentProductId }
        documentProductToUpdate?.let {
            val newDocumentProduct = updateDocumentProductUiState(it, pageElement, value)

            val newList = _deliveryNoteUiState.value.documentProducts?.toMutableList()
                ?.filter { it.id != documentProductId }

            _deliveryNoteUiState.value = _deliveryNoteUiState.value.copy()

        }*/

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
            note = note.copy(client = value as ClientOrIssuerEditable)
        }

        ScreenElement.DOCUMENT_ISSUER -> {
            note = note.copy(issuer = value as ClientOrIssuerEditable)
        }

        ScreenElement.DOCUMENT_ORDER_NUMBER -> {
            note = note.copy(orderNumber = value as TextFieldValue)
        }

        ScreenElement.DOCUMENT_PRODUCTS -> {
            note = note.copy(documentProducts = value as List<DocumentProductState>)
        }

        ScreenElement.DOCUMENT_CURRENCY -> {
            note = note.copy(currency = value as TextFieldValue)
        }

        else -> {}
    }
    return note
}



