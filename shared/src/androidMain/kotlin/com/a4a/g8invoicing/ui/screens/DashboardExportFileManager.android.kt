package com.a4a.g8invoicing.ui.screens

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.a4a.g8invoicing.ui.shared.AndroidPdfContext
import java.io.File

/**
 * Android implementation — writes the CSV to Downloads/g8 via MediaStore
 * (API 29+) or the public Downloads folder (API < 29), then launches
 * ACTION_SEND so the user can forward it to email, Drive, WhatsApp, …
 * in one tap. Mirrors the CiiXmlFileManager + ExportCii share flow.
 */
actual class DashboardExportFileManager actual constructor() {
    private val context: Context
        get() = AndroidPdfContext.context
            ?: throw IllegalStateException(
                "AndroidPdfContext.context must be set before using DashboardExportFileManager"
            )

    actual fun exportCsv(fileName: String, content: String) {
        val uri = writeToDownloads(fileName, mimeType = "text/csv") { out ->
            out.write(content.toByteArray(Charsets.UTF_8))
        } ?: return
        shareFile(uri, mimeType = "text/csv")
    }

    actual fun exportPdf(fileName: String, bytes: ByteArray) {
        if (bytes.isEmpty()) return
        val uri = writeToDownloads(fileName, mimeType = "application/pdf") { out ->
            out.write(bytes)
        } ?: return
        shareFile(uri, mimeType = "application/pdf")
    }

    /**
     * Shared Downloads/g8 writer used by both CSV and PDF paths — picks
     * MediaStore vs. the legacy public Downloads folder based on API level
     * and returns a share-ready URI.
     */
    private fun writeToDownloads(
        fileName: String,
        mimeType: String,
        write: (java.io.OutputStream) -> Unit,
    ): Uri? {
        val cacheDir = File(context.cacheDir, "export_temp").apply { if (!exists()) mkdirs() }
        val tempFile = File(cacheDir, fileName)
        tempFile.outputStream().use(write)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH, "Download/g8")
            }
            val uri: Uri? = context.contentResolver
                .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            if (uri == null) {
                tempFile.delete()
                return null
            }
            context.contentResolver.openOutputStream(uri)?.use { out ->
                tempFile.inputStream().use { it.copyTo(out) }
            }
            tempFile.delete()
            findMediaStoreUri(fileName) ?: uri
        } else {
            val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val g8Dir = File(folder, "g8").apply { if (!exists()) mkdirs() }
            val destFile = File(g8Dir, fileName)
            tempFile.copyTo(destFile, overwrite = true)
            tempFile.delete()
            try {
                FileProvider.getUriForFile(
                    context,
                    context.applicationContext.packageName + ".provider",
                    destFile,
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun shareFile(uri: Uri, mimeType: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun findMediaStoreUri(fileName: String): Uri? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null

        val projection = arrayOf(MediaStore.Downloads._ID)
        val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(fileName)

        context.contentResolver.query(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                return ContentUris.withAppendedId(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    id,
                )
            }
        }
        return null
    }
}
