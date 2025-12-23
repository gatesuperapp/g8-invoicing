package com.a4a.g8invoicing.data

import java.math.BigDecimal

/**
 * Interface for ProductLocalDataSourceImpl
 * Makes db implementation swappable
 *
 */


interface ProductTaxLocalDataSourceInterface {
    fun fetchProductTax(id: Long): BigDecimal?
    fun fetchProductTaxes():  List<BigDecimal>
    suspend fun saveProductTax(taxRate: BigDecimal)
    suspend fun deleteProductTax(id: Long)
}
