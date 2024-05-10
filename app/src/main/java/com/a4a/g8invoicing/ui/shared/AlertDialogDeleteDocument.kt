package com.a4a.g8invoicing.ui.shared

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.a4a.g8invoicing.R
import icons.IconDelete

@Composable
fun AlertDialogDeleteDocument(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    AlertDialog(
        icon = {
            Icon(
                imageVector = IconDelete,
                contentDescription = "Delete line item"
            )
        },
        text = {
            Text(text = stringResource(id =R.string.alert_dialog_delete))
        },
        textContentColor = Color.Black,
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(text = stringResource(id =R.string.alert_dialog_delete_confirm))
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(text = stringResource(id =R.string.alert_dialog_delete_cancel))
            }
        }
    )
}