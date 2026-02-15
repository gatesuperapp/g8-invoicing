package com.a4a.g8invoicing.data

import androidx.compose.ui.text.input.TextFieldValue
import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.data.util.DispatcherProvider
import com.a4a.g8invoicing.data.util.calculatePriceWithTax
import com.a4a.g8invoicing.ui.states.ClientRef
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.ProductPrice
import com.a4a.g8invoicing.ui.states.ProductState
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import g8invoicing.DocumentProduct
import g8invoicing.Product
import g8invoicing.ProductPriceQueries
import g8invoicing.TaxRateQueries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ProductLocalDataSource(
    db: Database,
) : ProductLocalDataSourceInterface {
    private val productQueries = db.productQueries
    private val taxQueries = db.taxRateQueries
    private val documentProductQueries = db.documentProductQueries
    private val productPriceQueries = db.productPriceQueries

    override suspend fun fetchProduct(id: Long): ProductState? {
        return withContext(DispatcherProvider.IO) {
            productQueries.getProduct(id).executeAsOneOrNull()
                ?.transformIntoEditableProduct(taxQueries, productPriceQueries)
        }
    }

    override fun fetchAllProducts(): Flow<List<ProductState>> {
        return productQueries.getAllProducts()
            .asFlow()
            .map { query ->
                query.executeAsList()
                    .map { it.transformIntoEditableProduct(taxQueries, productPriceQueries) }
            }
    }

    override suspend fun saveProduct(product: ProductState) {
        return withContext(DispatcherProvider.IO) {
            try {
                productQueries.transaction {
                    productQueries.saveProduct(
                        id = null,
                        name = product.name.text,
                        description = product.description?.text,
                        product_tax_id = product.taxRate?.let {
                            taxQueries.getTaxRateId(it.doubleValue(false)).executeAsOneOrNull()
                        },
                        unit = product.unit?.text
                    )

                    val lastInsertedProductId = productQueries.lastInsertRowId().executeAsOne()

                    product.defaultPriceWithoutTax?.let { price ->
                        productPriceQueries.saveProductPrice(
                            id = null,
                            product_id = lastInsertedProductId,
                            client_id = null,
                            price_without_tax = price.doubleValue(false)
                        )
                    }

                    product.additionalPrices?.forEach { price ->
                        price.clients.forEach { client ->
                            productPriceQueries.saveProductPrice(
                                id = null,
                                product_id = lastInsertedProductId,
                                client_id = client.id.toLong(),
                                price_without_tax = price.priceWithoutTax?.doubleValue(false)
                            )
                        }
                    }
                }
            } catch (cause: Throwable) {
                println("Error saving product: ${cause.message}")
            }
        }
    }

    override suspend fun duplicateProducts(
        products: List<ProductState>,
        duplicateNameSuffix: String,
    ) {
        return withContext(DispatcherProvider.IO) {
            try {
                products.forEach { productToDuplicate ->
                    val originalProduct =
                        productToDuplicate.id?.let { fetchProduct(it.toLong()) }

                    originalProduct?.let { product ->
                        productQueries.transaction {
                            productQueries.saveProduct(
                                id = null,
                                name = "${product.name.text}$duplicateNameSuffix",
                                description = product.description?.text,
                                product_tax_id = product.taxRate?.let {
                                    taxQueries.getTaxRateId(it.doubleValue(false)).executeAsOneOrNull()
                                },
                                unit = product.unit?.text
                            )

                            val newProductId = productQueries.lastInsertRowId().executeAsOne()

                            product.defaultPriceWithoutTax?.let { price ->
                                productPriceQueries.saveProductPrice(
                                    id = null,
                                    product_id = newProductId,
                                    client_id = null,
                                    price_without_tax = price.doubleValue(false)
                                )
                            }

                            product.additionalPrices?.forEach { price ->
                                price.clients.forEach { client ->
                                    productPriceQueries.saveProductPrice(
                                        id = null,
                                        product_id = newProductId,
                                        client_id = client.id.toLong(),
                                        price_without_tax = price.priceWithoutTax?.doubleValue(false)
                                    )
                                }
                            }
                        }
                    } ?: run {
                        println("Warning: Product with id ${productToDuplicate.id} not found for duplication.")
                    }
                }
            } catch (cause: Throwable) {
                println("Error duplicating products: ${cause.message}")
            }
        }
    }

    override suspend fun updateProduct(product: ProductState) {
        return withContext(DispatcherProvider.IO) {
            val productId = product.id?.toLong() ?: run {
                println("Error: Product ID is null, cannot update.")
                return@withContext
            }

            try {
                productQueries.transaction {
                    productQueries.updateProduct(
                        id = productId,
                        name = product.name.text,
                        description = product.description?.text,
                        product_tax_id = product.taxRate?.let {
                            taxQueries.getTaxRateId(it.doubleValue(false)).executeAsOneOrNull()
                        },
                        unit = product.unit?.text
                    )

                    productPriceQueries.deleteAllPricesForProduct(productId)

                    product.defaultPriceWithoutTax?.let { price ->
                        productPriceQueries.saveProductPrice(
                            id = null,
                            product_id = productId,
                            client_id = null,
                            price_without_tax = price.doubleValue(false)
                        )
                    }

                    product.additionalPrices?.forEach { price ->
                        price.clients.forEach { client ->
                            productPriceQueries.saveProductPrice(
                                id = null,
                                product_id = productId,
                                client_id = client.id.toLong(),
                                price_without_tax = price.priceWithoutTax?.doubleValue(false)
                            )
                        }
                    }
                }
            } catch (cause: Throwable) {
                println("Error updating product with id $productId: ${cause.message}")
            }
        }
    }


    override suspend fun updateDocumentProduct(documentProduct: DocumentProductState) {
        return withContext(DispatcherProvider.IO) {
            try {
                documentProduct.id?.toLong()?.let {
                    documentProductQueries.updateDocumentProduct(
                        document_product_id = it,
                        name = documentProduct.name.text,
                        quantity = documentProduct.quantity.doubleValue(false),
                        description = documentProduct.description?.text,
                        unit = documentProduct.unit?.text,
                        tax_rate = documentProduct.taxRate?.doubleValue(false),
                        price_without_tax = documentProduct.priceWithoutTax?.doubleValue(false)
                    )
                }
            } catch (cause: Throwable) {
                println("Error updating documentProduct $documentProduct: ${cause.message}")
            }
        }
    }

    override suspend fun deleteProduct(id: Long) {
        return withContext(DispatcherProvider.IO) {
            try {
                productQueries.transaction {
                    productPriceQueries.deleteAllPricesForProduct(id)
                    productQueries.deleteProduct(id)
                }
            } catch (cause: Throwable) {
                println("Error deleting product with id $id: ${cause.message}")
            }
        }
    }

    override suspend fun deleteAdditionalPrice(productId: Long, priceWithoutTax: BigDecimal) {
        return withContext(DispatcherProvider.IO) {
            try {
                productQueries.transaction {
                    productPriceQueries.deletePriceForClients(productId, priceWithoutTax.doubleValue(false))
                }
            } catch (cause: Throwable) {
                println("Error deleting price with value $priceWithoutTax : ${cause.message}")
            }
        }
    }

    override suspend fun removeClientFromAdditionalPrice(productId: Long, clientId: Long) {
        return withContext(DispatcherProvider.IO) {
            try {
                productQueries.transaction {
                    productPriceQueries.deletePriceForClient(productId, clientId)
                }
            } catch (cause: Throwable) {
                println("Error deleting price for client $clientId : ${cause.message}")
            }
        }
    }


    override suspend fun deleteDocumentProducts(ids: List<Long>) {
        return withContext(DispatcherProvider.IO) {
            ids.forEach {
                documentProductQueries.deleteDocumentProduct(it)
            }
        }
    }
}

fun Product.transformIntoEditableProduct(
    taxQueries: TaxRateQueries,
    productPriceQueries: ProductPriceQueries,
): ProductState {

    val taxRate = this.product_tax_id
        ?.let { taxQueries.getTaxRate(it).executeAsOneOrNull() }
        ?.let { BigDecimal.fromDouble(it) }

    val rows = productPriceQueries
        .getAdditionalPricesWithClients(this.id)
        .executeAsList()

    val defaultPriceRow = rows.firstOrNull { it.client_id == null }

    val defaultPriceWithoutTax = defaultPriceRow
        ?.price_without_tax
        ?.let { BigDecimal.fromDouble(it) }
        ?.roundToDigitPositionAfterDecimalPoint(2, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)

    val defaultPriceWithTax = defaultPriceWithoutTax?.let { price ->
        taxRate?.let { tax ->
            calculatePriceWithTax(price, tax)
        } ?: price
    }

    val additionalRows = rows.filter { it.client_id != null }

    val additionalPrices = if (additionalRows.isNotEmpty()) {
        val priceToClients = mutableMapOf<BigDecimal, MutableList<ClientRef>>()

        additionalRows.forEach { row ->
            val price = row.price_without_tax
                ?.let { BigDecimal.fromDouble(it) }
                ?.roundToDigitPositionAfterDecimalPoint(2, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
                ?: return@forEach

            val clientId = row.client_id ?: return@forEach
            val clientName = row.name ?: return@forEach

            val clientRef = ClientRef(
                id = clientId.toInt(),
                firstName = row.first_name?.takeIf { it.isNotBlank() },
                name = clientName
            )

            priceToClients
                .getOrPut(price) { mutableListOf() }
                .add(clientRef)
        }

        println("additionalPrices size = ${priceToClients.size}")

        priceToClients.map { (price, clients) ->
            val productPrice = ProductPrice(
                priceWithoutTax = price,
                priceWithTax = taxRate?.let {
                    calculatePriceWithTax(price, it)
                } ?: price,
                clients = clients
            )
            println(productPrice)
            productPrice
        }.ifEmpty { null }
    } else {
        null
    }

    return ProductState(
        id = this.id.toInt(),
        name = TextFieldValue(this.name),
        description = this.description?.let { TextFieldValue(it) },
        defaultPriceWithoutTax = defaultPriceWithoutTax,
        defaultPriceWithTax = defaultPriceWithTax,
        taxRate = taxRate,
        unit = this.unit?.let { TextFieldValue(it) } ?: TextFieldValue(""),
        additionalPrices = additionalPrices
    )
}



fun DocumentProduct.transformIntoEditableDocumentProduct(
    linkedDate: String? = null,
    linkedDocNumber: String? = null,
    sortOrder: Int?
): DocumentProductState {
    return DocumentProductState(
        id = this.id.toInt(),
        name = TextFieldValue(this.name),
        description = this.description?.let { TextFieldValue(it) },
        priceWithoutTax = this.price_without_tax?.let { BigDecimal.fromDouble(it) },
        priceWithTax = this.price_without_tax?.let { BigDecimal.fromDouble(it) }?.let { priceWithoutTax ->
            this.tax_rate?.let { BigDecimal.fromDouble(it) }?.let { tax ->
                calculatePriceWithTax(priceWithoutTax, tax)
            }
        },
        taxRate = this.tax_rate?.let { BigDecimal.fromDouble(it) },
        quantity = BigDecimal.fromDouble(this.quantity),
        unit = TextFieldValue(this.unit ?: ""),
        productId = this.product_id?.toInt(),
        linkedDate = linkedDate,
        linkedDocNumber = linkedDocNumber,
        sortOrder = sortOrder
    )
}
