package com.a4a.g8invoicing.ui.states

import com.a4a.g8invoicing.data.ClientOrIssuerEditable
import com.a4a.g8invoicing.ui.screens.Message

data class ClientsOrIssuerUiState(
    val clientsOrIssuers: List<ClientOrIssuerEditable> = listOf(),
    val userMessages: List<Message> = listOf(), //TODO display error messages to users
    val isFetching: Boolean = false,
    val recomposeToUnselectAllCheckboxes: Boolean = false,
)
