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
    const val CONFIRM_BODY_INTRO = "Si vous souhaitez restaurer vos données à partir d'une sauvegarde de votre base de données g8, vous pouvez continuer.\n\n"
    const val CONFIRM_BODY_WARNING_PREFIX = "⚠️ Attention"
    const val CONFIRM_BODY_WARNING_MID = ", la restauration va "
    const val CONFIRM_BODY_WARNING_EMPHASIS = "écraser"
    const val CONFIRM_BODY_WARNING_SUFFIX = " toutes les données qui sont actuellement dans l'application.\n\nL'application se fermera, relancez-la pour finaliser."
    const val CONFIRM_YES = "Je suis sûr·e de moi"
    const val CONFIRM_NO = "Annuler"

    const val PICK_TITLE = "Sélectionner un fichier"
    const val PICK_BODY = "Sélectionnez votre fichier .zip (ou .db si votre sauvegarde date d'avant la version 1.9)."
    const val PICK_UPLOAD = "Choisir un fichier"

    const val VALIDATING = "Vérification de la sauvegarde…"

    const val ERROR_TITLE = "Restauration impossible"
    const val ERROR_DOWNGRADE = "Cette sauvegarde vient d'une version plus récente de g8. Mettez à jour l'application avant de la restaurer."
    const val OK = "OK"

    const val READY_TITLE = "✅  Sauvegarde chargée"
    const val READY_BODY = "Appuyez sur Terminer pour fermer l'application. Relancez-la ensuite pour finaliser la restauration."
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
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(RestoreCopy.CONFIRM_TITLE) },
        text = {
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
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(RestoreCopy.CONFIRM_YES, color = ColorVioletLink)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(RestoreCopy.CONFIRM_NO, color = ColorVioletLink)
            }
        },
    )
}

@Composable
private fun PickFileDialog(onPick: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(RestoreCopy.PICK_TITLE) },
        text = { Text(RestoreCopy.PICK_BODY) },
        confirmButton = {
            TextButton(onClick = onPick) {
                Text(RestoreCopy.PICK_UPLOAD, color = ColorVioletLink)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(RestoreCopy.CONFIRM_NO, color = ColorVioletLink)
            }
        },
    )
}

@Composable
private fun ValidatingDialog() {
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(RestoreCopy.ERROR_TITLE) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(RestoreCopy.OK, color = ColorVioletLink)
            }
        },
    )
}

@Composable
private fun ReadyDialog(onConfirm: () -> Unit) {
    AlertDialog(
        // Non-dismissable so the user can't accidentally lose the staging
        // file by tapping outside without triggering the actual restore.
        onDismissRequest = {},
        title = { Text(RestoreCopy.READY_TITLE) },
        text = { Text(RestoreCopy.READY_BODY) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(RestoreCopy.READY_CONFIRM, color = ColorVioletLink)
            }
        },
    )
}

private fun killApp(@Suppress("UNUSED_PARAMETER") context: Context) {
    // Nuclear but exactly what we want: no lingering Koin instances, no cached
    // SqlDriver hanging onto the pre-restore DB file. Next cold start goes
    // through Application.onCreate → RestoreManager.applyPendingRestoreIfAny.
    Process.killProcess(Process.myPid())
}
