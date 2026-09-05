package com.a4a.g8invoicing.ui.screens

import android.content.Context
import android.net.Uri
import android.os.Process
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.data.RestoreManager
import com.a4a.g8invoicing.ui.shared.AppConfirmDialog
import com.a4a.g8invoicing.ui.shared.AppInfoDialog
import com.a4a.g8invoicing.ui.theme.ColorVioletLink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// TODO(i18n): all copy is hardcoded FR — extract to strings.xml via the
// `translations` branch once this ships. The strings live here (not in a
// resources file) because per CLAUDE.md rules the release/1.9 branch cannot
// touch values*/strings.xml directly.
private object RestoreCopy {
    const val CONFIRM_TITLE = "Restaurer une sauvegarde"
    const val CONFIRM_BODY_INTRO = "Si tu souhaites restaurer tes données à partir d'une sauvegarde de ta base de données g8, tu peux continuer.\n\n"
    const val CONFIRM_BODY_WARNING_PREFIX = "⚠️ Attention"
    const val CONFIRM_BODY_WARNING_MID = ", la restauration va "
    const val CONFIRM_BODY_WARNING_EMPHASIS = "écraser"
    const val CONFIRM_BODY_WARNING_SUFFIX = " toutes les données qui sont actuellement dans l'application.\n\nL'application se fermera, relance-la pour finaliser."
    const val CONFIRM_YES = "Je suis sûr·e de moi"
    const val CONFIRM_NO = "Annuler"

    const val PICK_TITLE = "Sélectionner un fichier"
    const val PICK_BODY = "Sélectionne ton fichier .zip (ou .db si ta sauvegarde date d'avant la version 1.9)."
    const val PICK_UPLOAD = "Choisir un fichier"

    const val VALIDATING = "Vérification de la sauvegarde…"

    const val ERROR_TITLE = "Restauration impossible"
    const val ERROR_DOWNGRADE = "Cette sauvegarde vient d'une version plus récente de g8. Mets à jour l'application avant de la restaurer."
    const val OK = "OK"

    const val READY_TITLE = "✅  Sauvegarde chargée"
    const val READY_BODY = "Appuie sur Terminer pour fermer l'application. Relance-la ensuite pour finaliser la restauration."
    const val READY_CONFIRM = "Terminer"
}

private sealed class RestoreStep {
    data object Confirmation : RestoreStep()
    data object PickFile : RestoreStep()
    data object Validating : RestoreStep()
    data class Error(val message: String) : RestoreStep()
    data class Ready(val stagingFile: File) : RestoreStep()
}

/**
 * Full restore choreography: confirmation → SAF picker → validation →
 * intermediate "Terminer" screen → process kill so the boot-time swap runs
 * on next launch. Renders nothing when [active] is false.
 */
@Composable
fun DatabaseRestoreFlow(
    active: Boolean,
    onDismiss: () -> Unit,
) {
    if (!active) return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf<RestoreStep>(RestoreStep.Confirmation) }

    val pickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) {
            // User backed out of the SAF picker — return to the intermediate
            // step so they can retry without going through the confirmation
            // again.
            step = RestoreStep.PickFile
            return@rememberLauncherForActivityResult
        }
        step = RestoreStep.Validating
        scope.launch {
            val (result, staging) = withContext(Dispatchers.IO) {
                RestoreManager.validateBackup(context, uri)
            }
            step = when (result) {
                is RestoreManager.ValidationResult.Ok ->
                    RestoreStep.Ready(staging!!)
                is RestoreManager.ValidationResult.DowngradeBlocked ->
                    RestoreStep.Error(RestoreCopy.ERROR_DOWNGRADE)
                is RestoreManager.ValidationResult.Error ->
                    RestoreStep.Error(result.reason)
            }
        }
    }

    when (val s = step) {
        RestoreStep.Confirmation -> ConfirmationDialog(
            onConfirm = { step = RestoreStep.PickFile },
            onCancel = onDismiss,
        )
        RestoreStep.PickFile -> PickFileDialog(
            onPick = {
                // Accept both the new zip format and legacy raw .db exports.
                pickerLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
            },
            onCancel = onDismiss,
        )
        RestoreStep.Validating -> ValidatingDialog()
        is RestoreStep.Error -> ErrorDialog(message = s.message, onDismiss = onDismiss)
        is RestoreStep.Ready -> ReadyDialog(
            onConfirm = {
                RestoreManager.queueRestore(context, s.stagingFile)
                killApp(context)
            },
        )
    }
}

@Composable
private fun ConfirmationDialog(onConfirm: () -> Unit, onCancel: () -> Unit) {
    // Annotated body (bold "Attention" + "écraser" spans) plugged via
    // bodyContent — plain body= only handles single-style strings.
    AppConfirmDialog(
        title = RestoreCopy.CONFIRM_TITLE,
        confirmText = RestoreCopy.CONFIRM_YES,
        cancelText = RestoreCopy.CONFIRM_NO,
        onConfirm = onConfirm,
        onDismiss = onCancel,
        bodyContent = {
            Text(
                buildAnnotatedString {
                    append(RestoreCopy.CONFIRM_BODY_INTRO)
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(RestoreCopy.CONFIRM_BODY_WARNING_PREFIX)
                    }
                    append(RestoreCopy.CONFIRM_BODY_WARNING_MID)
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(RestoreCopy.CONFIRM_BODY_WARNING_EMPHASIS)
                    }
                    append(RestoreCopy.CONFIRM_BODY_WARNING_SUFFIX)
                }
            )
        },
    )
}

@Composable
private fun PickFileDialog(onPick: () -> Unit, onCancel: () -> Unit) {
    AppConfirmDialog(
        title = RestoreCopy.PICK_TITLE,
        body = RestoreCopy.PICK_BODY,
        confirmText = RestoreCopy.PICK_UPLOAD,
        cancelText = RestoreCopy.CONFIRM_NO,
        onConfirm = onPick,
        onDismiss = onCancel,
    )
}

@Composable
private fun ValidatingDialog() {
    // No-button loader — doesn't map onto AppInfoDialog / AppConfirmDialog
    // (both mandate a CTA). Kept as raw AlertDialog for that reason. Same
    // exception as Account.kt's deletion-in-progress loader.
    AlertDialog(
        onDismissRequest = { /* not dismissable during validation */ },
        title = null,
        text = {
            Column {
                CircularProgressIndicator(modifier = Modifier.height(32.dp))
                Spacer(Modifier.height(12.dp))
                Text(RestoreCopy.VALIDATING)
            }
        },
        confirmButton = {},
    )
}

@Composable
private fun ErrorDialog(message: String, onDismiss: () -> Unit) {
    AppInfoDialog(
        title = RestoreCopy.ERROR_TITLE,
        body = message,
        confirmText = RestoreCopy.OK,
        onDismiss = onDismiss,
    )
}

@Composable
private fun ReadyDialog(onConfirm: () -> Unit) {
    // Non-dismissable — the user shouldn't be able to swipe past the "kill
    // the app to finalise" step. AppInfoDialog's onDismiss doubles as the
    // confirm callback so a scrim tap still fires the kill path (same
    // behaviour as tapping "Terminer" explicitly).
    AppInfoDialog(
        title = RestoreCopy.READY_TITLE,
        body = RestoreCopy.READY_BODY,
        confirmText = RestoreCopy.READY_CONFIRM,
        onDismiss = onConfirm,
    )
}

private fun killApp(@Suppress("UNUSED_PARAMETER") context: Context) {
    // Nuclear but exactly what we want: no lingering Koin instances, no cached
    // SqlDriver hanging onto the pre-restore DB file. Next cold start goes
    // through Application.onCreate → RestoreManager.applyPendingRestoreIfAny.
    Process.killProcess(Process.myPid())
}
