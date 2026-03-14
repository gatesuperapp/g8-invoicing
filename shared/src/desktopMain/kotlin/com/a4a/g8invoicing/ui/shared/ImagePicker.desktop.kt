package com.a4a.g8invoicing.ui.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.io.FilenameFilter
import javax.imageio.ImageIO

actual class ImageStorage actual constructor() {
    private val logoDir: File
        get() {
            val appDir = File(System.getProperty("user.home"), ".g8invoicing")
            val dir = File(appDir, "logos")
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
            val sourceFile = File(sourcePath)
            if (!sourceFile.exists()) {
                return LogoSaveResult.Error("Fichier introuvable")
            }

            // Check file size
            val fileSizeBytes = sourceFile.length()
            val maxFileSizeKb = 2048L // 2 MB
            if (fileSizeBytes > maxFileSizeKb * 1024) {
                return LogoSaveResult.FileTooLarge(
                    sizeKb = fileSizeBytes / 1024,
                    maxSizeKb = maxFileSizeKb
                )
            }

            // Check format
            val extension = sourceFile.extension.lowercase()
            val validExtensions = listOf("png", "jpg", "jpeg")
            if (extension !in validExtensions) {
                return LogoSaveResult.InvalidFormat(extension)
            }

            val originalImage = ImageIO.read(sourceFile)
                ?: return LogoSaveResult.Error("Impossible de lire l'image")

            val imageWidth = originalImage.width
            val imageHeight = originalImage.height

            // Check max source dimensions
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

            val resizedImage = resizeIfNeeded(originalImage, maxWidth = 800, maxHeight = 400)

            // Save as PNG with timestamp to ensure unique filename
            val timestamp = System.currentTimeMillis()
            val fileName = "logo_${issuerId}_$timestamp.png"
            val destFile = File(logoDir, fileName)

            // Delete any existing logo for this issuer
            logoDir.listFiles()?.filter { it.name.startsWith("logo_${issuerId}_") }?.forEach { it.delete() }

            // Save as PNG
            ImageIO.write(resizedImage, "PNG", destFile)

            LogoSaveResult.Success(fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            LogoSaveResult.Error(e.message ?: "Erreur inconnue")
        }
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

    private fun resizeIfNeeded(image: BufferedImage, maxWidth: Int, maxHeight: Int): BufferedImage {
        val width = image.width
        val height = image.height

        if (width <= maxWidth && height <= maxHeight) {
            return image
        }

        val ratio = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        val scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH)
        val bufferedImage = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics = bufferedImage.createGraphics()
        graphics.drawImage(scaledImage, 0, 0, null)
        graphics.dispose()

        return bufferedImage
    }
}

/**
 * Desktop implementation of ImagePickerLauncher.
 */
actual class ImagePickerLauncher(
    private val onLaunch: () -> Unit
) {
    actual fun launch() {
        onLaunch()
    }
}

/**
 * Desktop implementation using FileDialog.
 */
@Composable
actual fun rememberImagePickerLauncher(
    onResult: (ImagePickerResult) -> Unit
): ImagePickerLauncher {
    return remember {
        ImagePickerLauncher {
            try {
                val dialog = FileDialog(null as Frame?, "Choisir un logo", FileDialog.LOAD)
                dialog.filenameFilter = FilenameFilter { _, name ->
                    val lower = name.lowercase()
                    lower.endsWith(".png") || lower.endsWith(".jpg") ||
                    lower.endsWith(".jpeg") || lower.endsWith(".gif")
                }
                dialog.isVisible = true

                val directory = dialog.directory
                val file = dialog.file

                if (directory != null && file != null) {
                    val fullPath = File(directory, file).absolutePath
                    onResult(ImagePickerResult.Success(fullPath))
                } else {
                    onResult(ImagePickerResult.Cancelled)
                }
            } catch (e: Exception) {
                onResult(ImagePickerResult.Error(e.message ?: "Unknown error"))
            }
        }
    }
}

/**
 * Load a logo bitmap from an absolute file path.
 */
actual fun loadLogoBitmap(absolutePath: String): ImageBitmap? {
    return try {
        val file = File(absolutePath)
        if (!file.exists()) return null

        val bufferedImage = ImageIO.read(file) ?: return null

        // Convert BufferedImage to Compose ImageBitmap
        bufferedImage.toComposeImageBitmap()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * No-op for Desktop - context initialization not needed.
 */
@Composable
actual fun InitImageContext() {
    // No initialization needed for Desktop
}
