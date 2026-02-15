package com.a4a.g8invoicing

import androidx.compose.runtime.Composable

expect fun getPlatformName(): String

@Composable
expect fun getAppVersion(): String

/**
 * Set the application locale.
 * @param languageCode The language code (e.g., "fr", "en", "de") or null to use system default.
 */
expect fun setAppLocale(languageCode: String?)
