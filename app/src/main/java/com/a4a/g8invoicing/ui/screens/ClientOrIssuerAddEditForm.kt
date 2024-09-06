package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.shared.FormInput
import com.a4a.g8invoicing.ui.shared.FormUI
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.TextInput
import com.a4a.g8invoicing.ui.theme.ColorBackgroundGrey
import com.itextpdf.layout.element.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import com.a4a.g8invoicing.ui.shared.icons.IconPlus
import com.a4a.g8invoicing.ui.theme.ColorBlueLink
import com.a4a.g8invoicing.ui.theme.ColorDarkGray
import com.a4a.g8invoicing.ui.theme.ColorWhiteCultured
import com.a4a.g8invoicing.ui.theme.callForActions
import com.ninetyninepercent.funfactu.icons.IconArrowBack

@Composable
fun ClientOrIssuerAddEditForm(
    clientOrIssuerUiState: ClientOrIssuerState,
    onValueChange: (ScreenElement, Any) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
) {
    val localFocusManager = LocalFocusManager.current
    var clientAddresses by remember { mutableIntStateOf(1) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .background(ColorBackgroundGrey)
            .fillMaxSize()
            .padding(12.dp)
            .padding(top = 80.dp)
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
                            onValueChange(ScreenElement.CLIENT_OR_ISSUER_NAME, it)
                        }
                    ),
                    pageElement = ScreenElement.CLIENT_OR_ISSUER_NAME
                ),
                FormInput(
                    label = stringResource(id = R.string.client_first_name),
                    inputType = TextInput(
                        text = clientOrIssuerUiState.firstName,
                        placeholder = stringResource(id = R.string.client_first_name_input),
                        onValueChange = {
                            onValueChange(ScreenElement.CLIENT_OR_ISSUER_FIRST_NAME, it)
                        }
                    ),
                    pageElement = ScreenElement.CLIENT_OR_ISSUER_FIRST_NAME
                ),
                FormInput(
                    label = stringResource(id = R.string.client_email),
                    inputType = TextInput(
                        text = clientOrIssuerUiState.email,
                        placeholder = stringResource(id = R.string.client_email_input),
                        onValueChange = {
                            onValueChange(ScreenElement.CLIENT_OR_ISSUER_EMAIL, it)
                        }
                    ),
                    pageElement = ScreenElement.CLIENT_OR_ISSUER_EMAIL
                ),

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

        for (i in 1..clientAddresses) {
            Column(
                modifier = Modifier
                    .background(color = Color.White, shape = RoundedCornerShape(6.dp))
                    .padding(
                        top = 8.dp
                    )

            ) {
                val inputList = mutableListOf(
                    FormInput(
                        label = stringResource(id = R.string.client_address1),
                        inputType = TextInput(
                            text = clientOrIssuerUiState.address1,
                            placeholder = stringResource(id = R.string.client_address1_input),
                            onValueChange = {
                                onValueChange(ScreenElement.CLIENT_OR_ISSUER_ADDRESS1, it)
                            }
                        ),
                        pageElement = ScreenElement.CLIENT_OR_ISSUER_ADDRESS1
                    ),
                    FormInput(
                        label = stringResource(id = R.string.client_address2),
                        inputType = TextInput(
                            text = clientOrIssuerUiState.address2,
                            placeholder = stringResource(id = R.string.client_address2_input),
                            onValueChange = {
                                onValueChange(ScreenElement.CLIENT_OR_ISSUER_ADDRESS2, it)
                            }
                        ),
                        pageElement = ScreenElement.CLIENT_OR_ISSUER_ADDRESS2
                    ),
                    FormInput(
                        label = stringResource(id = R.string.client_zip_code),
                        inputType = TextInput(
                            text = clientOrIssuerUiState.zipCode,
                            placeholder = stringResource(id = R.string.client_zip_code_input),
                            onValueChange = {
                                onValueChange(ScreenElement.CLIENT_OR_ISSUER_ZIP, it)
                            }
                        ),
                        pageElement = ScreenElement.CLIENT_OR_ISSUER_ZIP
                    ),
                    FormInput(
                        label = stringResource(id = R.string.client_city),
                        inputType = TextInput(
                            text = clientOrIssuerUiState.city,
                            placeholder = stringResource(id = R.string.client_city_input),
                            onValueChange = {
                                onValueChange(ScreenElement.CLIENT_OR_ISSUER_CITY, it)
                            }
                        ),
                        pageElement = ScreenElement.CLIENT_OR_ISSUER_CITY
                    ),
                    FormInput(
                        label = stringResource(id = R.string.client_phone),
                        inputType = TextInput(
                            text = clientOrIssuerUiState.phone,
                            placeholder = stringResource(id = R.string.client_phone_input),
                            onValueChange = {
                                onValueChange(ScreenElement.CLIENT_OR_ISSUER_PHONE, it)
                            }
                        ),
                        pageElement = ScreenElement.CLIENT_OR_ISSUER_PHONE
                    )
                )

                if (clientAddresses > 1) {
                    var placeholderId = 0
                    when (i) {
                        1 -> placeholderId = R.string.client_address_title_invoicing_placeholder
                        2 -> placeholderId = R.string.client_address_title_delivery_placeholder
                        3 -> placeholderId = R.string.client_address_title_head_office_placeholder
                    }

                    inputList.add(0, FormInput(
                        label = stringResource(id = R.string.client_address_title),
                        inputType = TextInput(
                            text = clientOrIssuerUiState.addressTitle,
                            placeholder = stringResource(placeholderId),
                            onValueChange = {
                                onValueChange(ScreenElement.CLIENT_OR_ISSUER_ADDRESS_TITLE, it)
                            }
                        ),
                        pageElement = ScreenElement.CLIENT_OR_ISSUER_ADDRESS_TITLE
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
            if (i != clientAddresses) {
                Spacer(Modifier.padding(bottom = 12.dp))
            } else Spacer(Modifier.padding(bottom = 16.dp))
        }

        if (clientAddresses < 3) {
            Row() {
                Spacer(
                    modifier = Modifier
                        .weight(1F)
                )
                FilledIconButton(
                    onClick = { clientAddresses += 1 },
                    modifier = Modifier
                        .padding(end = 0.dp)
                        .size(30.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(contentColor = Color.White)
                ) {
                    Icon(
                        modifier = Modifier
                            .size(20.dp),
                        imageVector = IconPlus,
                        tint = ColorDarkGray,
                        contentDescription = "Add address"
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
                    label = TextInput(
                        text = clientOrIssuerUiState.companyId1Label,
                        onValueChange = {
                            onValueChange(ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION1_LABEL, it)
                        },

                        ),
                    inputType = TextInput(
                        text = clientOrIssuerUiState.companyId1Number,
                        placeholder = stringResource(id = R.string.client_company_identification1_input),
                        onValueChange = {
                            onValueChange(ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION1_VALUE, it)
                        }
                    ),
                    pageElement = ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION1
                ),
                FormInput(
                    label = TextInput(
                        text = clientOrIssuerUiState.companyId2Label,
                        onValueChange = {
                            onValueChange(ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION2_LABEL, it)
                        }
                    ),
                    inputType = TextInput(
                        text = clientOrIssuerUiState.companyId2Number,
                        placeholder = stringResource(id = R.string.client_company_identification2_input),
                        onValueChange = {
                            onValueChange(ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION2_VALUE, it)
                        }
                    ),
                    pageElement = ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION2
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
                            onValueChange(ScreenElement.CLIENT_OR_ISSUER_NOTES, it)
                        }
                    ),
                    pageElement = ScreenElement.CLIENT_OR_ISSUER_NOTES
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