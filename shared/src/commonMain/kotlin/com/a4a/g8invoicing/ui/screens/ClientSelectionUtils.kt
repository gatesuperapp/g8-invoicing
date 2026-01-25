package com.a4a.g8invoicing.ui.screens

import com.a4a.g8invoicing.ui.states.ClientRef
import com.a4a.g8invoicing.ui.states.ProductPrice

/**
 * Result of client filtering for additional price selection.
 */
data class ClientSelectionState(
    val availableClients: List<ClientRef>,
    val totalClientsInDatabase: Int
) {
    val hasNoClientsInDatabase: Boolean
        get() = totalClientsInDatabase == 0

    val allClientsAlreadyAssigned: Boolean
        get() = totalClientsInDatabase > 0 && availableClients.isEmpty()

    val hasAvailableClients: Boolean
        get() = availableClients.isNotEmpty()
}

/**
 * Filters clients for additional price selection.
 *
 * @param allClientsInDatabase All clients stored in the database
 * @param additionalPrices All additional prices for the current product
 * @param currentPriceId The ID of the price being edited (its clients should remain available)
 * @return ClientSelectionState with filtered available clients and total count
 */
fun filterClientsForAdditionalPrice(
    allClientsInDatabase: List<ClientRef>,
    additionalPrices: List<ProductPrice>?,
    currentPriceId: String
): ClientSelectionState {
    // Get IDs of clients already assigned to OTHER additional prices
    val alreadyAssignedClientIds = additionalPrices
        ?.filter { it.idStr != currentPriceId }
        ?.flatMap { it.clients.map { client -> client.id } }
        ?.toSet() ?: emptySet()

    // Filter out already assigned clients
    val availableClients = allClientsInDatabase.filter { client ->
        client.id !in alreadyAssignedClientIds
    }

    return ClientSelectionState(
        availableClients = availableClients,
        totalClientsInDatabase = allClientsInDatabase.size
    )
}
