package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.data.ClientOrIssuerEditable
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.navigation.actionDone
import com.a4a.g8invoicing.ui.shared.FormInput
import com.a4a.g8invoicing.ui.shared.FormUI
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.TextInput

@Composable
fun ClientOrIssuerAddEdit(
    navController: NavController,
    clientOrIssuer: ClientOrIssuerEditable,
    isNew: Boolean,
    onValueChange: (ScreenElement, Any) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    onClickDone: (Boolean) -> Unit,
    onClickBack: () -> Unit,
    onClickForward: (ScreenElement) -> Unit,
) {
    Scaffold(
        topBar = {
            ClientAddEditTopBar(
                isNewClient = isNew,
                navController = navController,
                onClickDone = { isNewClient ->
                    onClickDone(isNewClient)
                },
                onClickBackArrow = onClickBack
            )
        }
    ) {
        it

        val localFocusManager = LocalFocusManager.current

        Column(
            modifier = Modifier
                .background(Color.LightGray.copy(alpha = 0.4f))
                .fillMaxSize()
                .imePadding()
                .padding(
                    top = 80.dp,
                )
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        localFocusManager.clearFocus()
                        // So when we click outside a text input,
                        // selection is cleared
                    })
                }

        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            )
            {
                Column(
                    modifier = Modifier
                        .background(color = Color.White, shape = RoundedCornerShape(6.dp))
                        .fillMaxWidth()
                        .padding(
                            //start = 20.dp,
                            top = 8.dp,
                           // end = 20.dp,
                            bottom = 8.dp
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
                        ),
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
                        ),
                    )
                    // Create the UI with list items
                    FormUI(
                        inputList = inputList,
                        localFocusManager = localFocusManager,
                        onClickForward = onClickForward,
                        placeCursorAtTheEndOfText = placeCursorAtTheEndOfText
                    )
                }
            }
        }
    }
}

@Composable
private fun ClientAddEditTopBar(
    isNewClient: Boolean,
    navController: NavController,
    onClickDone: (Boolean) -> Unit,
    onClickBackArrow: () -> Unit,
) {
    TopBar(
        title = if (isNewClient) {
            R.string.appbar_title_add_new
        } else {
            R.string.appbar_title_info
        },
        actionDone(
            onClick = { onClickDone(isNewClient) }
        ),
        navController = navController,
        onClickBackArrow = onClickBackArrow
    )
}
