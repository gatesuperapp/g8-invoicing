package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.theme.textForDocuments
import com.a4a.g8invoicing.ui.theme.textForDocumentsBold

@Composable
fun DocumentBasicTemplateFooter(
    document: DocumentState,
    onClickElement: (ScreenElement) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp)
            .customCombinedClickable(
                onClick = {
                    onClickElement(ScreenElement.DOCUMENT_FOOTER)
                },
                onLongClick = {
                }
            )
    ) {
        Row(
            modifier = Modifier
                .padding(bottom = 6.dp)
        ) {
            if(document is InvoiceState) {
                Text(
                    style = MaterialTheme.typography.textForDocumentsBold,
                    text = Strings.get(R.string.invoice_pdf_due_date) + " : " + document.dueDate.substringBefore(" ")
                )
            }
        }
        Row {
            Text(
                modifier = Modifier
                    .padding(bottom = 12.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.textForDocuments,
                text = document.footerText.text,
                lineHeight = 10.sp
            )
        }
    }
}