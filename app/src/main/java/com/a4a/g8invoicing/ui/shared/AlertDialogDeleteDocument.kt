package com.a4a.g8invoicing.ui.shared

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import icons.IconDelete

@Composable
fun AlertDialogDeleteDocument(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    isInvoice: Boolean = false,
) {
    AlertDialog(
        icon = {
            Icon(
                imageVector = IconDelete,
                contentDescription = "Delete line item"
            )
        },
        text = {
            val text = if (isInvoice) R.string.alert_dialog_delete_invoice
            else R.string.alert_dialog_delete_general
            Text(
                text = Strings.get(text),
                style = TextStyle(
                    textAlign = TextAlign.Center
                )
            )
        },
        textContentColor = Color.Black,
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(text = stringResource(id = R.string.alert_dialog_delete_cancel))
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(text = stringResource(id = R.string.alert_dialog_delete_confirm))
            }
        }
    )
}