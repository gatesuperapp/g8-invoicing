package com.a4a.g8invoicing.data

import androidx.compose.ui.text.input.TextFieldValue
import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.util.DateUtils
import com.a4a.g8invoicing.data.util.DispatcherProvider
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.credit_note_default_number
import com.a4a.g8invoicing.shared.resources.credit_note_reference_from_invoice
import com.a4a.g8invoicing.shared.resources.credit_note_reference_from_invoices
import com.a4a.g8invoicing.shared.resources.invoice_watermark_default
import com.a4a.g8invoicing.shared.resources.retention_default_label
import com.a4a.g8invoicing.shared.resources.retention_default_mx_isr
import com.a4a.g8invoicing.shared.resources.retention_default_mx_iva
import com.a4a.g8invoicing.data.auth.ActivatedModulesRepository
import org.jetbrains.compose.resources.getString
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.screens.shared.DocumentLabels
import com.a4a.g8invoicing.ui.states.CreditNoteState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import g8invoicing.CreditNote
import g8invoicing.CreditNoteRetention
import g8invoicing.DocumentClientOrIssuer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CreditNoteLocalDataSource(
    db: Database,
    private val clientOrIssuerDataSource: ClientOrIssuerLocalDataSourceInterface,
    private val activatedModules: ActivatedModulesRepository,
    private val currencyManager: CurrencyManager,
) : CreditNoteLocalDataSourceInterface {
    private val creditNoteQueries = db.creditNoteQueries
    private val creditNoteRetentionQueries = db.creditNoteRetentionQueries
    private val invoiceRetentionQueries = db.invoiceRetentionQueries
    private val documentClientOrIssuerQueries = db.documentClientOrIssuerQueries
    private val documentClientOrIssuerAddressQueries = db.documentClientOrIssuerAddressQueries
    private val linkDocumentClientOrIssuerToAddressQueries =
        db.linkDocumentClientOrIssuerToAddressQueries
    private val documentProductQueries = db.documentProductQueries
    private val linkDocumentProductToCreditNoteQueries = db.linkCreditNoteToDocumentProductQueries
    private val linkCreditNoteDocumentProductToDeliveryNoteQueries =
        db.linkCreditNoteDocumentProductToDeliveryNoteQueries
    private val linkCreditNoteToDocumentClientOrIssuerQueries =
        db.linkCreditNoteToDocumentClientOrIssuerQueries
    private val documentClientOrIssuerEmailQueries = db.documentClientOrIssuerEmailQueries

    // Freeze watermark at creation; see InvoiceLocalDataSource.computeWatermark for rationale.
    private suspend fun computeWatermark(): String? {
        return if (activatedModules.isActive(ActivatedModulesRepository.MODULE_WATERMARK_REMOVAL)) {
            null
        } else {
            getString(Res.string.invoice_watermark_default)
        }
    }

    override suspend fun createNew(): Long? {
        val existingIssuer = clientOrIssuerDataSource.getLastIssuer()
        val frozenWatermark = computeWatermark()
        val frozenLabels = DocumentLabels.captureSnapshotJson()

        // Reuse retentions from the most recent doc (credit note or invoice)
        // for this master issuer; fall back to country defaults.
        val reusedRetentions: List<com.a4a.g8invoicing.ui.states.RetentionState> =
            if (existingIssuer?.taxWithholdingEnabled == true) {
                existingIssuer.originalClientOrIssuerId?.toLong()?.let { masterId ->
                    fetchLatestReusedRetentions(masterId)
                } ?: com.a4a.g8invoicing.data.models.defaultRetentionsForIssuer(
                    existingIssuer,
                    getString(Res.string.retention_default_label),
                    getString(Res.string.retention_default_mx_isr),
                    getString(Res.string.retention_default_mx_iva),
                )
            } else emptyList()

        return withContext(DispatcherProvider.IO) {
            val todayFormatted = DateUtils.getCurrentDateFormatted()
            val dueDateFormatted = DateUtils.getDatePlusDaysFormatted(30)

            val creditNote = CreditNoteState(
                documentNumber = TextFieldValue(getLastDocumentNumber()?.let {
                    incrementDocumentNumber(it)
                } ?: getString(Res.string.credit_note_default_number)),
                documentDate = todayFormatted,
                dueDate = dueDateFormatted,
                documentIssuer = existingIssuer,
                currency = TextFieldValue(currencyManager.currentCurrency),
                footerText = TextFieldValue(getExistingFooter() ?: ""),
                watermarkText = frozenWatermark,
                labelsSnapshot = frozenLabels,
                showCurrencyAndAutoTaxColumn = true,
                formatLocale = AppLocaleHolder.languageCode,
                retentions = reusedRetentions,
            )

            saveInfoInCreditNoteTable(creditNote)

            val newCreditNoteId = creditNoteQueries.getLastInsertedRowId().executeAsOneOrNull()

            newCreditNoteId?.let { id ->
                saveInfoInOtherTables(creditNote)
                saveRetentionsForCreditNote(id, reusedRetentions)
            }

            newCreditNoteId
        }
    }

    // Cross-doc lookup: newest doc (CN or invoice) with retentions wins,
    // so a credit note following an invoice inherits from it.
    private fun fetchLatestReusedRetentions(
        masterIssuerId: Long,
    ): List<com.a4a.g8invoicing.ui.states.RetentionState> {
        val cnRow = creditNoteRetentionQueries
            .getLastCreditNoteIdWithRetentionsForIssuer(masterIssuerId)
            .executeAsOneOrNull()
        val invRow = invoiceRetentionQueries
            .getLastInvoiceIdWithRetentionsForIssuer(masterIssuerId)
            .executeAsOneOrNull()

        val cnTs = cnRow?.created_at
        val invTs = invRow?.created_at

        val pickInvoice = when {
            invTs == null -> false
            cnTs == null -> true
            else -> invTs >= cnTs
        }

        return if (pickInvoice && invRow != null) {
            invoiceRetentionQueries.getForInvoice(invRow.invoice_id)
                .executeAsList()
                .map { it.transformIntoRetentionState() }
        } else if (cnRow != null) {
            creditNoteRetentionQueries.getForCreditNote(cnRow.credit_note_id)
                .executeAsList()
                .map { it.transformIntoRetentionState() }
        } else emptyList()
    }

    private fun CreditNoteRetention.transformIntoRetentionState(): com.a4a.g8invoicing.ui.states.RetentionState =
        com.a4a.g8invoicing.ui.states.RetentionState(
            id = this.id.toInt(),
            label = TextFieldValue(this.label),
            rate = BigDecimal.parseString(this.rate.toString()),
            sortOrder = this.sort_order?.toInt() ?: 0,
            hidden = this.hidden != 0L,
        )

    private fun g8invoicing.InvoiceRetention.transformIntoRetentionState(): com.a4a.g8invoicing.ui.states.RetentionState =
        com.a4a.g8invoicing.ui.states.RetentionState(
            id = this.id.toInt(),
            label = TextFieldValue(this.label),
            rate = BigDecimal.parseString(this.rate.toString()),
            sortOrder = this.sort_order?.toInt() ?: 0,
            hidden = this.hidden != 0L,
        )

    private fun saveRetentionsForCreditNote(
        creditNoteId: Long,
        retentions: List<com.a4a.g8invoicing.ui.states.RetentionState>,
    ) {
        try {
            creditNoteRetentionQueries.deleteAllForCreditNote(creditNoteId)
            retentions.forEachIndexed { index, r ->
                creditNoteRetentionQueries.save(
                    id = null,
                    credit_note_id = creditNoteId,
                    label = r.label.text,
                    rate = r.rate.doubleValue(false),
                    sort_order = index.toLong(),
                    hidden = if (r.hidden) 1L else 0L,
                )
            }
        } catch (_: Exception) {
        }
    }

    private fun fetchRetentions(creditNoteId: Long): List<com.a4a.g8invoicing.ui.states.RetentionState> {
        return try {
            creditNoteRetentionQueries.getForCreditNote(creditNoteId)
                .executeAsList()
                .map { it.transformIntoRetentionState() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun getLastDocumentNumber(): String? {
        try {
            return creditNoteQueries.getLastCreditNoteNumber().executeAsOneOrNull()?.number
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    override suspend fun fetch(id: Long): CreditNoteState? {

        return withContext(DispatcherProvider.IO) {
            try {
                creditNoteQueries.get(id).executeAsOneOrNull()
                    ?.let {
                        it.transformIntoEditableCreditNote(
                            fetchDocumentProducts(it.credit_note_id),
                            fetchClientAndIssuer(
                                it.credit_note_id,
                                linkCreditNoteToDocumentClientOrIssuerQueries,
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

    override fun fetchAll(): Flow<List<CreditNoteState>>? {
        try {
            return creditNoteQueries.getAll()
                .asFlow()
                .map {
                    it.executeAsList()
                        .map { document ->
                            val products = fetchDocumentProducts(document.credit_note_id)
                            val clientAndIssuer = fetchClientAndIssuer(
                                document.credit_note_id,
                                linkCreditNoteToDocumentClientOrIssuerQueries,
                                linkDocumentClientOrIssuerToAddressQueries,
                                documentClientOrIssuerQueries,
                                documentClientOrIssuerAddressQueries,
                                documentClientOrIssuerEmailQueries
                            )

                            document.transformIntoEditableCreditNote(
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

    private fun fetchDocumentProducts(id: Long): MutableList<DocumentProductState>? {
        try {
            val listOfIds =
                linkDocumentProductToCreditNoteQueries.getDocumentProductsLinkedToCreditNote(id)
                    .executeAsList()
            return if (listOfIds.isNotEmpty()) {
                listOfIds.map {
                    val additionalInfo = linkCreditNoteDocumentProductToDeliveryNoteQueries
                        .getInfoLinkedToDocumentProduct(it.document_product_id)
                        .executeAsOneOrNull()
                    documentProductQueries.getDocumentProduct(it.document_product_id)
                        .executeAsOne()
                        .transformIntoEditableDocumentProduct(
                            additionalInfo?.delivery_note_date,
                            additionalInfo?.delivery_note_number,
                            it.sort_order?.toInt() // << Passer le sort_order de la table de liaison
                        )
                }.toMutableList()
            } else null
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    private fun CreditNote.transformIntoEditableCreditNote(
        documentProducts: MutableList<DocumentProductState>? = null,
        documentClientAndIssuer: List<ClientOrIssuerState>? = null,
        documentTag: DocumentTag? = null,
    ): CreditNoteState {
        this.let {
            return CreditNoteState(
                documentId = it.credit_note_id.toInt(),
                documentNumber = TextFieldValue(text = it.number ?: ""),
                documentDate = it.issuing_date ?: "",
                reference = TextFieldValue(text = it.reference ?: ""),
                freeField = it.free_field?.let { TextFieldValue(text = it) },
                documentIssuer = documentClientAndIssuer?.filter { it.type == ClientOrIssuerType.DOCUMENT_ISSUER }?.maxByOrNull { it.id ?: 0 },
                documentClient = documentClientAndIssuer?.filter { it.type == ClientOrIssuerType.DOCUMENT_CLIENT }?.maxByOrNull { it.id ?: 0 },
                documentProducts = documentProducts?.sortedBy { it.sortOrder },
                documentTotalPrices = documentProducts?.let { prods ->
                    calculateDocumentPrices(prods, fetchRetentions(it.credit_note_id))
                },
                currency = TextFieldValue(it.currency ?: CurrencyManager.DEFAULT_FALLBACK),
                dueDate = it.due_date ?: "",
                footerText = TextFieldValue(text = it.footer ?: ""),
                createdDate = it.created_at,
                watermarkText = it.watermark_text,
                labelsSnapshot = it.labels_snapshot,
                showCurrencyAndAutoTaxColumn = it.show_currency_and_auto_tax_column != 0L,
                formatLocale = it.format_locale,
                retentions = fetchRetentions(it.credit_note_id),
            )
        }
    }

    override suspend fun convertInvoiceToCreditNote(invoices: List<InvoiceState>): Long? {
        val frozenWatermark = computeWatermark()
        val frozenLabels = DocumentLabels.captureSnapshotJson()
        val sourceNumbers = invoices
            .mapNotNull { it.documentNumber.text.takeIf { n -> n.isNotBlank() } }
        val referenceText: String? = when {
            sourceNumbers.isEmpty() -> null
            sourceNumbers.size == 1 -> getString(Res.string.credit_note_reference_from_invoice, sourceNumbers.single())
            else -> getString(Res.string.credit_note_reference_from_invoices, sourceNumbers.joinToString(", "))
        }
        return withContext(DispatcherProvider.IO) {
            val docNumber = getLastDocumentNumber()?.let {
                incrementDocumentNumber(it)
            } ?: getString(Res.string.credit_note_default_number)

            try {
                saveInfoInCreditNoteTable(
                    CreditNoteState(
                        documentNumber = TextFieldValue(docNumber),
                        documentDate = DateUtils.getCurrentDateFormatted(),
                        reference = referenceText?.let { TextFieldValue(it) }
                            ?: invoices.firstOrNull { it.reference != null }?.reference,
                        freeField = invoices.firstOrNull { it.freeField != null }?.freeField,
                        documentIssuer = invoices.firstOrNull { it.documentIssuer != null }?.documentIssuer,
                        documentClient = invoices.firstOrNull { it.documentClient != null }?.documentClient,
                        currency = TextFieldValue(
                            invoices.firstOrNull()?.currency?.text?.takeIf { it.isNotEmpty() }
                                ?: currencyManager.currentCurrency
                        ),
                        footerText = TextFieldValue(getExistingFooter() ?: ""),
                        watermarkText = frozenWatermark,
                        labelsSnapshot = frozenLabels,
                        showCurrencyAndAutoTaxColumn = true,
                        formatLocale = AppLocaleHolder.languageCode,
                    )
                )
                val newId = creditNoteQueries.getLastInsertedRowId().executeAsOneOrNull()
                invoices.forEach {
                    saveInfoInOtherTables(it)
                }
                newId
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun update(document: CreditNoteState) {
        return withContext(DispatcherProvider.IO) {
            try {
                creditNoteQueries.update(
                    credit_note_id = document.documentId?.toLong() ?: 0,
                    number = document.documentNumber.text,
                    issuing_date = document.documentDate,
                    reference = document.reference?.text,
                    free_field = document.freeField?.text,
                    currency = document.currency.text,
                    due_date = document.dueDate,
                    footer = document.footerText.text,
                    updated_at = DateUtils.getCurrentTimestamp()
                )
                document.documentId?.toLong()?.let { id ->
                    saveRetentionsForCreditNote(id, document.retentions)
                }
            } catch (e: Exception) {
                //Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun deleteAllRetentions(creditNoteId: Long) {
        withContext(DispatcherProvider.IO) {
            try {
                creditNoteRetentionQueries.deleteAllForCreditNote(creditNoteId)
            } catch (_: Exception) {
            }
        }
    }

    override suspend fun saveRetentions(
        creditNoteId: Long,
        retentions: List<com.a4a.g8invoicing.ui.states.RetentionState>,
    ) {
        withContext(DispatcherProvider.IO) {
            saveRetentionsForCreditNote(creditNoteId, retentions)
        }
    }

    override suspend fun duplicate(documents: List<CreditNoteState>) {
        val frozenWatermark = computeWatermark()
        val frozenLabels = DocumentLabels.captureSnapshotJson()
        withContext(DispatcherProvider.IO) {
            try {
                documents.forEach {
                    val docNumber = getLastDocumentNumber()?.let {
                        incrementDocumentNumber(it)
                    } ?: getString(Res.string.credit_note_default_number)
                    val creditNote = it
                    creditNote.documentNumber = TextFieldValue(docNumber)
                    // Reset dates to today (+30 for due date) — a duplicated
                    // credit note is a new doc, not the original month-old one.
                    creditNote.documentDate = DateUtils.getCurrentDateFormatted()
                    creditNote.dueDate = DateUtils.getDatePlusDaysFormatted(30)
                    creditNote.watermarkText = frozenWatermark
                    creditNote.labelsSnapshot = frozenLabels
                    creditNote.showCurrencyAndAutoTaxColumn = true
                    creditNote.formatLocale = AppLocaleHolder.languageCode

                    saveInfoInCreditNoteTable(creditNote)
                    saveInfoInOtherTables(creditNote)
                }
            } catch (e: Exception) {
                //Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun saveDocumentProductInDbAndLinkToDocument(
        documentProduct: DocumentProductState,
        documentId: Long,
        deliveryNoteDate: String?,
        deliveryNoteNumber: String?,
    ): Int? {
        return withContext(DispatcherProvider.IO) {
            try {
                val result = documentProductQueries.transactionWithResult {
                    saveDocumentProductInDbAndLink(
                        documentProductQueries,
                        linkDocumentProductToCreditNoteQueries,
                        linkCreditNoteDocumentProductToDeliveryNoteQueries,
                        documentProduct,
                        documentId,
                        deliveryNoteDate,
                        deliveryNoteNumber
                    )
                }
                result
            } catch (e: Exception) {
                null
            }
        }
    }

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
                    linkCreditNoteToDocumentClientOrIssuerQueries,
                    clientOrIssuerToSave,
                    documentId
                )
            } catch (e: Exception) {
                //Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun delete(documents: List<CreditNoteState>) {
        withContext(DispatcherProvider.IO) {
            try {
                documents.filter { it.documentId != null }.forEach { document ->
                    document.documentProducts?.mapNotNull { it.id }?.forEach {
                        documentProductQueries.deleteDocumentProduct(it.toLong())
                        linkCreditNoteDocumentProductToDeliveryNoteQueries.deleteInfoLinkedToDocumentProduct(
                            it.toLong()
                        )
                    }
                    creditNoteQueries.delete(id = document.documentId!!.toLong())
                    linkDocumentProductToCreditNoteQueries.deleteAllProductsLinkedToCreditNote(
                        document.documentId!!.toLong()
                    )
                    linkCreditNoteToDocumentClientOrIssuerQueries.deleteAllDocumentClientOrIssuerLinkedToCreditNote(
                        document.documentId!!.toLong()
                    )
                    document.documentClient?.addresses?.mapNotNull { it.id }?.forEach {
                        documentClientOrIssuerAddressQueries.delete(it.toLong())
                        linkDocumentClientOrIssuerToAddressQueries.delete(it.toLong())
                    }
                    document.documentClient?.addresses?.mapNotNull { it.id }?.forEach {
                        documentClientOrIssuerAddressQueries.delete(it.toLong())
                        linkDocumentClientOrIssuerToAddressQueries.delete(it.toLong())
                    }
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
                //Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun deleteDocumentProduct(documentId: Long, documentProductId: Long) {
        try {
            return withContext(DispatcherProvider.IO) {
                linkDocumentProductToCreditNoteQueries.deleteProductLinkedToCreditNote(
                    documentId,
                    documentProductId
                )
                linkCreditNoteDocumentProductToDeliveryNoteQueries.deleteInfoLinkedToDocumentProduct(
                    documentProductId
                )
            }
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

    override suspend fun deleteDocumentClientOrIssuer(
        id: Long,
        type: ClientOrIssuerType,
    ) {
        try {
            return withContext(DispatcherProvider.IO) {
                val documentClientOrIssuer =
                    fetchClientAndIssuer(
                        id,
                        linkCreditNoteToDocumentClientOrIssuerQueries,
                        linkDocumentClientOrIssuerToAddressQueries,
                        documentClientOrIssuerQueries,
                        documentClientOrIssuerAddressQueries,
                        documentClientOrIssuerEmailQueries
                    )?.firstOrNull { it.type == type }

                documentClientOrIssuer?.id?.let {
                    linkCreditNoteToDocumentClientOrIssuerQueries.deleteDocumentClientOrIssuerLinkedToCreditNote(
                        id,
                        it.toLong()
                    )
                    documentClientOrIssuerQueries.delete(it.toLong())
                }
            }
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

    private fun getExistingFooter(): String? {
        var footer: String? = null
        try {
            footer = creditNoteQueries.getLastInsertedFooter().executeAsOneOrNull()?.footer
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return footer
    }

    private fun saveInfoInCreditNoteTable(document: CreditNoteState) {
        try {
            creditNoteQueries.save(
                credit_note_id = null,
                number = document.documentNumber.text,
                issuing_date = document.documentDate,
                reference = document.reference?.text,
                free_field = document.freeField?.text,
                currency = document.currency.text,
                due_date = document.dueDate,
                footer = document.footerText.text,
                watermark_text = document.watermarkText,
                labels_snapshot = document.labelsSnapshot,
                show_currency_and_auto_tax_column = if (document.showCurrencyAndAutoTaxColumn) 1L else 0L,
                format_locale = document.formatLocale,
            )
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

    private suspend fun saveInfoInOtherTables(document: DocumentState) {
        try {
            creditNoteQueries.getLastInsertedRowId().executeAsOneOrNull()?.let { id ->
                // Link all products
                document.documentProducts?.forEach { documentProduct ->
                    saveDocumentProductInDbAndLinkToDocument(
                        documentProduct = documentProduct,
                        documentId = id
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
                        linkDocumentProductToCreditNoteQueries
                    )
                }
            } catch (e: Exception) {
                // Log.e("InvoiceLocalDataSource", "Error updating document products order in DB: ${e.message}", e)
                throw e // Relance pour que le ViewModel puisse la catcher si nécessaire
            }
        }
    }
}
