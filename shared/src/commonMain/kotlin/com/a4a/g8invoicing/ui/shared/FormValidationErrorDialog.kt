package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.cii_validation_confirm
import com.a4a.g8invoicing.shared.resources.form_validation_dialog_title
import org.jetbrains.compose.resources.stringResource

/**
 * Pre-save recap of the inline red errors under form fields
 * (ClientOrIssuerAddEditForm). Same pattern as [OupsDialog] over in
 * DocumentAddEdit.kt — Material3 AlertDialog with a verticalScroll body,
 * capped at 60 % of the window height so a long error list stays usable
 * on short devices. Bypasses AppInfoDialog / AppDialogShell because
 * those wrap in a plain [androidx.compose.ui.window.Dialog] whose
 * height isn't capped, so a long body would push the confirm button
 * off screen.
 *
 * [rawMessages] is the flat list of `errors.mapNotNull { it.second }`
 * pulled off [com.a4a.g8invoicing.ui.states.ClientOrIssuerState.errors] at
 * validation time. Some messages are aggregated in the modal via
 * [FormInputsValidator.MODAL_MESSAGE_OVERRIDES] (e.g. three "libellé manquant"
 * inline messages collapse to one "un des libellés…" line here), then a
 * final `distinct()` drops any duplicates.
 */
@Composable
fun FormValidationErrorDialog(
    rawMessages: List<String>,
    onDismiss: () -> Unit,
) {
    val displayed: List<String> = rawMessages
        .map { FormInputsValidator.MODAL_MESSAGE_OVERRIDES[it] ?: it }
        .distinct()

    // Total dialog height ≤ 50 % of the window. AlertDialog auto-sizes to
    // content, so we cap the scrollable body's max height at
    // (50 % window − chrome) where chrome ≈ 180.dp (title + confirm + padding).
    // Content shorter than the cap keeps the dialog compact; longer content
    // clips at the cap and scrolls. Fallback 300.dp for the very early
    // compose pass where the window hasn't reported its size yet.
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val maxBodyHeightDp = with(density) {
        val heightPx = windowInfo.containerSize.height
        if (heightPx > 0) ((heightPx * 0.5f).toDp() - 180.dp).coerceAtLeast(120.dp)
        else 300.dp
    }
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.form_validation_dialog_title)) },
        textContentColor = Color.Black,
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = maxBodyHeightDp)
                    .verticalScroll(scrollState)
                    .drawScrollThumb(scrollState)
                    .padding(end = 12.dp),
            ) {
                displayed.forEach { line ->
                    Text("• $line")
                    Spacer(Modifier.height(4.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(Res.string.cii_validation_confirm))
            }
        },
    )
}

/**
 * Compose-friendly state holder for the pre-save form validation modal.
 * Used across every NavGraph that hosts the ClientOrIssuerAddEdit form
 * (standalone + doc bottom sheets). Keeps the wiring boilerplate to one
 * line at each call site.
 */
class FormValidationDialogState {
    var messages: List<String> by mutableStateOf(emptyList())

    /** Populate from a `state.errors: List<Pair<ScreenElement, String?>>`. */
    fun showFrom(errors: List<Pair<*, String?>>) {
        messages = errors.mapNotNull { it.second }
    }

    fun dismiss() {
        messages = emptyList()
    }
}

@Composable
fun rememberFormValidationDialogState(): FormValidationDialogState =
    remember { FormValidationDialogState() }

/**
 * Renders the dialog when [state] has messages. Drop-in at the tail of
 * the composable body — no `if` boilerplate needed at the call site.
 */
@Composable
fun FormValidationDialogHost(state: FormValidationDialogState) {
    if (state.messages.isNotEmpty()) {
        FormValidationErrorDialog(
            rawMessages = state.messages,
            onDismiss = state::dismiss,
        )
    }
}
