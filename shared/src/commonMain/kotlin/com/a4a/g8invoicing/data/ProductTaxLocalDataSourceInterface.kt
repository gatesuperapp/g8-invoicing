package com.a4a.g8invoicing.data

import com.ionspin.kotlin.bignum.decimal.BigDecimal

/**
 * Interface for ProductTaxLocalDataSourceImpl
 * Makes db implementation swappable
 *
 */


interface ProductTaxLocalDataSourceInterface {
    fun fetchProductTax(id: Long): BigDecimal?
    fun fetchProductTaxes():  List<BigDecimal>
    suspend fun saveProductTax(taxRate: BigDecimal)
    suspend fun deleteProductTax(id: Long)
}
