package com.a4a.g8invoicing.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.a4a.g8invoicing.data.setSeenPopup
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.core.content.FileProvider
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.viewModelScope
import java.io.File
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.theme.ColorVioletLink
import com.a4a.g8invoicing.ui.viewmodels.InvoiceListViewModel

@Composable
fun DatabaseExportDialog(context: Context, onDismiss: () -> Unit, onResult: (File) -> Unit) {
    val viewModel: InvoiceListViewModel = koinViewModel()

    var exportMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = {
            viewModel.viewModelScope.launch { setSeenPopup(context.applicationContext) }
            onDismiss()
        },
        title = { Text(stringResource(id = R.string.database_export_dialog_title)) },
        text = {
            Text(
                buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(id = R.string.database_export_dialog_intro))
                    }
                    append(stringResource(id = R.string.database_export_dialog_explanation))
                    append(stringResource(id = R.string.database_export_dialog_where))

                    exportMessage?.let {
                        append("\n\n$it")
                    }
                }
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val file = try {
                    exportDatabaseToDownloads(context)
                } catch (e: Exception) {
                    exportMessage = context.getString(
                        R.string.database_export_dialog_error,
                        e.message ?: ""
                    )
                    return@TextButton
                }
                viewModel.viewModelScope.launch { setSeenPopup(context.applicationContext) }
                onDismiss()
                onResult(file)
            }) {
                Text(stringResource(id = R.string.database_export_dialog_confirm), color = ColorVioletLink)
            }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.viewModelScope.launch { setSeenPopup(context.applicationContext) }
                onDismiss()
            }) {
                Text(stringResource(id = R.string.database_export_dialog_dismiss), color = ColorVioletLink)
            }
        }
    )
}

@Composable
fun DatabaseEmailDialog(context: Context, onDismiss: () -> Unit, file: File) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.database_email_dialog_title)) },
        text = { Text(stringResource(id = R.string.database_email_dialog_text)) },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                sendDatabaseByEmail(context, file)
            }) {
                Text(stringResource(id = R.string.yes), color = ColorVioletLink)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.no), color = ColorVioletLink)
            }
        }
    )
}


fun exportDatabaseToDownloads(context: Context): File {
    val dbFile = context.getDatabasePath("g8_invoicing.db")
    val downloadsDir =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    if (!downloadsDir.exists()) downloadsDir.mkdirs()

    val exportFile = File(downloadsDir, "g8_invoicing_${System.currentTimeMillis()}.db")
    dbFile.copyTo(exportFile, overwrite = true)
    return exportFile
}

fun sendDatabaseByEmail(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/octet-stream"
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.database_email_subject))
        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.database_email_body))
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        Intent.createChooser(
            intent,
            context.getString(R.string.database_email_chooser)
        )
    )
}