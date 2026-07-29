package com.a4a.g8invoicing.data

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import platform.Foundation.NSDecimalNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle

actual fun formatAmount(
    amount: BigDecimal,
    currencyCode: String,
    languageCode: String?,
): String {
    // languageCode is accepted for API parity with the JVM actual; the iOS
    // formatter is still driven by NSLocale.currentLocale for now. When the
    // iOS build actually renders PDFs, plumb languageCode through NSLocale
    // localeWithLocaleIdentifier: to match the doc-frozen locale.
    val formatter = NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterCurrencyStyle
        this.currencyCode = currencyCode
    }
    val number = NSDecimalNumber(string = amount.toPlainString())
    return formatter.stringFromNumber(number) ?: "${amount.toPlainString()} $currencyCode"
}
