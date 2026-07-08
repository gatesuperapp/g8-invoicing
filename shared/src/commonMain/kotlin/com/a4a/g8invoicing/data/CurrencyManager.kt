package com.a4a.g8invoicing.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.russhwolf.settings.Settings

/**
 * Returns the ISO 4217 currency code derived from the device locale
 * (e.g. "EUR" on a French phone, "USD" on a US phone). Falls back to
 * "EUR" if the locale has no associated currency.
 */
expect fun deviceDefaultCurrencyCode(): String

/**
 * Returns the available ISO 4217 currency codes (3-letter, uppercase).
 */
expect fun availableCurrencyCodes(): List<String>

/**
 * Returns the localized display name of a currency code in the given
 * UI language code (e.g. "fr" → "Euro", "en" → "Euro", "de" → "Euro";
 * "fr" + "USD" → "dollar des États-Unis").
 */
expect fun currencyDisplayName(code: String, uiLanguageCode: String?): String

/**
 * Returns the currency symbol for a code, picked in the currency's
 * native locale so it matches what `formatAmount` will display
 * (e.g. "EUR" → "€", "USD" → "$", "CHF" → "CHF").
 */
expect fun currencySymbol(code: String): String

class CurrencyManager(private val settings: Settings = Settings()) {
    companion object {
        private const val KEY_CURRENCY = "app_currency"
        private const val KEY_CURRENCY_RECENT = "app_currency_recent"
        private const val RECENT_MAX = 5
        const val DEFAULT_FALLBACK = "EUR"
    }

    var currentCurrency: String by mutableStateOf(loadSavedCurrency())
        private set

    var recentCurrencies: List<String> by mutableStateOf(loadRecentCurrencies())
        private set

    private fun loadSavedCurrency(): String {
        return settings.getStringOrNull(KEY_CURRENCY) ?: deviceDefaultCurrencyCode()
    }

    private fun loadRecentCurrencies(): List<String> {
        val raw = settings.getStringOrNull(KEY_CURRENCY_RECENT) ?: return emptyList()
        return raw.split(",").map { it.trim() }.filter { it.length == 3 }
    }

    fun setCurrency(code: String) {
        val normalized = code.uppercase()
        currentCurrency = normalized
        settings.putString(KEY_CURRENCY, normalized)
        pushToRecent(normalized)
    }

    private fun pushToRecent(code: String) {
        val updated = (listOf(code) + recentCurrencies.filter { it != code }).take(RECENT_MAX)
        recentCurrencies = updated
        settings.putString(KEY_CURRENCY_RECENT, updated.joinToString(","))
    }
}
