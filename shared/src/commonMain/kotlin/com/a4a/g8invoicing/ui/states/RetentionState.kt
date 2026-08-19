package com.a4a.g8invoicing.ui.states

import androidx.compose.ui.text.input.TextFieldValue
import com.ionspin.kotlin.bignum.decimal.BigDecimal

/**
 * One withholding line on an invoice or credit note. [rate] is a percentage
 * on the untaxed subtotal (100.00 = 100 %). VAT is computed on the untouched
 * base; the retention amount is subtracted from (base + VAT) at the end:
 * `Base 1000 → IVA 21 % = 210 → Retención 15 % = −150 → Total = 1060`.
 * [hidden] toggles per-invoice via the eye icon — kept in the list, skipped
 * by the renderer.
 */
data class RetentionState(
    var id: Int? = null,
    var label: TextFieldValue = TextFieldValue(""),
    var rate: BigDecimal = BigDecimal.ZERO,
    var sortOrder: Int = 0,
    var hidden: Boolean = false,
)
