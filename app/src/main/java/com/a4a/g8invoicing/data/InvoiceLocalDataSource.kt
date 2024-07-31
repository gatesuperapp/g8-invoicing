package com.a4a.g8invoicing.data

import androidx.compose.ui.text.input.TextFieldValue
import app.cash.sqldelight.coroutines.asFlow
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.states.DocumentClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import g8invoicing.DocumentClientOrIssuer
import g8invoicing.Invoice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class InvoiceLocalDataSource(
    db: Database,
) : InvoiceLocalDataSourceInterface {
    private val invoiceQueries = db.invoiceQueries
    private val documentClientOrIssuerQueries = db.documentClientOrIssuerQueries
    private val documentProductQueries = db.documentProductQueries
    private val invoiceDocumentProductQueries = db.linkInvoiceToDocumentProductQueries
    private val invoiceDocumentClientOrIssuerQueries = db.linkInvoiceToDocumentClientOrIssuerQueries


    override fun fetch(id: Long): InvoiceState? {
        return invoiceQueries.get(id).executeAsOneOrNull()
            ?.let {
                it.transformIntoEditableInvoice(
                    fetchDocumentProducts(it.invoice_id),
                    fetchClientAndIssuer(it.invoice_id)
                )
            }
    }

    override fun fetchAll(): Flow<List<InvoiceState>> {
        return invoiceQueries.getAll()
            .asFlow()
            .map {
                it.executeAsList()
                    .map { document ->
                        val products = fetchDocumentProducts(document.invoice_id)
                        val clientAndIssuer = fetchClientAndIssuer(document.invoice_id)

                        document.transformIntoEditableInvoice(
                            products,
                            clientAndIssuer
                        )
                    }
            }
    }

    private fun fetchDocumentProducts(id: Long): MutableList<DocumentProductState>? {
        val listOfIds =
            invoiceDocumentProductQueries.getDocumentProductsLinkedToInvoice(
                id
            )
                .executeAsList()
        return if (listOfIds.isNotEmpty()) {
            listOfIds.map {
                documentProductQueries.getDocumentProduct(it.document_product_id)
                    .executeAsOne().transformIntoEditableDocumentProduct()
            }.toMutableList()
        } else null
    }

    private fun fetchClientAndIssuer(documentId: Long): List<DocumentClientOrIssuerState>? {
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
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun fetchDocumentProductsFlow(documentId: Long): Flow<List<DocumentProductState>> {
        return invoiceDocumentProductQueries.getDocumentProductsLinkedToInvoice(
            documentId
        )
            .asFlow()
            .flatMapMerge { query ->
                combine(
                    query.executeAsList().map {
                        documentProductQueries.getDocumentProduct(it.document_product_id)
                            .asFlow()
                            .map {
                                it.executeAsOne().transformIntoEditableDocumentProduct()
                            }
                    })
                {
                    it.asList()
                }
            }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    private fun fetchClientOrIssuerFlow(id: Long): Flow<List<DocumentClientOrIssuerState>> {
        return invoiceDocumentClientOrIssuerQueries.getDocumentClientOrIssuerLinkedToInvoice(
            id
        )
            .asFlow()
            .flatMapMerge { query ->
                combine(
                    query.executeAsList().map {
                        documentClientOrIssuerQueries.get(it.document_client_or_issuer_id)
                            .asFlow()
                            .map {
                                it.executeAsOne().transformIntoEditable()
                            }
                    })
                {
                    it.asList()
                }
            }
    }


    private fun Invoice.transformIntoEditableInvoice(
        documentProducts: MutableList<DocumentProductState>? = null,
        documentClientAndIssuer: List<DocumentClientOrIssuerState>? = null,
    ): InvoiceState {
        this.let {
            return InvoiceState(
                documentId = it.invoice_id.toInt(),
                documentNumber = TextFieldValue(text = it.number ?: ""),
                documentDate = it.delivery_date ?: "",
                orderNumber = TextFieldValue(text = it.order_number ?: ""),
                documentIssuer = documentClientAndIssuer?.firstOrNull { it.type == ClientOrIssuerType.DOCUMENT_ISSUER },
                documentClient = documentClientAndIssuer?.firstOrNull { it.type == ClientOrIssuerType.DOCUMENT_CLIENT },
                documentProducts = documentProducts,
                documentPrices = documentProducts?.let { calculateDocumentPrices(it) },
                currency = TextFieldValue(Strings.get(R.string.currency))
            )
        }
    }

    override suspend fun createNew(): Long? {
        saveInvoiceNoteInfoInAllTables(
            InvoiceState(
                documentIssuer = getExistingIssuer()?.transformIntoEditable()
                    ?: DocumentClientOrIssuerState(),
            )
        )

        return invoiceQueries.getLastInsertedRowId().executeAsOneOrNull()
    }

    override suspend fun update(document: InvoiceState) {
        return withContext(Dispatchers.IO) {
            try {
                invoiceQueries.update(
                    invoice_id = document.documentId?.toLong() ?: 0,
                    number = document.documentNumber.text,
                    delivery_date = document.documentDate,
                    order_number = document.orderNumber.text,
                    currency = document.currency.text,
                    due_date = document.documentDate,
                )
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun duplicate(documents: List<InvoiceState>) {
        withContext(Dispatchers.IO) {
            try {
                documents.forEach {
                    saveInvoiceNoteInfoInAllTables(
                        InvoiceState(
                            documentType = it.documentType,
                            documentId = it.documentId,
                            documentNumber = TextFieldValue("XXX"),
                            documentDate = it.documentDate,
                            orderNumber = it.orderNumber,
                            documentIssuer = it.documentIssuer,
                            documentClient = it.documentClient,
                            documentProducts = it.documentProducts,
                            documentPrices = it.documentPrices,
                            currency = it.currency,
                            dueDate = it.dueDate
                        )
                    )
                }
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun saveDocumentProductInDbAndLinkToDocument(
        documentProduct: DocumentProductState,
        documentId: Long?,
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
                    page = documentProduct.page.toLong(),
                    product_id = documentProduct.productId?.toLong()
                )

                documentId?.let { documentId ->
                    documentProductQueries.getLastInsertedRowId().executeAsOneOrNull()?.toInt()
                        ?.let { id ->
                            addDocumentProduct(
                                documentId,
                                id.toLong()
                            )
                        }
                }
            } catch (cause: Throwable) {
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
                            addDocumentClientOrIssuer(
                                documentId,
                                id.toLong()
                            )
                        }
                }
            } catch (cause: Throwable) {
            }
        }
    }


    override suspend fun delete(documents: List<InvoiceState>) {
        withContext(Dispatchers.IO) {
            try {
                documents.filter { it.documentId != null }.forEach { document ->
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
            } catch (cause: Throwable) {
            }
        }
    }

    override suspend fun deleteDocumentProduct(id: Long, documentProductId: Long) {
        return withContext(Dispatchers.IO) {
            invoiceDocumentProductQueries.deleteProductLinkedToInvoice(
                id,
                documentProductId
            )
        }
    }

    override suspend fun deleteDocumentClientOrIssuer(
        id: Long,
        type: ClientOrIssuerType,
    ) {
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
    }

    override fun addDocumentProduct(documentId: Long, documentProductId: Long) {
        invoiceDocumentProductQueries.saveProductLinkedToInvoice(
            id = null,
            invoice_id = documentId,
            document_product_id = documentProductId
        )
    }

    private fun addDocumentClientOrIssuer(documentId: Long, documentClientOrIssuerId: Long) {
        invoiceDocumentClientOrIssuerQueries.saveDocumentClientOrIssuerLinkedToInvoice(
            id = null,
            invoice_id = documentId,
            document_client_or_issuer_id = documentClientOrIssuerId
        )
    }

    private fun getExistingIssuer(): DocumentClientOrIssuer? {
        var issuer: DocumentClientOrIssuer? = null
        try {
            issuer = documentClientOrIssuerQueries.getLastInsertedIssuer().executeAsOneOrNull()
        } catch (e: Exception) {
            println("Fetching result failed with exception: ${e.localizedMessage}")
        }
        return issuer
    }

    private suspend fun saveInvoiceNoteInfoInAllTables(document: InvoiceState) {
        invoiceQueries.save(
            invoice_id = null,
            number = document.documentNumber.text,
            delivery_date = document.documentDate,
            order_number = document.orderNumber.text,
            currency = document.currency.text,
            due_date = document.dueDate
        )

        invoiceQueries.getLastInsertedRowId().executeAsOneOrNull()?.let { id ->
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
    }
}

