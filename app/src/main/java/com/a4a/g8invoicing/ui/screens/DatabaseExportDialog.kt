package com.a4a.g8invoicing.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.compose.material3.Text
import com.a4a.g8invoicing.ui.shared.AppConfirmDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.a4a.g8invoicing.data.setSeenDbExportPopup
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.core.content.FileProvider
import app.cash.sqldelight.db.SqlDriver
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import androidx.lifecycle.viewModelScope
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.database_email_body
import com.a4a.g8invoicing.shared.resources.database_email_chooser
import com.a4a.g8invoicing.shared.resources.database_email_dialog_text
import com.a4a.g8invoicing.shared.resources.database_email_dialog_title
import com.a4a.g8invoicing.shared.resources.database_email_subject
import com.a4a.g8invoicing.shared.resources.database_export_dialog_confirm
import com.a4a.g8invoicing.shared.resources.database_export_dialog_dismiss
import com.a4a.g8invoicing.shared.resources.database_export_dialog_error
import com.a4a.g8invoicing.shared.resources.database_export_dialog_explanation
import com.a4a.g8invoicing.shared.resources.database_export_dialog_intro
import com.a4a.g8invoicing.shared.resources.database_export_dialog_title
import com.a4a.g8invoicing.shared.resources.database_export_dialog_where
import com.a4a.g8invoicing.shared.resources.no
import com.a4a.g8invoicing.shared.resources.yes
import com.a4a.g8invoicing.ui.theme.ColorVioletLink
import com.a4a.g8invoicing.ui.viewmodels.InvoiceListViewModel

@Composable
fun DatabaseExportDialog(context: Context, onDismiss: () -> Unit, onResult: (File) -> Unit) {
    val viewModel: InvoiceListViewModel = koinViewModel()
    val sqlDriver: SqlDriver = koinInject()
    val scope = rememberCoroutineScope()

    var exportMessage by remember { mutableStateOf<String?>(null) }

    // Annotated body (bold intro span, error tail after export attempt)
    // plugged via bodyContent — plain body= only handles single-style strings.
    val markSeenAndDismiss: () -> Unit = {
        viewModel.viewModelScope.launch { setSeenDbExportPopup(context.applicationContext) }
        onDismiss()
    }
    AppConfirmDialog(
        title = stringResource(Res.string.database_export_dialog_title),
        confirmText = stringResource(Res.string.database_export_dialog_confirm),
        cancelText = stringResource(Res.string.database_export_dialog_dismiss),
        onConfirm = {
            val file = try {
                exportDatabaseToDownloads(context, sqlDriver)
            } catch (e: Exception) {
                scope.launch {
                    exportMessage = getString(
                        Res.string.database_export_dialog_error,
                        e.message ?: ""
                    )
                }
                return@AppConfirmDialog
            }
            markSeenAndDismiss()
            onResult(file)
        },
        onDismiss = markSeenAndDismiss,
        bodyContent = {
            Text(
                buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(Res.string.database_export_dialog_intro))
                    }
                    append(stringResource(Res.string.database_export_dialog_explanation))
                    append(stringResource(Res.string.database_export_dialog_where))

                    exportMessage?.let {
                        append("\n\n$it")
                    }
                }
            )
        },
    )
}

@Composable
fun DatabaseEmailDialog(context: Context, onDismiss: () -> Unit, file: File) {
    val scope = rememberCoroutineScope()

    AppConfirmDialog(
        title = stringResource(Res.string.database_email_dialog_title),
        body = stringResource(Res.string.database_email_dialog_text),
        confirmText = stringResource(Res.string.yes),
        cancelText = stringResource(Res.string.no),
        onConfirm = {
            onDismiss()
            scope.launch { sendDatabaseByEmail(context, file) }
        },
        onDismiss = onDismiss,
    )
}


/**
 * Full-backup bundle: SQLite file + every issuer logo stored under
 * filesDir/logos/. Ships as a single .zip so restoring a backup on a fresh
 * install rehydrates both the data and the images referenced by
 * ClientOrIssuer.logo_path / DocumentClientOrIssuer.logo_path.
 *
 * Uses `VACUUM INTO` for the DB snapshot rather than a raw file copy. In WAL
 * mode (SQLDelight's default) recent writes live in the -wal side file until
 * a checkpoint; a plain copyTo would miss them and ship a truncated backup.
 * VACUUM INTO emits a fully materialised, defragged copy in one statement,
 * with an implicit checkpoint — no partial state, no journal side-files to
 * ship alongside.
 *
 * Layout inside the zip:
 *   g8_invoicing.db          (the VACUUM INTO snapshot)
 *   logos/<file>             (only when filesDir/logos/ is non-empty)
 */
fun exportDatabaseToDownloads(context: Context, driver: SqlDriver): File {
    val logosDir = File(context.filesDir, "logos")

    val downloadsDir =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    if (!downloadsDir.exists()) downloadsDir.mkdirs()

    // Snapshot lands in cacheDir so it's auto-cleanable if the process dies
    // between the VACUUM INTO and the zip step.
    val snapshotFile = File(context.cacheDir, "g8_invoicing_snapshot_${System.currentTimeMillis()}.db")
    if (snapshotFile.exists()) snapshotFile.delete()

    // VACUUM INTO does not support bound parameters — the target path is baked
    // into the SQL literal. Path comes from our own construction (cacheDir +
    // timestamp) so there's no injection surface, but escape single quotes
    // defensively in case Android ever surfaces a cache path with a quote.
    val escapedPath = snapshotFile.absolutePath.replace("'", "''")
    driver.execute(null, "VACUUM INTO '$escapedPath'", 0)

    val exportFile = File(downloadsDir, "g8_invoicing_${System.currentTimeMillis()}.zip")
    try {
        ZipOutputStream(FileOutputStream(exportFile).buffered()).use { zip ->
            addFileToZip(zip, snapshotFile, "g8_invoicing.db")
            if (logosDir.exists() && logosDir.isDirectory) {
                logosDir.listFiles()
                    ?.filter { it.isFile }
                    ?.forEach { logo -> addFileToZip(zip, logo, "logos/${logo.name}") }
            }
        }
    } finally {
        snapshotFile.delete()
    }
    return exportFile
}

private fun addFileToZip(zip: ZipOutputStream, file: File, entryName: String) {
    zip.putNextEntry(ZipEntry(entryName))
    BufferedInputStream(FileInputStream(file)).use { input -> input.copyTo(zip) }
    zip.closeEntry()
}

/**
 * Silent safety-net snapshot to the app's internal files directory.
 * Meant to run right before the 1.9 migration wizard starts mutating the
 * schema — so even if the user skips the "Sauvegarder" CTA, we still have
 * a pre-wizard copy on disk. The file lives in filesDir/backups/, which
 * needs root or ADB pull to extract, but it's better than nothing.
 *
 * Returns the created file, or null on failure (missing source DB, IO
 * error, permission). Caller should log-and-ignore — this is a best-effort
 * safety net, not a hard requirement of the wizard.
 */
fun snapshotDatabaseInternally(context: Context, tag: String): File? {
    return try {
        val dbFile = context.getDatabasePath("g8_invoicing.db")
        if (!dbFile.exists()) return null
        val backupDir = File(context.filesDir, "backups").apply { if (!exists()) mkdirs() }
        val safeTag = tag.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val backupFile = File(backupDir, "${safeTag}_${System.currentTimeMillis()}.db")
        dbFile.copyTo(backupFile, overwrite = false)
        backupFile
    } catch (_: Exception) {
        null
    }
}

suspend fun sendDatabaseByEmail(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/zip"
        putExtra(Intent.EXTRA_SUBJECT, getString(Res.string.database_email_subject))
        putExtra(Intent.EXTRA_TEXT, getString(Res.string.database_email_body))
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        Intent.createChooser(intent, getString(Res.string.database_email_chooser))
    )
}
