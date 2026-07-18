package com.a4a.g8invoicing.ui.states

import androidx.compose.ui.text.input.TextFieldValue

data class AddressState(
    var id: Int? = null,
    var originalAddressId: Int? = null,
    var addressTitle: TextFieldValue? = null,
    var addressLine1: TextFieldValue? = null,
    var addressLine2: TextFieldValue? = null,
    var zipCode: TextFieldValue? = null,
    var city: TextFieldValue? = null,
    // ISO 3166-1 alpha-2 country code ("FR", "DE", "US"...). Nullable so pre-Factur-X
    // addresses stay untouched at migration — new addresses default to the cascade
    // last-used → device locale country → "FR" (see CountryCodes.pickDefaultForNewAddress).
    var countryCode: String? = null,
)
