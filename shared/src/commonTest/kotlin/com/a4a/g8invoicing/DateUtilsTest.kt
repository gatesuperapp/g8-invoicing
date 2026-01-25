package com.a4a.g8invoicing

import com.a4a.g8invoicing.data.util.DateUtils
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DateUtilsTest {

    @Test
    fun formatDate_singleDigitDayAndMonth() {
        val date = LocalDate(2024, 1, 5)
        val result = DateUtils.formatDate(date)
        assertEquals("05/01/2024", result)
    }

    @Test
    fun formatDate_doubleDigitDayAndMonth() {
        val date = LocalDate(2024, 12, 25)
        val result = DateUtils.formatDate(date)
        assertEquals("25/12/2024", result)
    }

    @Test
    fun parseDate_validFormat() {
        val result = DateUtils.parseDate("15/06/2024")

        assertNotNull(result)
        assertEquals(15, result.dayOfMonth)
        assertEquals(6, result.monthNumber)
        assertEquals(2024, result.year)
    }

    @Test
    fun parseDate_withPaddedZeros() {
        val result = DateUtils.parseDate("01/01/2024")

        assertNotNull(result)
        assertEquals(1, result.dayOfMonth)
        assertEquals(1, result.monthNumber)
        assertEquals(2024, result.year)
    }

    @Test
    fun parseDate_emptyString() {
        val result = DateUtils.parseDate("")
        assertNull(result)
    }

    @Test
    fun parseDate_blankString() {
        val result = DateUtils.parseDate("   ")
        assertNull(result)
    }

    @Test
    fun parseDate_invalidFormat() {
        val result = DateUtils.parseDate("2024-06-15")
        assertNull(result) // Wrong format (should be dd/MM/yyyy)
    }

    @Test
    fun parseDate_invalidDate() {
        val result = DateUtils.parseDate("32/13/2024")
        assertNull(result) // Invalid day and month
    }

    @Test
    fun parseDate_nonNumeric() {
        val result = DateUtils.parseDate("ab/cd/efgh")
        assertNull(result)
    }

    @Test
    fun getCurrentDateFormatted_returnsCorrectFormat() {
        val result = DateUtils.getCurrentDateFormatted()

        // Format should be dd/MM/yyyy
        assertTrue(result.matches(Regex("\\d{2}/\\d{2}/\\d{4}")))
    }

    @Test
    fun getDatePlusDaysFormatted_returnsCorrectFormat() {
        val result = DateUtils.getDatePlusDaysFormatted(30)

        // Format should be dd/MM/yyyy
        assertTrue(result.matches(Regex("\\d{2}/\\d{2}/\\d{4}")))
    }

    @Test
    fun getCurrentTimestamp_returnsCorrectFormat() {
        val result = DateUtils.getCurrentTimestamp()

        // Format should be yyyy-MM-dd HH:mm:ss
        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")))
    }

    @Test
    fun isDateBeforeToday_pastDate() {
        val pastDate = "01/01/2020"
        assertTrue(DateUtils.isDateBeforeToday(pastDate))
    }

    @Test
    fun isDateBeforeToday_futureDate() {
        val futureDate = "01/01/2030"
        assertFalse(DateUtils.isDateBeforeToday(futureDate))
    }

    @Test
    fun isDateBeforeToday_invalidDate() {
        val invalidDate = "invalid"
        assertFalse(DateUtils.isDateBeforeToday(invalidDate))
    }

    @Test
    fun currentTimeMillis_returnsPositiveValue() {
        val result = DateUtils.currentTimeMillis()
        assertTrue(result > 0)
    }

    @Test
    fun formatDate_parseDate_roundTrip() {
        val original = LocalDate(2024, 7, 14)
        val formatted = DateUtils.formatDate(original)
        val parsed = DateUtils.parseDate(formatted)

        assertEquals(original, parsed)
    }
}
