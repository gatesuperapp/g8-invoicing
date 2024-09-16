package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.shared.FormInput
import com.a4a.g8invoicing.ui.shared.FormUI
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.TextInput
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.theme.ColorBackgroundGrey
import com.a4a.g8invoicing.ui.theme.ColorDarkGray
import com.a4a.g8invoicing.ui.theme.callForActions
import icons.IconDelete

@Composable
fun ClientOrIssuerAddEditForm(
    clientOrIssuerUiState: ClientOrIssuerState,
    onValueChange: (ScreenElement, Any) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    isInBottomSheetModal: Boolean = false,
    onClickDeleteAddress: () -> Unit,
) {
    val localFocusManager = LocalFocusManager.current
    var numberOfClientAddresses by remember {
        mutableIntStateOf(
            clientOrIssuerUiState.addresses?.size ?: 1
        )
    }
    val paddingTop = if (isInBottomSheetModal) 20.dp else 80.dp

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .background(ColorBackgroundGrey)
            .fillMaxSize()
            .padding(12.dp)
            .padding(top = paddingTop, bottom = 40.dp)
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
                    label = stringResource(id = R.string.client_name),
                    inputType = TextInput(
                        text = clientOrIssuerUiState.name,
                        placeholder = stringResource(id = R.string.client_name_input),
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
                    label = stringResource(id = R.string.client_first_name),
                    inputType = TextInput(
                        text = clientOrIssuerUiState.firstName,
                        placeholder = stringResource(id = R.string.client_first_name_input),
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
                    label = stringResource(id = R.string.client_email),
                    inputType = TextInput(
                        text = clientOrIssuerUiState.email,
                        placeholder = stringResource(id = R.string.client_email_input),
                        onValueChange = {
                            onValueChange(
                                if (isInBottomSheetModal) ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_EMAIL
                                else ScreenElement.CLIENT_OR_ISSUER_EMAIL,
                                it
                            )
                        }
                    ),
                    pageElement = if (isInBottomSheetModal) ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_EMAIL
                    else ScreenElement.CLIENT_OR_ISSUER_EMAIL
                ),
                FormInput(
                    label = stringResource(id = R.string.client_phone),
                    inputType = TextInput(
                        text = clientOrIssuerUiState.phone,
                        placeholder = stringResource(id = R.string.client_phone_input),
                        onValueChange = {
                            onValueChange(
                                if (isInBottomSheetModal) ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_PHONE
                                else ScreenElement.CLIENT_OR_ISSUER_PHONE,
                                it
                            )
                        }
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

        for (i in 1..numberOfClientAddresses) {
            val address = clientOrIssuerUiState.addresses?.getOrNull(i - 1)

            Column(
                modifier = Modifier
                    .background(color = Color.White, shape = RoundedCornerShape(6.dp))
                    .padding(top = 8.dp)
            ) {
                val inputList = mutableListOf(
                    FormInput(
                        label = stringResource(id = R.string.client_address1),
                        inputType = TextInput(
                            text = address?.addressLine1,
                            placeholder = stringResource(id = R.string.client_address1_input),
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
                        label = stringResource(id = R.string.client_address2),
                        inputType = TextInput(
                            text = address?.addressLine2,
                            placeholder = stringResource(id = R.string.client_address2_input),
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
                        label = stringResource(id = R.string.client_zip_code),
                        inputType = TextInput(
                            text = address?.zipCode,
                            placeholder = stringResource(id = R.string.client_zip_code_input),
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
                        label = stringResource(id = R.string.client_city),
                        inputType = TextInput(
                            text = address?.city,
                            placeholder = stringResource(id = R.string.client_city_input),
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
                    var placeholderId = 0
                    when (i) {
                        1 -> placeholderId = R.string.client_address_title_invoicing_placeholder
                        2 -> placeholderId = R.string.client_address_title_delivery_placeholder
                        3 -> placeholderId = R.string.client_address_title_head_office_placeholder
                    }

                    inputList.add(0, FormInput(
                        label = stringResource(id = R.string.client_address_title),
                        inputType = TextInput(
                            text = address?.addressTitle,
                            placeholder = stringResource(placeholderId),
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

            if (!previousAddressIsFilled(clientOrIssuerUiState, i)) {
                Spacer(Modifier.padding(bottom = 16.dp))
            }

            Row(Modifier.padding(bottom = 6.dp)) {

                if (i != 3 &&
                    numberOfClientAddresses == i
                    && previousAddressIsFilled(clientOrIssuerUiState, i)
                ) {
                    AddAddressButton(
                        onClick = { numberOfClientAddresses += 1 },
                        bottomPadding = if (i == 1) 16.dp else 0.dp
                    )
                }
                if (i > 1 && i == numberOfClientAddresses) {
                    Spacer(Modifier.weight(1F))
                    DeleteAddressButton(onClick = {
                        numberOfClientAddresses -= 1
                        if (clientOrIssuerUiState.addresses?.getOrNull(i - 1) != null) {
                            onClickDeleteAddress()
                        }
                    })
                }

                /*else if (numberOfClientAddresses == i && clientOrIssuerUiState.addresses?.getOrNull(i - 1) != null) {
                    AddAddressButton(onClick = { numberOfClientAddresses += 1 })
                } else if (i != 1 && i == numberOfClientAddresses) {
                    AddAddressButton(onClick = {
                        numberOfClientAddresses += 1
                    })
                    Spacer(Modifier.weight(1F))
                    DeleteAddressButton(onClick = {
                        numberOfClientAddresses -= 1
                        if(clientOrIssuerUiState.addresses?.getOrNull(i - 1) != null) {
                            onClickDeleteAddress()
                        }
                    })
                }*/
            }
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
                        placeholder = stringResource(id = R.string.client_company_identification1_input),
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
                        placeholder = stringResource(id = R.string.client_company_identification2_input),
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
                        placeholder = stringResource(id = R.string.client_company_identification3_input),
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
                    label = stringResource(id = R.string.client_notes),
                    inputType = TextInput(
                        text = clientOrIssuerUiState.notes,
                        placeholder = stringResource(id = R.string.client_notes_input),
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
fun DeleteAddressButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .padding(end = 4.dp, top = 4.dp, bottom = 16.dp)
            .size(14.dp)
    ) {
        Icon(
            modifier = Modifier
                .size(22.dp),
            imageVector = IconDelete,
            tint = ColorDarkGray,
            contentDescription = Strings.get(R.string.client_delete_address)
        )
    }
}

@Composable
fun AddAddressButton(onClick: (Int) -> Unit, bottomPadding: Dp = 0.dp) {
    ClickableText(
        modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = bottomPadding),
        onClick = onClick,
        style = MaterialTheme.typography.callForActions,
        text = AnnotatedString(Strings.get(R.string.client_add_address))
    )
}