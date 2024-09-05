package com.a4a.g8invoicing.ui.viewmodels

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.InvoiceLocalDataSourceInterface
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.screens.shared.getDateFormatter
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.states.InvoicesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvoiceListViewModel @Inject constructor(
    private val invoiceDataSource: InvoiceLocalDataSourceInterface,
) : ViewModel() {

    private val _documentsUiState = MutableStateFlow(InvoicesUiState())
    val documentsUiState: StateFlow<InvoicesUiState> = _documentsUiState.asStateFlow()

    private var fetchJob: Job? = null
    private var deleteJob: Job? = null
    private var duplicateJob: Job? = null
    private var setTagJob: Job? = null
    private var markAsPaidJob: Job? = null

    init {
        fetch()
    }

    private fun fetch() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                invoiceDataSource.fetchAll()?.collect {
                    val lateDocuments = it.filter { isPaymentLate(it.dueDate) == true }
                    if (lateDocuments.isNotEmpty()) {
                        setTag(lateDocuments, DocumentTag.LATE)
                    }

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

    fun delete(selectedDocuments: List<InvoiceState>) {
        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            try {
                invoiceDataSource.delete(selectedDocuments)
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    fun duplicate(selectedDocuments: List<InvoiceState>) {
        duplicateJob?.cancel()
        duplicateJob = viewModelScope.launch {
            try {
                invoiceDataSource.duplicate(selectedDocuments)
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    fun setTag(selectedDocuments: List<InvoiceState>, tag: DocumentTag) {
        setTagJob?.cancel()
        setTagJob = viewModelScope.launch {
            try {
                // Set tag in Ui state
                selectedDocuments.forEach {
                    it.documentTag = tag
                }
                // Set tag in local db
                invoiceDataSource.setTag(selectedDocuments, tag)
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    fun markAsPaid(selectedDocuments: List<InvoiceState>, tag: DocumentTag) {
        markAsPaidJob?.cancel()
        markAsPaidJob = viewModelScope.launch {
            try {
                invoiceDataSource.markAsPaid(selectedDocuments, tag)
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



