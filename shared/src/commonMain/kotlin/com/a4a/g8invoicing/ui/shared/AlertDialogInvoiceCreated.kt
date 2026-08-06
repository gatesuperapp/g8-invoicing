package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.invoice_created_button
import com.a4a.g8invoicing.shared.resources.invoice_created_title
import com.a4a.g8invoicing.ui.theme.textCta
import org.jetbrains.compose.resources.stringResource

/**
 * "Just-created" confirmation popup with a single CTA that navigates to the
 * new document. Reused after: BL → invoice conversion (default strings),
 * invoice → credit note conversion, invoice → corrected invoice.
 * Pass explicit [titleText] / [buttonText] to override the invoice defaults.
 */
@Composable
fun AlertDialogInvoiceCreated(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    titleText: String = stringResource(Res.string.invoice_created_title),
    buttonText: String = stringResource(Res.string.invoice_created_button),
) {
    AlertDialog(
        text = {
            Text(text = titleText)
        },
        textContentColor = Color.Black,
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        onConfirmation()
                    }
                ) {
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.textCta
                    )
                }
            }
        }
    )
}
