package com.a4a.g8invoicing.data

import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.data.models.CountryCodes
import com.a4a.g8invoicing.data.util.DispatcherProvider
import kotlinx.coroutines.withContext
import com.ionspin.kotlin.bignum.decimal.BigDecimal

class ProductTaxLocalDataSource(
    db: Database,
) : ProductTaxLocalDataSourceInterface {
    private val productTaxQueries = db.taxRateQueries

    override fun fetchProductTax(id: Long): BigDecimal? {
        return productTaxQueries.getTaxRate(id).executeAsOneOrNull()?.let { BigDecimal.fromDouble(it) }
    }

    override fun fetchProductTaxes(): List<BigDecimal> {
        return productTaxQueries.getTaxRates().executeAsList().map { BigDecimal.fromDouble(it) }
    }

    override fun fetchProductTaxesWithIds(): List<Pair<Long, BigDecimal>> {
        return productTaxQueries.getTaxRatesWithIds().executeAsList().map {
            Pair(it.product_tax_id, BigDecimal.fromDouble(it.amount))
        }
    }

    override suspend fun saveProductTax(taxRate: BigDecimal) {
        return withContext(DispatcherProvider.IO) {
            try {
                taxRate.let {
                    productTaxQueries.saveTaxRate(
                        product_tax_id = null,
                        amount = it.doubleValue(false),
                    )
                }
            } catch (cause: Throwable) {
                // Log error if needed
            }
        }
    }

    override suspend fun updateProductTax(id: Long, amount: BigDecimal) {
        return withContext(DispatcherProvider.IO) {
            try {
                productTaxQueries.updateTaxRate(
                    id = id,
                    amount = amount.doubleValue(false)
                )
            } catch (cause: Throwable) {
                // Log error if needed
            }
        }
    }

    override suspend fun deleteProductTax(id: Long) {
        return withContext(DispatcherProvider.IO) {
            productTaxQueries.deleteTaxRate(id)
        }
    }

    override suspend fun seedDefaultsForCountryIfPristine(countryCode: String) {
        val countryRates = CountryCodes.defaultVatRatesForCountry(countryCode) ?: return
        withContext(DispatcherProvider.IO) {
            val current = productTaxQueries.getTaxRates().executeAsList().sorted()
            // Global defaults hardcoded in TaxRate.sq — only wipe & re-seed if
            // no row has ever been touched. Any deviation (rate added, existing
            // rate edited, one deleted) means the user has taken over the table
            // and we back off silently.
            if (current != GLOBAL_DEFAULT_RATES) return@withContext
            productTaxQueries.transaction {
                GLOBAL_DEFAULT_RATES.indices.forEach { idx ->
                    productTaxQueries.deleteTaxRate((idx + 1).toLong())
                }
                countryRates.forEachIndexed { idx, rate ->
                    productTaxQueries.saveTaxRate(
                        product_tax_id = (idx + 1).toLong(),
                        amount = rate,
                    )
                }
            }
        }
    }

    private companion object {
        val GLOBAL_DEFAULT_RATES = listOf(5.5, 10.0, 20.0)
    }
}
