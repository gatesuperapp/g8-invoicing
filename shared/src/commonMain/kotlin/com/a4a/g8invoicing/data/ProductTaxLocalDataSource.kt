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

    override suspend fun deleteProductTax(id: Long) {
        return withContext(DispatcherProvider.IO) {
            productTaxQueries.deleteTaxRate(id)
        }
    }
}
