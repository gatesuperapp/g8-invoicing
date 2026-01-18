package com.a4a.g8invoicing.ui.states

import androidx.compose.ui.text.input.TextFieldValue

data class AddressState(
    var id: Int? = null,
    var addressTitle: TextFieldValue? = null,
    var addressLine1: TextFieldValue? = null,
    var addressLine2: TextFieldValue? = null,
    var zipCode: TextFieldValue? = null,
    var city: TextFieldValue? = null,
)
