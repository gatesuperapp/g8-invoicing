package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Modifier
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
import com.a4a.g8invoicing.ui.screens.shared.CountryPicker
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.koinInject
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.client_add_address
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
import com.a4a.g8invoicing.shared.resources.client_delete_address
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
import com.a4a.g8invoicing.shared.resources.issuer_logo_error_dismiss
import com.a4a.g8invoicing.shared.resources.issuer_logo_error_title
import com.a4a.g8invoicing.shared.resources.issuer_logo_label
import com.a4a.g8invoicing.shared.resources.issuer_logo_remove
import com.a4a.g8invoicing.shared.resources.issuer_logo_select
import com.a4a.g8invoicing.shared.resources.issuer_intra_eu_sales_info_desc
import com.a4a.g8invoicing.shared.resources.issuer_intra_eu_sales_info_modal_content
import com.a4a.g8invoicing.shared.resources.issuer_intra_eu_sales_info_modal_title
import com.a4a.g8invoicing.shared.resources.issuer_intra_eu_sales_label
import com.a4a.g8invoicing.shared.resources.issuer_vat_exempt_info_desc
import com.a4a.g8invoicing.shared.resources.issuer_vat_exempt_info_modal_content
import com.a4a.g8invoicing.shared.resources.issuer_vat_exempt_info_modal_title
import com.a4a.g8invoicing.shared.resources.issuer_vat_exempt_label
import com.a4a.g8invoicing.shared.resources.client_zip_code
import com.a4a.g8invoicing.shared.resources.client_zip_code_input
import com.a4a.g8invoicing.ui.screens.shared.DocumentBottomSheetTypeOfForm
import com.a4a.g8invoicing.ui.shared.EmailListInput
import com.a4a.g8invoicing.ui.shared.ForwardElement
import com.a4a.g8invoicing.ui.shared.FormInput
import com.a4a.g8invoicing.ui.shared.FormUI
import com.a4a.g8invoicing.ui.shared.InfoTooltipButton
import com.a4a.g8invoicing.ui.shared.dismissKeyboardOnUnconsumedTap
import com.a4a.g8invoicing.ui.shared.LogoPickerComponent
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.TextInput
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.theme.ColorBackgroundGrey
import com.a4a.g8invoicing.ui.theme.ColorDarkGray
import com.a4a.g8invoicing.ui.theme.ColorVioletLink
import com.a4a.g8invoicing.ui.theme.callForActions
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
) {
    val dataSource: ClientOrIssuerLocalDataSourceInterface = koinInject()
    var defaultCountryCode by remember {
        mutableStateOf(CountryCodes.pickDefaultForNewAddress(null))
    }
    LaunchedEffect(clientOrIssuerUiState.id) {
        val fallback = CountryCodes.pickDefaultForNewAddress(dataSource.getLastCountryCode())
        defaultCountryCode = fallback
        // Populate le state pour les adresses migrées pre-1.8 qui ont country_code NULL :
        // l'affichage montrait le fallback (cascade) mais le state / la DB restaient null,
        // ce qui trompait l'user et ferait planter la génération Factur-X. On aligne
        // state = display en émettant onValueChange pour chaque adresse trouvée vide.
        clientOrIssuerUiState.addresses?.forEachIndexed { index, address ->
            if (address.countryCode.isNullOrBlank()) {
                val screenEl = if (isInBottomSheetModal)
                    ScreenElement.valueOf("DOCUMENT_CLIENT_OR_ISSUER_COUNTRY_${index + 1}")
                else
                    ScreenElement.valueOf("CLIENT_OR_ISSUER_COUNTRY_${index + 1}")
                onValueChange(screenEl, TextFieldValue(fallback))
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
    val clientDeleteAddressText = stringResource(Res.string.client_delete_address)
    val issuerLogoLabel = stringResource(Res.string.issuer_logo_label)
    val issuerLogoSelect = stringResource(Res.string.issuer_logo_select)
    val issuerLogoRemove = stringResource(Res.string.issuer_logo_remove)
    val issuerLogoErrorTitle = stringResource(Res.string.issuer_logo_error_title)
    val issuerLogoErrorDismiss = stringResource(Res.string.issuer_logo_error_dismiss)
    val issuerVatExemptLabel = stringResource(Res.string.issuer_vat_exempt_label)
    val issuerVatExemptInfoTitle = stringResource(Res.string.issuer_vat_exempt_info_modal_title)
    val issuerVatExemptInfoContent = stringResource(Res.string.issuer_vat_exempt_info_modal_content)
    val issuerVatExemptInfoDesc = stringResource(Res.string.issuer_vat_exempt_info_desc)
    val issuerIntraEuSalesLabel = stringResource(Res.string.issuer_intra_eu_sales_label)
    val issuerIntraEuSalesInfoTitle = stringResource(Res.string.issuer_intra_eu_sales_info_modal_title)
    val issuerIntraEuSalesInfoContent = stringResource(Res.string.issuer_intra_eu_sales_info_modal_content)
    val issuerIntraEuSalesInfoDesc = stringResource(Res.string.issuer_intra_eu_sales_info_desc)

    // Check if this is an issuer (to show logo field)
    // Also check typeOfCreation for new issuer creation where type might be null
    val isIssuer = clientOrIssuerUiState.type == ClientOrIssuerType.ISSUER ||
            clientOrIssuerUiState.type == ClientOrIssuerType.DOCUMENT_ISSUER ||
            typeOfCreation == DocumentBottomSheetTypeOfForm.NEW_ISSUER ||
            typeOfCreation == DocumentBottomSheetTypeOfForm.EDIT_ISSUER

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .background(ColorBackgroundGrey)
            .fillMaxSize()
            .dismissKeyboardOnUnconsumedTap()
            .padding(12.dp)
            .padding(top = paddingTop, bottom = 60.dp)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .background(color = Color.White, shape = RoundedCornerShape(6.dp))
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
                        }
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
                        }
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
                        keyboardType = KeyboardType.Phone
                    ),
                    pageElement = if (isInBottomSheetModal) ScreenElement.CLIENT_OR_ISSUER_PHONE
                    else ScreenElement.CLIENT_OR_ISSUER_PHONE
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

        Spacer(Modifier.padding(bottom = 16.dp))

        // Email section with chips
        Column(
            modifier = Modifier
                .background(color = Color.White, shape = RoundedCornerShape(6.dp))
                .padding(top = 8.dp)
        ) {
            val emailInputList = listOf(
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

            FormUI(
                inputList = emailInputList,
                localFocusManager = localFocusManager,
                placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                errors = clientOrIssuerUiState.errors
            )
        }

        Spacer(Modifier.padding(bottom = 16.dp))

        for (i in 1..numberOfClientAddresses) {
            val address = clientOrIssuerUiState.addresses?.getOrNull(i - 1)

            Column(
                modifier = Modifier
                    .background(color = Color.White, shape = RoundedCornerShape(6.dp))
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
                            }
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
                            }
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
                            }
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
                            }
                        ),
                        pageElement = if (isInBottomSheetModal)
                            ScreenElement.valueOf("DOCUMENT_CLIENT_OR_ISSUER_CITY_$i")
                        else ScreenElement.valueOf("CLIENT_OR_ISSUER_CITY_$i")
                    ),
                    // Factur-X requires country with ISO 3166-1 code
                    // New address will default to last country used in an address, else device locale
                    FormInput(
                        label = clientCountryLabel,
                        inputType = ForwardElement(
                            text = CountryCodes.displayNameOf(
                                address?.countryCode?.takeIf { it.isNotBlank() }
                                    ?: defaultCountryCode
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
                            }
                        ),
                        pageElement = if (isInBottomSheetModal)
                            ScreenElement.valueOf("DOCUMENT_CLIENT_OR_ISSUER_ADDRESS_TITLE_$i")
                        else ScreenElement.valueOf("CLIENT_OR_ISSUER_ADDRESS_TITLE_$i")
                    ))
                }


                // Create the UI with list items
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
                    errors = clientOrIssuerUiState.errors
                )
            }

            // Add buttons to add or delete address
            if (typeOfCreation?.name.toString().contains(ClientOrIssuerType.CLIENT.name)) {
                if (i == 1 && !previousAddressIsFilled(clientOrIssuerUiState, 1)) {
                    Spacer(Modifier.padding(bottom = 16.dp))
                }
                Row(Modifier.padding(bottom = 6.dp)) {
                    if (i != 3 &&
                        numberOfClientAddresses == i
                        && previousAddressIsFilled(clientOrIssuerUiState, i)
                    ) {
                        AddAddressButton(
                            onClick = { numberOfClientAddresses += 1 },
                            bottomPadding = 16.dp,
                            text = clientAddAddressText
                        )
                    }
                    if (i > 1 && i == numberOfClientAddresses) {
                        Spacer(Modifier.weight(1F))
                        DeleteAddressButton(
                            onClick = {
                                numberOfClientAddresses -= 1
                                if (clientOrIssuerUiState.addresses?.getOrNull(i - 1) != null) {
                                    onClickDeleteAddress()
                                }
                            },
                            contentDescription = clientDeleteAddressText
                        )
                    }
                }
            } else Spacer(Modifier.padding(bottom = 20.dp))
        }

        Column(
            modifier = Modifier
                .background(color = Color.White, shape = RoundedCornerShape(6.dp))
                .padding(
                    top = 8.dp
                )

        ) {
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
                        }
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
                        }
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
                        }
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
                        }
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
                        }
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

        Spacer(Modifier.padding(bottom = 16.dp))

        // Logo section (only for issuers)
        if (isIssuer) {
            Column(
                modifier = Modifier
                    .background(color = Color.White, shape = RoundedCornerShape(6.dp))
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

            Spacer(Modifier.padding(bottom = 16.dp))

            // Franchise en base de TVA (BT-118=E dans Factur-X). Toggle Switch dans un panneau
            // à part, même style visuel que le logo. La mention légale correspondante est
            // ajoutée par le sérialiseur XML au moment de la génération, pas ici.
            Row(
                modifier = Modifier
                    .background(color = Color.White, shape = RoundedCornerShape(6.dp))
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Text(
                    text = issuerVatExemptLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                InfoTooltipButton(
                    title = issuerVatExemptInfoTitle,
                    content = issuerVatExemptInfoContent,
                    contentDescription = issuerVatExemptInfoDesc,
                    persistenceKey = "issuer_vat_exempt",
                    modifier = Modifier.padding(start = 8.dp),
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
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ColorVioletLink,
                        checkedBorderColor = Color.Transparent,
                        uncheckedBorderColor = Color.Transparent,
                    ),
                )
            }

            // Ventes intra-UE : n'a de sens que pour un émetteur établi dans un pays UE
            // (post-Brexit → UK exclu, cf. CountryCodes.EU_COUNTRIES). Pour tous les
            // autres (Ghana, Mexique, US, UK, CH…), on cache complètement l'option pour
            // dégonfler l'UI. On applique le même fallback que l'affichage du champ
            // Pays (line ~410) sinon un issuer pre-1.8 avec country_code NULL en DB
            // affiche "France" via defaultCountryCode mais le switch resterait caché.
            val issuerCountry = clientOrIssuerUiState.addresses?.firstOrNull()?.countryCode
                ?.takeIf { it.isNotBlank() }
                ?: defaultCountryCode
            if (CountryCodes.isInEU(issuerCountry)) {
                Spacer(Modifier.padding(bottom = 16.dp))
                Row(
                    modifier = Modifier
                        .background(color = Color.White, shape = RoundedCornerShape(6.dp))
                        .fillMaxWidth()
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
        }

        Column(
            modifier = Modifier
                .background(color = Color.White, shape = RoundedCornerShape(6.dp))
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
                        }
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
    }

    // Country picker sheet — one instance shared by the three possible address rows.
    // Runs outside the form Columns so the ModalBottomSheet floats over the whole
    // screen. The cascade default (last-used → device locale → "FR") kicks in when
    // countryCode is null on the selected address.
    val pickerIndex = countryPickerAddressIndex
    if (pickerIndex != null) {
        val currentAddress = clientOrIssuerUiState.addresses?.getOrNull(pickerIndex - 1)
        val currentCode = currentAddress?.countryCode?.takeIf { it.isNotBlank() }
        val screenEl = if (isInBottomSheetModal)
            ScreenElement.valueOf("DOCUMENT_CLIENT_OR_ISSUER_COUNTRY_$pickerIndex")
        else
            ScreenElement.valueOf("CLIENT_OR_ISSUER_COUNTRY_$pickerIndex")
        CountryPicker(
            currentCode = currentCode,
            onSelect = { code ->
                onValueChange(screenEl, TextFieldValue(code))
                countryPickerAddressIndex = null
            },
            onDismiss = { countryPickerAddressIndex = null },
        )
    }
}

private fun previousAddressIsFilled(clientOrIssuerUiState: ClientOrIssuerState, i: Int): Boolean {
    val lastAddressesElement = clientOrIssuerUiState.addresses?.getOrNull(i - 1)
    val fieldsOfLastAddress = listOf(
        lastAddressesElement?.addressTitle?.text,
        lastAddressesElement?.addressLine1?.text,
        lastAddressesElement?.addressLine2?.text,
        lastAddressesElement?.zipCode?.text,
        lastAddressesElement?.city?.text,
    )
    return lastAddressesElement != null
            && fieldsOfLastAddress.any { !it.isNullOrEmpty() }
}

@Composable
fun DeleteAddressButton(onClick: () -> Unit, contentDescription: String) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .padding(end = 4.dp, top = 4.dp, bottom = 16.dp)
            .size(14.dp)
    ) {
        Icon(
            modifier = Modifier
                .size(22.dp),
            imageVector = Icons.Outlined.DeleteOutline,
            tint = ColorDarkGray,
            contentDescription = contentDescription
        )
    }
}

@Composable
fun AddAddressButton(onClick: () -> Unit, bottomPadding: Dp = 0.dp, text: String) {
    Box(
        modifier = Modifier
            .padding(start = 4.dp, top = 10.dp, bottom = bottomPadding)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(enabled = true) {
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            style = MaterialTheme.typography.callForActions,
            color = ColorVioletLink,
            text = AnnotatedString(text),
        )
    }
}

