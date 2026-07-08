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
expect fun formatAmount(amount: BigDecimal, currencyCode: String = "EUR"): String
