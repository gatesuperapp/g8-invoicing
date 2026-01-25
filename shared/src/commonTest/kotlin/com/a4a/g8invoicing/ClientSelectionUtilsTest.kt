package com.a4a.g8invoicing

import com.a4a.g8invoicing.ui.screens.filterClientsForAdditionalPrice
import com.a4a.g8invoicing.ui.states.ClientRef
import com.a4a.g8invoicing.ui.states.ProductPrice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClientSelectionUtilsTest {

    // Test data
    private val clientAlice = ClientRef(id = 1, name = "Alice")
    private val clientBob = ClientRef(id = 2, name = "Bob")
    private val clientCharlie = ClientRef(id = 3, name = "Charlie")

    @Test
    fun noClientsInDatabase_showsNoClientsMessage() {
        val result = filterClientsForAdditionalPrice(
            allClientsInDatabase = emptyList(),
            additionalPrices = null,
            currentPriceId = "price-1"
        )

        assertTrue(result.hasNoClientsInDatabase)
        assertFalse(result.allClientsAlreadyAssigned)
        assertFalse(result.hasAvailableClients)
        assertEquals(0, result.totalClientsInDatabase)
        assertTrue(result.availableClients.isEmpty())
    }

    @Test
    fun allClientsAssignedToOtherPrices_showsAllAssignedMessage() {
        val allClients = listOf(clientAlice, clientBob)
        val additionalPrices = listOf(
            ProductPrice(
                idStr = "price-1",
                clients = listOf(clientAlice)
            ),
            ProductPrice(
                idStr = "price-2",
                clients = listOf(clientBob)
            )
        )

        // Editing price-3, but Alice is in price-1 and Bob is in price-2
        val result = filterClientsForAdditionalPrice(
            allClientsInDatabase = allClients,
            additionalPrices = additionalPrices,
            currentPriceId = "price-3"
        )

        assertFalse(result.hasNoClientsInDatabase)
        assertTrue(result.allClientsAlreadyAssigned)
        assertFalse(result.hasAvailableClients)
        assertEquals(2, result.totalClientsInDatabase)
        assertTrue(result.availableClients.isEmpty())
    }

    @Test
    fun someClientsAvailable_showsOnlyAvailableClients() {
        val allClients = listOf(clientAlice, clientBob, clientCharlie)
        val additionalPrices = listOf(
            ProductPrice(
                idStr = "price-1",
                clients = listOf(clientAlice)
            )
        )

        // Editing price-2, Alice is already in price-1
        val result = filterClientsForAdditionalPrice(
            allClientsInDatabase = allClients,
            additionalPrices = additionalPrices,
            currentPriceId = "price-2"
        )

        assertFalse(result.hasNoClientsInDatabase)
        assertFalse(result.allClientsAlreadyAssigned)
        assertTrue(result.hasAvailableClients)
        assertEquals(3, result.totalClientsInDatabase)
        assertEquals(2, result.availableClients.size)
        assertTrue(result.availableClients.contains(clientBob))
        assertTrue(result.availableClients.contains(clientCharlie))
        assertFalse(result.availableClients.contains(clientAlice))
    }

    @Test
    fun editingExistingPrice_itsOwnClientsRemainAvailable() {
        val allClients = listOf(clientAlice, clientBob)
        val additionalPrices = listOf(
            ProductPrice(
                idStr = "price-1",
                clients = listOf(clientAlice)
            ),
            ProductPrice(
                idStr = "price-2",
                clients = listOf(clientBob)
            )
        )

        // Editing price-1 which already has Alice - Alice should still be available
        val result = filterClientsForAdditionalPrice(
            allClientsInDatabase = allClients,
            additionalPrices = additionalPrices,
            currentPriceId = "price-1"
        )

        assertFalse(result.hasNoClientsInDatabase)
        assertFalse(result.allClientsAlreadyAssigned)
        assertTrue(result.hasAvailableClients)
        assertEquals(2, result.totalClientsInDatabase)
        assertEquals(1, result.availableClients.size)
        assertTrue(result.availableClients.contains(clientAlice))
        assertFalse(result.availableClients.contains(clientBob))
    }

    @Test
    fun noAdditionalPrices_allClientsAvailable() {
        val allClients = listOf(clientAlice, clientBob, clientCharlie)

        val result = filterClientsForAdditionalPrice(
            allClientsInDatabase = allClients,
            additionalPrices = null,
            currentPriceId = "price-1"
        )

        assertFalse(result.hasNoClientsInDatabase)
        assertFalse(result.allClientsAlreadyAssigned)
        assertTrue(result.hasAvailableClients)
        assertEquals(3, result.totalClientsInDatabase)
        assertEquals(3, result.availableClients.size)
    }

    @Test
    fun emptyAdditionalPrices_allClientsAvailable() {
        val allClients = listOf(clientAlice, clientBob)

        val result = filterClientsForAdditionalPrice(
            allClientsInDatabase = allClients,
            additionalPrices = emptyList(),
            currentPriceId = "price-1"
        )

        assertFalse(result.hasNoClientsInDatabase)
        assertFalse(result.allClientsAlreadyAssigned)
        assertTrue(result.hasAvailableClients)
        assertEquals(2, result.totalClientsInDatabase)
        assertEquals(2, result.availableClients.size)
    }

    @Test
    fun multipleClientsPerPrice_allFilteredCorrectly() {
        val allClients = listOf(clientAlice, clientBob, clientCharlie)
        val additionalPrices = listOf(
            ProductPrice(
                idStr = "price-1",
                clients = listOf(clientAlice, clientBob) // Both assigned to price-1
            )
        )

        // Editing price-2, both Alice and Bob are in price-1
        val result = filterClientsForAdditionalPrice(
            allClientsInDatabase = allClients,
            additionalPrices = additionalPrices,
            currentPriceId = "price-2"
        )

        assertEquals(1, result.availableClients.size)
        assertTrue(result.availableClients.contains(clientCharlie))
    }
}
