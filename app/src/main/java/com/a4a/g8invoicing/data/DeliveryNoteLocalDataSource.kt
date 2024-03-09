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
import kotlinx.coroutines.flow.flatMapLatest
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
    private val linkToCompanyQueries = db.linkClientOrIssuerToCompanyIdentificatorQueries
    private val companyIdQueries = db.companyIdentificatorQueries
    private val linkDeliveryNoteToDocumentProductQueries = db.linkDeliveryNoteToDocumentProductQueries
    private val documentProductQueries = db.documentProductQueries

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun fetchDeliveryNoteFlow(id: Long): Flow<DeliveryNoteState?> {
        return deliveryNoteQueries.getDeliveryNote(id)
            .asFlow()
            .flatMapMerge { query ->
                val deliveryNote = query.executeAsOne()
                fetchDocumentProductsFlow(deliveryNote.delivery_note_id)
                    .map {
                        deliveryNote.transformIntoEditableNote(it)
                    }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun fetchAllDeliveryNotes(): Flow<List<DeliveryNoteState>> {
        return deliveryNoteQueries.getAllDeliveryNotes()
            .asFlow()
            .flatMapMerge { query ->
                combine(
                    query.executeAsList().map { deliveryNote ->
                        fetchDocumentProductsFlow(deliveryNote.delivery_note_id)
                            .map {
                                deliveryNote.transformIntoEditableNote(it)
                            }
                    }
                ) {
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
        // It's a suspend function so we can observe changes on DocumentProducts
        var client: ClientOrIssuerState? = null
        var issuer: ClientOrIssuerState? = null

        this.document_client_id?.let {
            client = clientOrIssuerQueries.get(it)
                .executeAsOneOrNull()
                ?.transformIntoEditable(linkToCompanyQueries, companyIdQueries)
        }

        this.document_issuer_id?.let {
            issuer = clientOrIssuerQueries.get(it)
                .executeAsOneOrNull()
                ?.transformIntoEditable(linkToCompanyQueries, companyIdQueries)
        }

        this.let {
            // Adding every fields except documentProducts & prices
            return DeliveryNoteState(
                deliveryNoteId = it.delivery_note_id.toInt(),
                number = TextFieldValue(text = it.number),
                deliveryDate = it.delivery_date,
                orderNumber = TextFieldValue(text = it.order_number ?: ""),
                issuer = issuer,
                client = client,
                documentProducts = documentProducts,
                documentPrices = calculateDocumentPrices(documentProducts)
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun fetchDocumentProductsFlow(deliveryNoteId: Long): Flow<List<DocumentProductState>> {
        return linkDeliveryNoteToDocumentProductQueries.getDeliveryNoteProduct(deliveryNoteId)
            .asFlow()
            .flatMapLatest { query ->
                combine(
                    query.executeAsList().map {
                        documentProductQueries.getDocumentProduct(it.document_product_id)
                            .asFlow()
                            .map {
                                it.executeAsOne().transformIntoEditableDocumentProduct()
                            }
                    }) {
                    it.asList()
                }
            }
    }

    // Used when duplicating a document (we don't need a flow)
    private fun fetchDocumentProducts(deliveryNoteId: Long): List<DocumentProductState> {
        val documentProducts: MutableList<DocumentProductState> = mutableListOf()

        val identifiers =
            linkDeliveryNoteToDocumentProductQueries.getDeliveryNoteProduct(deliveryNoteId).executeAsList()
        identifiers.forEach {
            documentProductQueries.getDocumentProduct(it.document_product_id).executeAsOneOrNull()
                ?.let { documentProduct ->
                    documentProducts += documentProduct.transformIntoEditableDocumentProduct()
                }
        }
        return documentProducts
    }

    override suspend fun saveDeliveryNote(deliveryNote: DeliveryNoteState) {
        return withContext(Dispatchers.IO) {
            try {
                getExistingIssuerOrSaveNewIssuer(deliveryNote)

                deliveryNoteQueries.saveDeliveryNote(
                    delivery_note_id = null,
                    number = deliveryNote.number?.text ?: "",
                    delivery_date = deliveryNote.deliveryDate.toString(),
                    order_number = deliveryNote.orderNumber?.text,
                    document_issuer_id = deliveryNote.issuer?.id?.toLong()
                        ?: deliveryNote.issuer?.email?.let {
                            clientOrIssuerQueries.getIdByMail(it.text)
                                .executeAsOneOrNull()
                        },
                    document_client_id = deliveryNote.client?.id?.toLong(),
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

    override suspend fun updateDeliveryNote(deliveryNote: DeliveryNoteState) {
        return withContext(Dispatchers.IO) {
            try {
                getExistingIssuerOrSaveNewIssuer(deliveryNote)

                deliveryNoteQueries.updateDeliveryNote(
                    delivery_note_id = deliveryNote.deliveryNoteId?.toLong() ?: 0,
                    number = deliveryNote.number?.text ?: "",
                    delivery_date = deliveryNote.deliveryDate.toString(),
                    document_client_id = deliveryNote.client?.id?.toLong(),
                    document_issuer_id = deliveryNote.issuer?.id?.toLong()
                        ?: deliveryNote.issuer?.email?.let {
                            clientOrIssuerQueries.getIdByMail(it.text)
                                .executeAsOneOrNull()
                        },
                    order_number = deliveryNote.orderNumber?.text,
                    currency = deliveryNote.currency?.text
                )
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun deleteDeliveryNote(id: Long) {
        return withContext(Dispatchers.IO) {
            deliveryNoteQueries.deleteDeliveryNote(id)
        }
    }

    override suspend fun deleteDeliveryNoteProduct(documentProductId: Long) {
        return withContext(Dispatchers.IO) {
            linkDeliveryNoteToDocumentProductQueries.deleteDeliveryNoteProduct(documentProductId)
        }
    }

    override suspend fun addDeliveryNoteProduct(deliveryNoteId: Long, documentProductId: Long) {
        return withContext(Dispatchers.IO) {
            linkDeliveryNoteToDocumentProductQueries.saveDeliveryNoteProduct(
                id = null,
                delivery_note_id = deliveryNoteId,
                document_product_id = documentProductId
            )
        }
    }

    private fun getExistingIssuerOrSaveNewIssuer(deliveryNote: DeliveryNoteState) {
        deliveryNote.issuer?.id ?: deliveryNote.issuer?.let {
            clientOrIssuerQueries.save(
                client_or_issuer_id = null,
                type = "issuer",
                first_name = it.firstName?.text,
                name = it.name.text,
                address1 = it.address1?.text,
                address2 = it.address2?.text,
                zip_code = it.zipCode?.text,
                city = it.city?.text,
                phone = it.phone?.text,
                email = it.email?.text,
                notes = it.notes?.text
            )
        }
    }
}

fun calculateDocumentPrices(products: List<DocumentProductState>): DocumentPrices {
    val totalPriceWithoutTax = products.sumOf {
        (it.priceWithTax - it.priceWithTax * it.taxRate / BigDecimal(100)) * (it.quantity)
    }.setScale(2, RoundingMode.HALF_UP)

// Calculate the total amount of each tax
    val groupedItems = products.groupBy {
        it.taxRate
    }
    val taxes = groupedItems.keys.distinct() // ex: taxes= [20, 10]

    val amounts: MutableList<BigDecimal> = mutableListOf()  // ex: amounts = [7.2, 2.4]
    groupedItems.values.forEach { documentProduct ->
        val listOfAmounts = documentProduct.map {
            val priceWithoutTax = it.priceWithTax - it.priceWithTax * it.taxRate / BigDecimal(100)

            priceWithoutTax * it.quantity * it.taxRate / BigDecimal(100)
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