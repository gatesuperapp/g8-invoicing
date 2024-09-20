package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.theme.ColorBlueLink
import icons.IconDelete

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
                imageVector = IconDelete,
                contentDescription = "Delete line item"
            )
        },
        text = {
            if (isInvoice) DeleteInvoiceLink(uriHandler)
            else
                Text(
                    text = Strings.get(R.string.alert_dialog_delete_general),
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

@Composable
fun DeleteInvoiceLink(uriHandler: UriHandler) {
    val annotatedString = buildAnnotatedString {
        append(Strings.get(R.string.alert_dialog_delete_invoice_1) + " ")
        pushStringAnnotation(
            tag = "link",
            annotation = Strings.get(R.string.alert_dialog_delete_url)
        )
        withStyle(style = SpanStyle(color = ColorBlueLink)) {
            append(Strings.get(R.string.alert_dialog_delete_invoice_2) + " ")
        }
       // pop()
        append(Strings.get(R.string.alert_dialog_delete_invoice_3))
       // pop()
    }

    ClickableText(
        modifier = Modifier.padding(top = 20.dp, start = 40.dp, end = 40.dp, bottom = 20.dp),
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "link", start = offset, end = offset)
                .firstOrNull()?.let {
                    uriHandler.openUri(it.item)
                }
        })
}