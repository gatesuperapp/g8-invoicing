package com.a4a.g8invoicing.data

import com.ionspin.kotlin.bignum.decimal.BigDecimal

/**
 * Format a monetary amount using the currency's native conventions
 * (symbol position, decimal/thousands separators, number of decimals).
 *
 * EUR is forced to French formatting since g8 is French-first; other
 * currencies use the locale natively associated with them
 * (USD → en_US, COP → es_CO, JPY → ja_JP, etc.).
 *
 * Falls back to "<plainString> <code>" if the currency code is unknown.
 */
/**
 * @param languageCode BCP-47 code (fr/en/es/de) that drives the formatting
 * locale — separator style, symbol position. Pass a doc's frozen
 * [DocumentState.formatLocale] to keep the amount identical across app-language
 * switches. null → fall back to the current app language (AppLocaleHolder),
 * suitable for renders that aren't tied to a specific frozen doc.
 */
expect fun formatAmount(
    amount: BigDecimal,
    currencyCode: String = "EUR",
    languageCode: String? = null,
): String
