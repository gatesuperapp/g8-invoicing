package com.a4a.g8invoicing.ui.states

import com.a4a.g8invoicing.ui.screens.Message

data class DeliveryNotesUiState(
    val deliveryNoteItems: List<DeliveryNoteState> = listOf(),
    val userMessages: List<Message> = listOf(), //TODO display error messages to users
    val isFetchingDeliveryNotes: Boolean = false,
)
