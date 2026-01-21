package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.data.util.DefaultStrings
import com.a4a.g8invoicing.ui.theme.ColorVioletLink

@Composable
fun AlertDialogDeleteDocument(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    isInvoice: Boolean = false,
) {
    val uriHandler = LocalUriHandler.current

    AlertDialog(
        icon = {
            Icon(
                imageVector = Icons.Outlined.Cancel,
                contentDescription = "Delete line item"
            )
        },
        text = {
            if (isInvoice) DeleteInvoiceLink(uriHandler)
            else
                Text(
                    text = DefaultStrings.ALERT_DIALOG_DELETE_GENERAL,
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
                Text(text = DefaultStrings.ALERT_DIALOG_DELETE_CONFIRM)
            }
        }
    )
}

@Composable
fun DeleteInvoiceLink(uriHandler: UriHandler) {
    val annotatedString = buildAnnotatedString {
        append(DefaultStrings.ALERT_DIALOG_DELETE_INVOICE_1 + " ")
        pushStringAnnotation(
            tag = "link",
            annotation = DefaultStrings.ALERT_DIALOG_DELETE_URL
        )
        withStyle(style = SpanStyle(color = ColorVioletLink)) {
            append(DefaultStrings.ALERT_DIALOG_DELETE_INVOICE_2)
        }
        // pop()
        append(DefaultStrings.ALERT_DIALOG_DELETE_INVOICE_3)
        // pop()
    }

    ClickableText(
        modifier = Modifier.padding(top = 20.dp, start = 40.dp, end = 40.dp),
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "link", start = offset, end = offset)
                .firstOrNull()?.let {
                    uriHandler.openUri(it.item)
                }
        })
}
