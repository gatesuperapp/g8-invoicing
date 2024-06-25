package com.a4a.g8invoicing.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.ui.states.DeliveryNote
import com.a4a.g8invoicing.data.DeliveryNoteLocalDataSourceInterface
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.ui.states.DeliveryNotesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeliveryNoteListViewModel @Inject constructor(
    private val deliveryNoteDataSource: DeliveryNoteLocalDataSourceInterface,
    private val documentProductDataSource: ProductLocalDataSourceInterface,
) : ViewModel() {

    private val _deliveryNotesUiState = MutableStateFlow(DeliveryNotesUiState())
    val deliveryNotesUiState: StateFlow<DeliveryNotesUiState> = _deliveryNotesUiState.asStateFlow()

    private var fetchJob: Job? = null
    private var fetchJob1: Job? = null
    private var deleteJob: Job? = null
    private var saveJob: Job? = null
    private var duplicateJob: Job? = null

    init {
        fetchDeliveryNotes()
    }

    private fun fetchDeliveryNotes() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                deliveryNoteDataSource.fetchAllDeliveryNotes().collect {
                    _deliveryNotesUiState.update { deliveryNotesUiState ->
                        deliveryNotesUiState.copy(
                            deliveryNoteStates = it
                        )
                    }
                }
            } catch (e: Exception) {
                println("Fetching all deliveryNotes failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun deleteDeliveryNotes(selectedDeliveryNotes: List<DeliveryNote>) {
        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            try {
                selectedDeliveryNotes.forEach { deliveryNote ->
                    deliveryNote.deliveryNoteId?.let {id ->
                        deliveryNoteDataSource.deleteDeliveryNote(id.toLong())

                        deliveryNote.documentProducts?.forEach { documentProduct ->
                            documentProduct.id?.let {
                                removeDocumentProductFromLocalDb(
                                    id.toLong(),
                                    it.toLong()
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("Deleting deliveryNotes failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun removeDocumentProductFromLocalDb(deliveryNoteId: Long, documentProductId: Long) {
        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            try {
                deliveryNoteDataSource.deleteDeliveryNoteProduct(
                    deliveryNoteId,
                    documentProductId
                )
                documentProductDataSource.deleteDocumentProduct(documentProductId)
            } catch (e: Exception) {
                println("Deleting delivery note product failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun duplicateDeliveryNotes(selectedDeliveryNotes: List<DeliveryNote>) {
        duplicateJob?.cancel()
        duplicateJob = viewModelScope.launch {
            try {
                deliveryNoteDataSource.duplicateDeliveryNotes(selectedDeliveryNotes)
            } catch (e: Exception) {
                println("Duplicating deliveryNotes failed with exception: ${e.localizedMessage}")
            }
        }
    }
}



