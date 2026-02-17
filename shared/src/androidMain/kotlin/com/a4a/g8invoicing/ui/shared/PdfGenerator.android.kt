package com.a4a.g8invoicing.ui.shared

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.a4a.g8invoicing.ui.states.DocumentState
import java.io.File

/**
 * Android Context holder for PDF operations.
 * Must be set before using PdfFileManager.
 */
object AndroidPdfContext {
    var context: Context? = null

    // Cache for last exported PDF URI (Android 10+)
    var lastExportedPdfUri: Uri? = null
    var lastExportedPdfFileName: String? = null
}

actual class PdfFileManager actual constructor() {
    private val context: Context
        get() = AndroidPdfContext.context ?: throw IllegalStateException("AndroidPdfContext.context must be set before using PdfFileManager")

    actual fun getTempFilePath(fileName: String): String {
        val cacheDir = File(context.cacheDir, "pdf_temp")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        return File(cacheDir, fileName).absolutePath
    }

    actual fun getFinalFilePath(fileName: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+: return temp path, actual save happens in saveToFinalLocation
            getTempFilePath(fileName)
        } else {
            // Android 9 and below: use Downloads/g8
            val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
            val directory = File(folder, "g8")
            if (!directory.exists()) directory.mkdirs()
            File(directory, fileName).absolutePath
        }
    }

    actual fun saveToFinalLocation(tempFilePath: String, finalFileName: String): String {
        val tempFile = File(tempFilePath)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+: use MediaStore
            deleteFromMediaStore(finalFileName)

            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, finalFileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, "Download/g8")
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    tempFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                AndroidPdfContext.lastExportedPdfUri = it
                AndroidPdfContext.lastExportedPdfFileName = finalFileName
            }

            tempFile.delete()
            finalFileName
        } else {
            // Android 9 and below: copy to Downloads/g8
            val destPath = getFinalFilePath(finalFileName)
            val destFile = File(destPath)
            if (destFile.exists()) destFile.delete()
            tempFile.copyTo(destFile, overwrite = true)
            tempFile.delete()
            destPath
        }
    }

    actual fun deleteTempFile(filePath: String) {
        File(filePath).delete()
    }

    actual fun openOrShare(filePath: String) {
        val fileName = File(filePath).name
        val uri = getFileUri(fileName)

        uri?.let {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(it, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e("PdfFileManager", "Error opening PDF: ${e.message}")
            }
        }
    }

    fun getFileUri(fileName: String): Uri? {
        // Android 10+: use cached URI or search MediaStore
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (AndroidPdfContext.lastExportedPdfFileName == fileName && AndroidPdfContext.lastExportedPdfUri != null) {
                return AndroidPdfContext.lastExportedPdfUri
            }
            findMediaStoreUri(fileName)?.let { return it }
        }

        // Fallback: FileProvider for local files
        val file = File(getTempFilePath(fileName))
        if (file.exists()) {
            return try {
                FileProvider.getUriForFile(
                    context,
                    context.applicationContext.packageName + ".provider",
                    file
                )
            } catch (e: Exception) {
                Log.e("PdfFileManager", "Error getting FileProvider URI: ${e.message}")
                null
            }
        }
        return null
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
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                return android.content.ContentUris.withAppendedId(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    id
                )
            }
        }
        return null
    }

    private fun deleteFromMediaStore(fileName: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false

        val uri = findMediaStoreUri(fileName) ?: return false
        return try {
            context.contentResolver.delete(uri, null, null) > 0
        } catch (e: Exception) {
            Log.e("PdfFileManager", "Error deleting from MediaStore: ${e.message}")
            false
        }
    }
}

/**
 * Android implementation of PdfGenerator.
 * Delegates to shared PdfGeneratorImpl in jvmMain.
 */
actual class PdfGenerator actual constructor(
    private val strings: PdfStrings,
    private val fileManager: PdfFileManager
) {
    private val impl = PdfGeneratorImpl(strings, fileManager)

    actual fun generatePdf(document: DocumentState): String {
        return impl.generatePdf(document)
    }
}
