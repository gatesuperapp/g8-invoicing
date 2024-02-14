package com.a4a.g8invoicing.ui.screens

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.ClientOrIssuerEditable
import com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSourceInterface
import com.a4a.g8invoicing.ui.shared.ScreenElement
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
    private val _uiState = mutableStateOf(
        ClientOrIssuerEditable(
            id = null,
            firstName = TextFieldValue(""),
            name = TextFieldValue(""),
            address1 = TextFieldValue(""),
            address2 = TextFieldValue(""),
            zipCode = TextFieldValue(""),
            city = TextFieldValue(""),
            phone = TextFieldValue(""),
            email = TextFieldValue(""),
            notes = TextFieldValue("")
        )
    )
    val uiState: State<ClientOrIssuerEditable> = _uiState

    init {
        id?.let {
            fetchFromLocalDb(it.toLong())
        }
    }

    private fun fetchFromLocalDb(id: Long) {
        val clientOrIssuer: ClientOrIssuerEditable? = dataSource.fetchClientOrIssuer(id)

        _uiState.value = _uiState.value.copy(
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
                dataSource.saveClientOrIssuer(uiState.value, type)
            } catch (e: Exception) {
                println("Saving clients failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun saveInLocalDbBlocking(type: String) { // We need it to be blocking for the documents,
        // because we retrieve the id to pass back to the document Add/Edit
        // TODO: make it blocking only if previous screen is DeliveryclientOrIssuer Add/Edit or Invoice Add/edit
        // no need to be blocking coming from Clients page
        runBlocking {
            val getLastItemIdJob = launch {
                try {
                    dataSource.saveClientOrIssuer(uiState.value, type)
                } catch (e: Exception) {
                }
            }
            getLastItemIdJob.join() // Waiting for the coroutine to complete
        }
    }


    fun updateClientInInLocalDb() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            try {
                dataSource.updateClientOrIssuer(uiState.value)
            } catch (e: Exception) {
                println("Updating clients failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun updateClientOrIssuerState(pageElement: ScreenElement, value: Any) {
        _uiState.value = updateProductUiState(_uiState.value, pageElement, value)
    }

    fun updateCursorOfClientOrIssuerState(pageElement: ScreenElement) {
        val text = when (pageElement) {
            ScreenElement.CLIENT_NAME -> uiState.value.name.text
            ScreenElement.CLIENT_FIRST_NAME -> uiState.value.firstName?.text
            ScreenElement.CLIENT_EMAIL -> uiState.value.email?.text
            ScreenElement.CLIENT_ADDRESS1 -> uiState.value.address1?.text
            ScreenElement.CLIENT_ADDRESS2 -> uiState.value.address2?.text
            ScreenElement.CLIENT_ZIP -> uiState.value.zipCode?.text
            ScreenElement.CLIENT_CITY -> uiState.value.city?.text
            ScreenElement.CLIENT_PHONE -> uiState.value.phone?.text
            ScreenElement.CLIENT_NOTES -> uiState.value.notes?.text
            else -> null
        }
        _uiState.value = updateProductUiState(
            _uiState.value, pageElement, TextFieldValue(
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
                    lastClientId = dataSource.getLastCreatedId()
                    println("clientDataSource.getLastCreatedClient() = " + dataSource.getLastCreatedId())
                } catch (e: Exception) {
                    println("Getting last client failed with exception: ${e.localizedMessage}")
                }
            }
            getLastItemIdJob.join() // Waiting for the coroutine to complete
        }
        return lastClientId
    }
}

private fun updateProductUiState(
    clientOrIssuer: ClientOrIssuerEditable,
    element: ScreenElement,
    value: Any,
): ClientOrIssuerEditable {
    var person = clientOrIssuer
    when (element) {
        ScreenElement.CLIENT_NAME -> {
            person = person.copy(name = value as TextFieldValue)
        }

        ScreenElement.CLIENT_FIRST_NAME -> {
            person = person.copy(firstName = value as TextFieldValue)
        }

        ScreenElement.CLIENT_EMAIL -> {
            person = person.copy(email = value as TextFieldValue)
        }

        ScreenElement.CLIENT_ADDRESS1 -> {
            person = person.copy(address1 = value as TextFieldValue)
        }

        ScreenElement.CLIENT_ADDRESS2 -> {
            person = person.copy(address2 = value as TextFieldValue)
        }

        ScreenElement.CLIENT_ZIP -> {
            person = person.copy(zipCode = value as TextFieldValue)
        }

        ScreenElement.CLIENT_CITY -> {
            person = person.copy(city = value as TextFieldValue)
        }

        ScreenElement.CLIENT_PHONE -> {
            person = person.copy(phone = value as TextFieldValue)
        }

        ScreenElement.CLIENT_NOTES -> {
            person = person.copy(notes = value as TextFieldValue)
        }

        else -> null
    }
    return person
}
