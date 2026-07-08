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

/**
 * Returns the current system locale as a two-letter language code (e.g., "fr", "en").
 * Used to resolve AppLanguage.SYSTEM to a concrete code when talking to the API
 * (magic-link emails, etc.) — the server needs a concrete tag, null falls back to English.
 */
expect fun getSystemLocaleCode(): String
