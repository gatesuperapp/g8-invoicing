package com.a4a.g8invoicing.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSourceInterface
import com.a4a.g8invoicing.ui.shared.FormInputsValidator
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.DocumentClientOrIssuerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
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
    private var autoSaveIssuerJob: Job? = null
    private var autoSaveClientJob: Job? = null

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
        MutableStateFlow(DocumentClientOrIssuerState(type = ClientOrIssuerType.DOCUMENT_CLIENT))
    val documentClientUiState: StateFlow<DocumentClientOrIssuerState> = _documentClientUiState

    private val _documentIssuerUiState =
        MutableStateFlow(DocumentClientOrIssuerState(type = ClientOrIssuerType.DOCUMENT_ISSUER))
    val documentIssuerUiState: StateFlow<DocumentClientOrIssuerState> = _documentIssuerUiState


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

    override fun onCleared() {
        super.onCleared()
        GlobalScope.launch {
            updateClientOrIssuerInLocalDb(ClientOrIssuerType.DOCUMENT_CLIENT)
            updateClientOrIssuerInLocalDb(ClientOrIssuerType.DOCUMENT_ISSUER)
        }
    }

    fun autoSaveIssuerFormInputsInLocalDb() {
        autoSaveIssuerJob?.cancel()
        autoSaveIssuerJob = viewModelScope.launch {
            @OptIn(FlowPreview::class)
            _documentIssuerUiState.debounce(300)
                .collect { updateClientOrIssuerInLocalDb(ClientOrIssuerType.DOCUMENT_ISSUER) }
        }
    }

    fun autoSaveClientFormInputsInLocalDb() {
        autoSaveClientJob?.cancel()
        autoSaveClientJob = viewModelScope.launch {
            @OptIn(FlowPreview::class)
            _documentClientUiState.debounce(300)
                .collect { updateClientOrIssuerInLocalDb(ClientOrIssuerType.DOCUMENT_CLIENT) }
        }
    }

    fun stopAutoSaveIssuerFormInputsInLocalDb() {
        autoSaveIssuerJob?.cancel()
    }

    fun stopAutoSaveClientFormInputsInLocalDb() {
        autoSaveClientJob?.cancel()
    }


    // Used when sliding the bottom form from documents
    // Editing a document client or issuer
    fun setDocumentClientOrIssuerUiState(documentClientOrIssuer: DocumentClientOrIssuerState) {
        if (documentClientOrIssuer.type == ClientOrIssuerType.DOCUMENT_CLIENT)
            _documentClientUiState.value = documentClientOrIssuer
        else {
            _documentIssuerUiState.value = documentClientOrIssuer
        }
    }

    // Used when sliding the bottom form from documents
    // Choosing a client or issuer
    fun setDocumentClientOrIssuerUiStateWithSelected(clientOrIssuer: ClientOrIssuerState) {
        if (clientOrIssuer.type == ClientOrIssuerType.CLIENT) {
            _documentClientUiState.value = DocumentClientOrIssuerState(
                id = null,
                name = clientOrIssuer.name,
                type = if (clientOrIssuer.type == ClientOrIssuerType.ISSUER) ClientOrIssuerType.DOCUMENT_ISSUER
                else ClientOrIssuerType.DOCUMENT_CLIENT,
                firstName = clientOrIssuer.firstName,
                address1 = clientOrIssuer.address1,
                address2 = clientOrIssuer.address2,
                zipCode = clientOrIssuer.zipCode,
                city = clientOrIssuer.city,
                phone = clientOrIssuer.phone,
                email = clientOrIssuer.email,
                notes = clientOrIssuer.notes,
                companyId1Label = clientOrIssuer.companyId1Label,
                companyId1Number = clientOrIssuer.companyId1Number,
                companyId2Label = clientOrIssuer.companyId2Label,
                companyId2Number = clientOrIssuer.companyId2Number
            )
        } else _documentIssuerUiState.value = DocumentClientOrIssuerState(
            id = null,
            name = clientOrIssuer.name,
            type = clientOrIssuer.type,
            firstName = clientOrIssuer.firstName,
            address1 = clientOrIssuer.address1,
            address2 = clientOrIssuer.address2,
            zipCode = clientOrIssuer.zipCode,
            city = clientOrIssuer.city,
            phone = clientOrIssuer.phone,
            email = clientOrIssuer.email,
            notes = clientOrIssuer.notes,
            companyId1Label = clientOrIssuer.companyId1Label,
            companyId1Number = clientOrIssuer.companyId1Number,
            companyId2Label = clientOrIssuer.companyId2Label,
            companyId2Number = clientOrIssuer.companyId2Number
        )
    }

    fun setClientOrIssuerUiState(type: ClientOrIssuerType) {
        if (type == ClientOrIssuerType.CLIENT) {
            _clientUiState.value = ClientOrIssuerState(
                id = null,
                type = type,
                firstName = _documentClientUiState.value.firstName,
                name = _documentClientUiState.value.name,
                address1 = _documentClientUiState.value.address1,
                address2 = _documentClientUiState.value.address2,
                zipCode = _documentClientUiState.value.zipCode,
                city = _documentClientUiState.value.city,
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
            address1 = _documentIssuerUiState.value.address1,
            address2 = _documentIssuerUiState.value.address2,
            zipCode = _documentIssuerUiState.value.zipCode,
            city = _documentIssuerUiState.value.city,
            phone = _documentIssuerUiState.value.phone,
            email = _documentIssuerUiState.value.email,
            notes = _documentIssuerUiState.value.notes,
            companyId1Label = _documentIssuerUiState.value.companyId1Label,
            companyId1Number = _documentIssuerUiState.value.companyId1Number,
            companyId2Label = _documentIssuerUiState.value.companyId2Label,
            companyId2Number = _documentIssuerUiState.value.companyId2Number
        )
    }

    fun clearClientUiState() {
        _clientUiState.value = ClientOrIssuerState()
        _documentClientUiState.value = DocumentClientOrIssuerState()
    }

    fun clearIssuerUiState() {
        _clientUiState.value = ClientOrIssuerState()
        _documentIssuerUiState.value = DocumentClientOrIssuerState()
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
            notes = TextFieldValue(clientOrIssuer?.notes?.text ?: ""),
            companyId1Label = TextFieldValue(clientOrIssuer?.companyId1Label?.text ?: ""),
            companyId1Number = TextFieldValue(clientOrIssuer?.companyId1Number?.text ?: ""),
            companyId2Label = TextFieldValue(clientOrIssuer?.companyId2Label?.text ?: ""),
            companyId2Number = TextFieldValue(clientOrIssuer?.companyId2Number?.text ?: ""),
        )
    }

    fun saveClientOrIssuerInLocalDb(type: ClientOrIssuerType) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            try {
                dataSource.saveClientOrIssuer(
                    if (type == ClientOrIssuerType.CLIENT) _clientUiState.value
                    else _issuerUiState.value
                )
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
            ScreenElement.CLIENT_OR_ISSUER_ADDRESS1 ->
                if (type == ClientOrIssuerType.CLIENT) _clientUiState.value.address1?.text
            else _issuerUiState.value.address1?.text
            ScreenElement.CLIENT_OR_ISSUER_ADDRESS2 ->
                if (type == ClientOrIssuerType.CLIENT) _clientUiState.value.address2?.text
            else _issuerUiState.value.address2?.text
            ScreenElement.CLIENT_OR_ISSUER_ZIP ->
                if (type == ClientOrIssuerType.CLIENT) _clientUiState.value.zipCode?.text
                else _issuerUiState.value.zipCode?.text
            ScreenElement.CLIENT_OR_ISSUER_CITY ->
                if (type == ClientOrIssuerType.CLIENT) _clientUiState.value.city?.text
                else _issuerUiState.value.city?.text
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
        val text = when (pageElement) {
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_NAME ->
                if (type == ClientOrIssuerType.DOCUMENT_CLIENT) _documentClientUiState.value.name.text
                else _documentIssuerUiState.value.name.text
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_FIRST_NAME ->
                if (type == ClientOrIssuerType.DOCUMENT_CLIENT) _documentClientUiState.value.firstName?.text
                else _documentIssuerUiState.value.firstName?.text
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_EMAIL ->
                if (type == ClientOrIssuerType.DOCUMENT_CLIENT) _documentClientUiState.value.email?.text
                else _documentIssuerUiState.value.email?.text
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS1 ->
                if (type == ClientOrIssuerType.DOCUMENT_CLIENT) _documentClientUiState.value.address1?.text
                else _documentIssuerUiState.value.address1?.text
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS2 ->
                if (type == ClientOrIssuerType.DOCUMENT_CLIENT) _documentClientUiState.value.address2?.text
                else _documentIssuerUiState.value.address2?.text
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ZIP ->
                if (type == ClientOrIssuerType.DOCUMENT_CLIENT) _documentClientUiState.value.zipCode?.text
                else _documentIssuerUiState.value.zipCode?.text
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_CITY ->
                if (type == ClientOrIssuerType.DOCUMENT_CLIENT) _documentClientUiState.value.city?.text
                else _documentIssuerUiState.value.city?.text
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
        when (element) {
            ScreenElement.CLIENT_OR_ISSUER_NAME -> person =
                person.copy(name = value as TextFieldValue)

            ScreenElement.CLIENT_OR_ISSUER_FIRST_NAME -> person =
                person.copy(firstName = value as TextFieldValue)

            ScreenElement.CLIENT_OR_ISSUER_EMAIL -> person =
                person.copy(email = value as TextFieldValue)

            ScreenElement.CLIENT_OR_ISSUER_ADDRESS1 -> person =
                person.copy(address1 = value as TextFieldValue)

            ScreenElement.CLIENT_OR_ISSUER_ADDRESS2 -> person =
                person.copy(address2 = value as TextFieldValue)

            ScreenElement.CLIENT_OR_ISSUER_ZIP -> person =
                person.copy(zipCode = value as TextFieldValue)

            ScreenElement.CLIENT_OR_ISSUER_CITY -> person =
                person.copy(city = value as TextFieldValue)

            ScreenElement.CLIENT_OR_ISSUER_PHONE -> person =
                person.copy(phone = value as TextFieldValue)

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

    private fun updateDocumentClientOrIssuerUiState(
        documentClientOrIssuer: DocumentClientOrIssuerState,
        element: ScreenElement,
        value: Any,
    ): DocumentClientOrIssuerState {
        var person = documentClientOrIssuer
        when (element) {
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_NAME -> person =
                person.copy(name = value as TextFieldValue)

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_FIRST_NAME -> person =
                person.copy(firstName = value as TextFieldValue)

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_EMAIL -> person =
                person.copy(email = value as TextFieldValue)

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS1 -> person =
                person.copy(address1 = value as TextFieldValue)

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS2 -> person =
                person.copy(address2 = value as TextFieldValue)

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ZIP -> person =
                person.copy(zipCode = value as TextFieldValue)

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_CITY -> person =
                person.copy(city = value as TextFieldValue)

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_PHONE -> person =
                person.copy(phone = value as TextFieldValue)

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
                    listOfErrors.add(Pair(ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_NAME, it))
                }
                FormInputsValidator.validateEmail(_documentIssuerUiState.value.email?.text)?.let {
                    listOfErrors.add(Pair(ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_EMAIL, it))
                }
                _documentIssuerUiState.value = _documentIssuerUiState.value.copy(
                    errors = listOfErrors
                )
            }
        }
        return listOfErrors.isEmpty()
    }
}

enum class ClientOrIssuerType {
    CLIENT, ISSUER, DOCUMENT_CLIENT, DOCUMENT_ISSUER
}
