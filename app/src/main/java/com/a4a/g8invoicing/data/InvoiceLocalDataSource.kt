package com.a4a.g8invoicing.data

import android.util.Log.e
import androidx.compose.ui.text.input.TextFieldValue
import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.screens.shared.getDateFormatter
import com.a4a.g8invoicing.ui.shared.DocumentType
import com.a4a.g8invoicing.ui.states.AddressState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType
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


    // --- createNew ---
    // Called from ViewModel
    // This function performs DB operations, so it needs Dispatchers.IO.
    override suspend fun createNew(): Long? {
        return withContext(Dispatchers.IO) {
            val newInvoiceState = InvoiceState(
                documentNumber = TextFieldValue(
                    getLastDocumentNumber()?.let { incrementDocumentNumber(it) }
                        ?: Strings.get(R.string.invoice_default_number)
                ),
                documentIssuer = getExistingIssuer()?.transformIntoEditable(), // DB call
                footerText = TextFieldValue(
                    getExistingFooter() ?: Strings.get(R.string.document_default_footer) // DB call
                )
            )

            saveInfoInInvoiceTable(newInvoiceState)

            val newInvoiceId = invoiceQueries.getLastInsertedRowId().executeAsOneOrNull()

            newInvoiceId?.let { id ->
                // Pass the obtained ID explicitly to helper functions
                saveTag(id, newInvoiceState) // saveTag is suspend
                saveInfoInOtherTables(id, newInvoiceState) // saveInfoInOtherTables is suspend
            }
            newInvoiceId // Return the ID
        }
    }

    // --- Synchronous private helpers for createNew (called from Dispatchers.IO context) ---
    private fun getLastDocumentNumber(): String? {
        try {
            return invoiceQueries.getLastInvoiceNumber().executeAsOneOrNull()?.number
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
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

    // --- fetch ---
    // Correctly uses withContext(Dispatchers.IO).
    // Internal fetch* helpers are synchronous and will run on this IO context.
    override suspend fun fetch(id: Long): InvoiceState? {
        return withContext(Dispatchers.IO) {
            try {
                invoiceQueries.get(id).executeAsOneOrNull()
                    ?.let {
                        it.transformIntoEditableInvoice(
                            fetchDocumentProducts(it.invoice_id),// Synchronous, runs on this IO context
                            fetchClientAndIssuer( // Synchronous, runs on this IO context
                                it.invoice_id,
                                linkInvoiceToDocumentClientOrIssuerQueries,
                                linkDocumentClientOrIssuerToAddressQueries,
                                documentClientOrIssuerQueries,
                                documentClientOrIssuerAddressQueries
                            ),
                            fetchTag(it.invoice_id)  // Synchronous, runs on this IO context
                        )
                    }
            } catch (e: Exception) {
                //Log.e("InvoiceDS", "Error fetch id $id: ${e.message}")
                null
            }
        }
    }


    // --- fetchAll (returning Flow) ---
    // Flow construction is fine.
    // The .map block executes on the collector's context.
    // This Flow is collected on Dispatchers.IO (e.g., using .flowOn(Dispatchers.IO) in ViewModel)
    // because internal fetch* helpers are synchronous DB calls.
    override fun fetchAll(): Flow<List<InvoiceState>>? {
        try {
            return invoiceQueries.getAll()
                .asFlow()
                .map { // This .map runs on the collector's dispatcher
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
            //Log.e("InvoiceDS", "Error fetchAll: ${e.message}")
        }
        return null
    }

    // --- fetchDocumentProducts ---
    // Synchronous private helper, performs DB IO.
    // Must be called from a Dispatchers.IO context.
    private fun fetchDocumentProducts(id: Long): MutableList<DocumentProductState>? {
        try {
            val listOfIds =
                linkInvoiceToDocumentProductQueries.getDocumentProductsLinkedToInvoice(id)
                    .executeAsList() // DB call
            return if (listOfIds.isNotEmpty()) {
                listOfIds.map {
                    val additionalInfo = linkInvoiceDocumentProductToDeliveryNoteQueries
                        .getInfoLinkedToDocumentProduct(it.document_product_id)
                        .executeAsOneOrNull()// DB call
                    documentProductQueries.getDocumentProduct(it.document_product_id)
                        .executeAsOne()// DB call
                        .transformIntoEditableDocumentProduct(
                            additionalInfo?.delivery_date,
                            additionalInfo?.delivery_note_number,
                            sortOrder = it.sort_order?.toInt() // Passer le sort_order de la table de liaison
                        )
                }.toMutableList()
            } else null
        } catch (e: Exception) {
            //Log.e("InvoiceDS", "Error fetchDocumentProducts for id $id: ${e.message}")
        }
        return null
    }

    // --- fetchTag ---
    // Synchronous private helper, performs DB IO.
    // Called from a Dispatchers.IO context.
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
            //Log.e("InvoiceDS", "Error fetchTag for documentId $documentId: ${e.message}")
        }
        return null
    }

    // --- transformIntoEditableInvoice ---
    // Pure transformation function, no IO, no suspend/withContext needed.
    private fun Invoice.transformIntoEditableInvoice(
        documentProducts: MutableList<DocumentProductState>? = null,
        documentClientAndIssuer: List<ClientOrIssuerState>? = null,
        documentTag: DocumentTag? = null,
    ): InvoiceState {
        return InvoiceState(
            documentId = this.invoice_id.toInt(),
            documentTag = documentTag ?: DocumentTag.DRAFT,
            documentNumber = TextFieldValue(text = this.number ?: ""),
            documentDate = this.issuing_date ?: "",
            reference = this.reference?.let { TextFieldValue(text = it) },
            freeField = this.free_field?.let { TextFieldValue(text = it) },
            documentIssuer = documentClientAndIssuer?.firstOrNull { it.type == ClientOrIssuerType.DOCUMENT_ISSUER },
            documentClient = documentClientAndIssuer?.firstOrNull { it.type == ClientOrIssuerType.DOCUMENT_CLIENT },
            documentProducts = documentProducts?.sortedBy { it.sortOrder },
            documentTotalPrices = documentProducts?.let { calculateDocumentPrices(it) },
            currency = TextFieldValue(Strings.get(R.string.currency)),
            dueDate = this.due_date ?: "",
            paymentStatus = this.payment_status.toInt(),
            footerText = TextFieldValue(text = this.footer ?: ""),
            createdDate = this.created_at
        )
    }


    // --- convertDeliveryNotesToInvoice ---
    // Uses withContext(Dispatchers.IO).
    override suspend fun convertDeliveryNotesToInvoice(deliveryNotes: List<DeliveryNoteState>) {
        withContext(Dispatchers.IO) {
            val docNumber = getLastDocumentNumber()?.let {
                incrementDocumentNumber(it)
            } ?: Strings.get(R.string.invoice_default_number)


            try {
                val newInvoiceState = InvoiceState(
                    documentNumber = TextFieldValue(docNumber),
                    reference = deliveryNotes.firstOrNull { it.reference != null }?.reference,
                    freeField = deliveryNotes.firstOrNull { it.freeField != null }?.freeField,
                    documentIssuer = deliveryNotes.firstOrNull { it.documentIssuer != null }?.documentIssuer,
                    documentClient = deliveryNotes.firstOrNull { it.documentClient != null }?.documentClient,
                    footerText = TextFieldValue(getExistingFooter() ?: "") // DB call
                )
                saveInfoInInvoiceTable(newInvoiceState) // DB call

                val newInvoiceId = invoiceQueries.getLastInsertedRowId()
                    .executeAsOneOrNull() // Get ID after main insert
                newInvoiceId?.let { id ->
                    // For simplicity, we take the tag of the first Delivery Note
                    val firstDeliveryNoteForTag = deliveryNotes.first()
                        .copy(documentTag = DocumentTag.DRAFT) // Or use newInvoiceState
                    saveTag(id, firstDeliveryNoteForTag) // saveTag is suspend

                    // saveInfoInOtherTables iterates deliveryNotes and link their products/clients to the new invoiceId
                    deliveryNotes.forEach { deliveryNote ->
                        saveInfoInOtherTables(id, deliveryNote)
                    }
                }
            } catch (e: Exception) {
                //Log.e("InvoiceDS", "Error convertDeliveryNotes: ${e.message}")
            }
        }
    }

    // --- update ---
    // Uses withContext(Dispatchers.IO).
    override suspend fun update(document: InvoiceState) {
        return withContext(Dispatchers.IO) {
            try {
                invoiceQueries.update( // DB Call
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
                if (isPaymentLate(document.dueDate)) { // isPaymentLate is pure
                    linkDocumentToDocumentTag( // linkDocumentToDocumentTag is suspend
                        document.documentId?.toLong() ?: 0,
                        initialTag = document.documentTag,
                        newTag = DocumentTag.LATE,
                        updateCase = TagUpdateOrCreationCase.DUE_DATE_EXPIRED
                    )
                }

            } catch (e: Exception) {
                //Log.e("InvoiceDS", "Error update: ${e.message}")
            }
        }
    }

    // --- duplicate ---
    // Correctly uses withContext(Dispatchers.IO).
    override suspend fun duplicate(documents: List<InvoiceState>) {
        withContext(Dispatchers.IO) {
            try {
                documents.forEach { originalDocument ->
                    val docNumber = getLastDocumentNumber()?.let { // DB Call
                        incrementDocumentNumber(it)
                    } ?: Strings.get(R.string.invoice_default_number)

                    val duplicatedInvoiceState = originalDocument.copy(
                        // Assuming InvoiceState is a data class
                        documentNumber = TextFieldValue(docNumber),
                        documentTag = DocumentTag.DRAFT,
                        paymentStatus = 0,
                    )

                    saveInfoInInvoiceTable(duplicatedInvoiceState) // DB Call

                    val newInvoiceId = invoiceQueries.getLastInsertedRowId()
                        .executeAsOneOrNull() // Get ID after main insert
                    newInvoiceId?.let { id ->
                        saveTag(id, duplicatedInvoiceState) // saveTag is suspend
                        saveInfoInOtherTables(
                            id,
                            originalDocument.copy(documentId = id.toInt())
                        ) // Pass new ID and original document's children context
                    }
                }
            } catch (e: Exception) {
                //Log.e("InvoiceDS", "Error duplicate: ${e.message}")
            }
        }
    }

    // --- setTag ---
    // Uses withContext(Dispatchers.IO).
    // linkDocumentToDocumentTag is called from IO and is suspend.
    override suspend fun setTag(
        documents: List<InvoiceState>,
        tag: DocumentTag,
        tagUpdateCase: TagUpdateOrCreationCase,
    ) {
        withContext(Dispatchers.IO) {
            try {
                documents.forEach { invoice ->
                    invoice.documentId?.toLong()?.let { invoiceId ->
                        linkDocumentToDocumentTag(  // linkDocumentToDocumentTag is suspend
                            invoiceId,
                            initialTag = invoice.documentTag,
                            newTag = tag,
                            tagUpdateCase
                        )
                    }
                }
            } catch (e: Exception) {
                //Log.e("InvoiceDS", "Error setTag: ${e.message}")
            }
        }
    }


    // --- markAsPaid ---
    // Added withContext(Dispatchers.IO).
    override suspend fun markAsPaid(documents: List<InvoiceState>, tag: DocumentTag) {
        withContext(Dispatchers.IO) {
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
                //Log.e("InvoiceDS", "Error markAsPaid: ${e.message}")
            }
        }
    }

    // --- saveDocumentProductInDbAndLinkToDocument ---
    // Correctly uses withContext(Dispatchers.IO) and transaction.
    // The global saveDocumentProductInDbAndLink is called from this IO context.
    override suspend fun saveDocumentProductInDbAndLinkToDocument(
        documentProduct: DocumentProductState,
        documentId: Long?, // This is the parent document ID (e.g., invoiceId)
        deliveryNoteDate: String?,
        deliveryNoteNumber: String?,
    ) {
        withContext(Dispatchers.IO) {
            documentProductQueries.transaction {
                try {
                    // This global function performs synchronous DB operations
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
                    //Log.e("InvoiceDS", "Error saveDocProdAndLink: ${e.message}")
                }
            }
        }
    }

    // --- saveDocumentClientOrIssuerInDbAndLinkToDocument ---
    // Uses withContext(Dispatchers.IO).
    // The global saveDocumentClientOrIssuerInDbAndLink is suspend and called from here.
    override suspend fun saveDocumentClientOrIssuerInDbAndLinkToDocument(
        documentClientOrIssuer: ClientOrIssuerState,
        documentId: Long?, // This is the parent document ID (e.g., invoiceId)
    ) {
        withContext(Dispatchers.IO) { // This IO context is inherited by the suspend call below
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
                //Log.e("InvoiceDS", "Error saveClientOrIssuerAndLink: ${e.message}")
            }
        }
    }

    // --- delete ---
    // Correctly uses withContext(Dispatchers.IO).
    // All internal DB operations will run on this IO context.
    override suspend fun delete(documents: List<InvoiceState>) {
        withContext(Dispatchers.IO) {
            try {
                documents.filter { it.documentId != null }.forEach { document ->
                    // Delete linked products and their specific links
                    document.documentProducts?.mapNotNull { it.id }?.forEach {
                        documentProductQueries.deleteDocumentProduct(it.toLong())
                        linkInvoiceDocumentProductToDeliveryNoteQueries.deleteInfoLinkedToDocumentProduct(
                            it.toLong() // DB call
                        )
                    }

                    // Delete linked products
                    linkInvoiceToDocumentProductQueries.deleteAllProductsLinkedToInvoice(
                        document.documentId!!.toLong()
                    )
                    // Delete linked client/issuer
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
                    // Delete client/issuer addresses
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
                    // Delete linked tag
                    document.documentId?.let { docId ->
                        deleteTag(docId.toLong())
                    }
                    // Delete the main invoice
                    invoiceQueries.delete(id = document.documentId!!.toLong())
                }
            } catch (e: Exception) {
                //Log.e("InvoiceDS", "Error delete: ${e.message}")
            }
        }
    }

    // --- deleteDocumentProduct (from an Invoice context) ---
    // Specific helper for deleting a product linked to an invoice.
    // Correctly uses withContext(Dispatchers.IO).
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
            //Log.e("InvoiceDS", "Error deleteDocumentProduct: ${e.message}")
        }
    }

    // --- deleteTag (from an Invoice context) ---
    // Uses withContext(Dispatchers.IO).
    override suspend fun deleteTag(invoiceId: Long) {
        try {
            return withContext(Dispatchers.IO) {
                linkInvoiceToTagQueries.delete(
                    invoiceId  // DB call
                )
            }
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }


    // --- deleteDocumentClientOrIssuer (from an Invoice context) ---
    // Uses withContext(Dispatchers.IO).
    override suspend fun deleteDocumentClientOrIssuer(
        documentId: Long,
        type: ClientOrIssuerType,
    ) {
        try {
            return withContext(Dispatchers.IO) {
                // Fetch the specific client/issuer linked to THIS invoice
                val clientOrIssuerToDelete =
                    fetchClientAndIssuer( // Synchronous, runs on this IO context
                        documentId,
                        linkInvoiceToDocumentClientOrIssuerQueries,
                        linkDocumentClientOrIssuerToAddressQueries,
                        documentClientOrIssuerQueries,
                        documentClientOrIssuerAddressQueries
                    )?.firstOrNull { it.type == type }


                clientOrIssuerToDelete?.id?.let { entityId ->
                    // 1. Delete the link between invoice and the client/issuer entity
                    linkInvoiceToDocumentClientOrIssuerQueries.deleteDocumentClientOrIssuerLinkedToInvoice(
                        entityId.toLong()
                    )
                    //2. Delete the client/issuer entity and its addresses
                    documentClientOrIssuerQueries.delete(entityId.toLong())
                    linkDocumentClientOrIssuerToAddressQueries.deleteWithClientId(entityId.toLong())
                    clientOrIssuerToDelete.addresses?.mapNotNull { it.id }?.forEach { addressId ->
                        documentClientOrIssuerAddressQueries.delete(addressId.toLong())
                    }
                }
            }
        } catch (e: Exception) {
            //Log.e("InvoiceDS", "Error deleteDocClientOrIssuer: ${e.message}")
        }
    }


    // --- linkDocumentToDocumentTag ---
    // Private suspend helper, uses withContext(Dispatchers.IO).
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
                    }
                }
            }
        } catch (e: Exception) {
            //Log.e("InvoiceDS", "Error linkDocToDocTag: ${e.message}")
        }
    }

    // --- saveInfoInInvoiceTable ---
    // Synchronous private helper, performs DB IO.
    // Must be called from a Dispatchers.IO context
    private fun saveInfoInInvoiceTable(document: InvoiceState) {
        try {
            invoiceQueries.save( // DB call
                invoice_id = null, // Auto-incremented by DB
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
            //Log.e("InvoiceDS", "Error saveInfoInInvoiceTable: ${e.message}")
        }
    }


    // --- saveTag ---
    // Private suspend helper. Called with an explicit invoiceId.
    // Calls linkDocumentToDocumentTag which is suspend and handles its own IO.
    private suspend fun saveTag(invoiceId: Long, document: DocumentState) {
        try {
            if (document is InvoiceState && isPaymentLate(document.dueDate)) {
                document.documentTag = DocumentTag.CANCELLED
            }

            // Link tag
            linkDocumentToDocumentTag( // This is suspend
                invoiceId,
                newTag = document.documentTag,
                updateCase = TagUpdateOrCreationCase.TAG_CREATION
            )

        } catch (e: Exception) {
            //Log.e("InvoiceDS", "Error saveTag for invoiceId $invoiceId: ${e.message}")
        }
    }

    // --- saveInfoInOtherTables ---
    // Private suspend helper. Called with an explicit parentId (invoiceId).
    // Calls other suspend functions that manage their own IO.
    private suspend fun saveInfoInOtherTables(documentId: Long, document: DocumentState) {
        try {
            // Link all products to the new parentId
            document.documentProducts?.forEach { documentProduct ->
                saveDocumentProductInDbAndLinkToDocument( // This is suspend
                    documentProduct = documentProduct,
                    documentId = documentId,
                    deliveryNoteDate = if (document is DeliveryNoteState) document.documentDate else null,
                    deliveryNoteNumber = if (document is DeliveryNoteState) document.documentNumber.text else null
                )
            }

            // Link client
            document.documentClient?.let {
                saveDocumentClientOrIssuerInDbAndLinkToDocument(
                    documentClientOrIssuer = it,
                    documentId = documentId
                )
            }

            // Link issuer
            document.documentIssuer?.let {
                saveDocumentClientOrIssuerInDbAndLinkToDocument(
                    documentClientOrIssuer = it,
                    documentId = documentId
                )
            }
        } catch (e: Exception) {
            //Log.e("InvoiceDS", "Error saveInfoInOtherTables for parentId $parentId: ${e.message}")
        }
    }

    // --- isPaymentLate ---
    // Pure utility function.
    private fun isPaymentLate(dueDate: String): Boolean {
        val formatter = getDateFormatter()
        val dueDate = formatter.parse(dueDate)?.time
        val currentDate = java.util.Date().time
        val isLatePayment = dueDate != null && dueDate < currentDate
        return isLatePayment
    }

    /**
     * Updates the sort_order for a list of document products linked to a parent document.
     */
    override suspend fun updateDocumentProductsOrderInDb(
        documentId: Long,
        orderedProducts: List<DocumentProductState>,
    ) {
        withContext(Dispatchers.IO) {
            try {
                documentProductQueries.transaction {
                    updateDocumentProductsOrderInDb(documentId, orderedProducts, linkInvoiceToDocumentProductQueries)
                }
            } catch (e: Exception) {
                // Log.e("InvoiceLocalDataSource", "Error updating document products order in DB: ${e.message}", e)
                throw e // Relance pour que le ViewModel puisse la catcher si nécessaire
            }
        }
    }

} // End of InvoiceLocalDataSource

// --- Global helper functions (outside the class) ---
// These should ideally be part of a relevant DataSource or utility class,

// --- incrementDocumentNumber ---
//  Pure utility function.
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


// This function performs synchronous DB IO.
// It must be called from a Dispatchers.IO context.
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
    documentProductQueries.saveDocumentProduct(  // DB call
        id = null,
        name = documentProduct.name.text,
        quantity = documentProduct.quantity.toDouble(),
        description = documentProduct.description?.text,
        price_without_tax = documentProduct.priceWithoutTax?.toDouble(),
        tax_rate = documentProduct.taxRate?.toDouble(),
        unit = documentProduct.unit?.text,
        product_id = documentProduct.productId?.toLong()
    )

    documentId?.let { documentId ->
        documentProductQueries.getLastInsertedRowId().executeAsOneOrNull()?.toInt()
            ?.let { id ->
                documentProductId = id

                linkDocumentProductToParentDocument(
                    linkToDocumentProductQueries,
                    documentId,
                    id.toLong(),
                    documentProduct.sortOrder
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


// Synchronous DB access. Must be called from an IO context.
fun linkDocumentProductToDeliveryNoteInfo(
    linkToDeliveryNotesQueries: Any,
    documentProductId: Long,
    deliveryNoteNumber: String?,
    deliveryNoteDate: String,
) {
    try {
        if (linkToDeliveryNotesQueries is LinkCreditNoteDocumentProductToDeliveryNoteQueries) {
            linkToDeliveryNotesQueries.saveInfoLinkedToDocumentProduct( // DB call
                document_product_id = documentProductId,
                delivery_note_number = deliveryNoteNumber,
                delivery_note_date = deliveryNoteDate
            )
        } else if (linkToDeliveryNotesQueries is LinkInvoiceDocumentProductToDeliveryNoteQueries) {
            linkToDeliveryNotesQueries.saveInfoLinkedToDocumentProduct( // DB call
                document_product_id = documentProductId,
                delivery_note_number = deliveryNoteNumber,
                delivery_date = deliveryNoteDate
            )
        } else {
            //Log.w("GlobalHelpers", "Unsupported query type for linkDocumentProductToDeliveryNoteInfo: ${linkQueries::class.simpleName}")
        }
    } catch (e: Exception) {
        //Log.e("GlobalHelpers", "Error in linkDocumentProductToDeliveryNoteInfo: ${e.message}")
    }
}

// Synchronous DB access. Must be called from an IO context.
fun linkDocumentProductToParentDocument(
    linkQueries: Any,
    parentId: Long,
    documentProductId: Long,
    sortOrder: Int?,
) {
    try {
        val newSortOrder: Long = when (linkQueries) {
            is LinkInvoiceToDocumentProductQueries -> {
                val result =
                    linkQueries.getMaxSortOrderForInvoice(parentId).executeAsOneOrNull() // DB call
                (result?.maxOrder ?: -1L) + 1L
            }

            is LinkDeliveryNoteToDocumentProductQueries -> {
                val result = linkQueries.getMaxSortOrderForDeliveryNote(parentId)
                    .executeAsOneOrNull() // DB call
                (result?.maxOrder ?: -1L) + 1L
            }

            is LinkCreditNoteToDocumentProductQueries -> {
                val result = linkQueries.getMaxSortOrderForCreditNote(parentId)
                    .executeAsOneOrNull() // DB call
                (result?.maxOrder ?: -1L) + 1L
            }

            else -> {
                //Log.w("GlobalHelpers", "Unsupported query type for linkDocumentProductToParentDocument (sort order): ${linkQueries::class.simpleName}")
                0L // Default sort order if type is unknown, or handle error
            }
        }

        val finalSortOrder = sortOrder?.toLong() ?: newSortOrder

        when (linkQueries) {
            is LinkInvoiceToDocumentProductQueries -> {
                linkQueries.saveProductLinkedToInvoice( // DB call
                    id = null, // Auto-incremented
                    invoice_id = parentId,
                    document_product_id = documentProductId,
                    sort_order = finalSortOrder
                )
            }

            is LinkDeliveryNoteToDocumentProductQueries -> {
                linkQueries.saveProductLinkedToDeliveryNote( // DB call
                    id = null,
                    delivery_note_id = parentId,
                    document_product_id = documentProductId,
                    sort_order = finalSortOrder
                )
            }

            is LinkCreditNoteToDocumentProductQueries -> {
                linkQueries.saveProductLinkedToCreditNote( // DB call
                    id = null,
                    credit_note_id = parentId,
                    document_product_id = documentProductId,
                    sort_order = finalSortOrder
                )
            }
            // else case already handled for sort order, no insert if type is unknown
        }
    } catch (e: Exception) {
        //Log.e("GlobalHelpers", "Error in linkDocumentProductToParentDocument: ${e.message}")
    }
}

// This function is suspend.It calls other suspend functions or synchronous DB calls that should be wrapped.
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

// Synchronous DB access. Must be called from an IO context.
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

            else -> {
                //Log.w("GlobalHelpers", "Unsupported query type for linkDocumentClientOrIssuerToDocument: ${linkQueries::class.simpleName}")
            }
        }
    } catch (e: Exception) {
        //Log.e("GlobalHelpers", "Error in linkDocumentClientOrIssuerToDocument: ${e.message}")
    }
}


// This function is suspend and wraps its DB operations in Dispatchers.IO.
// It returns the ID of the saved/updated ClientOrIssuer.
private suspend fun saveDocumentClientOrIssuer(
    documentClientOrIssuerQueries: DocumentClientOrIssuerQueries,
    documentClientOrIssuerAddressQueries: DocumentClientOrIssuerAddressQueries,
    linkDocumentClientOrIssuerToAddressQueries: LinkDocumentClientOrIssuerToAddressQueries,
    clientOrIssuerState: ClientOrIssuerState,
): Long? { // Return the ID of the saved client/issuer
    return withContext(Dispatchers.IO) {
        try {
            saveInfoInDocumentClientOrIssuerTable(
                documentClientOrIssuerQueries,
                clientOrIssuerState
            ) // Synchronous DB call
            val savedClientOrIssuerId =
                documentClientOrIssuerQueries.getLastInsertedRowId().executeAsOneOrNull() // DB call

            savedClientOrIssuerId?.let { id ->
                saveInfoInDocumentClientOrIssuerAddressTables( // Synchronous DB call
                    documentClientOrIssuerAddressQueries,
                    linkDocumentClientOrIssuerToAddressQueries,
                    id,
                    clientOrIssuerState.addresses
                )
            }
            savedClientOrIssuerId
        } catch (e: Exception) {
            //Log.e("GlobalHelpers", "Error in saveDocumentClientOrIssuer: ${e.message}")
            null
        }
    }
}


// Synchronous private helper, performs DB IO.
// Must be called from a Dispatchers.IO context.
private fun saveInfoInDocumentClientOrIssuerTable(
    documentClientOrIssuerQueries: DocumentClientOrIssuerQueries,
    documentClientOrIssuer: ClientOrIssuerState,
) {
    documentClientOrIssuerQueries.save(
        // DB call
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
}

// Synchronous private helper, performs DB IO.
// Must be called from a Dispatchers.IO context.
private fun saveInfoInDocumentClientOrIssuerAddressTables(
    documentClientOrIssuerAddressQueries: DocumentClientOrIssuerAddressQueries,
    linkDocumentClientOrIssuerToAddressQueries: LinkDocumentClientOrIssuerToAddressQueries,
    documentClientOrIssuerId: Long,
    addresses: List<AddressState>?,
) {
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
}

// Pure transformation function for DocumentClientOrIssuer.
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

// Synchronous function, performs multiple DB accesses.
// MUST be called from a Dispatchers.IO context.
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

// Synchronous function, performs DB access.
// Must be called from a Dispatchers.IO context.
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

// This function should be called from a Dispatchers.IO context
fun updateDocumentProductsOrderInDb(
    documentId: Long,
    orderedProducts: List<DocumentProductState>,
    linkQueries: Any
) {

    orderedProducts.forEach { product ->
        val productId = product.id?.toLong()
        val newSortOrder = product.sortOrder?.toLong()

        if (productId != null) {
            when (linkQueries) {
                is LinkInvoiceToDocumentProductQueries -> {
                    linkQueries.updateSortOrderForDocumentProduct( // DB Call
                        sort_order = newSortOrder,
                        id = documentId,
                        document_product_id = productId
                    )
                }

                is LinkDeliveryNoteToDocumentProductQueries -> {
                    linkQueries.updateSortOrderForDocumentProduct( // DB Call
                        sort_order = newSortOrder,
                        id = documentId,
                        document_product_id = productId
                    )
                }

                is LinkCreditNoteToDocumentProductQueries -> {
                    linkQueries.updateSortOrderForDocumentProduct( // DB Call
                        sort_order = newSortOrder,
                        id = documentId,
                        document_product_id = productId
                    )
                }
                // else -> Log.w("InvoiceLocalDataSource", "Unsupported document type for updating sort order: $documentType")
            }
        } else {
            // Log.w("InvoiceLocalDataSource", "Product ID is null for product: ${product.name.text}, cannot update sort order.")
        }
    }
}
