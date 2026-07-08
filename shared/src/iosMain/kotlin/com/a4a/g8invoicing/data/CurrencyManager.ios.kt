package com.a4a.g8invoicing.data

import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleCurrencyCode
import platform.Foundation.commonISOCurrencyCodes
import platform.Foundation.currentLocale
import platform.Foundation.localizedStringForCurrencyCode
import platform.Foundation.objectForKey

actual fun deviceDefaultCurrencyCode(): String {
    val code = NSLocale.currentLocale().objectForKey(NSLocaleCurrencyCode) as? String
    return code ?: "EUR"
}

actual fun availableCurrencyCodes(): List<String> {
    @Suppress("UNCHECKED_CAST")
    val codes = NSLocale.commonISOCurrencyCodes as? List<String> ?: return emptyList()
    return codes.filter { it.length == 3 }.distinct().sorted()
}

actual fun currencyDisplayName(code: String, uiLanguageCode: String?): String {
    val locale = uiLanguageCode?.let { NSLocale(localeIdentifier = it) } ?: NSLocale.currentLocale()
    return locale.localizedStringForCurrencyCode(code) ?: code
}

actual fun currencySymbol(code: String): String {
    // No equivalent to Java's Currency#getSymbol on Foundation; the localized
    // currency code formatted by NSNumberFormatter is the canonical path.
    // Return the code as a stable fallback; callers should prefer formatAmount
    // for actual display.
    return code
}
