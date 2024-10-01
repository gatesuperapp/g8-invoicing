package com.a4a.g8invoicing.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat.startActivity
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.shared.AlertDialogErrorOrInfo
import com.a4a.g8invoicing.ui.shared.DocumentType
import com.a4a.g8invoicing.ui.shared.createPdfWithIText
import com.a4a.g8invoicing.ui.shared.getFileUri
import com.a4a.g8invoicing.ui.shared.icons.IconShare
import com.a4a.g8invoicing.ui.states.DocumentState
import com.ninetyninepercent.funfactu.icons.IconMail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


@Composable
fun ExportPdf(
    deliveryNote: DocumentState,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    var isExportOngoing by remember { mutableStateOf(ExportStatus.ONGOING) }
    var finalFileName by remember { mutableStateOf("") }

    val openErrorDialog = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf("") }
    when {
        openErrorDialog.value -> {
            AlertDialogErrorOrInfo(
                onDismissRequest = {
                    openErrorDialog.value = false
                },
                onConfirmation = {
                    openErrorDialog.value = false
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
                ExportStatus.ONGOING -> stringResource(R.string.export_ongoing)
                ExportStatus.DONE -> stringResource(R.string.export_done)
                else -> stringResource(R.string.export_error)
            },
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )

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


            Send(context, deliveryNote, finalFileName)
            Share(context, finalFileName)
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
    ONGOING, DONE, ERROR
}


@Composable
fun Send(context: Context, document: DocumentState, finalFileName: String) {
    Button(onClick = {
        try {
            val uri = getFileUri(context, finalFileName)
            uri?.let {
                composeEmail(
                    address = document.documentClient?.email?.text,
                    documentNumber = document.documentNumber.text,
                    documentType = document.documentType,
                    emailMessage = Strings.get(R.string.export_send_file_content),
                    attachedDocumentUri = it,
                    context = context
                )
            } ?: Toast.makeText(
                context,
                R.string.export_error_sharing,
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        // startActivity(context, share, null)
    }) {
        Icon(imageVector = IconMail, contentDescription = null)
        Text(
            stringResource(R.string.export_send_file),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

fun composeEmail(
    address: String? = null,
    documentNumber: String? = null,
    documentType: DocumentType? = null,
    emailMessage: String,
    attachedDocumentUri: Uri? = null,
    context: Context,
) {
    val type = when (documentType) {
        DocumentType.INVOICE -> Strings.get(R.string.export_email_subject_invoice)
        DocumentType.DELIVERY_NOTE -> Strings.get(R.string.export_email_subject_delivery_note)
        else -> null
    }

    try {
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.selector = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(address))

        if (documentNumber != null) {
            intent.putExtra(
                Intent.EXTRA_SUBJECT,
                Strings.get(R.string.export_email_subject) + " $type N°$documentNumber"
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
fun Share(context: Context, finalFileName: String) {
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
            } ?: Toast.makeText(
                context,
                R.string.export_error_sharing,
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }) {
        Icon(imageVector = IconShare, contentDescription = null)
        Text(
            stringResource(R.string.export_share_file),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}