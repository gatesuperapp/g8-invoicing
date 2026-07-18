package com.a4a.g8invoicing.data


import androidx.compose.ui.text.input.TextFieldValue
import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.util.DateUtils
import com.a4a.g8invoicing.data.util.DispatcherProvider
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.quote_default_footer
import com.a4a.g8invoicing.shared.resources.quote_default_number
import com.a4a.g8invoicing.shared.resources.invoice_watermark_default
import com.a4a.g8invoicing.data.auth.ActivatedModulesRepository
import org.jetbrains.compose.resources.getString
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.screens.shared.DocumentLabels
import com.a4a.g8invoicing.ui.states.QuoteState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import g8invoicing.Quote
import g8invoicing.DocumentClientOrIssuer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class QuoteLocalDataSource(
    db: Database,
    private val clientOrIssuerDataSource: ClientOrIssuerLocalDataSourceInterface,
    private val activatedModules: ActivatedModulesRepository,
    private val currencyManager: CurrencyManager,
) : QuoteLocalDataSourceInterface {
    private val quoteQueries = db.quoteQueries
    private val documentClientOrIssuerQueries = db.documentClientOrIssuerQueries
    private val documentClientOrIssuerAddressQueries = db.documentClientOrIssuerAddressQueries
    private val linkDocumentClientOrIssuerToAddressQueries =
        db.linkDocumentClientOrIssuerToAddressQueries
    private val documentProductQueries = db.documentProductQueries
    private val linkQuoteToDocumentProductQueries =
        db.linkQuoteToDocumentProductQueries
    private val linkQuoteToDocumentClientOrIssuerQueries =
        db.linkQuoteToDocumentClientOrIssuerQueries
    private val documentClientOrIssuerEmailQueries = db.documentClientOrIssuerEmailQueries

    // Freeze watermark at creation; see InvoiceLocalDataSource.computeWatermark for rationale.
    private suspend fun computeWatermark(): String? {
        return if (activatedModules.isActive(ActivatedModulesRepository.MODULE_WATERMARK_REMOVAL)) {
            null
        } else {
            getString(Res.string.invoice_watermark_default)
        }
    }

    // --- createNew ---
    // Called from ViewModel
    // This function performs DB operations, so it needs Dispatchers.IO.
    override suspend fun createNew(): Long? {
        // Récupérer l'émetteur depuis la table maître
        val existingIssuer = clientOrIssuerDataSource.getLastIssuer()
        val frozenWatermark = computeWatermark()
        val frozenLabels = DocumentLabels.captureSnapshotJson()

        return withContext(DispatcherProvider.IO) {
            val todayFormatted = DateUtils.getCurrentDateFormatted()

            val newQuoteState = QuoteState(
                documentNumber = TextFieldValue(getLastDocumentNumber()?.let {
                    incrementDocumentNumber(it)
                } ?: getString(Res.string.quote_default_number)),
                documentDate = todayFormatted,
                documentIssuer = existingIssuer,
                currency = TextFieldValue(currencyManager.currentCurrency),
                footerText = TextFieldValue(getExistingFooter() ?: getString(Res.string.quote_default_footer)),
                watermarkText = frozenWatermark,
                labelsSnapshot = frozenLabels,
            )

            saveInfoInDocumentTable(newQuoteState)

            var newQuoteId = quoteQueries.getLastInsertedRowId().executeAsOneOrNull()

            newQuoteId?.let { id ->
                saveInfoInOtherTables(id, newQuoteState)
            }
            newQuoteId
        }
    }

    // --- Synchronous private helpers for createNew (called from Dispatchers.IO context) ---
    private fun getLastDocumentNumber(): String? {
        try {
            return quoteQueries.getLastQuoteNumber().executeAsOneOrNull()?.number
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    private fun getExistingFooter(): String? {
        var footer: String? = null
        try {
            footer = quoteQueries.getLastInsertedFooter().executeAsOneOrNull()?.footer
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return footer
    }

    // --- fetch ---
    // Correctly uses withContext(Dispatchers.IO).
    // Internal fetch* helpers are synchronous and will run on this IO context.
    override suspend fun fetch(id: Long): QuoteState? {
        return withContext(DispatcherProvider.IO) {
            try {
                quoteQueries.get(id).executeAsOneOrNull()
                    ?.let {
                        it.transformIntoEditableQuote(
                            fetchDocumentProducts(it.quote_id),
                            fetchClientAndIssuer(
                                it.quote_id,
                                linkQuoteToDocumentClientOrIssuerQueries,
                                linkDocumentClientOrIssuerToAddressQueries,
                                documentClientOrIssuerQueries,
                                documentClientOrIssuerAddressQueries,
                                documentClientOrIssuerEmailQueries
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
    override fun fetchAll(): Flow<List<QuoteState>>? {
        try {
            return quoteQueries.getAll()
                .asFlow()
                .map {
                    it.executeAsList()
                        .map { document ->
                            val products = fetchDocumentProducts(document.quote_id)
                            val clientAndIssuer = fetchClientAndIssuer(
                                document.quote_id,
                                linkQuoteToDocumentClientOrIssuerQueries,
                                linkDocumentClientOrIssuerToAddressQueries,
                                documentClientOrIssuerQueries,
                                documentClientOrIssuerAddressQueries,
                                documentClientOrIssuerEmailQueries
                            )

                            document.transformIntoEditableQuote(
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
    private fun fetchDocumentProducts(quoteId: Long): MutableList<DocumentProductState>? {
        try {
            val listOfIds =
                linkQuoteToDocumentProductQueries.getDocumentProductsLinkedToQuote(
                    quoteId
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

    // --- transformIntoEditableQuote ---
    // Pure transformation function, no IO, no suspend/withContext needed.
    private fun Quote.transformIntoEditableQuote(
        documentProducts: MutableList<DocumentProductState>? = null,
        documentClientAndIssuer: List<ClientOrIssuerState>? = null,
    ): QuoteState {
        this.let {
            return QuoteState(
                documentId = it.quote_id.toInt(),
                documentNumber = TextFieldValue(text = it.number ?: ""),
                documentDate = it.delivery_date ?: "",
                reference = TextFieldValue(text = it.reference ?: ""),
                freeField = TextFieldValue(text = it.free_field ?: ""),
                documentIssuer = documentClientAndIssuer?.firstOrNull { it.type == ClientOrIssuerType.DOCUMENT_ISSUER },
                documentClient = documentClientAndIssuer?.firstOrNull { it.type == ClientOrIssuerType.DOCUMENT_CLIENT },
                documentProducts = documentProducts?.sortedBy { it.sortOrder },
                documentTotalPrices = documentProducts?.let { calculateDocumentPrices(it) },
                currency = TextFieldValue(it.currency ?: CurrencyManager.DEFAULT_FALLBACK),
                footerText = TextFieldValue(text = it.footer ?: ""),
                createdDate = it.created_at,
                watermarkText = it.watermark_text,
                labelsSnapshot = it.labels_snapshot,
            )
        }
    }

    // --- update ---
    // Uses withContext(Dispatchers.IO)
    override suspend fun update(document: QuoteState) {
        return withContext(DispatcherProvider.IO) {
            try {
                quoteQueries.update(
                    quote_id = document.documentId?.toLong() ?: 0,
                    number = document.documentNumber.text,
                    delivery_date = document.documentDate,
                    reference = document.reference?.text,
                    free_field = document.freeField?.text,
                    currency = document.currency.text,
                    footer = document.footerText.text,
                    updated_at = DateUtils.getCurrentTimestamp()
                )
            } catch (e: Exception) {
                //Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    // --- duplicate ---
    // Uses withContext(Dispatchers.IO).
    override suspend fun duplicate(documents: List<QuoteState>) {
        val frozenWatermark = computeWatermark()
        val frozenLabels = DocumentLabels.captureSnapshotJson()
        withContext(DispatcherProvider.IO) {
            try {
                documents.forEach { originalDocument ->
                    val docNumber = getLastDocumentNumber()?.let {
                        incrementDocumentNumber(it)
                    } ?: getString(Res.string.quote_default_number)

                    val duplicatedDocumentState = originalDocument.copy(
                        documentNumber = TextFieldValue(docNumber),
                        watermarkText = frozenWatermark,
                        labelsSnapshot = frozenLabels,
                    )

                    saveInfoInDocumentTable(duplicatedDocumentState)

                    val newQuoteId = quoteQueries.getLastInsertedRowId()
                        .executeAsOneOrNull()

                    newQuoteId?.let { id ->
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
        return withContext(DispatcherProvider.IO) {
            try {
                documentProductQueries.transactionWithResult {
                    // This global function performs synchronous DB operations
                    saveDocumentProductInDbAndLink(
                        documentProductQueries = documentProductQueries,
                        linkToDocumentProductQueries = linkQuoteToDocumentProductQueries,
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
        // Si c'est un émetteur sans originalClientOrIssuerId, le créer dans la table maître d'abord
        val clientOrIssuerToSave = if (
            (documentClientOrIssuer.type == ClientOrIssuerType.ISSUER ||
                documentClientOrIssuer.type == ClientOrIssuerType.DOCUMENT_ISSUER) &&
            documentClientOrIssuer.originalClientOrIssuerId == null
        ) {
            // Créer l'émetteur dans la table maître
            val masterIssuer = documentClientOrIssuer.copy(type = ClientOrIssuerType.ISSUER)
            clientOrIssuerDataSource.createNew(masterIssuer)
            val masterId = clientOrIssuerDataSource.getLastCreatedIssuerId()
            // Lier au master
            documentClientOrIssuer.copy(originalClientOrIssuerId = masterId?.toInt())
        } else {
            documentClientOrIssuer
        }

        withContext(DispatcherProvider.IO) {
            try {
                saveDocumentClientOrIssuerInDbAndLink(
                    documentClientOrIssuerQueries,
                    documentClientOrIssuerAddressQueries,
                    linkDocumentClientOrIssuerToAddressQueries,
                    documentClientOrIssuerEmailQueries,
                    linkQuoteToDocumentClientOrIssuerQueries,
                    clientOrIssuerToSave,
                    documentId
                )
            } catch (e: Exception) {
                //Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    // --- delete ---
    // Uses withContext(Dispatchers.IO).
    override suspend fun delete(documents: List<QuoteState>) {
        withContext(DispatcherProvider.IO) {
            try {
                documents.filter { it.documentId != null }.forEach { document ->
                    document.documentProducts?.mapNotNull { it.id }?.forEach {
                        documentProductQueries.deleteDocumentProduct(it.toLong())
                    }

                    // Delete linked products
                    linkQuoteToDocumentProductQueries.deleteAllProductsLinkedToAQuote(
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
                    linkQuoteToDocumentClientOrIssuerQueries.deleteAllDocumentClientOrIssuerLinkedToAQuote(
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
                    quoteQueries.delete(id = document.documentId!!.toLong())

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
            return withContext(DispatcherProvider.IO) {
                linkQuoteToDocumentProductQueries.deleteProductLinkedToQuote(
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
            return withContext(DispatcherProvider.IO) {
                val clientOrIssuerToDelete =
                    fetchClientAndIssuer(
                        documentId,
                        linkQuoteToDocumentClientOrIssuerQueries,
                        linkDocumentClientOrIssuerToAddressQueries,
                        documentClientOrIssuerQueries,
                        documentClientOrIssuerAddressQueries,
                        documentClientOrIssuerEmailQueries
                    )?.firstOrNull { it.type == type }

                clientOrIssuerToDelete?.id?.let { entityId ->
                    // 1. Delete the link between invoice and the client/issuer entity
                    linkQuoteToDocumentClientOrIssuerQueries.deleteDocumentClientOrIssuerLinkedToQuote(
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
    private fun saveInfoInDocumentTable(document: QuoteState) {
        try {
            quoteQueries.save(
                quote_id = null,
                number = document.documentNumber.text,
                delivery_date = document.documentDate,
                reference = document.reference?.text,
                free_field = document.freeField?.text,
                currency = document.currency.text,
                footer = document.footerText.text,
                watermark_text = document.watermarkText,
                labels_snapshot = document.labelsSnapshot,
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
        quote: QuoteState,
    ) {
        try {
            // Link all products to the new parentId
            quote.documentProducts?.forEach { documentProduct ->
                saveDocumentProductInDbAndLinkToDocument(
                    documentProduct = documentProduct,
                    documentId = documentId
                )
            }

            // Link client
            quote.documentClient?.let {
                saveDocumentClientOrIssuerInDbAndLinkToDocument(
                    documentClientOrIssuer = it,
                    documentId = documentId
                )
            }

            // Link issuer
            quote.documentIssuer?.let {
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
        withContext(DispatcherProvider.IO) {
            try {
                documentProductQueries.transaction {
                    updateDocumentProductsOrderInDb(
                        documentId,
                        orderedProducts,
                        linkQuoteToDocumentProductQueries
                    )
                }
            } catch (e: Exception) {
                // Log.e("InvoiceLocalDataSource", "Error updating document products order in DB: ${e.message}", e)
                throw e // Relance pour que le ViewModel puisse la catcher si nécessaire
            }
        }
    }
}
