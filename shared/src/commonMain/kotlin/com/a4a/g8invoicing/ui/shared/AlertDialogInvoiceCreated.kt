package com.a4a.g8invoicing.ui.shared

import androidx.compose.runtime.Composable
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.invoice_created_button
import com.a4a.g8invoicing.shared.resources.invoice_created_title
import org.jetbrains.compose.resources.stringResource

/**
 * Thin wrapper over [AppInfoDialog]. "titleText" is a legacy misnomer — the
 * string is actually rendered as the body, not a title. New callers should
 * call [AppInfoDialog] directly.
 */
@Composable
fun AlertDialogInvoiceCreated(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    titleText: String = stringResource(Res.string.invoice_created_title),
    buttonText: String = stringResource(Res.string.invoice_created_button),
) {
    AppInfoDialog(
        body = titleText,
        confirmText = buttonText,
        onConfirm = onConfirmation,
        onDismiss = onDismissRequest,
    )
}
