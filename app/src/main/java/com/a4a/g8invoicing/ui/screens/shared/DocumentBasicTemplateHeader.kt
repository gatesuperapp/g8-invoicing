package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.border
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.CreditNoteState
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DocumentState
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
                style = MaterialTheme.typography.titleForDocuments,
                text = stringResource(
                    id = if (document is DeliveryNoteState) R.string.delivery_note_number
                    else if (document is CreditNoteState) R.string.credit_note_number
                    else R.string.invoice_number
                ) + document.documentNumber.text
            )

            Spacer(
                modifier = Modifier
                    .padding(bottom = 6.dp)
            )

            Text(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .getBorder(ScreenElement.DOCUMENT_DATE, selectedItem)
                    .customCombinedClickable(
                        onClick = {
                            onClickElement(ScreenElement.DOCUMENT_DATE)
                        },
                        onLongClick = {
                        }
                    ),
                style = MaterialTheme.typography.subTitleForDocuments,
                text = stringResource(id = R.string.document_date) + " : " + document.documentDate.substringBefore(
                    " "
                )
            )
        }
    }
    val client = document.documentClient

    // ROW 1: NOTHING ------- CLIENT ADDRESS TITLE 1
    Row {
        Spacer(
            modifier = Modifier
                .fillMaxWidth(0.5f)
        )
        val addressTitle =
            if (client?.addresses?.size == 1) stringResource(id = R.string.document_recipient)
            else client?.addresses?.getOrNull(0)?.addressTitle?.text
                ?: ""
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .padding(bottom = 2.dp),
                style = MaterialTheme.typography.textForDocumentsSecondary,
                text = addressTitle
            )
        }
    }

    // ROW 2 : ISSUER --------- CLIENT ADDRESS 1
    Row() {
        val numberOfAddresses = document.documentClient?.addresses?.size ?: 0
        Column(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .getBorder(ScreenElement.DOCUMENT_ISSUER, selectedItem)
                .customCombinedClickable(
                    onClick = {
                        onClickElement(ScreenElement.DOCUMENT_ISSUER)
                    },
                    onLongClick = {
                    }
                )
        ) {
            document.documentIssuer?.let {
                DocumentBasicTemplateClientOrIssuer(it)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            document.documentClient?.let {
                DocumentClientRectangleAndContent(
                    it,
                    onClickElement,
                    selectedItem,
                    addressIndex = 0,
                    displayAllInfo = true
                )
            }
        }
    }

    document.documentClient?.addresses?.let { addresses ->
        if (addresses.size > 1) {
            // ROW 3:  CLIENT ADDRESS TITLE 2 ------- TTITLE 3
            Row {
                for (i in 1..<addresses.size) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(if (i == 1) 0.5f else 1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(bottom = 2.dp),
                            style = MaterialTheme.typography.textForDocumentsSecondary,
                            text = addresses.getOrNull(i)?.addressTitle?.text ?: ""
                        )
                    }
                }
            }

            // ROW 4:  CLIENT ADDRESS 2 ----------ADDRESS 3
            Row {
                for (i in 1..<addresses.size) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(if (i == 1) 0.5f else 1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        document.documentClient?.let {
                            DocumentClientRectangleAndContent(
                                it,
                                onClickElement,
                                selectedItem,
                                addressIndex = i,
                                displayAllInfo = false
                            )
                        }
                    }
                    if (i == 1) Spacer(modifier = Modifier.width(2.dp))
                }
            }
        }
    }
}

@Composable
fun DocumentClientRectangleAndContent(
    documentClient: ClientOrIssuerState,
    onClickElement: (ScreenElement) -> Unit,
    selectedItem: ScreenElement?,
    addressIndex: Int,
    displayAllInfo: Boolean,
) {

    Column(
        modifier = Modifier
            .padding(bottom = 6.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var customModifier2 = Modifier
            .getBorder(ScreenElement.DOCUMENT_CLIENT, selectedItem)
            .customCombinedClickable(
                onClick = {
                    onClickElement(ScreenElement.DOCUMENT_CLIENT)
                },
                onLongClick = {
                }
            )
            .border(
                1.dp,
                SolidColor(Color.LightGray),
                shape = RoundedCornerShape(15.dp)
            )
            .padding(top = 8.dp)
            .fillMaxWidth()
        customModifier2 = if (addressIndex == 0)
            customModifier2.then(
                Modifier
                    .padding(bottom = 8.dp)
            )
        else
            customModifier2.then(
                Modifier
                    .padding(bottom = 4.dp)
            )

        Column(
            modifier = customModifier2,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DocumentBasicTemplateClientOrIssuer(
                documentClient,
                addressIndex = addressIndex,
                displayAllInfo = displayAllInfo
            )
        }
    }
}