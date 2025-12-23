package com.a4a.g8invoicing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.data.DeliveryNoteLocalDataSourceInterface
import com.a4a.g8invoicing.data.InvoiceLocalDataSourceInterface
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
    private val invoiceDataSource: InvoiceLocalDataSourceInterface,
) : ViewModel() {

    private val _deliveryNotesUiState = MutableStateFlow(DeliveryNotesUiState())
    val deliveryNotesUiState: StateFlow<DeliveryNotesUiState> = _deliveryNotesUiState.asStateFlow()

    private var fetchJob: Job? = null
    private var deleteJob: Job? = null
    private var duplicateJob: Job? = null
    private var convertJob: Job? = null

    init {
        fetchDeliveryNotes()
    }

    private fun fetchDeliveryNotes() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                deliveryNoteDataSource.fetchAll()?.collect {
                    _deliveryNotesUiState.update { deliveryNotesUiState ->
                        deliveryNotesUiState.copy(
                            deliveryNoteStates = it
                        )
                    }
                }
            } catch (e: Exception) {
                //println("Fetching all deliveryNotes failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun deleteDeliveryNotes(selectedDeliveryNotes: List<DeliveryNoteState>) {
        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            try {
                deliveryNoteDataSource.delete(selectedDeliveryNotes)
            } catch (e: Exception) {
                //println("Duplicating deliveryNotes failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun duplicateDeliveryNotes(selectedDeliveryNotes: List<DeliveryNoteState>) {
        duplicateJob?.cancel()
        duplicateJob = viewModelScope.launch {
            try {
                deliveryNoteDataSource.duplicate(selectedDeliveryNotes)
            } catch (e: Exception) {
                //println("Duplicating deliveryNotes failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun convertDeliveryNotes(selectedDeliveryNotes: List<DeliveryNoteState>) {
        convertJob?.cancel()
        convertJob = viewModelScope.launch {
            try {
                invoiceDataSource.convertDeliveryNotesToInvoice(selectedDeliveryNotes)
            } catch (e: Exception) {
                //println("Duplicating deliveryNotes failed with exception: ${e.localizedMessage}")
            }
        }
    }
}



