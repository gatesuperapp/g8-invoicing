package com.a4a.g8invoicing.data

import androidx.compose.ui.text.input.TextFieldValue
import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DocumentClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentPrices
import com.a4a.g8invoicing.ui.states.DocumentProductState
import g8invoicing.ClientOrIssuer
import g8invoicing.DeliveryNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode

class DeliveryNoteLocalDataSource(
    db: Database,
) : DeliveryNoteLocalDataSourceInterface {
    private val deliveryNoteQueries = db.deliveryNoteQueries
    private val documentClientOrIssuerQueries = db.documentClientOrIssuerQueries

    // private val deliveryNoteDocumentClientOrIssuerQueries = db.link
    private val documentProductQueries = db.documentProductQueries
    private val deliveryNoteDocumentProductQueries = db.linkDeliveryNoteToDocumentProductQueries
    private val deliveryNoteDocumentClientOrIssuerQueries =
        db.linkDeliveryNoteToDocumentClientOrIssuerQueries


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun fetchDeliveryNoteFlow(id: Long): Flow<com.a4a.g8invoicing.ui.states.DeliveryNoteState?> {
        return deliveryNoteQueries.getDeliveryNote(id)
            .asFlow()
            .flatMapMerge { query ->
                val deliveryNote = query.executeAsOne()
                fetchDocumentProductsFlow(deliveryNote.delivery_note_id)
                    .map {
                        val l = it.filterNot { it.name == TextFieldValue("FAKE") }
                        // Adding a fake product is ugly but it's the only way i found for
                        // the flow to return something even when there's
                        // no product added to the document..
                        // I wanted to use flow.onStart { emit(initialValue) } but couldn't
                        // make it work as a Query is expected
                        deliveryNote.transformIntoEditableNote(l)
                    }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun fetchAllDeliveryNotes(): Flow<List<com.a4a.g8invoicing.ui.states.DeliveryNoteState>> {
        return deliveryNoteQueries.getAllDeliveryNotes()
            .asFlow()
            .map {
                it.executeAsList()
                    .map { deliveryNote ->
                        val products = fetchDocumentProducts(deliveryNote.delivery_note_id)
                            .filterNot { it.name == TextFieldValue("FAKE") }

                        deliveryNote.transformIntoEditableNote(
                            products
                        )
                    }
            }
    }

    private fun fetchDocumentProducts(deliveryNoteId: Long): List<DocumentProductState> {
        return deliveryNoteDocumentProductQueries.getProductsLinkedToDeliveryNote(deliveryNoteId)
            .executeAsList().map {
                documentProductQueries.getDocumentProduct(it.document_product_id)
                    .executeAsOne().transformIntoEditableDocumentProduct()
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun fetchDocumentProductsFlow(deliveryNoteId: Long): Flow<List<DocumentProductState>> {
        return deliveryNoteDocumentProductQueries.getProductsLinkedToDeliveryNote(deliveryNoteId)
            .asFlow()
            .flatMapMerge { query ->
                combine(
                    query.executeAsList().map {
                        documentProductQueries.getDocumentProduct(it.document_product_id)
                            .asFlow()
                            .map {
                                it.executeAsOne().transformIntoEditableDocumentProduct()
                            }
                    })
                {
                    it.asList()
                }
            }
    }


    // Used when duplicating a document (we don't need a flow)
    override fun fetchDeliveryNote(id: Long): com.a4a.g8invoicing.ui.states.DeliveryNoteState? {
        return deliveryNoteQueries.getDeliveryNote(id).executeAsOneOrNull()
            ?.let {
                it.transformIntoEditableNote(fetchDocumentProducts(it.delivery_note_id))
            }
    }

    private fun DeliveryNote.transformIntoEditableNote(documentProducts: List<DocumentProductState>): DeliveryNoteState {
        var client: DocumentClientOrIssuerState? = null
        var issuer: DocumentClientOrIssuerState? = null

        /*        this.document_client_id?.let {
                    client = documentClientOrIssuerQueries.get(it)
                        .executeAsOneOrNull()
                        ?.transformIntoEditable()
                }

                this.document_issuer_id?.let {
                    issuer = documentClientOrIssuerQueries.get(it)
                        .executeAsOneOrNull()
                        ?.transformIntoEditable()
                }*/

        this.let {
            // Adding every fields except documentProducts & prices
            return DeliveryNoteState(
                documentId = it.delivery_note_id.toInt(),
                documentNumber = TextFieldValue(text = it.number ?: ""),
                documentDate = it.delivery_date ?: "",
                orderNumber = TextFieldValue(text = it.order_number ?: ""),
                documentIssuer = issuer ?: DocumentClientOrIssuerState(),
                documentClient = client ?: DocumentClientOrIssuerState(),
                documentProducts = documentProducts,
                documentPrices = calculateDocumentPrices(documentProducts),
                currency = TextFieldValue(Strings.get(R.string.currency))
            )
        }
    }

    override suspend fun createNewDeliveryNote(): Long? {
        saveDeliveryNoteInfoInAllTables(
            DeliveryNoteState(
                documentIssuer = getExistingIssuer()?.transformIntoDocumentClientOrIssuer()
                    ?: DocumentClientOrIssuerState(),
            )
        )

        return deliveryNoteQueries.lastInsertRowId().executeAsOneOrNull()
    }

    override suspend fun updateDeliveryNote(deliveryNote: DeliveryNoteState) {
        return withContext(Dispatchers.IO) {
            try {
                deliveryNoteQueries.updateDeliveryNote(
                    delivery_note_id = deliveryNote.documentId?.toLong() ?: 0,
                    number = deliveryNote.documentNumber.text ?: "",
                    delivery_date = deliveryNote.documentDate.toString(),
                    /*                    document_client_id = deliveryNote.client.id?.toLong(),
                                        document_issuer_id = deliveryNote.issuer.id?.toLong()
                                            ?: saveNewIssuerAndGetId(deliveryNote.issuer),*/
                    order_number = deliveryNote.orderNumber.text,
                    currency = deliveryNote.currency.text
                )
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun duplicateDeliveryNotes(deliveryNotes: List<DeliveryNoteState>) {
        withContext(Dispatchers.IO) {
            try {
                deliveryNotes.forEach {
                    saveDeliveryNoteInfoInAllTables(
                        DeliveryNoteState(
                            documentType = it.documentType,
                            documentId = it.documentId,
                            documentNumber = TextFieldValue("XXX"),
                            documentDate = it.documentDate,
                            orderNumber = it.orderNumber,
                            documentIssuer = it.documentIssuer,
                            documentClient = it.documentClient,
                            documentProducts = it.documentProducts,
                            documentPrices = it.documentPrices,
                            currency = it.currency
                        )
                    )
                }
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun saveDocumentProductInDbAndLinkToDocument(
        documentProduct: DocumentProductState,
        deliveryNoteId: Long?,
    ) {
        withContext(Dispatchers.IO) {
            try {
                documentProductQueries.saveDocumentProduct(
                    document_product_id = null,
                    name = documentProduct.name.text,
                    quantity = documentProduct.quantity.toDouble(),
                    description = documentProduct.description?.text,
                    final_price = documentProduct.priceWithTax?.toDouble(),
                    tax_rate = documentProduct.taxRate?.toDouble(),
                    unit = documentProduct.unit?.text,
                    product_id = documentProduct.productId?.toLong()
                )

                deliveryNoteId?.let { deliveryNoteId ->
                    documentProductQueries.lastInsertRowId().executeAsOneOrNull()?.toInt()
                        ?.let { id ->
                            addDocumentProduct(
                                deliveryNoteId,
                                id.toLong()
                            )
                        }
                }
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun saveDocumentClientOrIssuerInDbAndLinkToDocument(
        documentClientOrIssuer: DocumentClientOrIssuerState,
        deliveryNoteId: Long?,
    ) {
        withContext(Dispatchers.IO) {
            try {
                documentClientOrIssuerQueries.save(
                    document_client_or_issuer_id = null,
                    type = documentClientOrIssuer.type.name.lowercase(),
                    first_name = documentClientOrIssuer.firstName?.text,
                    name = documentClientOrIssuer.name.text,
                    address1 = documentClientOrIssuer.address1?.text,
                    address2 = documentClientOrIssuer.address2?.text,
                    zip_code = documentClientOrIssuer.zipCode?.text,
                    city = documentClientOrIssuer.city?.text,
                    phone = documentClientOrIssuer.phone?.text,
                    email = documentClientOrIssuer.email?.text,
                    notes = documentClientOrIssuer.notes?.text,
                    company_id1_label = documentClientOrIssuer.companyId1Label?.text,
                    company_id1_number = documentClientOrIssuer.companyId1Number?.text,
                    company_id2_label = documentClientOrIssuer.companyId2Label?.text,
                    company_id2_number = documentClientOrIssuer.companyId2Number?.text
                )

                deliveryNoteId?.let { deliveryNoteId ->
                    documentClientOrIssuerQueries.lastInsertedRowId().executeAsOneOrNull()?.toInt()
                        ?.let { id ->
                            addDocumentClientOrIssuer(
                                deliveryNoteId,
                                id.toLong()
                            )
                        }
                }
            } catch (cause: Throwable) {
            }
        }
    }


    override suspend fun deleteDeliveryNotes(deliveryNotes: List<DeliveryNoteState>) {
        withContext(Dispatchers.IO) {
            try {
                deliveryNotes.filter { it.documentId != null }.forEach { deliveryNote ->
                    deliveryNoteQueries.deleteDeliveryNote(id = deliveryNote.documentId!!.toLong())
                    deliveryNoteDocumentProductQueries.deleteAllProductsLinkedToADeliveryNote(
                        deliveryNote.documentId!!.toLong()
                    )
                    deliveryNote.documentProducts.filter { it.id != null }
                        .forEach { documentProduct ->
                            deleteDeliveryNoteProduct(
                                deliveryNote.documentId!!.toLong(),
                                documentProduct.id!!.toLong()
                            )
                        }
                }
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun deleteDeliveryNoteProduct(deliveryNoteId: Long, documentProductId: Long) {
        return withContext(Dispatchers.IO) {
            deliveryNoteDocumentProductQueries.deleteProductLinkedToDeliveryNote(
                deliveryNoteId,
                documentProductId
            )
        }
    }

    override suspend fun deleteDocumentClientOrIssuer(
        deliveryNoteId: Long,
        documentClientOrIssuerId: Long,
    ) {
        return withContext(Dispatchers.IO) {
            deliveryNoteDocumentClientOrIssuerQueries.deleteDocumentClientOrIssuerLinkedToDeliveryNote(
                deliveryNoteId,
                documentClientOrIssuerId
            )
        }
    }

    override fun addDocumentProduct(deliveryNoteId: Long, documentProductId: Long) {
        deliveryNoteDocumentProductQueries.saveProductLinkedToDeliveryNote(
            id = null,
            delivery_note_id = deliveryNoteId,
            document_product_id = documentProductId
        )
    }

    private fun addDocumentClientOrIssuer(deliveryNoteId: Long, documentClientOrIssuerId: Long) {
        deliveryNoteDocumentClientOrIssuerQueries.saveDocumentClientOrIssuerLinkedToDeliveryNote(
            id = null,
            delivery_note_id = deliveryNoteId,
            document_client_or_issuer_id = documentClientOrIssuerId
        )
    }

    private fun getExistingIssuer(): ClientOrIssuer? {
        var issuer: ClientOrIssuer? = null
        try {
            issuer = documentClientOrIssuerQueries.getLastInsertedIssuer().executeAsOneOrNull()
        } catch (e: Exception) {
            println("Fetching result failed with exception: ${e.localizedMessage}")
        }
        return issuer
    }

    private suspend fun saveDeliveryNoteInfoInAllTables(deliveryNote: DeliveryNoteState) {
        deliveryNoteQueries.saveDeliveryNote(
            delivery_note_id = null,
            number = deliveryNote.documentNumber.text,
            delivery_date = deliveryNote.documentDate,
            order_number = deliveryNote.orderNumber.text,
            currency = deliveryNote.currency.text
        )

        deliveryNoteQueries.lastInsertRowId().executeAsOneOrNull()?.let { id ->
            // Link to fake product
            addDocumentProduct(
                id,
                1
            )
            // Link all products
            deliveryNote.documentProducts.forEach { documentProduct ->
                saveDocumentProductInDbAndLinkToDocument(
                    documentProduct = documentProduct,
                    deliveryNoteId = id
                )
            }

            // Link client
            saveDocumentClientOrIssuerInDbAndLinkToDocument(
                documentClientOrIssuer = deliveryNote.documentClient,
                deliveryNoteId = id
            )

            // Link issuer
            saveDocumentClientOrIssuerInDbAndLinkToDocument(
                documentClientOrIssuer = deliveryNote.documentIssuer,
                deliveryNoteId = id
            )
        }
    }
}


fun calculateDocumentPrices(products: List<DocumentProductState>): DocumentPrices {
    val totalPriceWithoutTax = products.filter { it.priceWithTax != null }.sumOf {
        (it.priceWithTax!! - it.priceWithTax!! * (it.taxRate
            ?: BigDecimal(0)) / BigDecimal(100)) * (it.quantity)
    }.setScale(2, RoundingMode.HALF_UP)

// Calculate the total amount of each tax
    val groupedItems = products.groupBy {
        it.taxRate
    }
    val taxes = groupedItems.keys.filterNotNull().distinct().toMutableList() // ex: taxes= [10, 20]

    val amounts: MutableList<BigDecimal> = mutableListOf()  // ex: amounts = [2.4, 9.0]
    groupedItems.values.forEach { documentProduct ->
        val listOfAmounts = documentProduct.filter { it.priceWithTax != null }.map {
            val priceWithoutTax =
                it.priceWithTax!! - it.priceWithTax!! * (it.taxRate ?: BigDecimal(0)) / BigDecimal(
                    100
                )

            priceWithoutTax * it.quantity * (it.taxRate ?: BigDecimal(0)) / BigDecimal(100)
        }
        val sumOfAmounts = listOfAmounts.sumOf { it }.setScale(2, RoundingMode.HALF_UP)
        amounts.add(
            sumOfAmounts
        )
    }
    val amountsPerTaxRate: MutableList<Pair<BigDecimal, BigDecimal>> = mutableListOf()
    taxes.forEachIndexed { index, key ->
        amountsPerTaxRate.add(Pair(key, amounts[index]))
    } // ex: amountsPerTaxRate = [(20.0, 7.2), (10.0, 2.4)]

    return DocumentPrices(
        totalPriceWithoutTax = totalPriceWithoutTax,
        totalAmountsOfEachTax = amountsPerTaxRate,
        totalPriceWithTax = totalPriceWithoutTax + amounts.sumOf { it }
    )
}