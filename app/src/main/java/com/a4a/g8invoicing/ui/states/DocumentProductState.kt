package com.a4a.g8invoicing.ui.states

import androidx.compose.ui.text.input.TextFieldValue
import java.math.BigDecimal

data class DocumentProductState(
    var id: Int? = null,
    var name: TextFieldValue = TextFieldValue(""),
    var description: TextFieldValue? = null,
    var finalPrice: BigDecimal = BigDecimal(0),
    var priceWithoutTax: BigDecimal = BigDecimal(0),
    var taxRate: BigDecimal = BigDecimal(0),
    var quantity: BigDecimal = BigDecimal(1),
    var unit: TextFieldValue? = null,
    var productId: Int? = null,
)
