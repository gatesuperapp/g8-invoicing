package com.a4a.g8invoicing.ui.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class ImageStorage actual constructor() {
    actual fun getLogoDirectory(): String {
        // TODO: Implement for iOS
        return ""
    }

    actual fun saveLogoImage(sourcePath: String, issuerId: Int): String? {
        // TODO: Implement for iOS
        return null
    }

    actual fun saveLogoImageWithValidation(sourcePath: String, issuerId: Int): LogoSaveResult {
        // TODO: Implement for iOS
        return LogoSaveResult.Error("iOS not yet implemented")
    }

    actual fun deleteLogo(logoPath: String) {
        // TODO: Implement for iOS
    }

    actual fun getAbsolutePath(logoPath: String): String {
        // TODO: Implement for iOS
        return logoPath
    }

    actual fun logoExists(logoPath: String): Boolean {
        // TODO: Implement for iOS
        return false
    }
}

/**
 * iOS stub implementation of ImagePickerLauncher.
 */
actual class ImagePickerLauncher(
    private val onLaunch: () -> Unit
) {
    actual fun launch() {
        // TODO: Implement for iOS
        onLaunch()
    }
}

/**
 * iOS stub implementation.
 */
@Composable
actual fun rememberImagePickerLauncher(
    onResult: (ImagePickerResult) -> Unit
): ImagePickerLauncher {
    return remember {
        ImagePickerLauncher {
            // TODO: Implement iOS image picker
            onResult(ImagePickerResult.Error("iOS not yet implemented"))
        }
    }
}

/**
 * iOS stub implementation for loading logo bitmap.
 */
actual fun loadLogoBitmap(absolutePath: String): androidx.compose.ui.graphics.ImageBitmap? {
    // TODO: Implement for iOS
    return null
}

/**
 * No-op for iOS - context initialization not needed.
 */
@Composable
actual fun InitImageContext() {
    // No initialization needed for iOS
}
