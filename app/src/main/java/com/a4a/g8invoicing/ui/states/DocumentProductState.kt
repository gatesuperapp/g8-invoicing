package com.a4a.g8invoicing.ui.states

import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.ui.shared.ScreenElement
import java.math.BigDecimal

// The equivalent of ProductState, but used to save the products
// linked to the documents, so the product is not deleted from the doc
// when deleted from the product list.
data class DocumentProductState(
    var id: Int? = null,
    var name: TextFieldValue = TextFieldValue(""),
    var description: TextFieldValue? = null,
    var priceWithTax: BigDecimal? = null,
    var taxRate: BigDecimal? = null,
    var quantity: BigDecimal = BigDecimal(1),
    var unit: TextFieldValue? = null,
    var productId: Int? = null,
    var page: Int = 1,
    var errors: MutableList<Pair<ScreenElement, String?>> = mutableListOf(),
)
