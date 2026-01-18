package com.a4a.g8invoicing.data.util

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode

/**
 * Calculate price with tax from price without tax
 */
fun calculatePriceWithTax(priceWithoutTax: BigDecimal, taxRate: BigDecimal): BigDecimal {
    val hundred = BigDecimal.fromInt(100)
    val decimalMode = DecimalMode(decimalPrecision = 10, roundingMode = RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
    val taxAmount = (priceWithoutTax * taxRate).divide(hundred, decimalMode)
        .roundToDigitPositionAfterDecimalPoint(4, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
    return (priceWithoutTax + taxAmount)
        .roundToDigitPositionAfterDecimalPoint(2, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
}

/**
 * Calculate price without tax from price with tax
 */
fun calculatePriceWithoutTax(priceWithTax: BigDecimal, taxRate: BigDecimal): BigDecimal {
    val hundred = BigDecimal.fromInt(100)
    val decimalMode = DecimalMode(decimalPrecision = 10, roundingMode = RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
    val divisor = BigDecimal.ONE + taxRate.divide(hundred, decimalMode)
    return priceWithTax.divide(divisor, decimalMode)
        .roundToDigitPositionAfterDecimalPoint(2, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
}
