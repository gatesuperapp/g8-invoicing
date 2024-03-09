package com.a4a.g8invoicing.ui.screens

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSourceInterface
import com.a4a.g8invoicing.data.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.ClientsOrIssuerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientOrIssuerListViewModel @Inject constructor(
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
                clientOrIssuerDataSource.fetchAll(PersonType.Client)
                    .collect { clientsOrIssuers ->
                        _clientsUiState.update {
                            it.copy(
                                clientsOrIssuers = clientsOrIssuers.sortedWith(
                                    compareBy(String.CASE_INSENSITIVE_ORDER, { it.name.text })
                                )
                            )
                        }
                    }
            } catch (e: Exception) {
                println("Fetching clients failed with exception: ${e.localizedMessage}")
            }
        }
    }

    private fun fetchAllIssuers() {
        fetchJobIssuers?.cancel()
        fetchJobIssuers = viewModelScope.launch {
            try {
                clientOrIssuerDataSource.fetchAll(PersonType.Issuer)
                    .collect { clientsOrIssuers ->
                        _issuersUiState.update {
                            it.copy(
                                clientsOrIssuers = clientsOrIssuers.sortedBy { it.name.text })
                        }
                    }
            } catch (e: Exception) {
                println("Fetching issuers failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun fetchClientOrIssuerFromLocalDb(id: Long): ClientOrIssuerState? {
        return clientOrIssuerDataSource.fetchClientOrIssuer(id)
    }

    fun deleteClientsOrIssuers(selectedItems: List<ClientOrIssuerState>) {
        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            try {
                selectedItems.forEach { selectedClientOrIssuer ->
                    selectedClientOrIssuer.id?.let {
                        clientOrIssuerDataSource.deleteClientOrIssuer(it.toLong())
                    }
                }
            } catch (e: Exception) {
                println("Deleting clients failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun duplicateClientsOrIssuers(selectedItems: List<ClientOrIssuerState>) {
        duplicateJob?.cancel()
        duplicateJob = viewModelScope.launch {
            try {
                selectedItems.forEach { selectedClientOrIssuer ->
                    selectedClientOrIssuer.id?.let {
                        var clientOrIssuer =
                            clientOrIssuerDataSource.fetchClientOrIssuer(it.toLong())
                        //TODO: get the string outta here
                        clientOrIssuer = if (!clientOrIssuer?.firstName?.text.isNullOrEmpty()) {
                            clientOrIssuer?.copy(firstName = TextFieldValue("${selectedClientOrIssuer.firstName?.text} - Copie"))
                        } else {
                            clientOrIssuer?.copy(name = TextFieldValue("${selectedClientOrIssuer.name.text} - Copie"))
                        }
                        clientOrIssuer?.let { client ->
                            clientOrIssuerDataSource.duplicateClientOrIssuer(client)
                        }
                    }
                }
            } catch (e: Exception) {
                println("Duplicating clients failed with exception: ${e.localizedMessage}")
            }
        }
    }
}

data class Message(val id: Long, val message: String)


enum class PersonType {
    Client, Issuer
}
