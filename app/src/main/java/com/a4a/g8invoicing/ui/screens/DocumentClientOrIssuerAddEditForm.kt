package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.shared.FormInput
import com.a4a.g8invoicing.ui.shared.FormUI
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.TextInput
import com.a4a.g8invoicing.ui.states.DocumentClientOrIssuerState
import com.a4a.g8invoicing.ui.theme.ColorBackgroundGrey

@Composable
fun DocumentClientOrIssuerAddEditForm(
    documentClientOrIssuerState: DocumentClientOrIssuerState,
    onValueChange: (ScreenElement, Any) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    isDisplayedInBottomSheet: Boolean = false,
) {
    val localFocusManager = LocalFocusManager.current

    var modifier = Modifier
        .verticalScroll(rememberScrollState())
        .background(ColorBackgroundGrey)
        .fillMaxSize()
        .padding(12.dp)
        .imePadding()
        .pointerInput(Unit) {
            detectTapGestures(onTap = {
                localFocusManager.clearFocus()
                // So when we click outside of a text input,
                // the selection is cleared
            })
        }
    modifier = if (isDisplayedInBottomSheet)
        modifier.then(
            Modifier
                .fillMaxHeight(0.7f)
        )
    else
        modifier.then(
            Modifier
                .padding(top = 80.dp)
        )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
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
                        text = documentClientOrIssuerState.name,
                        placeholder = stringResource(id = R.string.client_name_input),
                        onValueChange = {
                            onValueChange(ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_NAME, it)
                        }
                    ),
                    pageElement = ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_NAME
                ),
                FormInput(
                    label = stringResource(id = R.string.client_first_name),
                    inputType = TextInput(
                        text = documentClientOrIssuerState.firstName,
                        placeholder = stringResource(id = R.string.client_first_name_input),
                        onValueChange = {
                            onValueChange(ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_FIRST_NAME, it)
                        }
                    ),
                    pageElement = ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_FIRST_NAME
                ),
                FormInput(
                    label = stringResource(id = R.string.client_email),
                    inputType = TextInput(
                        text = documentClientOrIssuerState.email,
                        placeholder = stringResource(id = R.string.client_email_input),
                        onValueChange = {
                            onValueChange(ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_EMAIL, it)
                        }
                    ),
                    pageElement = ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_EMAIL
                ),

                )
            // Create the UI with list items
            FormUI(
                inputList = inputList,
                localFocusManager = localFocusManager,
                placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                errors = documentClientOrIssuerState.errors
            )
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
                    label = stringResource(id = R.string.client_address1),
                    inputType = TextInput(
                        text = documentClientOrIssuerState.address1,
                        placeholder = stringResource(id = R.string.client_address1_input),
                        onValueChange = {
                            onValueChange(ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS1, it)
                        }
                    ),
                    pageElement = ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS1
                ),
                FormInput(
                    label = stringResource(id = R.string.client_address2),
                    inputType = TextInput(
                        text = documentClientOrIssuerState.address2,
                        placeholder = stringResource(id = R.string.client_address2_input),
                        onValueChange = {
                            onValueChange(ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS2, it)
                        }
                    ),
                    pageElement = ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ADDRESS2
                ),
                FormInput(
                    label = stringResource(id = R.string.client_zip_code),
                    inputType = TextInput(
                        text = documentClientOrIssuerState.zipCode,
                        placeholder = stringResource(id = R.string.client_zip_code_input),
                        onValueChange = {
                            onValueChange(ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ZIP, it)
                        }
                    ),
                    pageElement = ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_ZIP
                ),
                FormInput(
                    label = stringResource(id = R.string.client_city),
                    inputType = TextInput(
                        text = documentClientOrIssuerState.city,
                        placeholder = stringResource(id = R.string.client_city_input),
                        onValueChange = {
                            onValueChange(ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_CITY, it)
                        }
                    ),
                    pageElement = ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_CITY
                ),
                FormInput(
                    label = stringResource(id = R.string.client_phone),
                    inputType = TextInput(
                        text = documentClientOrIssuerState.phone,
                        placeholder = stringResource(id = R.string.client_phone_input),
                        onValueChange = {
                            onValueChange(ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_PHONE, it)
                        }
                    ),
                    pageElement = ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_PHONE
                ))

            // Create the UI with list items
            FormUI(
                inputList = inputList,
                localFocusManager = localFocusManager,
                placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                errors = documentClientOrIssuerState.errors
            )
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
                        text = documentClientOrIssuerState.companyId1Label,
                        onValueChange = {
                            onValueChange(ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION1_LABEL, it)
                        },

                        ),
                    inputType = TextInput(
                        text = documentClientOrIssuerState.companyId1Number,
                        placeholder = stringResource(id = R.string.client_company_identification1_input),
                        onValueChange = {
                            onValueChange(ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION1_VALUE, it)
                        }
                    ),
                    pageElement = ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION1
                ),
                FormInput(
                    label = TextInput(
                        text = documentClientOrIssuerState.companyId2Label,
                        onValueChange = {
                            onValueChange(ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION2_LABEL, it)
                        }
                    ),
                    inputType = TextInput(
                        text = documentClientOrIssuerState.companyId2Number,
                        placeholder = stringResource(id = R.string.client_company_identification2_input),
                        onValueChange = {
                            onValueChange(ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION2_VALUE, it)
                        }
                    ),
                    pageElement = ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_IDENTIFICATION2
                )
            )

            // Create the UI with list items
            FormUI(
                inputList = inputList,
                localFocusManager = localFocusManager,
                placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                errors = documentClientOrIssuerState.errors
            )
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
                    label = stringResource(id = R.string.client_notes),
                    inputType = TextInput(
                        text = documentClientOrIssuerState.notes,
                        placeholder = stringResource(id = R.string.client_notes_input),
                        onValueChange = {
                            onValueChange(ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_NOTES, it)
                        }
                    ),
                    pageElement = ScreenElement.DOCUMENT_CLIENT_OR_ISSUER_NOTES
                ))

            // Create the UI with list items
            FormUI(
                inputList = inputList,
                localFocusManager = localFocusManager,
                placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                errors = documentClientOrIssuerState.errors
            )
        }
    }
}