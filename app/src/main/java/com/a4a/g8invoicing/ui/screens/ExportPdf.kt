package com.a4a.g8invoicing.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.core.content.FileProvider
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.shared.createPdfWithIText
import com.a4a.g8invoicing.ui.shared.icons.IconVisibility
import kotlinx.coroutines.delay
import java.io.File


@Composable
fun ExportPdf(
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
                .padding(bottom = 40.dp),
            text = if (isExportOngoing == ExportStatus.ONGOING) stringResource(R.string.export_ongoing)
            else if (isExportOngoing == ExportStatus.DONE)
                stringResource(R.string.export_done)
            else
                stringResource(R.string.export_error),
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )
        ProgressBar(
            context,
            loadingIsOver = {
                isExportOngoing = ExportStatus.DONE
            }
        )

        if (isExportOngoing == ExportStatus.DONE) {
            Text(
                text = stringResource(R.string.export_done_file_location),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            Button(
                onClick = {
/*
    val share = Intent()
    share.setAction(Intent.ACTION_SEND)
    share.setType("application/pdf")
    share.putExtra(Intent.EXTRA_STREAM, uri)
    share.setPackage("com.whatsapp")
*/
                    val uri = getFileUri(context)
                    val intent = Intent(Intent.ACTION_VIEW)
                    // intent.setType("application/pdf")
                    // val mydir = Uri.parse("file://$location")
                    intent.setDataAndType(uri, "application/*") // or use */*
                    startActivity(context, intent, null)
                },
                modifier = Modifier
                    .padding(bottom = 10.dp),
            ) {
                Icon(imageVector = IconVisibility, contentDescription = null)
                Text(
                    stringResource(R.string.export_done_open_file),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Share(context)
        }

    }
}

@Composable
fun ProgressBar(
    context: Context,
    loadingIsOver: () -> Unit,
) {
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(true) {
        try {
            createPdfWithIText(context)
        } catch (e: Exception) {
            Log.e("xxx", "Error: ${e.message}")
        }
       // delay(2000)
        loading = false
        loadingIsOver()
    }

    if (!loading) {
        return
    }

    CircularProgressIndicator(
        modifier = Modifier.width(64.dp),
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

enum class ExportStatus {
    ONGOING, DONE, ERROR
}


@Composable
fun Share(context: Context) {
    val uri = getFileUri(context, "BL.pdf")
    Button(onClick = {
        uri?.let {
            ShareCompat.IntentBuilder(context)
                .setType("application/pdf")
                .addStream(uri)
                .setChooserTitle("Share image")
                .setSubject("Shared image")
                .startChooser()
        }
        // startActivity(context, share, null)
    }) {
        Icon(imageVector = Icons.Default.Share, contentDescription = null)
        Text(
            stringResource(R.string.export_done_send_file),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

fun getFileUri(context: Context, fileName: String? = ""): Uri? {
    var uri: Uri? = null
    val file = File(getFilePath("BL.pdf"))

    try {
        uri = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".provider",
            file
        )
    } catch (e: Exception) {
        Log.e(ContentValues.TAG, "Error: ${e.message}")
    }
    return uri
}

fun getFilePath(fileName: String? = ""): String {
    var path = ""
    createNewDirectory()?.let {
        path = it.absolutePath + "/" + fileName
    }
    return path
}

fun createNewDirectory(): File? {
    val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
    val directory = File(folder,"g8")
    if (directory.exists() && directory.isDirectory) {
        println("Directory exists.")
    } else {
        val result = directory.mkdir()
        if (result) {
            Log.e(ContentValues.TAG, "Directory created successfully: ${directory.absolutePath}")
        } else {
            Log.e(ContentValues.TAG, "Failed to create directory.")
            return null
        }
    }
    return directory
}