package com.a4a.g8invoicing.ui.shared

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.io.FileOutputStream

/**
 * Android Context holder for image operations.
 * Must be set before using ImageStorage.
 */
object AndroidImageContext {
    var context: Context? = null
}

actual class ImageStorage actual constructor() {
    private val context: Context
        get() = AndroidImageContext.context
            ?: throw IllegalStateException("AndroidImageContext.context must be set before using ImageStorage")

    private val logoDir: File
        get() {
            val dir = File(context.filesDir, "logos")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    actual fun getLogoDirectory(): String = logoDir.absolutePath

    actual fun saveLogoImage(sourcePath: String, issuerId: Int): String? {
        return when (val result = saveLogoImageWithValidation(sourcePath, issuerId)) {
            is LogoSaveResult.Success -> result.path
            else -> null
        }
    }

    actual fun saveLogoImageWithValidation(sourcePath: String, issuerId: Int): LogoSaveResult {
        return try {
            val sourceUri = Uri.parse(sourcePath)

            // Check file size first
            val fileDescriptor = context.contentResolver.openAssetFileDescriptor(sourceUri, "r")
            val fileSizeBytes = fileDescriptor?.length ?: -1L
            fileDescriptor?.close()

            val maxFileSizeKb = 2048L // 2 MB
            if (fileSizeBytes > maxFileSizeKb * 1024) {
                return LogoSaveResult.FileTooLarge(
                    sizeKb = fileSizeBytes / 1024,
                    maxSizeKb = maxFileSizeKb
                )
            }

            // Check mime type
            val mimeType = context.contentResolver.getType(sourceUri) ?: ""
            val validMimeTypes = listOf("image/png", "image/jpeg", "image/jpg")
            if (mimeType.isNotEmpty() && !validMimeTypes.any { mimeType.contains(it, ignoreCase = true) }) {
                return LogoSaveResult.InvalidFormat(mimeType)
            }

            // Decode the image to check dimensions
            val inputStream = context.contentResolver.openInputStream(sourceUri)
                ?: return LogoSaveResult.Error("Impossible d'ouvrir le fichier")

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            val imageWidth = options.outWidth
            val imageHeight = options.outHeight

            if (imageWidth <= 0 || imageHeight <= 0) {
                return LogoSaveResult.Error("Format d'image non reconnu")
            }

            // Check max source dimensions (allow reasonable photos, but not huge ones)
            val maxSourceWidth = 4000
            val maxSourceHeight = 4000
            if (imageWidth > maxSourceWidth || imageHeight > maxSourceHeight) {
                return LogoSaveResult.ImageTooLarge(
                    width = imageWidth,
                    height = imageHeight,
                    maxWidth = maxSourceWidth,
                    maxHeight = maxSourceHeight
                )
            }

            // Now decode the actual bitmap with sample size for large images
            val inputStream2 = context.contentResolver.openInputStream(sourceUri)
                ?: return LogoSaveResult.Error("Impossible d'ouvrir le fichier")

            val sampleSize = calculateInSampleSize(imageWidth, imageHeight, 1600, 800)
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val originalBitmap = BitmapFactory.decodeStream(inputStream2, null, decodeOptions)
            inputStream2.close()

            if (originalBitmap == null) {
                return LogoSaveResult.Error("Impossible de décoder l'image")
            }

            // Resize to final dimensions (larger for better PDF quality)
            val resizedBitmap = resizeIfNeeded(originalBitmap, maxWidth = 800, maxHeight = 400)

            // Save as PNG with timestamp to ensure unique filename
            val timestamp = System.currentTimeMillis()
            val fileName = "logo_${issuerId}_$timestamp.png"
            val destFile = File(logoDir, fileName)

            // Delete any existing logo for this issuer
            logoDir.listFiles()?.filter { it.name.startsWith("logo_${issuerId}_") }?.forEach { it.delete() }

            // Delete existing logo if present
            if (destFile.exists()) destFile.delete()

            FileOutputStream(destFile).use { out ->
                resizedBitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }

            if (resizedBitmap != originalBitmap) {
                resizedBitmap.recycle()
            }
            originalBitmap.recycle()

            LogoSaveResult.Success(fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            LogoSaveResult.Error(e.message ?: "Erreur inconnue")
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    actual fun deleteLogo(logoPath: String) {
        val file = File(logoDir, logoPath)
        if (file.exists()) file.delete()
    }

    actual fun getAbsolutePath(logoPath: String): String {
        return File(logoDir, logoPath).absolutePath
    }

    actual fun logoExists(logoPath: String): Boolean {
        return File(logoDir, logoPath).exists()
    }

    private fun resizeIfNeeded(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val ratio = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}

/**
 * Android implementation of ImagePickerLauncher.
 */
actual class ImagePickerLauncher(
    private val launcher: androidx.activity.result.ActivityResultLauncher<String>
) {
    actual fun launch() {
        launcher.launch("image/*")
    }
}

/**
 * Android implementation using ActivityResultContracts.GetContent().
 */
@Composable
actual fun rememberImagePickerLauncher(
    onResult: (ImagePickerResult) -> Unit
): ImagePickerLauncher {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onResult(ImagePickerResult.Success(uri.toString()))
        } else {
            onResult(ImagePickerResult.Cancelled)
        }
    }

    return remember(launcher) { ImagePickerLauncher(launcher) }
}

/**
 * Load a logo bitmap from an absolute file path.
 */
actual fun loadLogoBitmap(absolutePath: String): androidx.compose.ui.graphics.ImageBitmap? {
    return try {
        val file = File(absolutePath)
        if (!file.exists()) return null

        val bitmap = BitmapFactory.decodeFile(absolutePath) ?: return null

        // Convert Android Bitmap to Compose ImageBitmap
        bitmap.asImageBitmap()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Initialize Android context for ImageStorage.
 */
@Composable
actual fun InitImageContext() {
    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.SideEffect {
        AndroidImageContext.context = context
    }
}
