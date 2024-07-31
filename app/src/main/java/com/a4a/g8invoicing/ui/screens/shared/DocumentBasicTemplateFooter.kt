package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.theme.textForDocuments
import com.a4a.g8invoicing.ui.theme.textForDocumentsImportant
import java.math.BigDecimal

@Composable
fun DocumentBasicTemplateFooter(
    uiState: InvoiceState,
    footerArray: List<FooterRow>,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        Row {
            Text(
                style = MaterialTheme.typography.textForDocumentsImportant,
                text = Strings.get(R.string.invoice_pdf_due_date) + " : " + uiState.dueDate
            )
        }
        Row {
            Text(
                style = MaterialTheme.typography.textForDocuments,
                text = Strings.get(R.string.document_default_additional_info_discount)
            )
        }
        Row {
            Text(
                style = MaterialTheme.typography.textForDocuments,
                text = Strings.get(R.string.document_default_additional_info_penalties)
            )
        }
        Spacer(modifier = Modifier.height(30.dp))
        Row {
            Text(
                style = MaterialTheme.typography.textForDocuments,
                text = Strings.get(R.string.document_default_additional_info_bank)
            )
        }
    }
}