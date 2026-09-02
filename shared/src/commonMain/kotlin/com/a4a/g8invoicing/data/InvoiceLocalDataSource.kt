package com.a4a.g8invoicing.data

import androidx.compose.ui.text.input.TextFieldValue
import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.models.TagUpdateOrCreationCase
import com.a4a.g8invoicing.data.util.DateUtils
import com.a4a.g8invoicing.data.util.DispatcherProvider
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.document_default_footer
import com.a4a.g8invoicing.shared.resources.payment_terms_discount_default
import com.a4a.g8invoicing.shared.resources.payment_terms_late_fees_default
import com.a4a.g8invoicing.shared.resources.payment_terms_recovery_fees_default
import com.a4a.g8invoicing.shared.resources.document_payment_means_default_label
import com.a4a.g8invoicing.shared.resources.invoice_watermark_default
import com.a4a.g8invoicing.shared.resources.retention_default_label
import com.a4a.g8invoicing.shared.resources.retention_default_mx_isr
import com.a4a.g8invoicing.shared.resources.retention_default_mx_iva
import com.a4a.g8invoicing.data.auth.ActivatedModulesRepository
import com.a4a.g8invoicing.data.auth.SubscriptionRepository
import com.a4a.g8invoicing.shared.resources.invoice_default_number
import org.jetbrains.compose.resources.getString
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.screens.shared.DocumentLabels
import com.a4a.g8invoicing.ui.states.AddressState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.EmailState
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.DocumentTotalPrices
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.states.QuoteState
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import g8invoicing.DocumentClientOrIssuer
import g8invoicing.DocumentClientOrIssuerAddressQueries
import g8invoicing.DocumentClientOrIssuerEmailQueries
import g8invoicing.DocumentClientOrIssuerQueries
import g8invoicing.DocumentProductQueries
import g8invoicing.GetLastInsertedInvoicePaymentTerms
import g8invoicing.Invoice
import g8invoicing.InvoiceRetention
import g8invoicing.LinkCreditNoteDocumentProductToDeliveryNoteQueries
import g8invoicing.LinkCreditNoteToDocumentClientOrIssuerQueries
import g8invoicing.LinkCreditNoteToDocumentProductQueries
import g8invoicing.LinkDeliveryNoteToDocumentClientOrIssuerQueries
import g8invoicing.LinkDeliveryNoteToDocumentProductQueries
import g8invoicing.LinkDocumentClientOrIssuerToAddressQueries
import g8invoicing.LinkQuoteToDocumentClientOrIssuerQueries
import g8invoicing.LinkQuoteToDocumentProductQueries
import g8invoicing.LinkInvoiceDocumentProductToDeliveryNoteQueries
import g8invoicing.LinkInvoiceDocumentProductToQuoteQueries
import g8invoicing.LinkInvoiceToDocumentClientOrIssuerQueries
import g8invoicing.LinkInvoiceToDocumentProductQueries
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class InvoiceLocalDataSource(
    db: Database,
    private val clientOrIssuerDataSource: ClientOrIssuerLocalDataSourceInterface,
    private val activatedModules: ActivatedModulesRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val currencyManager: CurrencyManager,
    private val currentCompanyRepository: CurrentCompanyRepository,
) : InvoiceLocalDataSourceInterface {
    private val invoiceQueries = db.invoiceQueries
    private val invoiceTagQueries = db.invoiceTagQueries
    private val documentClientOrIssuerQueries = db.documentClientOrIssuerQueries
    private val documentClientOrIssuerAddressQueries = db.documentClientOrIssuerAddressQueries
    private val linkDocumentClientOrIssuerToAddressQueries =
        db.linkDocumentClientOrIssuerToAddressQueries
    private val documentClientOrIssuerEmailQueries = db.documentClientOrIssuerEmailQueries
    private val documentProductQueries = db.documentProductQueries
    private val linkInvoiceToDocumentProductQueries = db.linkInvoiceToDocumentProductQueries
    private val linkInvoiceToTagQueries = db.linkInvoiceToTagQueries
    private val linkInvoiceDocumentProductToDeliveryNoteQueries =
        db.linkInvoiceDocumentProductToDeliveryNoteQueries
    private val linkInvoiceDocumentProductToQuoteQueries =
        db.linkInvoiceDocumentProductToQuoteQueries
    private val linkInvoiceToDocumentClientOrIssuerQueries =
        db.linkInvoiceToDocumentClientOrIssuerQueries
    private val invoiceRetentionQueries = db.invoiceRetentionQueries


    // --- createNew ---
    // Called from ViewModel
    // This function performs DB operations, so it needs Dispatchers.IO.
    override suspend fun createNew(): Long? {
        // Résout l'entreprise courante (menu latéral). Fallback getLastIssuer()
        // pour les installs qui n'ont pas encore Settings hydratée — ne devrait
        // jamais tomber ici post-migration 6→7, sécurité seulement.
        val currentCompanyId = currentCompanyRepository.current
        val existingIssuer = currentCompanyId
            ?.let { clientOrIssuerDataSource.getCurrentIssuer(it) }
            ?: clientOrIssuerDataSource.getLastIssuer()
        val frozenWatermark = computeWatermark()
        val frozenLabels = DocumentLabels.captureSnapshotJson()

        // Per-issuer reuse: pull payment_terms / payment_means / payment_bank
        // from the most recent invoice for the same master issuer, so the new
        // invoice inherits whatever the user last set on THIS company (not the
        // last global invoice, which might belong to a different émetteur).
        // getLastIssuer() returns a DOCUMENT_ISSUER shell with id=null and the
        // master pointer on originalClientOrIssuerId — that's the id our SQL
        // filters on.
        // Null when no prior invoice matches → fall back to defaults.
        val reuse = existingIssuer?.originalClientOrIssuerId?.toLong()?.let { masterId ->
            invoiceQueries.getLastInvoicePaymentReuseForIssuer(masterId)
                .executeAsOneOrNull()
        }
        // Reuse the last invoice's retentions for this master issuer; fall
        // back to country defaults. Keyed on originalClientOrIssuerId since
        // the fresh DOCUMENT_ISSUER shell has no id yet.
        val reusedRetentions: List<com.a4a.g8invoicing.ui.states.RetentionState> =
            if (existingIssuer?.taxWithholdingEnabled == true) {
                existingIssuer.originalClientOrIssuerId?.toLong()?.let { masterId ->
                    invoiceRetentionQueries.getLastInvoiceIdWithRetentionsForIssuer(masterId)
                        .executeAsOneOrNull()?.let { row ->
                            invoiceRetentionQueries.getForInvoice(row.invoice_id)
                                .executeAsList()
                                .map { it.transformIntoRetentionState() }
                        }
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

            val reusedSelections = reuse?.payment_means_selections
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?.toSet()
                ?.takeIf { it.isNotEmpty() }
            val reusedSegments = reuse?.payment_means_label?.let {
                com.a4a.g8invoicing.data.models.parsePaymentLabel(it)
            }?.takeIf { it.isNotEmpty() }
            val reusedBankSegments = reuse?.payment_bank_label?.let {
                com.a4a.g8invoicing.data.models.parsePaymentBankLabel(it)
            }?.takeIf { it.isNotEmpty() }
            // 3-way payment terms — each field is seeded independently from
            // the last invoice's corresponding column (so a user who only
            // customised "pénalités de retard" keeps the other two on their
            // localised defaults instead of getting blanks).
            val reusedRecoveryFees = reuse?.payment_terms_recovery_fees?.takeIf { it.isNotEmpty() }
            val reusedLateFees = reuse?.payment_terms_late_fees?.takeIf { it.isNotEmpty() }
            val reusedDiscount = reuse?.payment_terms_discount?.takeIf { it.isNotEmpty() }
            val lastTerms = getLastInvoicePaymentTerms()

            val newInvoiceState = InvoiceState(
                documentNumber = TextFieldValue(
                    getLastDocumentNumber(currentCompanyId)?.let { incrementDocumentNumber(it) }
                        ?: getString(Res.string.invoice_default_number)
                ),
                documentDate = todayFormatted,
                dueDate = dueDateFormatted,
                documentIssuer = existingIssuer,
                currency = TextFieldValue(currencyManager.currentCurrency),
                footerText = TextFieldValue(
                    getExistingFooter() ?: getString(Res.string.document_default_footer)
                ),
                watermarkText = frozenWatermark,
                labelsSnapshot = frozenLabels,
                showCurrencyAndAutoTaxColumn = true,
                formatLocale = AppLocaleHolder.languageCode,
                paymentMeansSelections = reusedSelections ?: setOf(
                    com.a4a.g8invoicing.data.models.PaymentMeans.TRANSFER.chipId,
                    com.a4a.g8invoicing.data.models.PaymentMeans.CHEQUE.chipId,
                    com.a4a.g8invoicing.data.models.PaymentMeans.CASH.chipId,
                ),
                paymentMeansOtherChecked = (reuse?.payment_means_other_checked ?: 0L) != 0L,
                paymentMeansSegments = reusedSegments
                    ?: com.a4a.g8invoicing.data.models.defaultPaymentSegments(
                        getString(Res.string.document_payment_means_default_label)
                    ),
                paymentBankSegments = reusedBankSegments ?: emptyList(),
                paymentTermsRecoveryFees = TextFieldValue(
                    reusedRecoveryFees
                        ?: lastTerms?.payment_terms_recovery_fees?.takeIf { it.isNotEmpty() }
                        ?: getString(Res.string.payment_terms_recovery_fees_default)
                ),
                paymentTermsLateFees = TextFieldValue(
                    reusedLateFees
                        ?: lastTerms?.payment_terms_late_fees?.takeIf { it.isNotEmpty() }
                        ?: getString(Res.string.payment_terms_late_fees_default)
                ),
                paymentTermsDiscount = TextFieldValue(
                    reusedDiscount
                        ?: lastTerms?.payment_terms_discount?.takeIf { it.isNotEmpty() }
                        ?: getString(Res.string.payment_terms_discount_default)
                ),
                // Frozen at creation. Falls back to the master id of the
                // resolved issuer when currentCompanyRepository has nothing
                // yet — keeps original_company_id NOT NULL for numbering.
                originalCompanyId = currentCompanyId
                    ?: existingIssuer?.originalClientOrIssuerId?.toLong(),
                // BT-120 seed. Preference order:
                //   1. the wording the user set on the previous invoice for
                //      this master issuer (per-issuer reuse — they'll rarely
                //      re-word between two consecutive invoices, and losing
                //      their custom wording on every "New invoice" is jarring),
                //   2. the country-based legal default (only when we know a
                //      correct citation for the issuer's country).
                // Foreign issuers with no reuse and no default get null → the
                // export guard blocks Factur-X until the user fills the field
                // via the text menu.
                vatExemptionText = existingIssuer
                    ?.takeIf { it.vatExempt }
                    ?.let {
                        reuse?.vat_exemption_text?.trim()?.takeIf { s -> s.isNotEmpty() }
                            ?: com.a4a.g8invoicing.data.models.defaultVatExemptionText(
                                it.addresses?.firstOrNull()?.countryCode
                            )
                    }
                    ?.let { TextFieldValue(it) },
                retentions = reusedRetentions,
                // Reuse the last invoice's picked typeface so users don't
                // have to re-pick on every new doc. Null (no invoices yet)
                // → DocumentFont.Default resolves at render time.
                fontFamily = getExistingFont(),
            )

            saveInfoInInvoiceTable(newInvoiceState)

            val newInvoiceId = invoiceQueries.getLastInsertedRowId().executeAsOneOrNull()

            newInvoiceId?.let { id ->
                // Pass the obtained ID explicitly to helper functions
                saveTag(id, newInvoiceState) // saveTag is suspend
                saveInfoInOtherTables(id, newInvoiceState) // saveInfoInOtherTables is suspend
                saveRetentionsForInvoice(id, reusedRetentions)
            }
            newInvoiceId // Return the ID
        }
    }

    private fun InvoiceRetention.transformIntoRetentionState(): com.a4a.g8invoicing.ui.states.RetentionState =
        com.a4a.g8invoicing.ui.states.RetentionState(
            id = this.id.toInt(),
            label = TextFieldValue(this.label),
            rate = BigDecimal.parseString(this.rate.toString()),
            sortOrder = this.sort_order?.toInt() ?: 0,
            hidden = this.hidden != 0L,
        )

    // Wipe + re-insert on save; N is tiny (1-3 rows) so per-row diffing isn't
    // worth the bookkeeping.
    private fun saveRetentionsForInvoice(
        invoiceId: Long,
        retentions: List<com.a4a.g8invoicing.ui.states.RetentionState>,
    ) {
        try {
            invoiceRetentionQueries.deleteAllForInvoice(invoiceId)
            retentions.forEachIndexed { index, r ->
                invoiceRetentionQueries.save(
                    id = null,
                    invoice_id = invoiceId,
                    label = r.label.text,
                    rate = r.rate.doubleValue(false),
                    sort_order = index.toLong(),
                    hidden = if (r.hidden) 1L else 0L,
                )
            }
        } catch (_: Exception) {
        }
    }

    private fun fetchRetentions(invoiceId: Long): List<com.a4a.g8invoicing.ui.states.RetentionState> {
        return try {
            invoiceRetentionQueries.getForInvoice(invoiceId)
                .executeAsList()
                .map { it.transformIntoRetentionState() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // Compute the watermark to freeze on a newly-created invoice. Returns null
    // when the watermark-removal module is active — the invoice then renders
    // without a watermark forever, even if the user later un-activates the
    // module. Conversely, an invoice created without removal keeps its
    // watermark forever, even if the module is activated later. Freezing at
    // creation is the whole point of persisting the string in the DB.
    // Module is free since the 1.9 gStore pass — no premium gate here.
    private suspend fun computeWatermark(): String? {
        return if (activatedModules.isActive(ActivatedModulesRepository.MODULE_WATERMARK_REMOVAL)) null
        else getString(Res.string.invoice_watermark_default)
    }

    // --- Synchronous private helpers for createNew (called from Dispatchers.IO context) ---
    // companyId non-null → the new invoice's number continues that entreprise's
    // counter (multi-entreprise). Null falls back to the global counter for
    // installs where the CurrentCompanyRepository isn't hydrated yet.
    private fun getLastDocumentNumber(companyId: Long?): String? {
        try {
            return if (companyId != null) {
                invoiceQueries.getLastInvoiceNumberForCompany(companyId).executeAsOneOrNull()?.number
            } else {
                invoiceQueries.getLastInvoiceNumber().executeAsOneOrNull()?.number
            }
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
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

    private fun getExistingFont(): String? {
        return try {
            invoiceQueries.getLastInsertedInvoiceFont().executeAsOneOrNull()?.font_family
        } catch (e: Exception) {
            null
        }
    }

    // Last invoice's 3 payment-terms columns — used by createNew() to seed
    // each field independently (per-field last-used, not a blanket copy of
    // the paragraph). Null when there are no invoices yet; callers fall
    // back to the localised payment_terms_*_default per field.
    private fun getLastInvoicePaymentTerms(): GetLastInsertedInvoicePaymentTerms? {
        return try {
            invoiceQueries.getLastInsertedInvoicePaymentTerms().executeAsOneOrNull()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Seed the 3 payment-terms fields for a brand-new invoice: last-invoice
     * value if non-empty, else the localised default. Called by createNew()
     * and by the convertX toInvoice flows.
     */
    private suspend fun seedPaymentTermsForNewInvoice(): Triple<TextFieldValue, TextFieldValue, TextFieldValue> {
        val last = getLastInvoicePaymentTerms()
        return Triple(
            TextFieldValue(
                last?.payment_terms_recovery_fees?.takeIf { it.isNotEmpty() }
                    ?: getString(Res.string.payment_terms_recovery_fees_default)
            ),
            TextFieldValue(
                last?.payment_terms_late_fees?.takeIf { it.isNotEmpty() }
                    ?: getString(Res.string.payment_terms_late_fees_default)
            ),
            TextFieldValue(
                last?.payment_terms_discount?.takeIf { it.isNotEmpty() }
                    ?: getString(Res.string.payment_terms_discount_default)
            ),
        )
    }

    // --- fetch ---
    // Correctly uses withContext(Dispatchers.IO).
    // Internal fetch* helpers are synchronous and will run on this IO context.
    override suspend fun fetch(id: Long): InvoiceState? {
        return withContext(DispatcherProvider.IO) {
            try {
                invoiceQueries.get(id).executeAsOneOrNull()
                    ?.let {
                        it.transformIntoEditableInvoice(
                            fetchDocumentProducts(it.invoice_id),// Synchronous, runs on this IO context
                            hydrateBanksOnDocIssuer(
                                fetchClientAndIssuer(
                                    it.invoice_id,
                                    linkInvoiceToDocumentClientOrIssuerQueries,
                                    linkDocumentClientOrIssuerToAddressQueries,
                                    documentClientOrIssuerQueries,
                                    documentClientOrIssuerAddressQueries,
                                    documentClientOrIssuerEmailQueries
                                )
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

    // Populate DOCUMENT_ISSUER states with their master's bank accounts. Needed
    // by the doc-embedded issuer form + the payment picker: the doc-frozen
    // snapshot only stores the *picked* IBAN/BIC, not the whole list. Fetching
    // banks here surfaces every account so the picker can offer them and the
    // form can edit them (edits then sync back to master on save).
    private suspend fun hydrateBanksOnDocIssuer(
        states: List<ClientOrIssuerState>?,
    ): List<ClientOrIssuerState>? = states?.map { state ->
        if (state.type == ClientOrIssuerType.DOCUMENT_ISSUER &&
            state.originalClientOrIssuerId != null
        ) {
            state.copy(
                banks = clientOrIssuerDataSource
                    .getIssuerBanks(state.originalClientOrIssuerId!!.toLong())
            )
        } else state
    }


    // --- fetchAll (returning Flow) ---
    // Flow construction
    // The .map block executes on the collector's context.
    // This Flow is collected on Dispatchers.IO (e.g., using .flowOn(Dispatchers.IO) in ViewModel)
    // because internal fetch* helpers are synchronous DB calls.
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun fetchAll(): Flow<List<InvoiceState>>? {
        try {
            // Scoped to the entreprise courante — re-emits when the user
            // switches company. Falls back to the global list only when the
            // repository has no hydrated value (pre-migration safety).
            return currentCompanyRepository.state.flatMapLatest { companyId ->
                val query = if (companyId != null) {
                    invoiceQueries.getAllForCompany(companyId)
                } else {
                    invoiceQueries.getAll()
                }
                query.asFlow().map { rows -> // This .map runs on the collector's dispatcher
                    rows.executeAsList()
                        .map { document ->
                            val products = fetchDocumentProducts(document.invoice_id)
                            val clientAndIssuer = fetchClientAndIssuer(
                                document.invoice_id,
                                linkInvoiceToDocumentClientOrIssuerQueries,
                                linkDocumentClientOrIssuerToAddressQueries,
                                documentClientOrIssuerQueries,
                                documentClientOrIssuerAddressQueries,
                                documentClientOrIssuerEmailQueries
                            )
                            val tag = fetchTag(document.invoice_id)

                            document.transformIntoEditableInvoice(
                                products,
                                clientAndIssuer,
                                tag
                            )
                        }
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
                    // Each invoice product carries at most one source-doc trace: it was
                    // either cloned from a delivery note or from a quote (never both).
                    // Try the delivery-note link table first, fall back to the quote one
                    // so the product exposes a linkedDocNumber the display layer can
                    // group on (see getLinkedDeliveryNotes / LinkedDeliveryNoteRow).
                    val dnInfo = linkInvoiceDocumentProductToDeliveryNoteQueries
                        .getInfoLinkedToDocumentProduct(it.document_product_id)
                        .executeAsOneOrNull() // DB call
                    val linkedDate: String?
                    val linkedDocNumber: String?
                    if (dnInfo != null) {
                        linkedDate = dnInfo.delivery_date
                        linkedDocNumber = dnInfo.delivery_note_number
                    } else {
                        val qInfo = linkInvoiceDocumentProductToQuoteQueries
                            .getInfoLinkedToDocumentProduct(it.document_product_id)
                            .executeAsOneOrNull() // DB call
                        linkedDate = qInfo?.delivery_date
                        linkedDocNumber = qInfo?.quote_number
                    }
                    documentProductQueries.getDocumentProduct(it.document_product_id)
                        .executeAsOne()// DB call
                        .transformIntoEditableDocumentProduct(
                            linkedDate,
                            linkedDocNumber,
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
        val retentions = fetchRetentions(this.invoice_id)
        return InvoiceState(
            documentId = this.invoice_id.toInt(),
            documentTag = documentTag ?: DocumentTag.DRAFT,
            documentNumber = TextFieldValue(text = this.number ?: ""),
            documentDate = this.issuing_date ?: "",
            reference = this.reference?.let { TextFieldValue(text = it) },
            freeField = this.free_field?.let { TextFieldValue(text = it) },
            documentIssuer = documentClientAndIssuer?.filter { it.type == ClientOrIssuerType.DOCUMENT_ISSUER }?.maxByOrNull { it.id ?: 0 },
            documentClient = documentClientAndIssuer?.filter { it.type == ClientOrIssuerType.DOCUMENT_CLIENT }?.maxByOrNull { it.id ?: 0 },
            documentProducts = documentProducts?.sortedBy { it.sortOrder },
            documentTotalPrices = documentProducts?.let { calculateDocumentPrices(it, retentions) },
            currency = TextFieldValue(this.currency ?: CurrencyManager.DEFAULT_FALLBACK),
            dueDate = this.due_date ?: "",
            paymentStatus = this.payment_status.toInt(),
            footerText = TextFieldValue(text = this.footer ?: ""),
            createdDate = this.created_at,
            watermarkText = this.watermark_text,
            labelsSnapshot = this.labels_snapshot,
            showCurrencyAndAutoTaxColumn = this.show_currency_and_auto_tax_column != 0L,
            formatLocale = this.format_locale,
            hideLinkedSourceHeaders = this.hide_linked_source_headers != 0L,
            paymentMeansSelections = this.payment_means_selections
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?.toSet()
                ?.takeIf { it.isNotEmpty() },
            paymentMeansOtherChecked = this.payment_means_other_checked != 0L,
            paymentMeansSegments = com.a4a.g8invoicing.data.models.parsePaymentLabel(this.payment_means_label),
            paymentMeansHidden = this.payment_means_hidden != 0L,
            paymentBankHidden = this.payment_bank_hidden != 0L,
            paymentBankSegments = com.a4a.g8invoicing.data.models.parsePaymentBankLabel(this.payment_bank_label),
            paymentTermsRecoveryFees = TextFieldValue(text = this.payment_terms_recovery_fees ?: ""),
            paymentTermsLateFees = TextFieldValue(text = this.payment_terms_late_fees ?: ""),
            paymentTermsDiscount = TextFieldValue(text = this.payment_terms_discount ?: ""),
            originalCompanyId = this.original_company_id,
            vatExemptionText = this.vat_exemption_text?.let { TextFieldValue(text = it) },
            fontFamily = this.font_family,
            retentions = retentions,
        )
    }


    // --- convertDeliveryNotesToInvoice ---
    // Uses withContext(Dispatchers.IO).
    override suspend fun convertDeliveryNotesToInvoice(deliveryNotes: List<DeliveryNoteState>): Long? {
        val frozenWatermark = computeWatermark()
        val frozenLabels = DocumentLabels.captureSnapshotJson()
        val newCompanyId = currentCompanyRepository.current
            ?: deliveryNotes.firstOrNull()?.originalCompanyId
        val issuer = deliveryNotes.firstOrNull { it.documentIssuer != null }?.documentIssuer
        // Reuse the previous invoice's BT-120 wording for this master issuer
        // (see createNew for the full rationale on why per-issuer reuse beats
        // re-seeding the country default on every new doc).
        val reusedVatExemptionText = issuer?.originalClientOrIssuerId?.toLong()?.let { masterId ->
            invoiceQueries.getLastInvoicePaymentReuseForIssuer(masterId)
                .executeAsOneOrNull()?.vat_exemption_text
        }
        // Same seed rule as createNew (reuse → country defaults).
        val reusedRetentions: List<com.a4a.g8invoicing.ui.states.RetentionState> =
            if (issuer?.taxWithholdingEnabled == true) {
                issuer.originalClientOrIssuerId?.toLong()?.let { masterId ->
                    invoiceRetentionQueries.getLastInvoiceIdWithRetentionsForIssuer(masterId)
                        .executeAsOneOrNull()?.let { row ->
                            invoiceRetentionQueries.getForInvoice(row.invoice_id)
                                .executeAsList()
                                .map { it.transformIntoRetentionState() }
                        }
                } ?: com.a4a.g8invoicing.data.models.defaultRetentionsForIssuer(
                    issuer,
                    getString(Res.string.retention_default_label),
                    getString(Res.string.retention_default_mx_isr),
                    getString(Res.string.retention_default_mx_iva),
                )
            } else emptyList()
        return withContext(DispatcherProvider.IO) {
            val docNumber = getLastDocumentNumber(newCompanyId)?.let {
                incrementDocumentNumber(it)
            } ?: getString(Res.string.invoice_default_number)

            try {
                val seededTerms = seedPaymentTermsForNewInvoice()
                val newInvoiceState = InvoiceState(
                    documentNumber = TextFieldValue(docNumber),
                    documentDate = DateUtils.getCurrentDateFormatted(),
                    dueDate = DateUtils.getDatePlusDaysFormatted(30),
                    reference = deliveryNotes.firstOrNull { it.reference != null }?.reference,
                    freeField = deliveryNotes.firstOrNull { it.freeField != null }?.freeField,
                    documentIssuer = issuer,
                    documentClient = deliveryNotes.firstOrNull { it.documentClient != null }?.documentClient,
                    currency = TextFieldValue(
                        deliveryNotes.firstOrNull()?.currency?.text?.takeIf { it.isNotEmpty() }
                            ?: currencyManager.currentCurrency
                    ),
                    footerText = TextFieldValue(getExistingFooter() ?: getString(Res.string.document_default_footer)), // DB call
                    watermarkText = frozenWatermark,
                    labelsSnapshot = frozenLabels,
                    showCurrencyAndAutoTaxColumn = true,
                    formatLocale = AppLocaleHolder.languageCode,
                    paymentMeansSelections = setOf(
                        com.a4a.g8invoicing.data.models.PaymentMeans.TRANSFER.chipId,
                        com.a4a.g8invoicing.data.models.PaymentMeans.CHEQUE.chipId,
                        com.a4a.g8invoicing.data.models.PaymentMeans.CASH.chipId,
                    ),
                    paymentMeansSegments = com.a4a.g8invoicing.data.models.defaultPaymentSegments(
                        getString(Res.string.document_payment_means_default_label)
                    ),
                    paymentTermsRecoveryFees = seededTerms.first,
                    paymentTermsLateFees = seededTerms.second,
                    paymentTermsDiscount = seededTerms.third,
                    originalCompanyId = newCompanyId,
                    vatExemptionText = issuer
                        ?.takeIf { it.vatExempt }
                        ?.let {
                            reusedVatExemptionText?.trim()?.takeIf { s -> s.isNotEmpty() }
                                ?: com.a4a.g8invoicing.data.models.defaultVatExemptionText(
                                    it.addresses?.firstOrNull()?.countryCode
                                )
                        }
                        ?.let { TextFieldValue(it) },
                    retentions = reusedRetentions,
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
                    saveRetentionsForInvoice(id, reusedRetentions)
                }
                newInvoiceId
            } catch (e: Exception) {
                //Log.e("InvoiceDS", "Error convertDeliveryNotes: ${e.message}")
                null
            }
        }
    }

    // --- convertQuotesToInvoice ---
    // Mirrors convertDeliveryNotesToInvoice but writes trace rows to
    // LinkInvoiceDocumentProductToQuote so each invoice product remembers
    // which quote it was cloned from. We bypass saveInfoInOtherTables here
    // because we need the returned new-document-product-id to build the trace.
    override suspend fun convertQuotesToInvoice(quotes: List<QuoteState>): Long? {
        val frozenWatermark = computeWatermark()
        val frozenLabels = DocumentLabels.captureSnapshotJson()
        val newCompanyId = currentCompanyRepository.current
            ?: quotes.firstOrNull()?.originalCompanyId
        val issuer = quotes.firstOrNull { it.documentIssuer != null }?.documentIssuer
        // Retention hierarchy on convert:
        //   1. the quote's own retentions (user's most recent intent — they may
        //      have edited label / rate on the quote and expect that to carry
        //      over verbatim to the resulting facture),
        //   2. the last invoice for the same master issuer (reuse across docs),
        //   3. country defaults.
        // Reset ids to null so saveRetentionsForInvoice inserts fresh rows on
        // the new invoice rather than referencing the quote's row ids.
        val quoteRetentions = quotes.firstOrNull { it.retentions.isNotEmpty() }
            ?.retentions
            ?.map { it.copy(id = null) }
        val reusedVatExemptionText = issuer?.originalClientOrIssuerId?.toLong()?.let { masterId ->
            invoiceQueries.getLastInvoicePaymentReuseForIssuer(masterId)
                .executeAsOneOrNull()?.vat_exemption_text
        }
        val reusedRetentions: List<com.a4a.g8invoicing.ui.states.RetentionState> =
            if (issuer?.taxWithholdingEnabled == true) {
                quoteRetentions
                    ?: issuer.originalClientOrIssuerId?.toLong()?.let { masterId ->
                        invoiceRetentionQueries.getLastInvoiceIdWithRetentionsForIssuer(masterId)
                            .executeAsOneOrNull()?.let { row ->
                                invoiceRetentionQueries.getForInvoice(row.invoice_id)
                                    .executeAsList()
                                    .map { it.transformIntoRetentionState() }
                            }
                    }
                    ?: com.a4a.g8invoicing.data.models.defaultRetentionsForIssuer(
                        issuer,
                        getString(Res.string.retention_default_label),
                        getString(Res.string.retention_default_mx_isr),
                        getString(Res.string.retention_default_mx_iva),
                    )
            } else emptyList()
        return withContext(DispatcherProvider.IO) {
            val docNumber = getLastDocumentNumber(newCompanyId)?.let {
                incrementDocumentNumber(it)
            } ?: getString(Res.string.invoice_default_number)

            try {
                val seededTerms = seedPaymentTermsForNewInvoice()
                val newInvoiceState = InvoiceState(
                    documentNumber = TextFieldValue(docNumber),
                    documentDate = DateUtils.getCurrentDateFormatted(),
                    dueDate = DateUtils.getDatePlusDaysFormatted(30),
                    reference = quotes.firstOrNull { it.reference != null }?.reference,
                    freeField = quotes.firstOrNull { it.freeField != null }?.freeField,
                    documentIssuer = issuer,
                    documentClient = quotes.firstOrNull { it.documentClient != null }?.documentClient,
                    currency = TextFieldValue(
                        quotes.firstOrNull()?.currency?.text?.takeIf { it.isNotEmpty() }
                            ?: currencyManager.currentCurrency
                    ),
                    footerText = TextFieldValue(getExistingFooter() ?: getString(Res.string.document_default_footer)),
                    watermarkText = frozenWatermark,
                    labelsSnapshot = frozenLabels,
                    paymentMeansSelections = setOf(
                        com.a4a.g8invoicing.data.models.PaymentMeans.TRANSFER.chipId,
                        com.a4a.g8invoicing.data.models.PaymentMeans.CHEQUE.chipId,
                        com.a4a.g8invoicing.data.models.PaymentMeans.CASH.chipId,
                    ),
                    paymentMeansSegments = com.a4a.g8invoicing.data.models.defaultPaymentSegments(
                        getString(Res.string.document_payment_means_default_label)
                    ),
                    paymentTermsRecoveryFees = seededTerms.first,
                    paymentTermsLateFees = seededTerms.second,
                    paymentTermsDiscount = seededTerms.third,
                    originalCompanyId = newCompanyId,
                    vatExemptionText = issuer
                        ?.takeIf { it.vatExempt }
                        ?.let {
                            reusedVatExemptionText?.trim()?.takeIf { s -> s.isNotEmpty() }
                                ?: com.a4a.g8invoicing.data.models.defaultVatExemptionText(
                                    it.addresses?.firstOrNull()?.countryCode
                                )
                        }
                        ?.let { TextFieldValue(it) },
                    retentions = reusedRetentions,
                )
                saveInfoInInvoiceTable(newInvoiceState)

                val newInvoiceId = invoiceQueries.getLastInsertedRowId().executeAsOneOrNull()
                newInvoiceId?.let { id ->
                    saveRetentionsForInvoice(id, reusedRetentions)
                    val firstQuoteForTag = quotes.first().copy(documentTag = DocumentTag.DRAFT)
                    saveTag(id, firstQuoteForTag)

                    quotes.forEach { quote ->
                        quote.documentProducts?.forEach { documentProduct ->
                            val newDocProductId = saveDocumentProductInDbAndLinkToDocument(
                                documentProduct = documentProduct,
                                documentId = id,
                                deliveryNoteDate = null,
                                deliveryNoteNumber = null,
                            )
                            newDocProductId?.let { pid ->
                                linkInvoiceDocumentProductToQuoteQueries.saveInfoLinkedToDocumentProduct(
                                    document_product_id = pid.toLong(),
                                    quote_number = quote.documentNumber.text,
                                    delivery_date = quote.documentDate,
                                )
                            }
                        }
                        quote.documentClient?.let {
                            saveDocumentClientOrIssuerInDbAndLinkToDocument(
                                documentClientOrIssuer = it,
                                documentId = id,
                            )
                        }
                        quote.documentIssuer?.let {
                            saveDocumentClientOrIssuerInDbAndLinkToDocument(
                                documentClientOrIssuer = it,
                                documentId = id,
                            )
                        }
                    }
                }
                newInvoiceId
            } catch (e: Exception) {
                null
            }
        }
    }

    // --- update ---
    // Uses withContext(Dispatchers.IO).
    override suspend fun update(document: InvoiceState) {
        return withContext(DispatcherProvider.IO) {
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
                    payment_means_selections = document.paymentMeansSelections?.joinToString(","),
                    payment_means_label = com.a4a.g8invoicing.data.models.serializePaymentLabel(document.paymentMeansSegments),
                    payment_means_hidden = if (document.paymentMeansHidden) 1L else 0L,
                    payment_terms_recovery_fees = document.paymentTermsRecoveryFees.text.takeIf { it.isNotEmpty() },
                    payment_terms_late_fees = document.paymentTermsLateFees.text.takeIf { it.isNotEmpty() },
                    payment_terms_discount = document.paymentTermsDiscount.text.takeIf { it.isNotEmpty() },
                    payment_bank_hidden = if (document.paymentBankHidden) 1L else 0L,
                    payment_means_other_checked = if (document.paymentMeansOtherChecked) 1L else 0L,
                    payment_bank_label = com.a4a.g8invoicing.data.models.serializePaymentBankLabel(document.paymentBankSegments),
                    vat_exemption_text = document.vatExemptionText?.text?.trim()?.takeIf { it.isNotEmpty() },
                    font_family = document.fontFamily,
                    updated_at = DateUtils.getCurrentTimestamp()
                )
                document.documentId?.toLong()?.let { id ->
                    saveRetentionsForInvoice(id, document.retentions)
                }
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

    // --- updateHideLinkedSourceHeaders ---
    // Dedicated write so the eye toggle in the doc form doesn't have to round-trip
    // through the full update() (which validates every field).
    override suspend fun updateHideLinkedSourceHeaders(invoiceId: Long, hide: Boolean) {
        withContext(DispatcherProvider.IO) {
            try {
                invoiceQueries.updateHideLinkedSourceHeaders(
                    invoice_id = invoiceId,
                    hide_linked_source_headers = if (hide) 1L else 0L,
                    updated_at = DateUtils.getCurrentTimestamp(),
                )
            } catch (_: Exception) {
            }
        }
    }

    override suspend fun deleteAllRetentions(invoiceId: Long) {
        withContext(DispatcherProvider.IO) {
            try {
                invoiceRetentionQueries.deleteAllForInvoice(invoiceId)
            } catch (_: Exception) {
            }
        }
    }

    override suspend fun saveRetentions(
        invoiceId: Long,
        retentions: List<com.a4a.g8invoicing.ui.states.RetentionState>,
    ) {
        withContext(DispatcherProvider.IO) {
            saveRetentionsForInvoice(invoiceId, retentions)
        }
    }

    // --- duplicate ---
    // Uses withContext(Dispatchers.IO).
    override suspend fun duplicate(documents: List<InvoiceState>): List<Long> {
        // Duplicating creates new docs → each gets a fresh watermark decision based on
        // the CURRENT premium/module state, not whatever was frozen on the source doc.
        // Same logic for labelsSnapshot: a duplicated doc is a new doc, snapshotted in
        // the current locale.
        val frozenWatermark = computeWatermark()
        val frozenLabels = DocumentLabels.captureSnapshotJson()
        return withContext(DispatcherProvider.IO) {
            val createdIds = mutableListOf<Long>()
            try {
                documents.forEach { originalDocument ->
                    // Duplicate keeps the source's company (per-company counter);
                    // fall back to CurrentCompanyRepository if the source was
                    // pre-migration and has no originalCompanyId yet.
                    val docCompanyId = originalDocument.originalCompanyId
                        ?: currentCompanyRepository.current
                    val docNumber = getLastDocumentNumber(docCompanyId)?.let { // DB Call
                        incrementDocumentNumber(it)
                    } ?: getString(Res.string.invoice_default_number)

                    val duplicatedDocumentState = originalDocument.copy(
                        documentNumber = TextFieldValue(docNumber),
                        documentTag = DocumentTag.DRAFT,
                        paymentStatus = 0,
                        // Reset dates to today (+30 for due date) — a duplicated
                        // invoice is a new invoice, and users don't want the
                        // months-old issuing/due dates on their fresh doc.
                        documentDate = DateUtils.getCurrentDateFormatted(),
                        dueDate = DateUtils.getDatePlusDaysFormatted(30),
                        watermarkText = frozenWatermark,
                        labelsSnapshot = frozenLabels,
                        showCurrencyAndAutoTaxColumn = true,
                        formatLocale = AppLocaleHolder.languageCode,
                    )

                    saveInfoInInvoiceTable(duplicatedDocumentState) // DB Call

                    val newInvoiceId = invoiceQueries.getLastInsertedRowId()
                        .executeAsOneOrNull() // Get ID after main insert
                    newInvoiceId?.let { id ->
                        createdIds.add(id)
                        saveTag(id, duplicatedDocumentState) // saveTag is suspend
                        saveInfoInOtherTables(
                            id,
                            originalDocument.copy(documentId = id.toInt())
                        ) // Pass new ID and original document's children context
                    }
                }
            } catch (e: Exception) {
                //Log.e("InvoiceDS", "Error duplicate: ${e.message}")
            }
            createdIds
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
        withContext(DispatcherProvider.IO) {
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
        withContext(DispatcherProvider.IO) {
            try {
                documents.forEach {
                    it.documentId?.toLong()?.let {
                        invoiceQueries.updatePaymentStatus(
                            invoice_id = it,
                            payment_status = if (tag == DocumentTag.PAID) 2 else 0,
                            updated_at = DateUtils.getCurrentTimestamp()
                        )
                    }
                }
            } catch (e: Exception) {
                //Log.e("InvoiceDS", "Error markAsPaid: ${e.message}")
            }
        }
    }

    // --- saveDocumentProductInDbAndLinkToDocument ---
    // Uses withContext(Dispatchers.IO) and transaction.
    override suspend fun saveDocumentProductInDbAndLinkToDocument(
        documentProduct: DocumentProductState,
        documentId: Long, // This is the parent document ID (e.g., invoiceId)
        deliveryNoteDate: String?,
        deliveryNoteNumber: String?,
    ): Int? {
        return withContext(DispatcherProvider.IO) {
            try {
                documentProductQueries.transactionWithResult {
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
        documentId: Long?, // This is the parent document ID (e.g., invoiceId)
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
            // Seed the doc's frozen payment_iban / payment_bic from the first
            // bank the user typed in. Without this the master row saves the
            // bank correctly but the doc's snapshot stays null, so the invoice
            // renders nothing under the "IBAN :" / "BIC :" slot until the user
            // manually picks a bank in the payment-means modal.
            val firstBank = documentClientOrIssuer.banks.firstOrNull()
            val seededIban = firstBank?.identifier?.text?.trim()?.takeIf { it.isNotEmpty() }
                ?.let { TextFieldValue(it) }
            val seededBic = firstBank?.bic?.text?.trim()?.takeIf { it.isNotEmpty() }
                ?.let { TextFieldValue(it) }
            val seededCountry = firstBank?.countryCode?.trim()?.takeIf { it.isNotEmpty() }
            documentClientOrIssuer.copy(
                originalClientOrIssuerId = masterId?.toInt(),
                paymentIban = seededIban ?: documentClientOrIssuer.paymentIban,
                paymentBic = seededBic ?: documentClientOrIssuer.paymentBic,
                paymentCountry = seededCountry ?: documentClientOrIssuer.paymentCountry,
            )
        } else if (
            (documentClientOrIssuer.type == ClientOrIssuerType.ISSUER ||
                documentClientOrIssuer.type == ClientOrIssuerType.DOCUMENT_ISSUER) &&
            documentClientOrIssuer.paymentIban?.text.isNullOrEmpty() &&
            documentClientOrIssuer.banks.isNotEmpty()
        ) {
            // Existing issuer picked for the doc: banks are hydrated on the
            // state but payment_iban/payment_bic haven't been picked yet →
            // default to the first bank so the invoice's "IBAN :" line
            // renders straight away.
            val firstBank = documentClientOrIssuer.banks.first()
            documentClientOrIssuer.copy(
                paymentIban = firstBank.identifier.text.trim().takeIf { it.isNotEmpty() }
                    ?.let { TextFieldValue(it) },
                paymentBic = firstBank.bic.text.trim().takeIf { it.isNotEmpty() }
                    ?.let { TextFieldValue(it) },
                paymentCountry = firstBank.countryCode?.trim()?.takeIf { it.isNotEmpty() },
            )
        } else {
            documentClientOrIssuer
        }

        withContext(DispatcherProvider.IO) { // This IO context is inherited by the suspend call below
            try {
                saveDocumentClientOrIssuerInDbAndLink(
                    documentClientOrIssuerQueries,
                    documentClientOrIssuerAddressQueries,
                    linkDocumentClientOrIssuerToAddressQueries,
                    documentClientOrIssuerEmailQueries,
                    linkInvoiceToDocumentClientOrIssuerQueries,
                    clientOrIssuerToSave,
                    documentId
                )
            } catch (e: Exception) {
                //Log.e("InvoiceDS", "Error saveClientOrIssuerAndLink: ${e.message}")
            }
        }
    }

    // --- delete ---
    // Uses withContext(Dispatchers.IO).
    override suspend fun delete(documents: List<InvoiceState>) {
        withContext(DispatcherProvider.IO) {
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
                    document.documentProducts?.filter { it.id != null }?.let {
                        it.forEach { documentProduct ->
                            deleteDocumentProduct(
                                document.documentId!!.toLong(),
                                documentProduct.id!!.toLong()
                            )
                        }
                    }

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
    // Uses withContext(Dispatchers.IO).
    override suspend fun deleteDocumentProduct(documentId: Long, documentProductId: Long) {
        try {
            return withContext(DispatcherProvider.IO) {
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

    // --- deleteTag ---
    // Uses withContext(Dispatchers.IO).
    override suspend fun deleteTag(invoiceId: Long) {
        try {
            return withContext(DispatcherProvider.IO) {
                linkInvoiceToTagQueries.delete(
                    invoiceId  // DB call
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
                // Fetch the specific client/issuer linked to THIS invoice
                val clientOrIssuerToDelete =
                    fetchClientAndIssuer( // Synchronous, runs on this IO context
                        documentId,
                        linkInvoiceToDocumentClientOrIssuerQueries,
                        linkDocumentClientOrIssuerToAddressQueries,
                        documentClientOrIssuerQueries,
                        documentClientOrIssuerAddressQueries,
                        documentClientOrIssuerEmailQueries
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
            withContext(DispatcherProvider.IO) {
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
                footer = document.footerText.text,
                watermark_text = document.watermarkText,
                labels_snapshot = document.labelsSnapshot,
                show_currency_and_auto_tax_column = if (document.showCurrencyAndAutoTaxColumn) 1L else 0L,
                format_locale = document.formatLocale,
                hide_linked_source_headers = if (document.hideLinkedSourceHeaders) 1L else 0L,
                payment_means_selections = document.paymentMeansSelections?.joinToString(","),
                payment_means_label = com.a4a.g8invoicing.data.models.serializePaymentLabel(document.paymentMeansSegments),
                payment_means_hidden = if (document.paymentMeansHidden) 1L else 0L,
                payment_terms_recovery_fees = document.paymentTermsRecoveryFees.text.takeIf { it.isNotEmpty() },
                payment_terms_late_fees = document.paymentTermsLateFees.text.takeIf { it.isNotEmpty() },
                payment_terms_discount = document.paymentTermsDiscount.text.takeIf { it.isNotEmpty() },
                payment_bank_hidden = if (document.paymentBankHidden) 1L else 0L,
                payment_means_other_checked = if (document.paymentMeansOtherChecked) 1L else 0L,
                payment_bank_label = com.a4a.g8invoicing.data.models.serializePaymentBankLabel(document.paymentBankSegments),
                original_company_id = document.originalCompanyId,
                vat_exemption_text = document.vatExemptionText?.text?.trim()?.takeIf { it.isNotEmpty() },
                font_family = document.fontFamily,
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
    // Pure utility function using DateUtils.
    private fun isPaymentLate(dueDate: String): Boolean {
        return DateUtils.isDateBeforeToday(dueDate)
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
                        linkInvoiceToDocumentProductQueries
                    )
                }
            } catch (e: Exception) {
                // Log.e("InvoiceLocalDataSource", "Error updating document products order in DB: ${e.message}", e)
                throw e // Relance pour que le ViewModel puisse la catcher si nécessaire
            }
        }
    }

    override suspend fun getRecentFootersForCompany(companyId: Long, limit: Int): List<String> {
        return withContext(DispatcherProvider.IO) {
            // The .sq query caps at 10 rows; [limit] is applied client-side so
            // callers can further trim without a new query variant.
            invoiceQueries.getRecentFootersForCompany(companyId)
                .executeAsList()
                .mapNotNull { it }
                .take(limit)
        }
    }

} // End of InvoiceLocalDataSource

// --- Global helper functions (outside the class) ---
// These should ideally be part of a relevant DataSource or utility class

// --- incrementDocumentNumber ---
//  Pure utility function.
fun incrementDocumentNumber(docNumber: String): String {
    val numberToIncrement = docNumber.takeLastWhile { it.isDigit() }
    if (numberToIncrement.isNotEmpty()) {
        val firstPartOfDocNumber = docNumber.substringBeforeLast(numberToIncrement)
        val incremented = (numberToIncrement.toInt() + 1).toString()
        // Pad to the trailing digit block's ORIGINAL length so we preserve
        // whatever zero-padding the user picked:
        //   F00GJ3   → F00GJ4    (1-digit trailer → keep 1 digit)
        //   F00GJ003 → F00GJ004  (3-digit padding preserved)
        //   f00368   → f00369    (5-digit padding preserved)
        // padStart is a no-op when the incremented value already exceeds the
        // original length (e.g. F999 → F1000), so overflow is handled cleanly.
        return firstPartOfDocNumber + incremented.padStart(numberToIncrement.length, '0')
    } else return docNumber
}



// This function performs synchronous DB IO.
// It must be called from a Dispatchers.IO context.
fun saveDocumentProductInDbAndLink(
    documentProductQueries: DocumentProductQueries,
    linkToDocumentProductQueries: Any,
    linkToDeliveryNotesQueries: Any? = null,
    documentProduct: DocumentProductState,
    documentId: Long,
    deliveryNoteDate: String? = null,
    deliveryNoteNumber: String? = null,
): Int? {
    var newDocumentProductId: Long? = null

    // 1. Insert DocumentProduct in db
    documentProductQueries.saveDocumentProduct(  // DB call
        id = null,
        name = documentProduct.name.text,
        quantity = documentProduct.quantity.doubleValue(false),
        description = documentProduct.description?.text,
        price_without_tax = documentProduct.priceWithoutTax?.doubleValue(false),
        tax_rate = documentProduct.taxRate?.doubleValue(false),
        unit = documentProduct.unit?.text,
        unit_code = documentProduct.unitCode,
        type = documentProduct.type?.name,
        product_id = documentProduct.productId?.toLong()
    )
    // Get the id after inserting
    newDocumentProductId = documentProductQueries.getLastInsertedRowId().executeAsOneOrNull()

    if (newDocumentProductId == null) {
        throw IllegalStateException("Failed to insert document product and get its ID.")
    } else {
        // 2. Link DocumentProduct to Document (Invoice, DeliveryNote, etc.)
        linkDocumentProductToParentDocument(
            linkToDocumentProductQueries,
            documentId,
            newDocumentProductId
        )
        // Link to delivery note info if we have at least the delivery note number
        if (!deliveryNoteNumber.isNullOrEmpty() && linkToDeliveryNotesQueries != null) {
            linkDocumentProductToDeliveryNoteInfo(
                linkToDeliveryNotesQueries,
                newDocumentProductId,
                deliveryNoteNumber,
                deliveryNoteDate ?: "",
            )
        }
        return newDocumentProductId.toInt()
    }
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
) {
    try {
        val sortOrder: Long = when (linkQueries) {
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

            is LinkQuoteToDocumentProductQueries -> {
                val result = linkQueries.getMaxSortOrderForQuote(parentId)
                    .executeAsOneOrNull() // DB call
                (result?.maxOrder ?: -1L) + 1L
            }

            else -> {
                //Log.w("GlobalHelpers", "Unsupported query type for linkDocumentProductToParentDocument (sort order): ${linkQueries::class.simpleName}")
                0L // Default sort order if type is unknown, or handle error
            }
        }

        when (linkQueries) {
            is LinkInvoiceToDocumentProductQueries -> {
                linkQueries.saveProductLinkedToInvoice( // DB call
                    id = null, // Auto-incremented
                    invoice_id = parentId,
                    document_product_id = documentProductId,
                    sort_order = sortOrder
                )
            }

            is LinkDeliveryNoteToDocumentProductQueries -> {
                linkQueries.saveProductLinkedToDeliveryNote( // DB call
                    id = null,
                    delivery_note_id = parentId,
                    document_product_id = documentProductId,
                    sort_order = sortOrder
                )
            }

            is LinkCreditNoteToDocumentProductQueries -> {
                linkQueries.saveProductLinkedToCreditNote( // DB call
                    id = null,
                    credit_note_id = parentId,
                    document_product_id = documentProductId,
                    sort_order = sortOrder
                )
            }

            is LinkQuoteToDocumentProductQueries -> {
                linkQueries.saveProductLinkedToQuote( // DB call
                    id = null,
                    quote_id = parentId,
                    document_product_id = documentProductId,
                    sort_order = sortOrder
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
    documentClientOrIssuerEmailQueries: DocumentClientOrIssuerEmailQueries,
    linkQueries: Any,
    documentClientOrIssuer: ClientOrIssuerState,
    documentId: Long?,
) {
    saveDocumentClientOrIssuer(
        documentClientOrIssuerQueries,
        documentClientOrIssuerAddressQueries,
        linkDocumentClientOrIssuerToAddressQueries,
        documentClientOrIssuerEmailQueries,
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

            is LinkQuoteToDocumentClientOrIssuerQueries -> {
                linkQueries.saveDocumentClientOrIssuerLinkedToQuote(
                    id = null,
                    quote_id = documentId,
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
    documentClientOrIssuerEmailQueries: DocumentClientOrIssuerEmailQueries,
    clientOrIssuerState: ClientOrIssuerState,
): Long? { // Return the ID of the saved client/issuer
    return withContext(DispatcherProvider.IO) {
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
                saveInfoInDocumentClientOrIssuerEmailTable(
                    documentClientOrIssuerEmailQueries,
                    id,
                    clientOrIssuerState.emails
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
        original_client_id = documentClientOrIssuer.originalClientOrIssuerId?.toLong(),
        original_version = documentClientOrIssuer.originalVersion?.toLong(),
        first_name = documentClientOrIssuer.firstName?.text,
        name = documentClientOrIssuer.name.text,
        phone = documentClientOrIssuer.phone?.text,
        email = documentClientOrIssuer.emails?.firstOrNull()?.email?.text,
        notes = documentClientOrIssuer.notes?.text,
        company_id1_label = documentClientOrIssuer.companyId1Label?.text,
        company_id1_number = documentClientOrIssuer.companyId1Number?.text,
        company_id2_label = documentClientOrIssuer.companyId2Label?.text,
        company_id2_number = documentClientOrIssuer.companyId2Number?.text,
        company_id3_label = documentClientOrIssuer.companyId3Label?.text,
        company_id3_number = documentClientOrIssuer.companyId3Number?.text,
        logo_path = documentClientOrIssuer.logoPath,
        vat_exempt = if (documentClientOrIssuer.vatExempt) 1L else 0L,
        intra_eu_sales = if (documentClientOrIssuer.intraEuSales) 1L else 0L,
        payment_iban = documentClientOrIssuer.paymentIban?.text?.trim(),
        payment_bic = documentClientOrIssuer.paymentBic?.text?.trim(),
        payment_country = documentClientOrIssuer.paymentCountry?.trim()?.ifEmpty { null },
        // Frozen only on the client-side snapshot; issuers keep NULL. Drives
        // the Factur-X export gate deterministically per doc.
        client_type = if (documentClientOrIssuer.type == ClientOrIssuerType.CLIENT ||
            documentClientOrIssuer.type == ClientOrIssuerType.DOCUMENT_CLIENT
        ) documentClientOrIssuer.clientType?.name else null,
        tax_withholding_enabled = if (documentClientOrIssuer.taxWithholdingEnabled) 1L else 0L,
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
    addresses?.forEachIndexed { index, address ->
        // Skip rows on slots ≥ 2 without meaningful content. Country doesn't
        // count on those — the form auto-seeds it (LaunchedEffect + address
        // value-typing handlers that seed defaultCountryCode on any fresh
        // AddressState), so a country-only slot ≥ 2 is almost always a ghost
        // slot (user tapped "+ Ajouter une adresse" twice and only filled
        // slot 3 → slot 2 is just the auto-country). Slot 0 is always kept:
        // every issuer/client must carry at least one address (country is
        // required for Factur-X / EN 16931). Mirrors the master-side
        // isAddressEmpty in ClientOrIssuerLocalDataSource.
        val hasContent = !address.addressTitle?.text.isNullOrBlank() ||
            !address.addressLine1?.text.isNullOrBlank() ||
            !address.addressLine2?.text.isNullOrBlank() ||
            !address.zipCode?.text.isNullOrBlank() ||
            !address.city?.text.isNullOrBlank()
        if (index > 0 && !hasContent) return@forEachIndexed
        // 1: Save address
        documentClientOrIssuerAddressQueries.save( // DB call
            id = null,
            original_address_id = address.originalAddressId?.toLong(),
            address_title = address.addressTitle?.text,
            address_line_1 = address.addressLine1?.text,
            address_line_2 = address.addressLine2?.text,
            zip_code = address.zipCode?.text,
            city = address.city?.text,
            country_code = address.countryCode,
        )

        // 2: Link address to client/issuer
        documentClientOrIssuerAddressQueries.getLastInsertedRowId().executeAsOneOrNull()
            ?.let { newAddressId ->
                linkDocumentClientOrIssuerToAddressQueries.save( // DB call
                    id = null,
                    document_client_or_issuer_id = documentClientOrIssuerId,
                    address_id = newAddressId)
            }
    }
}

// Synchronous private helper, performs DB IO.
// Must be called from a Dispatchers.IO context.
private fun saveInfoInDocumentClientOrIssuerEmailTable(
    documentClientOrIssuerEmailQueries: DocumentClientOrIssuerEmailQueries,
    documentClientOrIssuerId: Long,
    emails: List<EmailState>?,
) {
    emails?.forEach { email ->
        if (email.email.text.isNotEmpty()) {
            documentClientOrIssuerEmailQueries.save(
                id = null,
                document_client_or_issuer_id = documentClientOrIssuerId,
                email = email.email.text.trim()
            )
        }
    }
}

// Pure transformation function for DocumentClientOrIssuer.
fun DocumentClientOrIssuer.transformIntoEditable(
    addresses: List<AddressState>? = null,
    emails: List<EmailState>? = null,
): ClientOrIssuerState {
    val documentClientOrIssuer = this

    return ClientOrIssuerState(
        id = documentClientOrIssuer.id.toInt(),
        type = if (documentClientOrIssuer.type == ClientOrIssuerType.CLIENT.name.lowercase())
            ClientOrIssuerType.DOCUMENT_CLIENT
        else ClientOrIssuerType.DOCUMENT_ISSUER,
        originalClientOrIssuerId = documentClientOrIssuer.original_client_id?.toInt(),
        originalVersion = documentClientOrIssuer.original_version?.toInt(),
        firstName = documentClientOrIssuer.first_name?.let { TextFieldValue(text = it) },
        addresses = addresses,
        name = TextFieldValue(text = documentClientOrIssuer.name),
        phone = documentClientOrIssuer.phone?.let { TextFieldValue(text = it) },
        emails = emails,
        notes = documentClientOrIssuer.notes?.let { TextFieldValue(text = it) },
        companyId1Label = documentClientOrIssuer.company_id1_label?.let {
            TextFieldValue(text = it)
        },
        companyId1Number = documentClientOrIssuer.company_id1_number?.let { TextFieldValue(text = it) },
        companyId2Label = documentClientOrIssuer.company_id2_label?.let {
            TextFieldValue(text = it)
        },
        companyId2Number = documentClientOrIssuer.company_id2_number?.let { TextFieldValue(text = it) },
        companyId3Label = documentClientOrIssuer.company_id3_label?.let {
            TextFieldValue(text = it)
        },
        companyId3Number = documentClientOrIssuer.company_id3_number?.let { TextFieldValue(text = it) },
        logoPath = documentClientOrIssuer.logo_path,
        vatExempt = (documentClientOrIssuer.vat_exempt ?: 0L) != 0L,
        intraEuSales = (documentClientOrIssuer.intra_eu_sales ?: 0L) != 0L,
        paymentIban = documentClientOrIssuer.payment_iban?.let { TextFieldValue(text = it) },
        paymentBic = documentClientOrIssuer.payment_bic?.let { TextFieldValue(text = it) },
        paymentCountry = documentClientOrIssuer.payment_country,
        clientType = com.a4a.g8invoicing.data.models.ClientType.fromDb(documentClientOrIssuer.client_type),
        taxWithholdingEnabled = documentClientOrIssuer.tax_withholding_enabled != 0L,
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
    documentClientOrIssuerEmailQueries: DocumentClientOrIssuerEmailQueries? = null,
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
        else if (linkQueries is LinkQuoteToDocumentClientOrIssuerQueries)
            linkQueries.getDocumentClientOrIssuerLinkedToQuote(
                documentId
            ).executeAsList().map { it.document_client_or_issuer_id }
        else emptyList()


        val clientAndIssuer: MutableList<ClientOrIssuerState> = mutableListOf()
        listOfIds.forEach {
            val documentClientOrIssuer = documentClientOrIssuerQueries.get(it)
                .executeAsOneOrNull()?.let {
                    it.transformIntoEditable(
                        addresses = fetchDocumentClientOrIssuerAddresses(
                            it.id,
                            linkAddressQueries,
                            documentClientOrIssuerAddressQueries
                        )?.toMutableList(),
                        emails = documentClientOrIssuerEmailQueries?.let { emailQueries ->
                            fetchDocumentClientOrIssuerEmails(it.id, emailQueries)
                        }
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

// Synchronous function, performs DB access.
// Must be called from a Dispatchers.IO context.
fun fetchDocumentClientOrIssuerEmails(
    documentClientOrIssuerId: Long,
    documentClientOrIssuerEmailQueries: DocumentClientOrIssuerEmailQueries,
): List<EmailState>? {
    try {
        val emails = documentClientOrIssuerEmailQueries.getByDocumentClientOrIssuerId(documentClientOrIssuerId)
            .executeAsList()
        return if (emails.isNotEmpty()) {
            emails.map { it.transformIntoEditable() }
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
    linkQueries: Any,
) {
    orderedProducts.forEach { documentProduct ->
        val documentProductId = documentProduct.id?.toLong()
        val newSortOrder = documentProduct.sortOrder?.toLong()

        if (documentProductId != null) {
            when (linkQueries) {
                is LinkInvoiceToDocumentProductQueries -> {
                    try {
                        linkQueries.updateSortOrderForDocumentProduct( // DB Call
                            sort_order = newSortOrder,
                            id = documentId,
                            document_product_id = documentProductId
                        )
                    } catch (e: Exception) {
                        // Error updating sort order
                    }

                }

                is LinkDeliveryNoteToDocumentProductQueries -> {
                    linkQueries.updateSortOrderForDocumentProduct( // DB Call
                        sort_order = newSortOrder,
                        id = documentId,
                        document_product_id = documentProductId
                    )
                }

                is LinkCreditNoteToDocumentProductQueries -> {
                    linkQueries.updateSortOrderForDocumentProduct( // DB Call
                        sort_order = newSortOrder,
                        id = documentId,
                        document_product_id = documentProductId
                    )
                }

                is LinkQuoteToDocumentProductQueries -> {
                    linkQueries.updateSortOrderForDocumentProduct( // DB Call
                        sort_order = newSortOrder,
                        id = documentId,
                        document_product_id = documentProductId
                    )
                }

                else -> {
                    // Unsupported document type for updating sort order
                }
            }
        } else {
            // Product ID is null, cannot update sort order
        }
    }
}

fun calculateDocumentPrices(
    products: List<DocumentProductState>,
    retentions: List<com.a4a.g8invoicing.ui.states.RetentionState> = emptyList(),
): DocumentTotalPrices {
    val totalPriceWithoutTax = products
        .filter { it.priceWithoutTax != null }
        .fold(BigDecimal.ZERO) { acc, item ->
            acc + (item.priceWithoutTax!! * item.quantity)
        }
        .roundToDigitPositionAfterDecimalPoint(2, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)

    // Calculate the total amount of each tax
    val groupedItems = products.groupBy { it.taxRate }
    val taxes = groupedItems.keys.filterNotNull().distinct() // ex: taxes= [10, 20]

    val amountsPerTaxRate: MutableList<Pair<BigDecimal, BigDecimal>> = mutableListOf()
    for (taxRate in taxes) {
        val productsWithThisTax = groupedItems[taxRate] ?: continue
        val sumOfAmounts = productsWithThisTax
            .filter { it.priceWithoutTax != null }
            .fold(BigDecimal.ZERO) { acc, item ->
                acc + (item.priceWithoutTax!! * item.quantity * taxRate / BigDecimal.fromInt(100))
            }
            .roundToDigitPositionAfterDecimalPoint(2, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
        amountsPerTaxRate.add(Pair(taxRate, sumOfAmounts))
    } // ex: amountsPerTaxRate = [(20.0, 7.2), (10.0, 2.4)]

    // Retention amounts: rate% × untaxed base. Deducted from (base+VAT) at
    // the end — VAT is computed on the untouched base (Spanish convention).
    val visibleRetentions = retentions.filter { !it.hidden }
    val retentionAmounts = visibleRetentions.map { r ->
        val amt = (totalPriceWithoutTax * r.rate / BigDecimal.fromInt(100))
            .roundToDigitPositionAfterDecimalPoint(2, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
        com.a4a.g8invoicing.ui.states.RetentionLine(
            label = r.label.text.ifBlank { "Retención" },
            rate = r.rate,
            amount = amt,
        )
    }
    val totalRetentions = retentionAmounts.fold(BigDecimal.ZERO) { acc, line -> acc + line.amount }

    val grossWithTax = totalPriceWithoutTax +
        amountsPerTaxRate.fold(BigDecimal.ZERO) { acc, pair -> acc + pair.second }

    return DocumentTotalPrices(
        totalPriceWithoutTax = totalPriceWithoutTax,
        totalAmountsOfEachTax = amountsPerTaxRate,
        totalPriceWithTax = grossWithTax - totalRetentions,
        retentionAmounts = retentionAmounts,
        totalRetentions = totalRetentions,
    )
}
