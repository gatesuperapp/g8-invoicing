package com.a4a.g8invoicing.data


import androidx.compose.ui.text.input.TextFieldValue
import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.screens.shared.getDateFormatter
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType
import g8invoicing.DeliveryNote
import g8invoicing.DocumentClientOrIssuer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar

class DeliveryNoteLocalDataSource(
    db: Database,
) : DeliveryNoteLocalDataSourceInterface {
    private val deliveryNoteQueries = db.deliveryNoteQueries
    private val documentClientOrIssuerQueries = db.documentClientOrIssuerQueries
    private val documentClientOrIssuerAddressQueries = db.documentClientOrIssuerAddressQueries
    private val linkDocumentClientOrIssuerToAddressQueries =
        db.linkDocumentClientOrIssuerToAddressQueries
    private val documentProductQueries = db.documentProductQueries
    private val linkDeliveryNoteToDocumentProductQueries =
        db.linkDeliveryNoteToDocumentProductQueries
    private val linkDeliveryNoteToDocumentClientOrIssuerQueries =
        db.linkDeliveryNoteToDocumentClientOrIssuerQueries

    // --- createNew ---
    // Called from ViewModel
    // This function performs DB operations, so it needs Dispatchers.IO.
    override suspend fun createNew(): Long? {
        return withContext(Dispatchers.IO) {

            val newDeliveryNoteState = DeliveryNoteState(
                documentNumber = TextFieldValue(getLastDocumentNumber()?.let {
                    incrementDocumentNumber(it)
                } ?: Strings.get(R.string.delivery_note_default_number)),
                documentIssuer = getExistingIssuer()?.transformIntoEditable(),
                footerText = TextFieldValue(getExistingFooter() ?: "")
            )

            saveInfoInDocumentTable(newDeliveryNoteState)

            var newDeliveryNoteId = deliveryNoteQueries.getLastInsertedRowId().executeAsOneOrNull()

            newDeliveryNoteId?.let { id ->
                saveInfoInOtherTables(id, newDeliveryNoteState)
            }
            newDeliveryNoteId
        }
    }

    // --- Synchronous private helpers for createNew (called from Dispatchers.IO context) ---
    private fun getLastDocumentNumber(): String? {
        try {
            return deliveryNoteQueries.getLastDeliveryNoteNumber().executeAsOneOrNull()?.number
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
            footer = deliveryNoteQueries.getLastInsertedFooter().executeAsOneOrNull()?.footer
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return footer
    }

    // --- fetch ---
    // Correctly uses withContext(Dispatchers.IO).
    // Internal fetch* helpers are synchronous and will run on this IO context.
    override suspend fun fetch(id: Long): DeliveryNoteState? {
        return withContext(Dispatchers.IO) {
            try {
                deliveryNoteQueries.get(id).executeAsOneOrNull()
                    ?.let {
                        it.transformIntoEditableDeliveryNote(
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
                null
            }
        }
    }

    // --- fetchAll (returning Flow) ---
    // Flow construction
    // The .map block executes on the collector's context.
    // This Flow is collected on Dispatchers.IO (e.g., using .flowOn(Dispatchers.IO) in ViewModel)
    // because internal fetch* helpers are synchronous DB calls.
    override fun fetchAll(): Flow<List<DeliveryNoteState>>? {
        try {
            return deliveryNoteQueries.getAll()
                .asFlow()
                .map {
                    it.executeAsList()
                        .map { document ->
                            val products = fetchDocumentProducts(document.delivery_note_id)
                            val clientAndIssuer = fetchClientAndIssuer(
                                document.delivery_note_id,
                                linkDeliveryNoteToDocumentClientOrIssuerQueries,
                                linkDocumentClientOrIssuerToAddressQueries,
                                documentClientOrIssuerQueries,
                                documentClientOrIssuerAddressQueries
                            )

                            document.transformIntoEditableDeliveryNote(
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

    // --- fetchDocumentProducts ---
    // Synchronous private helper, performs DB IO.
    // Must be called from a Dispatchers.IO context.
    private fun fetchDocumentProducts(deliveryNoteId: Long): MutableList<DocumentProductState>? {
        try {
            val listOfIds =
                linkDeliveryNoteToDocumentProductQueries.getDocumentProductsLinkedToDeliveryNote(
                    deliveryNoteId
                ).executeAsList()
            return if (listOfIds.isNotEmpty()) {
                listOfIds.map {
                    documentProductQueries.getDocumentProduct(it.document_product_id)
                        .executeAsOne()
                        .transformIntoEditableDocumentProduct(
                            sortOrder = it.sort_order?.toInt()
                        )
                }.toMutableList()
            } else null
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    // --- transformIntoEditableDeliveryNote ---
    // Pure transformation function, no IO, no suspend/withContext needed.
    private fun DeliveryNote.transformIntoEditableDeliveryNote(
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
                documentProducts = documentProducts?.sortedBy { it.sortOrder },
                documentTotalPrices = documentProducts?.let { calculateDocumentPrices(it) },
                currency = TextFieldValue(Strings.get(R.string.currency)),
                footerText = TextFieldValue(text = it.footer ?: ""),
                createdDate = it.created_at
            )
        }
    }

    // --- update ---
    // Uses withContext(Dispatchers.IO)
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

    // --- duplicate ---
    // Uses withContext(Dispatchers.IO).
    override suspend fun duplicate(documents: List<DeliveryNoteState>) {
        withContext(Dispatchers.IO) {
            try {
                documents.forEach { originalDocument ->
                    val docNumber = getLastDocumentNumber()?.let {
                        incrementDocumentNumber(it)
                    } ?: Strings.get(R.string.delivery_note_default_number)

                    val duplicatedDocumentState = originalDocument.copy(
                        documentNumber = TextFieldValue(docNumber),
                    )

                    saveInfoInDocumentTable(duplicatedDocumentState)

                    val newDeliveryNoteId = deliveryNoteQueries.getLastInsertedRowId()
                        .executeAsOneOrNull()

                    newDeliveryNoteId?.let { id ->
                        saveInfoInOtherTables(
                            id,
                            duplicatedDocumentState
                        )
                    }
                }
            } catch (e: Exception) {
                //Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    // --- saveDocumentProductInDbAndLinkToDocument ---
    // Uses withContext(Dispatchers.IO) and transaction.
    override suspend fun saveDocumentProductInDbAndLinkToDocument(
        documentProduct: DocumentProductState,
        documentId: Long
    ): Int? {
        return withContext(Dispatchers.IO) {
            try {
                documentProductQueries.transactionWithResult {
                    // This global function performs synchronous DB operations
                    saveDocumentProductInDbAndLink(
                        documentProductQueries = documentProductQueries,
                        linkToDocumentProductQueries = linkDeliveryNoteToDocumentProductQueries,
                        documentProduct = documentProduct,
                        documentId = documentId
                    )
                }
            } catch (e: Exception) {
                null
                //Log.e("InvoiceDS", "Error saveDocProdAndLink: ${e.message}")
            }
        }
    }

    // --- saveDocumentClientOrIssuerInDbAndLinkToDocument ---
    // Uses withContext(Dispatchers.IO)
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

    // --- delete ---
    // Uses withContext(Dispatchers.IO).
    override suspend fun delete(documents: List<DeliveryNoteState>) {
        withContext(Dispatchers.IO) {
            try {
                documents.filter { it.documentId != null }.forEach { document ->
                    document.documentProducts?.mapNotNull { it.id }?.forEach {
                        documentProductQueries.deleteDocumentProduct(it.toLong())
                    }

                    // Delete linked products
                    linkDeliveryNoteToDocumentProductQueries.deleteAllProductsLinkedToADeliveryNote(
                        document.documentId!!.toLong()
                    )
                    document.documentProducts?.filter { it.id != null }?.let {
                        it.forEach { documentProduct ->
                            deleteDocumentProduct(
                                document.documentId!!.toLong(),
                                documentProduct.id!!.toLong()
                            )
                        }
                    }

                    // Delete linked client/issuer
                    linkDeliveryNoteToDocumentClientOrIssuerQueries.deleteAllDocumentClientOrIssuerLinkedToADeliveryNote(
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


                    // Delete the main document
                    deliveryNoteQueries.delete(id = document.documentId!!.toLong())

                }
            } catch (e: Exception) {
                //Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    // --- deleteDocumentProduct (from an Invoice context) ---
    // Specific helper for deleting a product linked to an invoice.
    // Uses withContext(Dispatchers.IO).
    override suspend fun deleteDocumentProduct(documentId: Long, documentProductId: Long) {
        try {
            return withContext(Dispatchers.IO) {
                linkDeliveryNoteToDocumentProductQueries.deleteProductLinkedToDeliveryNote(
                    documentId,
                    documentProductId
                )
            }
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

    // --- deleteDocumentClientOrIssuer ---
    // Uses withContext(Dispatchers.IO)
    override suspend fun deleteDocumentClientOrIssuer(
        documentId: Long,
        type: ClientOrIssuerType,
    ) {
        try {
            return withContext(Dispatchers.IO) {
                val clientOrIssuerToDelete =
                    fetchClientAndIssuer(
                        documentId,
                        linkDeliveryNoteToDocumentClientOrIssuerQueries,
                        linkDocumentClientOrIssuerToAddressQueries,
                        documentClientOrIssuerQueries,
                        documentClientOrIssuerAddressQueries
                    )?.firstOrNull { it.type == type }

                clientOrIssuerToDelete?.id?.let { entityId ->
                    // 1. Delete the link between invoice and the client/issuer entity
                    linkDeliveryNoteToDocumentClientOrIssuerQueries.deleteDocumentClientOrIssuerLinkedToDeliveryNote(
                        documentId,
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
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

    // --- saveInfoInDocumentTable ---
    // Synchronous private helper, performs DB IO.
    // Must be called from a Dispatchers.IO context
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

    // --- saveInfoInOtherTables ---
    // Private suspend helper. Called with an explicit parentId (invoiceId).
    // Calls other suspend functions that manage their own IO.
    private suspend fun saveInfoInOtherTables(
        documentId: Long,
        deliveryNote: DeliveryNoteState,
    ) {
        try {
            // Link all products to the new parentId
            deliveryNote.documentProducts?.forEach { documentProduct ->
                saveDocumentProductInDbAndLinkToDocument(
                    documentProduct = documentProduct,
                    documentId = documentId
                )
            }

            // Link client
            deliveryNote.documentClient?.let {
                saveDocumentClientOrIssuerInDbAndLinkToDocument(
                    documentClientOrIssuer = it,
                    documentId = documentId
                )
            }

            // Link issuer
            deliveryNote.documentIssuer?.let {
                saveDocumentClientOrIssuerInDbAndLinkToDocument(
                    documentClientOrIssuer = it,
                    documentId = documentId
                )
            }
        } catch (e: Exception) {
            //Log.e("InvoiceDS", "Error saveInfoInOtherTables for parentId $parentId: ${e.message}")
        }
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
                    updateDocumentProductsOrderInDb(
                        documentId,
                        orderedProducts,
                        linkDeliveryNoteToDocumentProductQueries
                    )
                }
            } catch (e: Exception) {
                // Log.e("InvoiceLocalDataSource", "Error updating document products order in DB: ${e.message}", e)
                throw e // Relance pour que le ViewModel puisse la catcher si nécessaire
            }
        }
    }
}

