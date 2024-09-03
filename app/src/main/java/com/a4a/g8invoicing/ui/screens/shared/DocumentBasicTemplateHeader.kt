package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.CreditNoteState
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.theme.subTitleForDocuments
import com.a4a.g8invoicing.ui.theme.textForDocumentsSecondary
import com.a4a.g8invoicing.ui.theme.titleForDocuments

@Composable
fun DocumentBasicTemplateHeader(
    document: DocumentState,
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
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleForDocuments,
                text = stringResource(
                    id = if (document is DeliveryNoteState) R.string.delivery_note_number
                    else if(document is CreditNoteState) R.string.delivery_note_number
                        else R.string.invoice_number
                ) + " " + document.documentNumber.text
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
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.subTitleForDocuments,
                text = stringResource(id = R.string.document_date) + " : " + document.documentDate.substringBefore(
                    " "
                )
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
                .padding(end = 20.dp, bottom = 20.dp)
                .weight(1f)
                .fillMaxWidth(0.3f)
        ) {
            document.documentIssuer?.let {
                DocumentBasicTemplateClientOrIssuer(it)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(bottom = 20.dp),
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
                document.documentClient?.let {
                    DocumentBasicTemplateClientOrIssuer(it)
                }
            }
        }
    }
}