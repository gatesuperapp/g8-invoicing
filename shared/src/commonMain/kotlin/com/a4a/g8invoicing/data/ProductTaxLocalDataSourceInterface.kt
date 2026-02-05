package com.a4a.g8invoicing.data

import com.ionspin.kotlin.bignum.decimal.BigDecimal

/**
 * Interface for ProductTaxLocalDataSourceImpl
 * Makes db implementation swappable
 *
 */


interface ProductTaxLocalDataSourceInterface {
    fun fetchProductTax(id: Long): BigDecimal?
    fun fetchProductTaxes(): List<BigDecimal>
    fun fetchProductTaxesWithIds(): List<Pair<Long, BigDecimal>>
    suspend fun saveProductTax(taxRate: BigDecimal)
    suspend fun updateProductTax(id: Long, amount: BigDecimal)
    suspend fun deleteProductTax(id: Long)
}
