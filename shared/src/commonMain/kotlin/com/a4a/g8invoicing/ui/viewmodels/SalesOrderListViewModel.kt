package com.a4a.g8invoicing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.DeliveryNoteLocalDataSourceInterface
import com.a4a.g8invoicing.data.InvoiceLocalDataSourceInterface
import com.a4a.g8invoicing.data.SalesOrderLocalDataSourceInterface
import com.a4a.g8invoicing.data.models.TagUpdateOrCreationCase
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.states.SalesOrderState
import com.a4a.g8invoicing.ui.states.SalesOrdersUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SalesOrderListViewModel(
    private val salesOrderDataSource: SalesOrderLocalDataSourceInterface,
    private val invoiceDataSource: InvoiceLocalDataSourceInterface,
    private val deliveryNoteDataSource: DeliveryNoteLocalDataSourceInterface,
) : ViewModel() {

    private val _salesOrdersUiState = MutableStateFlow(SalesOrdersUiState())
    val salesOrdersUiState: StateFlow<SalesOrdersUiState> = _salesOrdersUiState.asStateFlow()

    private var fetchJob: Job? = null
    private var deleteJob: Job? = null
    private var duplicateJob: Job? = null
    private var convertJob: Job? = null
    private var setTagJob: Job? = null

    init {
        fetchSalesOrders()
    }

    private fun fetchSalesOrders() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                salesOrderDataSource.fetchAll()?.collect {
                    _salesOrdersUiState.update { state ->
                        state.copy(salesOrderStates = it)
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    fun deleteSalesOrders(selected: List<SalesOrderState>) {
        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            try {
                salesOrderDataSource.delete(selected)
            } catch (e: Exception) {
            }
        }
    }

    fun duplicateSalesOrders(selected: List<SalesOrderState>) {
        duplicateJob?.cancel()
        duplicateJob = viewModelScope.launch {
            try {
                salesOrderDataSource.duplicate(selected)
            } catch (e: Exception) {
            }
        }
    }

    fun convertSalesOrdersToInvoice(selected: List<SalesOrderState>) {
        convertJob?.cancel()
        convertJob = viewModelScope.launch {
            try {
                val invoiceId = invoiceDataSource.convertSalesOrdersToInvoice(selected)
                selected.forEach { it.documentTag = DocumentTag.INVOICED }
                salesOrderDataSource.setTag(
                    selected,
                    DocumentTag.INVOICED,
                    TagUpdateOrCreationCase.UPDATED_BY_USER,
                )
                _salesOrdersUiState.update { state ->
                    state.copy(createdInvoiceId = invoiceId)
                }
            } catch (e: Exception) {
            }
        }
    }

    fun convertSalesOrdersToDeliveryNote(selected: List<SalesOrderState>) {
        convertJob?.cancel()
        convertJob = viewModelScope.launch {
            try {
                val deliveryNoteId = deliveryNoteDataSource.convertSalesOrdersToDeliveryNote(selected)
                // Converting to a BL doesn't close the sales order (it's still
                // "to invoice") — keep its current tag untouched. The user can
                // re-run convert → Facture later from the same BC.
                _salesOrdersUiState.update { state ->
                    state.copy(createdDeliveryNoteId = deliveryNoteId)
                }
            } catch (e: Exception) {
            }
        }
    }

    fun setTag(
        selected: List<SalesOrderState>,
        tag: DocumentTag,
        tagUpdateCase: TagUpdateOrCreationCase,
    ) {
        setTagJob?.cancel()
        setTagJob = viewModelScope.launch {
            try {
                selected.forEach { it.documentTag = tag }
                salesOrderDataSource.setTag(selected, tag, tagUpdateCase)
            } catch (e: Exception) {
            }
        }
    }

    fun clearCreatedInvoiceId() {
        _salesOrdersUiState.update { state -> state.copy(createdInvoiceId = null) }
    }

    fun clearCreatedDeliveryNoteId() {
        _salesOrdersUiState.update { state -> state.copy(createdDeliveryNoteId = null) }
    }
}
