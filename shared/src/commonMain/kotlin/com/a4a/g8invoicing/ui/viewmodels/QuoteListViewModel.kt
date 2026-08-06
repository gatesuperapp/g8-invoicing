package com.a4a.g8invoicing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.ui.states.QuoteState
import com.a4a.g8invoicing.data.QuoteLocalDataSourceInterface
import com.a4a.g8invoicing.data.InvoiceLocalDataSourceInterface
import com.a4a.g8invoicing.ui.states.QuotesUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuoteListViewModel(
    private val quoteDataSource: QuoteLocalDataSourceInterface,
    private val invoiceDataSource: InvoiceLocalDataSourceInterface,
) : ViewModel() {

    private val _quotesUiState = MutableStateFlow(QuotesUiState())
    val quotesUiState: StateFlow<QuotesUiState> = _quotesUiState.asStateFlow()

    private var fetchJob: Job? = null
    private var deleteJob: Job? = null
    private var duplicateJob: Job? = null
    private var convertJob: Job? = null

    init {
        fetchQuotes()
    }

    private fun fetchQuotes() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                quoteDataSource.fetchAll()?.collect {
                    _quotesUiState.update { quotesUiState ->
                        quotesUiState.copy(
                            quoteStates = it
                        )
                    }
                }
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    fun deleteQuotes(selectedQuotes: List<QuoteState>) {
        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            try {
                quoteDataSource.delete(selectedQuotes)
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    fun duplicateQuotes(selectedQuotes: List<QuoteState>) {
        duplicateJob?.cancel()
        duplicateJob = viewModelScope.launch {
            try {
                quoteDataSource.duplicate(selectedQuotes)
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    fun convertQuotes(selectedQuotes: List<QuoteState>) {
        convertJob?.cancel()
        convertJob = viewModelScope.launch {
            try {
                val invoiceId = invoiceDataSource.convertQuotesToInvoice(selectedQuotes)
                _quotesUiState.update { state ->
                    state.copy(createdInvoiceId = invoiceId)
                }
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    fun clearCreatedInvoiceId() {
        _quotesUiState.update { state ->
            state.copy(createdInvoiceId = null)
        }
    }
}
