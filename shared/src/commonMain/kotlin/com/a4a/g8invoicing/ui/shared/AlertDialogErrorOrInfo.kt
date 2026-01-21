package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.theme.callForActionsViolet

@Composable
fun AlertDialogErrorOrInfo(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    message: String,
    confirmationText: String,
) {
    AlertDialog(
        text = {
            Text(
                modifier = Modifier.padding(top = 20.dp),
                text =  message,
            )
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
                Text(
                    text = confirmationText,
                    style = MaterialTheme.typography.callForActionsViolet
                )
            }
        }
    )
}
