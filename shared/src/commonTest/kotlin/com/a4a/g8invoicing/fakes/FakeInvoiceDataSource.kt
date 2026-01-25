package com.a4a.g8invoicing.fakes

import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.data.InvoiceLocalDataSourceInterface
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.models.TagUpdateOrCreationCase
import com.a4a.g8invoicing.data.util.DateUtils
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.InvoiceState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeInvoiceDataSource : InvoiceLocalDataSourceInterface {

    private val invoices = mutableListOf<InvoiceState>()
    private val invoicesFlow = MutableStateFlow<List<InvoiceState>>(emptyList())
    private var nextInvoiceId = 1
    private var nextProductId = 1

    // Default footer text for invoices
    var defaultFooterText: String = "Merci pour votre confiance."

    // For testing: access internal state
    fun getInvoices(): List<InvoiceState> = invoices.toList()
    fun getInvoiceCount(): Int = invoices.size
    fun clear() {
        invoices.clear()
        invoicesFlow.value = emptyList()
        nextInvoiceId = 1
        nextProductId = 1
    }

    override suspend fun fetch(id: Long): InvoiceState? {
        return invoices.find { it.documentId == id.toInt() }
    }

    override fun fetchAll(): Flow<List<InvoiceState>> {
        return invoicesFlow
    }

    override suspend fun createNew(): Long {
        val todayFormatted = DateUtils.getCurrentDateFormatted()
        val dueDateFormatted = DateUtils.getDatePlusDaysFormatted(30)
        val newInvoice = InvoiceState(
            documentId = nextInvoiceId,
            documentNumber = TextFieldValue("FA-${nextInvoiceId.toString().padStart(3, '0')}"),
            documentDate = todayFormatted,
            dueDate = dueDateFormatted,
            documentTag = DocumentTag.DRAFT,
            footerText = TextFieldValue(defaultFooterText),
            createdDate = DateUtils.getCurrentTimestamp()
        )
        invoices.add(newInvoice)
        invoicesFlow.value = invoices.toList()
        return nextInvoiceId++.toLong()
    }

    override suspend fun saveDocumentProductInDbAndLinkToDocument(
        documentProduct: DocumentProductState,
        documentId: Long,
        deliveryNoteDate: String?,
        deliveryNoteNumber: String?
    ): Int {
        val invoice = invoices.find { it.documentId == documentId.toInt() }
        invoice?.let {
            val productId = nextProductId++
            val product = documentProduct.copy(
                id = productId,
                linkedDate = deliveryNoteDate,
                linkedDocNumber = deliveryNoteNumber
            )
            val currentProducts = it.documentProducts?.toMutableList() ?: mutableListOf()
            currentProducts.add(product)
            val index = invoices.indexOf(it)
            invoices[index] = it.copy(documentProducts = currentProducts)
            invoicesFlow.value = invoices.toList()
            return productId
        }
        return -1
    }

    override suspend fun deleteDocumentProduct(id: Long, documentProductId: Long) {
        val invoice = invoices.find { it.documentId == id.toInt() }
        invoice?.let {
            val updatedProducts = it.documentProducts?.filterNot { product ->
                product.id == documentProductId.toInt()
            }
            val index = invoices.indexOf(it)
            invoices[index] = it.copy(documentProducts = updatedProducts)
            invoicesFlow.value = invoices.toList()
        }
    }

    override suspend fun saveDocumentClientOrIssuerInDbAndLinkToDocument(
        documentClientOrIssuer: ClientOrIssuerState,
        id: Long?
    ) {
        val invoice = invoices.find { it.documentId == id?.toInt() }
        invoice?.let {
            val index = invoices.indexOf(it)
            if (documentClientOrIssuer.type == ClientOrIssuerType.CLIENT) {
                invoices[index] = it.copy(documentClient = documentClientOrIssuer)
            } else {
                invoices[index] = it.copy(documentIssuer = documentClientOrIssuer)
            }
            invoicesFlow.value = invoices.toList()
        }
    }

    override suspend fun deleteDocumentClientOrIssuer(id: Long, type: ClientOrIssuerType) {
        val invoice = invoices.find { it.documentId == id.toInt() }
        invoice?.let {
            val index = invoices.indexOf(it)
            if (type == ClientOrIssuerType.CLIENT) {
                invoices[index] = it.copy(documentClient = null)
            } else {
                invoices[index] = it.copy(documentIssuer = null)
            }
            invoicesFlow.value = invoices.toList()
        }
    }

    override suspend fun duplicate(documents: List<InvoiceState>) {
        documents.forEach { doc ->
            val newId = nextInvoiceId++
            val duplicate = doc.copy(
                documentId = newId,
                documentNumber = TextFieldValue("FA-${newId.toString().padStart(3, '0')}"),
                documentTag = DocumentTag.DRAFT
            )
            invoices.add(duplicate)
        }
        invoicesFlow.value = invoices.toList()
    }

    override suspend fun convertDeliveryNotesToInvoice(deliveryNotes: List<DeliveryNoteState>) {
        val newId = nextInvoiceId++
        val todayFormatted = DateUtils.getCurrentDateFormatted()
        val dueDateFormatted = DateUtils.getDatePlusDaysFormatted(30)

        // Link each product with its source delivery note info
        val allProducts = deliveryNotes.flatMap { deliveryNote ->
            deliveryNote.documentProducts?.map { product ->
                product.copy(
                    linkedDate = deliveryNote.documentDate,
                    linkedDocNumber = deliveryNote.documentNumber.text
                )
            } ?: emptyList()
        }

        val newInvoice = InvoiceState(
            documentId = newId,
            documentNumber = TextFieldValue("FA-${newId.toString().padStart(3, '0')}"),
            documentDate = todayFormatted,
            dueDate = dueDateFormatted,
            documentProducts = allProducts,
            documentClient = deliveryNotes.firstOrNull()?.documentClient,
            documentIssuer = deliveryNotes.firstOrNull()?.documentIssuer,
            footerText = TextFieldValue(defaultFooterText),
            createdDate = DateUtils.getCurrentTimestamp()
        )
        invoices.add(newInvoice)
        invoicesFlow.value = invoices.toList()
    }

    override suspend fun update(document: InvoiceState) {
        val index = invoices.indexOfFirst { it.documentId == document.documentId }
        if (index >= 0) {
            invoices[index] = document
            invoicesFlow.value = invoices.toList()
        }
    }

    override suspend fun delete(documents: List<InvoiceState>) {
        val idsToDelete = documents.mapNotNull { it.documentId }
        invoices.removeAll { it.documentId in idsToDelete }
        invoicesFlow.value = invoices.toList()
    }

    override suspend fun setTag(
        documents: List<InvoiceState>,
        tag: DocumentTag,
        tagUpdateCase: TagUpdateOrCreationCase
    ) {
        documents.forEach { doc ->
            val index = invoices.indexOfFirst { it.documentId == doc.documentId }
            if (index >= 0) {
                invoices[index] = invoices[index].copy(documentTag = tag)
            }
        }
        invoicesFlow.value = invoices.toList()
    }

    override suspend fun deleteTag(invoiceId: Long) {
        val index = invoices.indexOfFirst { it.documentId == invoiceId.toInt() }
        if (index >= 0) {
            invoices[index] = invoices[index].copy(documentTag = DocumentTag.DRAFT)
        }
        invoicesFlow.value = invoices.toList()
    }

    override suspend fun markAsPaid(documents: List<InvoiceState>, tag: DocumentTag) {
        documents.forEach { doc ->
            val index = invoices.indexOfFirst { it.documentId == doc.documentId }
            if (index >= 0) {
                invoices[index] = invoices[index].copy(
                    documentTag = DocumentTag.PAID,
                    paymentStatus = 1
                )
            }
        }
        invoicesFlow.value = invoices.toList()
    }

    override suspend fun updateDocumentProductsOrderInDb(
        documentId: Long,
        orderedProducts: List<DocumentProductState>
    ) {
        val index = invoices.indexOfFirst { it.documentId == documentId.toInt() }
        if (index >= 0) {
            val reorderedProducts = orderedProducts.mapIndexed { i, product ->
                product.copy(sortOrder = i)
            }
            invoices[index] = invoices[index].copy(documentProducts = reorderedProducts)
            invoicesFlow.value = invoices.toList()
        }
    }

    // ============= HELPER METHODS FOR TESTING =============

    /**
     * Creates a corrective invoice (facture rectificative) from an existing invoice.
     * - Creates a new invoice with "Annule et remplace FA-XXX" in freeField
     * - Marks the original invoice as CANCELLED
     * @param cancelAndReplaceText The text to use for "Annule et remplace"
     */
    suspend fun createCorrectiveInvoice(
        originalInvoice: InvoiceState,
        cancelAndReplaceText: String = "Annule et remplace"
    ): Long {
        // Set the freeField with cancel and replace text
        val invoiceWithFreeField = originalInvoice.copy(
            freeField = TextFieldValue("$cancelAndReplaceText ${originalInvoice.documentNumber.text}")
        )

        // Duplicate the invoice (creates a new one)
        duplicate(listOf(invoiceWithFreeField))

        // Mark original invoice as CANCELLED
        setTag(listOf(originalInvoice), DocumentTag.CANCELLED, TagUpdateOrCreationCase.AUTOMATICALLY_CANCELLED)

        // Return the ID of the new corrective invoice
        return invoices.last().documentId?.toLong() ?: -1
    }
}
