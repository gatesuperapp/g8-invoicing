package com.a4a.g8invoicing.ui.states

import com.ionspin.kotlin.bignum.decimal.BigDecimal

data class DocumentTotalPrices(
    var totalPriceWithoutTax: BigDecimal? = null,
    var totalAmountsOfEachTax: MutableList<Pair<BigDecimal, BigDecimal>>? = null, //ex:  [(20.0, 7.2), (10.0, 2.4)]
    var totalPriceWithTax: BigDecimal? = null,
    // Retentions computed once at price-time so the totals block + PDF share
    // the exact same numbers. Each row keeps the label + rate (needed to
    // render "Retención 15 % : − X €") + the amount subtracted from the
    // pre-retention total-with-tax. Only lines with hidden=false land here.
    var retentionAmounts: List<RetentionLine> = emptyList(),
    // Sum of retentionAmounts, cached to avoid recomputing at every render.
    var totalRetentions: BigDecimal = BigDecimal.ZERO,
)

data class RetentionLine(
    val label: String,
    val rate: BigDecimal,
    val amount: BigDecimal,
)
