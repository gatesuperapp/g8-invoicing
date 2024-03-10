package com.a4a.g8invoicing.ui.screens

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.ClientOrIssuerState
import com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSourceInterface
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.CompanyDataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class ClientOrIssuerAddEditViewModel @Inject constructor(
    private val dataSource: ClientOrIssuerLocalDataSourceInterface,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private var saveJob: Job? = null
    private var updateJob: Job? = null

    // Getting the argument in "ClientAddEdit?itemId={itemId}" with savedStateHandle
    private val id: String? = savedStateHandle["itemId"]
    private val type: String? = savedStateHandle["type"]

    private val _clientUiState = mutableStateOf(ClientOrIssuerState())
    val clientUiState: State<ClientOrIssuerState> = _clientUiState

    private val _issuerUiState = mutableStateOf(ClientOrIssuerState())
    val issuerUiState: State<ClientOrIssuerState> = _issuerUiState

    init {
        // We initialize only if coming from the navigation (NavGraph)
        // Not if calling from a document (to open the bottom sheet form)
        if (type == ClientOrIssuerType.CLIENT.name.lowercase()) {
            id?.let {
                fetchFromLocalDb(it.toLong())
            }
        }
    }

    fun setClientUiState(client: ClientOrIssuerState) {
        _clientUiState.value = client
    }
    fun setIssuerUiState(issuer: ClientOrIssuerState) {
        _issuerUiState.value = issuer
    }

    private fun fetchFromLocalDb(id: Long) {
        val clientOrIssuer: ClientOrIssuerState? = dataSource.fetchClientOrIssuer(id)

        _clientUiState.value = _clientUiState.value.copy(
            id = clientOrIssuer?.id,
            firstName = TextFieldValue(clientOrIssuer?.firstName?.text ?: ""),
            name = TextFieldValue(clientOrIssuer?.name?.text ?: ""),
            address1 = TextFieldValue(clientOrIssuer?.address1?.text ?: ""),
            address2 = TextFieldValue(clientOrIssuer?.address2?.text ?: ""),
            zipCode = TextFieldValue(clientOrIssuer?.zipCode?.text ?: ""),
            city = TextFieldValue(clientOrIssuer?.city?.text ?: ""),
            phone = TextFieldValue(clientOrIssuer?.phone?.text ?: ""),
            email = TextFieldValue(clientOrIssuer?.email?.text ?: ""),
            notes = TextFieldValue(clientOrIssuer?.notes?.text ?: "")
        )
    }

    fun saveInLocalDb(type: String) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            try {
                dataSource.saveClientOrIssuer(clientUiState.value, type)
            } catch (e: Exception) {
                println("Saving clients failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun updateClientInInLocalDb() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            try {
                dataSource.updateClientOrIssuer(clientUiState.value)
            } catch (e: Exception) {
                println("Updating clients failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun updateClientOrIssuerState(pageElement: ScreenElement, value: Any) {
        _clientUiState.value = updateClientOrIssuerUiState(_clientUiState.value, pageElement, value)
    }

    fun updateCursorOfClientOrIssuerState(pageElement: ScreenElement) {
        val text = when (pageElement) {
            ScreenElement.CLIENT_NAME -> clientUiState.value.name.text
            ScreenElement.CLIENT_FIRST_NAME -> clientUiState.value.firstName?.text
            ScreenElement.CLIENT_EMAIL -> clientUiState.value.email?.text
            ScreenElement.CLIENT_ADDRESS1 -> clientUiState.value.address1?.text
            ScreenElement.CLIENT_ADDRESS2 -> clientUiState.value.address2?.text
            ScreenElement.CLIENT_ZIP -> clientUiState.value.zipCode?.text
            ScreenElement.CLIENT_CITY -> clientUiState.value.city?.text
            ScreenElement.CLIENT_PHONE -> clientUiState.value.phone?.text
            ScreenElement.CLIENT_NOTES -> clientUiState.value.notes?.text
/*            ScreenElement.CLIENT_IDENTIFICATION1_LABEL -> clientUiState.value.companyData?.first()?.label?.text
            ScreenElement.CLIENT_IDENTIFICATION1_VALUE -> clientUiState.value.companyData?.first()?.number?.text
            ScreenElement.CLIENT_IDENTIFICATION2_LABEL -> clientUiState.value.companyData?.get(1)?.label?.text
            ScreenElement.CLIENT_IDENTIFICATION2_VALUE -> clientUiState.value.companyData?.get(1)?.number?.text*/
            else -> null
        }
        _clientUiState.value = updateClientOrIssuerUiState(
            _clientUiState.value, pageElement, TextFieldValue(
                text = text ?: "",
                selection = TextRange(text?.length ?: 0)
            )
        )
    }

    fun getLastCreated(): Long? {
        var lastClientId: Long? = null
        runBlocking {
            val getLastItemIdJob = launch {
                try {
                    lastClientId = dataSource.getLastCreatedClientOrIssuerId()
                    println("clientDataSource.getLastCreatedClient() = " + dataSource.getLastCreatedClientOrIssuerId())
                } catch (e: Exception) {
                    println("Getting last client failed with exception: ${e.localizedMessage}")
                }
            }
            getLastItemIdJob.join() // Waiting for the coroutine to complete
        }
        return lastClientId
    }

    private fun updateClientOrIssuerUiState(
        clientOrIssuer: ClientOrIssuerState,
        element: ScreenElement,
        value: Any,
    ): ClientOrIssuerState {
        var person = clientOrIssuer
        val companyData = mutableListOf(CompanyDataState(), CompanyDataState())
        when (element) {
            ScreenElement.CLIENT_NAME -> person = person.copy(name = value as TextFieldValue)
            ScreenElement.CLIENT_FIRST_NAME ->         person = person.copy(firstName = value as TextFieldValue)
            ScreenElement.CLIENT_EMAIL ->     person = person.copy(email = value as TextFieldValue)
            ScreenElement.CLIENT_ADDRESS1 -> person = person.copy(address1 = value as TextFieldValue)
            ScreenElement.CLIENT_ADDRESS2 -> person = person.copy(address2 = value as TextFieldValue)
            ScreenElement.CLIENT_ZIP -> person = person.copy(zipCode = value as TextFieldValue)
            ScreenElement.CLIENT_CITY -> person = person.copy(city = value as TextFieldValue)
            ScreenElement.CLIENT_PHONE -> person = person.copy(phone = value as TextFieldValue)
            ScreenElement.CLIENT_NOTES -> person = person.copy(notes = value as TextFieldValue)
            ScreenElement.CLIENT_IDENTIFICATION1_LABEL -> {
                companyData.first().label = value as TextFieldValue
                person = person.copy(companyData = companyData)
            }
            ScreenElement.CLIENT_IDENTIFICATION1_VALUE -> {
                companyData.first().number = value as TextFieldValue
                person = person.copy(companyData = companyData)
            }
            ScreenElement.CLIENT_IDENTIFICATION2_LABEL ->  {
                companyData[1].label = value as TextFieldValue
                person = person.copy(companyData = companyData)
            }
            ScreenElement.CLIENT_IDENTIFICATION2_VALUE ->  {
                companyData[1].number = value as TextFieldValue
                person = person.copy(companyData = companyData)
            }
            else -> null
        }
        return person
    }
}

enum class ClientOrIssuerType {
    CLIENT, ISSUER
}
