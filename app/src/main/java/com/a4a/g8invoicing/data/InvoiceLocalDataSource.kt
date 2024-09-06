package com.a4a.g8invoicing.data

import android.content.ContentValues
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.screens.shared.getDateFormatter
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
import java.util.Calendar

class InvoiceLocalDataSource(
    db: Database,
) : InvoiceLocalDataSourceInterface {
    private val invoiceQueries = db.invoiceQueries
    private val invoiceTagQueries = db.invoiceTagQueries
    private val documentClientOrIssuerQueries = db.documentClientOrIssuerQueries
    private val documentProductQueries = db.documentProductQueries
    private val creditNoteQueries = db.creditNoteQueries
    private val linkInvoiceToDocumentProductQueries = db.linkInvoiceToDocumentProductQueries
    private val linkInvoiceToTagQueries = db.linkInvoiceToTagQueries
    private val linkInvoiceDocumentProductToDeliveryNoteQueries =
        db.linkInvoiceDocumentProductToDeliveryNoteQueries
    private val linkInvoiceToDocumentClientOrIssuerQueries =
        db.linkInvoiceToDocumentClientOrIssuerQueries

    override suspend fun createNew(): Long? {
        var newInvoiceId: Long? = null
        val docNumber = getLastDocumentNumber()?.let {
            incrementDocumentNumber(it)
        } ?: Strings.get(R.string.invoice_default_number)

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
                        fetchClientAndIssuer(it.invoice_id),
                        fetchTag(it.invoice_id)
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
                linkInvoiceToDocumentClientOrIssuerQueries.getDocumentClientOrIssuerLinkedToInvoice(
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
            val tagId =
                linkInvoiceToTagQueries.getInvoiceTag(documentId).executeAsOneOrNull()?.tag_id
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
                Log.e(ContentValues.TAG, "Error: ${e.message}")
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

                    saveInfoInInvoiceTable(invoice)
                    saveInfoInOtherTables(invoice)
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
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
                            tagUpdateCase)
                    }
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
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
            Log.e(ContentValues.TAG, "Error: ${e.message}")
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
                saveDocumentClientOrIssuerInDbAndLink(
                    documentClientOrIssuerQueries,
                    linkInvoiceToDocumentClientOrIssuerQueries,
                    documentClientOrIssuer,
                    documentId
                )
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
                linkInvoiceToDocumentProductQueries.deleteProductLinkedToInvoice(
                    documentId,
                    documentProductId
                )
                linkInvoiceDocumentProductToDeliveryNoteQueries.deleteInfoLinkedToDocumentProduct(
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
                    linkInvoiceToDocumentClientOrIssuerQueries.deleteDocumentClientOrIssuerLinkedToInvoice(
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

    private suspend fun linkDocumentToDocumentTag(
        documentId: Long,
        initialTag: DocumentTag,
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
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
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
                reference = document.reference?.text,
                free_field = document.freeField?.text,
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
                if (document is InvoiceState && isPaymentLate(document.dueDate)) {
                    document.documentTag = DocumentTag.CANCELLED
                }

                // Link tag
                linkDocumentToDocumentTag(
                    id,
                    initialTag = document.documentTag,
                    newTag = document.documentTag,
                    TagUpdateOrCreationCase.TAG_CREATION
                )

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