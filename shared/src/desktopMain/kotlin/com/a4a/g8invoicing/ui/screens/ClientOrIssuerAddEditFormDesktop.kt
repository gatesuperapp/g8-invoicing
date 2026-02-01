package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.client_address1
import com.a4a.g8invoicing.shared.resources.client_address1_input
import com.a4a.g8invoicing.shared.resources.client_address2_input
import com.a4a.g8invoicing.shared.resources.client_city
import com.a4a.g8invoicing.shared.resources.client_city_input
import com.a4a.g8invoicing.shared.resources.client_edit_title
import com.a4a.g8invoicing.shared.resources.client_email
import com.a4a.g8invoicing.shared.resources.client_email_input
import com.a4a.g8invoicing.shared.resources.client_first_name
import com.a4a.g8invoicing.shared.resources.client_first_name_input
import com.a4a.g8invoicing.shared.resources.client_form_cancel
import com.a4a.g8invoicing.shared.resources.client_form_validate
import com.a4a.g8invoicing.shared.resources.client_add_title
import com.a4a.g8invoicing.shared.resources.client_name
import com.a4a.g8invoicing.shared.resources.client_name_input
import com.a4a.g8invoicing.shared.resources.client_notes
import com.a4a.g8invoicing.shared.resources.client_notes_input
import com.a4a.g8invoicing.shared.resources.client_phone
import com.a4a.g8invoicing.shared.resources.client_phone_input
import com.a4a.g8invoicing.shared.resources.client_section_address
import com.a4a.g8invoicing.shared.resources.client_section_complement
import com.a4a.g8invoicing.shared.resources.client_section_identity
import com.a4a.g8invoicing.shared.resources.client_section_legal
import com.a4a.g8invoicing.shared.resources.client_zip_code
import com.a4a.g8invoicing.shared.resources.client_zip_code_input
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.theme.ColorBackgroundGrey
import com.a4a.g8invoicing.ui.theme.ColorVioletLight
import org.jetbrains.compose.resources.stringResource

@Composable
fun ClientOrIssuerAddEditFormDesktop(
    clientOrIssuerUiState: ClientOrIssuerState,
    onValueChange: (ScreenElement, Any) -> Unit,
    onClickDone: () -> Unit,
    onClickBack: () -> Unit,
    isNewClient: Boolean = true,
) {
    val localFocusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Strings
    val titleText = if (isNewClient) stringResource(Res.string.client_add_title) else stringResource(Res.string.client_edit_title)
    val cancelText = stringResource(Res.string.client_form_cancel)
    val validateText = stringResource(Res.string.client_form_validate)
    val sectionIdentity = stringResource(Res.string.client_section_identity)
    val sectionAddress = stringResource(Res.string.client_section_address)
    val sectionLegal = stringResource(Res.string.client_section_legal)
    val complementLabel = stringResource(Res.string.client_section_complement)

    val nameLabel = stringResource(Res.string.client_name)
    val namePlaceholder = stringResource(Res.string.client_name_input)
    val firstNameLabel = stringResource(Res.string.client_first_name)
    val firstNamePlaceholder = stringResource(Res.string.client_first_name_input)
    val emailLabel = stringResource(Res.string.client_email)
    val emailPlaceholder = stringResource(Res.string.client_email_input)
    val phoneLabel = stringResource(Res.string.client_phone)
    val phonePlaceholder = stringResource(Res.string.client_phone_input)
    val addressLabel = stringResource(Res.string.client_address1)
    val addressPlaceholder = stringResource(Res.string.client_address1_input)
    val complementPlaceholder = stringResource(Res.string.client_address2_input)
    val zipCodeLabel = stringResource(Res.string.client_zip_code)
    val zipCodePlaceholder = stringResource(Res.string.client_zip_code_input)
    val cityLabel = stringResource(Res.string.client_city)
    val cityPlaceholder = stringResource(Res.string.client_city_input)
    val notesLabel = stringResource(Res.string.client_notes)
    val notesPlaceholder = stringResource(Res.string.client_notes_input)

    // Get the first address
    val address = clientOrIssuerUiState.addresses?.getOrNull(0)

    // Build subtitle from first name + name
    val subtitle = buildString {
        clientOrIssuerUiState.firstName?.text?.takeIf { it.isNotEmpty() }?.let { append(it) }
        clientOrIssuerUiState.name.text.takeIf { it.isNotEmpty() }?.let {
            if (isNotEmpty()) append(" ")
            append(it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackgroundGrey)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    localFocusManager.clearFocus()
                })
            }
    ) {
        // Scrollable content area with scrollbar
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(vertical = 32.dp)
                    .padding(end = 12.dp) // Space for scrollbar
                    .imePadding(),
                contentAlignment = Alignment.TopCenter
            ) {
                Surface(
                    modifier = Modifier
                        .widthIn(max = 700.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp)
                    ) {
                        // Header
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (subtitle.isNotEmpty()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 24.dp),
                            color = Color.LightGray.copy(alpha = 0.5f)
                        )

                        // IDENTITÉ Section
                        SectionTitle(sectionIdentity)
                        Spacer(Modifier.height(16.dp))

                        // Nom / Prénom
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            DesktopFormField(
                                label = nameLabel,
                                value = clientOrIssuerUiState.name,
                                placeholder = namePlaceholder,
                                onValueChange = { onValueChange(ScreenElement.CLIENT_OR_ISSUER_NAME, it) },
                                modifier = Modifier.weight(1f)
                            )
                            DesktopFormField(
                                label = firstNameLabel,
                                value = clientOrIssuerUiState.firstName,
                                placeholder = firstNamePlaceholder,
                                onValueChange = { onValueChange(ScreenElement.CLIENT_OR_ISSUER_FIRST_NAME, it) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // E-mail / Téléphone
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            DesktopFormField(
                                label = emailLabel,
                                value = clientOrIssuerUiState.emails?.firstOrNull()?.email,
                                placeholder = emailPlaceholder,
                                onValueChange = { onValueChange(ScreenElement.CLIENT_OR_ISSUER_EMAIL_1, it) },
                                keyboardType = KeyboardType.Email,
                                modifier = Modifier.weight(1f)
                            )
                            DesktopFormField(
                                label = phoneLabel,
                                value = clientOrIssuerUiState.phone,
                                placeholder = phonePlaceholder,
                                onValueChange = { onValueChange(ScreenElement.CLIENT_OR_ISSUER_PHONE, it) },
                                keyboardType = KeyboardType.Phone,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(32.dp))

                        // ADRESSE Section
                        SectionTitle(sectionAddress)
                        Spacer(Modifier.height(16.dp))

                        // Adresse (full width)
                        DesktopFormField(
                            label = addressLabel,
                            value = address?.addressLine1,
                            placeholder = addressPlaceholder,
                            onValueChange = { onValueChange(ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_1_1, it) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        // Complément (full width)
                        DesktopFormField(
                            label = complementLabel,
                            value = address?.addressLine2,
                            placeholder = complementPlaceholder,
                            onValueChange = { onValueChange(ScreenElement.CLIENT_OR_ISSUER_ADDRESS_LINE_2_1, it) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        // Code postal / Ville
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            DesktopFormField(
                                label = zipCodeLabel,
                                value = address?.zipCode,
                                placeholder = zipCodePlaceholder,
                                onValueChange = { onValueChange(ScreenElement.CLIENT_OR_ISSUER_ZIP_1, it) },
                                keyboardType = KeyboardType.Number,
                                modifier = Modifier.weight(0.35f)
                            )
                            DesktopFormField(
                                label = cityLabel,
                                value = address?.city,
                                placeholder = cityPlaceholder,
                                onValueChange = { onValueChange(ScreenElement.CLIENT_OR_ISSUER_CITY_1, it) },
                                modifier = Modifier.weight(0.65f)
                            )
                        }

                        Spacer(Modifier.height(32.dp))

                        // INFORMATIONS LÉGALES Section
                        SectionTitle(sectionLegal)
                        Spacer(Modifier.height(16.dp))

                        // SIRET (label editable + value)
                        DesktopFormFieldWithEditableLabel(
                            labelValue = clientOrIssuerUiState.companyId1Label,
                            labelPlaceholder = "SIRET",
                            onLabelChange = { onValueChange(ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION1_LABEL, it) },
                            value = clientOrIssuerUiState.companyId1Number,
                            placeholder = "123456789 12345",
                            onValueChange = { onValueChange(ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION1_VALUE, it) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        // TVA (label editable + value)
                        DesktopFormFieldWithEditableLabel(
                            labelValue = clientOrIssuerUiState.companyId2Label,
                            labelPlaceholder = "N° TVA",
                            onLabelChange = { onValueChange(ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION2_LABEL, it) },
                            value = clientOrIssuerUiState.companyId2Number,
                            placeholder = "123456789",
                            onValueChange = { onValueChange(ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION2_VALUE, it) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        // RCS (label editable + value)
                        DesktopFormFieldWithEditableLabel(
                            labelValue = clientOrIssuerUiState.companyId3Label,
                            labelPlaceholder = "RCS",
                            onLabelChange = { onValueChange(ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION3_LABEL, it) },
                            value = clientOrIssuerUiState.companyId3Number,
                            placeholder = "123456789",
                            onValueChange = { onValueChange(ScreenElement.CLIENT_OR_ISSUER_IDENTIFICATION3_VALUE, it) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(32.dp))

                        // NOTES Section
                        SectionTitle("NOTES")
                        Spacer(Modifier.height(16.dp))

                        DesktopFormField(
                            label = notesLabel,
                            value = clientOrIssuerUiState.notes,
                            placeholder = notesPlaceholder,
                            onValueChange = { onValueChange(ScreenElement.CLIENT_OR_ISSUER_NOTES, it) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false,
                            minLines = 3
                        )
                    }
                }
            }

            // Vertical scrollbar
            VerticalScrollbar(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(end = 4.dp, top = 4.dp, bottom = 4.dp),
                adapter = rememberScrollbarAdapter(scrollState),
                style = LocalScrollbarStyle.current.copy(
                    thickness = 8.dp,
                    hoverColor = ColorVioletLight.copy(alpha = 0.5f),
                    unhoverColor = Color.LightGray.copy(alpha = 0.5f)
                )
            )
        }

        // Fixed footer with buttons
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onClickBack,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color.Gray),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.DarkGray
                    )
                ) {
                    Text(
                        text = cancelText,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.padding(8.dp))

                Button(
                    onClick = onClickDone,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorVioletLight,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = validateText,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = ColorVioletLight,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp
    )
}

@Composable
private fun DesktopFormField(
    label: String,
    value: TextFieldValue?,
    placeholder: String,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value ?: TextFieldValue(""),
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color.LightGray
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = ColorVioletLight,
                cursorColor = ColorVioletLight
            ),
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = keyboardType
            )
        )
    }
}

@Composable
private fun DesktopFormFieldWithEditableLabel(
    labelValue: TextFieldValue?,
    labelPlaceholder: String,
    onLabelChange: (TextFieldValue) -> Unit,
    value: TextFieldValue?,
    placeholder: String,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Editable label field
        Column(modifier = Modifier.weight(0.35f)) {
            Text(
                text = "Intitulé",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = labelValue ?: TextFieldValue(""),
                onValueChange = onLabelChange,
                placeholder = {
                    Text(
                        text = labelPlaceholder,
                        color = Color.LightGray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = ColorVioletLight,
                    cursorColor = ColorVioletLight
                ),
                singleLine = true
            )
        }

        // Value field
        Column(modifier = Modifier.weight(0.65f)) {
            Text(
                text = "Valeur",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = value ?: TextFieldValue(""),
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        text = placeholder,
                        color = Color.LightGray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = ColorVioletLight,
                    cursorColor = ColorVioletLight
                ),
                singleLine = true
            )
        }
    }
}
