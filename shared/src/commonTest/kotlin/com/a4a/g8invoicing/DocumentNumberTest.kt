package com.a4a.g8invoicing

import com.a4a.g8invoicing.data.incrementDocumentNumber
import kotlin.test.Test
import kotlin.test.assertEquals

class DocumentNumberTest {

    @Test
    fun incrementDocumentNumber_simpleNumber() {
        val result = incrementDocumentNumber("FA-001")
        assertEquals("FA-002", result)
    }

    @Test
    fun incrementDocumentNumber_rolloverToFourDigits() {
        val result = incrementDocumentNumber("FA-999")
        assertEquals("FA-1000", result)
    }

    @Test
    fun incrementDocumentNumber_withYear() {
        val result = incrementDocumentNumber("FA-2024-001")
        assertEquals("FA-2024-002", result)
    }

    @Test
    fun incrementDocumentNumber_noPrefix() {
        val result = incrementDocumentNumber("001")
        assertEquals("002", result)
    }

    @Test
    fun incrementDocumentNumber_singleDigit() {
        val result = incrementDocumentNumber("FA-1")
        assertEquals("FA-002", result)
    }

    @Test
    fun incrementDocumentNumber_twoDigits() {
        val result = incrementDocumentNumber("FA-99")
        assertEquals("FA-100", result)
    }

    @Test
    fun incrementDocumentNumber_noDigits() {
        val result = incrementDocumentNumber("FA-ABC")
        assertEquals("FA-ABC", result) // Returns unchanged if no digits
    }

    @Test
    fun incrementDocumentNumber_emptyString() {
        val result = incrementDocumentNumber("")
        assertEquals("", result)
    }

    @Test
    fun incrementDocumentNumber_deliveryNote() {
        val result = incrementDocumentNumber("BL-2024-015")
        assertEquals("BL-2024-016", result)
    }

    @Test
    fun incrementDocumentNumber_creditNote() {
        val result = incrementDocumentNumber("AV-2024-003")
        assertEquals("AV-2024-004", result)
    }

    @Test
    fun incrementDocumentNumber_preservesPadding() {
        val result = incrementDocumentNumber("FA-005")
        assertEquals("FA-006", result) // Padding to 3 digits minimum
    }

    @Test
    fun incrementDocumentNumber_largeNumber() {
        val result = incrementDocumentNumber("FA-12345")
        assertEquals("FA-12346", result)
    }
}
