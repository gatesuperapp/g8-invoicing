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
import com.a4a.g8invoicing.ui.states.CreditNoteState
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import g8invoicing.CreditNote
import g8invoicing.DocumentClientOrIssuer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar

class CreditNoteLocalDataSource(
    db: Database,
) : CreditNoteLocalDataSourceInterface {
    private val creditNoteQueries = db.creditNoteQueries
    private val documentClientOrIssuerQueries = db.documentClientOrIssuerQueries
    private val documentClientOrIssuerAddressQueries = db.documentClientOrIssuerAddressQueries
    private val linkDocumentClientOrIssuerToAddressQueries = db.linkDocumentClientOrIssuerToAddressQueries
    private val documentProductQueries = db.documentProductQueries
    private val linkCreditNoteToDocumentProductQueries = db.linkCreditNoteToDocumentProductQueries
    private val linkCreditNoteDocumentProductToDeliveryNoteQueries =
        db.linkCreditNoteDocumentProductToDeliveryNoteQueries
    private val linkCreditNoteToDocumentClientOrIssuerQueries =
        db.linkCreditNoteToDocumentClientOrIssuerQueries

    override suspend fun createNew(): Long? {
        var newCreditNoteId: Long? = null
        val issuer = getExistingIssuer()?.transformIntoEditable()
            ?: ClientOrIssuerState()
        val creditNote = CreditNoteState(
            documentNumber = TextFieldValue(getLastDocumentNumber()?.let {
                incrementDocumentNumber(it)
            } ?: Strings.get(R.string.credit_note_default_number)),
            documentIssuer = issuer,
            footerText = TextFieldValue(getExistingFooter() ?: "")
        )

        saveInfoInCreditNoteTable(creditNote)
        saveInfoInOtherTables(creditNote)
        try {
            newCreditNoteId = creditNoteQueries.getLastInsertedRowId().executeAsOneOrNull()
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return newCreditNoteId
    }

    private fun getLastDocumentNumber(): String? {
        try {
            return creditNoteQueries.getLastCreditNoteNumber().executeAsOneOrNull()?.number
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    override fun fetch(id: Long): CreditNoteState? {
        try {
            return creditNoteQueries.get(id).executeAsOneOrNull()
                ?.let {
                    it.transformIntoEditableCreditNote(
                        fetchDocumentProducts(it.credit_note_id),
                        fetchClientAndIssuer(it.credit_note_id),
                    )
                }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    override fun fetchAll(): Flow<List<CreditNoteState>>? {
        try {
            return creditNoteQueries.getAll()
                .asFlow()
                .map {
                    it.executeAsList()
                        .map { document ->
                            val products = fetchDocumentProducts(document.credit_note_id)
                            val clientAndIssuer = fetchClientAndIssuer(document.credit_note_id)

                            document.transformIntoEditableCreditNote(
                                products,
                                clientAndIssuer
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
                linkCreditNoteToDocumentProductQueries.getDocumentProductsLinkedToCreditNote(id)
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
                            additionalInfo?.delivery_note_number
                        )
                }.toMutableList()
            } else null
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    private fun fetchClientAndIssuer(documentId: Long): List<ClientOrIssuerState>? {
        try {
            val listOfIds =
                linkCreditNoteToDocumentClientOrIssuerQueries.getDocumentClientOrIssuerLinkedToCreditNote(
                    documentId
                ).executeAsList()

            return if (listOfIds.isNotEmpty()) {
                listOfIds.map {
                    documentClientOrIssuerQueries.get(it.id)
                        .executeAsOne()
                        .transformIntoEditable()
                }
            } else null
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
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
                documentIssuer = documentClientAndIssuer?.firstOrNull { it.type == ClientOrIssuerType.DOCUMENT_ISSUER },
                documentClient = documentClientAndIssuer?.firstOrNull { it.type == ClientOrIssuerType.DOCUMENT_CLIENT },
                documentProducts = documentProducts,
                documentPrices = documentProducts?.let { calculateDocumentPrices(it) },
                currency = TextFieldValue(Strings.get(R.string.currency)),
                dueDate = it.due_date ?: "",
                footerText = TextFieldValue(text = it.footer ?: ""),
                createdDate = it.created_at
            )
        }
    }

    override suspend fun convertInvoiceToCreditNote(invoices: List<InvoiceState>) {
        withContext(Dispatchers.IO) {
            val docNumber = getLastDocumentNumber()?.let {
                incrementDocumentNumber(it)
            } ?: Strings.get(R.string.credit_note_default_number)

            try {
                saveInfoInCreditNoteTable(
                    CreditNoteState(
                        documentNumber = TextFieldValue(docNumber),
                        reference = invoices.firstOrNull { it.reference != null }?.reference,
                        freeField = invoices.firstOrNull { it.freeField != null }?.freeField,
                        documentIssuer = invoices.firstOrNull { it.documentIssuer != null }?.documentIssuer,
                        documentClient = invoices.firstOrNull { it.documentClient != null }?.documentClient,
                        footerText = TextFieldValue(getExistingFooter() ?: "")
                    )
                )
                invoices.forEach {
                    saveInfoInOtherTables(
                        it
                    )
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun update(document: CreditNoteState) {
        return withContext(Dispatchers.IO) {
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
                    updated_at = getDateFormatter(pattern = "yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().time)
                )
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun duplicate(documents: List<CreditNoteState>) {
        withContext(Dispatchers.IO) {
            try {
                documents.forEach {
                    val docNumber = getLastDocumentNumber()?.let {
                        incrementDocumentNumber(it)
                    } ?: Strings.get(R.string.credit_note_default_number)
                    val creditNote = it
                    creditNote.documentNumber = TextFieldValue(docNumber)

                    saveInfoInCreditNoteTable(creditNote)
                    saveInfoInOtherTables(creditNote)
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
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
                    linkCreditNoteToDocumentProductQueries,
                    linkDocumentClientOrIssuerToAddressQueries,
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
        documentClientOrIssuer: ClientOrIssuerState,
        documentId: Long?,
    ) {
        withContext(Dispatchers.IO) {
            try {
                saveDocumentClientOrIssuerInDbAndLink(
                    documentClientOrIssuerQueries,
                    documentClientOrIssuerAddressQueries,
                    linkDocumentClientOrIssuerToAddressQueries,
                    linkCreditNoteToDocumentClientOrIssuerQueries,
                    documentClientOrIssuer,
                    documentId
                )
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun delete(documents: List<CreditNoteState>) {
        withContext(Dispatchers.IO) {
            try {
                documents.filter { it.documentId != null }.forEach { document ->
                    document.documentProducts?.mapNotNull { it.id }?.forEach {
                        documentProductQueries.deleteDocumentProduct(it.toLong())
                        linkCreditNoteDocumentProductToDeliveryNoteQueries.deleteInfoLinkedToDocumentProduct(
                            it.toLong()
                        )
                    }
                    creditNoteQueries.delete(id = document.documentId!!.toLong())
                    linkCreditNoteToDocumentProductQueries.deleteAllProductsLinkedToCreditNote(
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
                Log.e(ContentValues.TAG, "Error: ${e.message}")
            }
        }
    }

    override suspend fun deleteDocumentProduct(documentId: Long, documentProductId: Long) {
        try {
            return withContext(Dispatchers.IO) {
                linkCreditNoteToDocumentProductQueries.deleteProductLinkedToCreditNote(
                    documentId,
                    documentProductId
                )
                linkCreditNoteDocumentProductToDeliveryNoteQueries.deleteInfoLinkedToDocumentProduct(
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
                    linkCreditNoteToDocumentClientOrIssuerQueries.deleteDocumentClientOrIssuerLinkedToCreditNote(
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
            footer = creditNoteQueries.getLastInsertedFooter().executeAsOneOrNull()?.footer
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
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
                footer = document.footerText.text
            )
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error: ${e.message}")
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
            Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }
}

