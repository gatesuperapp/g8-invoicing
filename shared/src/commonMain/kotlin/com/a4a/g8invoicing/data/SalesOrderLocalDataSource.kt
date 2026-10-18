package com.a4a.g8invoicing.data

import androidx.compose.ui.text.input.TextFieldValue
import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.data.auth.ActivatedModulesRepository
import com.a4a.g8invoicing.data.auth.SubscriptionRepository
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.models.TagUpdateOrCreationCase
import com.a4a.g8invoicing.data.util.DateUtils
import com.a4a.g8invoicing.data.util.DispatcherProvider
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.invoice_watermark_default
import com.a4a.g8invoicing.shared.resources.sales_order_default_number
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.screens.shared.DocumentLabels
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.SalesOrderState
import g8invoicing.SalesOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString

class SalesOrderLocalDataSource(
    db: Database,
    private val clientOrIssuerDataSource: ClientOrIssuerLocalDataSourceInterface,
    private val activatedModules: ActivatedModulesRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val currencyManager: CurrencyManager,
    private val currentCompanyRepository: CurrentCompanyRepository,
) : SalesOrderLocalDataSourceInterface {
    private val salesOrderQueries = db.salesOrderQueries
    private val documentClientOrIssuerQueries = db.documentClientOrIssuerQueries
    private val documentClientOrIssuerAddressQueries = db.documentClientOrIssuerAddressQueries
    private val linkDocumentClientOrIssuerToAddressQueries =
        db.linkDocumentClientOrIssuerToAddressQueries
    private val documentProductQueries = db.documentProductQueries
    private val linkSalesOrderToDocumentProductQueries =
        db.linkSalesOrderToDocumentProductQueries
    private val linkSalesOrderToDocumentClientOrIssuerQueries =
        db.linkSalesOrderToDocumentClientOrIssuerQueries
    private val documentClientOrIssuerEmailQueries = db.documentClientOrIssuerEmailQueries
    private val salesOrderTagQueries = db.salesOrderTagQueries
    private val linkSalesOrderToTagQueries = db.linkSalesOrderToTagQueries

    private suspend fun computeWatermark(): String? {
        return if (activatedModules.isActive(ActivatedModulesRepository.MODULE_WATERMARK_REMOVAL)) null
        else getString(Res.string.invoice_watermark_default)
    }

    override suspend fun createNew(): Long? {
        val currentCompanyId = currentCompanyRepository.current
        val existingIssuer = currentCompanyId
            ?.let { clientOrIssuerDataSource.getCurrentIssuer(it) }
            ?: clientOrIssuerDataSource.getLastIssuer()
        val frozenWatermark = computeWatermark()
        val frozenLabels = DocumentLabels.captureSnapshotJson()

        return withContext(DispatcherProvider.IO) {
            val todayFormatted = DateUtils.getCurrentDateFormatted()

            val newSalesOrderState = SalesOrderState(
                documentNumber = TextFieldValue(getLastDocumentNumber(currentCompanyId)?.let {
                    incrementDocumentNumber(it)
                } ?: getString(Res.string.sales_order_default_number)),
                documentDate = todayFormatted,
                documentIssuer = existingIssuer,
                currency = TextFieldValue(currencyManager.currentCurrency),
                footerText = TextFieldValue(getExistingFooter() ?: ""),
                watermarkText = frozenWatermark,
                labelsSnapshot = frozenLabels,
                showCurrencyAndAutoTaxColumn = true,
                formatLocale = AppLocaleHolder.languageCode,
                originalCompanyId = currentCompanyId
                    ?: existingIssuer?.originalClientOrIssuerId?.toLong(),
            )

            saveInfoInDocumentTable(newSalesOrderState)

            val newSalesOrderId = salesOrderQueries.getLastInsertedRowId().executeAsOneOrNull()

            newSalesOrderId?.let { id ->
                saveInfoInOtherTables(id, newSalesOrderState)
                saveTag(id, newSalesOrderState.documentTag)
            }
            newSalesOrderId
        }
    }

    private fun getLastDocumentNumber(companyId: Long?): String? {
        try {
            return if (companyId != null) {
                salesOrderQueries.getLastSalesOrderNumberForCompany(companyId).executeAsOneOrNull()?.number
            } else {
                salesOrderQueries.getLastSalesOrderNumber().executeAsOneOrNull()?.number
            }
        } catch (e: Exception) {
        }
        return null
    }

    private fun getExistingFooter(): String? {
        var footer: String? = null
        try {
            footer = salesOrderQueries.getLastInsertedFooter().executeAsOneOrNull()?.footer
        } catch (e: Exception) {
        }
        return footer
    }

    override suspend fun fetch(id: Long): SalesOrderState? {
        return withContext(DispatcherProvider.IO) {
            try {
                salesOrderQueries.get(id).executeAsOneOrNull()
                    ?.let {
                        it.transformIntoEditableSalesOrder(
                            fetchDocumentProducts(it.sales_order_id),
                            hydrateBanksOnDocIssuer(
                                fetchClientAndIssuer(
                                    it.sales_order_id,
                                    linkSalesOrderToDocumentClientOrIssuerQueries,
                                    linkDocumentClientOrIssuerToAddressQueries,
                                    documentClientOrIssuerQueries,
                                    documentClientOrIssuerAddressQueries,
                                    documentClientOrIssuerEmailQueries
                                )
                            ),
                            fetchTag(it.sales_order_id)
                        )
                    }
            } catch (e: Exception) {
                null
            }
        }
    }

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

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun fetchAll(): Flow<List<SalesOrderState>>? {
        try {
            return currentCompanyRepository.state.flatMapLatest { companyId ->
                val query = if (companyId != null) {
                    salesOrderQueries.getAllForCompany(companyId)
                } else {
                    salesOrderQueries.getAll()
                }
                query.asFlow().map { rows ->
                    rows.executeAsList()
                        .map { document ->
                            val products = fetchDocumentProducts(document.sales_order_id)
                            val clientAndIssuer = fetchClientAndIssuer(
                                document.sales_order_id,
                                linkSalesOrderToDocumentClientOrIssuerQueries,
                                linkDocumentClientOrIssuerToAddressQueries,
                                documentClientOrIssuerQueries,
                                documentClientOrIssuerAddressQueries,
                                documentClientOrIssuerEmailQueries
                            )

                            document.transformIntoEditableSalesOrder(
                                products,
                                clientAndIssuer,
                                fetchTag(document.sales_order_id)
                            )
                        }
                }
            }
        } catch (e: Exception) {
        }
        return null
    }

    private fun fetchDocumentProducts(salesOrderId: Long): MutableList<DocumentProductState>? {
        try {
            val listOfIds =
                linkSalesOrderToDocumentProductQueries.getDocumentProductsLinkedToSalesOrder(
                    salesOrderId
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
        }
        return null
    }

    private fun fetchTag(documentId: Long): DocumentTag? {
        try {
            val tagId = linkSalesOrderToTagQueries.getSalesOrderTag(documentId)
                .executeAsOneOrNull()?.tag_id
            tagId?.let {
                salesOrderTagQueries.getTag(it).executeAsOneOrNull()?.let { tagName ->
                    return enumValueOf<DocumentTag>(tagName)
                }
            }
        } catch (e: Exception) {
        }
        return null
    }

    private fun SalesOrder.transformIntoEditableSalesOrder(
        documentProducts: MutableList<DocumentProductState>? = null,
        documentClientAndIssuer: List<ClientOrIssuerState>? = null,
        documentTag: DocumentTag? = null,
    ): SalesOrderState {
        this.let {
            return SalesOrderState(
                documentId = it.sales_order_id.toInt(),
                documentTag = documentTag ?: DocumentTag.DRAFT,
                documentNumber = TextFieldValue(text = it.number ?: ""),
                documentDate = it.sales_order_date ?: "",
                reference = TextFieldValue(text = it.reference ?: ""),
                freeField = TextFieldValue(text = it.free_field ?: ""),
                documentIssuer = documentClientAndIssuer?.filter { it.type == ClientOrIssuerType.DOCUMENT_ISSUER }?.maxByOrNull { it.id ?: 0 },
                documentClient = documentClientAndIssuer?.filter { it.type == ClientOrIssuerType.DOCUMENT_CLIENT }?.maxByOrNull { it.id ?: 0 },
                documentProducts = documentProducts?.sortedBy { it.sortOrder },
                documentTotalPrices = documentProducts?.let { calculateDocumentPrices(it) },
                currency = TextFieldValue(it.currency ?: CurrencyManager.DEFAULT_FALLBACK),
                footerText = TextFieldValue(text = it.footer ?: ""),
                createdDate = it.created_at,
                watermarkText = it.watermark_text,
                labelsSnapshot = it.labels_snapshot,
                showCurrencyAndAutoTaxColumn = it.show_currency_and_auto_tax_column != 0L,
                formatLocale = it.format_locale,
                originalCompanyId = it.original_company_id,
                fontFamily = it.font_family,
            )
        }
    }

    override suspend fun update(document: SalesOrderState) {
        return withContext(DispatcherProvider.IO) {
            try {
                salesOrderQueries.update(
                    sales_order_id = document.documentId?.toLong() ?: 0,
                    number = document.documentNumber.text,
                    sales_order_date = document.documentDate,
                    reference = document.reference?.text,
                    free_field = document.freeField?.text,
                    currency = document.currency.text,
                    footer = document.footerText.text,
                    font_family = document.fontFamily,
                    updated_at = DateUtils.getCurrentTimestamp()
                )
            } catch (e: Exception) {
            }
        }
    }

    override suspend fun duplicate(documents: List<SalesOrderState>) {
        val frozenWatermark = computeWatermark()
        val frozenLabels = DocumentLabels.captureSnapshotJson()
        withContext(DispatcherProvider.IO) {
            try {
                documents.forEach { originalDocument ->
                    val docCompanyId = originalDocument.originalCompanyId
                        ?: currentCompanyRepository.current
                    val docNumber = getLastDocumentNumber(docCompanyId)?.let {
                        incrementDocumentNumber(it)
                    } ?: getString(Res.string.sales_order_default_number)

                    val duplicatedDocumentState = originalDocument.copy(
                        documentNumber = TextFieldValue(docNumber),
                        documentDate = DateUtils.getCurrentDateFormatted(),
                        watermarkText = frozenWatermark,
                        labelsSnapshot = frozenLabels,
                        showCurrencyAndAutoTaxColumn = true,
                        formatLocale = AppLocaleHolder.languageCode,
                    )

                    saveInfoInDocumentTable(duplicatedDocumentState)

                    val newSalesOrderId = salesOrderQueries.getLastInsertedRowId()
                        .executeAsOneOrNull()

                    newSalesOrderId?.let { id ->
                        saveInfoInOtherTables(id, duplicatedDocumentState)
                        saveTag(id, DocumentTag.DRAFT)
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    override suspend fun saveDocumentProductInDbAndLinkToDocument(
        documentProduct: DocumentProductState,
        documentId: Long,
    ): Int? {
        return withContext(DispatcherProvider.IO) {
            try {
                documentProductQueries.transactionWithResult {
                    saveDocumentProductInDbAndLink(
                        documentProductQueries = documentProductQueries,
                        linkToDocumentProductQueries = linkSalesOrderToDocumentProductQueries,
                        documentProduct = documentProduct,
                        documentId = documentId
                    )
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun saveDocumentClientOrIssuerInDbAndLinkToDocument(
        documentClientOrIssuer: ClientOrIssuerState,
        documentId: Long?,
    ) {
        val clientOrIssuerToSave = if (
            (documentClientOrIssuer.type == ClientOrIssuerType.ISSUER ||
                documentClientOrIssuer.type == ClientOrIssuerType.DOCUMENT_ISSUER) &&
            documentClientOrIssuer.originalClientOrIssuerId == null
        ) {
            val masterIssuer = documentClientOrIssuer.copy(type = ClientOrIssuerType.ISSUER)
            clientOrIssuerDataSource.createNew(masterIssuer)
            val masterId = clientOrIssuerDataSource.getLastCreatedIssuerId()
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
                    linkSalesOrderToDocumentClientOrIssuerQueries,
                    clientOrIssuerToSave,
                    documentId
                )
            } catch (e: Exception) {
            }
        }
    }

    override suspend fun delete(documents: List<SalesOrderState>) {
        withContext(DispatcherProvider.IO) {
            try {
                documents.filter { it.documentId != null }.forEach { document ->
                    document.documentProducts?.mapNotNull { it.id }?.forEach {
                        documentProductQueries.deleteDocumentProduct(it.toLong())
                    }

                    linkSalesOrderToDocumentProductQueries.deleteAllProductsLinkedToASalesOrder(
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

                    linkSalesOrderToDocumentClientOrIssuerQueries.deleteAllDocumentClientOrIssuerLinkedToASalesOrder(
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

                    linkSalesOrderToTagQueries.delete(document.documentId!!.toLong())

                    salesOrderQueries.delete(id = document.documentId!!.toLong())
                }
            } catch (e: Exception) {
            }
        }
    }

    override suspend fun deleteDocumentProduct(documentId: Long, documentProductId: Long) {
        try {
            return withContext(DispatcherProvider.IO) {
                linkSalesOrderToDocumentProductQueries.deleteProductLinkedToSalesOrder(
                    documentId,
                    documentProductId
                )
            }
        } catch (e: Exception) {
        }
    }

    override suspend fun deleteDocumentClientOrIssuer(
        documentId: Long,
        type: ClientOrIssuerType,
    ) {
        try {
            return withContext(DispatcherProvider.IO) {
                val clientOrIssuerToDelete =
                    fetchClientAndIssuer(
                        documentId,
                        linkSalesOrderToDocumentClientOrIssuerQueries,
                        linkDocumentClientOrIssuerToAddressQueries,
                        documentClientOrIssuerQueries,
                        documentClientOrIssuerAddressQueries,
                        documentClientOrIssuerEmailQueries
                    )?.firstOrNull { it.type == type }

                clientOrIssuerToDelete?.id?.let { entityId ->
                    linkSalesOrderToDocumentClientOrIssuerQueries.deleteDocumentClientOrIssuerLinkedToSalesOrder(
                        documentId,
                        entityId.toLong()
                    )
                    documentClientOrIssuerQueries.delete(entityId.toLong())
                    linkDocumentClientOrIssuerToAddressQueries.deleteWithClientId(entityId.toLong())
                    clientOrIssuerToDelete.addresses?.mapNotNull { it.id }?.forEach { addressId ->
                        documentClientOrIssuerAddressQueries.delete(addressId.toLong())
                    }
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun saveInfoInDocumentTable(document: SalesOrderState) {
        try {
            salesOrderQueries.save(
                sales_order_id = null,
                number = document.documentNumber.text,
                sales_order_date = document.documentDate,
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
            )
        } catch (e: Exception) {
        }
    }

    private suspend fun saveInfoInOtherTables(
        documentId: Long,
        salesOrder: SalesOrderState,
    ) {
        try {
            salesOrder.documentProducts?.forEach { documentProduct ->
                saveDocumentProductInDbAndLinkToDocument(
                    documentProduct = documentProduct,
                    documentId = documentId
                )
            }

            salesOrder.documentClient?.let {
                saveDocumentClientOrIssuerInDbAndLinkToDocument(
                    documentClientOrIssuer = it,
                    documentId = documentId
                )
            }

            salesOrder.documentIssuer?.let {
                saveDocumentClientOrIssuerInDbAndLinkToDocument(
                    documentClientOrIssuer = it,
                    documentId = documentId
                )
            }
        } catch (e: Exception) {
        }
    }

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
                        linkSalesOrderToDocumentProductQueries,
                    )
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    override suspend fun setTag(
        documents: List<SalesOrderState>,
        tag: DocumentTag,
        tagUpdateCase: TagUpdateOrCreationCase,
    ) {
        withContext(DispatcherProvider.IO) {
            try {
                documents.forEach { salesOrder ->
                    salesOrder.documentId?.toLong()?.let { salesOrderId ->
                        linkDocumentToDocumentTag(
                            salesOrderId,
                            newTag = tag,
                            updateCase = tagUpdateCase,
                        )
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    private suspend fun linkDocumentToDocumentTag(
        documentId: Long,
        newTag: DocumentTag,
        @Suppress("UNUSED_PARAMETER") updateCase: TagUpdateOrCreationCase,
    ) {
        try {
            withContext(DispatcherProvider.IO) {
                val tagId = salesOrderTagQueries.getTagId(newTag.name)
                    .executeAsOneOrNull() ?: return@withContext
                val existing = linkSalesOrderToTagQueries
                    .getSalesOrderTag(documentId).executeAsOneOrNull()
                if (existing == null) {
                    linkSalesOrderToTagQueries.saveSalesOrderTag(
                        id = null,
                        sales_order_id = documentId,
                        tag_id = tagId,
                    )
                } else {
                    linkSalesOrderToTagQueries.updateSalesOrderTag(
                        sales_order_id = documentId,
                        tag_id = tagId,
                    )
                }
            }
        } catch (e: Exception) {
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
        }
    }
}
