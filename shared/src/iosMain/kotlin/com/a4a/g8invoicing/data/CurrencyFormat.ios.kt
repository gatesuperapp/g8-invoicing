package com.a4a.g8invoicing.data

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import platform.Foundation.NSDecimalNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle

actual fun formatAmount(amount: BigDecimal, currencyCode: String): String {
    val formatter = NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterCurrencyStyle
        this.currencyCode = currencyCode
    }
    val number = NSDecimalNumber(string = amount.toPlainString())
    return formatter.stringFromNumber(number) ?: "${amount.toPlainString()} $currencyCode"
}
