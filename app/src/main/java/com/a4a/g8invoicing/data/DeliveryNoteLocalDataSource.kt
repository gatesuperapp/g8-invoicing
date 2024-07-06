package com.a4a.g8invoicing.data

import androidx.compose.ui.text.input.TextFieldValue
import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.screens.ClientOrIssuerType
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DocumentClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentPrices
import com.a4a.g8invoicing.ui.states.DocumentProductState
import g8invoicing.ClientOrIssuer
import g8invoicing.DeliveryNote
import g8invoicing.DocumentClientOrIssuer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode

class DeliveryNoteLocalDataSource(
    db: Database,
) : DeliveryNoteLocalDataSourceInterface {
    private val deliveryNoteQueries = db.deliveryNoteQueries
    private val documentClientOrIssuerQueries = db.documentClientOrIssuerQueries
    private val documentProductQueries = db.documentProductQueries
    private val deliveryNoteDocumentProductQueries = db.linkDeliveryNoteToDocumentProductQueries
    private val deliveryNoteDocumentClientOrIssuerQueries =
        db.linkDeliveryNoteToDocumentClientOrIssuerQueries


    override fun fetchDeliveryNoteFlow(id: Long): Flow<DeliveryNoteState?> {
        val deliveryNoteId = deliveryNoteQueries.getDeliveryNoteId(id).executeAsOne()
        val deliveryNoteFlow = deliveryNoteQueries.getDeliveryNote(id).asFlow().map { query ->
            query.executeAsOne()
        }

        val documentProductFlow = fetchDocumentProductsFlow(deliveryNoteId).onStart { emit(emptyList()) }
        val clientAndIssuerFlow = fetchClientOrIssuerFlow(deliveryNoteId).onStart { emit(emptyList()) }

        val combinedFlow = combine(
            deliveryNoteFlow,
            documentProductFlow,
            clientAndIssuerFlow
        ) { value1, value2, value3 ->
            value1.transformIntoEditableNote(
                documentProducts = value2,
                documentClientAndIssuer = value3,
            )
        }
        return combinedFlow
    }


    override fun fetchAllDeliveryNotes(): Flow<List<DeliveryNoteState>> {
        return deliveryNoteQueries.getAllDeliveryNotes()
            .asFlow()
            .map {
                it.executeAsList()
                    .map { deliveryNote ->
                        val products = fetchDocumentProducts(deliveryNote.delivery_note_id)
                        val clientAndIssuer = fetchClientAndIssuer(deliveryNote.delivery_note_id)

                        deliveryNote.transformIntoEditableNote(
                            products,
                            clientAndIssuer
                        )
                    }
            }
    }

    private fun fetchDocumentProducts(deliveryNoteId: Long): List<DocumentProductState>? {
        val listOfIds =
            deliveryNoteDocumentProductQueries.getProductsLinkedToDeliveryNote(deliveryNoteId)
                .executeAsList()
        return if (listOfIds.isNotEmpty()) {
            listOfIds.map {
                documentProductQueries.getDocumentProduct(it.document_product_id)
                    .executeAsOne().transformIntoEditableDocumentProduct()
            }
        } else null
    }

    private fun fetchClientAndIssuer(deliveryNoteId: Long): List<DocumentClientOrIssuerState>? {
        val listOfIds =
            deliveryNoteDocumentClientOrIssuerQueries.getDocumentClientOrIssuerLinkedToDeliveryNote(
                deliveryNoteId
            ).executeAsList()

        return if (listOfIds.isNotEmpty()) {
            listOfIds.map {
                documentClientOrIssuerQueries.get(it.document_client_or_issuer_id)
                    .executeAsOne()
                    .transformIntoEditable()
            }
        } else null
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun fetchClientOrIssuerFlow(deliveryNoteId: Long): Flow<List<DocumentClientOrIssuerState>> {
        return deliveryNoteDocumentClientOrIssuerQueries.getDocumentClientOrIssuerLinkedToDeliveryNote(
            deliveryNoteId
        )
            .asFlow()
            .flatMapMerge { query ->
                combine(
                    query.executeAsList().map {
                        documentClientOrIssuerQueries.get(it.document_client_or_issuer_id)
                            .asFlow()
                            .map {
                                it.executeAsOne().transformIntoEditable()
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
                it.transformIntoEditableNote(
                    fetchDocumentProducts(it.delivery_note_id),
                    fetchClientAndIssuer(it.delivery_note_id)
                )
            }
    }

    private fun DeliveryNote.transformIntoEditableNote(
        documentProducts: List<DocumentProductState>? = null,
        documentClientAndIssuer: List<DocumentClientOrIssuerState>? = null,
    ): DeliveryNoteState {

        this.let {
            return DeliveryNoteState(
                documentId = it.delivery_note_id.toInt(),
                documentNumber = TextFieldValue(text = it.number ?: ""),
                documentDate = it.delivery_date ?: "",
                orderNumber = TextFieldValue(text = it.order_number ?: ""),
                documentIssuer = documentClientAndIssuer?.firstOrNull { it.type == ClientOrIssuerType.DOCUMENT_ISSUER },
                documentClient = documentClientAndIssuer?.firstOrNull { it.type == ClientOrIssuerType.DOCUMENT_CLIENT },
                documentProducts = documentProducts,
                documentPrices = documentProducts?.let { calculateDocumentPrices(it) },
                currency = TextFieldValue(Strings.get(R.string.currency))
            )
        }
    }

    override suspend fun createNewDeliveryNote(): Long? {
        saveDeliveryNoteInfoInAllTables(
            DeliveryNoteState(
                documentIssuer = getExistingIssuer()?.transformIntoEditable()
                    ?: DocumentClientOrIssuerState(),
            )
        )

        return deliveryNoteQueries.getLastInsertedRowId().executeAsOneOrNull()
    }

    override suspend fun updateDeliveryNote(deliveryNote: DeliveryNoteState) {
        return withContext(Dispatchers.IO) {
            try {
                deliveryNoteQueries.updateDeliveryNote(
                    delivery_note_id = deliveryNote.documentId?.toLong() ?: 0,
                    number = deliveryNote.documentNumber.text,
                    delivery_date = deliveryNote.documentDate,
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
                    documentProductQueries.getLastInsertedRowId().executeAsOneOrNull()?.toInt()
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
                    type = if (documentClientOrIssuer.type == ClientOrIssuerType.DOCUMENT_ISSUER ||
                        documentClientOrIssuer.type == ClientOrIssuerType.ISSUER
                    )
                        ClientOrIssuerType.ISSUER.name.lowercase() else ClientOrIssuerType.CLIENT.name.lowercase(),
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
                    documentClientOrIssuerQueries.getLastInsertedClientOrIssuerId().executeAsOneOrNull()?.toInt()
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
                    deliveryNoteDocumentClientOrIssuerQueries.deleteAllDocumentClientOrIssuerLinkedToADeliveryNote(
                        deliveryNote.documentId!!.toLong()
                    )
                    deliveryNote.documentClient?.id?.let {
                        deleteDocumentClientOrIssuer(
                            deliveryNote.documentId!!.toLong(),
                            it.toLong()
                        )
                    }
                    deliveryNote.documentIssuer?.id?.let {
                        deleteDocumentClientOrIssuer(
                            deliveryNote.documentId!!.toLong(),
                            it.toLong()
                        )
                    }
                    deliveryNote.documentProducts?.filter { it.id != null }?.let {
                        it.forEach { documentProduct ->
                            deleteDeliveryNoteProduct(
                                deliveryNote.documentId!!.toLong(),
                                documentProduct.id!!.toLong()
                            )
                        }
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

    private fun getExistingIssuer(): DocumentClientOrIssuer? {
        var issuer: DocumentClientOrIssuer? = null
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

        deliveryNoteQueries.getLastInsertedRowId().executeAsOneOrNull()?.let { deliveryNoteId ->
            // Link all products
            deliveryNote.documentProducts?.forEach { documentProduct ->
                saveDocumentProductInDbAndLinkToDocument(
                    documentProduct = documentProduct,
                    deliveryNoteId = deliveryNoteId
                )
            }

            // Link client
            deliveryNote.documentClient?.let {
                saveDocumentClientOrIssuerInDbAndLinkToDocument(
                    documentClientOrIssuer = it,
                    deliveryNoteId = deliveryNoteId
                )
            }

            // Link issuer
            deliveryNote.documentIssuer?.let {
                saveDocumentClientOrIssuerInDbAndLinkToDocument(
                    documentClientOrIssuer = it,
                    deliveryNoteId = deliveryNoteId
                )
            }
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