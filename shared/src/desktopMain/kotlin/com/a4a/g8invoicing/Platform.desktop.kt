package com.a4a.g8invoicing

import androidx.compose.runtime.Composable
import java.util.Locale

actual fun getPlatformName(): String = "Desktop"

@Composable
actual fun getAppVersion(): String {
    // Try jpackage property first (set when running packaged app)
    return System.getProperty("jpackage.app-version")
        ?: object {}.javaClass.`package`?.implementationVersion
        ?: "1.4.0" // Fallback to build version
}

actual fun setAppLocale(languageCode: String?) {
    val locale = if (languageCode != null) {
        Locale.forLanguageTag(languageCode)
    } else {
        Locale.getDefault()
    }
    Locale.setDefault(locale)
}
