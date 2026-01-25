package com.a4a.g8invoicing

import com.a4a.g8invoicing.data.setScale
import com.a4a.g8invoicing.data.stripTrailingZeros
import com.a4a.g8invoicing.data.toBigDecimalKmp
import com.a4a.g8invoicing.data.toIntKmp
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import kotlin.test.Test
import kotlin.test.assertEquals

class BigDecimalExtensionsTest {

    // toBigDecimalKmp tests
    @Test
    fun stringToBigDecimal_integer() {
        val result = "123".toBigDecimalKmp()
        assertEquals(BigDecimal.fromInt(123), result)
    }

    @Test
    fun stringToBigDecimal_decimal() {
        val result = "123.45".toBigDecimalKmp()
        assertEquals(BigDecimal.fromDouble(123.45), result)
    }

    @Test
    fun stringToBigDecimal_negative() {
        val result = "-99.99".toBigDecimalKmp()
        assertEquals(BigDecimal.fromDouble(-99.99), result)
    }

    @Test
    fun doubleToBigDecimal() {
        val result = 123.45.toBigDecimalKmp()
        assertEquals(BigDecimal.fromDouble(123.45), result)
    }

    @Test
    fun longToBigDecimal() {
        val result = 1000000L.toBigDecimalKmp()
        assertEquals(BigDecimal.fromLong(1000000L), result)
    }

    @Test
    fun intToBigDecimal() {
        val result = 42.toBigDecimalKmp()
        assertEquals(BigDecimal.fromInt(42), result)
    }

    // setScale tests
    @Test
    fun setScale_roundDown() {
        val value = BigDecimal.fromDouble(123.456)
        val result = value.setScale(2)
        assertEquals(BigDecimal.fromDouble(123.46), result) // ROUND_HALF_AWAY_FROM_ZERO
    }

    @Test
    fun setScale_noChange() {
        val value = BigDecimal.fromDouble(123.45)
        val result = value.setScale(2)
        assertEquals(BigDecimal.fromDouble(123.45), result)
    }

    @Test
    fun setScale_zeroDecimals() {
        val value = BigDecimal.fromDouble(123.56)
        val result = value.setScale(0)
        assertEquals(BigDecimal.fromInt(124), result)
    }

    @Test
    fun setScale_moreDecimals() {
        val value = BigDecimal.fromDouble(123.4)
        val result = value.setScale(3)
        // Should add trailing zeros conceptually
        assertEquals(BigDecimal.fromDouble(123.4), result)
    }

    // stripTrailingZeros tests
    @Test
    fun stripTrailingZeros_withTrailingZeros() {
        val value = BigDecimal.parseString("123.4500")
        val result = value.stripTrailingZeros()
        assertEquals(BigDecimal.parseString("123.45"), result)
    }

    @Test
    fun stripTrailingZeros_noTrailingZeros() {
        val value = BigDecimal.parseString("123.45")
        val result = value.stripTrailingZeros()
        assertEquals(BigDecimal.parseString("123.45"), result)
    }

    @Test
    fun stripTrailingZeros_integer() {
        val value = BigDecimal.fromInt(100)
        val result = value.stripTrailingZeros()
        assertEquals(BigDecimal.fromInt(100), result)
    }

    @Test
    fun stripTrailingZeros_allZerosAfterDecimal() {
        val value = BigDecimal.parseString("123.00")
        val result = value.stripTrailingZeros()
        assertEquals(BigDecimal.parseString("123"), result)
    }

    // toIntKmp tests
    @Test
    fun toIntKmp_wholeNumber() {
        val value = BigDecimal.fromInt(42)
        assertEquals(42, value.toIntKmp())
    }

    @Test
    fun toIntKmp_truncatesDecimals() {
        val value = BigDecimal.fromDouble(42.99)
        assertEquals(42, value.toIntKmp())
    }

    @Test
    fun toIntKmp_negative() {
        val value = BigDecimal.fromInt(-15)
        assertEquals(-15, value.toIntKmp())
    }

    @Test
    fun toIntKmp_zero() {
        val value = BigDecimal.ZERO
        assertEquals(0, value.toIntKmp())
    }
}
