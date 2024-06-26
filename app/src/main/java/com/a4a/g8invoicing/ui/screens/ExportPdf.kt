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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat.startActivity
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.shared.createPdfWithIText
import com.a4a.g8invoicing.ui.shared.fileNameAfterNumbering
import com.a4a.g8invoicing.ui.shared.getFileUri
import com.a4a.g8invoicing.ui.shared.icons.IconShare
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.ninetyninepercent.funfactu.icons.IconMail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


@Composable
fun ExportPdf(
    deliveryNote: DeliveryNoteState,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    var isExportOngoing by remember { mutableStateOf(ExportStatus.ONGOING) }

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
                isExportOngoing = ExportStatus.DONE
            }
        )

        if (isExportOngoing == ExportStatus.DONE) {
            Text(
                modifier = Modifier
                    .padding(bottom = 50.dp),
                text = stringResource(R.string.export_done_file_location),
                style = MaterialTheme.typography.titleSmall,
                color = Color.White
            )

            Send(context)
            Share(context)
        }

    }
}

@Composable
fun ExportDocumentAndShowProgressBar(
    deliveryNote: DeliveryNoteState,
    context: Context,
    loadingIsOver: () -> Unit,
) {
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(true) {
        val job: Job = launch(context = Dispatchers.Default) {
            try {
                createPdfWithIText(deliveryNote, context)
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
        job.join()
        loading = false
        loadingIsOver()
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
fun Send(context: Context) {
    Button(onClick = {
        try {
            val uri = getFileUri(context, fileNameAfterNumbering)
            uri?.let {
                composeEmail(context = context)
                /*ShareCompat.IntentBuilder(context)
                    .setEmailTo(arrayOf("aude@fk.com"))
                    .setType("application/pdf")
                    .addStream(uri)
                    .startChooser()*/

            } ?: Toast.makeText(
                context,
                R.string.export_error_sharing,
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Log.e("xxx", "Error: ${e.message}")
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
    addresses: Array<String?>? = arrayOf(),
    documentNumber: String? = "",
    context: Context,
) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.setData(Uri.parse("mailto:")) // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("cdcc@gmail.fr"))
        intent.putExtra(Intent.EXTRA_SUBJECT, "Votre bon de livraison N°BLDJFH")
        intent.putExtra(Intent.EXTRA_TEXT, "dwdqsdsqdzqdzd sefssf" )
        startActivity(context, intent, null)

    } catch (e: Exception) {
        Log.e("xxx", "Error: ${e.message}")
    }
}

@Composable
fun Share(context: Context) {
    Button(onClick = {
        try {
            val uri = getFileUri(context, fileNameAfterNumbering)
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
            Log.e("xxx", "Error: ${e.message}")
        }
    }) {
        Icon(imageVector = IconShare, contentDescription = null)
        Text(
            stringResource(R.string.export_share_file),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
