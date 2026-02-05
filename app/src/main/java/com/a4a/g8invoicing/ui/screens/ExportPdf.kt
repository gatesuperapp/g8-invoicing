package com.a4a.g8invoicing.ui.screens

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.shared.AlertDialogErrorOrInfo
import com.a4a.g8invoicing.ui.shared.DocumentType
import com.a4a.g8invoicing.ui.shared.createPdfWithIText
import com.a4a.g8invoicing.ui.shared.getFileUri
import com.a4a.g8invoicing.ui.states.DocumentState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


@Composable
fun ExportPdf(
    deliveryNote: DocumentState,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    var isExportOngoing by remember { mutableStateOf(ExportStatus.WAITING_PERMISSION) }
    var finalFileName by remember { mutableStateOf("") }

    val openErrorDialog = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf("") }

    // Sur Android 10+ (API 29+), pas besoin de permissions (MediaStore)
    val needsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

    // Vérification des permissions (seulement pour Android 9 et moins)
    val hasPermissions = remember {
        if (!needsPermission) {
            true // Pas besoin de permission sur Android 10+
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Launcher pour demander les permissions (seulement pour Android 9 et moins)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            isExportOngoing = ExportStatus.ONGOING
        } else {
            errorMessage.value = Strings.get(R.string.export_permission_denied)
            openErrorDialog.value = true
            isExportOngoing = ExportStatus.ERROR
        }
    }

    // Demander les permissions ou lancer l'export au démarrage
    LaunchedEffect(Unit) {
        if (hasPermissions) {
            isExportOngoing = ExportStatus.ONGOING
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    when {
        openErrorDialog.value -> {
            AlertDialogErrorOrInfo(
                onDismissRequest = {
                    openErrorDialog.value = false
                    onDismissRequest()
                },
                onConfirmation = {
                    openErrorDialog.value = false
                    onDismissRequest()
                },
                message = Strings.get(R.string.alert_dialog_error) + errorMessage.value,
                confirmationText = stringResource(id = R.string.alert_dialog_error_confirm)
            )
        }
    }
    val openInfoDialog = remember { mutableStateOf(false) }
    when {
        openInfoDialog.value -> {
            AlertDialogErrorOrInfo(
                onDismissRequest = {
                    openInfoDialog.value = false
                },
                onConfirmation = {
                    openInfoDialog.value = false
                },
                message = Strings.get(R.string.export_info_popup) + errorMessage.value,
                confirmationText = stringResource(id = R.string.export_info_popup_validate)
            )
        }
    }

    // Dialog pour erreur de partage
    var showShareError by remember { mutableStateOf(false) }
    if (showShareError) {
        AlertDialogErrorOrInfo(
            onDismissRequest = { showShareError = false },
            onConfirmation = { showShareError = false },
            message = stringResource(id = R.string.export_error_sharing),
            confirmationText = stringResource(id = R.string.ok)
        )
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.padding(14.dp)
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .clickable {
                        onDismissRequest()
                    },
                text = stringResource(R.string.export_close),
                color = Color.White
            )
        }
        Text(
            modifier = Modifier
                .padding(bottom = 30.dp),
            text = when (isExportOngoing) {
                ExportStatus.WAITING_PERMISSION -> stringResource(R.string.export_waiting_permission)
                ExportStatus.ONGOING -> stringResource(R.string.export_ongoing)
                ExportStatus.DONE -> stringResource(R.string.export_done)
                else -> stringResource(R.string.export_error)
            },
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )

        // Lancer l'export seulement si les permissions sont accordées
        if (isExportOngoing == ExportStatus.ONGOING) {
            ExportDocumentAndShowProgressBar(
                deliveryNote,
                context,
                loadingIsOver = {
                    finalFileName = it
                    isExportOngoing = ExportStatus.DONE
                },
                displayErrorMessage = {
                    errorMessage.value = it ?: ""
                    openErrorDialog.value = true
                }
            )
        }

        if (isExportOngoing == ExportStatus.DONE) {
            Text(
                modifier = Modifier
                    .padding(bottom = 5.dp),
                text = stringResource(R.string.export_done_file_location),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall,
                color = White
            )

            ClickableText(
                modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 50.dp),
                onClick = { openInfoDialog.value = true },
                style = TextStyle(
                    color = White,
                    fontSize = 18.sp
                ),
                text = AnnotatedString(Strings.get(R.string.export_clickable_text)),
            )


            Send(context, deliveryNote, finalFileName, onError = { showShareError = true })
            Share(context, finalFileName, onError = { showShareError = true })
        }
    }
}

@Composable
fun ExportDocumentAndShowProgressBar(
    document: DocumentState,
    context: Context,
    loadingIsOver: (String) -> Unit,
    displayErrorMessage: (String?) -> Unit,
) {
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(true) {
        var fileName = ""
        val job: Job = launch(context = Dispatchers.Default) {
            try {
                fileName = createPdfWithIText(document, context)
            } catch (e: Exception) {
                displayErrorMessage(e.message)
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
        job.join()
        loading = false
        loadingIsOver(fileName)
    }

    if (!loading) {
        return
    }

    LinearProgressIndicator(
        modifier = Modifier.width(64.dp),
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

enum class ExportStatus {
    WAITING_PERMISSION, ONGOING, DONE, ERROR
}


@Composable
fun Send(context: Context, document: DocumentState, finalFileName: String, onError: () -> Unit) {
    Button(onClick = {
        try {
            val type = when (document.documentType) {
                DocumentType.INVOICE -> Strings.get(R.string.export_email_subject_invoice)
                DocumentType.DELIVERY_NOTE -> Strings.get(R.string.export_email_subject_delivery_note)
                DocumentType.CREDIT_NOTE -> Strings.get(R.string.export_email_subject_credit_note)
                else -> ""
            }

            val uri = getFileUri(context, finalFileName)
            uri?.let {
                // Récupérer toutes les adresses email du client
                val emailAddresses = document.documentClient?.emails
                    ?.map { it.email.text }
                    ?.filter { it.isNotEmpty() }
                    ?: emptyList()

                composeEmail(
                    addresses = emailAddresses,
                    documentNumber = document.documentNumber.text,
                    emailSubject = Strings.get(R.string.export_email_subject, type, document.documentNumber.text),
                    emailMessage = Strings.get(R.string.export_send_file_content, type ?: ""),
                    attachedDocumentUri = it,
                    context = context
                )
            } ?: onError()
        } catch (e: Exception) {
            onError()
        }
    }) {
        Icon(imageVector = Icons.Outlined.Email, contentDescription = null)
        Text(
            stringResource(R.string.export_send_file),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

fun composeEmail(
    addresses: List<String> = emptyList(),
    documentNumber: String? = null,
    emailSubject: String,
    emailMessage: String,
    attachedDocumentUri: Uri? = null,
    context: Context,
) {

    try {
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.selector = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
        intent.putExtra(Intent.EXTRA_EMAIL, addresses.toTypedArray())

        if (documentNumber != null) {
            intent.putExtra(
                Intent.EXTRA_SUBJECT,
                emailSubject
            )
        }
        intent.putExtra(Intent.EXTRA_TEXT, emailMessage)
        attachedDocumentUri?.let {
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(listOf(it)))
        }

        startActivity(context, intent, null)

    } catch (e: Exception) {
        //Log.e(ContentValues.TAG, "Error: ${e.message}")
    }
}

@Composable
fun Share(context: Context, finalFileName: String, onError: () -> Unit) {
    Button(onClick = {
        try {
            val uri = getFileUri(context, finalFileName)
            uri?.let {
                ShareCompat.IntentBuilder(context)
                    .setType("application/pdf")
                    .addStream(uri)
                    .setChooserTitle("Share document")
                    .setSubject("Shared document")
                    .startChooser()
            } ?: onError()
        } catch (e: Exception) {
            onError()
        }
    }) {
        Icon(imageVector = Icons.Outlined.Share, contentDescription = null)
        Text(
            stringResource(R.string.export_share_file),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
