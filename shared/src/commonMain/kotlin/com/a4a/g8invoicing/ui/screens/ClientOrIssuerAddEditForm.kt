package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.client_add_address
import com.a4a.g8invoicing.shared.resources.client_address1
import com.a4a.g8invoicing.shared.resources.client_address1_input
import com.a4a.g8invoicing.shared.resources.client_address2
import com.a4a.g8invoicing.shared.resources.client_address2_input
import com.a4a.g8invoicing.shared.resources.client_address_title
import com.a4a.g8invoicing.shared.resources.client_address_title_delivery_placeholder
import com.a4a.g8invoicing.shared.resources.client_address_title_head_office_placeholder
import com.a4a.g8invoicing.shared.resources.client_address_title_invoicing_placeholder
import com.a4a.g8invoicing.shared.resources.client_city
import com.a4a.g8invoicing.shared.resources.client_city_input
import com.a4a.g8invoicing.shared.resources.client_company_identification1_input
import com.a4a.g8invoicing.shared.resources.client_company_identification2_input
import com.a4a.g8invoicing.shared.resources.client_company_identification3_input
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
import com.a4a.g8invoicing.shared.resources.client_zip_code
import com.a4a.g8invoicing.shared.resources.client_zip_code_input
import com.a4a.g8invoicing.ui.screens.shared.DocumentBottomSheetTypeOfForm
import com.a4a.g8invoicing.ui.shared.FormInput
import com.a4a.g8invoicing.ui.shared.FormUI
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.TextInput
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.theme.ColorBackgroundGrey
import com.a4a.g8invoicing.ui.theme.ColorDarkGray
import com.a4a.g8invoicing.ui.theme.callForActions
import org.jetbrains.compose.resources.stringResource

@Composable
fun ClientOrIssuerAddEditForm(
    clientOrIssuerUiState: ClientOrIssuerState,
    onValueChange: (ScreenElement, Any) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    isInBottomSheetModal: Boolean = false,
    onClickDeleteAddress: () -> Unit,
    typeOfCreation: DocumentBottomSheetTypeOfForm?,
    scrollState: ScrollState = rememberScrollState(),
) {
    val localFocusManager = LocalFocusManager.current
    var numberOfClientAddresses by remember {
        mutableIntStateOf(
            clientOrIssuerUiState.addresses?.size ?: 1
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
    val clientAddress2Label = stringResource(Res.string.client_address2)
    val clientAddress2Placeholder = stringResource(Res.string.client_address2_input)
    val clientCityLabel = stringResource(Res.string.client_city)
    val clientCityPlaceholder = stringResource(Res.string.client_city_input)
    val clientZipCodeLabel = stringResource(Res.string.client_zip_code)
    val clientZipCodePlaceholder = stringResource(Res.string.client_zip_code_input)
    val clientCompanyId1Placeholder = stringResource(Res.string.client_company_identification1_input)
    val clientCompanyId2Placeholder = stringResource(Res.string.client_company_identification2_input)
    val clientCompanyId3Placeholder = stringResource(Res.string.client_company_identification3_input)
    val clientNotesLabel = stringResource(Res.string.client_notes)
    val clientNotesPlaceholder = stringResource(Res.string.client_notes_input)
    val clientAddAddressText = stringResource(Res.string.client_add_address)
    val clientDeleteAddressText = stringResource(Res.string.client_delete_address)

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .background(ColorBackgroundGrey)
            .fillMaxSize()
            .padding(12.dp)
            .padding(top = paddingTop, bottom = 60.dp)
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    localFocusManager.clearFocus()
                    // So when we click outside of a text input,
                    // the selection is cleared
                })
            }
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
            // Create the list with all fields
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
                    else ScreenElement.CLIENT_OR_ISSUER_NAME
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
                    label = clientEmailLabel,
                    inputType = TextInput(
                        text = clientOrIssuerUiState.email,
                        placeholder = clientEmailPlaceholder,
                        onValueChange = {
                            onValueChange(
                                if (isInBottomSheetModal) ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_EMAIL
                                else ScreenElement.CLIENT_OR_ISSUER_EMAIL,
                                it
                            )
                        },
                        keyboardType = KeyboardType.Email
                    ),
                    pageElement = if (isInBottomSheetModal) ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_EMAIL
                    else ScreenElement.CLIENT_OR_ISSUER_EMAIL
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
                        keyboardType = KeyboardType.Number
                    ),
                    pageElement = if (isInBottomSheetModal) ScreenElement.CLIENT_OR_ISSUER_PHONE
                    else ScreenElement.CLIENT_OR_ISSUER_PHONE,

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
                        label = clientAddress2Label,
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
                            },
                            keyboardType = KeyboardType.Number
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
                            bottomPadding = if (i == 1) 16.dp else 0.dp,
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
                        text = clientOrIssuerUiState.companyId1Label,
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
                        text = clientOrIssuerUiState.companyId2Label,
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
                        text = clientOrIssuerUiState.companyId3Label,
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
    Text(
        style = MaterialTheme.typography.callForActions,
        modifier = Modifier
            .padding(start = 4.dp, top = 4.dp, bottom = bottomPadding)
            .clickable(enabled = true) {
                onClick()
            },
        text = AnnotatedString(text),
    )
}
