package com.a4a.g8invoicing.data

/**
 * Single source of truth for the language the app UI currently displays.
 * Kept as a plain object (not a Compose state) because it is read from
 * ordinary functions like [formatAmount] that must work outside composition
 * (PDF generation, background jobs). LocaleManager updates it on init and
 * whenever the user changes language.
 *
 * Rationale: the app ships fr/en/es/de strings. On a device set to Bengali
 * (or any other unsupported system language), the UI still shows French, so
 * amounts must be formatted with French conventions too — otherwise
 * NumberFormat.getCurrencyInstance() would fall back to Locale.getDefault()
 * and produce Bengali digits inside an otherwise-French document.
 */
object AppLocaleHolder {
    val supportedLanguages = setOf("fr", "en", "es", "de")

    /** BCP-47 language code actually rendered on screen. Defaults to French. */
    @Volatile
    var languageCode: String = "fr"
        private set

    fun set(code: String?) {
        languageCode = code?.takeIf { it in supportedLanguages } ?: "fr"
    }
}
