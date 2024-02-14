package com.a4a.g8invoicing.data

import androidx.compose.ui.text.input.TextFieldValue
import java.math.BigDecimal

// This Product is created to manipulate client data,
// without using the ProductSimple.kt generated by sql
// it allows to not specify the client id (that will be auto-incremented)
// and to change properties values (var instead of val)
data class DocumentProductEditable(
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
