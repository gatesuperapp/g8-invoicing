package com.a4a.g8invoicing.ui.states

data class InvoicesUiState(
    val documentStates: List<InvoiceState> = listOf(),
    val userMessages: List<Message> = listOf(),
    val isFetchingDeliveryNotes: Boolean = false
)

data class DeliveryNotesUiState(
    val deliveryNoteStates: List<DeliveryNoteState> = listOf(),
    val userMessages: List<Message> = listOf(),
    val isFetchingDeliveryNotes: Boolean = false
)

data class CreditNotesUiState(
    val documentStates: List<CreditNoteState> = listOf(),
    val userMessages: List<Message> = listOf(),
    val isFetchingCreditNotes: Boolean = false
)

data class ProductsUiState(
    val products: List<ProductState> = listOf(),
    val userMessages: List<Message> = listOf(),
    val isFetchingProducts: Boolean = false
)

data class ClientsOrIssuerUiState(
    val clientsOrIssuerList: List<ClientOrIssuerState> = listOf(),
    val userMessages: List<Message> = listOf(),
    val isFetchingClientsOrIssuers: Boolean = false
)
