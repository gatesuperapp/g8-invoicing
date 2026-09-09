package com.a4a.g8invoicing.ui.shared

import androidx.compose.runtime.Composable

/**
 * Thin wrapper over [AppInfoDialog] — kept as a legacy signature so existing
 * call sites (export flows, error banners) don't need touching. New code
 * should call [AppInfoDialog] directly.
 */
@Composable
fun AlertDialogErrorOrInfo(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    message: String,
    confirmationText: String,
) {
    AppInfoDialog(
        body = message,
        confirmText = confirmationText,
        onConfirm = onConfirmation,
        onDismiss = onDismissRequest,
    )
}
