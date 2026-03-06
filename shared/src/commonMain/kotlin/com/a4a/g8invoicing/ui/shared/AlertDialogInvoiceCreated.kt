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
import com.a4a.g8invoicing.ui.theme.callForActionsViolet
import org.jetbrains.compose.resources.stringResource

@Composable
fun AlertDialogInvoiceCreated(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    AlertDialog(
        text = {
            Text(
                text = stringResource(Res.string.invoice_created_title),
            )
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
                        text = stringResource(Res.string.invoice_created_button),
                        style = MaterialTheme.typography.callForActionsViolet
                    )
                }
            }
        }
    )
}
