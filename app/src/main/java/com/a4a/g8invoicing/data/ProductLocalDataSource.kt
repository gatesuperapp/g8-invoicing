package com.a4a.g8invoicing.data

import androidx.compose.ui.text.input.TextFieldValue
import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.ProductState
import g8invoicing.DocumentProduct
import g8invoicing.Product
import g8invoicing.TaxRateQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode

// Product is a simple product: name, description, price
// DocumentProduct is a product added to a document: quantity, name, description, price, currency

class ProductLocalDataSource(
    db: Database,
) : ProductLocalDataSourceInterface {
    private val productQueries = db.productQueries
    private val productPriceQueries = db.productAdditionalPriceQueries
    private val taxQueries = db.taxRateQueries
    private val documentProductQueries = db.documentProductQueries

    override fun fetchProduct(id: Long): ProductState? {
        return productQueries.getProduct(id).executeAsOneOrNull()
            ?.transformIntoEditableProduct(taxQueries)
    }

    override fun fetchDocumentProduct(id: Long): DocumentProductState? {
        return documentProductQueries.getDocumentProduct(id).executeAsOneOrNull()
            ?.transformIntoEditableDocumentProduct()
    }

    override fun fetchAllProducts(): Flow<List<ProductState>> {
        return productQueries.getAllProducts()
            .asFlow()
            .map { query ->
                query.executeAsList()
                    .map { it.transformIntoEditableProduct(taxQueries) }
            }
    }

    override suspend fun saveProduct(product: ProductState) {
        return withContext(Dispatchers.IO) {
            try {
                product.priceWithoutTax?.let {
                    productPriceQueries.saveProductPrice(
                        product_price_id = null,
                        amount = it.toDouble(),
                    )
                }
                productQueries.saveProduct(
                    product_id = null,
                    name = product.name.text,
                    description = product.description?.text,
                    final_price = product.finalPrice?.toDouble(),
                    product_additional_price_id = null, //TODO get the price id from db
                    product_tax_id = product.taxRate?.let {
                        taxQueries.getTaxRateId(it.toDouble()).executeAsOneOrNull()
                    },
                    price_without_tax = product.priceWithoutTax?.toDouble(),
                    unit = product.unit?.text
                )
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun saveDocumentProduct(documentProduct: DocumentProductState): Int? {
        var documentProductId: Int? = null
        withContext(Dispatchers.IO) {
            try {
                documentProductQueries.saveDocumentProduct(
                    document_product_id = null,
                    name = documentProduct.name.text,
                    quantity = documentProduct.quantity.toDouble(),
                    description = documentProduct.description?.text,
                    final_price = documentProduct.priceWithTax.toDouble(),
                    tax_rate = documentProduct.taxRate.toLong(),
                    unit = documentProduct.unit?.text,
                    product_id = documentProduct.productId?.toLong()
                )
                documentProductId = documentProductQueries.lastInsertRowId().executeAsOneOrNull()?.toInt()

            } catch (cause: Throwable) {
            }
        }
        return documentProductId
    }

    override suspend fun duplicateProduct(product: ProductState) {
        return withContext(Dispatchers.IO) {
            try {
                productQueries.saveProduct(
                    product_id = null,
                    name = product.name.text,
                    description = product.description?.text,
                    final_price = product.finalPrice?.toDouble(),
                    product_tax_id = product.taxRate?.let {
                        taxQueries.getTaxRateId(it.toDouble()).executeAsOneOrNull()
                    },
                    product_additional_price_id = 1, // TODO change when adding several prices / product
                    price_without_tax = product.priceWithoutTax?.toDouble(),
                    unit = product.unit?.text
                )
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun updateProduct(product: ProductState) {
        return withContext(Dispatchers.IO) {
            try {
                productQueries.updateProduct(
                    product_id = product.productId?.toLong() ?: 0,
                    name = product.name.text,
                    description = product.description?.text ?: "",
                    final_price = product.finalPrice?.toDouble(),
                    product_tax_id = product.taxRate?.let {
                        taxQueries.getTaxRateId(it.toDouble()).executeAsOneOrNull()
                    },
                    price_without_tax = product.priceWithoutTax?.toDouble(),
                    unit = product.unit?.text
                )
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun updateDocumentProduct(documentProduct: DocumentProductState) {
        return withContext(Dispatchers.IO) {
            try {
                documentProductQueries.updateDocumentProduct(
                    document_product_id = documentProduct.id?.toLong() ?: 0,
                    name = documentProduct.name.text,
                    quantity = documentProduct.quantity.toDouble(),
                    description = documentProduct.description?.text,
                    price_with_tax = documentProduct.priceWithTax.toDouble(),
                    tax_rate = documentProduct.taxRate.toLong(),
                    unit = documentProduct.unit?.text,
                    product_id = documentProduct.productId?.toLong(),
                )
            } catch (cause: Throwable) {
            }
        }
    }

    override fun checkIfEmpty(): Int {
        return productQueries.checkIfEmpty().executeAsOne().toInt()
    }

    override suspend fun deleteProduct(id: Long) {
        return withContext(Dispatchers.IO) {
            productQueries.deleteProduct(id)
        }
    }

    override suspend fun deleteDocumentProduct(id: Long) {
        return withContext(Dispatchers.IO) {
            documentProductQueries.deleteDocumentProduct(id)
        }
    }
}

fun Product.transformIntoEditableProduct(
    taxQueries: TaxRateQueries,
): ProductState {
    return ProductState(
        productId = this.product_id.toInt(),
        name = TextFieldValue(this.name),
        description = TextFieldValue(this.description ?: ""),
        finalPrice = this.final_price?.toBigDecimal()?.setScale(2, RoundingMode.HALF_UP),
        priceWithoutTax = this.price_without_tax?.toBigDecimal()?.setScale(2, RoundingMode.HALF_UP),
        taxRate = this.product_tax_id?.let {
            taxQueries.getTaxRate(it)
        }?.executeAsOneOrNull()?.toBigDecimal(),
        unit = TextFieldValue(this.unit ?: "")
    )
}

fun DocumentProduct.transformIntoEditableDocumentProduct(): DocumentProductState {
    return DocumentProductState(
        id = this.document_product_id.toInt(),
        name = TextFieldValue(this.name),
        description = TextFieldValue(this.description ?: ""),
        priceWithTax = this.final_price?.toBigDecimal()?.setScale(2, RoundingMode.HALF_UP)
            ?: BigDecimal(0),
        taxRate = this.tax_rate?.toBigDecimal()?.setScale(0, RoundingMode.HALF_UP)
            ?: BigDecimal(0),
        quantity = this.quantity.toBigDecimal().setScale(2, RoundingMode.HALF_UP)
            .stripTrailingZeros(),
        unit = TextFieldValue(this.unit ?: ""),
        productId = this.product_id?.toInt()
    )
}

