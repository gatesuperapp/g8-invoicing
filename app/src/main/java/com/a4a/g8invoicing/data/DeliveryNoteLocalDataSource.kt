package com.a4a.g8invoicing.data

import android.content.ContentValues
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.screens.shared.getDateFormatter
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentPrices
import com.a4a.g8invoicing.ui.states.DocumentProductState
import g8invoicing.DeliveryNote
import g8invoicing.DocumentClientOrIssuer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Calendar

class DeliveryNoteLocalDataSource(
    db: Database,
) : DeliveryNoteLocalDataSourceInterface {
    private val deliveryNoteQueries = db.deliveryNoteQueries
    private val documentClientOrIssuerQueries = db.documentClientOrIssuerQueries
    private val documentClientOrIssuerAddressQueries = db.documentClientOrIssuerAddressQueries
    private val linkDocumentClientOrIssuerToAddressQueries = db.linkDocumentClientOrIssuerToAddressQueries
    private val documentProductQueries = db.documentProductQueries
    private val linkDeliveryNoteToDocumentProductQueries = db.linkDeliveryNoteToDocumentProductQueries
    private val linkDeliveryNoteToDocumentClientOrIssuerQueries =
        db.linkDeliveryNoteToDocumentClientOrIssuerQueries

    override suspend fun createNew(): Long? {
        var newDeliveryNoteId: Long? = null
        val docNumber = getLastDocumentNumber()?.let {
            incrementDocumentNumber(it)
        } ?: Strings.get(R.string.delivery_note_default_number)
        val issuer = getExistingIssuer()?.transformIntoEditable()

        saveInfoInDocumentTable(
            DeliveryNoteState(
                documentNumber = TextFieldValue(docNumber),
                documentIssuer = issuer,
                footerText = TextFieldValue(getExistingFooter() ?: "")
            )
        )
        saveInfoInOtherTables(
            DeliveryNoteState(documentIssuer = issuer)
        )

        try {
            newDeliveryNoteId = deliveryNoteQueries.getLastInsertedRowId().executeAsOneOrNull()
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return newDeliveryNoteId
    }


    override fun fetchDeliveryNote(id: Long): DeliveryNoteState? {
        try {
            return deliveryNoteQueries.get(id).executeAsOneOrNull()
                ?.let {
                    it.transformIntoEditableNote(
                        fetchDocumentProducts(it.delivery_note_id),
                        fetchClientAndIssuer(
                            it.delivery_note_id,
                            linkDeliveryNoteToDocumentClientOrIssuerQueries,
                            linkDocumentClientOrIssuerToAddressQueries,
                            documentClientOrIssuerQueries,
                            documentClientOrIssuerAddressQueries
                        )
                    )
                }
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    override fun fetchAllDeliveryNotes(): Flow<List<DeliveryNoteState>>? {
        try {
            return deliveryNoteQueries.getAll()
                .asFlow()
                .map {
                    it.executeAsList()
                        .map { deliveryNote ->
                            val products = fetchDocumentProducts(deliveryNote.delivery_note_id)
                            val clientAndIssuer =
                                fetchClientAndIssuer(
                                    deliveryNote.delivery_note_id,
                                    linkDeliveryNoteToDocumentClientOrIssuerQueries,
                                    linkDocumentClientOrIssuerToAddressQueries,
                                    documentClientOrIssuerQueries,
                                    documentClientOrIssuerAddressQueries
                                )

                            deliveryNote.transformIntoEditableNote(
                                products,
                                clientAndIssuer
                            )
                        }
                }
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    private fun getExistingFooter(): String? {
        var footer: String? = null
        try {
            footer = deliveryNoteQueries.getLastInsertedFooter().executeAsOneOrNull()?.footer
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return footer
    }

    private fun fetchDocumentProducts(deliveryNoteId: Long): MutableList<DocumentProductState>? {
        try {
            val listOfIds =
                linkDeliveryNoteToDocumentProductQueries.getDocumentProductsLinkedToDeliveryNote(
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
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }


    private fun DeliveryNote.transformIntoEditableNote(
        documentProducts: MutableList<DocumentProductState>? = null,
        documentClientAndIssuer: List<ClientOrIssuerState>? = null,
    ): DeliveryNoteState {
        this.let {
            return DeliveryNoteState(
                documentId = it.delivery_note_id.toInt(),
                documentNumber = TextFieldValue(text = it.number ?: ""),
                documentDate = it.delivery_date ?: "",
                reference = TextFieldValue(text = it.reference ?: ""),
                freeField = TextFieldValue(text = it.free_field ?: ""),
                documentIssuer = documentClientAndIssuer?.firstOrNull { it.type == ClientOrIssuerType.DOCUMENT_ISSUER },
                documentClient = documentClientAndIssuer?.firstOrNull { it.type == ClientOrIssuerType.DOCUMENT_CLIENT },
                documentProducts = documentProducts,
                documentPrices = documentProducts?.let { calculateDocumentPrices(it) },
                currency = TextFieldValue(Strings.get(R.string.currency)),
                footerText = TextFieldValue(text = it.footer ?: ""),
                createdDate = it.created_at
            )
        }
    }

    override suspend fun update(document: DeliveryNoteState) {
        return withContext(Dispatchers.IO) {
            try {
                deliveryNoteQueries.update(
                    delivery_note_id = document.documentId?.toLong() ?: 0,
                    number = document.documentNumber.text,
                    delivery_date = document.documentDate,
                    reference = document.reference?.text,
                    free_field = document.freeField?.text,
                    currency = document.currency.text,
                    footer = document.footerText.text,
                    updated_at = getDateFormatter(pattern = "yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().time)
                )
            } catch (e: Exception) {
                //Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun duplicate(documents: List<DeliveryNoteState>) {
        withContext(Dispatchers.IO) {
            try {
                documents.forEach {
                    val docNumber = getLastDocumentNumber()?.let {
                        incrementDocumentNumber(it)
                    } ?: Strings.get(R.string.delivery_note_default_number)
                    val document = it
                    document.documentNumber = TextFieldValue(docNumber)

                    saveInfoInDocumentTable(document)
                    saveInfoInOtherTables(document)
                }
            } catch (e: Exception) {
                //Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    private fun getLastDocumentNumber(): String? {
        try {
            return deliveryNoteQueries.getLastDeliveryNoteNumber().executeAsOneOrNull()?.number
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    override suspend fun saveDocumentProductInDbAndLinkToDocument(
        documentProduct: DocumentProductState,
        deliveryNoteId: Long?,
    ): Int? {
        var documentProductId: Int? = null
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
                            documentProductId = id
                            addDocumentProduct(
                                deliveryNoteId,
                                id.toLong()
                            )
                        }
                }
            } catch (e: Exception) {
                //Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
        return documentProductId

    }

    override suspend fun saveDocumentClientOrIssuerInDbAndLinkToDocument(
        documentClientOrIssuer: ClientOrIssuerState,
        documentId: Long?,
    ) {
        withContext(Dispatchers.IO) {
            try {
                saveDocumentClientOrIssuerInDbAndLink(
                    documentClientOrIssuerQueries,
                    documentClientOrIssuerAddressQueries,
                    linkDocumentClientOrIssuerToAddressQueries,
                    linkDeliveryNoteToDocumentClientOrIssuerQueries,
                    documentClientOrIssuer,
                    documentId
                )
            } catch (e: Exception) {
                //Log.e(ContentValues.TAG, "Error: ${e.message}")
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

                    deliveryNoteQueries.delete(id = deliveryNote.documentId!!.toLong())
                    linkDeliveryNoteToDocumentProductQueries.deleteAllProductsLinkedToADeliveryNote(
                        deliveryNote.documentId!!.toLong()
                    )
                    linkDeliveryNoteToDocumentClientOrIssuerQueries.deleteAllDocumentClientOrIssuerLinkedToADeliveryNote(
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
                //Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun deleteDocumentProduct(deliveryNoteId: Long, documentProductId: Long) {
        try {
            return withContext(Dispatchers.IO) {
                linkDeliveryNoteToDocumentProductQueries.deleteProductLinkedToDeliveryNote(
                    deliveryNoteId,
                    documentProductId
                )
            }
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

    override suspend fun deleteDocumentClientOrIssuer(
        deliveryNoteId: Long,
        type: ClientOrIssuerType,
    ) {
        try {
            return withContext(Dispatchers.IO) {
                val documentClientOrIssuer =
                    fetchClientAndIssuer(
                        deliveryNoteId,
                        linkDeliveryNoteToDocumentClientOrIssuerQueries,
                        linkDocumentClientOrIssuerToAddressQueries,
                        documentClientOrIssuerQueries,
                        documentClientOrIssuerAddressQueries
                    )?.firstOrNull { it.type == type }

                documentClientOrIssuer?.id?.let {
                    linkDeliveryNoteToDocumentClientOrIssuerQueries.deleteDocumentClientOrIssuerLinkedToDeliveryNote(
                        deliveryNoteId,
                        it.toLong()
                    )
                    documentClientOrIssuerQueries.delete(it.toLong())
                }
            }
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

    override fun addDocumentProduct(deliveryNoteId: Long, documentProductId: Long) {
        try {
            linkDeliveryNoteToDocumentProductQueries.saveProductLinkedToDeliveryNote(
                id = null,
                delivery_note_id = deliveryNoteId,
                document_product_id = documentProductId
            )
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }


    private fun getExistingIssuer(): DocumentClientOrIssuer? {
        var issuer: DocumentClientOrIssuer? = null
        try {
            issuer = documentClientOrIssuerQueries.getLastInsertedIssuer().executeAsOneOrNull()
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return issuer
    }

    private fun saveInfoInDocumentTable(document: DeliveryNoteState) {
        try {
            deliveryNoteQueries.save(
                delivery_note_id = null,
                number = document.documentNumber.text,
                delivery_date = document.documentDate,
                reference = document.reference?.text,
                free_field = document.freeField?.text,
                currency = document.currency.text,
                footer = document.footerText.text
            )
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

    private suspend fun saveInfoInOtherTables(
        deliveryNote: DeliveryNoteState,
    ) {
        deliveryNoteQueries.getLastInsertedRowId().executeAsOneOrNull()?.let { id ->
            // Link all products
            deliveryNote.documentProducts?.forEach { documentProduct ->
                saveDocumentProductInDbAndLinkToDocument(
                    documentProduct = documentProduct,
                    deliveryNoteId = id
                )
            }

            // Link client
            deliveryNote.documentClient?.let {
                saveDocumentClientOrIssuerInDbAndLinkToDocument(
                    documentClientOrIssuer = it,
                    documentId = id
                )
            }

            // Link issuer
            deliveryNote.documentIssuer?.let {
                saveDocumentClientOrIssuerInDbAndLinkToDocument(
                    documentClientOrIssuer = it,
                    documentId = id
                )
            }
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
            val value = it.priceWithTax!!.toDouble() / (1.0 + (it.taxRate ?: 0.0).toDouble() / 100.0)
            val priceWithoutTax = BigDecimal(value)

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