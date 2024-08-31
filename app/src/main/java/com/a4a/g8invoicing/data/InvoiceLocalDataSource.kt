package com.a4a.g8invoicing.data

import android.content.ContentValues
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.states.DocumentClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.DocumentState
import g8invoicing.DocumentClientOrIssuer
import g8invoicing.Invoice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class InvoiceLocalDataSource(
    db: Database,
) : InvoiceLocalDataSourceInterface {
    private val invoiceQueries = db.invoiceQueries
    private val invoiceTagQueries = db.invoiceTagQueries
    private val documentClientOrIssuerQueries = db.documentClientOrIssuerQueries
    private val documentProductQueries = db.documentProductQueries
    private val invoiceDocumentProductQueries = db.linkInvoiceToDocumentProductQueries
    private val linkDocumentToTagQueries = db.linkInvoiceToTagQueries
    private val invoiceDocumentProductAdditionalInfoQueries =
        db.linkInvoiceDocumentProductToDeliveryNoteQueries
    private val invoiceDocumentProductLinkQueries =
        db.linkInvoiceDocumentProductToDeliveryNoteQueries
    private val invoiceDocumentClientOrIssuerQueries = db.linkInvoiceToDocumentClientOrIssuerQueries

    override suspend fun createNew(): Long? {
        var newInvoiceId: Long? = null
        val docNumber = getLastDocumentNumber()?.let {
            incrementDocumentNumber(it)
        } ?: Strings.get(R.string.document_default_number)

        val issuer = getExistingIssuer()?.transformIntoEditable()
            ?: DocumentClientOrIssuerState()
        saveInfoInInvoiceTable(
            InvoiceState(
                documentNumber = TextFieldValue(docNumber),
                documentIssuer = issuer,
                footerText = TextFieldValue(getExistingFooter() ?: "")
            )
        )
        saveInfoInOtherTables(
            InvoiceState(documentIssuer = issuer)
        )
        try {
            newInvoiceId = invoiceQueries.getLastInsertedRowId().executeAsOneOrNull()
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return newInvoiceId
    }

    private fun getLastDocumentNumber(): String? {
        try {
            return invoiceQueries.getLastInvoiceNumber().executeAsOneOrNull()?.number
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    override fun fetch(id: Long): InvoiceState? {
        try {
            return invoiceQueries.get(id).executeAsOneOrNull()
                ?.let {
                    it.transformIntoEditableInvoice(
                        fetchDocumentProducts(it.invoice_id),
                        fetchClientAndIssuer(it.invoice_id)
                    )
                }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    override fun fetchAll(): Flow<List<InvoiceState>>? {
        try {
            return invoiceQueries.getAll()
                .asFlow()
                .map {
                    it.executeAsList()
                        .map { document ->
                            val products = fetchDocumentProducts(document.invoice_id)
                            val clientAndIssuer = fetchClientAndIssuer(document.invoice_id)
                            val tag = fetchTag(document.invoice_id)

                            document.transformIntoEditableInvoice(
                                products,
                                clientAndIssuer,
                                tag
                            )
                        }
                }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    private fun fetchDocumentProducts(id: Long): MutableList<DocumentProductState>? {
        try {
            val listOfIds = invoiceDocumentProductQueries.getDocumentProductsLinkedToInvoice(id)
                .executeAsList()
            return if (listOfIds.isNotEmpty()) {
                listOfIds.map {
                    val additionalInfo = invoiceDocumentProductLinkQueries
                        .getInfoLinkedToDocumentProduct(it.document_product_id)
                        .executeAsOneOrNull()
                    documentProductQueries.getDocumentProduct(it.document_product_id)
                        .executeAsOne()
                        .transformIntoEditableDocumentProduct(
                            additionalInfo?.date,
                            additionalInfo?.delivery_note_number
                        )
                }.toMutableList()
            } else null
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    private fun fetchClientAndIssuer(documentId: Long): List<DocumentClientOrIssuerState>? {
        try {
            val listOfIds =
                invoiceDocumentClientOrIssuerQueries.getDocumentClientOrIssuerLinkedToInvoice(
                    documentId
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

    private fun fetchTag(documentId: Long): DocumentTag? {
        try {
            val tagId = linkDocumentToTagQueries.getInvoiceTag(documentId).executeAsOneOrNull()
            tagId?.let {
                invoiceTagQueries.getTag(it).executeAsOneOrNull()?.let { tagName ->
                    val tag: DocumentTag = enumValueOf(tagName)
                    return tag
                }
            }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    private fun Invoice.transformIntoEditableInvoice(
        documentProducts: MutableList<DocumentProductState>? = null,
        documentClientAndIssuer: List<DocumentClientOrIssuerState>? = null,
        documentTag: DocumentTag? = null,
    ): InvoiceState {
        this.let {
            return InvoiceState(
                documentId = it.invoice_id.toInt(),
                documentTag = documentTag ?: DocumentTag.DRAFT,
                documentNumber = TextFieldValue(text = it.number ?: ""),
                documentDate = it.issuing_date ?: "",
                orderNumber = TextFieldValue(text = it.order_number ?: ""),
                documentIssuer = documentClientAndIssuer?.firstOrNull { it.type == ClientOrIssuerType.DOCUMENT_ISSUER },
                documentClient = documentClientAndIssuer?.firstOrNull { it.type == ClientOrIssuerType.DOCUMENT_CLIENT },
                documentProducts = documentProducts,
                documentPrices = documentProducts?.let { calculateDocumentPrices(it) },
                currency = TextFieldValue(Strings.get(R.string.currency)),
                dueDate = it.due_date ?: "",
                paymentStatus = it.payment_status.toInt(),
                footerText = TextFieldValue(text = it.footer ?: "")
            )
        }
    }

    override suspend fun convertDeliveryNotesToInvoice(deliveryNotes: List<DeliveryNoteState>) {
        withContext(Dispatchers.IO) {
            val docNumber = getLastDocumentNumber()?.let {
                incrementDocumentNumber(it)
            } ?: Strings.get(R.string.document_default_number)

            try {
                saveInfoInInvoiceTable(
                    InvoiceState(
                        documentNumber = TextFieldValue(docNumber),
                        orderNumber = deliveryNotes.firstOrNull { it.orderNumber != null }?.orderNumber,
                        documentIssuer = deliveryNotes.firstOrNull { it.documentIssuer != null }?.documentIssuer,
                        documentClient = deliveryNotes.firstOrNull { it.documentClient != null }?.documentClient,
                        footerText = TextFieldValue(getExistingFooter() ?: "")
                    )
                )
                deliveryNotes.forEach {
                    saveInfoInOtherTables(
                        it
                    )
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun update(document: InvoiceState) {
        return withContext(Dispatchers.IO) {
            try {
                invoiceQueries.update(
                    invoice_id = document.documentId?.toLong() ?: 0,
                    number = document.documentNumber.text,
                    issuing_date = document.documentDate,
                    order_number = document.orderNumber?.text,
                    currency = document.currency.text,
                    due_date = document.dueDate,
                    payment_status = document.paymentStatus.toLong(),
                    footer = document.footerText.text
                )
                // Update tag
                linkDocumentToDocumentTag(
                    document.documentId?.toLong() ?: 0,
                    document.documentTag,
                    isUpdate = true
                )
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun duplicate(documents: List<InvoiceState>) {
        val docNumber = getLastDocumentNumber()?.let {
            incrementDocumentNumber(it)
        } ?: Strings.get(R.string.document_default_number)

        withContext(Dispatchers.IO) {
            try {
                documents.forEach {
                    val invoice = InvoiceState(
                        documentType = it.documentType,
                        documentId = it.documentId,
                        documentNumber = TextFieldValue(docNumber),
                        documentDate = it.documentDate,
                        orderNumber = it.orderNumber,
                        documentIssuer = it.documentIssuer,
                        documentClient = it.documentClient,
                        documentProducts = it.documentProducts,
                        documentPrices = it.documentPrices,
                        currency = it.currency,
                        dueDate = it.dueDate,
                        footerText = it.footerText
                    )
                    saveInfoInInvoiceTable(invoice)
                    saveInfoInOtherTables(invoice)
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun setTag(documents: List<InvoiceState>, tag: DocumentTag) {
        withContext(Dispatchers.IO) {
            try {
                if(tag == DocumentTag.PAID) {
                    markAsPaid(documents)
                }
                documents.forEach { invoice ->
                    invoice.documentId?.toLong()?.let { invoiceId ->
                        linkDocumentToDocumentTag(invoiceId, tag, isUpdate = true)
                    }
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun markAsPaid(documents: List<InvoiceState>) {
        withContext(Dispatchers.IO) {
            try {
                documents.forEach {
                    if(it.paymentStatus == 0) {
                        it.paymentStatus = 2
                    } else {
                        it.paymentStatus = 0
                    }
                    update(it)
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun saveDocumentProductInDbAndLinkToDocument(
        documentProduct: DocumentProductState,
        documentId: Long?,
        deliveryNoteDate: String?,
        deliveryNoteNumber: String?,
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

                documentId?.let { documentId ->
                    documentProductQueries.getLastInsertedRowId().executeAsOneOrNull()?.toInt()
                        ?.let { id ->
                            linkDocumentProductToDocument(
                                documentId,
                                id.toLong()
                            )
                            if (!deliveryNoteDate.isNullOrEmpty()) {
                                linkDocumentProductToAdditionalInfo(
                                    id.toLong(),
                                    deliveryNoteNumber,
                                    deliveryNoteDate,
                                )
                            }
                        }
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun saveDocumentClientOrIssuerInDbAndLinkToDocument(
        documentClientOrIssuer: DocumentClientOrIssuerState,
        documentId: Long?,
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

                documentId?.let { documentId ->
                    documentClientOrIssuerQueries.getLastInsertedClientOrIssuerId()
                        .executeAsOneOrNull()?.toInt()
                        ?.let { id ->
                            linkDocumentClientOrIssuerToDocument(
                                documentId,
                                id.toLong()
                            )
                        }
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }


    override suspend fun delete(documents: List<InvoiceState>) {
        withContext(Dispatchers.IO) {
            try {
                documents.filter { it.documentId != null }.forEach { document ->
                    document.documentProducts?.mapNotNull { it.id }?.forEach {
                        documentProductQueries.deleteDocumentProduct(it.toLong())
                        invoiceDocumentProductAdditionalInfoQueries.deleteInfoLinkedToDocumentProduct(
                            it.toLong()
                        )
                    }
                    invoiceQueries.delete(id = document.documentId!!.toLong())
                    invoiceDocumentProductQueries.deleteAllProductsLinkedToInvoice(
                        document.documentId!!.toLong()
                    )
                    invoiceDocumentClientOrIssuerQueries.deleteAllDocumentClientOrIssuerLinkedToInvoice(
                        document.documentId!!.toLong()
                    )
                    document.documentClient?.type?.let {
                        deleteDocumentClientOrIssuer(
                            document.documentId!!.toLong(),
                            it
                        )
                    }
                    document.documentIssuer?.type?.let {
                        deleteDocumentClientOrIssuer(
                            document.documentId!!.toLong(),
                            it
                        )
                    }
                    document.documentProducts?.filter { it.id != null }?.let {
                        it.forEach { documentProduct ->
                            deleteDocumentProduct(
                                document.documentId!!.toLong(),
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

    override suspend fun deleteDocumentProduct(documentId: Long, documentProductId: Long) {
        try {
            return withContext(Dispatchers.IO) {
                invoiceDocumentProductQueries.deleteProductLinkedToInvoice(
                    documentId,
                    documentProductId
                )
                invoiceDocumentProductAdditionalInfoQueries.deleteInfoLinkedToDocumentProduct(
                    documentProductId
                )
            }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

    override suspend fun deleteDocumentClientOrIssuer(
        id: Long,
        type: ClientOrIssuerType,
    ) {
        try {
            return withContext(Dispatchers.IO) {
                val documentClientOrIssuer =
                    fetchClientAndIssuer(id)?.firstOrNull { it.type == type }

                documentClientOrIssuer?.id?.let {
                    invoiceDocumentClientOrIssuerQueries.deleteDocumentClientOrIssuerLinkedToInvoice(
                        id,
                        it.toLong()
                    )
                    documentClientOrIssuerQueries.delete(it.toLong())
                }
            }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

    override fun linkDocumentProductToDocument(id: Long, documentProductId: Long) {
        try {
            invoiceDocumentProductQueries.saveProductLinkedToInvoice(
                id = null,
                invoice_id = id,
                document_product_id = documentProductId
            )
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

    override suspend fun linkDocumentToDocumentTag(documentId: Long, tag: DocumentTag, isUpdate: Boolean) {
        try {
            withContext(Dispatchers.IO) {
                invoiceTagQueries.getTagId(tag.name).executeAsOneOrNull()?.let {
                    if (isUpdate) {
                        linkDocumentToTagQueries.updateInvoiceTag(
                            invoice_id = documentId,
                            tag_id = it
                        )
                    } else {
                        linkDocumentToTagQueries.saveInvoiceTag(
                            id = null,
                            invoice_id = documentId,
                            tag_id = it
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

    override fun linkDocumentProductToAdditionalInfo(
        documentProductId: Long,
        deliveryNoteNumber: String?,
        deliveryNoteDate: String,
    ) {
        try {
            invoiceDocumentProductAdditionalInfoQueries.saveInfoLinkedToDocumentProduct(
                document_product_id = documentProductId,
                delivery_note_number = deliveryNoteNumber,
                date = deliveryNoteDate
            )
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

    private fun linkDocumentClientOrIssuerToDocument(
        documentId: Long,
        documentClientOrIssuerId: Long,
    ) {
        try {
            invoiceDocumentClientOrIssuerQueries.saveDocumentClientOrIssuerLinkedToInvoice(
                id = null,
                invoice_id = documentId,
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

    private fun getExistingFooter(): String? {
        var footer: String? = null
        try {
            footer = invoiceQueries.getLastInsertedInvoiceFooter().executeAsOneOrNull()?.footer
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return footer
    }

    private fun saveInfoInInvoiceTable(document: InvoiceState) {
        try {
            invoiceQueries.save(
                invoice_id = null,
                number = document.documentNumber.text,
                issuing_date = document.documentDate,
                order_number = document.orderNumber?.text,
                currency = document.currency.text,
                due_date = document.dueDate,
                payment_status = document.paymentStatus.toLong(),
                footer = document.footerText.text
            )
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }

    }

    private suspend fun saveInfoInOtherTables(document: DocumentState) {
        try {
            invoiceQueries.getLastInsertedRowId().executeAsOneOrNull()?.let { id ->
                // Link tag
                linkDocumentToDocumentTag(id, document.documentTag)

                // Link all products
                document.documentProducts?.forEach { documentProduct ->
                    saveDocumentProductInDbAndLinkToDocument(
                        documentProduct = documentProduct,
                        documentId = id,
                        deliveryNoteDate = if (document is DeliveryNoteState) document.documentDate else null,
                        deliveryNoteNumber = if (document is DeliveryNoteState) document.documentNumber.text else null
                    )
                }

                // Link client
                document.documentClient?.let {
                    saveDocumentClientOrIssuerInDbAndLinkToDocument(
                        documentClientOrIssuer = it,
                        documentId = id
                    )
                }

                // Link issuer
                document.documentIssuer?.let {
                    saveDocumentClientOrIssuerInDbAndLinkToDocument(
                        documentClientOrIssuer = it,
                        documentId = id
                    )
                }

            }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }
}

fun incrementDocumentNumber(docNumber: String): String {
    val numberToIncrement = docNumber.takeLastWhile { it.isDigit() }
    if (numberToIncrement.isNotEmpty()) {
        val firstPartOfDocNumber = docNumber.substringBeforeLast(numberToIncrement)
        return firstPartOfDocNumber + (numberToIncrement.toInt() + 1).toString().padStart(3, '0')
    } else return docNumber
}