package com.a4a.g8invoicing.ui.screens.shared

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.theme.textForDocuments
import com.a4a.g8invoicing.ui.theme.textForDocumentsSecondary

@Composable
fun DocumentBasicTemplateHeader(
    uiState: DocumentState,
    onClickElement: (ScreenElement) -> Unit,
    selectedItem: ScreenElement?,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .customCombinedClickable(
                onClick = {
                    onClickElement(ScreenElement.DOCUMENT_HEADER)
                },
                onLongClick = {
                }
            )
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
                text = stringResource(
                    id = if (uiState is DeliveryNoteState) R.string.delivery_note_number
                    else R.string.invoice_number
                ) + " " + uiState.documentNumber.text
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
                text = stringResource(id = R.string.document_date) + " : " + uiState.documentDate
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
                .padding(end = 20.dp)
                .weight(1f)
                .fillMaxWidth(0.3f)
        ) {
            BuildClientOrIssuerInTemplate(uiState.documentIssuer)
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
                text = stringResource(id = R.string.document_recipient)
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
                BuildClientOrIssuerInTemplate(uiState.documentClient)
            }
        }
    }
}