package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.alert_dialog_cancel
import com.a4a.g8invoicing.shared.resources.alert_dialog_delete_confirm
import com.a4a.g8invoicing.shared.resources.alert_dialog_delete_general
import com.a4a.g8invoicing.shared.resources.alert_dialog_delete_invoice_1
import com.a4a.g8invoicing.shared.resources.alert_dialog_delete_invoice_2
import com.a4a.g8invoicing.shared.resources.alert_dialog_delete_invoice_3
import com.a4a.g8invoicing.shared.resources.alert_dialog_delete_url
import com.a4a.g8invoicing.ui.theme.ColorVioletLink
import org.jetbrains.compose.resources.stringResource

@Composable
fun AlertDialogDeleteDocument(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    isInvoice: Boolean = false,
) {
    val uriHandler = LocalUriHandler.current

    AlertDialog(
        text = {
            if (isInvoice) DeleteInvoiceLink(uriHandler)
            else
                Text(
                    text = stringResource(Res.string.alert_dialog_delete_general),
                )
        },
        textContentColor = Color.Black,
        onDismissRequest = {
            onDismissRequest()
        },
        dismissButton = {
            Button(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(text = stringResource(Res.string.alert_dialog_cancel))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(text = stringResource(Res.string.alert_dialog_delete_confirm))
            }
        }
    )
}

@Composable
fun DeleteInvoiceLink(uriHandler: UriHandler) {
    val deleteInvoice1 = stringResource(Res.string.alert_dialog_delete_invoice_1)
    val deleteInvoice2 = stringResource(Res.string.alert_dialog_delete_invoice_2)
    val deleteInvoice3 = stringResource(Res.string.alert_dialog_delete_invoice_3)
    val deleteUrl = stringResource(Res.string.alert_dialog_delete_url)

    val annotatedString = buildAnnotatedString {
        append("$deleteInvoice1 ")
        pushStringAnnotation(
            tag = "link",
            annotation = deleteUrl
        )
        withStyle(style = SpanStyle(color = ColorVioletLink)) {
            append(deleteInvoice2)
        }
        // pop()
        append(deleteInvoice3)
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
