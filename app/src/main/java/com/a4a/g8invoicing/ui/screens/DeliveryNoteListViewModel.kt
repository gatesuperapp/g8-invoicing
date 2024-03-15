package com.a4a.g8invoicing.ui.screens

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
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
                    println("yaaaa" + it)
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

    fun deleteDeliveryNotes(selectedDeliveryNotes: List<DeliveryNoteState>) {
        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            try {
                selectedDeliveryNotes.forEach {
                    it.deliveryNoteId?.let {
                        deliveryNoteDataSource.deleteDeliveryNote(it.toLong())
                    }
                }
            } catch (e: Exception) {
                println("Deleting deliveryNotes failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun duplicateDeliveryNotes(selectedDeliveryNotes: List<DeliveryNoteState>) {
        duplicateJob?.cancel()
        duplicateJob = viewModelScope.launch {
            try {
                selectedDeliveryNotes.forEach { selectedDeliveryNote ->
                    selectedDeliveryNote.deliveryNoteId?.let { noteId ->
                        var deliveryNote = deliveryNoteDataSource.fetchDeliveryNote(
                            noteId.toLong()
                        )

                        //TODO: get the string outta here
                        selectedDeliveryNote.number?.let {
                            deliveryNote = deliveryNote?.copy(
                                number = TextFieldValue("${it.text} - X")
                            )
                        }

                        deliveryNote?.let {
                            val deliveryNoteId = deliveryNoteDataSource.duplicateDeliveryNote(it)

                            deliveryNoteId?.let { id ->
                                it.documentProducts?.forEach { documentProduct ->
                                    saveDocumentProductInDbAndLinkToDeliveryNote(
                                        documentProduct = documentProduct,
                                        deliveryNoteDataSource = deliveryNoteDataSource,
                                        documentProductDataSource = documentProductDataSource,
                                        viewModelScope = viewModelScope,
                                        deliveryNoteId = id
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("Duplicating deliveryNotes failed with exception: ${e.localizedMessage}")
            }
        }
    }
}



