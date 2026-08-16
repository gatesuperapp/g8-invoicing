package com.a4a.g8invoicing.ui.states

import androidx.compose.ui.text.input.TextFieldValue

data class IssuerBankState(
    var id: Int? = null,
    var label: TextFieldValue? = null,
    // ISO 3166-1 alpha-2. Determines the identifier field's label + validation
    // (IBAN vs domestic account number). Null = fall back to the company's
    // country when rendering.
    var countryCode: String? = null,
    // IBAN for IBAN countries, domestic BBAN otherwise. Single string like
    // Factur-X BT-84 expects.
    var identifier: TextFieldValue = TextFieldValue(""),
    var bic: TextFieldValue = TextFieldValue(""),
    var sortOrder: Int = 0,
)
