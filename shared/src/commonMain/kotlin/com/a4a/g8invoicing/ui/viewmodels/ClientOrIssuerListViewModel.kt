package com.a4a.g8invoicing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSourceInterface
import com.a4a.g8invoicing.data.models.PersonType
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.ClientsOrIssuerUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClientOrIssuerListViewModel(
    private val clientOrIssuerDataSource: ClientOrIssuerLocalDataSourceInterface,
) : ViewModel() {

    private val _clientsUiState = MutableStateFlow(ClientsOrIssuerUiState())
    val clientsUiState: StateFlow<ClientsOrIssuerUiState> =
        _clientsUiState.asStateFlow()

    private val _issuersUiState = MutableStateFlow(ClientsOrIssuerUiState())
    val issuersUiState: StateFlow<ClientsOrIssuerUiState> =
        _issuersUiState.asStateFlow()

    private var fetchJobClients: Job? = null
    private var fetchJobIssuers: Job? = null
    private var deleteJob: Job? = null
    private var saveJob: Job? = null
    private var duplicateJob: Job? = null

    init {
        fetchAllClients()
        fetchAllIssuers()
    }

    private fun fetchAllClients() {
        fetchJobClients?.cancel()
        fetchJobClients = viewModelScope.launch {
            try {
                clientOrIssuerDataSource.fetchAll(PersonType.CLIENT)
                    .collect { clientsOrIssuers ->
                        _clientsUiState.update {
                            it.copy(
                                clientsOrIssuerList = clientsOrIssuers.sortedWith(
                                    compareBy(String.CASE_INSENSITIVE_ORDER, { it.name.text })
                                )
                            )
                        }
                    }
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    private fun fetchAllIssuers() {
        fetchJobIssuers?.cancel()
        fetchJobIssuers = viewModelScope.launch {
            try {
                clientOrIssuerDataSource.fetchAll(PersonType.ISSUER)
                    .collect { clientsOrIssuers ->
                        _issuersUiState.update {
                            it.copy(
                                clientsOrIssuerList = clientsOrIssuers.sortedBy { it.name.text })
                        }
                    }
            } catch (e: Exception) {
                // Error handling
            }
        }
    }


    fun deleteClientsOrIssuers(selectedItems: List<ClientOrIssuerState>) {
        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            try {
                selectedItems.forEach { clientOrIssuer ->
                    clientOrIssuer.id?.let {
                        clientOrIssuerDataSource.deleteClientOrIssuer(clientOrIssuer)
                    }
                }
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    fun duplicateClientsOrIssuers(selectedItems: List<ClientOrIssuerState>) {
        duplicateJob?.cancel()
        duplicateJob = viewModelScope.launch {
            try {
                clientOrIssuerDataSource.duplicateClients(selectedItems)
            } catch (e: Exception) {
                // Error handling
            }
        }
    }
}

data class Message(val id: Long, val message: String)
