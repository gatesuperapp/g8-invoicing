package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
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

/**
 * Delete confirmation using the destructive variant of [AppConfirmDialog].
 * The invoice path (isInvoice=true) plugs a clickable-link body into the
 * [bodyContent] slot to explain the "why can't I delete invoices?" rule.
 */
@Composable
fun AlertDialogDeleteDocument(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    isInvoice: Boolean = false,
) {
    val uriHandler = LocalUriHandler.current
    if (isInvoice) {
        AppConfirmDialog(
            confirmText = stringResource(Res.string.alert_dialog_delete_confirm),
            cancelText = stringResource(Res.string.alert_dialog_cancel),
            destructive = true,
            onConfirm = onConfirmation,
            onDismiss = onDismissRequest,
            bodyContent = { DeleteInvoiceLink(uriHandler) },
        )
    } else {
        AppConfirmDialog(
            body = stringResource(Res.string.alert_dialog_delete_general),
            confirmText = stringResource(Res.string.alert_dialog_delete_confirm),
            cancelText = stringResource(Res.string.alert_dialog_cancel),
            destructive = true,
            onConfirm = onConfirmation,
            onDismiss = onDismissRequest,
        )
    }
}

@Composable
fun DeleteInvoiceLink(uriHandler: UriHandler) {
    val deleteInvoice1 = stringResource(Res.string.alert_dialog_delete_invoice_1)
    val deleteInvoice2 = stringResource(Res.string.alert_dialog_delete_invoice_2)
    val deleteInvoice3 = stringResource(Res.string.alert_dialog_delete_invoice_3)
    val deleteUrl = stringResource(Res.string.alert_dialog_delete_url)

    val annotatedString = buildAnnotatedString {
        append("$deleteInvoice1 ")
        pushStringAnnotation(tag = "link", annotation = deleteUrl)
        withStyle(style = SpanStyle(color = ColorVioletLink)) {
            append(deleteInvoice2)
        }
        append(deleteInvoice3)
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "link", start = offset, end = offset)
                .firstOrNull()?.let { uriHandler.openUri(it.item) }
        },
    )
}
