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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.data.ClientOrIssuerState
import com.a4a.g8invoicing.ui.shared.FormInput
import com.a4a.g8invoicing.ui.shared.FormUI
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.TextInput
import com.a4a.g8invoicing.ui.theme.ColorBackgroundGrey

@Composable
fun ClientOrIssuerAddEditForm(
    clientOrIssuer: ClientOrIssuerState,
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
                        text = clientOrIssuer.name,
                        placeholder = stringResource(id = R.string.client_name_input),
                        onValueChange = {
                            onValueChange(ScreenElement.CLIENT_NAME, it)
                        }
                    ),
                    pageElement = ScreenElement.CLIENT_NAME
                ),
                FormInput(
                    label = stringResource(id = R.string.client_first_name),
                    inputType = TextInput(
                        text = clientOrIssuer.firstName,
                        placeholder = stringResource(id = R.string.client_first_name_input),
                        onValueChange = {
                            onValueChange(ScreenElement.CLIENT_FIRST_NAME, it)
                        }
                    ),
                    pageElement = ScreenElement.CLIENT_FIRST_NAME
                ),
                FormInput(
                    label = stringResource(id = R.string.client_email),
                    inputType = TextInput(
                        text = clientOrIssuer.email,
                        placeholder = stringResource(id = R.string.client_email_input),
                        onValueChange = {
                            onValueChange(ScreenElement.CLIENT_EMAIL, it)
                        }
                    ),
                    pageElement = ScreenElement.CLIENT_EMAIL
                ),

                )
            // Create the UI with list items
            FormUI(
                inputList = inputList,
                localFocusManager = localFocusManager,
                placeCursorAtTheEndOfText = placeCursorAtTheEndOfText
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
                        text = clientOrIssuer.address1,
                        placeholder = stringResource(id = R.string.client_address1_input),
                        onValueChange = {
                            onValueChange(ScreenElement.CLIENT_ADDRESS1, it)
                        }
                    ),
                    pageElement = ScreenElement.CLIENT_ADDRESS1
                ),
                FormInput(
                    label = stringResource(id = R.string.client_address2),
                    inputType = TextInput(
                        text = clientOrIssuer.address2,
                        placeholder = stringResource(id = R.string.client_address2_input),
                        onValueChange = {
                            onValueChange(ScreenElement.CLIENT_ADDRESS2, it)
                        }
                    ),
                    pageElement = ScreenElement.CLIENT_ADDRESS2
                ),
                FormInput(
                    label = stringResource(id = R.string.client_zip_code),
                    inputType = TextInput(
                        text = clientOrIssuer.zipCode,
                        placeholder = stringResource(id = R.string.client_zip_code_input),
                        onValueChange = {
                            onValueChange(ScreenElement.CLIENT_ZIP, it)
                        }
                    ),
                    pageElement = ScreenElement.CLIENT_ZIP
                ),
                FormInput(
                    label = stringResource(id = R.string.client_city),
                    inputType = TextInput(
                        text = clientOrIssuer.city,
                        placeholder = stringResource(id = R.string.client_city_input),
                        onValueChange = {
                            onValueChange(ScreenElement.CLIENT_CITY, it)
                        }
                    ),
                    pageElement = ScreenElement.CLIENT_CITY
                ),
                FormInput(
                    label = stringResource(id = R.string.client_phone),
                    inputType = TextInput(
                        text = clientOrIssuer.phone,
                        placeholder = stringResource(id = R.string.client_phone_input),
                        onValueChange = {
                            onValueChange(ScreenElement.CLIENT_PHONE, it)
                        }
                    ),
                    pageElement = ScreenElement.CLIENT_PHONE
                ))

            // Create the UI with list items
            FormUI(
                inputList = inputList,
                localFocusManager = localFocusManager,
                placeCursorAtTheEndOfText = placeCursorAtTheEndOfText
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
                        text = clientOrIssuer.companyData?.first()?.label ?: TextFieldValue(
                            stringResource(id = R.string.client_company_identification1)
                        ),
                        onValueChange = {
                            onValueChange(ScreenElement.CLIENT_IDENTIFICATION1_LABEL, it)
                        },

                    ),
                    inputType = TextInput(
                        text = clientOrIssuer.companyData?.first()?.number,
                        placeholder = stringResource(id = R.string.client_company_identification1_input),
                        onValueChange = {
                            onValueChange(ScreenElement.CLIENT_IDENTIFICATION1_VALUE, it)
                        }
                    ),
                    pageElement = ScreenElement.CLIENT_IDENTIFICATION1
                ),
                FormInput(
                    label = TextInput(
                        text = clientOrIssuer.companyData?.get(1)?.label ?: TextFieldValue(
                            stringResource(id = R.string.client_company_identification2)
                        ),
                        onValueChange = {
                            onValueChange(ScreenElement.CLIENT_IDENTIFICATION2_LABEL, it)
                        }
                    ),
                    inputType = TextInput(
                        text = clientOrIssuer.companyData?.get(1)?.number,
                        placeholder = stringResource(id = R.string.client_company_identification2_input),
                        onValueChange = {
                            onValueChange(ScreenElement.CLIENT_IDENTIFICATION2_VALUE, it)
                        }
                    ),
                    pageElement = ScreenElement.CLIENT_IDENTIFICATION2
                )
            )

            // Create the UI with list items
            FormUI(
                inputList = inputList,
                localFocusManager = localFocusManager,
                placeCursorAtTheEndOfText = placeCursorAtTheEndOfText
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
                        text = clientOrIssuer.notes,
                        placeholder = stringResource(id = R.string.client_notes_input),
                        onValueChange = {
                            onValueChange(ScreenElement.CLIENT_NOTES, it)
                        }
                    ),
                    pageElement = ScreenElement.CLIENT_NOTES
                ))

            // Create the UI with list items
            FormUI(
                inputList = inputList,
                localFocusManager = localFocusManager,
                placeCursorAtTheEndOfText = placeCursorAtTheEndOfText
            )
        }
    }
}