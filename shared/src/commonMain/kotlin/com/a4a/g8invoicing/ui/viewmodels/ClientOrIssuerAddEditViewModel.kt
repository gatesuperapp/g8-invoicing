package com.a4a.g8invoicing.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSourceInterface
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.models.CountryCodes
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
    val issuerUiState: State<ClientOrIssuerState> = _issuerUiState

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

    // Country the write handlers seed on any new AddressState. Cascade
    // "last-used address country → device locale → FR" — see
    // CountryCodes.pickDefaultForNewAddress.
    private var defaultCountryCode: String = CountryCodes.pickDefaultForNewAddress(null)

    init {
        if (type == ClientOrIssuerType.CLIENT.name.lowercase()) {
            itemId?.let { fetchFromLocalDb(it.toLong(), ClientOrIssuerType.CLIENT) }
        } else if (type == ClientOrIssuerType.ISSUER.name.lowercase()) {
            itemId?.let { fetchFromLocalDb(it.toLong(), ClientOrIssuerType.ISSUER) }
        }
        viewModelScope.launch {
            defaultCountryCode =
                CountryCodes.pickDefaultForNewAddress(dataSource.getLastCountryCode())
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
                originalClientOrIssuerId = clientOrIssuer.id,
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
            originalClientOrIssuerId = clientOrIssuer.id,
            name = clientOrIssuer.name,
            type = if (clientOrIssuer.type == ClientOrIssuerType.ISSUER) ClientOrIssuerType.DOCUMENT_ISSUER
            else ClientOrIssuerType.DOCUMENT_CLIENT,
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
            logoPath = clientOrIssuer.logoPath,
            vatExempt = clientOrIssuer.vatExempt,
            intraEuSales = clientOrIssuer.intraEuSales,
            taxWithholdingEnabled = clientOrIssuer.taxWithholdingEnabled,
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
                companyId3Number = _documentClientUiState.value.companyId3Number,
                // Carry the auto-classification (INDIVIDUAL when a firstName
                // was typed / PROFESSIONAL when a SIREN was) from the doc-side
                // draft over to the master master row about to be inserted.
                // Without this the master lands with clientType=null even
                // though the form's radio visibly landed on Particulier /
                // Professionnel while the user was editing.
                clientType = _documentClientUiState.value.clientType,
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
                companyId3Number = _documentIssuerUiState.value.companyId3Number,
                logoPath = _documentIssuerUiState.value.logoPath,
                vatExempt = _documentIssuerUiState.value.vatExempt,
                intraEuSales = _documentIssuerUiState.value.intraEuSales,
                // Carry over the banks the user typed in the doc-embedded issuer
                // form — createNewAndReturnId reads them from this state to seed
                // the IssuerBank table for the new master. Without this the doc
                // gets a frozen payment_iban seeded from the first bank but the
                // master has zero linked accounts, so the payment-means picker
                // dropdown shows the empty-state hint on the next open.
                banks = _documentIssuerUiState.value.banks,
                taxWithholdingEnabled = _documentIssuerUiState.value.taxWithholdingEnabled,
            )
        }
    }

    fun clearClientOrIssuerUiState(type: ClientOrIssuerType) {
        // Explicit .type on the reset — a bare ClientOrIssuerState() defaults
        // .type to null, and the auto-classification bumps (typing a first
        // name flips clientType to INDIVIDUAL, typing a SIREN flips it to
        // PROFESSIONAL) key off .type == CLIENT / DOCUMENT_CLIENT. A null
        // .type silently disables both.
        if (type == ClientOrIssuerType.DOCUMENT_CLIENT) {
            _clientUiState.value = ClientOrIssuerState(type = ClientOrIssuerType.CLIENT)
            _documentClientUiState.value = ClientOrIssuerState(type = ClientOrIssuerType.DOCUMENT_CLIENT)
        } else {
            _issuerUiState.value = ClientOrIssuerState(type = ClientOrIssuerType.ISSUER)
            _documentIssuerUiState.value = ClientOrIssuerState(type = ClientOrIssuerType.DOCUMENT_ISSUER)
        }
    }

    private fun fetchFromLocalDb(id: Long, target: ClientOrIssuerType = ClientOrIssuerType.CLIENT) {
        viewModelScope.launch {
            try {
                val clientOrIssuer: ClientOrIssuerState? = dataSource.fetchClientOrIssuer(id)
                clientOrIssuer?.let {
                    when (target) {
                        ClientOrIssuerType.ISSUER -> _issuerUiState.value = it
                        else -> _clientUiState.value = it
                    }
                }
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    suspend fun createNew(type: ClientOrIssuerType): Long? {
        val stateToSave = when (type) {
            ClientOrIssuerType.CLIENT -> _clientUiState.value
            ClientOrIssuerType.ISSUER -> _issuerUiState.value
            ClientOrIssuerType.DOCUMENT_CLIENT -> _documentClientUiState.value
            ClientOrIssuerType.DOCUMENT_ISSUER -> _documentIssuerUiState.value
        }

        return try {
            dataSource.createNewAndReturnId(stateToSave)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateClientOrIssuerInLocalDb(
        type: ClientOrIssuerType,
        documentClientOrIssuer: ClientOrIssuerState? = null,
        syncToMaster: Boolean = true,
    ): Boolean {
        return try {
            when (type) {
                ClientOrIssuerType.CLIENT -> {
                    if (clientUiState.value.id == null) {
                        return false
                    }
                    dataSource.updateClientOrIssuer(clientUiState.value)
                }

                ClientOrIssuerType.ISSUER -> {
                    if (issuerUiState.value.id == null) {
                        return false
                    }
                    dataSource.updateClientOrIssuer(issuerUiState.value)
                }

                ClientOrIssuerType.DOCUMENT_CLIENT -> {
                    if (documentClientOrIssuer == null || documentClientOrIssuer.id == null) {
                        return false
                    }
                    // updateDocumentClientOrIssuer handles sync to master table based on syncToMaster param
                    dataSource.updateDocumentClientOrIssuer(documentClientOrIssuer, syncToMaster)
                }

                ClientOrIssuerType.DOCUMENT_ISSUER -> {
                    if (documentClientOrIssuer == null || documentClientOrIssuer.id == null) {
                        return false
                    }
                    dataSource.updateDocumentClientOrIssuer(documentClientOrIssuer, syncToMaster)
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
        // Real-time inline check on the company-id "label empty + value
        // filled" rule: re-run whenever a company_id label OR value
        // sub-field changes, so the red-under-row error appears as soon as
        // the user clears a label (matches the email-on-focus-loss UX).
        if (isCompanyIdSubField(pageElement)) {
            refreshCompanyIdLabelErrors(type)
        }
    }

    private fun isCompanyIdSubField(element: ScreenElement): Boolean = element in setOf(
        ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION1_LABEL,
        ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION1_VALUE,
        ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION2_LABEL,
        ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION2_VALUE,
        ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION3_LABEL,
        ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION3_VALUE,
        ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION1_LABEL,
        ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION1_VALUE,
        ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION2_LABEL,
        ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION2_VALUE,
        ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION3_LABEL,
        ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION3_VALUE,
    )

    /**
     * Strip any existing company-id label errors from the state's errors list
     * and re-add fresh ones based on the current slot values. Called on every
     * keystroke in a company_id sub-field so inline red-under-row appears /
     * disappears in real time. The full save-time [validateInputs] still runs
     * the same check plus everything else.
     */
    private fun refreshCompanyIdLabelErrors(type: ClientOrIssuerType) {
        val isDocument = type == ClientOrIssuerType.DOCUMENT_CLIENT ||
            type == ClientOrIssuerType.DOCUMENT_ISSUER
        val idElements = if (isDocument) setOf(
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION1,
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION2,
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION3,
        ) else setOf(
            ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION1,
            ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION2,
            ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION3,
        )
        val state = when (type) {
            ClientOrIssuerType.CLIENT -> _clientUiState.value
            ClientOrIssuerType.ISSUER -> _issuerUiState.value
            ClientOrIssuerType.DOCUMENT_CLIENT -> _documentClientUiState.value
            ClientOrIssuerType.DOCUMENT_ISSUER -> _documentIssuerUiState.value
        }
        val refreshed: MutableList<Pair<ScreenElement, String?>> =
            state.errors.filterNot { it.first in idElements }.toMutableList()
        validateCompanyIdLabels(state, refreshed, isDocument)
        when (type) {
            ClientOrIssuerType.CLIENT -> _clientUiState.value = state.copy(errors = refreshed)
            ClientOrIssuerType.ISSUER -> _issuerUiState.value = state.copy(errors = refreshed)
            ClientOrIssuerType.DOCUMENT_CLIENT -> _documentClientUiState.value = state.copy(errors = refreshed)
            ClientOrIssuerType.DOCUMENT_ISSUER -> _documentIssuerUiState.value = state.copy(errors = refreshed)
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

            ClientOrIssuerType.ISSUER -> {
                val currentEmails = _issuerUiState.value.emails ?: return
                if (indexToRemove >= currentEmails.size) return
                val newEmails = currentEmails.filterIndexed { index, _ -> index != indexToRemove }
                _issuerUiState.value = _issuerUiState.value.copy(
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

            ClientOrIssuerType.ISSUER -> {
                val currentEmails = _issuerUiState.value.emails ?: emptyList()
                if (currentEmails.size >= 4) return
                _issuerUiState.value = _issuerUiState.value.copy(
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
            ScreenElement.CLIENT_OR_ISSUER_FIRST_NAME -> {
                val newFirstName = value as TextFieldValue
                // Symmetric to the SIREN → PROFESSIONAL bump below: typing a
                // first name on an unclassified client (clientType == null)
                // flips the rail to Particulier so the user sees where they
                // stand. Only touches an unset type — an explicit choice
                // (Particulier or Professionnel) wins on subsequent edits.
                val autoTypeBump = if (
                    person.type == ClientOrIssuerType.CLIENT &&
                    person.clientType == null &&
                    newFirstName.text.isNotBlank()
                ) {
                    com.a4a.g8invoicing.data.models.ClientType.INDIVIDUAL
                } else {
                    person.clientType
                }
                person = person.copy(firstName = newFirstName, clientType = autoTypeBump)
            }
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
                    ?: AddressState(addressTitle = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 0))
            }
            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_1_1 -> {
                val newAddress = firstAddress?.copy(addressLine1 = value as TextFieldValue)
                    ?: AddressState(addressLine1 = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 0))
            }
            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_2_1 -> {
                val newAddress = firstAddress?.copy(addressLine2 = value as TextFieldValue)
                    ?: AddressState(addressLine2 = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 0))
            }
            ScreenElement.CLIENT_OR_ISSUER_ZIP_1 -> {
                val newAddress = firstAddress?.copy(zipCode = value as TextFieldValue)
                    ?: AddressState(zipCode = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 0))
            }
            ScreenElement.CLIENT_OR_ISSUER_CITY_1 -> {
                val newAddress = firstAddress?.copy(city = value as TextFieldValue)
                    ?: AddressState(city = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 0))
            }
            ScreenElement.CLIENT_OR_ISSUER_COUNTRY_1 -> {
                val newAddress = firstAddress?.copy(countryCode = (value as TextFieldValue).text)
                    ?: AddressState(countryCode = (value as TextFieldValue).text)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 0))
            }

            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_TITLE_2 -> {
                val newAddress = secondAddress?.copy(addressTitle = value as TextFieldValue)
                    ?: AddressState(addressTitle = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 1))
            }
            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_1_2 -> {
                val newAddress = secondAddress?.copy(addressLine1 = value as TextFieldValue)
                    ?: AddressState(addressLine1 = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 1))
            }
            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_2_2 -> {
                val newAddress = secondAddress?.copy(addressLine2 = value as TextFieldValue)
                    ?: AddressState(addressLine2 = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 1))
            }
            ScreenElement.CLIENT_OR_ISSUER_ZIP_2 -> {
                val newAddress = secondAddress?.copy(zipCode = value as TextFieldValue)
                    ?: AddressState(zipCode = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 1))
            }
            ScreenElement.CLIENT_OR_ISSUER_CITY_2 -> {
                val newAddress = secondAddress?.copy(city = value as TextFieldValue)
                    ?: AddressState(city = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 1))
            }
            ScreenElement.CLIENT_OR_ISSUER_COUNTRY_2 -> {
                val newAddress = secondAddress?.copy(countryCode = (value as TextFieldValue).text)
                    ?: AddressState(countryCode = (value as TextFieldValue).text)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 1))
            }

            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_TITLE_3 -> {
                val newAddress = thirdAddress?.copy(addressTitle = value as TextFieldValue)
                    ?: AddressState(addressTitle = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 2))
            }
            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_1_3 -> {
                val newAddress = thirdAddress?.copy(addressLine1 = value as TextFieldValue)
                    ?: AddressState(addressLine1 = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 2))
            }
            ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_2_3 -> {
                val newAddress = thirdAddress?.copy(addressLine2 = value as TextFieldValue)
                    ?: AddressState(addressLine2 = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 2))
            }
            ScreenElement.CLIENT_OR_ISSUER_ZIP_3 -> {
                val newAddress = thirdAddress?.copy(zipCode = value as TextFieldValue)
                    ?: AddressState(zipCode = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 2))
            }
            ScreenElement.CLIENT_OR_ISSUER_CITY_3 -> {
                val newAddress = thirdAddress?.copy(city = value as TextFieldValue)
                    ?: AddressState(city = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 2))
            }
            ScreenElement.CLIENT_OR_ISSUER_COUNTRY_3 -> {
                val newAddress = thirdAddress?.copy(countryCode = (value as TextFieldValue).text)
                    ?: AddressState(countryCode = (value as TextFieldValue).text)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 2))
            }

            ScreenElement.CLIENT_OR_ISSUER_NOTES -> person = person.copy(notes = value as TextFieldValue)
            ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION1_LABEL -> person = person.copy(companyId1Label = value as TextFieldValue)
            ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION1_VALUE -> {
                val newNumber = value as TextFieldValue
                // Typing anything into a client's SIREN slot (companyId1 is
                // SIREN in FR defaults, and even under other locale-specific
                // labels it's still a business tax id) auto-flags the client
                // as a professional the FIRST time — user override wins on
                // subsequent edits (we only touch clientType when it's null).
                val autoTypeBump = if (
                    person.type == ClientOrIssuerType.CLIENT &&
                    person.clientType == null &&
                    newNumber.text.isNotBlank()
                ) {
                    com.a4a.g8invoicing.data.models.ClientType.PROFESSIONAL
                } else {
                    person.clientType
                }
                person = person.copy(companyId1Number = newNumber, clientType = autoTypeBump)
            }
            ScreenElement.CLIENT_TYPE -> {
                // Wrapped payload — the picker fires ClientTypeChoice(null)
                // when the user re-taps the active chip to clear the choice.
                person = person.copy(
                    clientType = (value as com.a4a.g8invoicing.data.models.ClientTypeChoice).value
                )
            }
            ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION2_LABEL -> person = person.copy(companyId2Label = value as TextFieldValue)
            ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION2_VALUE -> person = person.copy(companyId2Number = value as TextFieldValue)
            ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION3_LABEL -> person = person.copy(companyId3Label = value as TextFieldValue)
            ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION3_VALUE -> person = person.copy(companyId3Number = value as TextFieldValue)

            ScreenElement.ISSUER_LOGO -> {
                val logoPath = (value as? String)?.takeIf { it.isNotEmpty() }
                person = person.copy(logoPath = logoPath)
            }

            ScreenElement.ISSUER_BANKS -> {
                @Suppress("UNCHECKED_CAST")
                person = person.copy(
                    banks = value as List<com.a4a.g8invoicing.ui.states.IssuerBankState>
                )
            }

            ScreenElement.ISSUER_VAT_EXEMPT -> {
                person = person.copy(vatExempt = value as Boolean)
            }

            ScreenElement.ISSUER_INTRA_EU_SALES -> {
                person = person.copy(intraEuSales = value as Boolean)
            }

            ScreenElement.ISSUER_TAX_WITHHOLDING -> {
                person = person.copy(taxWithholdingEnabled = value as Boolean)
            }

            else -> {}
        }
        return person
    }

    private fun getNewAddresses(
        newAddress: AddressState,
        addresses: List<AddressState>?,
        addressIndex: Int,
    ): List<AddressState> {
        // Bounds-safe rewrite of the old slice() version. The client form now
        // lets the user tap "+ Ajouter une adresse" without filling slot 1
        // first (the previousAddressIsFilled guard was dropped so the button
        // stays discoverable), so typing into slot 2 or 3 while the state
        // still holds a shorter list would blow slice() up with
        // IndexOutOfBounds. Pad the gap with empty AddressStates instead;
        // saveInfoInDocumentClientOrIssuerAddressTables / isAddressEmpty
        // strip them on save, so nothing blank ever reaches the DB.
        val existing = addresses ?: emptyList()
        val before = existing.take(addressIndex)
        val gap = List(maxOf(0, addressIndex - existing.size)) { AddressState() }
        val after = existing.drop(addressIndex + 1)
        return before + gap + newAddress + after
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
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_FIRST_NAME -> {
                val newFirstName = value as TextFieldValue
                // Same first-name → INDIVIDUAL auto-bump as the master client
                // form; only fires the first time (null → INDIVIDUAL).
                val autoTypeBump = if (
                    (person.type == ClientOrIssuerType.CLIENT ||
                        person.type == ClientOrIssuerType.DOCUMENT_CLIENT) &&
                    person.clientType == null &&
                    newFirstName.text.isNotBlank()
                ) {
                    com.a4a.g8invoicing.data.models.ClientType.INDIVIDUAL
                } else {
                    person.clientType
                }
                person = person.copy(firstName = newFirstName, clientType = autoTypeBump)
            }
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
                    ?: AddressState(addressTitle = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 0))
            }
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_1_1 -> {
                val newAddress = firstAddress?.copy(addressLine1 = value as TextFieldValue)
                    ?: AddressState(addressLine1 = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 0))
            }
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_2_1 -> {
                val newAddress = firstAddress?.copy(addressLine2 = value as TextFieldValue)
                    ?: AddressState(addressLine2 = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 0))
            }
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ZIP_1 -> {
                val newAddress = firstAddress?.copy(zipCode = value as TextFieldValue)
                    ?: AddressState(zipCode = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 0))
            }
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_CITY_1 -> {
                val newAddress = firstAddress?.copy(city = value as TextFieldValue)
                    ?: AddressState(city = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 0))
            }
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_COUNTRY_1 -> {
                val newAddress = firstAddress?.copy(countryCode = (value as TextFieldValue).text)
                    ?: AddressState(countryCode = (value as TextFieldValue).text)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 0))
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_TITLE_2 -> {
                val newAddress = secondAddress?.copy(addressTitle = value as TextFieldValue)
                    ?: AddressState(addressTitle = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 1))
            }
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_1_2 -> {
                val newAddress = secondAddress?.copy(addressLine1 = value as TextFieldValue)
                    ?: AddressState(addressLine1 = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 1))
            }
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_2_2 -> {
                val newAddress = secondAddress?.copy(addressLine2 = value as TextFieldValue)
                    ?: AddressState(addressLine2 = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 1))
            }
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ZIP_2 -> {
                val newAddress = secondAddress?.copy(zipCode = value as TextFieldValue)
                    ?: AddressState(zipCode = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 1))
            }
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_CITY_2 -> {
                val newAddress = secondAddress?.copy(city = value as TextFieldValue)
                    ?: AddressState(city = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 1))
            }
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_COUNTRY_2 -> {
                val newAddress = secondAddress?.copy(countryCode = (value as TextFieldValue).text)
                    ?: AddressState(countryCode = (value as TextFieldValue).text)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 1))
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_TITLE_3 -> {
                val newAddress = thirdAddress?.copy(addressTitle = value as TextFieldValue)
                    ?: AddressState(addressTitle = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 2))
            }
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_1_3 -> {
                val newAddress = thirdAddress?.copy(addressLine1 = value as TextFieldValue)
                    ?: AddressState(addressLine1 = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 2))
            }
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_2_3 -> {
                val newAddress = thirdAddress?.copy(addressLine2 = value as TextFieldValue)
                    ?: AddressState(addressLine2 = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 2))
            }
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ZIP_3 -> {
                val newAddress = thirdAddress?.copy(zipCode = value as TextFieldValue)
                    ?: AddressState(zipCode = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 2))
            }
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_CITY_3 -> {
                val newAddress = thirdAddress?.copy(city = value as TextFieldValue)
                    ?: AddressState(city = value as TextFieldValue, countryCode = defaultCountryCode)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 2))
            }
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_COUNTRY_3 -> {
                val newAddress = thirdAddress?.copy(countryCode = (value as TextFieldValue).text)
                    ?: AddressState(countryCode = (value as TextFieldValue).text)
                person = person.copy(addresses = getNewAddresses(newAddress, person.addresses, 2))
            }

            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_NOTES -> person = person.copy(notes = value as TextFieldValue)
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION1_LABEL -> person = person.copy(companyId1Label = value as TextFieldValue)
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION1_VALUE -> {
                val newNumber = value as TextFieldValue
                // Same SIREN → PROFESSIONAL auto-bump as the master client
                // form; only fires the first time (null → PROFESSIONAL).
                val autoTypeBump = if (
                    (person.type == ClientOrIssuerType.CLIENT ||
                        person.type == ClientOrIssuerType.DOCUMENT_CLIENT) &&
                    person.clientType == null &&
                    newNumber.text.isNotBlank()
                ) {
                    com.a4a.g8invoicing.data.models.ClientType.PROFESSIONAL
                } else {
                    person.clientType
                }
                person = person.copy(companyId1Number = newNumber, clientType = autoTypeBump)
            }
            ScreenElement.CLIENT_TYPE -> {
                person = person.copy(
                    clientType = (value as com.a4a.g8invoicing.data.models.ClientTypeChoice).value
                )
            }
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION2_LABEL -> person = person.copy(companyId2Label = value as TextFieldValue)
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION2_VALUE -> person = person.copy(companyId2Number = value as TextFieldValue)
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION3_LABEL -> person = person.copy(companyId3Label = value as TextFieldValue)
            ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION3_VALUE -> person = person.copy(companyId3Number = value as TextFieldValue)

            ScreenElement.DOCUMENT_ISSUER_LOGO -> {
                val logoPath = (value as? String)?.takeIf { it.isNotEmpty() }
                person = person.copy(logoPath = logoPath)
            }

            ScreenElement.DOCUMENT_ISSUER_PAYMENT_IBAN -> {
                person = person.copy(paymentIban = value as TextFieldValue)
            }

            ScreenElement.DOCUMENT_ISSUER_PAYMENT_BIC -> {
                person = person.copy(paymentBic = value as TextFieldValue)
            }

            ScreenElement.ISSUER_BANKS -> {
                @Suppress("UNCHECKED_CAST")
                person = person.copy(
                    banks = value as List<com.a4a.g8invoicing.ui.states.IssuerBankState>
                )
            }

            ScreenElement.DOCUMENT_ISSUER_VAT_EXEMPT -> {
                person = person.copy(vatExempt = value as Boolean)
            }

            ScreenElement.DOCUMENT_ISSUER_INTRA_EU_SALES -> {
                person = person.copy(intraEuSales = value as Boolean)
            }

            ScreenElement.DOCUMENT_ISSUER_TAX_WITHHOLDING -> {
                person = person.copy(taxWithholdingEnabled = value as Boolean)
            }

            else -> {}
        }
        return person
    }

    fun validateInputs(type: ClientOrIssuerType): Boolean {
        val listOfErrors: MutableList<Pair<ScreenElement, String?>> = mutableListOf()

        // Un-committed pending-email input (typed but not yet added via
        // enter/focus-loss) fails validation with `_pendingEmailIsValid=false`.
        // The FormInputCreatorEmailList already shows its own inline red
        // message via a local state, but we ALSO mirror the error into the
        // state.errors list so the pre-save recap modal picks it up alongside
        // any other issue. Uses EMAIL_1 as a stand-in ScreenElement — the
        // modal only reads the message text, and EMAIL_1 always exists.
        if (!_pendingEmailIsValid) {
            val emailElement = when (type) {
                ClientOrIssuerType.CLIENT, ClientOrIssuerType.ISSUER ->
                    ScreenElement.CLIENT_OR_ISSUER_EMAIL_1
                ClientOrIssuerType.DOCUMENT_CLIENT, ClientOrIssuerType.DOCUMENT_ISSUER ->
                    ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_EMAIL_1
            }
            listOfErrors.add(Pair(emailElement, FormInputsValidator.VALIDATION_EMAIL_INVALID))
        }

        when (type) {
            ClientOrIssuerType.CLIENT -> {
                FormInputsValidator.validateName(_clientUiState.value.name.text)?.let {
                    listOfErrors.add(Pair(ScreenElement.CLIENT_OR_ISSUER_NAME, it))
                }
                validateEmails(_clientUiState.value.emails, listOfErrors, isDocument = false)
                validateCompanyIdLabels(_clientUiState.value, listOfErrors, isDocument = false)
                val trimmedEmails = trimEmails(_clientUiState.value.emails)
                _clientUiState.value = _clientUiState.value
                    .copy(emails = trimmedEmails, errors = listOfErrors)
                    .cleanFieldsForClientType()
            }

            ClientOrIssuerType.ISSUER -> {
                FormInputsValidator.validateName(_issuerUiState.value.name.text)?.let {
                    listOfErrors.add(Pair(ScreenElement.CLIENT_OR_ISSUER_NAME, it))
                }
                validateEmails(_issuerUiState.value.emails, listOfErrors, isDocument = false)
                validateCompanyIdLabels(_issuerUiState.value, listOfErrors, isDocument = false)
                val trimmedEmails = trimEmails(_issuerUiState.value.emails)
                _issuerUiState.value = _issuerUiState.value
                    .copy(emails = trimmedEmails, errors = listOfErrors)
                    .cleanFieldsForClientType()
            }

            ClientOrIssuerType.DOCUMENT_CLIENT -> {
                FormInputsValidator.validateName(_documentClientUiState.value.name.text)?.let {
                    listOfErrors.add(Pair(ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_NAME, it))
                }
                validateEmails(_documentClientUiState.value.emails, listOfErrors, isDocument = true)
                validateCompanyIdLabels(_documentClientUiState.value, listOfErrors, isDocument = true)
                val trimmedEmails = trimEmails(_documentClientUiState.value.emails)
                _documentClientUiState.value = _documentClientUiState.value
                    .copy(emails = trimmedEmails, errors = listOfErrors)
                    .cleanFieldsForClientType()
            }

            ClientOrIssuerType.DOCUMENT_ISSUER -> {
                FormInputsValidator.validateName(_documentIssuerUiState.value.name.text)?.let {
                    listOfErrors.add(Pair(ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_NAME, it))
                }
                validateEmails(_documentIssuerUiState.value.emails, listOfErrors, isDocument = true)
                validateCompanyIdLabels(_documentIssuerUiState.value, listOfErrors, isDocument = true)
                val trimmedEmails = trimEmails(_documentIssuerUiState.value.emails)
                _documentIssuerUiState.value = _documentIssuerUiState.value
                    .copy(emails = trimmedEmails, errors = listOfErrors)
                    .cleanFieldsForClientType()
            }
        }
        // Fail-save if either the committed-fields check produced errors OR
        // the pending-email flag is invalid (its error was already merged into
        // listOfErrors above, but we still need to block save).
        return _pendingEmailIsValid && listOfErrors.isEmpty()
    }

    /**
     * Wipe type-mismatched fields at save time: a PROFESSIONAL client can't
     * have a firstName (companies don't have one), an INDIVIDUAL can't have
     * SIREN / TVA / RCS slots (they're issued to legal entities only).
     *
     * Fires only inside [validateInputs] — while the user is still editing we
     * keep every field as-typed so someone who switches type by mistake, or
     * who wants to peek at the other-type fields, isn't punished with a data
     * wipe on each toggle. clientType == null (unclassified) stays untouched
     * so the auto-classification code path in [updateClientOrIssuerUiState] /
     * [updateDocumentClientOrIssuerUiState] retains the raw input to work
     * from. Issuers carry a null clientType too and fall through untouched.
     */
    private fun ClientOrIssuerState.cleanFieldsForClientType(): ClientOrIssuerState {
        return when (clientType) {
            com.a4a.g8invoicing.data.models.ClientType.PROFESSIONAL ->
                copy(firstName = null)
            com.a4a.g8invoicing.data.models.ClientType.INDIVIDUAL ->
                copy(
                    companyId1Label = null, companyId1Number = null,
                    companyId2Label = null, companyId2Number = null,
                    companyId3Label = null, companyId3Number = null,
                )
            null -> this
        }
    }

    /**
     * Fire an inline "libellé manquant" error under each company-id LABEL
     * slot where the matching VALUE slot is filled but the label itself is
     * empty. Both empty = unused slot, silent. See
     * [FormInputsValidator.validateCompanyIdLabelForFilledValue].
     */
    private fun validateCompanyIdLabels(
        state: ClientOrIssuerState,
        listOfErrors: MutableList<Pair<ScreenElement, String?>>,
        isDocument: Boolean,
    ) {
        // ScreenElement is the FormInput aggregate (…_IDENTIFICATION1, no
        // _LABEL suffix) — that's what FormUI matches on for its per-row
        // errorMessage lookup. The error text sits under the whole ident
        // row rather than pinpointing the label sub-field, which reads
        // clearly enough since the row visually groups label + number.
        val slots = listOf(
            Triple(
                state.companyId1Label?.text, state.companyId1Number?.text,
                if (isDocument) ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION1
                else ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION1,
            ),
            Triple(
                state.companyId2Label?.text, state.companyId2Number?.text,
                if (isDocument) ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION2
                else ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION2,
            ),
            Triple(
                state.companyId3Label?.text, state.companyId3Number?.text,
                if (isDocument) ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION3
                else ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION3,
            ),
        )
        slots.forEach { (label, value, element) ->
            FormInputsValidator.validateCompanyIdLabelForFilledValue(label, value)?.let { err ->
                listOfErrors.add(Pair(element, err))
            }
        }
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
                _documentClientUiState.value = ClientOrIssuerState(type = ClientOrIssuerType.DOCUMENT_CLIENT)
                _documentClientUiState.value.errors.clear()
            }
            ClientOrIssuerType.DOCUMENT_ISSUER -> _documentIssuerUiState.value.errors.clear()
        }
    }

    /**
     * Checks if the master version has changed since the document was created
     * (or since the user last acknowledged the drift via "Keep current").
     * Returns true if versions don't match (master was updated elsewhere).
     * For legacy documents without originalVersion, compares against master version > 1.
     */
    suspend fun checkVersionMismatch(documentClientOrIssuer: ClientOrIssuerState): Boolean {
        val masterId = documentClientOrIssuer.originalClientOrIssuerId?.toLong() ?: return false
        val masterVersion = dataSource.getMasterVersion(masterId) ?: return false
        val documentVersion = documentClientOrIssuer.originalVersion
        // If documentVersion is null (legacy document), check if master has been updated (version > 1)
        if (documentVersion == null) {
            return masterVersion > 1
        }
        return documentVersion != masterVersion
    }

    /**
     * "Keep current" flow: bump the doc snapshot's originalVersion to the
     * master's current version so the mismatch dialog stops re-firing on every
     * reopen. Data stays frozen (no field is refreshed) — we only record that
     * the user has *seen* this master version and chosen to skip it.
     *
     * Persists the change to DB, refreshes the VM's internal editing state so
     * a following EDIT_CLIENT/EDIT_ISSUER form save doesn't write the stale
     * originalVersion back to DB, and returns a fresh state copy so the caller
     * can push it into the invoice/quote/BL/avoir UiState (the check reads
     * originalVersion from that state on the next open).
     */
    suspend fun acknowledgeMasterVersion(
        documentClientOrIssuer: ClientOrIssuerState,
    ): ClientOrIssuerState? {
        val docId = documentClientOrIssuer.id?.toLong() ?: return null
        val masterId = documentClientOrIssuer.originalClientOrIssuerId?.toLong() ?: return null
        val newVersion = dataSource.acknowledgeDocumentClientOrIssuerVersion(docId, masterId)
            ?: return null
        val updated = documentClientOrIssuer.copy(originalVersion = newVersion)
        // Sync the internal editing state so onClickDoneForm's
        // updateClientOrIssuerInLocalDb picks up the acknowledged version.
        // The edit-block flow seeds this state to the pre-ack snapshot right
        // before the dialog fires — without this sync, closing the edit form
        // would silently overwrite the DB write we just did.
        when (documentClientOrIssuer.type) {
            ClientOrIssuerType.DOCUMENT_CLIENT -> {
                val current = _documentClientUiState.value
                if (current.id == documentClientOrIssuer.id) {
                    _documentClientUiState.value = current.copy(originalVersion = newVersion)
                }
            }
            ClientOrIssuerType.DOCUMENT_ISSUER -> {
                val current = _documentIssuerUiState.value
                if (current.id == documentClientOrIssuer.id) {
                    _documentIssuerUiState.value = current.copy(originalVersion = newVersion)
                }
            }
            else -> {}
        }
        return updated
    }

    /**
     * Checks if the document client/issuer has changes compared to the master.
     * Returns true if there are differences that need syncing.
     */
    suspend fun hasChangesFromMaster(documentClientOrIssuer: ClientOrIssuerState): Boolean {
        val masterId = documentClientOrIssuer.originalClientOrIssuerId?.toLong() ?: return false
        val masterData = dataSource.fetchClientOrIssuer(masterId) ?: return false

        // Compare relevant fields
        if (documentClientOrIssuer.firstName?.text?.trim() != masterData.firstName?.text?.trim()) return true
        if (documentClientOrIssuer.name.text.trim() != masterData.name.text.trim()) return true
        if (documentClientOrIssuer.phone?.text?.trim() != masterData.phone?.text?.trim()) return true
        if (documentClientOrIssuer.notes?.text?.trim() != masterData.notes?.text?.trim()) return true
        if (documentClientOrIssuer.companyId1Label?.text?.trim() != masterData.companyId1Label?.text?.trim()) return true
        if (documentClientOrIssuer.companyId1Number?.text?.trim() != masterData.companyId1Number?.text?.trim()) return true
        if (documentClientOrIssuer.companyId2Label?.text?.trim() != masterData.companyId2Label?.text?.trim()) return true
        if (documentClientOrIssuer.companyId2Number?.text?.trim() != masterData.companyId2Number?.text?.trim()) return true
        if (documentClientOrIssuer.companyId3Label?.text?.trim() != masterData.companyId3Label?.text?.trim()) return true
        if (documentClientOrIssuer.companyId3Number?.text?.trim() != masterData.companyId3Number?.text?.trim()) return true
        if (documentClientOrIssuer.logoPath != masterData.logoPath) return true

        // Compare emails
        val docEmails = documentClientOrIssuer.emails?.map { it.email.text.trim() } ?: emptyList()
        val masterEmails = masterData.emails?.map { it.email.text.trim() } ?: emptyList()
        if (docEmails != masterEmails) return true

        // Compare addresses (simplified comparison)
        val docAddresses = documentClientOrIssuer.addresses?.map { addr ->
            listOf(
                addr.addressTitle?.text?.trim(),
                addr.addressLine1?.text?.trim(),
                addr.addressLine2?.text?.trim(),
                addr.zipCode?.text?.trim(),
                addr.city?.text?.trim()
            )
        } ?: emptyList()
        val masterAddresses = masterData.addresses?.map { addr ->
            listOf(
                addr.addressTitle?.text?.trim(),
                addr.addressLine1?.text?.trim(),
                addr.addressLine2?.text?.trim(),
                addr.zipCode?.text?.trim(),
                addr.city?.text?.trim()
            )
        } ?: emptyList()
        if (docAddresses != masterAddresses) return true

        return false
    }

    /**
     * Fetches the latest master data and updates the document client/issuer state.
     */
    suspend fun loadLatestMasterVersion(type: ClientOrIssuerType): ClientOrIssuerState? {
        val currentState = when (type) {
            ClientOrIssuerType.DOCUMENT_CLIENT -> _documentClientUiState.value
            ClientOrIssuerType.DOCUMENT_ISSUER -> _documentIssuerUiState.value
            else -> return null
        }

        val masterId = currentState.originalClientOrIssuerId?.toLong() ?: return null
        val masterData = dataSource.fetchClientOrIssuer(masterId) ?: return null

        // Reset IDs for addresses and emails so they will be created as new document entries
        // (Master IDs are from ClientOrIssuerAddress/Email, not DocumentClientOrIssuerAddress/Email)
        val addressesWithNullIds = masterData.addresses?.map { it.copy(id = null) }
        val emailsWithNullIds = masterData.emails?.map { it.copy(id = null) }

        // Create updated state keeping document-specific fields but with master data
        val updatedState = currentState.copy(
            firstName = masterData.firstName,
            name = masterData.name,
            phone = masterData.phone,
            emails = emailsWithNullIds,
            addresses = addressesWithNullIds,
            notes = masterData.notes,
            companyId1Label = masterData.companyId1Label,
            companyId1Number = masterData.companyId1Number,
            companyId2Label = masterData.companyId2Label,
            companyId2Number = masterData.companyId2Number,
            companyId3Label = masterData.companyId3Label,
            companyId3Number = masterData.companyId3Number,
            logoPath = masterData.logoPath,
            vatExempt = masterData.vatExempt,
            intraEuSales = masterData.intraEuSales,
            taxWithholdingEnabled = masterData.taxWithholdingEnabled,
            originalVersion = masterData.version // Update to current master version
        )

        // Update the appropriate state
        when (type) {
            ClientOrIssuerType.DOCUMENT_CLIENT -> _documentClientUiState.value = updatedState
            ClientOrIssuerType.DOCUMENT_ISSUER -> _documentIssuerUiState.value = updatedState
            else -> {}
        }

        return updatedState
    }
}
