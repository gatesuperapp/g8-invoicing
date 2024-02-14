package com.a4a.g8invoicing.ui.states

import com.a4a.g8invoicing.data.ProductEditable
import com.a4a.g8invoicing.ui.screens.Message

data class ProductsUiState(
    val products: List<ProductEditable> = listOf(),
    val userMessages: List<Message> = listOf(), //TODO display error messages to users
    val isFetchingproducts: Boolean = false,
    val recomposeToUnselectAllCheckboxes: Boolean = false,
)

