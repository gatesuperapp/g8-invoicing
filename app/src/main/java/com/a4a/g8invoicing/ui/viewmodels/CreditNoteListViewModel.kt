package com.a4a.g8invoicing.ui.viewmodels

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.CreditNoteLocalDataSourceInterface
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.screens.shared.getDateFormatter
import com.a4a.g8invoicing.ui.states.CreditNoteState
import com.a4a.g8invoicing.ui.states.CreditNotesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreditNoteListViewModel @Inject constructor(
    private val creditNoteDataSource: CreditNoteLocalDataSourceInterface,
) : ViewModel() {
    private val _documentsUiState = MutableStateFlow(CreditNotesUiState())
    val documentsUiState: StateFlow<CreditNotesUiState> = _documentsUiState.asStateFlow()
    private var fetchJob: Job? = null
    private var deleteJob: Job? = null
    private var duplicateJob: Job? = null
    private var setTagJob: Job? = null

    init {
        fetch()
    }

    private fun fetch() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                creditNoteDataSource.fetchAll()?.collect {
                    _documentsUiState.update { uiState ->
                        uiState.copy(
                            documentStates = it
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    fun delete(selectedDocuments: List<CreditNoteState>) {
        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            try {
                creditNoteDataSource.delete(selectedDocuments)
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    fun duplicate(selectedDocuments: List<CreditNoteState>) {
        duplicateJob?.cancel()
        duplicateJob = viewModelScope.launch {
            try {
                creditNoteDataSource.duplicate(selectedDocuments)
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    fun setTag(selectedDocuments: List<CreditNoteState>, tag: DocumentTag) {
        setTagJob?.cancel()
        setTagJob = viewModelScope.launch {
            try {
                // Set tag in Ui state
                selectedDocuments.forEach {
                    it.documentTag = tag
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    private fun isPaymentLate(dueDate: String): Boolean {
        val formatter = getDateFormatter()
        val dueDate = formatter.parse(dueDate)?.time
        val currentDate = java.util.Date().time
        val isLatePayment = dueDate != null && dueDate < currentDate
        return isLatePayment
    }
}



