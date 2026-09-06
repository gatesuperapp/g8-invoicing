package com.a4a.g8invoicing

import androidx.compose.runtime.Composable
import platform.Foundation.NSBundle
import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleCountryCode
import platform.Foundation.NSUserDefaults
import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.Foundation.displayNameForKey
import platform.Foundation.languageCode

actual fun getPlatformName(): String = "iOS"

@Composable
actual fun getAppVersion(): String {
    return NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
        ?: "unknown"
}

actual fun setAppLocale(languageCode: String?) {
    val code = languageCode ?: NSLocale.currentLocale.languageCode
    NSUserDefaults.standardUserDefaults.setObject(listOf(code), forKey = "AppleLanguages")
    NSUserDefaults.standardUserDefaults.synchronize()
}

actual fun getSystemLocaleCode(): String =
    NSLocale.currentLocale.languageCode.takeIf { it.isNotEmpty() } ?: "en"

actual fun getSystemCountryCode(): String =
    NSLocale.currentLocale.countryCode?.uppercase() ?: ""

actual fun getLocalizedCountryName(code: String, languageCode: String): String? {
    // Build an NSLocale for the target app language, then ask it to render
    // the country name. Returns null when Apple's locale layer doesn't
    // recognise the code so the caller can fall back to the curated map.
    val target = NSLocale(languageCode.ifBlank { "en" })
    val name = target.displayNameForKey(NSLocaleCountryCode, code.uppercase()) as? String
    return name?.takeIf { it.isNotBlank() && !it.equals(code, ignoreCase = true) }
}
