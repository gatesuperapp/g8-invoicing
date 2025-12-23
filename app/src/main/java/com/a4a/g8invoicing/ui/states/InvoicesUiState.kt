package com.a4a.g8invoicing.ui.states

import com.a4a.g8invoicing.ui.viewmodels.Message

data class InvoicesUiState(
    val documentStates: List<InvoiceState> = listOf(),
    val userMessages: List<Message> = listOf(), //TODO display error messages to users
    val isFetchingDeliveryNotes: Boolean = false
)
