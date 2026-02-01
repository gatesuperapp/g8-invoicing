package com.a4a.g8invoicing.ui.states

import androidx.compose.ui.text.input.TextFieldValue

data class EmailState(
    var id: Int? = null,
    var email: TextFieldValue = TextFieldValue("")
)
