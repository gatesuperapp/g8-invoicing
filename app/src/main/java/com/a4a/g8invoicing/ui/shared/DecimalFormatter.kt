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
    private val comaSeparator = ",".single()
    private val dotSeparator = ".".single()

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
            if (char == comaSeparator || char == dotSeparator
                && !hasDecimalSep && sb.isNotEmpty()) {
                sb.append(comaSeparator)
                hasDecimalSep = true
            }
        }

        return sb.toString()
    }
}
