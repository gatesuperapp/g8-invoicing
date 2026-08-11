package com.a4a.g8invoicing

import com.a4a.g8invoicing.util.normalizeForSearch
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchNormalizeTest {

    @Test
    fun stripsCommonFrenchAccents() {
        assertEquals("cafe", "Café".normalizeForSearch())
        assertEquals("cafe", "CAFÉ".normalizeForSearch())
        assertEquals("cafe", "cafe".normalizeForSearch())
        assertEquals("etudiant", "étudiant".normalizeForSearch())
        assertEquals("noel", "Noël".normalizeForSearch())
        assertEquals("cœur", "cœur".normalizeForSearch()) // ligature stays (not a combining mark)
    }

    @Test
    fun stripsGermanAndSpanishDiacritics() {
        assertEquals("uber", "Über".normalizeForSearch())
        assertEquals("gruss", "Grüss".normalizeForSearch())
        assertEquals("manana", "Mañana".normalizeForSearch())
        assertEquals("pinata", "piñata".normalizeForSearch())
    }

    @Test
    fun lowercasesAscii() {
        assertEquals("acme corp", "ACME Corp".normalizeForSearch())
    }

    @Test
    fun handlesEmptyAndBlank() {
        assertEquals("", "".normalizeForSearch())
        assertEquals("   ", "   ".normalizeForSearch())
    }

    @Test
    fun leavesUnrelatedCharsAlone() {
        assertEquals("hello-world_42", "Hello-World_42".normalizeForSearch())
        assertEquals("a b c", "A B C".normalizeForSearch())
    }

    @Test
    fun idempotent() {
        val once = "Résumé".normalizeForSearch()
        val twice = once.normalizeForSearch()
        assertEquals(once, twice)
    }

    @Test
    fun matchesBothWays() {
        // The whole point: "café" contains "cafe" and vice versa after normalization.
        val haystack = "Café Créole".normalizeForSearch()
        val needleAccented = "Créo".normalizeForSearch()
        val needleFlat = "creo".normalizeForSearch()
        assertEquals(true, needleAccented in haystack)
        assertEquals(true, needleFlat in haystack)
    }
}
