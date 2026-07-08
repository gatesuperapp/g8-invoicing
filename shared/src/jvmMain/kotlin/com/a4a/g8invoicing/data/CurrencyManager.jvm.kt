package com.a4a.g8invoicing.data

import java.util.Currency
import java.util.Locale

actual fun deviceDefaultCurrencyCode(): String {
    return try {
        Currency.getInstance(Locale.getDefault()).currencyCode
    } catch (_: Throwable) {
        "EUR"
    }
}

actual fun availableCurrencyCodes(): List<String> {
    // Only currencies tied to at least one current locale: filters out
    // historical/legacy ISO codes the JDK still ships (FRF, DEM, BRC, etc.).
    return localeByCurrency.keys.asSequence()
        .map { it.currencyCode }
        .filter { it.length == 3 }
        .distinct()
        .sorted()
        .toList()
}

actual fun currencyDisplayName(code: String, uiLanguageCode: String?): String {
    return try {
        val locale = uiLanguageCode?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault()
        Currency.getInstance(code).getDisplayName(locale)
    } catch (_: Throwable) {
        code
    }
}

// Reverse map built once at first access: avoids re-scanning every available
// JVM locale (~750) for every currency lookup (the picker triggers ~150 of
// those in a row).
private val localeByCurrency: Map<Currency, Locale> by lazy {
    val map = mutableMapOf<Currency, Locale>()
    Locale.getAvailableLocales().forEach { locale ->
        if (locale.country.isEmpty()) return@forEach
        try {
            val c = Currency.getInstance(locale)
            map.putIfAbsent(c, locale)
        } catch (_: Throwable) {
            // Skip locales without a country-bound currency.
        }
    }
    map
}

internal fun jvmLocaleForCurrency(code: String): Locale? {
    return try {
        localeByCurrency[Currency.getInstance(code)]
    } catch (_: Throwable) {
        null
    }
}

actual fun currencySymbol(code: String): String {
    return try {
        val currency = Currency.getInstance(code)
        currency.getSymbol(localeByCurrency[currency] ?: Locale.ROOT)
    } catch (_: Throwable) {
        code
    }
}
