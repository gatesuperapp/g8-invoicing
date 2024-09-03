package com.a4a.g8invoicing.ui.states

import com.a4a.g8invoicing.ui.viewmodels.Message

data class CreditNotesUiState(
    val documentStates: List<CreditNoteState> = listOf(),
    val userMessages: List<Message> = listOf(), //TODO display error messages to users
    val isFetchingDeliveryNotes: Boolean = false
)
