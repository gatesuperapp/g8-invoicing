package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.data.ClientOrIssuerState
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.theme.textForDocuments
import com.a4a.g8invoicing.ui.theme.textForDocumentsSecondary

@Composable
fun DeliveryNoteBasicTemplateHeader(
    uiState: DeliveryNoteState,
    onClickElement: (ScreenElement) -> Unit,
    selectedItem: ScreenElement?,
) {
    Row(
        Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier
                    .getBorder(ScreenElement.DOCUMENT_NUMBER, selectedItem)
                    .customCombinedClickable(
                        onClick = {
                            onClickElement(ScreenElement.DOCUMENT_NUMBER)
                        },
                        onLongClick = {
                        }
                    ),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.textForDocuments,
                text = stringResource(id = R.string.delivery_note_number) + " " + (uiState.number?.text
                    ?: stringResource(id = R.string.delivery_note_default_number))
            )

            Spacer(
                modifier = Modifier
                    .padding(bottom = 6.dp)
            )

            Text(
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .getBorder(ScreenElement.DOCUMENT_DATE, selectedItem)
                    .customCombinedClickable(
                        onClick = {
                            onClickElement(ScreenElement.DOCUMENT_DATE)
                        },
                        onLongClick = {
                        }
                    ),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.textForDocuments,
                text = stringResource(id = R.string.delivery_note_date) + " " + (uiState.deliveryDate
                    ?: stringResource(id = R.string.delivery_note_default_date))
            )
        }
    }
    Row(
        Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .getBorder(ScreenElement.DOCUMENT_ISSUER, selectedItem)
                .customCombinedClickable(
                    onClick = {
                        onClickElement(ScreenElement.DOCUMENT_ISSUER)
                    },
                    onLongClick = {
                    }
                )
                .padding(top = 10.dp)
                .weight(1f)
                .fillMaxWidth(0.3f)
        ) {
            BuildClientOrIssuerInTemplate(uiState.issuer ?: fakeIssuer())
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(0.5f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .padding(bottom = 2.dp),
                style = MaterialTheme.typography.textForDocumentsSecondary,
                text = stringResource(id = R.string.delivery_note_recipient)
            )

            Column(
                modifier = Modifier
                    .getBorder(ScreenElement.DOCUMENT_CLIENT, selectedItem)
                    .customCombinedClickable(
                        onClick = {
                            onClickElement(ScreenElement.DOCUMENT_CLIENT)
                        },
                        onLongClick = {
                        }
                    )
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        SolidColor(Color.LightGray),
                        shape = RoundedCornerShape(15.dp)
                    )
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BuildClientOrIssuerInTemplate(uiState.client)
            }
        }
    }
}

@Composable
private fun fakeClient() =
    ClientOrIssuerState(
        id = null,
        firstName = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_client_firstName)),
        name = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_client_name)),
        address1 = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_client_address1)),
        address2 = null,
        zipCode = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_client_zipCode)),
        city = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_client_city)),
        phone = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_client_phone)),
        email = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_client_email)),
        notes = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_client_notes)),
        companyId1Label = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_issuer_company_label1)),
        companyId1Number = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_issuer_company_number1)),
        companyId2Label = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_issuer_company_label2)),
        companyId2Number = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_issuer_company_number2)),
    )

@Composable
private fun fakeIssuer() =
    ClientOrIssuerState(
        // Fill with dummy values to show how it looks
        id = null,
        firstName = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_issuer_firstName)),
        name = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_issuer_name)),
        address1 = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_issuer_address1)),
        address2 = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_issuer_address2)),
        zipCode = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_issuer_zipCode)),
        city = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_issuer_city)),
        phone = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_issuer_phone)),
        email = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_issuer_email)),
        notes = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_issuer_company_notes)),
        companyId1Label = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_issuer_company_label1)),
        companyId1Number = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_issuer_company_number1)),
        companyId2Label = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_issuer_company_label2)),
        companyId2Number = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_issuer_company_number2)),
    )
