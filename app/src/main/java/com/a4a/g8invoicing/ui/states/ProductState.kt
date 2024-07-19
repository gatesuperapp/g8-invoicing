package com.a4a.g8invoicing.ui.states

import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.ui.shared.ScreenElement
import java.math.BigDecimal

// This Product is created to manipulate client data,
// without using the ProductSimple.kt generated by sql
// it allows to not specify the client id (that will be auto-incremented)
// and to change properties values (var instead of val)
data class ProductState(
    var productId: Int? = null,
    var name: TextFieldValue = TextFieldValue(""),
    var description: TextFieldValue? = null,
    var priceWithTax: BigDecimal? = null,
    var taxRate: BigDecimal? = null,
    var unit: TextFieldValue? = null,
    var errors: MutableList<Pair<ScreenElement, String?>> = mutableListOf(),
)
