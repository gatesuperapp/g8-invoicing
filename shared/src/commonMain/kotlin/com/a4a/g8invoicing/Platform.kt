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

/**
 * Returns the current system country as a two-letter uppercase ISO 3166-1 code
 * (e.g., "FR", "DE", "US"). Used as the last-fallback default for the address country
 * on new addresses when no previous address exists — the primary cascade is
 * "last-created address country → this system country → 'FR'".
 * Empty string when the device locale exposes no country (rare — treat as unknown).
 */
expect fun getSystemCountryCode(): String
