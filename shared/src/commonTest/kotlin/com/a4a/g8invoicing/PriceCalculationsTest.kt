package com.a4a.g8invoicing

import com.a4a.g8invoicing.data.util.calculatePriceWithTax
import com.a4a.g8invoicing.data.util.calculatePriceWithoutTax
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class PriceCalculationsTest {

    @Test
    fun calculatePriceWithTax_20percent() {
        val priceHT = BigDecimal.fromDouble(100.0)
        val taxRate = BigDecimal.fromDouble(20.0)

        val result = calculatePriceWithTax(priceHT, taxRate)

        assertEquals(BigDecimal.fromDouble(120.0), result)
    }

    @Test
    fun calculatePriceWithTax_5_5percent() {
        val priceHT = BigDecimal.fromDouble(100.0)
        val taxRate = BigDecimal.fromDouble(5.5)

        val result = calculatePriceWithTax(priceHT, taxRate)

        assertEquals(BigDecimal.fromDouble(105.5), result)
    }

    @Test
    fun calculatePriceWithTax_zero() {
        val priceHT = BigDecimal.fromDouble(100.0)
        val taxRate = BigDecimal.ZERO

        val result = calculatePriceWithTax(priceHT, taxRate)

        assertEquals(BigDecimal.fromDouble(100.0), result)
    }

    @Test
    fun calculatePriceWithTax_withDecimals() {
        val priceHT = BigDecimal.fromDouble(99.99)
        val taxRate = BigDecimal.fromDouble(20.0)

        val result = calculatePriceWithTax(priceHT, taxRate)

        // 99.99 * 1.20 = 119.988 → arrondi à 119.99
        assertEquals(BigDecimal.fromDouble(119.99), result)
    }

    @Test
    fun calculatePriceWithoutTax_20percent() {
        val priceTTC = BigDecimal.fromDouble(120.0)
        val taxRate = BigDecimal.fromDouble(20.0)

        val result = calculatePriceWithoutTax(priceTTC, taxRate)

        assertEquals(BigDecimal.fromDouble(100.0), result)
    }

    @Test
    fun calculatePriceWithoutTax_5_5percent() {
        val priceTTC = BigDecimal.fromDouble(105.5)
        val taxRate = BigDecimal.fromDouble(5.5)

        val result = calculatePriceWithoutTax(priceTTC, taxRate)

        assertEquals(BigDecimal.fromDouble(100.0), result)
    }

    @Test
    fun calculatePriceWithoutTax_zero() {
        val priceTTC = BigDecimal.fromDouble(100.0)
        val taxRate = BigDecimal.ZERO

        val result = calculatePriceWithoutTax(priceTTC, taxRate)

        assertEquals(BigDecimal.fromDouble(100.0), result)
    }

    @Test
    fun priceConversion_roundTrip() {
        // Verify HT → TTC → HT gives back the same value
        val originalHT = BigDecimal.fromDouble(85.50)
        val taxRate = BigDecimal.fromDouble(20.0)

        val ttc = calculatePriceWithTax(originalHT, taxRate)
        val backToHT = calculatePriceWithoutTax(ttc, taxRate)

        assertEquals(originalHT, backToHT)
    }
}
