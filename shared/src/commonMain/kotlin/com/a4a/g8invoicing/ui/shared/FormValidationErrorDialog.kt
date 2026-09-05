package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.cii_validation_confirm
import com.a4a.g8invoicing.shared.resources.form_validation_dialog_title
import org.jetbrains.compose.resources.stringResource

/**
 * Pre-save recap of the inline red errors under form fields
 * (ClientOrIssuerAddEditForm). Same visual template as the Factur-X export
 * OupsDialog but stripped of the emoji/premium/CII sections — just the
 * localized error list.
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.form_validation_dialog_title)) },
        textContentColor = Color.Black,
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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
