package com.a4a.g8invoicing.ui.viewmodels

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.CreditNoteLocalDataSourceInterface
import com.a4a.g8invoicing.data.InvoiceLocalDataSourceInterface
import com.a4a.g8invoicing.data.models.TagUpdateOrCreationCase
import com.a4a.g8invoicing.data.util.DispatcherProvider
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.corrected_invoice_cancel_and_replace
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.states.InvoicesUiState
import g8invoicing.ClientOrIssuerQueries
import g8invoicing.DeliveryNoteQueries
import g8invoicing.InvoiceQueries
import g8invoicing.ProductQueries
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

class InvoiceListViewModel(
    private val invoiceDataSource: InvoiceLocalDataSourceInterface,
    private val creditNoteDataSource: CreditNoteLocalDataSourceInterface,
    private val invoiceQueries: InvoiceQueries,
    private val productQueries: ProductQueries,
    private val deliveryNoteQueries: DeliveryNoteQueries,
    private val clientOrIssuerQueries: ClientOrIssuerQueries
) : ViewModel() {

    private val _documentsUiState = MutableStateFlow(InvoicesUiState())
    val documentsUiState: StateFlow<InvoicesUiState> = _documentsUiState.asStateFlow()

    private var fetchJob: Job? = null
    private var deleteJob: Job? = null
    private var createCreditNoteJob: Job? = null
    private var createCorrectedInvoiceJob: Job? = null
    private var duplicateJob: Job? = null
    private var setTagJob: Job? = null
    private var sendReminderJob: Job? = null
    private var markAsPaidJob: Job? = null

    init {
        fetch()
    }

    private fun fetch() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                invoiceDataSource.fetchAll()
                    ?.flowOn(DispatcherProvider.IO)
                    ?.collect {
                        _documentsUiState.update { uiState ->
                            uiState.copy(
                                documentStates = it
                            )
                        }
                    }
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    fun delete(selectedDocuments: List<InvoiceState>) {
        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            try {
                invoiceDataSource.delete(selectedDocuments)
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    fun duplicate(selectedDocuments: List<InvoiceState>) {
        duplicateJob?.cancel()
        duplicateJob = viewModelScope.launch {
            try {
                invoiceDataSource.duplicate(selectedDocuments)
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    fun convertToCreditNote(selectedDocuments: List<InvoiceState>) {
        createCreditNoteJob?.cancel()
        createCreditNoteJob = viewModelScope.launch {
            try {
                creditNoteDataSource.convertInvoiceToCreditNote(selectedDocuments)
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    fun convertToCorrectedInvoice(selectedDocuments: List<InvoiceState>) {
        createCorrectedInvoiceJob?.cancel()
        createCorrectedInvoiceJob = viewModelScope.launch {
            try {
                val cancelAndReplaceText = getString(Res.string.corrected_invoice_cancel_and_replace)
                selectedDocuments.forEach {
                    it.freeField = TextFieldValue(cancelAndReplaceText + " " + it.documentNumber.text)
                }
                invoiceDataSource.duplicate(selectedDocuments)
                setTag(selectedDocuments, DocumentTag.CANCELLED, TagUpdateOrCreationCase.AUTOMATICALLY_CANCELLED)
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    fun setTag(selectedDocuments: List<InvoiceState>, tag: DocumentTag, tagUpdateCase: TagUpdateOrCreationCase) {
        setTagJob?.cancel()
        setTagJob = viewModelScope.launch {
            try {
                // Set tag in Ui state
                selectedDocuments.forEach {
                    it.documentTag = tag
                }
                // Set tag in local db
                invoiceDataSource.setTag(selectedDocuments, tag, tagUpdateCase)
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    fun markAsPaid(selectedDocuments: List<InvoiceState>, tag: DocumentTag) {
        markAsPaidJob?.cancel()
        markAsPaidJob = viewModelScope.launch {
            try {
                invoiceDataSource.markAsPaid(selectedDocuments, tag)
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    // Used to display download db popup to existing users
    fun hasUserData(): Boolean {
        return invoiceQueries.countAll().executeAsOne() > 3 ||
                deliveryNoteQueries.countAll().executeAsOne() > 3 ||
                productQueries.countAll().executeAsOne() > 3 ||
                clientOrIssuerQueries.countAll().executeAsOne() > 3
    }
}
