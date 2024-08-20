package com.a4a.g8invoicing.data

import android.content.ContentValues
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DocumentClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentPrices
import com.a4a.g8invoicing.ui.states.DocumentProductState
import g8invoicing.DeliveryNote
import g8invoicing.DocumentClientOrIssuer
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
    private val invoiceQueries = db.invoiceQueries
    private val documentClientOrIssuerQueries = db.documentClientOrIssuerQueries
    private val documentProductQueries = db.documentProductQueries
    private val deliveryNoteDocumentProductQueries = db.linkDeliveryNoteToDocumentProductQueries
    private val deliveryNoteDocumentClientOrIssuerQueries =
        db.linkDeliveryNoteToDocumentClientOrIssuerQueries

    override suspend fun createNewDeliveryNote(): Long? {
        var newDeliveryNoteId: Long? = null

        var docNumber = getLastDocumentNumber() ?: Strings.get(R.string.document_default_number)
        docNumber = incrementDocumentNumber(docNumber)

        saveDeliveryNoteInfoInAllTables(
            DeliveryNoteState(
                documentNumber = TextFieldValue(docNumber),
                documentIssuer = getExistingIssuer()?.transformIntoEditable()
                    ?: DocumentClientOrIssuerState(),
            )
        )

        try {
            newDeliveryNoteId = deliveryNoteQueries.getLastInsertedRowId().executeAsOneOrNull()
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return newDeliveryNoteId
    }


    override fun fetchDeliveryNote(id: Long): DeliveryNoteState? {
        try {
            return deliveryNoteQueries.getDeliveryNote(id).executeAsOneOrNull()
                ?.let {
                    it.transformIntoEditableNote(
                        fetchDocumentProducts(it.delivery_note_id),
                        fetchClientAndIssuer(it.delivery_note_id)
                    )
                }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    override fun fetchAllDeliveryNotes(): Flow<List<DeliveryNoteState>>? {
        try {
            return deliveryNoteQueries.getAllDeliveryNotes()
                .asFlow()
                .map {
                    it.executeAsList()
                        .map { deliveryNote ->
                            val products = fetchDocumentProducts(deliveryNote.delivery_note_id)
                            val clientAndIssuer =
                                fetchClientAndIssuer(deliveryNote.delivery_note_id)

                            deliveryNote.transformIntoEditableNote(
                                products,
                                clientAndIssuer
                            )
                        }
                }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    private fun fetchDocumentProducts(deliveryNoteId: Long): MutableList<DocumentProductState>? {
        try {
            val listOfIds =
                deliveryNoteDocumentProductQueries.getDocumentProductsLinkedToDeliveryNote(
                    deliveryNoteId
                )
                    .executeAsList()
            return if (listOfIds.isNotEmpty()) {
                listOfIds.map {
                    documentProductQueries.getDocumentProduct(it.document_product_id)
                        .executeAsOne().transformIntoEditableDocumentProduct()
                }.toMutableList()
            } else null
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    private fun fetchClientAndIssuer(deliveryNoteId: Long): List<DocumentClientOrIssuerState>? {
        try {
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

        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    private fun DeliveryNote.transformIntoEditableNote(
        documentProducts: MutableList<DocumentProductState>? = null,
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

    override suspend fun updateDeliveryNote(deliveryNote: DeliveryNoteState) {
        return withContext(Dispatchers.IO) {
            try {
                deliveryNoteQueries.updateDeliveryNote(
                    delivery_note_id = deliveryNote.documentId?.toLong() ?: 0,
                    number = deliveryNote.documentNumber.text,
                    delivery_date = deliveryNote.documentDate,
                    order_number = deliveryNote.orderNumber?.text,
                    currency = deliveryNote.currency.text
                )
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun duplicateDeliveryNotes(deliveryNotes: List<DeliveryNoteState>) {
        var docNumber = getLastDocumentNumber() ?: Strings.get(R.string.document_default_number)
        docNumber = incrementDocumentNumber(docNumber)

        withContext(Dispatchers.IO) {
            try {
                deliveryNotes.forEach {
                    saveDeliveryNoteInfoInAllTables(
                        DeliveryNoteState(
                            documentType = it.documentType,
                            documentId = it.documentId,
                            documentNumber = TextFieldValue(docNumber),
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
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    private fun getLastDocumentNumber(): String? {
        try {
            return deliveryNoteQueries.getLastDeliveryNoteNumber().executeAsOneOrNull()?.number
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
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
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
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
                    documentClientOrIssuerQueries.getLastInsertedClientOrIssuerId()
                        .executeAsOneOrNull()?.toInt()
                        ?.let { id ->
                            addDocumentClientOrIssuer(
                                deliveryNoteId,
                                id.toLong()
                            )
                        }
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }


    override suspend fun deleteDeliveryNotes(deliveryNotes: List<DeliveryNoteState>) {
        withContext(Dispatchers.IO) {
            try {
                deliveryNotes.filter { it.documentId != null }.forEach { deliveryNote ->
                    deliveryNote.documentProducts?.mapNotNull { it.id }?.forEach {
                        documentProductQueries.deleteDocumentProduct(it.toLong())
                    }

                    deliveryNoteQueries.deleteDeliveryNote(id = deliveryNote.documentId!!.toLong())
                    deliveryNoteDocumentProductQueries.deleteAllProductsLinkedToADeliveryNote(
                        deliveryNote.documentId!!.toLong()
                    )
                    deliveryNoteDocumentClientOrIssuerQueries.deleteAllDocumentClientOrIssuerLinkedToADeliveryNote(
                        deliveryNote.documentId!!.toLong()
                    )
                    deliveryNote.documentClient?.type?.let {
                        deleteDocumentClientOrIssuer(
                            deliveryNote.documentId!!.toLong(),
                            it
                        )
                    }
                    deliveryNote.documentIssuer?.type?.let {
                        deleteDocumentClientOrIssuer(
                            deliveryNote.documentId!!.toLong(),
                            it
                        )
                    }
                    deliveryNote.documentProducts?.filter { it.id != null }?.let {
                        it.forEach { documentProduct ->
                            deleteDocumentProduct(
                                deliveryNote.documentId!!.toLong(),
                                documentProduct.id!!.toLong()
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun deleteDocumentProduct(deliveryNoteId: Long, documentProductId: Long) {
        try {
            return withContext(Dispatchers.IO) {
                deliveryNoteDocumentProductQueries.deleteProductLinkedToDeliveryNote(
                    deliveryNoteId,
                    documentProductId
                )
            }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

    override suspend fun deleteDocumentClientOrIssuer(
        deliveryNoteId: Long,
        type: ClientOrIssuerType,
    ) {
        try {
            return withContext(Dispatchers.IO) {
                val documentClientOrIssuer =
                    fetchClientAndIssuer(deliveryNoteId)?.firstOrNull { it.type == type }

                documentClientOrIssuer?.id?.let {
                    deliveryNoteDocumentClientOrIssuerQueries.deleteDocumentClientOrIssuerLinkedToDeliveryNote(
                        deliveryNoteId,
                        it.toLong()
                    )
                    documentClientOrIssuerQueries.delete(it.toLong())
                }
            }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

    override fun addDocumentProduct(deliveryNoteId: Long, documentProductId: Long) {
        try {
            deliveryNoteDocumentProductQueries.saveProductLinkedToDeliveryNote(
                id = null,
                delivery_note_id = deliveryNoteId,
                document_product_id = documentProductId
            )
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

    private fun addDocumentClientOrIssuer(deliveryNoteId: Long, documentClientOrIssuerId: Long) {
        try {
            deliveryNoteDocumentClientOrIssuerQueries.saveDocumentClientOrIssuerLinkedToDeliveryNote(
                id = null,
                delivery_note_id = deliveryNoteId,
                document_client_or_issuer_id = documentClientOrIssuerId
            )
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

    private fun getExistingIssuer(): DocumentClientOrIssuer? {
        var issuer: DocumentClientOrIssuer? = null
        try {
            issuer = documentClientOrIssuerQueries.getLastInsertedIssuer().executeAsOneOrNull()
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return issuer
    }

    private suspend fun saveDeliveryNoteInfoInAllTables(deliveryNote: DeliveryNoteState) {
        try {
            deliveryNoteQueries.saveDeliveryNote(
                delivery_note_id = null,
                number = deliveryNote.documentNumber.text,
                delivery_date = deliveryNote.documentDate,
                order_number = deliveryNote.orderNumber?.text,
                currency = deliveryNote.currency.text
            )

            deliveryNoteQueries.getLastInsertedRowId().executeAsOneOrNull()?.let { deliveryNoteId ->
                saveAndLinkOtherElements(deliveryNote, deliveryNoteId)
            }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }


    private suspend fun saveAndLinkOtherElements(
        deliveryNote: DeliveryNoteState,
        deliveryNoteId: Long,
    ) {
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


fun calculateDocumentPrices(products: List<DocumentProductState>): DocumentPrices {
    val totalPriceWithoutTax = products.filter { it.priceWithTax != null }.sumOf {
        (it.priceWithTax!! -
                it.priceWithTax!!
                * (it.taxRate ?: BigDecimal(0))
                / BigDecimal(100)) * (it.quantity)
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