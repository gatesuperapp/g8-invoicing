package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.invoice_pdf_due_date
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.theme.textForDocuments
import com.a4a.g8invoicing.ui.theme.textForDocumentsBold
import org.jetbrains.compose.resources.stringResource

@Composable
fun DocumentBasicTemplateFooter(
    document: DocumentState,
    onClickElement: (ScreenElement) -> Unit,
    labels: Map<String, String>? = null,
) {
    // The watermark string is frozen at document creation in the DB (watermark_text
    // column). Display whatever is stored — toggling the module later doesn't change
    // existing docs. null/blank = no watermark on this doc.
    val watermark = document.watermarkText?.takeIf { it.isNotBlank() }
    val uriHandler = LocalUriHandler.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
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
                    text = documentLabel(labels, "invoice_pdf_due_date", Res.string.invoice_pdf_due_date) + document.dueDate.substringBefore(" ")
                )
            }
        }
        Row {
            Text(
                modifier = Modifier
                    .padding(bottom = 6.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.textForDocuments,
                text = document.footerText.text,
                lineHeight = 10.sp
            )
        }
        if (watermark != null) {
            // Tiny watermark, smaller than the address text. Whole line is clickable →
            // opens the website. Underlined to signal interactivity.
            Text(
                modifier = Modifier
                    .clickable { uriHandler.openUri("https://the-gate.fr") }
                    .padding(top = 4.dp, bottom = 6.dp),
                textAlign = TextAlign.Center,
                text = watermark,
                color = Color(0xFF888888),
                fontSize = 5.sp,
                lineHeight = 7.sp,
                textDecoration = TextDecoration.Underline,
            )
        }
    }
}
