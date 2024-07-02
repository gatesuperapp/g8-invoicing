package com.a4a.g8invoicing.ui.states

import com.a4a.g8invoicing.ui.screens.Message

data class ClientsOrIssuerUiState(
    val clientsOrIssuerList: List<ClientOrIssuerState> = listOf(),
    val userMessages: List<Message> = listOf(), //TODO display error messages to users
    val isFetching: Boolean = false,
    val recomposeToUnselectAllCheckboxes: Boolean = false,
)
