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
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.theme.textForDocumentsVerySmall
import com.a4a.g8invoicing.ui.theme.textForDocumentsVerySmallBold

@Composable
fun DocumentBasicTemplateFooter(
    uiState: InvoiceState,
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
            Text(
                style = MaterialTheme.typography.textForDocumentsVerySmallBold,
                text = Strings.get(R.string.invoice_pdf_due_date) + " : " + uiState.dueDate
            )
        }
        Row {
            Text(
                modifier = Modifier
                    .padding(bottom = 12.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.textForDocumentsVerySmall,
                text = uiState.footerText.text
            )
        }
    }
}