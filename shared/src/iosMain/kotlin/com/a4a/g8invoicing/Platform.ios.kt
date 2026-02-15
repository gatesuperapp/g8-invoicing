package com.a4a.g8invoicing

import androidx.compose.runtime.Composable
import platform.Foundation.NSBundle
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.currentLocale
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
