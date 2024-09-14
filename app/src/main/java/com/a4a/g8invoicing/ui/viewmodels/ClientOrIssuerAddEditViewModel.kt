package com.a4a.g8invoicing.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSourceInterface
import com.a4a.g8invoicing.ui.shared.FormInputsValidator
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.AddressState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    private var autoSaveJob: Job? = null

    // Getting the argument in "ClientAddEdit?itemId={itemId}" with savedStateHandle
    private val id: String? = savedStateHandle["itemId"]
    private val type: String? = savedStateHandle["type"]

    private val _clientUiState =
        mutableStateOf(ClientOrIssuerState(type = ClientOrIssuerType.CLIENT))
    val clientUiState: State<ClientOrIssuerState> = _clientUiState

    private val _issuerUiState =
        mutableStateOf(ClientOrIssuerState(type = ClientOrIssuerType.ISSUER))
    private val issuerUiState: State<ClientOrIssuerState> = _issuerUiState

    private val _documentClientUiState =
        MutableStateFlow(ClientOrIssuerState(type = ClientOrIssuerType.DOCUMENT_CLIENT))
    val documentClientUiState: StateFlow<ClientOrIssuerState> = _documentClientUiState

    private val _documentIssuerUiState =
        MutableStateFlow(ClientOrIssuerState(type = ClientOrIssuerType.DOCUMENT_ISSUER))
    val documentIssuerUiState: StateFlow<ClientOrIssuerState> = _documentIssuerUiState


    init {
        // When coming from the navigation (NavGraph) we must get the savedStateHandle and init
        // -- & it is necessarily a client, as there's no "Issuers" list in the menu
        // When coming from a document (the bottom sheet form), no need to init
        if (type == ClientOrIssuerType.CLIENT.name.lowercase()) {
            id?.let {
                fetchFromLocalDb(it.toLong())
            }
        }
    }


    // Used when sliding the bottom form from documents
    // Editing a document client or issuer
    fun setDocumentClientOrIssuerUiState(documentClientOrIssuer: ClientOrIssuerState) {
        if (documentClientOrIssuer.type == ClientOrIssuerType.DOCUMENT_CLIENT) {
            _documentClientUiState.value = documentClientOrIssuer
            _documentClientUiState.value.errors = mutableListOf()
        } else {
            _documentIssuerUiState.value = documentClientOrIssuer
        }
    }

    // Used when sliding the bottom form from documents
    // Choosing a client or issuer
    fun setDocumentClientOrIssuerUiStateWithSelected(clientOrIssuer: ClientOrIssuerState) {
        if (clientOrIssuer.type == ClientOrIssuerType.CLIENT) {
            _documentClientUiState.value = ClientOrIssuerState(
                id = null,
                name = clientOrIssuer.name,
                phone = clientOrIssuer.phone,
                email = clientOrIssuer.email,
                type = if (clientOrIssuer.type == ClientOrIssuerType.ISSUER) ClientOrIssuerType.DOCUMENT_ISSUER
                else ClientOrIssuerType.DOCUMENT_CLIENT,
                firstName = clientOrIssuer.firstName,
                addresses = clientOrIssuer.addresses,
                notes = clientOrIssuer.notes,
                companyId1Label = clientOrIssuer.companyId1Label,
                companyId1Number = clientOrIssuer.companyId1Number,
                companyId2Label = clientOrIssuer.companyId2Label,
                companyId2Number = clientOrIssuer.companyId2Number,
                errors = mutableListOf()
            )
        } else _documentIssuerUiState.value = ClientOrIssuerState(
            id = null,
            name = clientOrIssuer.name,
            type = clientOrIssuer.type,
            firstName = clientOrIssuer.firstName,
            addresses = clientOrIssuer.addresses,
            phone = clientOrIssuer.phone,
            email = clientOrIssuer.email,
            notes = clientOrIssuer.notes,
            companyId1Label = clientOrIssuer.companyId1Label,
            companyId1Number = clientOrIssuer.companyId1Number,
            companyId2Label = clientOrIssuer.companyId2Label,
            companyId2Number = clientOrIssuer.companyId2Number,
            errors = mutableListOf()
        )
    }

    fun setClientOrIssuerUiState(type: ClientOrIssuerType) {
        if (type == ClientOrIssuerType.CLIENT) {
            _clientUiState.value = ClientOrIssuerState(
                id = null,
                type = type,
                firstName = _documentClientUiState.value.firstName,
                name = _documentClientUiState.value.name,
                addresses = _documentClientUiState.value.addresses,
                phone = _documentClientUiState.value.phone,
                email = _documentClientUiState.value.email,
                notes = _documentClientUiState.value.notes,
                companyId1Label = _documentClientUiState.value.companyId1Label,
                companyId1Number = _documentClientUiState.value.companyId1Number,
                companyId2Label = _documentClientUiState.value.companyId2Label,
                companyId2Number = _documentClientUiState.value.companyId2Number
            )
        } else _issuerUiState.value = ClientOrIssuerState(
            id = null,
            type = type,
            firstName = _documentIssuerUiState.value.firstName,
            name = _documentIssuerUiState.value.name,
            addresses = _documentClientUiState.value.addresses,
            phone = _documentIssuerUiState.value.phone,
            email = _documentIssuerUiState.value.email,
            notes = _documentIssuerUiState.value.notes,
            companyId1Label = _documentIssuerUiState.value.companyId1Label,
            companyId1Number = _documentIssuerUiState.value.companyId1Number,
            companyId2Label = _documentIssuerUiState.value.companyId2Label,
            companyId2Number = _documentIssuerUiState.value.companyId2Number
        )
    }

    fun clearClientOrIssuerUiState(type: ClientOrIssuerType) {
        if (type == ClientOrIssuerType.DOCUMENT_CLIENT) {
            _clientUiState.value = ClientOrIssuerState()
            _documentClientUiState.value = ClientOrIssuerState()
        } else {
            _issuerUiState.value = ClientOrIssuerState()
            _documentIssuerUiState.value = ClientOrIssuerState()
        }
    }


    private fun fetchFromLocalDb(id: Long) {
        val clientOrIssuer: ClientOrIssuerState? = dataSource.fetch(id)

        clientOrIssuer?.let {
            _clientUiState.value = it
        }
    }

    fun createNew(type: ClientOrIssuerType) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            val stateToSave = when (type) {
                ClientOrIssuerType.CLIENT -> _clientUiState.value
                ClientOrIssuerType.ISSUER ->_issuerUiState.value
                ClientOrIssuerType.DOCUMENT_CLIENT -> _documentClientUiState.value
                ClientOrIssuerType.DOCUMENT_ISSUER -> _documentIssuerUiState.value
            }

            try {
                dataSource.createNew(stateToSave)
            } catch (e: Exception) {
                println("Saving clients failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun updateClientOrIssuerInLocalDb(type: ClientOrIssuerType) {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            try {
                when (type) {
                    ClientOrIssuerType.CLIENT -> {
                        dataSource.updateClientOrIssuer(clientUiState.value)
                    }

                    ClientOrIssuerType.DOCUMENT_CLIENT -> {
                        dataSource.updateDocumentClientOrIssuer(documentClientUiState.value)
                    }

                    ClientOrIssuerType.ISSUER -> {
                        dataSource.updateClientOrIssuer(issuerUiState.value)
                    }

                    ClientOrIssuerType.DOCUMENT_ISSUER -> {
                        dataSource.updateDocumentClientOrIssuer(documentIssuerUiState.value)
                    }
                }

            } catch (e: Exception) {
                println("Updating clients failed with exception: ${e.localizedMessage}")
            }
        }
    }

    fun updateClientOrIssuerState(
        pageElement: ScreenElement,
        value: Any,
        type: ClientOrIssuerType,
    ) {
        when (type) {
            ClientOrIssuerType.CLIENT -> _clientUiState.value =
                updateClientOrIssuerUiState(_clientUiState.value, pageElement, value)

            ClientOrIssuerType.ISSUER -> _issuerUiState.value =
                updateClientOrIssuerUiState(_issuerUiState.value, pageElement, value)

            ClientOrIssuerType.DOCUMENT_CLIENT -> {
                _documentClientUiState.value = updateDocumentClientOrIssuerUiState(
                    _documentClientUiState.value,
                    pageElement,
                    value
                )
            }

            ClientOrIssuerType.DOCUMENT_ISSUER -> {
                _documentIssuerUiState.value =
                    updateDocumentClientOrIssuerUiState(
                        _documentIssuerUiState.value,
                        pageElement,
                        value
                    )
            }
        }
    }

    fun removeAddressFromClientOrIssuerState(
        type: ClientOrIssuerType,
    ) {
        when (type) {
            ClientOrIssuerType.CLIENT -> {
                val newAddresses = _clientUiState.value.addresses?.dropLast(1)
                _clientUiState.value = _clientUiState.value.copy(
                    addresses = newAddresses
                )
            }

            ClientOrIssuerType.ISSUER -> {
                val newAddresses = _issuerUiState.value.addresses?.dropLast(1)
                _issuerUiState.value = _issuerUiState.value.copy(
                    addresses = newAddresses
                )
            }

            ClientOrIssuerType.DOCUMENT_CLIENT -> {
                val newAddresses = _documentClientUiState.value.addresses?.dropLast(1)
                _documentClientUiState.value = _documentClientUiState.value.copy(
                    addresses = newAddresses
                )
            }

            ClientOrIssuerType.DOCUMENT_ISSUER -> {
                val newAddresses = _documentIssuerUiState.value.addresses?.dropLast(1)
                _documentIssuerUiState.value = _documentIssuerUiState.value.copy(
                    addresses = newAddresses
                )
            }
        }
    }

    fun updateCursor(pageElement: ScreenElement, type: ClientOrIssuerType) {
        when (type) {
            ClientOrIssuerType.CLIENT -> {
                val text = updateCursorOfClientOrIssuer(type, pageElement)
                text?.let {
                    _clientUiState.value = updateClientOrIssuerUiState(
                        _clientUiState.value, pageElement, TextFieldValue(
                            text = it,
                            selection = TextRange(it.length)
                        )
                    )
                }
            }

            ClientOrIssuerType.ISSUER -> {
                val text = updateCursorOfClientOrIssuer(type, pageElement)
                text?.let {
                    _issuerUiState.value = updateClientOrIssuerUiState(
                        _issuerUiState.value, pageElement, TextFieldValue(
                            text = it,
                            selection = TextRange(it.length)
                        )
                    )
                }
            }

            ClientOrIssuerType.DOCUMENT_CLIENT -> {
                val text = updateCursorOfDocumentClientOrIssuer(type, pageElement)
                text?.let {
                    _documentClientUiState.value = updateDocumentClientOrIssuerUiState(
                        _documentClientUiState.value, pageElement, TextFieldValue(
                            text = it,
                            selection = TextRange(it.length)
                        )
                    )
                }
            }

            ClientOrIssuerType.DOCUMENT_ISSUER -> {
                val text = updateCursorOfDocumentClientOrIssuer(type, pageElement)
                text?.let {
                    _documentIssuerUiState.value = updateDocumentClientOrIssuerUiState(
                        _documentIssuerUiState.value, pageElement, TextFieldValue(
                            text = it,
                            selection = TextRange(it.length)
                        )
                    )
                }
            }

            else -> {}
        }
    }

    private fun updateCursorOfClientOrIssuer(
        type: ClientOrIssuerType,
        pageElement: ScreenElement,
    ): String? {
        val firstAddress = if (type == ClientOrIssuerType.CLIENT)
            _clientUiState.value.addresses?.getOrNull(0)
        else _issuerUiState.value.addresses?.getOrNull(0)
        val secondAddress = if (type == ClientOrIssuerType.CLIENT)
            _clientUiState.value.addresses?.getOrNull(1)
        else _issuerUiState.value.addresses?.getOrNull(1)

        val thirdAddress = if (type == ClientOrIssuerType.CLIENT)
            _clientUiState.value.addresses?.getOrNull(2)
        else _issuerUiState.value.addresses?.getOrNull(2)

        val text = when (pageElement) {
            ScreenElement.CLIENT_OR_ISSUER_NAME ->
                if (type == ClientOrIssuerType.CLIENT) _clientUiState.value.name.text
                else _issuerUiState.value.name.text

            ScreenElement.CLIENT_OR_ISSUER_FIRST_NAME ->
                if (type == ClientOrIssuerType.CLIENT) _clientUiState.value.firstName?.text
                else _issuerUiState.value.firstName?.text

            ScreenElement.CLIENT_OR_ISSUER_EMAIL ->
                if (type == ClientOrIssuerType.CLIENT) _clientUiState.value.email?.text
                else _issuerUiState.value.email?.text

            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_TITLE_1 -> {
                firstAddress?.let {
                    it.addressTitle?.text
                }
            }

            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_1_1 -> {
                firstAddress?.let {
                    it.addressLine1?.text
                }
            }

            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_2_1 -> {
                firstAddress?.let {
                    it.addressLine2?.text
                }
            }

            ScreenElement.CLIENT_OR_ISSUER_ZIP_1 -> {
                firstAddress?.let {
                    it.addressLine1?.text
                }
            }

            ScreenElement.CLIENT_OR_ISSUER_CITY_1 -> {
                firstAddress?.let {
                    it.city?.text
                }
            }

            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_TITLE_2 -> {
                secondAddress?.let {
                    it.addressTitle?.text
                }
            }

            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_1_2 -> {
                secondAddress?.let {
                    it.addressLine1?.text
                }
            }

            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_2_2 -> {
                secondAddress?.let {
                    it.addressLine2?.text
                }
            }

            ScreenElement.CLIENT_OR_ISSUER_ZIP_2 -> {
                secondAddress?.let {
                    it.addressLine1?.text
                }
            }

            ScreenElement.CLIENT_OR_ISSUER_CITY_2 -> {
                secondAddress?.let {
                    it.city?.text
                }
            }

            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_TITLE_3 -> {
                thirdAddress?.let {
                    it.addressTitle?.text
                }
            }

            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_1_3 -> {
                thirdAddress?.let {
                    it.addressLine1?.text
                }
            }

            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_2_3 -> {
                thirdAddress?.let {
                    it.addressLine2?.text
                }
            }

            ScreenElement.CLIENT_OR_ISSUER_ZIP_3 -> {
                thirdAddress?.let {
                    it.addressLine1?.text
                }
            }

            ScreenElement.CLIENT_OR_ISSUER_CITY_3 -> {
                thirdAddress?.let {
                    it.city?.text
                }
            }

            ScreenElement.CLIENT_OR_ISSUER_PHONE ->
                if (type == ClientOrIssuerType.CLIENT) _clientUiState.value.phone?.text
                else _issuerUiState.value.phone?.text

            ScreenElement.CLIENT_OR_ISSUER_NOTES ->
                if (type == ClientOrIssuerType.CLIENT) _clientUiState.value.notes?.text
                else _issuerUiState.value.notes?.text

            ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION1_LABEL ->
                if (type == ClientOrIssuerType.CLIENT) _clientUiState.value.companyId1Label?.text
                else _issuerUiState.value.companyId1Label?.text

            ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION1_VALUE ->
                if (type == ClientOrIssuerType.CLIENT) _clientUiState.value.companyId1Number?.text
                else _issuerUiState.value.companyId1Number?.text

            ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION2_LABEL ->
                if (type == ClientOrIssuerType.CLIENT) _clientUiState.value.companyId2Label?.text
                else _issuerUiState.value.companyId2Label?.text

            ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION2_VALUE ->
                if (type == ClientOrIssuerType.CLIENT) _clientUiState.value.companyId2Number?.text
                else _issuerUiState.value.companyId2Number?.text

            else -> null
        }
        return text
    }

    private fun updateCursorOfDocumentClientOrIssuer(
        type: ClientOrIssuerType,
        pageElement: ScreenElement,
    ): String? {
        val firstAddress = if (type == ClientOrIssuerType.DOCUMENT_CLIENT)
            _documentClientUiState.value.addresses?.getOrNull(0)
        else _documentIssuerUiState.value.addresses?.getOrNull(0)
        val secondAddress = if (type == ClientOrIssuerType.DOCUMENT_CLIENT)
            _documentClientUiState.value.addresses?.getOrNull(1)
        else _documentIssuerUiState.value.addresses?.getOrNull(1)

        val thirdAddress = if (type == ClientOrIssuerType.DOCUMENT_CLIENT)
            _documentClientUiState.value.addresses?.getOrNull(2)
        else _documentIssuerUiState.value.addresses?.getOrNull(2)

        val text = when (pageElement) {
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_NAME ->
                if (type == ClientOrIssuerType.CLIENT) _documentClientUiState.value.name.text
                else _documentIssuerUiState.value.name.text

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_FIRST_NAME ->
                if (type == ClientOrIssuerType.CLIENT) _documentClientUiState.value.firstName?.text
                else _documentIssuerUiState.value.firstName?.text

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_EMAIL ->
                if (type == ClientOrIssuerType.CLIENT) _documentClientUiState.value.email?.text
                else _documentIssuerUiState.value.email?.text

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_TITLE_1 -> {
                firstAddress?.let {
                    it.addressTitle?.text
                }
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_1_1 -> {
                firstAddress?.let {
                    it.addressLine1?.text
                }
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_2_1 -> {
                firstAddress?.let {
                    it.addressLine2?.text
                }
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ZIP_1 -> {
                firstAddress?.let {
                    it.addressLine1?.text
                }
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_CITY_1 -> {
                firstAddress?.let {
                    it.city?.text
                }
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_TITLE_2 -> {
                secondAddress?.let {
                    it.addressTitle?.text
                }
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_1_2 -> {
                secondAddress?.let {
                    it.addressLine1?.text
                }
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_2_2 -> {
                secondAddress?.let {
                    it.addressLine2?.text
                }
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ZIP_2 -> {
                secondAddress?.let {
                    it.addressLine1?.text
                }
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_CITY_2 -> {
                secondAddress?.let {
                    it.city?.text
                }
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_TITLE_3 -> {
                thirdAddress?.let {
                    it.addressTitle?.text
                }
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_1_3 -> {
                thirdAddress?.let {
                    it.addressLine1?.text
                }
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_2_3 -> {
                thirdAddress?.let {
                    it.addressLine2?.text
                }
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ZIP_3 -> {
                thirdAddress?.let {
                    it.addressLine1?.text
                }
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_CITY_3 -> {
                thirdAddress?.let {
                    it.city?.text
                }
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_PHONE ->
                if (type == ClientOrIssuerType.DOCUMENT_CLIENT) _documentClientUiState.value.phone?.text
                else _documentIssuerUiState.value.phone?.text

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_NOTES ->
                if (type == ClientOrIssuerType.DOCUMENT_CLIENT) _documentClientUiState.value.notes?.text
                else _documentIssuerUiState.value.notes?.text

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION1_LABEL ->
                if (type == ClientOrIssuerType.DOCUMENT_CLIENT) _documentClientUiState.value.companyId1Label?.text
                else _documentIssuerUiState.value.companyId1Label?.text

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION1_VALUE ->
                if (type == ClientOrIssuerType.DOCUMENT_CLIENT) _documentClientUiState.value.companyId1Number?.text
                else _documentIssuerUiState.value.companyId1Number?.text

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION2_LABEL ->
                if (type == ClientOrIssuerType.DOCUMENT_CLIENT) _documentClientUiState.value.companyId2Label?.text
                else _documentIssuerUiState.value.companyId2Label?.text

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION2_VALUE ->
                if (type == ClientOrIssuerType.DOCUMENT_CLIENT) _documentClientUiState.value.companyId2Number?.text
                else _documentIssuerUiState.value.companyId2Number?.text

            else -> null
        }
        return text
    }

    fun getLastCreatedClientId(): Long? {
        var lastClientId: Long? = null
        runBlocking {
            val getLastItemIdJob = launch {
                try {
                    lastClientId = dataSource.getLastCreatedClientId()
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
        val firstAddress = person.addresses?.getOrNull(0)
        val secondAddress = person.addresses?.getOrNull(1)
        val thirdAddress = person.addresses?.getOrNull(2)

        when (element) {
            ScreenElement.CLIENT_OR_ISSUER_NAME -> person =
                person.copy(name = value as TextFieldValue)

            ScreenElement.CLIENT_OR_ISSUER_FIRST_NAME -> person =
                person.copy(firstName = value as TextFieldValue)

            ScreenElement.CLIENT_OR_ISSUER_EMAIL -> person =
                person.copy(email = value as TextFieldValue)

            ScreenElement.CLIENT_OR_ISSUER_PHONE -> person =
                person.copy(phone = value as TextFieldValue)

            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_TITLE_1 -> {
                val newAddress = firstAddress?.copy(addressTitle = value as TextFieldValue)
                    ?: AddressState(addressTitle = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 0))
            }

            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_1_1 -> {
                val newAddress = firstAddress?.copy(addressLine1 = value as TextFieldValue)
                    ?: AddressState(addressLine1 = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 0))
            }

            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_2_1 -> {
                val newAddress = firstAddress?.copy(addressLine2 = value as TextFieldValue)
                    ?: AddressState(addressLine2 = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 0))
            }

            ScreenElement.CLIENT_OR_ISSUER_ZIP_1 -> {
                val newAddress = firstAddress?.copy(zipCode = value as TextFieldValue)
                    ?: AddressState(zipCode = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 0))
            }

            ScreenElement.CLIENT_OR_ISSUER_CITY_1 -> {
                val newAddress = firstAddress?.copy(city = value as TextFieldValue)
                    ?: AddressState(city = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 0))
            }

            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_TITLE_2 -> {
                val newAddress = secondAddress?.copy(addressTitle = value as TextFieldValue)
                    ?: AddressState(addressTitle = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 1))
            }

            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_1_2 -> {
                val newAddress = secondAddress?.copy(addressLine1 = value as TextFieldValue)
                    ?: AddressState(addressLine1 = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 1))
            }

            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_2_2 -> {
                val newAddress = secondAddress?.copy(addressLine2 = value as TextFieldValue)
                    ?: AddressState(addressLine2 = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 1))
            }

            ScreenElement.CLIENT_OR_ISSUER_ZIP_2 -> {
                val newAddress = secondAddress?.copy(zipCode = value as TextFieldValue)
                    ?: AddressState(zipCode = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 1))
            }

            ScreenElement.CLIENT_OR_ISSUER_CITY_2 -> {
                val newAddress = secondAddress?.copy(city = value as TextFieldValue)
                    ?: AddressState(city = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 1))
            }

            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_TITLE_3 -> {
                val newAddress = thirdAddress?.copy(addressTitle = value as TextFieldValue)
                    ?: AddressState(addressTitle = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 2))
            }

            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_1_3 -> {
                val newAddress = thirdAddress?.copy(addressLine1 = value as TextFieldValue)
                    ?: AddressState(addressLine1 = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 2))
            }

            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_2_3 -> {
                val newAddress = thirdAddress?.copy(addressLine2 = value as TextFieldValue)
                    ?: AddressState(addressLine2 = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 2))
            }

            ScreenElement.CLIENT_OR_ISSUER_ZIP_3 -> {
                val newAddress = thirdAddress?.copy(zipCode = value as TextFieldValue)
                    ?: AddressState(zipCode = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 2))
            }

            ScreenElement.CLIENT_OR_ISSUER_CITY_3 -> {
                val newAddress = thirdAddress?.copy(city = value as TextFieldValue)
                    ?: AddressState(city = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 2))
            }

            ScreenElement.CLIENT_OR_ISSUER_NOTES -> person =
                person.copy(notes = value as TextFieldValue)

            ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION1_LABEL -> person =
                person.copy(companyId1Label = value as TextFieldValue)

            ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION1_VALUE -> person =
                person.copy(companyId1Number = value as TextFieldValue)

            ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION2_LABEL -> person =
                person.copy(companyId2Label = value as TextFieldValue)

            ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION2_VALUE -> person =
                person.copy(companyId2Number = value as TextFieldValue)

            else -> null
        }
        return person
    }

    private fun getNewAddresses(
        newAddress: AddressState,
        addresses: List<AddressState>?,
        addressIndex: Int,
    ): List<AddressState> {

        val initialAddresses = addresses ?: mutableListOf()
        val newAddresses = if (initialAddresses.isEmpty()) listOf(newAddress) else
            initialAddresses.slice(0 until addressIndex) + newAddress + initialAddresses.slice(
                addressIndex + 1 until initialAddresses.size
            )
        return newAddresses
    }

    private fun updateDocumentClientOrIssuerUiState(
        documentClientOrIssuer: ClientOrIssuerState,
        element: ScreenElement,
        value: Any,
    ): ClientOrIssuerState {
        var person = documentClientOrIssuer
        val firstAddress = person.addresses?.getOrNull(0)
        val secondAddress = person.addresses?.getOrNull(1)
        val thirdAddress = person.addresses?.getOrNull(2)

        when (element) {
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_NAME -> person =
                person.copy(name = value as TextFieldValue)

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_FIRST_NAME -> person =
                person.copy(firstName = value as TextFieldValue)

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_EMAIL -> person =
                person.copy(email = value as TextFieldValue)

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_PHONE -> person =
                person.copy(phone = value as TextFieldValue)

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_TITLE_1 -> {
                val newAddress = firstAddress?.copy(addressTitle = value as TextFieldValue)
                    ?: AddressState(addressTitle = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 0))
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_1_1 -> {
                val newAddress = firstAddress?.copy(addressLine1 = value as TextFieldValue)
                    ?: AddressState(addressLine1 = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 0))
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_2_1 -> {
                val newAddress = firstAddress?.copy(addressLine2 = value as TextFieldValue)
                    ?: AddressState(addressLine2 = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 0))
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ZIP_1 -> {
                val newAddress = firstAddress?.copy(zipCode = value as TextFieldValue)
                    ?: AddressState(zipCode = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 0))
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_CITY_1 -> {
                val newAddress = firstAddress?.copy(city = value as TextFieldValue)
                    ?: AddressState(city = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 0))
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_TITLE_2 -> {
                val newAddress = secondAddress?.copy(addressTitle = value as TextFieldValue)
                    ?: AddressState(addressTitle = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 1))
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_1_2 -> {
                val newAddress = secondAddress?.copy(addressLine1 = value as TextFieldValue)
                    ?: AddressState(addressLine1 = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 1))
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_2_2 -> {
                val newAddress = secondAddress?.copy(addressLine2 = value as TextFieldValue)
                    ?: AddressState(addressLine2 = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 1))
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ZIP_2 -> {
                val newAddress = secondAddress?.copy(zipCode = value as TextFieldValue)
                    ?: AddressState(zipCode = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 1))
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_CITY_2 -> {
                val newAddress = secondAddress?.copy(city = value as TextFieldValue)
                    ?: AddressState(city = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 1))
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_TITLE_3 -> {
                val newAddress = thirdAddress?.copy(addressTitle = value as TextFieldValue)
                    ?: AddressState(addressTitle = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 2))
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_1_3 -> {
                val newAddress = thirdAddress?.copy(addressLine1 = value as TextFieldValue)
                    ?: AddressState(addressLine1 = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 2))
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_2_3 -> {
                val newAddress = thirdAddress?.copy(addressLine2 = value as TextFieldValue)
                    ?: AddressState(addressLine2 = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 2))
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ZIP_3 -> {
                val newAddress = thirdAddress?.copy(zipCode = value as TextFieldValue)
                    ?: AddressState(zipCode = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 2))
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_CITY_3 -> {
                val newAddress = thirdAddress?.copy(city = value as TextFieldValue)
                    ?: AddressState(city = value as TextFieldValue)

                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 2))
            }


            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_NOTES -> person =
                person.copy(notes = value as TextFieldValue)

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION1_LABEL -> person =
                person.copy(companyId1Label = value as TextFieldValue)

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION1_VALUE -> person =
                person.copy(companyId1Number = value as TextFieldValue)

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION2_LABEL -> person =
                person.copy(companyId2Label = value as TextFieldValue)

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION2_VALUE -> person =
                person.copy(companyId2Number = value as TextFieldValue)

            else -> null
        }
        return person
    }

    fun validateInputs(type: ClientOrIssuerType): Boolean {
        val listOfErrors: MutableList<Pair<ScreenElement, String?>> = mutableListOf()
        when (type) {
            ClientOrIssuerType.CLIENT -> {
                FormInputsValidator.validateName(_clientUiState.value.name.text)?.let {
                    listOfErrors.add(Pair(ScreenElement.CLIENT_OR_ISSUER_NAME, it))
                }
                FormInputsValidator.validateEmail(_clientUiState.value.email?.text)?.let {
                    listOfErrors.add(Pair(ScreenElement.CLIENT_OR_ISSUER_EMAIL, it))
                }
                _clientUiState.value = _clientUiState.value.copy(
                    errors = listOfErrors
                )
            }

            ClientOrIssuerType.ISSUER -> {
                FormInputsValidator.validateName(_issuerUiState.value.name.text)?.let {
                    listOfErrors.add(Pair(ScreenElement.CLIENT_OR_ISSUER_NAME, it))
                }
                FormInputsValidator.validateEmail(_issuerUiState.value.email?.text)?.let {
                    listOfErrors.add(Pair(ScreenElement.CLIENT_OR_ISSUER_EMAIL, it))
                }
                _issuerUiState.value = _issuerUiState.value.copy(
                    errors = listOfErrors
                )
            }

            ClientOrIssuerType.DOCUMENT_CLIENT -> {
                FormInputsValidator.validateName(_documentClientUiState.value.name.text)?.let {
                    listOfErrors.add(Pair(ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_NAME, it))
                }
                FormInputsValidator.validateEmail(_documentClientUiState.value.email?.text)?.let {
                    listOfErrors.add(Pair(ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_EMAIL, it))
                }
                _documentClientUiState.value = _documentClientUiState.value.copy(
                    errors = listOfErrors
                )
            }

            ClientOrIssuerType.DOCUMENT_ISSUER -> {
                FormInputsValidator.validateName(_documentIssuerUiState.value.name.text)?.let {
                    listOfErrors.add(Pair(ScreenElement.CLIENT_OR_ISSUER_NAME, it))
                }
                FormInputsValidator.validateEmail(_documentIssuerUiState.value.email?.text)?.let {
                    listOfErrors.add(Pair(ScreenElement.CLIENT_OR_ISSUER_EMAIL, it))
                }
                _documentIssuerUiState.value = _documentIssuerUiState.value.copy(
                    errors = listOfErrors
                )
            }
        }
        return listOfErrors.isEmpty()
    }

    fun clearValidateInputErrors(type: ClientOrIssuerType) {
        when (type) {
            ClientOrIssuerType.CLIENT -> _clientUiState.value.errors.clear()
            ClientOrIssuerType.ISSUER -> _issuerUiState.value.errors.clear()
            ClientOrIssuerType.DOCUMENT_CLIENT -> {
                _documentClientUiState.value = ClientOrIssuerState()
                _documentClientUiState.value.errors.clear()
            }

            ClientOrIssuerType.DOCUMENT_ISSUER -> _documentIssuerUiState.value.errors.clear()
        }
    }
}

enum class ClientOrIssuerType {
    CLIENT, ISSUER, DOCUMENT_CLIENT, DOCUMENT_ISSUER
}
