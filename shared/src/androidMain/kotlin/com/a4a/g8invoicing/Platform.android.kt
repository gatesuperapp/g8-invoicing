package com.a4a.g8invoicing

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat

actual fun getPlatformName(): String = "Android"

@Composable
actual fun getAppVersion(): String {
    val context = LocalContext.current
    return try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
    } catch (e: Exception) {
        "unknown"
    }
}

actual fun setAppLocale(languageCode: String?) {
    val localeList = if (languageCode != null) {
        LocaleListCompat.forLanguageTags(languageCode)
    } else {
        LocaleListCompat.getEmptyLocaleList()
    }
    AppCompatDelegate.setApplicationLocales(localeList)
}
