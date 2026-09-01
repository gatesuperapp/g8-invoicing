package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSourceInterface
import com.a4a.g8invoicing.data.models.CountryCodes
import com.a4a.g8invoicing.ui.screens.shared.ClientTypePicker
import com.a4a.g8invoicing.ui.screens.shared.CountryPicker
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.koinInject
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.client_add_address
import com.a4a.g8invoicing.shared.resources.issuer_bank_add
import com.a4a.g8invoicing.shared.resources.issuer_bank_label
import com.a4a.g8invoicing.shared.resources.issuer_bank_label_input
import com.a4a.g8invoicing.shared.resources.client_address1
import com.a4a.g8invoicing.shared.resources.client_address1_input
import com.a4a.g8invoicing.shared.resources.client_address2_input
import com.a4a.g8invoicing.shared.resources.client_address_title
import com.a4a.g8invoicing.shared.resources.client_address_title_delivery_placeholder
import com.a4a.g8invoicing.shared.resources.client_address_title_head_office_placeholder
import com.a4a.g8invoicing.shared.resources.client_address_title_invoicing_placeholder
import com.a4a.g8invoicing.shared.resources.client_city
import com.a4a.g8invoicing.shared.resources.client_city_input
import com.a4a.g8invoicing.shared.resources.client_country
import com.a4a.g8invoicing.shared.resources.client_company_identification1_input
import com.a4a.g8invoicing.shared.resources.client_company_identification2_input
import com.a4a.g8invoicing.shared.resources.client_company_identification3_input
import com.a4a.g8invoicing.shared.resources.company_identification1
import com.a4a.g8invoicing.shared.resources.company_identification2
import com.a4a.g8invoicing.shared.resources.company_identification3
import com.a4a.g8invoicing.shared.resources.client_email
import com.a4a.g8invoicing.shared.resources.client_email_input
import com.a4a.g8invoicing.shared.resources.client_first_name
import com.a4a.g8invoicing.shared.resources.client_first_name_input
import com.a4a.g8invoicing.shared.resources.client_name
import com.a4a.g8invoicing.shared.resources.client_name_input
import com.a4a.g8invoicing.shared.resources.client_notes
import com.a4a.g8invoicing.shared.resources.client_notes_input
import com.a4a.g8invoicing.shared.resources.client_phone
import com.a4a.g8invoicing.shared.resources.client_phone_input
import com.a4a.g8invoicing.shared.resources.document_form_sync_to_master
import com.a4a.g8invoicing.shared.resources.issuer_logo_error_dismiss
import com.a4a.g8invoicing.shared.resources.issuer_logo_error_title
import com.a4a.g8invoicing.shared.resources.issuer_logo_label
import com.a4a.g8invoicing.shared.resources.issuer_logo_remove
import com.a4a.g8invoicing.shared.resources.issuer_logo_select
import com.a4a.g8invoicing.shared.resources.issuer_intra_eu_sales_info_desc
import com.a4a.g8invoicing.shared.resources.issuer_intra_eu_sales_info_modal_content
import com.a4a.g8invoicing.shared.resources.issuer_intra_eu_sales_info_modal_title
import com.a4a.g8invoicing.shared.resources.issuer_intra_eu_sales_label
import com.a4a.g8invoicing.shared.resources.issuer_tax_withholding_info_desc
import com.a4a.g8invoicing.shared.resources.issuer_tax_withholding_info_modal_content
import com.a4a.g8invoicing.shared.resources.issuer_tax_withholding_label
import com.a4a.g8invoicing.shared.resources.issuer_vat_exempt_label
import com.a4a.g8invoicing.shared.resources.client_zip_code
import com.a4a.g8invoicing.shared.resources.client_zip_code_input
import com.a4a.g8invoicing.ui.screens.shared.DocumentBottomSheetTypeOfForm
import com.a4a.g8invoicing.ui.shared.DeleteBlockRow
import com.a4a.g8invoicing.ui.shared.EmailListInput
import com.a4a.g8invoicing.ui.shared.ForwardElement
import com.a4a.g8invoicing.ui.shared.FormInput
import com.a4a.g8invoicing.ui.shared.FormUI
import com.a4a.g8invoicing.ui.shared.INFO_GLYPH_I
import com.a4a.g8invoicing.ui.shared.InfoTooltipButton
import com.a4a.g8invoicing.ui.shared.LabelInfoTooltip
import com.a4a.g8invoicing.shared.resources.issuer_bank_country
import com.a4a.g8invoicing.shared.resources.issuer_bank_identifier_generic
import com.a4a.g8invoicing.shared.resources.issuer_bank_identifier_iban
import com.a4a.g8invoicing.ui.shared.dismissKeyboardOnUnconsumedTap
import com.a4a.g8invoicing.ui.shared.LogoPickerComponent
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.TextInput
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.ColorVioletLink
import com.a4a.g8invoicing.ui.theme.textBodyBold
import com.a4a.g8invoicing.ui.theme.textBodySmall
import com.a4a.g8invoicing.ui.theme.textSection
import com.a4a.g8invoicing.shared.resources.client_or_issuer_section_addresses
import com.a4a.g8invoicing.shared.resources.client_or_issuer_section_backup
import com.a4a.g8invoicing.shared.resources.client_or_issuer_section_bank
import com.a4a.g8invoicing.shared.resources.client_or_issuer_section_identification
import com.a4a.g8invoicing.shared.resources.client_or_issuer_section_notes
import org.jetbrains.compose.resources.stringResource

@Composable
fun ClientOrIssuerAddEditForm(
    clientOrIssuerUiState: ClientOrIssuerState,
    onValueChange: (ScreenElement, Any) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    isInBottomSheetModal: Boolean = false,
    onClickDeleteAddress: () -> Unit,
    onClickDeleteEmail: (Int) -> Unit = {},
    onAddEmail: (String) -> Unit = {},
    typeOfCreation: DocumentBottomSheetTypeOfForm?,
    scrollState: ScrollState = rememberScrollState(),
    pendingEmailStateHolder: MutableState<String>? = null,
    onPendingEmailValidationResult: (Boolean) -> Unit = {},
    syncToMasterChecked: Boolean = false,
    onSyncToMasterChange: (Boolean) -> Unit = {},
) {
    val dataSource: ClientOrIssuerLocalDataSourceInterface = koinInject()
    var defaultCountryCode by remember {
        mutableStateOf(CountryCodes.pickDefaultForNewAddress(null))
    }
    LaunchedEffect(clientOrIssuerUiState.id) {
        val fallback = CountryCodes.pickDefaultForNewAddress(dataSource.getLastCountryCode())
        defaultCountryCode = fallback
        // Only seed the country on creation (id == null). In edit mode we never
        // write to state from here: an existing client's country_code=NULL means
        // the user hasn't set one yet, and silently backfilling with the cascade
        // fallback would clobber whatever they intended (e.g. an onboarding-set
        // country the client-list flow hasn't propagated yet).
        if (clientOrIssuerUiState.id == null) {
            val addresses = clientOrIssuerUiState.addresses
            if (addresses.isNullOrEmpty()) {
                // No AddressState exists yet ("Ajouter une entreprise" starts
                // with addresses=null). Seed COUNTRY_1 so the VM's write path
                // creates the first address with the cascade country — without
                // this, the display fallback lied about what was persisted and
                // the country was empty on reopen.
                val screenEl = if (isInBottomSheetModal)
                    ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_COUNTRY_1
                else ScreenElement.CLIENT_OR_ISSUER_COUNTRY_1
                onValueChange(screenEl, TextFieldValue(fallback))
            } else {
                addresses.forEachIndexed { index, address ->
                    if (address.countryCode.isNullOrBlank()) {
                        val screenEl = if (isInBottomSheetModal)
                            ScreenElement.valueOf("DOCUMENT_CLIENT_OR_ISSUER_COUNTRY_${index + 1}")
                        else
                            ScreenElement.valueOf("CLIENT_OR_ISSUER_COUNTRY_${index + 1}")
                        onValueChange(screenEl, TextFieldValue(fallback))
                    }
                }
            }
        }
    }
    val localFocusManager = LocalFocusManager.current
    // Use client ID as key to re-calculate when editing a different client
    // Use client ID AND addresses size as key to re-calculate when addresses change
    var numberOfClientAddresses by remember(clientOrIssuerUiState.id, clientOrIssuerUiState.addresses?.size) {
        mutableIntStateOf(
            clientOrIssuerUiState.addresses?.size?.coerceIn(1, 3) ?: 1
        )
    }
    val paddingTop = if (isInBottomSheetModal) 10.dp else 110.dp

    // Hoist all string resources
    val clientNameLabel = stringResource(Res.string.client_name)
    val clientNamePlaceholder = stringResource(Res.string.client_name_input)
    val clientFirstNameLabel = stringResource(Res.string.client_first_name)
    val clientFirstNamePlaceholder = stringResource(Res.string.client_first_name_input)
    val clientEmailLabel = stringResource(Res.string.client_email)
    val clientEmailPlaceholder = stringResource(Res.string.client_email_input)
    val clientPhoneLabel = stringResource(Res.string.client_phone)
    val clientPhonePlaceholder = stringResource(Res.string.client_phone_input)
    val clientAddressTitleLabel = stringResource(Res.string.client_address_title)
    val clientAddressTitleInvoicingPlaceholder = stringResource(Res.string.client_address_title_invoicing_placeholder)
    val clientAddressTitleDeliveryPlaceholder = stringResource(Res.string.client_address_title_delivery_placeholder)
    val clientAddressTitleHeadOfficePlaceholder = stringResource(Res.string.client_address_title_head_office_placeholder)
    val clientAddress1Label = stringResource(Res.string.client_address1)
    val clientAddress1Placeholder = stringResource(Res.string.client_address1_input)
    val clientAddress2Placeholder = stringResource(Res.string.client_address2_input)
    val clientCityLabel = stringResource(Res.string.client_city)
    val clientCityPlaceholder = stringResource(Res.string.client_city_input)
    val clientZipCodeLabel = stringResource(Res.string.client_zip_code)
    val clientZipCodePlaceholder = stringResource(Res.string.client_zip_code_input)
    val clientCountryLabel = stringResource(Res.string.client_country)
    // Which address-index (1..3) currently has its country picker sheet open. null = none.
    // Tracked at the outer scope so the sheet renders once, after the address loop, and
    // the same rendering path is shared by all three possible addresses.
    var countryPickerAddressIndex: Int? by remember { mutableStateOf(null) }
    val companyId1Label = stringResource(Res.string.company_identification1)
    val companyId2Label = stringResource(Res.string.company_identification2)
    val companyId3Label = stringResource(Res.string.company_identification3)
    val clientCompanyId1Placeholder = stringResource(Res.string.client_company_identification1_input)
    val clientCompanyId2Placeholder = stringResource(Res.string.client_company_identification2_input)
    val clientCompanyId3Placeholder = stringResource(Res.string.client_company_identification3_input)
    val clientNotesLabel = stringResource(Res.string.client_notes)
    val clientNotesPlaceholder = stringResource(Res.string.client_notes_input)
    val clientAddAddressText = stringResource(Res.string.client_add_address)
    val issuerLogoLabel = stringResource(Res.string.issuer_logo_label)
    val issuerLogoSelect = stringResource(Res.string.issuer_logo_select)
    val issuerLogoRemove = stringResource(Res.string.issuer_logo_remove)
    val issuerLogoErrorTitle = stringResource(Res.string.issuer_logo_error_title)
    val issuerLogoErrorDismiss = stringResource(Res.string.issuer_logo_error_dismiss)
    val issuerVatExemptLabel = stringResource(Res.string.issuer_vat_exempt_label)
    val issuerIntraEuSalesLabel = stringResource(Res.string.issuer_intra_eu_sales_label)
    val issuerIntraEuSalesInfoTitle = stringResource(Res.string.issuer_intra_eu_sales_info_modal_title)
    val issuerIntraEuSalesInfoContent = stringResource(Res.string.issuer_intra_eu_sales_info_modal_content)
    val issuerIntraEuSalesInfoDesc = stringResource(Res.string.issuer_intra_eu_sales_info_desc)
    val issuerTaxWithholdingLabel = stringResource(Res.string.issuer_tax_withholding_label)
    val issuerTaxWithholdingDesc = stringResource(Res.string.issuer_tax_withholding_info_modal_content)
    val issuerTaxWithholdingInfoDesc = stringResource(Res.string.issuer_tax_withholding_info_desc)

    // Check if this is an issuer (to show logo field)
    // Also check typeOfCreation for new issuer creation where type might be null
    val isIssuer = clientOrIssuerUiState.type == ClientOrIssuerType.ISSUER ||
            clientOrIssuerUiState.type == ClientOrIssuerType.DOCUMENT_ISSUER ||
            typeOfCreation == DocumentBottomSheetTypeOfForm.NEW_ISSUER ||
            typeOfCreation == DocumentBottomSheetTypeOfForm.EDIT_ISSUER

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .background(AppColors.divider)
            .fillMaxSize()
            .dismissKeyboardOnUnconsumedTap()
            .padding(12.dp)
            .padding(top = paddingTop, bottom = 60.dp)
            .imePadding()
    ) {
        // B2B/B2C picker — only relevant on clients (issuers don't carry a
        // client_type; the emitter is always "us"). Sits above every other
        // block so the choice frames the rest of the form (Factur-X eligibility
        // hinges on it) and mirrors the ordering asked for in the work log.
        if (!isIssuer) {
            // No surface card behind the picker — the grey rail is the entire
            // affordance, we want it to sit directly on the screen background.
            ClientTypePicker(
                selected = clientOrIssuerUiState.clientType,
                onSelect = { newType ->
                    // Wrap so the (nullable) choice survives the
                    // onValueChange(_, Any) contract.
                    onValueChange(
                        ScreenElement.CLIENT_TYPE,
                        com.a4a.g8invoicing.data.models.ClientTypeChoice(newType),
                    )
                },
            )
            Spacer(Modifier.padding(bottom = 16.dp))
        }
        Column(
            modifier = Modifier
                .background(color = AppColors.surface, shape = RoundedCornerShape(6.dp))
                .padding(
                    //start = 20.dp,
                    top = 8.dp,
                    // end = 20.dp,
                )
        ) {
            // Create the list with all fields (name, first name, phone)
            val inputList = listOf(
                FormInput(
                    label = clientNameLabel,
                    inputType = TextInput(
                        text = clientOrIssuerUiState.name,
                        placeholder = clientNamePlaceholder,
                        onValueChange = {
                            onValueChange(
                                if (isInBottomSheetModal) ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_NAME
                                else ScreenElement.CLIENT_OR_ISSUER_NAME,
                                it
                            )
                        },
                    ),
                    pageElement = if (isInBottomSheetModal) ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_NAME
                    else ScreenElement.CLIENT_OR_ISSUER_NAME,
                    isMandatory = true
                ),
                FormInput(
                    label = clientFirstNameLabel,
                    inputType = TextInput(
                        text = clientOrIssuerUiState.firstName,
                        placeholder = clientFirstNamePlaceholder,
                        onValueChange = {
                            onValueChange(
                                if (isInBottomSheetModal) ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_FIRST_NAME
                                else ScreenElement.CLIENT_OR_ISSUER_FIRST_NAME,
                                it
                            )
                        },
                    ),
                    pageElement = if (isInBottomSheetModal) ScreenElement.CLIENT_OR_ISSUER_FIRST_NAME
                    else ScreenElement.CLIENT_OR_ISSUER_FIRST_NAME
                ),
                FormInput(
                    label = clientPhoneLabel,
                    inputType = TextInput(
                        text = clientOrIssuerUiState.phone,
                        placeholder = clientPhonePlaceholder,
                        onValueChange = {
                            onValueChange(
                                if (isInBottomSheetModal) ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_PHONE
                                else ScreenElement.CLIENT_OR_ISSUER_PHONE,
                                it
                            )
                        },
                        keyboardType = KeyboardType.Phone,
                    ),
                    pageElement = if (isInBottomSheetModal) ScreenElement.CLIENT_OR_ISSUER_PHONE
                    else ScreenElement.CLIENT_OR_ISSUER_PHONE
                ),
                FormInput(
                    label = clientEmailLabel,
                    inputType = EmailListInput(
                        emails = clientOrIssuerUiState.emails ?: emptyList(),
                        placeholder = clientEmailPlaceholder,
                        onAddEmail = onAddEmail,
                        onRemoveEmail = onClickDeleteEmail,
                        maxEmails = 4,
                        onPendingEmailValidationResult = onPendingEmailValidationResult,
                        pendingEmailStateHolder = pendingEmailStateHolder
                    ),
                    pageElement = if (isInBottomSheetModal)
                        ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_EMAIL_1
                    else ScreenElement.CLIENT_OR_ISSUER_EMAIL_1
                )
            )
            // A professional client is a company — no personal first name to
            // capture. Hide the field once the picker locks in PROFESSIONAL
            // (individual + unset still show it, since a null clientType might
            // resolve to individual at export time).
            val hideFirstName = !isIssuer &&
                clientOrIssuerUiState.clientType == com.a4a.g8invoicing.data.models.ClientType.PROFESSIONAL
            val visibleInputs = if (hideFirstName) {
                inputList.filter { form ->
                    form.pageElement != ScreenElement.CLIENT_OR_ISSUER_FIRST_NAME &&
                        form.pageElement != ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_FIRST_NAME
                }
            } else inputList
            // Create the UI with list items
            FormUI(
                inputList = visibleInputs,
                localFocusManager = localFocusManager,
                placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                errors = clientOrIssuerUiState.errors
            )
        }

        if (isIssuer) {
            Spacer(Modifier.padding(bottom = 16.dp))
            Column(
                modifier = Modifier
                    .background(color = AppColors.surface, shape = RoundedCornerShape(6.dp))
                    .padding(top = 8.dp, bottom = 8.dp)
            ) {
                LogoPickerComponent(
                    label = issuerLogoLabel,
                    selectButtonText = issuerLogoSelect,
                    removeButtonText = issuerLogoRemove,
                    issuerId = clientOrIssuerUiState.id?.toInt(),
                    currentLogoPath = clientOrIssuerUiState.logoPath,
                    onLogoPathChanged = { newPath ->
                        onValueChange(
                            if (isInBottomSheetModal) ScreenElement.DOCUMENT_ISSUER_LOGO
                            else ScreenElement.ISSUER_LOGO,
                            newPath ?: ""
                        )
                    },
                    errorTitle = issuerLogoErrorTitle,
                    errorDismissText = issuerLogoErrorDismiss
                )
            }
        }

        Spacer(Modifier.padding(bottom = 16.dp))
        SectionTitle(stringResource(Res.string.client_or_issuer_section_addresses))

        for (i in 1..numberOfClientAddresses) {
            val address = clientOrIssuerUiState.addresses?.getOrNull(i - 1)

            Column(
                modifier = Modifier
                    .background(color = AppColors.surface, shape = RoundedCornerShape(6.dp))
                    .padding(top = 8.dp)
            ) {
                val inputList = mutableListOf(
                    FormInput(
                        label = clientAddress1Label,
                        inputType = TextInput(
                            text = address?.addressLine1,
                            placeholder = clientAddress1Placeholder,
                            onValueChange = {
                                onValueChange(
                                    if (isInBottomSheetModal) ScreenElement.valueOf("DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_1_$i")
                                    else ScreenElement.valueOf("CLIENT_OR_ISSUER_ADDRESS_LINE_1_$i"),
                                    it
                                )
                            },
                        ),
                        pageElement = if (isInBottomSheetModal)
                            ScreenElement.valueOf("DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_1_$i")
                        else ScreenElement.valueOf("CLIENT_OR_ISSUER_ADDRESS_LINE_1_$i")
                    ),
                    FormInput(
                        label = "",
                        inputType = TextInput(
                            text = address?.addressLine2,
                            placeholder = clientAddress2Placeholder,
                            onValueChange = {
                                onValueChange(
                                    if (isInBottomSheetModal) ScreenElement.valueOf("DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_2_$i")
                                    else ScreenElement.valueOf("CLIENT_OR_ISSUER_ADDRESS_LINE_2_$i"),
                                    it
                                )
                            },
                        ),
                        pageElement = if (isInBottomSheetModal)
                            ScreenElement.valueOf("DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_LINE_2_$i")
                        else ScreenElement.valueOf("CLIENT_OR_ISSUER_ADDRESS_LINE_2_$i")
                    ),
                    FormInput(
                        label = clientZipCodeLabel,
                        inputType = TextInput(
                            text = address?.zipCode,
                            placeholder = clientZipCodePlaceholder,
                            onValueChange = {
                                onValueChange(
                                    if (isInBottomSheetModal) ScreenElement.valueOf("DOCUMENT_CLIENT_OR_ISSUER_ZIP_$i")
                                    else ScreenElement.valueOf("CLIENT_OR_ISSUER_ZIP_$i"),
                                    it
                                )
                            },
                        ),
                        pageElement = if (isInBottomSheetModal)
                            ScreenElement.valueOf("DOCUMENT_CLIENT_OR_ISSUER_ZIP_$i")
                        else ScreenElement.valueOf("CLIENT_OR_ISSUER_ZIP_$i")
                    ),
                    FormInput(
                        label = clientCityLabel,
                        inputType = TextInput(
                            text = address?.city,
                            placeholder = clientCityPlaceholder,
                            onValueChange = {
                                onValueChange(
                                    if (isInBottomSheetModal) ScreenElement.valueOf("DOCUMENT_CLIENT_OR_ISSUER_CITY_$i")
                                    else ScreenElement.valueOf("CLIENT_OR_ISSUER_CITY_$i"),
                                    it
                                )
                            },
                        ),
                        pageElement = if (isInBottomSheetModal)
                            ScreenElement.valueOf("DOCUMENT_CLIENT_OR_ISSUER_CITY_$i")
                        else ScreenElement.valueOf("CLIENT_OR_ISSUER_CITY_$i")
                    ),
                    // Factur-X requires country with ISO 3166-1 code
                    // In creation, prefill with cascade default (last-used → device locale → FR).
                    // In edit, mirror the state exactly (empty when NULL) so the display never
                    // lies about what's persisted.
                    FormInput(
                        label = clientCountryLabel,
                        inputType = ForwardElement(
                            text = CountryCodes.displayNameOf(
                                address?.countryCode?.takeIf { it.isNotBlank() }
                                    ?: if (clientOrIssuerUiState.id == null) defaultCountryCode else null
                            ),
                            isMultiline = false,
                        ),
                        pageElement = if (isInBottomSheetModal)
                            ScreenElement.valueOf("DOCUMENT_CLIENT_OR_ISSUER_COUNTRY_$i")
                        else ScreenElement.valueOf("CLIENT_OR_ISSUER_COUNTRY_$i")
                    )
                )

                if (numberOfClientAddresses > 1) {
                    val placeholderText = when (i) {
                        1 -> clientAddressTitleInvoicingPlaceholder
                        2 -> clientAddressTitleDeliveryPlaceholder
                        3 -> clientAddressTitleHeadOfficePlaceholder
                        else -> ""
                    }

                    inputList.add(0, FormInput(
                        label = clientAddressTitleLabel,
                        inputType = TextInput(
                            text = address?.addressTitle,
                            placeholder = placeholderText,
                            onValueChange = {
                                onValueChange(
                                    if (isInBottomSheetModal) ScreenElement.valueOf("DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_TITLE_$i")
                                    else ScreenElement.valueOf("CLIENT_OR_ISSUER_ADDRESS_TITLE_$i"),
                                    it
                                )
                            },
                        ),
                        pageElement = if (isInBottomSheetModal)
                            ScreenElement.valueOf("DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_TITLE_$i")
                        else ScreenElement.valueOf("CLIENT_OR_ISSUER_ADDRESS_TITLE_$i")
                    ))
                }


                val showDelete = typeOfCreation?.name.toString()
                    .contains(ClientOrIssuerType.CLIENT.name)
                    && i > 1 && i == numberOfClientAddresses
                FormUI(
                    inputList = inputList,
                    localFocusManager = localFocusManager,
                    onClickForward = { element ->
                        // The only ForwardElement in this form is the country row; every
                        // ScreenElement whose name contains "COUNTRY_" opens the picker for
                        // the address slot pointed to by its trailing digit.
                        val name = element.name
                        if ("COUNTRY_" in name) {
                            countryPickerAddressIndex = name.last().digitToIntOrNull()
                        }
                    },
                    placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                    errors = clientOrIssuerUiState.errors,
                    trailingContent = if (showDelete) {
                        {
                            DeleteBlockRow(
                                onClick = {
                                    numberOfClientAddresses -= 1
                                    if (clientOrIssuerUiState.addresses?.getOrNull(i - 1) != null) {
                                        onClickDeleteAddress()
                                    }
                                },
                            )
                        }
                    } else null,
                )
            }

            if (typeOfCreation?.name.toString().contains(ClientOrIssuerType.CLIENT.name)) {
                Row(Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                    Spacer(Modifier.weight(1F))
                    if (i != 3 && numberOfClientAddresses == i) {
                        AddAddressButton(
                            onClick = { numberOfClientAddresses += 1 },
                            topPadding = 3.dp,
                            bottomPadding = 16.dp,
                            text = clientAddAddressText
                        )
                    }
                }
            } else Spacer(Modifier.padding(bottom = 20.dp))
        }

        // A particulier (INDIVIDUAL client) has no business identifiers by
        // definition, so the whole SIRET / VAT / RCS block is hidden once
        // the picker locks in INDIVIDUAL. Issuers always keep the section
        // (an issuer is always a business entity in this app).
        val hideCompanyIdentification = !isIssuer &&
            clientOrIssuerUiState.clientType == com.a4a.g8invoicing.data.models.ClientType.INDIVIDUAL

        if (!hideCompanyIdentification) {
        // Breathing room between the address block(s) and the identification
        // header — the address stack can be tall (up to 3 blocks + "+ Ajouter"
        // button + delete row) and without this the identification title
        // felt visually glued to the last address.
        Spacer(Modifier.height(30.dp))
        SectionTitle(stringResource(Res.string.client_or_issuer_section_identification))

        Column(
            modifier = Modifier
                .background(color = AppColors.surface, shape = RoundedCornerShape(6.dp))
                .padding(
                    top = 8.dp
                )

        ) {
            // Company-ID (SIRET / VAT / RCS) have no matching ContentType — opt out of
            // autofill entirely so the system doesn't offer credit-card or password
            // suggestions in a business-identifier field.
            val inputList = listOf(
                FormInput(
                    label = TextInput(
                        text = clientOrIssuerUiState.companyId1Label ?: TextFieldValue(companyId1Label),
                        onValueChange = {
                            onValueChange(
                                if (isInBottomSheetModal) ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION1_LABEL
                                else ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION1_LABEL,
                                it
                            )
                        },
                    ),
                    inputType = TextInput(
                        text = clientOrIssuerUiState.companyId1Number,
                        placeholder = clientCompanyId1Placeholder,
                        onValueChange = {
                            onValueChange(
                                if (isInBottomSheetModal) ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION1_VALUE
                                else ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION1_VALUE,
                                it
                            )
                        },
                    ),
                    pageElement = if (isInBottomSheetModal) ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION1
                    else ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION1
                ),
                FormInput(
                    label = TextInput(
                        text = clientOrIssuerUiState.companyId2Label ?: TextFieldValue(companyId2Label),
                        onValueChange = {
                            onValueChange(
                                if (isInBottomSheetModal) ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION2_LABEL
                                else ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION2_LABEL,
                                it
                            )
                        },
                    ),
                    inputType = TextInput(
                        text = clientOrIssuerUiState.companyId2Number,
                        placeholder = clientCompanyId2Placeholder,
                        onValueChange = {
                            onValueChange(
                                if (isInBottomSheetModal) ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION2_VALUE
                                else ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION2_VALUE,
                                it
                            )
                        },
                    ),
                    pageElement = if (isInBottomSheetModal) ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION2
                    else ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION2
                ),
                FormInput(
                    label = TextInput(
                        text = clientOrIssuerUiState.companyId3Label ?: TextFieldValue(companyId3Label),
                        onValueChange = {
                            onValueChange(
                                if (isInBottomSheetModal) ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION3_LABEL
                                else ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION3_LABEL,
                                it
                            )
                        },
                    ),
                    inputType = TextInput(
                        text = clientOrIssuerUiState.companyId3Number,
                        placeholder = clientCompanyId3Placeholder,
                        onValueChange = {
                            onValueChange(
                                if (isInBottomSheetModal) ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION3_VALUE
                                else ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION3_VALUE,
                                it
                            )
                        },
                    ),
                    pageElement = if (isInBottomSheetModal) ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION3
                    else ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION3
                )
            )

            // Create the UI with list items
            FormUI(
                inputList = inputList,
                localFocusManager = localFocusManager,
                placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                errors = clientOrIssuerUiState.errors
            )
        }
        }

        if (isIssuer) {
            Spacer(Modifier.padding(bottom = 16.dp))

            // BT-118=E. Matching legal mention is appended by the XML serializer.
            Row(
                modifier = Modifier
                    .background(color = AppColors.surface, shape = RoundedCornerShape(6.dp))
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Text(
                    text = issuerVatExemptLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = clientOrIssuerUiState.vatExempt,
                    onCheckedChange = { checked ->
                        onValueChange(
                            if (isInBottomSheetModal) ScreenElement.DOCUMENT_ISSUER_VAT_EXEMPT
                            else ScreenElement.ISSUER_VAT_EXEMPT,
                            checked
                        )
                    },
                    modifier = Modifier.scale(0.8f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ColorVioletLink,
                        checkedBorderColor = Color.Transparent,
                        uncheckedBorderColor = Color.Transparent,
                    ),
                )
            }

            // Same defaultCountryCode fallback as the country field: without it,
            // a pre-1.8 issuer with country_code NULL would render "France" but
            // hide this switch.
            val issuerCountry = clientOrIssuerUiState.addresses?.firstOrNull()?.countryCode
                ?.takeIf { it.isNotBlank() }
                ?: defaultCountryCode
            if (CountryCodes.isInEU(issuerCountry)) {
                Spacer(Modifier.padding(bottom = 16.dp))
                Row(
                    modifier = Modifier
                        .background(color = AppColors.surface, shape = RoundedCornerShape(6.dp))
                        .fillMaxWidth()
                        // Match the 26.dp start-padding used on the "Franchise en
                        // base de TVA" row above so both labels align on the same
                        // vertical guide.
                        .padding(start = 16.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Text(
                        text = issuerIntraEuSalesLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    InfoTooltipButton(
                        title = issuerIntraEuSalesInfoTitle,
                        content = issuerIntraEuSalesInfoContent,
                        contentDescription = issuerIntraEuSalesInfoDesc,
                        persistenceKey = "issuer_intra_eu_sales",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                    Spacer(Modifier.weight(1f))
                    Switch(
                        checked = clientOrIssuerUiState.intraEuSales,
                        onCheckedChange = { checked ->
                            onValueChange(
                                if (isInBottomSheetModal) ScreenElement.DOCUMENT_ISSUER_INTRA_EU_SALES
                                else ScreenElement.ISSUER_INTRA_EU_SALES,
                                checked
                            )
                        },
                        modifier = Modifier.scale(0.8f),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ColorVioletLink,
                            checkedBorderColor = Color.Transparent,
                            uncheckedBorderColor = Color.Transparent,
                        ),
                    )
                }
            }

            // Withholding tax toggle. Hidden outside the RETENTION_COUNTRIES set
            // (Spain, Portugal, Italy, Japan, Chile, Peru, Mexico, Brazil,
            // Colombia) — the row would be noise for users in other markets.
            if (CountryCodes.isRetentionCountry(issuerCountry)) {
                Spacer(Modifier.padding(bottom = 16.dp))
                Row(
                    modifier = Modifier
                        .background(color = AppColors.surface, shape = RoundedCornerShape(6.dp))
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Text(
                        text = issuerTaxWithholdingLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    InfoTooltipButton(
                        title = issuerTaxWithholdingLabel,
                        content = issuerTaxWithholdingDesc,
                        contentDescription = issuerTaxWithholdingInfoDesc,
                        persistenceKey = "issuer_tax_withholding",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                    Spacer(Modifier.weight(1f))
                    Switch(
                        checked = clientOrIssuerUiState.taxWithholdingEnabled,
                        onCheckedChange = { checked ->
                            onValueChange(
                                if (isInBottomSheetModal) ScreenElement.DOCUMENT_ISSUER_TAX_WITHHOLDING
                                else ScreenElement.ISSUER_TAX_WITHHOLDING,
                                checked
                            )
                        },
                        modifier = Modifier.scale(0.8f),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ColorVioletLink,
                            checkedBorderColor = Color.Transparent,
                            uncheckedBorderColor = Color.Transparent,
                        ),
                    )
                }
            }

            Spacer(Modifier.padding(bottom = 16.dp))
            SectionTitle(stringResource(Res.string.client_or_issuer_section_bank))
            IssuerBanksSection(
                banks = clientOrIssuerUiState.banks,
                onBanksChange = { onValueChange(ScreenElement.ISSUER_BANKS, it) },
                defaultCountryCode = issuerCountry,
            )
        }

        Spacer(Modifier.padding(bottom = 16.dp))
        SectionTitle(stringResource(Res.string.client_or_issuer_section_notes))

        Column(
            modifier = Modifier
                .background(color = AppColors.surface, shape = RoundedCornerShape(6.dp))
                .padding(
                    top = 8.dp
                )
        ) {
            val inputList = listOf(
                FormInput(
                    label = clientNotesLabel,
                    inputType = TextInput(
                        text = clientOrIssuerUiState.notes,
                        placeholder = clientNotesPlaceholder,
                        onValueChange = {
                            onValueChange(
                                if (isInBottomSheetModal) ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_NOTES
                                else ScreenElement.CLIENT_OR_ISSUER_NOTES,
                                it
                            )
                        },
                        isMultiline = true,
                        minLines = 3,
                    ),
                    pageElement = if (isInBottomSheetModal) ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_NOTES
                    else ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_NOTES
                ))

            // Create the UI with list items
            FormUI(
                inputList = inputList,
                localFocusManager = localFocusManager,
                placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                errors = clientOrIssuerUiState.errors
            )
        }

        // Sync-to-master switch — only when editing a document snapshot of an
        // existing master client / issuer. Sits in its own white block, detached
        // from the notes block above so the toggle reads as a separate decision.
        // Style matches the VAT-exempt / intra-EU switches above.
        val showSyncSwitch = isInBottomSheetModal && (
            typeOfCreation == DocumentBottomSheetTypeOfForm.EDIT_CLIENT ||
                typeOfCreation == DocumentBottomSheetTypeOfForm.EDIT_ISSUER
            )
        if (showSyncSwitch) {
            Spacer(modifier = Modifier.padding(top = 12.dp))
            SectionTitle(stringResource(Res.string.client_or_issuer_section_backup))
            Row(
                modifier = Modifier
                    .background(color = AppColors.surface, shape = RoundedCornerShape(6.dp))
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.document_form_sync_to_master),
                    // Match the Franchise / Intra-EU / Withholding switch rows
                    // above so the four switches read at the same visual weight.
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 15.dp),
                )
                Switch(
                    checked = syncToMasterChecked,
                    onCheckedChange = { onSyncToMasterChange(it) },
                    modifier = Modifier.scale(0.8f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ColorVioletLink,
                        checkedBorderColor = Color.Transparent,
                        uncheckedBorderColor = Color.Transparent,
                    ),
                )
            }
        }
    }

    // Country picker sheet — one instance shared by the three possible address rows.
    // Runs outside the form Columns so the ModalBottomSheet floats over the whole
    // screen. The cascade default (last-used → device locale → "FR") kicks in when
    // countryCode is null on the selected address.
    val pickerIndex = countryPickerAddressIndex
    if (pickerIndex != null) {
        val currentAddress = clientOrIssuerUiState.addresses?.getOrNull(pickerIndex - 1)
        val currentCode = currentAddress?.countryCode?.takeIf { it.isNotBlank() }
        val screenElFor: (Int) -> ScreenElement = { i ->
            if (isInBottomSheetModal)
                ScreenElement.valueOf("DOCUMENT_CLIENT_OR_ISSUER_COUNTRY_$i")
            else
                ScreenElement.valueOf("CLIENT_OR_ISSUER_COUNTRY_$i")
        }
        CountryPicker(
            currentCode = currentCode,
            onSelect = { code ->
                onValueChange(screenElFor(pickerIndex), TextFieldValue(code))
                // Broadcast the pick to every other address that still has no
                // country set — the common case (multi-address clients live in
                // one country). Addresses that already carry a country are left
                // alone so users can keep distinct countries per address.
                clientOrIssuerUiState.addresses?.forEachIndexed { index, address ->
                    val position = index + 1
                    if (position != pickerIndex && address.countryCode.isNullOrBlank()) {
                        onValueChange(screenElFor(position), TextFieldValue(code))
                    }
                }
                countryPickerAddressIndex = null
            },
            onDismiss = { countryPickerAddressIndex = null },
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.textSection,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 6.dp),
    )
}

@Composable
fun AddAddressButton(onClick: () -> Unit, bottomPadding: Dp = 0.dp, topPadding: Dp = 10.dp, text: String) {
    Box(
        modifier = Modifier
            .padding(start = 4.dp, top = topPadding, bottom = bottomPadding)
            .background(
                color = AppColors.surface,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(enabled = true) {
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary),
            color = ColorVioletLink,
            text = AnnotatedString(text),
        )
    }
}

/**
 * Dynamic bank-accounts section for the issuer form. One [IssuerBankRow] per
 * account in [banks], plus a "+ Ajouter un IBAN" button at the bottom. The
 * [IssuerBankState.label] field only shows when the issuer holds 2+ accounts —
 * with a single account there is no ambiguity, so hiding the label keeps the
 * form uncluttered for the 90% case.
 *
 * The section fires [onBanksChange] on every mutation (typed field, add, remove)
 * with the whole updated list. Empty rows persist as-is here and get dropped
 * only at save time (see ClientOrIssuerLocalDataSource.saveIssuerBanks) so the
 * user can leave a placeholder open while editing.
 *
 * Cap at 2 for the moment (user's rule); the model supports N.
 */
@Composable
private fun IssuerBanksSection(
    banks: List<com.a4a.g8invoicing.ui.states.IssuerBankState>,
    onBanksChange: (List<com.a4a.g8invoicing.ui.states.IssuerBankState>) -> Unit,
    defaultCountryCode: String?,
) {
    val addLabel = stringResource(Res.string.issuer_bank_add)
    // Show at least one row so the user has something to type into on a fresh
    // issuer — treat "no banks stored" as "one empty placeholder".
    val display = if (banks.isEmpty()) listOf(com.a4a.g8invoicing.ui.states.IssuerBankState()) else banks
    val showLabelField = display.size > 1

    // Country picker index (1-based to match address pattern), null = closed.
    var bankCountryPickerIndex: Int? by remember { mutableStateOf(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        display.forEachIndexed { index, bank ->
            if (index > 0) {
                Spacer(Modifier.padding(bottom = 13.dp))
            }
            IssuerBankRow(
                bank = bank,
                showLabelField = showLabelField,
                defaultCountryCode = defaultCountryCode,
                onBankChange = { updated ->
                    val base = if (banks.isEmpty()) listOf(com.a4a.g8invoicing.ui.states.IssuerBankState()) else banks
                    val newList = base.toMutableList().apply { set(index, updated) }
                    onBanksChange(newList)
                },
                onCountryClick = { bankCountryPickerIndex = index + 1 },
                onDelete = if (index > 0) {
                    { onBanksChange(banks.toMutableList().apply { removeAt(index) }) }
                } else null,
            )
        }

        // Capped at 2 per the current spec.
        if (display.size < 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                AddAddressButton(
                    onClick = {
                        val base = if (banks.isEmpty()) listOf(com.a4a.g8invoicing.ui.states.IssuerBankState()) else banks
                        onBanksChange(base + com.a4a.g8invoicing.ui.states.IssuerBankState())
                    },
                    topPadding = 3.dp,
                    text = addLabel,
                )
            }
        }
    }

    val pickerIndex = bankCountryPickerIndex
    if (pickerIndex != null) {
        val currentBank = display.getOrNull(pickerIndex - 1)
        val currentCode = currentBank?.countryCode?.takeIf { it.isNotBlank() }
            ?: defaultCountryCode
        CountryPicker(
            currentCode = currentCode,
            onSelect = { code ->
                currentBank?.let {
                    val base = if (banks.isEmpty()) listOf(com.a4a.g8invoicing.ui.states.IssuerBankState()) else banks
                    val newList = base.toMutableList().apply {
                        set(pickerIndex - 1, it.copy(countryCode = code))
                    }
                    onBanksChange(newList)
                }
                bankCountryPickerIndex = null
            },
            onDismiss = { bankCountryPickerIndex = null },
        )
    }
}

@Composable
private fun IssuerBankRow(
    bank: com.a4a.g8invoicing.ui.states.IssuerBankState,
    showLabelField: Boolean,
    defaultCountryCode: String?,
    onBankChange: (com.a4a.g8invoicing.ui.states.IssuerBankState) -> Unit,
    onCountryClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val labelInputHint = stringResource(Res.string.issuer_bank_label_input)
    val labelFieldLabel = stringResource(Res.string.issuer_bank_label)
    val countryLabel = stringResource(Res.string.issuer_bank_country)
    val identifierIbanLabel = stringResource(Res.string.issuer_bank_identifier_iban)
    val identifierGenericLabel = stringResource(Res.string.issuer_bank_identifier_generic)

    // Effective country = user pick > company default. Drives the identifier
    // field label + placeholder (IBAN vs domestic account number).
    val effectiveCountry = bank.countryCode?.takeIf { it.isNotBlank() }
        ?: defaultCountryCode
    val isIban = CountryCodes.isIbanCountry(effectiveCountry)
    val identifierLabel = if (isIban) identifierIbanLabel else identifierGenericLabel
    val identifierPlaceholder = if (isIban) "FR76 1234 1234 1234 123456 1234" else ""

    // Single FormUI with all rows in one list so the built-in Separator() draws
    // between them consistently. "Libellé" is prepended only when the issuer
    // holds 2+ banks (no ambiguity to resolve with a single account).
    val inputs = buildList {
        if (showLabelField) {
            add(
                FormInput(
                    label = labelFieldLabel,
                    inputType = TextInput(
                        text = bank.label,
                        placeholder = labelInputHint,
                        onValueChange = {
                            onBankChange(bank.copy(label = it as TextFieldValue))
                        },
                    ),
                    pageElement = ScreenElement.ISSUER_BANKS,
                )
            )
        }
        add(
            FormInput(
                label = countryLabel,
                inputType = ForwardElement(
                    text = CountryCodes.displayNameOf(effectiveCountry),
                    isMultiline = false,
                ),
                pageElement = ScreenElement.ISSUER_BANK_COUNTRY,
            )
        )
        add(
            FormInput(
                label = identifierLabel,
                inputType = TextInput(
                    text = bank.identifier,
                    placeholder = identifierPlaceholder,
                    onValueChange = {
                        onBankChange(bank.copy(identifier = it as TextFieldValue))
                    },
                    // The FR76… placeholder wraps to 2 lines on narrow phones;
                    // reserve 2 lines when IBAN so the field height stays
                    // stable. Generic identifier has a shorter/no placeholder,
                    // single-line is fine.
                    minLines = if (isIban) 2 else 1,
                ),
                pageElement = ScreenElement.ISSUER_BANKS,
            )
        )
        // BIC is a SWIFT/ISO 9362 code used for international routing. Non-IBAN
        // countries typically use their domestic clearing (ACH, BSB, etc.), so
        // the BIC field isn't meaningful there — hide it.
        if (isIban) {
            add(
                FormInput(
                    label = "BIC",
                    inputType = TextInput(
                        text = bank.bic,
                        placeholder = "XXXXFRPPXXX",
                        onValueChange = {
                            onBankChange(bank.copy(bic = it as TextFieldValue))
                        },
                    ),
                    pageElement = ScreenElement.ISSUER_BANKS,
                )
            )
        }
    }
    Column(
        modifier = Modifier
            .background(color = AppColors.surface, shape = RoundedCornerShape(6.dp))
            .padding(top = 8.dp)
    ) {
        FormUI(
            inputList = inputs,
            localFocusManager = LocalFocusManager.current,
            onClickForward = { element ->
                if (element == ScreenElement.ISSUER_BANK_COUNTRY) onCountryClick()
            },
            placeCursorAtTheEndOfText = {},
            errors = mutableListOf(),
            trailingContent = if (onDelete != null) {
                { DeleteBlockRow(onClick = onDelete) }
            } else null,
        )
    }
}

