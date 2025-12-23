package com.a4a.g8invoicing.data

import com.a4a.g8invoicing.Database
import g8invoicing.TaxRate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class ProductTaxLocalDataSource(
    db: Database,
) : ProductTaxLocalDataSourceInterface {
    private val productTaxQueries = db.taxRateQueries

    override fun fetchProductTax(id: Long): BigDecimal? {
        return productTaxQueries.getTaxRate(id).executeAsOneOrNull()?.toBigDecimal()
    }

    override fun fetchProductTaxes(): List<BigDecimal> {
        return productTaxQueries.getTaxRates().executeAsList().map { it.toBigDecimal() }
    }

    override suspend fun saveProductTax(taxRate: BigDecimal) {
        return withContext(Dispatchers.IO) {
            try {
                taxRate.let {
                    productTaxQueries.saveTaxRate(
                        product_tax_id = null,
                        amount = it.toDouble(),
                    )
                }
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun deleteProductTax(id: Long) {
        return withContext(Dispatchers.IO) {
            productTaxQueries.deleteTaxRate(id)
        }
    }
}

