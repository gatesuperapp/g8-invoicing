package com.a4a.g8invoicing.data


import androidx.compose.ui.text.input.TextFieldValue
import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.util.DateUtils
import com.a4a.g8invoicing.data.util.DispatcherProvider
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.document_payment_means_default_label
import com.a4a.g8invoicing.shared.resources.payment_terms_discount_default
import com.a4a.g8invoicing.shared.resources.payment_terms_late_fees_default
import com.a4a.g8invoicing.shared.resources.payment_terms_recovery_fees_default
import com.a4a.g8invoicing.shared.resources.quote_default_footer
import com.a4a.g8invoicing.shared.resources.quote_default_number
import com.a4a.g8invoicing.shared.resources.invoice_watermark_default
import com.a4a.g8invoicing.shared.resources.retention_default_label
import com.a4a.g8invoicing.shared.resources.retention_default_mx_isr
import com.a4a.g8invoicing.shared.resources.retention_default_mx_iva
import com.a4a.g8invoicing.data.auth.ActivatedModulesRepository
import com.a4a.g8invoicing.data.auth.SubscriptionRepository
import com.a4a.g8invoicing.data.models.TagUpdateOrCreationCase
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import org.jetbrains.compose.resources.getString
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.screens.shared.DocumentLabels
import com.a4a.g8invoicing.ui.states.QuoteState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.RetentionState
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import g8invoicing.Quote
import g8invoicing.QuoteRetention
import g8invoicing.DocumentClientOrIssuer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class QuoteLocalDataSource(
    db: Database,
    private val clientOrIssuerDataSource: ClientOrIssuerLocalDataSourceInterface,
    private val activatedModules: ActivatedModulesRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val currencyManager: CurrencyManager,
    private val currentCompanyRepository: CurrentCompanyRepository,
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
    private val quoteTagQueries = db.quoteTagQueries
    private val linkQuoteToTagQueries = db.linkQuoteToTagQueries
    private val quoteRetentionQueries = db.quoteRetentionQueries

    // Freeze watermark at creation; see InvoiceLocalDataSource.computeWatermark for rationale.
    private suspend fun computeWatermark(): String? {
        return if (activatedModules.isActive(ActivatedModulesRepository.MODULE_WATERMARK_REMOVAL)) null
        else getString(Res.string.invoice_watermark_default)
    }

    // --- createNew ---
    // Called from ViewModel
    // This function performs DB operations, so it needs Dispatchers.IO.
    override suspend fun createNew(): Long? {
        // Résout l'entreprise courante (menu latéral). Fallback getLastIssuer()
        // pour les installs sans Settings hydratée (sécurité post-migration).
        val currentCompanyId = currentCompanyRepository.current
        val existingIssuer = currentCompanyId
            ?.let { clientOrIssuerDataSource.getCurrentIssuer(it) }
            ?: clientOrIssuerDataSource.getLastIssuer()
        val frozenWatermark = computeWatermark()
        val frozenLabels = DocumentLabels.captureSnapshotJson()

        // Per-issuer reuse: pull payment_means / payment_bank / selections from
        // the most recent quote for the same master issuer, so a new quote
        // inherits whatever the user last set on THIS company. Mirror of
        // InvoiceLocalDataSource.createNew — see the rationale + language guard
        // notes there. Null when no prior quote matches → falls back to
        // defaults. Retentions have their own query below because the schema
        // is a separate table.
        val reuse = existingIssuer?.originalClientOrIssuerId?.toLong()?.let { masterId ->
            quoteQueries.getLastQuotePaymentReuseForIssuer(masterId)
                .executeAsOneOrNull()
        }

        // Reuse retentions from the most recent quote for this master issuer;
        // fall back to country defaults. Mirrors the invoice / credit-note
        // seed path so a devis for a retention-eligible issuer (MX, ES…)
        // previews the same withholding lines the eventual facture will carry.
        val reusedRetentions: List<RetentionState> =
            if (existingIssuer?.taxWithholdingEnabled == true) {
                existingIssuer.originalClientOrIssuerId?.toLong()?.let { masterId ->
                    quoteRetentionQueries.getLastQuoteIdWithRetentionsForIssuer(masterId)
                        .executeAsOneOrNull()?.let { row ->
                            quoteRetentionQueries.getForQuote(row.quote_id)
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

            // Language guard for every reused text field. Chip IDs and the
            // "Autre" boolean carry over regardless — only the label prose
            // (payment_means_label prefix + payment_bank_label) needs to
            // match the app locale, otherwise a user who switched from FR
            // to EN would land French wording on a doc they're now writing
            // in English. Mirror of InvoiceLocalDataSource.
            val currentAppLocale = AppLocaleHolder.languageCode
            val reuseSameLanguage = reuse?.format_locale == currentAppLocale
            val reusableWithLang = reuse.takeIf { reuseSameLanguage }

            val reusedSelections = reuse?.payment_means_selections
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?.toSet()
                ?.takeIf { it.isNotEmpty() }
            val reusedSegments = reusableWithLang?.payment_means_label?.let {
                com.a4a.g8invoicing.data.models.parsePaymentLabel(it)
            }?.takeIf { it.isNotEmpty() }
            // Bank label: reuse under the same language guard, then adapt
            // tokens to the current bank's country class (IBAN vs domestic
            // account number). "Current country" = the master issuer's first
            // bank, which is what the doc-side snapshot will freeze onto.
            val currentBankCountry = existingIssuer?.banks?.firstOrNull()?.countryCode
            val reusedBankSegments = reusableWithLang?.payment_bank_label?.let {
                val parsed = com.a4a.g8invoicing.data.models.parsePaymentBankLabel(it)
                com.a4a.g8invoicing.data.models.adaptPaymentBankSegmentsForCountry(
                    segments = parsed,
                    previousCountry = reusableWithLang.payment_bank_country,
                    currentCountry = currentBankCountry,
                )
            }?.takeIf { it.isNotEmpty() }

            val newQuoteState = QuoteState(
                documentNumber = TextFieldValue(getLastDocumentNumber(currentCompanyId)?.let {
                    incrementDocumentNumber(it)
                } ?: getString(Res.string.quote_default_number)),
                documentDate = todayFormatted,
                documentIssuer = existingIssuer,
                currency = TextFieldValue(currencyManager.currentCurrency),
                footerText = TextFieldValue(getExistingFooter() ?: getString(Res.string.quote_default_footer)),
                watermarkText = frozenWatermark,
                labelsSnapshot = frozenLabels,
                showCurrencyAndAutoTaxColumn = true,
                formatLocale = AppLocaleHolder.languageCode,
                originalCompanyId = currentCompanyId
                    ?: existingIssuer?.originalClientOrIssuerId?.toLong(),
                retentions = reusedRetentions,
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
                // Payment-terms rows aren't surfaced on Quote anymore (see
                // DocumentBottomSheetElementsContent — invoice-only entry).
                // Kept populated with localised defaults so the DB columns
                // aren't null; harmless dead data until / unless the feature
                // is restored on devis.
                paymentTermsRecoveryFees = TextFieldValue(
                    getString(Res.string.payment_terms_recovery_fees_default)
                ),
                paymentTermsLateFees = TextFieldValue(
                    getString(Res.string.payment_terms_late_fees_default)
                ),
                paymentTermsDiscount = TextFieldValue(
                    getString(Res.string.payment_terms_discount_default)
                ),
                // BT-120 seeded from the issuer's country via the shared
                // resolver (same helper the Invoice / CreditNote paths use).
                // Now also passes previousVatText / previousIssuerCountry
                // from the last-quote reuse row, so an issuer's second
                // devis keeps whatever wording the user typed on the first.
                vatExemptionText = com.a4a.g8invoicing.data.models.resolveVatExemptionForNewDoc(
                    issuer = existingIssuer,
                    previousVatText = reuse?.vat_exemption_text,
                    previousIssuerCountry = reuse?.issuer_country_code,
                ),
            )

            saveInfoInDocumentTable(newQuoteState)

            var newQuoteId = quoteQueries.getLastInsertedRowId().executeAsOneOrNull()

            newQuoteId?.let { id ->
                saveInfoInOtherTables(id, newQuoteState)
                saveTag(id, newQuoteState.documentTag)
                saveRetentionsForQuote(id, reusedRetentions)
            }
            newQuoteId
        }
    }

    // --- Synchronous private helpers for createNew (called from Dispatchers.IO context) ---
    // companyId non-null → the new quote's number continues that entreprise's
    // counter. Null falls back to the global counter (pre-migration safety).
    private fun getLastDocumentNumber(companyId: Long?): String? {
        try {
            return if (companyId != null) {
                quoteQueries.getLastQuoteNumberForCompany(companyId).executeAsOneOrNull()?.number
            } else {
                quoteQueries.getLastQuoteNumber().executeAsOneOrNull()?.number
            }
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
                            hydrateBanksOnDocIssuer(
                                fetchClientAndIssuer(
                                    it.quote_id,
                                    linkQuoteToDocumentClientOrIssuerQueries,
                                    linkDocumentClientOrIssuerToAddressQueries,
                                    documentClientOrIssuerQueries,
                                    documentClientOrIssuerAddressQueries,
                                    documentClientOrIssuerEmailQueries
                                )
                            ),
                            fetchTag(it.quote_id),
                            fetchRetentions(it.quote_id),
                        )
                    }
            } catch (e: Exception) {
                //Log.e(ContentValues.TAG, "Error: ${e.message}")
                null
            }
        }
    }

    // See InvoiceLocalDataSource.hydrateBanksOnDocIssuer. The document-side
    // ClientOrIssuer snapshot only carries the doc-frozen columns; the
    // (potentially multiple) IssuerBank rows live on the master issuer and
    // need to be hydrated here so the bottom-sheet "Éditer émetteur" form
    // shows the IBAN/BIC section pre-filled instead of empty.
    private suspend fun hydrateBanksOnDocIssuer(
        states: List<com.a4a.g8invoicing.ui.states.ClientOrIssuerState>?,
    ): List<com.a4a.g8invoicing.ui.states.ClientOrIssuerState>? = states?.map { state ->
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
    override fun fetchAll(): Flow<List<QuoteState>>? {
        try {
            return currentCompanyRepository.state.flatMapLatest { companyId ->
                val query = if (companyId != null) {
                    quoteQueries.getAllForCompany(companyId)
                } else {
                    quoteQueries.getAll()
                }
                query.asFlow().map { rows ->
                    rows.executeAsList()
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
                                clientAndIssuer,
                                fetchTag(document.quote_id),
                                fetchRetentions(document.quote_id),
                            )
                        }
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

    // --- fetchTag ---
    // Synchronous private helper, performs DB IO.
    // Called from a Dispatchers.IO context.
    private fun fetchTag(documentId: Long): DocumentTag? {
        try {
            val tagId = linkQuoteToTagQueries.getQuoteTag(documentId)
                .executeAsOneOrNull()?.tag_id
            tagId?.let {
                quoteTagQueries.getTag(it).executeAsOneOrNull()?.let { tagName ->
                    return enumValueOf<DocumentTag>(tagName)
                }
            }
        } catch (e: Exception) {
            //Log.e("QuoteDS", "Error fetchTag for documentId $documentId: ${e.message}")
        }
        return null
    }

    // --- transformIntoEditableQuote ---
    // Pure transformation function, no IO, no suspend/withContext needed.
    private fun Quote.transformIntoEditableQuote(
        documentProducts: MutableList<DocumentProductState>? = null,
        documentClientAndIssuer: List<ClientOrIssuerState>? = null,
        documentTag: DocumentTag? = null,
        retentions: List<RetentionState> = emptyList(),
    ): QuoteState {
        this.let {
            return QuoteState(
                documentId = it.quote_id.toInt(),
                documentTag = documentTag ?: DocumentTag.DRAFT,
                documentNumber = TextFieldValue(text = it.number ?: ""),
                documentDate = it.delivery_date ?: "",
                reference = TextFieldValue(text = it.reference ?: ""),
                freeField = TextFieldValue(text = it.free_field ?: ""),
                documentIssuer = documentClientAndIssuer?.filter { it.type == ClientOrIssuerType.DOCUMENT_ISSUER }?.maxByOrNull { it.id ?: 0 },
                documentClient = documentClientAndIssuer?.filter { it.type == ClientOrIssuerType.DOCUMENT_CLIENT }?.maxByOrNull { it.id ?: 0 },
                documentProducts = documentProducts?.sortedBy { it.sortOrder },
                documentTotalPrices = documentProducts?.let { calculateDocumentPrices(it, retentions) },
                currency = TextFieldValue(it.currency ?: CurrencyManager.DEFAULT_FALLBACK),
                footerText = TextFieldValue(text = it.footer ?: ""),
                createdDate = it.created_at,
                watermarkText = it.watermark_text,
                labelsSnapshot = it.labels_snapshot,
                showCurrencyAndAutoTaxColumn = it.show_currency_and_auto_tax_column != 0L,
                formatLocale = it.format_locale,
                originalCompanyId = it.original_company_id,
                fontFamily = it.font_family,
                retentions = retentions,
                paymentMeansSelections = it.payment_means_selections
                    ?.split(",")
                    ?.map { code -> code.trim() }
                    ?.filter { code -> code.isNotEmpty() }
                    ?.toSet()
                    ?.takeIf { set -> set.isNotEmpty() },
                paymentMeansOtherChecked = it.payment_means_other_checked != 0L,
                paymentMeansSegments = com.a4a.g8invoicing.data.models.parsePaymentLabel(it.payment_means_label),
                paymentMeansHidden = it.payment_means_hidden != 0L,
                paymentBankHidden = it.payment_bank_hidden != 0L,
                paymentBankSegments = com.a4a.g8invoicing.data.models.parsePaymentBankLabel(it.payment_bank_label),
                paymentTermsRecoveryFees = TextFieldValue(text = it.payment_terms_recovery_fees ?: ""),
                paymentTermsLateFees = TextFieldValue(text = it.payment_terms_late_fees ?: ""),
                paymentTermsDiscount = TextFieldValue(text = it.payment_terms_discount ?: ""),
                vatExemptionText = it.vat_exemption_text?.let { TextFieldValue(text = it) },
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
                    font_family = document.fontFamily,
                    payment_means_selections = document.paymentMeansSelections?.joinToString(","),
                    payment_means_label = com.a4a.g8invoicing.data.models.serializePaymentLabel(document.paymentMeansSegments),
                    payment_means_hidden = if (document.paymentMeansHidden) 1L else 0L,
                    payment_means_other_checked = if (document.paymentMeansOtherChecked) 1L else 0L,
                    payment_terms_recovery_fees = document.paymentTermsRecoveryFees.text,
                    payment_terms_late_fees = document.paymentTermsLateFees.text,
                    payment_terms_discount = document.paymentTermsDiscount.text,
                    payment_bank_hidden = if (document.paymentBankHidden) 1L else 0L,
                    payment_bank_label = com.a4a.g8invoicing.data.models.serializePaymentBankLabel(document.paymentBankSegments),
                    vat_exemption_text = document.vatExemptionText?.text,
                    updated_at = DateUtils.getCurrentTimestamp()
                )
                document.documentId?.toLong()?.let { id ->
                    saveRetentionsForQuote(id, document.retentions)
                }
            } catch (e: Exception) {
                //Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun deleteAllRetentions(quoteId: Long) {
        withContext(DispatcherProvider.IO) {
            try {
                quoteRetentionQueries.deleteAllForQuote(quoteId)
            } catch (_: Exception) {
            }
        }
    }

    override suspend fun saveRetentions(
        quoteId: Long,
        retentions: List<RetentionState>,
    ) {
        withContext(DispatcherProvider.IO) {
            saveRetentionsForQuote(quoteId, retentions)
        }
    }

    // Wipe + re-insert on save; N is tiny (1-3 rows) so per-row diffing isn't
    // worth the bookkeeping. Mirrors InvoiceLocalDataSource.saveRetentionsForInvoice.
    private fun saveRetentionsForQuote(
        quoteId: Long,
        retentions: List<RetentionState>,
    ) {
        try {
            quoteRetentionQueries.deleteAllForQuote(quoteId)
            retentions.forEachIndexed { index, r ->
                quoteRetentionQueries.save(
                    id = null,
                    quote_id = quoteId,
                    label = r.label.text,
                    rate = r.rate.doubleValue(false),
                    sort_order = index.toLong(),
                    hidden = if (r.hidden) 1L else 0L,
                )
            }
        } catch (_: Exception) {
        }
    }

    private fun fetchRetentions(quoteId: Long): List<RetentionState> {
        return try {
            quoteRetentionQueries.getForQuote(quoteId)
                .executeAsList()
                .map { it.transformIntoRetentionState() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun QuoteRetention.transformIntoRetentionState(): RetentionState =
        RetentionState(
            id = this.id.toInt(),
            label = TextFieldValue(this.label),
            rate = BigDecimal.parseString(this.rate.toString()),
            sortOrder = this.sort_order?.toInt() ?: 0,
            hidden = this.hidden != 0L,
        )

    // --- duplicate ---
    // Uses withContext(Dispatchers.IO).
    override suspend fun duplicate(documents: List<QuoteState>) {
        val frozenWatermark = computeWatermark()
        val frozenLabels = DocumentLabels.captureSnapshotJson()
        withContext(DispatcherProvider.IO) {
            try {
                documents.forEach { originalDocument ->
                    // Duplicate keeps the source's company (per-company counter);
                    // fall back to current if the source predates the migration.
                    val docCompanyId = originalDocument.originalCompanyId
                        ?: currentCompanyRepository.current
                    val docNumber = getLastDocumentNumber(docCompanyId)?.let {
                        incrementDocumentNumber(it)
                    } ?: getString(Res.string.quote_default_number)

                    val duplicatedDocumentState = originalDocument.copy(
                        documentNumber = TextFieldValue(docNumber),
                        // Reset the issue date to today — a duplicated quote is
                        // a new quote; users don't want the old date.
                        documentDate = DateUtils.getCurrentDateFormatted(),
                        watermarkText = frozenWatermark,
                        labelsSnapshot = frozenLabels,
                        showCurrencyAndAutoTaxColumn = true,
                        formatLocale = AppLocaleHolder.languageCode,
                    )

                    saveInfoInDocumentTable(duplicatedDocumentState)

                    val newQuoteId = quoteQueries.getLastInsertedRowId()
                        .executeAsOneOrNull()

                    newQuoteId?.let { id ->
                        saveInfoInOtherTables(
                            id,
                            duplicatedDocumentState
                        )
                        saveTag(id, DocumentTag.DRAFT)
                        saveRetentionsForQuote(id, duplicatedDocumentState.retentions)
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
            // Seed doc-frozen payment_iban/payment_bic from the first bank
            // (see InvoiceLocalDataSource for rationale).
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

                    // Delete linked tag
                    linkQuoteToTagQueries.delete(document.documentId!!.toLong())

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
                show_currency_and_auto_tax_column = if (document.showCurrencyAndAutoTaxColumn) 1L else 0L,
                format_locale = document.formatLocale,
                original_company_id = document.originalCompanyId,
                font_family = document.fontFamily,
                payment_means_selections = document.paymentMeansSelections?.joinToString(","),
                payment_means_label = com.a4a.g8invoicing.data.models.serializePaymentLabel(document.paymentMeansSegments),
                payment_means_hidden = if (document.paymentMeansHidden) 1L else 0L,
                payment_means_other_checked = if (document.paymentMeansOtherChecked) 1L else 0L,
                payment_terms_recovery_fees = document.paymentTermsRecoveryFees.text,
                payment_terms_late_fees = document.paymentTermsLateFees.text,
                payment_terms_discount = document.paymentTermsDiscount.text,
                payment_bank_hidden = if (document.paymentBankHidden) 1L else 0L,
                payment_bank_label = com.a4a.g8invoicing.data.models.serializePaymentBankLabel(document.paymentBankSegments),
                vat_exemption_text = document.vatExemptionText?.text,
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

    // --- setTag ---
    // Public entry-point called by the ViewModel when the user picks a tag
    // in the bottom-bar picker, or by the auto-tag flow after a quote has
    // been converted to an invoice.
    override suspend fun setTag(
        documents: List<QuoteState>,
        tag: DocumentTag,
        tagUpdateCase: TagUpdateOrCreationCase,
    ) {
        withContext(DispatcherProvider.IO) {
            try {
                documents.forEach { quote ->
                    quote.documentId?.toLong()?.let { quoteId ->
                        linkDocumentToDocumentTag(
                            quoteId,
                            newTag = tag,
                            updateCase = tagUpdateCase,
                        )
                    }
                }
            } catch (e: Exception) {
                //Log.e("QuoteDS", "Error setTag: ${e.message}")
            }
        }
    }

    // Upsert on quote_id — same rationale as DeliveryNoteLocalDataSource's
    // linkDocumentToDocumentTag: check-then-branch handles both fresh quotes
    // and pre-migration quotes with no junction row.
    private suspend fun linkDocumentToDocumentTag(
        documentId: Long,
        newTag: DocumentTag,
        @Suppress("UNUSED_PARAMETER") updateCase: TagUpdateOrCreationCase,
    ) {
        try {
            withContext(DispatcherProvider.IO) {
                val tagId = quoteTagQueries.getTagId(newTag.name)
                    .executeAsOneOrNull() ?: return@withContext
                val existing = linkQuoteToTagQueries.getQuoteTag(documentId)
                    .executeAsOneOrNull()
                if (existing == null) {
                    linkQuoteToTagQueries.saveQuoteTag(
                        id = null,
                        quote_id = documentId,
                        tag_id = tagId,
                    )
                } else {
                    linkQuoteToTagQueries.updateQuoteTag(
                        quote_id = documentId,
                        tag_id = tagId,
                    )
                }
            }
        } catch (e: Exception) {
            //Log.e("QuoteDS", "Error linkDocToDocTag: ${e.message}")
        }
    }

    private suspend fun saveTag(documentId: Long, tag: DocumentTag) {
        try {
            linkDocumentToDocumentTag(
                documentId = documentId,
                newTag = tag,
                updateCase = TagUpdateOrCreationCase.TAG_CREATION,
            )
        } catch (e: Exception) {
            //Log.e("QuoteDS", "Error saveTag for documentId $documentId: ${e.message}")
        }
    }
}
