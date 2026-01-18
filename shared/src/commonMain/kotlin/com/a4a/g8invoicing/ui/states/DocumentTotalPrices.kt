package com.a4a.g8invoicing.ui.states

import com.ionspin.kotlin.bignum.decimal.BigDecimal

data class DocumentTotalPrices(
    var totalPriceWithoutTax: BigDecimal? = null,
    var totalAmountsOfEachTax: MutableList<Pair<BigDecimal, BigDecimal>>? = null, //ex:  [(20.0, 7.2), (10.0, 2.4)]
    var totalPriceWithTax: BigDecimal? = null,
)
