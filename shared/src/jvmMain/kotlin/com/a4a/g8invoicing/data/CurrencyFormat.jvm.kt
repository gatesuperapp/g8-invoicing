package com.a4a.g8invoicing.data

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import java.math.BigDecimal as JavaBigDecimal

private fun localeForCurrency(currencyCode: String): Locale {
    if (currencyCode == "EUR") return Locale.FRANCE
    return jvmLocaleForCurrency(currencyCode) ?: Locale.getDefault()
}

actual fun formatAmount(amount: BigDecimal, currencyCode: String): String {
    return try {
        val javaAmount = JavaBigDecimal(amount.toPlainString())
        val format = NumberFormat.getCurrencyInstance(localeForCurrency(currencyCode))
        format.currency = Currency.getInstance(currencyCode)
        format.format(javaAmount).normalizeSpaces()
    } catch (_: Exception) {
        "${amount.toPlainString()} $currencyCode"
    }
}

// NumberFormat returns narrow no-break (U+202F) and no-break (U+00A0) spaces
// that bold fonts often lack. Normalize to plain ASCII spaces so the glyph
// always renders.
private fun String.normalizeSpaces(): String =
    this.replace(' ', ' ').replace(' ', ' ')

