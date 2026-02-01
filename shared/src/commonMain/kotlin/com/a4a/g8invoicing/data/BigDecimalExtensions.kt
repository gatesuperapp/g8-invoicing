package com.a4a.g8invoicing.data

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.RoundingMode

// Extensions pour faciliter l'utilisation de bignum BigDecimal

fun String.toBigDecimalKmp(): BigDecimal = BigDecimal.parseString(this)

fun Double.toBigDecimalKmp(): BigDecimal = BigDecimal.fromDouble(this)

fun Long.toBigDecimalKmp(): BigDecimal = BigDecimal.fromLong(this)

fun Int.toBigDecimalKmp(): BigDecimal = BigDecimal.fromInt(this)

// Extensions utilitaires
fun BigDecimal.setScaleKmp(scale: Int, roundingMode: RoundingMode = RoundingMode.ROUND_HALF_AWAY_FROM_ZERO): BigDecimal {
    return this.roundToDigitPositionAfterDecimalPoint(scale.toLong(), roundingMode)
}

// Extension pour remplacer setScale(int, RoundingMode) de java.math.BigDecimal
fun BigDecimal.setScale(scale: Int, roundingMode: RoundingMode = RoundingMode.ROUND_HALF_AWAY_FROM_ZERO): BigDecimal {
    return this.roundToDigitPositionAfterDecimalPoint(scale.toLong(), roundingMode)
}

// Extension pour simuler stripTrailingZeros() - retourne une représentation sans les zéros finaux
fun BigDecimal.stripTrailingZeros(): BigDecimal {
    // toPlainString supprime les zéros trailing lors du re-parsing
    val plainStr = this.toPlainString()
    return if (plainStr.contains('.')) {
        BigDecimal.parseString(plainStr.trimEnd('0').trimEnd('.'))
    } else {
        this
    }
}

// Conversion vers Int
fun BigDecimal.toIntKmp(): Int = this.intValue(false)

// Format avec exactement 2 décimales (ex: 3.5 -> "3.50", 3 -> "3.00")
fun BigDecimal.toStringWithTwoDecimals(): String {
    val rounded = this.roundToDigitPositionAfterDecimalPoint(2, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
    val str = rounded.toPlainString()
    return when {
        !str.contains('.') -> "$str.00"
        str.substringAfter('.').length == 1 -> "${str}0"
        else -> str
    }
}
