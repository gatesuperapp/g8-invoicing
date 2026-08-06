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
    // Also align the JVM-global Locale.getDefault(). Compose Resources' suspend
    // getString() reads Locale.getDefault() via getSystemResourceEnvironment(),
    // which AppCompatDelegate.setApplicationLocales does NOT reliably update on
    // Android — so the picker's stringResource (composition-scoped) would show
    // EN while a coroutine-side getString() would still return FR. Setting the
    // JVM default explicitly closes that gap.
    if (languageCode != null) {
        java.util.Locale.setDefault(java.util.Locale.forLanguageTag(languageCode))
    }
}

actual fun getSystemLocaleCode(): String = java.util.Locale.getDefault().language.ifEmpty { "en" }

actual fun getSystemCountryCode(): String = java.util.Locale.getDefault().country.uppercase()
