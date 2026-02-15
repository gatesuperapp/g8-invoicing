package com.a4a.g8invoicing.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSourceInterface
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.ui.shared.FormInputsValidator
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.AddressState
import com.a4a.g8invoicing.ui.states.EmailState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ClientOrIssuerAddEditViewModel(
    private val dataSource: ClientOrIssuerLocalDataSourceInterface,
    private val itemId: String?,
    private val type: String?,
) : ViewModel() {

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

    // Flag to track if pending email validation failed
    private var _pendingEmailIsValid = true

    fun setPendingEmailValidationResult(isValid: Boolean) {
        _pendingEmailIsValid = isValid
    }


    init {
        if (type == ClientOrIssuerType.CLIENT.name.lowercase()) {
            itemId?.let {
                fetchFromLocalDb(it.toLong())
            }
        }
    }

    fun setDocumentClientOrIssuerUiState(documentClientOrIssuer: ClientOrIssuerState) {
        if (documentClientOrIssuer.type == ClientOrIssuerType.DOCUMENT_CLIENT) {
            _documentClientUiState.value = documentClientOrIssuer
            _documentClientUiState.value.errors = mutableListOf()
        } else {
            _documentIssuerUiState.value = documentClientOrIssuer
        }
    }

    fun setDocumentClientOrIssuerUiStateWithSelected(clientOrIssuer: ClientOrIssuerState) {
        if (clientOrIssuer.type == ClientOrIssuerType.CLIENT) {
            _documentClientUiState.value = ClientOrIssuerState(
                id = null,
                name = clientOrIssuer.name,
                phone = clientOrIssuer.phone,
                emails = clientOrIssuer.emails,
                type = if (clientOrIssuer.type == ClientOrIssuerType.ISSUER) ClientOrIssuerType.DOCUMENT_ISSUER
                else ClientOrIssuerType.DOCUMENT_CLIENT,
                firstName = clientOrIssuer.firstName,
                addresses = clientOrIssuer.addresses,
                notes = clientOrIssuer.notes,
                companyId1Label = clientOrIssuer.companyId1Label,
                companyId1Number = clientOrIssuer.companyId1Number,
                companyId2Label = clientOrIssuer.companyId2Label,
                companyId2Number = clientOrIssuer.companyId2Number,
                companyId3Label = clientOrIssuer.companyId3Label,
                companyId3Number = clientOrIssuer.companyId3Number,
                errors = mutableListOf()
            )
        } else _documentIssuerUiState.value = ClientOrIssuerState(
            id = null,
            name = clientOrIssuer.name,
            type = clientOrIssuer.type,
            firstName = clientOrIssuer.firstName,
            addresses = clientOrIssuer.addresses,
            phone = clientOrIssuer.phone,
            emails = clientOrIssuer.emails,
            notes = clientOrIssuer.notes,
            companyId1Label = clientOrIssuer.companyId1Label,
            companyId1Number = clientOrIssuer.companyId1Number,
            companyId2Label = clientOrIssuer.companyId2Label,
            companyId2Number = clientOrIssuer.companyId2Number,
            companyId3Label = clientOrIssuer.companyId3Label,
            companyId3Number = clientOrIssuer.companyId3Number,
            errors = mutableListOf()
        )
    }

    fun setClientOrIssuerUiState(type: ClientOrIssuerType) {
        if (type == ClientOrIssuerType.CLIENT || type == ClientOrIssuerType.DOCUMENT_CLIENT) {
            _clientUiState.value = ClientOrIssuerState(
                id = null,
                type = ClientOrIssuerType.CLIENT,
                firstName = _documentClientUiState.value.firstName,
                name = _documentClientUiState.value.name,
                addresses = _documentClientUiState.value.addresses,
                phone = _documentClientUiState.value.phone,
                emails = _documentClientUiState.value.emails,
                notes = _documentClientUiState.value.notes,
                companyId1Label = _documentClientUiState.value.companyId1Label,
                companyId1Number = _documentClientUiState.value.companyId1Number,
                companyId2Label = _documentClientUiState.value.companyId2Label,
                companyId2Number = _documentClientUiState.value.companyId2Number,
                companyId3Label = _documentClientUiState.value.companyId3Label,
                companyId3Number = _documentClientUiState.value.companyId3Number
            )
        } else {
            _issuerUiState.value = ClientOrIssuerState(
                id = null,
                type = ClientOrIssuerType.ISSUER,
                firstName = _documentIssuerUiState.value.firstName,
                name = _documentIssuerUiState.value.name,
                addresses = _documentIssuerUiState.value.addresses,
                phone = _documentIssuerUiState.value.phone,
                emails = _documentIssuerUiState.value.emails,
                notes = _documentIssuerUiState.value.notes,
                companyId1Label = _documentIssuerUiState.value.companyId1Label,
                companyId1Number = _documentIssuerUiState.value.companyId1Number,
                companyId2Label = _documentIssuerUiState.value.companyId2Label,
                companyId2Number = _documentIssuerUiState.value.companyId2Number,
                companyId3Label = _documentIssuerUiState.value.companyId3Label,
                companyId3Number = _documentIssuerUiState.value.companyId3Number
            )
        }
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
        viewModelScope.launch {
            try {
                val clientOrIssuer: ClientOrIssuerState? = dataSource.fetchClientOrIssuer(id)
                clientOrIssuer?.let {
                    _clientUiState.value = it
                }
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    suspend fun createNew(type: ClientOrIssuerType): Boolean {
        val stateToSave = when (type) {
            ClientOrIssuerType.CLIENT -> _clientUiState.value
            ClientOrIssuerType.ISSUER -> _issuerUiState.value
            ClientOrIssuerType.DOCUMENT_CLIENT -> _documentClientUiState.value
            ClientOrIssuerType.DOCUMENT_ISSUER -> _documentIssuerUiState.value
        }.withTrimmedEmails()

        return try {
            dataSource.createNew(stateToSave)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun ClientOrIssuerState.withTrimmedEmails(): ClientOrIssuerState {
        val trimmedEmails = emails?.map { emailState ->
            emailState.copy(
                email = TextFieldValue(
                    emailState.email.text.trim(),
                    emailState.email.selection,
                    emailState.email.composition
                )
            )
        }
        return copy(emails = trimmedEmails)
    }

    suspend fun updateClientOrIssuerInLocalDb(
        type: ClientOrIssuerType,
        documentClientOrIssuer: ClientOrIssuerState? = null,
    ): Boolean {
        return try {
            when (type) {
                ClientOrIssuerType.CLIENT -> {
                    if (clientUiState.value.id == null) {
                        return false
                    }
                    dataSource.updateClientOrIssuer(clientUiState.value.withTrimmedEmails())
                }

                ClientOrIssuerType.ISSUER -> {
                    if (issuerUiState.value.id == null) {
                        return false
                    }
                    dataSource.updateClientOrIssuer(issuerUiState.value.withTrimmedEmails())
                }

                ClientOrIssuerType.DOCUMENT_CLIENT -> {
                    if (documentClientOrIssuer == null || documentClientOrIssuer.id == null) {
                        return false
                    }
                    dataSource.updateDocumentClientOrIssuer(documentClientOrIssuer.withTrimmedEmails())
                }

                ClientOrIssuerType.DOCUMENT_ISSUER -> {
                    if (documentClientOrIssuer == null || documentClientOrIssuer.id == null) {
                        return false
                    }
                    dataSource.updateDocumentClientOrIssuer(documentClientOrIssuer.withTrimmedEmails())
                }
            }
            true
        } catch (e: Exception) {
            false
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

    fun removeAddressFromClientOrIssuerState(type: ClientOrIssuerType) {
        when (type) {
            ClientOrIssuerType.CLIENT -> {
                var newAddresses: List<AddressState>? =
                    _clientUiState.value.addresses?.dropLast(1)
                if (newAddresses?.size == 1) {
                    val onlyAddress = _clientUiState.value.addresses!![0].copy(
                        addressTitle = null
                    )
                    newAddresses = listOf(onlyAddress)
                }
                _clientUiState.value = _clientUiState.value.copy(addresses = newAddresses)
            }

            ClientOrIssuerType.DOCUMENT_CLIENT -> {
                var newAddresses: List<AddressState>? =
                    _documentClientUiState.value.addresses?.dropLast(1)
                if (newAddresses?.size == 1) {
                    val onlyAddress = _documentClientUiState.value.addresses!![0].copy(
                        addressTitle = null
                    )
                    newAddresses = listOf(onlyAddress)
                }
                _documentClientUiState.value = _documentClientUiState.value.copy(addresses = newAddresses)
            }

            else -> {}
        }
    }

    fun removeEmailFromClientOrIssuerState(type: ClientOrIssuerType, indexToRemove: Int) {
        when (type) {
            ClientOrIssuerType.CLIENT -> {
                val currentEmails = _clientUiState.value.emails ?: return
                if (indexToRemove >= currentEmails.size) return
                val newEmails = currentEmails.filterIndexed { index, _ -> index != indexToRemove }
                _clientUiState.value = _clientUiState.value.copy(
                    emails = if (newEmails.isEmpty()) null else newEmails
                )
            }

            ClientOrIssuerType.DOCUMENT_CLIENT -> {
                val currentEmails = _documentClientUiState.value.emails ?: return
                if (indexToRemove >= currentEmails.size) return
                val newEmails = currentEmails.filterIndexed { index, _ -> index != indexToRemove }
                _documentClientUiState.value = _documentClientUiState.value.copy(
                    emails = if (newEmails.isEmpty()) null else newEmails
                )
            }

            ClientOrIssuerType.DOCUMENT_ISSUER -> {
                val currentEmails = _documentIssuerUiState.value.emails ?: return
                if (indexToRemove >= currentEmails.size) return
                val newEmails = currentEmails.filterIndexed { index, _ -> index != indexToRemove }
                _documentIssuerUiState.value = _documentIssuerUiState.value.copy(
                    emails = if (newEmails.isEmpty()) null else newEmails
                )
            }

            else -> {}
        }
    }

    fun addEmailToClientOrIssuerState(type: ClientOrIssuerType, email: String) {
        val newEmailState = EmailState(email = TextFieldValue(email))
        when (type) {
            ClientOrIssuerType.CLIENT -> {
                val currentEmails = _clientUiState.value.emails ?: emptyList()
                if (currentEmails.size >= 4) return
                _clientUiState.value = _clientUiState.value.copy(
                    emails = currentEmails + newEmailState
                )
            }

            ClientOrIssuerType.DOCUMENT_CLIENT -> {
                val currentEmails = _documentClientUiState.value.emails ?: emptyList()
                if (currentEmails.size >= 4) return
                _documentClientUiState.value = _documentClientUiState.value.copy(
                    emails = currentEmails + newEmailState
                )
            }

            ClientOrIssuerType.DOCUMENT_ISSUER -> {
                val currentEmails = _documentIssuerUiState.value.emails ?: emptyList()
                if (currentEmails.size >= 4) return
                _documentIssuerUiState.value = _documentIssuerUiState.value.copy(
                    emails = currentEmails + newEmailState
                )
            }

            else -> {}
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

            ScreenElement.CLIENT_OR_ISSUER_EMAIL_1 ->
                if (type == ClientOrIssuerType.CLIENT) _clientUiState.value.emails?.getOrNull(0)?.email?.text
                else _issuerUiState.value.emails?.getOrNull(0)?.email?.text
            ScreenElement.CLIENT_OR_ISSUER_EMAIL_2 ->
                if (type == ClientOrIssuerType.CLIENT) _clientUiState.value.emails?.getOrNull(1)?.email?.text
                else _issuerUiState.value.emails?.getOrNull(1)?.email?.text
            ScreenElement.CLIENT_OR_ISSUER_EMAIL_3 ->
                if (type == ClientOrIssuerType.CLIENT) _clientUiState.value.emails?.getOrNull(2)?.email?.text
                else _issuerUiState.value.emails?.getOrNull(2)?.email?.text
            ScreenElement.CLIENT_OR_ISSUER_EMAIL_4 ->
                if (type == ClientOrIssuerType.CLIENT) _clientUiState.value.emails?.getOrNull(3)?.email?.text
                else _issuerUiState.value.emails?.getOrNull(3)?.email?.text

            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_TITLE_1 -> firstAddress?.addressTitle?.text
            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_1_1 -> firstAddress?.addressLine1?.text
            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_2_1 -> firstAddress?.addressLine2?.text
            ScreenElement.CLIENT_OR_ISSUER_ZIP_1 -> firstAddress?.zipCode?.text
            ScreenElement.CLIENT_OR_ISSUER_CITY_1 -> firstAddress?.city?.text
            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_TITLE_2 -> secondAddress?.addressTitle?.text
            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_1_2 -> secondAddress?.addressLine1?.text
            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_2_2 -> secondAddress?.addressLine2?.text
            ScreenElement.CLIENT_OR_ISSUER_ZIP_2 -> secondAddress?.zipCode?.text
            ScreenElement.CLIENT_OR_ISSUER_CITY_2 -> secondAddress?.city?.text
            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_TITLE_3 -> thirdAddress?.addressTitle?.text
            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_1_3 -> thirdAddress?.addressLine1?.text
            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_2_3 -> thirdAddress?.addressLine2?.text
            ScreenElement.CLIENT_OR_ISSUER_ZIP_3 -> thirdAddress?.zipCode?.text
            ScreenElement.CLIENT_OR_ISSUER_CITY_3 -> thirdAddress?.city?.text

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

            ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION3_LABEL ->
                if (type == ClientOrIssuerType.CLIENT) _clientUiState.value.companyId3Label?.text
                else _issuerUiState.value.companyId3Label?.text

            ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION3_VALUE ->
                if (type == ClientOrIssuerType.CLIENT) _clientUiState.value.companyId3Number?.text
                else _issuerUiState.value.companyId3Number?.text

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
                if (type == ClientOrIssuerType.DOCUMENT_CLIENT)
                    _documentClientUiState.value.name.text
                else _documentIssuerUiState.value.name.text

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_FIRST_NAME ->
                if (type == ClientOrIssuerType.DOCUMENT_CLIENT) _documentClientUiState.value.firstName?.text
                else _documentIssuerUiState.value.firstName?.text

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_EMAIL_1 ->
                if (type == ClientOrIssuerType.DOCUMENT_CLIENT) _documentClientUiState.value.emails?.getOrNull(0)?.email?.text
                else _documentIssuerUiState.value.emails?.getOrNull(0)?.email?.text
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_EMAIL_2 ->
                if (type == ClientOrIssuerType.DOCUMENT_CLIENT) _documentClientUiState.value.emails?.getOrNull(1)?.email?.text
                else _documentIssuerUiState.value.emails?.getOrNull(1)?.email?.text
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_EMAIL_3 ->
                if (type == ClientOrIssuerType.DOCUMENT_CLIENT) _documentClientUiState.value.emails?.getOrNull(2)?.email?.text
                else _documentIssuerUiState.value.emails?.getOrNull(2)?.email?.text
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_EMAIL_4 ->
                if (type == ClientOrIssuerType.DOCUMENT_CLIENT) _documentClientUiState.value.emails?.getOrNull(3)?.email?.text
                else _documentIssuerUiState.value.emails?.getOrNull(3)?.email?.text

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_TITLE_1 -> firstAddress?.addressTitle?.text
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_1_1 -> firstAddress?.addressLine1?.text
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_2_1 -> firstAddress?.addressLine2?.text
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ZIP_1 -> firstAddress?.zipCode?.text
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_CITY_1 -> firstAddress?.city?.text
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_TITLE_2 -> secondAddress?.addressTitle?.text
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_1_2 -> secondAddress?.addressLine1?.text
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_2_2 -> secondAddress?.addressLine2?.text
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ZIP_2 -> secondAddress?.zipCode?.text
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_CITY_2 -> secondAddress?.city?.text
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_TITLE_3 -> thirdAddress?.addressTitle?.text
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_1_3 -> thirdAddress?.addressLine1?.text
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_2_3 -> thirdAddress?.addressLine2?.text
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ZIP_3 -> thirdAddress?.zipCode?.text
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_CITY_3 -> thirdAddress?.city?.text

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

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION3_LABEL ->
                if (type == ClientOrIssuerType.DOCUMENT_CLIENT) _documentClientUiState.value.companyId3Label?.text
                else _documentIssuerUiState.value.companyId3Label?.text

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION3_VALUE ->
                if (type == ClientOrIssuerType.DOCUMENT_CLIENT) _documentClientUiState.value.companyId3Number?.text
                else _documentIssuerUiState.value.companyId3Number?.text

            else -> null
        }
        return text
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

        val firstEmail = person.emails?.getOrNull(0)
        val secondEmail = person.emails?.getOrNull(1)
        val thirdEmail = person.emails?.getOrNull(2)
        val fourthEmail = person.emails?.getOrNull(3)

        when (element) {
            ScreenElement.CLIENT_OR_ISSUER_NAME -> person = person.copy(name = value as TextFieldValue)
            ScreenElement.CLIENT_OR_ISSUER_FIRST_NAME -> person = person.copy(firstName = value as TextFieldValue)
            ScreenElement.CLIENT_OR_ISSUER_PHONE -> person = person.copy(phone = value as TextFieldValue)

            ScreenElement.CLIENT_OR_ISSUER_EMAIL_1 -> {
                val newEmail = firstEmail?.copy(email = value as TextFieldValue)
                    ?: EmailState(email = value as TextFieldValue)
                person = person.copy(emails = getNewEmails(newEmail, person.emails, 0))
            }
            ScreenElement.CLIENT_OR_ISSUER_EMAIL_2 -> {
                val newEmail = secondEmail?.copy(email = value as TextFieldValue)
                    ?: EmailState(email = value as TextFieldValue)
                person = person.copy(emails = getNewEmails(newEmail, person.emails, 1))
            }
            ScreenElement.CLIENT_OR_ISSUER_EMAIL_3 -> {
                val newEmail = thirdEmail?.copy(email = value as TextFieldValue)
                    ?: EmailState(email = value as TextFieldValue)
                person = person.copy(emails = getNewEmails(newEmail, person.emails, 2))
            }
            ScreenElement.CLIENT_OR_ISSUER_EMAIL_4 -> {
                val newEmail = fourthEmail?.copy(email = value as TextFieldValue)
                    ?: EmailState(email = value as TextFieldValue)
                person = person.copy(emails = getNewEmails(newEmail, person.emails, 3))
            }

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

            ScreenElement.CLIENT_OR_ISSUER_NOTES -> person = person.copy(notes = value as TextFieldValue)
            ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION1_LABEL -> person = person.copy(companyId1Label = value as TextFieldValue)
            ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION1_VALUE -> person = person.copy(companyId1Number = value as TextFieldValue)
            ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION2_LABEL -> person = person.copy(companyId2Label = value as TextFieldValue)
            ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION2_VALUE -> person = person.copy(companyId2Number = value as TextFieldValue)
            ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION3_LABEL -> person = person.copy(companyId3Label = value as TextFieldValue)
            ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION3_VALUE -> person = person.copy(companyId3Number = value as TextFieldValue)

            else -> {}
        }
        return person
    }

    private fun getNewAddresses(
        newAddress: AddressState,
        addresses: List<AddressState>?,
        addressIndex: Int,
    ): List<AddressState> {
        return if (addresses.isNullOrEmpty()) listOf(newAddress)
        else addresses.slice(0 until addressIndex) + newAddress + addresses.slice(addressIndex + 1 until addresses.size)
    }

    private fun getNewEmails(
        newEmail: EmailState,
        emails: List<EmailState>?,
        emailIndex: Int,
    ): List<EmailState> {
        return if (emails.isNullOrEmpty()) listOf(newEmail)
        else emails.slice(0 until emailIndex) + newEmail + emails.slice(emailIndex + 1 until emails.size)
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
        val firstEmail = person.emails?.getOrNull(0)
        val secondEmail = person.emails?.getOrNull(1)
        val thirdEmail = person.emails?.getOrNull(2)
        val fourthEmail = person.emails?.getOrNull(3)

        when (element) {
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_NAME -> person = person.copy(name = value as TextFieldValue)
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_FIRST_NAME -> person = person.copy(firstName = value as TextFieldValue)
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_PHONE -> person = person.copy(phone = value as TextFieldValue)

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_EMAIL_1 -> {
                val newEmail = firstEmail?.copy(email = value as TextFieldValue)
                    ?: EmailState(email = value as TextFieldValue)
                person = person.copy(emails = getNewEmails(newEmail, person.emails, 0))
            }
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_EMAIL_2 -> {
                val newEmail = secondEmail?.copy(email = value as TextFieldValue)
                    ?: EmailState(email = value as TextFieldValue)
                person = person.copy(emails = getNewEmails(newEmail, person.emails, 1))
            }
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_EMAIL_3 -> {
                val newEmail = thirdEmail?.copy(email = value as TextFieldValue)
                    ?: EmailState(email = value as TextFieldValue)
                person = person.copy(emails = getNewEmails(newEmail, person.emails, 2))
            }
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_EMAIL_4 -> {
                val newEmail = fourthEmail?.copy(email = value as TextFieldValue)
                    ?: EmailState(email = value as TextFieldValue)
                person = person.copy(emails = getNewEmails(newEmail, person.emails, 3))
            }

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

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_NOTES -> person = person.copy(notes = value as TextFieldValue)
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION1_LABEL -> person = person.copy(companyId1Label = value as TextFieldValue)
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION1_VALUE -> person = person.copy(companyId1Number = value as TextFieldValue)
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION2_LABEL -> person = person.copy(companyId2Label = value as TextFieldValue)
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION2_VALUE -> person = person.copy(companyId2Number = value as TextFieldValue)
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION3_LABEL -> person = person.copy(companyId3Label = value as TextFieldValue)
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION3_VALUE -> person = person.copy(companyId3Number = value as TextFieldValue)

            else -> {}
        }
        return person
    }

    fun validateInputs(type: ClientOrIssuerType): Boolean {
        // Check if there's an invalid pending email
        if (!_pendingEmailIsValid) {
            return false
        }

        val listOfErrors: MutableList<Pair<ScreenElement, String?>> = mutableListOf()
        when (type) {
            ClientOrIssuerType.CLIENT -> {
                FormInputsValidator.validateName(_clientUiState.value.name.text)?.let {
                    listOfErrors.add(Pair(ScreenElement.CLIENT_OR_ISSUER_NAME, it))
                }
                validateEmails(_clientUiState.value.emails, listOfErrors, isDocument = false)
                val trimmedEmails = trimEmails(_clientUiState.value.emails)
                _clientUiState.value = _clientUiState.value.copy(emails = trimmedEmails, errors = listOfErrors)
            }

            ClientOrIssuerType.ISSUER -> {
                FormInputsValidator.validateName(_issuerUiState.value.name.text)?.let {
                    listOfErrors.add(Pair(ScreenElement.CLIENT_OR_ISSUER_NAME, it))
                }
                validateEmails(_issuerUiState.value.emails, listOfErrors, isDocument = false)
                val trimmedEmails = trimEmails(_issuerUiState.value.emails)
                _issuerUiState.value = _issuerUiState.value.copy(emails = trimmedEmails, errors = listOfErrors)
            }

            ClientOrIssuerType.DOCUMENT_CLIENT -> {
                FormInputsValidator.validateName(_documentClientUiState.value.name.text)?.let {
                    listOfErrors.add(Pair(ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_NAME, it))
                }
                validateEmails(_documentClientUiState.value.emails, listOfErrors, isDocument = true)
                val trimmedEmails = trimEmails(_documentClientUiState.value.emails)
                _documentClientUiState.value = _documentClientUiState.value.copy(emails = trimmedEmails, errors = listOfErrors)
            }

            ClientOrIssuerType.DOCUMENT_ISSUER -> {
                FormInputsValidator.validateName(_documentIssuerUiState.value.name.text)?.let {
                    listOfErrors.add(Pair(ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_NAME, it))
                }
                validateEmails(_documentIssuerUiState.value.emails, listOfErrors, isDocument = true)
                val trimmedEmails = trimEmails(_documentIssuerUiState.value.emails)
                _documentIssuerUiState.value = _documentIssuerUiState.value.copy(emails = trimmedEmails, errors = listOfErrors)
            }
        }
        return listOfErrors.isEmpty()
    }

    private fun validateEmails(
        emails: List<EmailState>?,
        listOfErrors: MutableList<Pair<ScreenElement, String?>>,
        isDocument: Boolean
    ) {
        emails?.forEachIndexed { index, emailState ->
            FormInputsValidator.validateEmail(emailState.email.text)?.let { error ->
                val element = if (isDocument) {
                    ScreenElement.valueOf("DOCUMENT_CLIENT_OR_ISSUER_EMAIL_${index + 1}")
                } else {
                    ScreenElement.valueOf("CLIENT_OR_ISSUER_EMAIL_${index + 1}")
                }
                listOfErrors.add(Pair(element, error))
            }
        }
    }

    private fun trimEmails(emails: List<EmailState>?): List<EmailState>? {
        return emails?.map { emailState ->
            val trimmedText = emailState.email.text.trim()
            emailState.copy(
                email = TextFieldValue(
                    text = trimmedText,
                    selection = TextRange(trimmedText.length)
                )
            )
        }
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
