package com.a4a.g8invoicing.data

import androidx.compose.ui.text.input.TextFieldValue
import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DocumentPrices
import com.a4a.g8invoicing.ui.states.DocumentProductState
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
    private val clientOrIssuerQueries = db.clientOrIssuerQueries
    private val deliveryNoteProductQueries =
        db.deliveryNoteProductQueries
    private val documentProductQueries = db.documentProductQueries

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun fetchDeliveryNoteFlow(id: Long): Flow<DeliveryNoteState?> {
        return deliveryNoteQueries.getDeliveryNote(id)
            .asFlow()
            .flatMapMerge { query ->
                val deliveryNote = query.executeAsOne()
                fetchDocumentProductsFlow(deliveryNote.delivery_note_id)
                    .map {
                        val l = it.filterNot { it.name == TextFieldValue("FAKE") }
                        // Ugly but only way i've found for the flow to return something
                        // even when there's no product added to the document..
                        // I wanted to use flow.onStart { emit(initialValue) } but couldn't
                        // find a solution as a Query is expected
                        deliveryNote.transformIntoEditableNote(l)
                    }
            }
    }


    /*
        override fun fetchAllDeliveryNotes(): Flow<List<DeliveryNoteState>> {
            return deliveryNoteQueries.getAllDeliveryNotes()
                .asFlow()
                .map { query ->
                    query.executeAsList()
                        .map { it.transformIntoEditableNote(listOf(DocumentProductState())) }
                }
        }
    */

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun fetchAllDeliveryNotes(): Flow<List<DeliveryNoteState>> {
        return deliveryNoteQueries.getAllDeliveryNotes()
            .asFlow()
            .flatMapMerge { query ->
                combine(
                    query.executeAsList().map { deliveryNote ->
                        fetchDocumentProductsFlow(deliveryNote.delivery_note_id)
                            .map {
                                val l = it.filterNot { it.name == TextFieldValue("FAKE") }
                                // Ugly but only way i've found for the flow to return something
                                // even when there's no product added to the document..
                                // I wanted to use flow.onStart { emit(initialValue) } but couldn't
                                // find a solution as a Query is expected
                                deliveryNote.transformIntoEditableNote(l)
                            }
                    }
                ) {
                    it.asList()
                }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun fetchDocumentProductsFlow(deliveryNoteId: Long): Flow<List<DocumentProductState>> {
        return deliveryNoteProductQueries.getProductsLinkedToDeliveryNote(deliveryNoteId)
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
    override fun fetchDeliveryNote(id: Long): DeliveryNoteState? {
        return deliveryNoteQueries.getDeliveryNote(id).executeAsOneOrNull()
            ?.let {
                it.transformIntoEditableNote(fetchDocumentProducts(it.delivery_note_id))
            }
    }

    private fun DeliveryNote.transformIntoEditableNote(documentProducts: List<DocumentProductState>): DeliveryNoteState {
        var client: ClientOrIssuerState? = null
        var issuer: ClientOrIssuerState? = null

        this.document_client_id?.let {
            client = clientOrIssuerQueries.get(it)
                .executeAsOneOrNull()
                ?.transformIntoEditable()
        }

        this.document_issuer_id?.let {
            issuer = clientOrIssuerQueries.get(it)
                .executeAsOneOrNull()
                ?.transformIntoEditable()
        }

        this.let {
            // Adding every fields except documentProducts & prices
            return DeliveryNoteState(
                deliveryNoteId = it.delivery_note_id.toInt(),
                number = TextFieldValue(text = it.number ?: ""),
                deliveryDate = it.delivery_date,
                orderNumber = TextFieldValue(text = it.order_number ?: ""),
                issuer = issuer,
                client = client,
                documentProducts = documentProducts,
                documentPrices = calculateDocumentPrices(documentProducts)
            )
        }
    }

    // Used when duplicating a document (we don't need a flow)
    private fun fetchDocumentProducts(deliveryNoteId: Long): List<DocumentProductState> {
        val documentProducts: MutableList<DocumentProductState> = mutableListOf()

        val identifiers =
            deliveryNoteProductQueries.getProductsLinkedToDeliveryNote(deliveryNoteId)
                .executeAsList()
        identifiers.forEach {
            documentProductQueries.getDocumentProduct(it.document_product_id).executeAsOneOrNull()
                ?.let { documentProduct ->
                    documentProducts += documentProduct.transformIntoEditableDocumentProduct()
                }
        }
        return documentProducts
    }

    override fun saveDeliveryNote(): Long? {
        deliveryNoteQueries.saveDeliveryNote(
            delivery_note_id = null,
            number = null,
            delivery_date = "",
            order_number = "",
            document_issuer_id = getExistingIssuerId(),
            document_client_id = null,
            currency = null
        )
        return deliveryNoteQueries.getLastInsertedRowId().executeAsOneOrNull()
    }

    override suspend fun updateDeliveryNote(deliveryNote: DeliveryNoteState) {
        return withContext(Dispatchers.IO) {
            try {
                deliveryNoteQueries.updateDeliveryNote(
                    delivery_note_id = deliveryNote.deliveryNoteId?.toLong() ?: 0,
                    number = deliveryNote.number?.text ?: "",
                    delivery_date = deliveryNote.deliveryDate.toString(),
                    document_client_id = deliveryNote.client?.id?.toLong(),
                    document_issuer_id = deliveryNote.issuer?.id?.toLong()
                        ?: deliveryNote.issuer?.let { saveNewIssuerAndGetId(it) },
                    order_number = deliveryNote.orderNumber?.text,
                    currency = deliveryNote.currency?.text
                )
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun duplicateDeliveryNote(deliveryNote: DeliveryNoteState): Long? {
        var deliveryNoteId: Long? = null
        withContext(Dispatchers.IO) {
            try {
                deliveryNoteQueries.saveDeliveryNote(
                    delivery_note_id = null,
                    number = deliveryNote.number?.text ?: " - ",
                    delivery_date = deliveryNote.deliveryDate,
                    order_number = deliveryNote.orderNumber?.text,
                    document_issuer_id = deliveryNote.issuer?.id?.toLong(),
                    document_client_id = deliveryNote.client?.id?.toLong(),
                    currency = deliveryNote.currency?.text
                )
                deliveryNoteId = documentProductQueries.lastInsertRowId().executeAsOneOrNull()

            } catch (cause: Throwable) {
            }
        }
        return deliveryNoteId
    }

    override suspend fun deleteDeliveryNote(id: Long) {
        return withContext(Dispatchers.IO) {
            deliveryNoteQueries.deleteDeliveryNote(id)
        }
    }

    override suspend fun deleteDeliveryNoteProduct(id: Long, documentProductId: Long) {
        return withContext(Dispatchers.IO) {
            deliveryNoteProductQueries.deleteProductLinkedToDeliveryNote(id, documentProductId)
        }
    }

    override suspend fun addDeliveryNoteProduct(deliveryNoteId: Long, documentProductId: Long) {
        return withContext(Dispatchers.IO) {
            deliveryNoteProductQueries.saveProductLinkedToDeliveryNote(
                id = null,
                delivery_note_id = deliveryNoteId,
                document_product_id = documentProductId
            )
        }
    }

    private fun saveNewIssuerAndGetId(issuer: ClientOrIssuerState): Long {
        clientOrIssuerQueries.save(
            client_or_issuer_id = null,
            type = "issuer",
            first_name = issuer.firstName?.text,
            name = issuer.name.text,
            address1 = issuer.address1?.text,
            address2 = issuer.address2?.text,
            zip_code = issuer.zipCode?.text,
            city = issuer.city?.text,
            phone = issuer.phone?.text,
            email = issuer.email?.text,
            notes = issuer.notes?.text,
            company_id1_label = issuer.companyId1Label?.text,
            company_id1_number = issuer.companyId1Number?.text,
            company_id2_label = issuer.companyId2Label?.text,
            company_id2_number = issuer.companyId2Number?.text,
        )

        return clientOrIssuerQueries.getLastInsertedRowId().executeAsOne()
    }

    private fun getExistingIssuerId(): Long? {
        return clientOrIssuerQueries.getLastInsertedIssuer().executeAsOneOrNull()
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