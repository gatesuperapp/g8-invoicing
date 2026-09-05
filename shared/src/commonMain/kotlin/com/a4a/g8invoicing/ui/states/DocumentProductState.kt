package com.a4a.g8invoicing.ui.states

import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.data.models.ProductNature
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.ionspin.kotlin.bignum.decimal.BigDecimal

// The equivalent of ProductState, but used to save the products
// linked to the documents, so the product is not deleted from the doc
// when deleted from the product list.
data class DocumentProductState(
    var id: Int? = null,
    var name: TextFieldValue = TextFieldValue(""),
    var description: TextFieldValue? = null,
    var priceWithoutTax: BigDecimal? = null,
    var priceWithTax: BigDecimal? = null,
    var taxRate: BigDecimal? = null,
    var quantity: BigDecimal = BigDecimal.ONE,
    var unit: TextFieldValue? = null,
    var unitCode: String? = null,
    var type: ProductNature? = null,
    var productId: Int? = null,
    val linkedDate: String? = null,
    val linkedDocNumber: String? = null,
    // Origin of [linkedDocNumber] — either a delivery-note number or a
    // quote number, populated by [InvoiceLocalDataSource.fetchDocumentProducts]
    // depending on which link table hit. Nullable = unknown (safe default,
    // callers that only care about display fall back to old behaviour).
    val linkedDocType: LinkedDocType? = null,
    var errors: MutableList<Pair<ScreenElement, String?>> = mutableListOf(),
    var sortOrder: Int? = null // remember product sorting in document
)

/**
 * Origin of a `DocumentProductState.linkedDocNumber`. Drives per-line
 * CII emission (`DeliveryNoteReferencedDocument` vs `AdditionalReferenced­
 * Document` with TypeCode 1001 for a quote), so the receiver knows what
 * type of source doc the invoice line traces back to.
 */
enum class LinkedDocType { DELIVERY_NOTE, QUOTE }
