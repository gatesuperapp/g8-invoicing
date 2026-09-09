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
    /**
     * Replaces the global default rates (5.5/10/20 from TaxRate.sq) with a
     * country-specific shortlist. No-op if the table has been customised
     * (any row that isn't one of the three global defaults) — the user
     * created their own rates and we don't want to wipe them.
     * Called once, from FirstLaunchIssuerNameDialog's submit callback,
     * before any product has been created.
     */
    suspend fun seedDefaultsForCountryIfPristine(countryCode: String)
}
