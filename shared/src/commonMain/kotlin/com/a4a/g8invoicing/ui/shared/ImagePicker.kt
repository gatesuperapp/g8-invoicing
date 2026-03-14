package com.a4a.g8invoicing.ui.shared

/**
 * Result of logo save operation with error details.
 */
sealed class LogoSaveResult {
    data class Success(val path: String) : LogoSaveResult()
    data class ImageTooLarge(val width: Int, val height: Int, val maxWidth: Int, val maxHeight: Int) : LogoSaveResult()
    data class FileTooLarge(val sizeKb: Long, val maxSizeKb: Long) : LogoSaveResult()
    data class InvalidFormat(val format: String) : LogoSaveResult()
    data class Error(val message: String) : LogoSaveResult()
}

/**
 * Platform-specific image storage operations.
 */
expect class ImageStorage() {
    /**
     * Get the directory path for storing logos.
     */
    fun getLogoDirectory(): String

    /**
     * Copy an image to the app's internal storage with the given name.
     * Returns the relative path to the stored image, or null if failed.
     */
    fun saveLogoImage(sourcePath: String, issuerId: Int): String?

    /**
     * Save a logo image with validation.
     * Returns a LogoSaveResult indicating success or the type of error.
     *
     * Constraints:
     * - Max dimensions: 1000x500 pixels (will be resized to 200x100 for storage)
     * - Max file size: 2 MB
     * - Accepted formats: PNG, JPG, JPEG
     */
    fun saveLogoImageWithValidation(sourcePath: String, issuerId: Int): LogoSaveResult

    /**
     * Delete a logo image by its relative path.
     */
    fun deleteLogo(logoPath: String)

    /**
     * Get the absolute path for a logo given its relative path.
     */
    fun getAbsolutePath(logoPath: String): String

    /**
     * Check if a logo file exists.
     */
    fun logoExists(logoPath: String): Boolean
}

/**
 * Result of an image picker operation.
 */
sealed class ImagePickerResult {
    data class Success(val path: String) : ImagePickerResult()
    data object Cancelled : ImagePickerResult()
    data class Error(val message: String) : ImagePickerResult()
}

/**
 * State holder for image picker launcher.
 */
expect class ImagePickerLauncher {
    fun launch()
}

/**
 * Creates a remembered image picker launcher.
 * @param onResult Callback when an image is selected or cancelled
 */
@androidx.compose.runtime.Composable
expect fun rememberImagePickerLauncher(
    onResult: (ImagePickerResult) -> Unit
): ImagePickerLauncher

/**
 * Initialize platform-specific image context.
 * Must be called before using ImageStorage on Android.
 */
@androidx.compose.runtime.Composable
expect fun InitImageContext()
