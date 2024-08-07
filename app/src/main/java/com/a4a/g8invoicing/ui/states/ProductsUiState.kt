package com.a4a.g8invoicing.ui.states

import com.a4a.g8invoicing.ui.viewmodels.Message

data class ProductsUiState(
    val products: List<ProductState> = listOf(),
    val userMessages: List<Message> = listOf(), //TODO display error messages to users
    val isFetchingproducts: Boolean = false,
    val recomposeToUnselectAllCheckboxes: Boolean = false,
)

