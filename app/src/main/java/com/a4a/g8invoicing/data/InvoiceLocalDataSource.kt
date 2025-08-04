package com.a4a.g8invoicing.data

import androidx.compose.ui.text.input.TextFieldValue
import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.screens.shared.getDateFormatter
import com.a4a.g8invoicing.ui.states.AddressState
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.DocumentState
import g8invoicing.DocumentClientOrIssuer
import g8invoicing.DocumentClientOrIssuerAddressQueries
import g8invoicing.DocumentClientOrIssuerQueries
import g8invoicing.DocumentProductQueries
import g8invoicing.Invoice
import g8invoicing.LinkCreditNoteDocumentProductToDeliveryNoteQueries
import g8invoicing.LinkCreditNoteToDocumentClientOrIssuerQueries
import g8invoicing.LinkCreditNoteToDocumentProductQueries
import g8invoicing.LinkDeliveryNoteToDocumentClientOrIssuerQueries
import g8invoicing.LinkDeliveryNoteToDocumentProductQueries
import g8invoicing.LinkDocumentClientOrIssuerToAddressQueries
import g8invoicing.LinkInvoiceDocumentProductToDeliveryNoteQueries
import g8invoicing.LinkInvoiceToDocumentClientOrIssuerQueries
import g8invoicing.LinkInvoiceToDocumentProductQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar

class InvoiceLocalDataSource(
    db: Database,
) : InvoiceLocalDataSourceInterface {
    private val invoiceQueries = db.invoiceQueries
    private val invoiceTagQueries = db.invoiceTagQueries
    private val documentClientOrIssuerQueries = db.documentClientOrIssuerQueries
    private val documentClientOrIssuerAddressQueries = db.documentClientOrIssuerAddressQueries
    private val linkDocumentClientOrIssuerToAddressQueries =
        db.linkDocumentClientOrIssuerToAddressQueries
    private val documentProductQueries = db.documentProductQueries
    private val linkInvoiceToDocumentProductQueries = db.linkInvoiceToDocumentProductQueries
    private val linkInvoiceToTagQueries = db.linkInvoiceToTagQueries
    private val linkInvoiceDocumentProductToDeliveryNoteQueries =
        db.linkInvoiceDocumentProductToDeliveryNoteQueries
    private val linkInvoiceToDocumentClientOrIssuerQueries =
        db.linkInvoiceToDocumentClientOrIssuerQueries


    override suspend fun createNew(): Long? {
        var newInvoiceId: Long? = null
        val issuer = getExistingIssuer()?.transformIntoEditable()
        saveInfoInInvoiceTable(
            InvoiceState(
                documentNumber = TextFieldValue(getLastDocumentNumber()?.let {
                    incrementDocumentNumber(it)
                } ?: Strings.get(R.string.invoice_default_number)),
                documentIssuer = issuer,
                footerText = TextFieldValue(
                    getExistingFooter() ?: Strings.get(R.string.document_default_footer)
                )
            )
        )
        saveTag(InvoiceState(documentIssuer = issuer))
        saveInfoInOtherTables(InvoiceState(documentIssuer = issuer))
        try {
            newInvoiceId = invoiceQueries.getLastInsertedRowId().executeAsOneOrNull()
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return newInvoiceId
    }

    private fun getLastDocumentNumber(): String? {
        try {
            return invoiceQueries.getLastInvoiceNumber().executeAsOneOrNull()?.number
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    override fun fetch(id: Long): InvoiceState? {
        try {
            return invoiceQueries.get(id).executeAsOneOrNull()
                ?.let {
                    it.transformIntoEditableInvoice(
                        fetchDocumentProducts(it.invoice_id),
                        fetchClientAndIssuer(
                            it.invoice_id,
                            linkInvoiceToDocumentClientOrIssuerQueries,
                            linkDocumentClientOrIssuerToAddressQueries,
                            documentClientOrIssuerQueries,
                            documentClientOrIssuerAddressQueries
                        ),
                        fetchTag(it.invoice_id)
                    )
                }
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
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
                            val clientAndIssuer = fetchClientAndIssuer(
                                document.invoice_id,
                                linkInvoiceToDocumentClientOrIssuerQueries,
                                linkDocumentClientOrIssuerToAddressQueries,
                                documentClientOrIssuerQueries,
                                documentClientOrIssuerAddressQueries
                            )
                            val tag = fetchTag(document.invoice_id)

                            document.transformIntoEditableInvoice(
                                products,
                                clientAndIssuer,
                                tag
                            )
                        }
                }
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    private fun fetchDocumentProducts(id: Long): MutableList<DocumentProductState>? {
        try {
            val listOfIds =
                linkInvoiceToDocumentProductQueries.getDocumentProductsLinkedToInvoice(id)
                    .executeAsList()
            return if (listOfIds.isNotEmpty()) {
                listOfIds.map {
                    val additionalInfo = linkInvoiceDocumentProductToDeliveryNoteQueries
                        .getInfoLinkedToDocumentProduct(it.document_product_id)
                        .executeAsOneOrNull()
                    documentProductQueries.getDocumentProduct(it.document_product_id)
                        .executeAsOne()
                        .transformIntoEditableDocumentProduct(
                            additionalInfo?.delivery_date,
                            additionalInfo?.delivery_note_number
                        )
                }.toMutableList()
            } else null
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }


    private fun fetchTag(documentId: Long): DocumentTag? {
        try {
            val tagId =
                linkInvoiceToTagQueries.getInvoiceTag(documentId).executeAsOneOrNull()?.tag_id
            tagId?.let {
                invoiceTagQueries.getTag(it).executeAsOneOrNull()?.let { tagName ->
                    val tag: DocumentTag = enumValueOf(tagName)
                    return tag
                }
            }
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    private fun Invoice.transformIntoEditableInvoice(
        documentProducts: MutableList<DocumentProductState>? = null,
        documentClientAndIssuer: List<ClientOrIssuerState>? = null,
        documentTag: DocumentTag? = null,
    ): InvoiceState {
        this.let {
            return InvoiceState(
                documentId = it.invoice_id.toInt(),
                documentTag = documentTag ?: DocumentTag.DRAFT,
                documentNumber = TextFieldValue(text = it.number ?: ""),
                documentDate = it.issuing_date ?: "",
                reference = it.reference?.let { TextFieldValue(text = it) },
                freeField = it.free_field?.let { TextFieldValue(text = it) },
                documentIssuer = documentClientAndIssuer?.firstOrNull { it.type == ClientOrIssuerType.DOCUMENT_ISSUER },
                documentClient = documentClientAndIssuer?.firstOrNull { it.type == ClientOrIssuerType.DOCUMENT_CLIENT },
                documentProducts = documentProducts,
                documentPrices = documentProducts?.let { calculateDocumentPrices(it) },
                currency = TextFieldValue(Strings.get(R.string.currency)),
                dueDate = it.due_date ?: "",
                paymentStatus = it.payment_status.toInt(),
                footerText = TextFieldValue(text = it.footer ?: ""),
                createdDate = it.created_at
            )
        }
    }

    override suspend fun convertDeliveryNotesToInvoice(deliveryNotes: List<DeliveryNoteState>) {
        withContext(Dispatchers.IO) {
            val docNumber = getLastDocumentNumber()?.let {
                incrementDocumentNumber(it)
            } ?: Strings.get(R.string.invoice_default_number)

            try {
                saveInfoInInvoiceTable(
                    InvoiceState(
                        documentNumber = TextFieldValue(docNumber),
                        reference = deliveryNotes.firstOrNull { it.reference != null }?.reference,
                        freeField = deliveryNotes.firstOrNull { it.freeField != null }?.freeField,
                        documentIssuer = deliveryNotes.firstOrNull { it.documentIssuer != null }?.documentIssuer,
                        documentClient = deliveryNotes.firstOrNull { it.documentClient != null }?.documentClient,
                        footerText = TextFieldValue(getExistingFooter() ?: "")
                    )
                )
                // We want to save tag only once for the invoice
                val deliveryNote = deliveryNotes.first().copy(documentTag = DocumentTag.DRAFT)
                saveTag(deliveryNote)

                // We want to save other info for each delivery note
                deliveryNotes.forEach {
                    saveInfoInOtherTables(
                        it
                    )
                }
            } catch (e: Exception) {
                //Log.e(ContentValues.TAG, "Error: ${e.message}")
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
                    reference = document.reference?.text,
                    free_field = document.freeField?.text,
                    currency = document.currency.text,
                    due_date = document.dueDate,
                    payment_status = document.paymentStatus.toLong(),
                    footer = document.footerText.text,
                    updated_at = getDateFormatter(pattern = "yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().time)
                )
                // Update tag if payment is late (due date expired)
                if (isPaymentLate(document.dueDate)) {
                    linkDocumentToDocumentTag(
                        document.documentId?.toLong() ?: 0,
                        initialTag = document.documentTag,
                        newTag = DocumentTag.LATE,
                        updateCase = TagUpdateOrCreationCase.DUE_DATE_EXPIRED
                    )
                }

            } catch (e: Exception) {
                //Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun duplicate(documents: List<InvoiceState>) {
        withContext(Dispatchers.IO) {
            try {
                documents.forEach {
                    val docNumber = getLastDocumentNumber()?.let {
                        incrementDocumentNumber(it)
                    } ?: Strings.get(R.string.invoice_default_number)
                    val invoice = it
                    invoice.documentNumber = TextFieldValue(docNumber)
                    invoice.documentTag = DocumentTag.DRAFT
                    invoice.paymentStatus = 0

                    saveInfoInInvoiceTable(invoice)
                    saveTag(invoice)
                    saveInfoInOtherTables(invoice)
                }
            } catch (e: Exception) {
                //Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun setTag(
        documents: List<InvoiceState>,
        tag: DocumentTag,
        tagUpdateCase: TagUpdateOrCreationCase,
    ) {
        withContext(Dispatchers.IO) {
            try {
                documents.forEach { invoice ->
                    invoice.documentId?.toLong()?.let { invoiceId ->
                        linkDocumentToDocumentTag(
                            invoiceId,
                            initialTag = invoice.documentTag,
                            newTag = tag,
                            tagUpdateCase
                        )
                    }
                }
            } catch (e: Exception) {
                //Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun markAsPaid(documents: List<InvoiceState>, tag: DocumentTag) {
        try {
            documents.forEach {
                it.documentId?.toLong()?.let {
                    invoiceQueries.updatePaymentStatus(
                        invoice_id = it,
                        payment_status = if (tag == DocumentTag.PAID) 2 else 0,
                        updated_at = getDateFormatter(pattern = "yyyy-MM-dd HH:mm:ss").format(
                            Calendar.getInstance().time
                        )
                    )
                }
            }
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
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
                saveDocumentProductInDbAndLink(
                    documentProductQueries,
                    linkInvoiceToDocumentProductQueries,
                    linkInvoiceDocumentProductToDeliveryNoteQueries,
                    documentProduct,
                    documentId,
                    deliveryNoteDate,
                    deliveryNoteNumber
                )

            } catch (e: Exception) {
                //Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
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
                    linkInvoiceToDocumentClientOrIssuerQueries,
                    documentClientOrIssuer,
                    documentId
                )
            } catch (e: Exception) {
                //Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }


    override suspend fun delete(documents: List<InvoiceState>) {
        withContext(Dispatchers.IO) {
            try {
                documents.filter { it.documentId != null }.forEach { document ->
                    document.documentProducts?.mapNotNull { it.id }?.forEach {
                        documentProductQueries.deleteDocumentProduct(it.toLong())
                        linkInvoiceDocumentProductToDeliveryNoteQueries.deleteInfoLinkedToDocumentProduct(
                            it.toLong()
                        )
                    }
                    invoiceQueries.delete(id = document.documentId!!.toLong())
                    linkInvoiceToDocumentProductQueries.deleteAllProductsLinkedToInvoice(
                        document.documentId!!.toLong()
                    )
                    linkInvoiceToDocumentClientOrIssuerQueries.deleteAllDocumentClientOrIssuerLinkedToInvoice(
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
                    document.documentClient?.addresses?.mapNotNull { it.id }?.forEach {
                        documentClientOrIssuerAddressQueries.delete(it.toLong())
                        linkDocumentClientOrIssuerToAddressQueries.delete(it.toLong())
                    }
                    document.documentProducts?.filter { it.id != null }?.let {
                        it.forEach { documentProduct ->
                            deleteDocumentProduct(
                                document.documentId!!.toLong(),
                                documentProduct.id!!.toLong()
                            )
                        }
                    }
                    document.documentId?.let {
                        deleteTag(it)
                    }
                }
            } catch (e: Exception) {
                //Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun deleteDocumentProduct(documentId: Long, documentProductId: Long) {
        try {
            return withContext(Dispatchers.IO) {
                linkInvoiceToDocumentProductQueries.deleteProductLinkedToInvoice(
                    documentId,
                    documentProductId
                )
                linkInvoiceDocumentProductToDeliveryNoteQueries.deleteInfoLinkedToDocumentProduct(
                    documentProductId
                )
            }
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

    override suspend fun deleteTag(invoiceId: Int) {
        try {
            return withContext(Dispatchers.IO) {
                linkInvoiceToTagQueries.delete(
                    invoiceId.toLong()
                )
            }
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

    override suspend fun deleteDocumentClientOrIssuer(
        documentId: Long,
        type: ClientOrIssuerType,
    ) {
        try {
            return withContext(Dispatchers.IO) {
                // As it was saved automatically, we have to go fetch
                // the id now

                fetchClientAndIssuer(
                    documentId,
                    linkInvoiceToDocumentClientOrIssuerQueries,
                    linkDocumentClientOrIssuerToAddressQueries,
                    documentClientOrIssuerQueries,
                    documentClientOrIssuerAddressQueries
                )?.firstOrNull { it.type == type }
                    ?.let { documentClientOrIssuer ->
                        documentClientOrIssuer.id?.let {
                            linkInvoiceToDocumentClientOrIssuerQueries.deleteDocumentClientOrIssuerLinkedToInvoice(
                                it.toLong()
                            )
                            documentClientOrIssuerQueries.delete(it.toLong())
                            linkDocumentClientOrIssuerToAddressQueries.deleteWithClientId(it.toLong())
                            documentClientOrIssuer.addresses?.filter { it.id != null }?.forEach {
                                documentClientOrIssuerAddressQueries.delete(it.id!!.toLong())
                            }
                        }
                    }
            }
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

    private suspend fun linkDocumentToDocumentTag(
        documentId: Long,
        initialTag: DocumentTag? = null,
        newTag: DocumentTag,
        updateCase: TagUpdateOrCreationCase,
    ) {
        try {
            withContext(Dispatchers.IO) {
                invoiceTagQueries.getTagId(newTag.name).executeAsOneOrNull()?.let {
                    if (updateCase == TagUpdateOrCreationCase.UPDATED_BY_USER ||
                        updateCase == TagUpdateOrCreationCase.AUTOMATICALLY_CANCELLED ||
                        (updateCase == TagUpdateOrCreationCase.DUE_DATE_EXPIRED
                                && (initialTag == DocumentTag.DRAFT
                                || initialTag == DocumentTag.SENT))
                    ) {
                        linkInvoiceToTagQueries.updateInvoiceTag(
                            invoice_id = documentId,
                            tag_id = it
                        )
                    } else if (updateCase == TagUpdateOrCreationCase.TAG_CREATION) {
                        linkInvoiceToTagQueries.saveInvoiceTag(
                            id = null,
                            invoice_id = documentId,
                            tag_id = it
                        )
                    } else {}
                }
            }
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

    private fun linkDocumentClientOrIssuerToDocument(
        documentId: Long,
        documentClientOrIssuerId: Long,
    ) {
        try {
            linkInvoiceToDocumentClientOrIssuerQueries.saveDocumentClientOrIssuerLinkedToInvoice(
                id = null,
                invoice_id = documentId,
                document_client_or_issuer_id = documentClientOrIssuerId
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

    private fun getExistingFooter(): String? {
        var footer: String? = null
        try {
            footer = invoiceQueries.getLastInsertedInvoiceFooter().executeAsOneOrNull()?.footer
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return footer
    }

    private fun saveInfoInInvoiceTable(document: InvoiceState) {
        try {
            invoiceQueries.save(
                invoice_id = null,
                number = document.documentNumber.text,
                issuing_date = document.documentDate,
                reference = document.reference?.text,
                free_field = document.freeField?.text,
                currency = document.currency.text,
                due_date = document.dueDate,
                payment_status = document.paymentStatus.toLong(),
                footer = document.footerText.text
            )
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

    private suspend fun saveTag(document: DocumentState) {
        try {
            invoiceQueries.getLastInsertedRowId().executeAsOneOrNull()?.let { id ->
                if (document is InvoiceState && isPaymentLate(document.dueDate)) {
                    document.documentTag = DocumentTag.CANCELLED
                }

                // Link tag
                linkDocumentToDocumentTag(
                    id,
                    newTag = document.documentTag,
                    updateCase = TagUpdateOrCreationCase.TAG_CREATION
                )
            }
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

    private suspend fun saveInfoInOtherTables(document: DocumentState) {
        try {
            invoiceQueries.getLastInsertedRowId().executeAsOneOrNull()?.let { id ->
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
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

    private fun isPaymentLate(dueDate: String): Boolean {
        val formatter = getDateFormatter()
        val dueDate = formatter.parse(dueDate)?.time
        val currentDate = java.util.Date().time
        val isLatePayment = dueDate != null && dueDate < currentDate
        return isLatePayment
    }
}

fun incrementDocumentNumber(docNumber: String): String {
    val numberToIncrement = docNumber.takeLastWhile { it.isDigit() }
    if (numberToIncrement.isNotEmpty()) {
        val firstPartOfDocNumber = docNumber.substringBeforeLast(numberToIncrement)
        return firstPartOfDocNumber + (numberToIncrement.toInt() + 1).toString().padStart(3, '0')
    } else return docNumber
}

enum class TagUpdateOrCreationCase {
    TAG_CREATION, // new invoice, delivery note conversion, duplication
    UPDATED_BY_USER,
    AUTOMATICALLY_CANCELLED, //after creating credit note or corrected invoice
    DUE_DATE_EXPIRED
}

fun saveDocumentProductInDbAndLink(
    documentProductQueries: DocumentProductQueries,
    linkToDocumentProductQueries: Any,
    linkToDeliveryNotesQueries: Any,
    documentProduct: DocumentProductState,
    documentId: Long?,
    deliveryNoteDate: String?,
    deliveryNoteNumber: String?,
): Int? {
    var documentProductId: Int? = null
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
                documentProductId = id

                linkDocumentProductToDocument(
                    linkToDocumentProductQueries,
                    documentId,
                    id.toLong()
                )
                if (!deliveryNoteDate.isNullOrEmpty()) {
                    linkDocumentProductToDeliveryNoteInfo(
                        linkToDeliveryNotesQueries,
                        id.toLong(),
                        deliveryNoteNumber,
                        deliveryNoteDate,
                    )
                }
            }
    }
    return documentProductId
}

fun linkDocumentProductToDocument(
    linkQueries: Any,
    documentId: Long,
    documentProductId: Long,
) {
    try {
        if (linkQueries is LinkCreditNoteToDocumentProductQueries) {
            linkQueries.saveProductLinkedToCreditNote(
                id = null,
                credit_note_id = documentId,
                document_product_id = documentProductId
            )
        } else if (linkQueries is LinkDeliveryNoteToDocumentProductQueries) {
            linkQueries.saveProductLinkedToDeliveryNote(
                id = null,
                delivery_note_id = documentId,
                document_product_id = documentProductId
            )
        } else if (linkQueries is LinkInvoiceToDocumentProductQueries) {
            linkQueries.saveProductLinkedToInvoice(
                id = null,
                invoice_id = documentId,
                document_product_id = documentProductId
            )
        }
    } catch (e: Exception) {
        //Log.e(ContentValues.TAG, "Error: ${e.message}")
    }
}

fun linkDocumentProductToDeliveryNoteInfo(
    linkToDeliveryNotesQueries: Any,
    documentProductId: Long,
    deliveryNoteNumber: String?,
    deliveryNoteDate: String,
) {
    try {
        if (linkToDeliveryNotesQueries is LinkCreditNoteDocumentProductToDeliveryNoteQueries) {
            linkToDeliveryNotesQueries.saveInfoLinkedToDocumentProduct(
                document_product_id = documentProductId,
                delivery_note_number = deliveryNoteNumber,
                delivery_note_date = deliveryNoteDate
            )
        } else if (linkToDeliveryNotesQueries is LinkInvoiceDocumentProductToDeliveryNoteQueries) {
            linkToDeliveryNotesQueries.saveInfoLinkedToDocumentProduct(
                document_product_id = documentProductId,
                delivery_note_number = deliveryNoteNumber,
                delivery_date = deliveryNoteDate
            )
        }
    } catch (e: Exception) {
        //Log.e(ContentValues.TAG, "Error: ${e.message}")
    }
}

suspend fun saveDocumentClientOrIssuerInDbAndLink(
    documentClientOrIssuerQueries: DocumentClientOrIssuerQueries,
    documentClientOrIssuerAddressQueries: DocumentClientOrIssuerAddressQueries,
    linkDocumentClientOrIssuerToAddressQueries: LinkDocumentClientOrIssuerToAddressQueries,
    linkQueries: Any,
    documentClientOrIssuer: ClientOrIssuerState,
    documentId: Long?,
) {
    saveDocumentClientOrIssuer(
        documentClientOrIssuerQueries,
        documentClientOrIssuerAddressQueries,
        linkDocumentClientOrIssuerToAddressQueries,
        documentClientOrIssuer
    )

    documentId?.let { documentId ->
        documentClientOrIssuerQueries.getLastInsertedClientOrIssuerId()
            .executeAsOneOrNull()?.toInt()
            ?.let { id ->
                linkDocumentClientOrIssuerToDocument(
                    linkQueries,
                    documentId,
                    id.toLong()
                )
            }
    }
}

fun linkDocumentClientOrIssuerToDocument(
    linkQueries: Any,
    documentId: Long,
    documentClientOrIssuerId: Long,
) {
    try {
        when (linkQueries) {
            is LinkInvoiceToDocumentClientOrIssuerQueries -> {
                linkQueries.saveDocumentClientOrIssuerLinkedToInvoice(
                    id = null,
                    invoice_id = documentId,
                    document_client_or_issuer_id = documentClientOrIssuerId
                )
            }

            is LinkCreditNoteToDocumentClientOrIssuerQueries -> {
                linkQueries.saveDocumentClientOrIssuerLinkedToCreditNote(
                    id = null,
                    credit_note_id = documentId,
                    document_client_or_issuer_id = documentClientOrIssuerId
                )
            }

            is LinkDeliveryNoteToDocumentClientOrIssuerQueries -> {
                linkQueries.saveDocumentClientOrIssuerLinkedToDeliveryNote(
                    id = null,
                    delivery_note_id = documentId,
                    document_client_or_issuer_id = documentClientOrIssuerId
                )
            }
        }
    } catch (e: Exception) {
        //Log.e(ContentValues.TAG, "Error: ${e.message}")
    }
}

private suspend fun saveDocumentClientOrIssuer(
    documentClientOrIssuerQueries: DocumentClientOrIssuerQueries,
    documentClientOrIssuerAddressQueries: DocumentClientOrIssuerAddressQueries,
    linkDocumentClientOrIssuerToAddressQueries: LinkDocumentClientOrIssuerToAddressQueries,
    documentClientOrIssuer: ClientOrIssuerState,
) {
    saveInfoInDocumentClientOrIssuerTable(documentClientOrIssuerQueries, documentClientOrIssuer)
    documentClientOrIssuerQueries.getLastInsertedRowId().executeAsOneOrNull()
        ?.let { documentClientId ->
            saveInfoInDocumentClientOrIssuerAddressTables(
                documentClientOrIssuerAddressQueries,
                linkDocumentClientOrIssuerToAddressQueries,
                documentClientId,
                documentClientOrIssuer.addresses
            )
        }
}

private suspend fun saveInfoInDocumentClientOrIssuerTable(
    documentClientOrIssuerQueries: DocumentClientOrIssuerQueries,
    documentClientOrIssuer: ClientOrIssuerState,
) {
    withContext(Dispatchers.IO) {
        try {
            documentClientOrIssuerQueries.save(
                id = null,
                type = if (documentClientOrIssuer.type == ClientOrIssuerType.CLIENT ||
                    documentClientOrIssuer.type == ClientOrIssuerType.DOCUMENT_CLIENT
                ) ClientOrIssuerType.CLIENT.name.lowercase()
                else ClientOrIssuerType.ISSUER.name.lowercase(),
                documentClientOrIssuer.firstName?.text,
                documentClientOrIssuer.name.text,
                documentClientOrIssuer.phone?.text,
                documentClientOrIssuer.email?.text,
                documentClientOrIssuer.notes?.text,
                documentClientOrIssuer.companyId1Label?.text,
                documentClientOrIssuer.companyId1Number?.text,
                documentClientOrIssuer.companyId2Label?.text,
                documentClientOrIssuer.companyId2Number?.text,
                documentClientOrIssuer.companyId3Label?.text,
                documentClientOrIssuer.companyId3Number?.text,
            )
        } catch (cause: Throwable) {
        }
    }
}

private suspend fun saveInfoInDocumentClientOrIssuerAddressTables(
    documentClientOrIssuerAddressQueries: DocumentClientOrIssuerAddressQueries,
    linkDocumentClientOrIssuerToAddressQueries: LinkDocumentClientOrIssuerToAddressQueries,
    documentClientOrIssuerId: Long,
    addresses: List<AddressState>?,
) {
    return withContext(Dispatchers.IO) {
        try {
            addresses?.forEach { address ->
                documentClientOrIssuerAddressQueries.save(
                    id = null,
                    address_title = address.addressTitle?.text,
                    address_line_1 = address.addressLine1?.text,
                    address_line_2 = address.addressLine2?.text,
                    zip_code = address.zipCode?.text,
                    city = address.city?.text,
                )

                documentClientOrIssuerAddressQueries.getLastInsertedRowId().executeAsOneOrNull()
                    ?.let { addressId ->
                        linkClientOrIssuerToAddress(
                            linkDocumentClientOrIssuerToAddressQueries,
                            documentClientOrIssuerId,
                            addressId
                        )
                    }
            }
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }
}

fun DocumentClientOrIssuer.transformIntoEditable(
    addresses: List<AddressState>? = null,
): ClientOrIssuerState {
    val documentClientOrIssuer = this

    return ClientOrIssuerState(
        id = documentClientOrIssuer.id.toInt(),
        type = if (documentClientOrIssuer.type == ClientOrIssuerType.CLIENT.name.lowercase())
            ClientOrIssuerType.DOCUMENT_CLIENT
        else ClientOrIssuerType.DOCUMENT_ISSUER,
        firstName = documentClientOrIssuer.first_name?.let { TextFieldValue(text = it) },
        addresses = addresses,
        name = TextFieldValue(text = documentClientOrIssuer.name),
        phone = documentClientOrIssuer.phone?.let { TextFieldValue(text = it) },
        email = documentClientOrIssuer.email?.let { TextFieldValue(text = it) },
        notes = documentClientOrIssuer.notes?.let { TextFieldValue(text = it) },
        companyId1Label = documentClientOrIssuer.company_id1_number?.let {
            documentClientOrIssuer.company_id1_label?.let {
                TextFieldValue(
                    text = it
                )
            }
        },
        companyId1Number = documentClientOrIssuer.company_id1_number?.let { TextFieldValue(text = it) },
        companyId2Label = documentClientOrIssuer.company_id2_number?.let {
            documentClientOrIssuer.company_id2_label?.let {
                TextFieldValue(
                    text = it
                )
            }
        },
        companyId2Number = documentClientOrIssuer.company_id2_number?.let { TextFieldValue(text = it) },
        companyId3Label = documentClientOrIssuer.company_id3_number?.let {
            documentClientOrIssuer.company_id3_label?.let {
                TextFieldValue(
                    text = it
                )
            }
        },
        companyId3Number = documentClientOrIssuer.company_id3_number?.let { TextFieldValue(text = it) },
    )
}

fun fetchClientAndIssuer(
    documentId: Long,
    linkQueries: Any,
    linkAddressQueries: LinkDocumentClientOrIssuerToAddressQueries,
    documentClientOrIssuerQueries: DocumentClientOrIssuerQueries,
    documentClientOrIssuerAddressQueries: DocumentClientOrIssuerAddressQueries,
): List<ClientOrIssuerState>? {
    try {
        val listOfIds: List<Long> = if (linkQueries is LinkInvoiceToDocumentClientOrIssuerQueries) {
            linkQueries.getDocumentClientOrIssuerLinkedToInvoice(
                documentId
            ).executeAsList().map { it.document_client_or_issuer_id }
        } else if (linkQueries is LinkCreditNoteToDocumentClientOrIssuerQueries) {
            linkQueries.getDocumentClientOrIssuerLinkedToCreditNote(
                documentId
            ).executeAsList().map { it.document_client_or_issuer_id }
        } else if (linkQueries is LinkDeliveryNoteToDocumentClientOrIssuerQueries)
            linkQueries.getDocumentClientOrIssuerLinkedToDeliveryNote(
                documentId
            ).executeAsList().map { it.document_client_or_issuer_id }
        else emptyList()


        val clientAndIssuer: MutableList<ClientOrIssuerState> = mutableListOf()
        listOfIds.forEach {
            val documentClientOrIssuer = documentClientOrIssuerQueries.get(it)
                .executeAsOneOrNull()?.let {
                    it.transformIntoEditable(
                        fetchDocumentClientOrIssuerAddresses(
                            it.id,
                            linkAddressQueries,
                            documentClientOrIssuerAddressQueries
                        )?.toMutableList()
                    )
                }
            documentClientOrIssuer?.let {
                clientAndIssuer.add(it)
            }
        }
        return if (clientAndIssuer.isNotEmpty())
            clientAndIssuer.toList()
        else
            null

    } catch (e: Exception) {
        //Log.e(ContentValues.TAG, "Error: ${e.message}")
    }
    return null
}

fun fetchDocumentClientOrIssuerAddresses(
    id: Long,
    linkQueries: LinkDocumentClientOrIssuerToAddressQueries,
    documentClientOrIssuerAddressQueries: DocumentClientOrIssuerAddressQueries,
): MutableList<AddressState>? {
    try {
        val listOfIds: List<Long> = linkQueries.get(id).executeAsList().map { it.address_id }

        return if (listOfIds.isNotEmpty()) {
            listOfIds.map {
                documentClientOrIssuerAddressQueries.get(it)
                    .executeAsOne()
                    .transformIntoEditable()
            }.toMutableList()
        } else null
    } catch (e: Exception) {
        //Log.e(ContentValues.TAG, "Error: ${e.message}")
    }
    return null
}
