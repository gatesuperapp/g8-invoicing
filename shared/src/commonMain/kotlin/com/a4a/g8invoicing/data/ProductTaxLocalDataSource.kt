package com.a4a.g8invoicing.data

import com.a4a.g8invoicing.Database
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
}
