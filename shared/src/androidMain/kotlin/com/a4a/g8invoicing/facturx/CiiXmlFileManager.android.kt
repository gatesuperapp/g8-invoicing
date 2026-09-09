package com.a4a.g8invoicing.facturx

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.a4a.g8invoicing.ui.shared.AndroidPdfContext
import java.io.File

/**
 * Android implementation — mirrors the write-then-MediaStore-copy flow
 * used for PDFs. Writing and looking up the resulting URI are separate
 * calls so the export screen can render its own confirmation UI and let
 * the user pick email / share themselves (same UX as the PDF path).
 */
actual class CiiXmlFileManager actual constructor() {
    private val context: Context
        get() = AndroidPdfContext.context
            ?: throw IllegalStateException("AndroidPdfContext.context must be set before using CiiXmlFileManager")

    actual fun writeXml(fileName: String, xml: String): String {
        val cacheDir = File(context.cacheDir, "xml_temp").apply { if (!exists()) mkdirs() }
        val tempFile = File(cacheDir, fileName)
        tempFile.writeText(xml, Charsets.UTF_8)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/xml")
                put(MediaStore.Downloads.RELATIVE_PATH, "Download/g8")
            }
            val uri: Uri = context.contentResolver
                .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IllegalStateException("MediaStore.insert returned null for $fileName")
            context.contentResolver.openOutputStream(uri)?.use { out ->
                tempFile.inputStream().use { it.copyTo(out) }
            }
            tempFile.delete()
            fileName
        } else {
            val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val g8Dir = File(folder, "g8").apply { if (!exists()) mkdirs() }
            val destFile = File(g8Dir, fileName)
            tempFile.copyTo(destFile, overwrite = true)
            tempFile.delete()
            destFile.name
        }
    }

    /**
     * Look up the URI for a previously-written XML file so it can be
     * attached to Intent.ACTION_SEND (email) or ACTION_SEND (share).
     * Returns null if the file cannot be located — the caller shows an
     * error dialog in that case.
     */
    fun getFileUri(fileName: String): Uri? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            findMediaStoreUri(fileName)?.let { return it }
        }
        val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(File(folder, "g8"), fileName)
        if (file.exists()) {
            return try {
                FileProvider.getUriForFile(
                    context,
                    context.applicationContext.packageName + ".provider",
                    file,
                )
            } catch (e: Exception) {
                Log.e("CiiXmlFileManager", "Error getting FileProvider URI: ${e.message}")
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
