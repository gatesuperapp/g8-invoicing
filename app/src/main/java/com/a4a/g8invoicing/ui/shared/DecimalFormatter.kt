package com.a4a.g8invoicing.ui.shared

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

// Credits: Jemshit Tuvakov https://dev.to/tuvakov/decimal-input-formatting-with-jetpack-composes-visualtransformation-110n

class DecimalFormatter(
    symbols: DecimalFormatSymbols = DecimalFormatSymbols.getInstance()
) {

    // Not used yet, Adds a separator for thousands (ex: 2,500 instead of 2500)
    private val thousandsSeparator = symbols.groupingSeparator
    // Decimal separator will be "," for France for instance, "." for USA
    private val decimalSeparator = symbols.decimalSeparator

    fun cleanup(input: String): String {

        if (input.matches("\\D".toRegex())) return ""
        if (input.matches("0+".toRegex())) return "0"

        val sb = StringBuilder()

        var hasDecimalSep = false

        for (char in input) {
            if (char.isDigit()) {
                sb.append(char)
                continue
            }
            if (char == decimalSeparator && !hasDecimalSep && sb.isNotEmpty()) {
                sb.append(char)
                hasDecimalSep = true
            }
        }

        return sb.toString()
    }

    // Not used yet, Adds a separator for thousands (ex: 2,500 instead of 2500)
    fun formatForVisual(input: String): String {

        val split = input.split(decimalSeparator)

        val intPart = split[0]
            .reversed()
            .chunked(3)
            .joinToString(separator = thousandsSeparator.toString())
            .reversed()

        val fractionPart = split.getOrNull(1)

        return if (fractionPart == null) intPart else intPart + decimalSeparator + fractionPart
    }
}
