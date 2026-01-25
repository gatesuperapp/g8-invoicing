package com.a4a.g8invoicing.fakes

import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.data.CreditNoteLocalDataSourceInterface
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.util.DateUtils
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.CreditNoteState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.InvoiceState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCreditNoteDataSource : CreditNoteLocalDataSourceInterface {

    private val creditNotes = mutableListOf<CreditNoteState>()
    private val creditNotesFlow = MutableStateFlow<List<CreditNoteState>>(emptyList())
    private var nextCreditNoteId = 1
    private var nextProductId = 1

    // For testing: access internal state
    fun getCreditNotes(): List<CreditNoteState> = creditNotes.toList()
    fun getCreditNoteCount(): Int = creditNotes.size
    fun clear() {
        creditNotes.clear()
        creditNotesFlow.value = emptyList()
        nextCreditNoteId = 1
        nextProductId = 1
    }

    override suspend fun fetch(id: Long): CreditNoteState? {
        return creditNotes.find { it.documentId == id.toInt() }
    }

    override fun fetchAll(): Flow<List<CreditNoteState>> {
        return creditNotesFlow
    }

    override suspend fun createNew(): Long {
        val todayFormatted = DateUtils.getCurrentDateFormatted()
        val newCreditNote = CreditNoteState(
            documentId = nextCreditNoteId,
            documentNumber = TextFieldValue("AV-${nextCreditNoteId.toString().padStart(3, '0')}"),
            documentDate = todayFormatted,
            documentTag = DocumentTag.DRAFT,
            createdDate = DateUtils.getCurrentTimestamp()
        )
        creditNotes.add(newCreditNote)
        creditNotesFlow.value = creditNotes.toList()
        return nextCreditNoteId++.toLong()
    }

    override suspend fun saveDocumentProductInDbAndLinkToDocument(
        documentProduct: DocumentProductState,
        documentId: Long,
        deliveryNoteDate: String?,
        deliveryNoteNumber: String?
    ): Int {
        val creditNote = creditNotes.find { it.documentId == documentId.toInt() }
        creditNote?.let {
            val productId = nextProductId++
            val product = documentProduct.copy(
                id = productId,
                linkedDate = deliveryNoteDate,
                linkedDocNumber = deliveryNoteNumber
            )
            val currentProducts = it.documentProducts?.toMutableList() ?: mutableListOf()
            currentProducts.add(product)
            val index = creditNotes.indexOf(it)
            creditNotes[index] = it.copy(documentProducts = currentProducts)
            creditNotesFlow.value = creditNotes.toList()
            return productId
        }
        return -1
    }

    override suspend fun deleteDocumentProduct(id: Long, documentProductId: Long) {
        val creditNote = creditNotes.find { it.documentId == id.toInt() }
        creditNote?.let {
            val updatedProducts = it.documentProducts?.filterNot { product ->
                product.id == documentProductId.toInt()
            }
            val index = creditNotes.indexOf(it)
            creditNotes[index] = it.copy(documentProducts = updatedProducts)
            creditNotesFlow.value = creditNotes.toList()
        }
    }

    override suspend fun saveDocumentClientOrIssuerInDbAndLinkToDocument(
        documentClientOrIssuer: ClientOrIssuerState,
        id: Long?
    ) {
        val creditNote = creditNotes.find { it.documentId == id?.toInt() }
        creditNote?.let {
            val index = creditNotes.indexOf(it)
            if (documentClientOrIssuer.type == ClientOrIssuerType.CLIENT) {
                creditNotes[index] = it.copy(documentClient = documentClientOrIssuer)
            } else {
                creditNotes[index] = it.copy(documentIssuer = documentClientOrIssuer)
            }
            creditNotesFlow.value = creditNotes.toList()
        }
    }

    override suspend fun deleteDocumentClientOrIssuer(id: Long, type: ClientOrIssuerType) {
        val creditNote = creditNotes.find { it.documentId == id.toInt() }
        creditNote?.let {
            val index = creditNotes.indexOf(it)
            if (type == ClientOrIssuerType.CLIENT) {
                creditNotes[index] = it.copy(documentClient = null)
            } else {
                creditNotes[index] = it.copy(documentIssuer = null)
            }
            creditNotesFlow.value = creditNotes.toList()
        }
    }

    override suspend fun duplicate(documents: List<CreditNoteState>) {
        documents.forEach { doc ->
            val newId = nextCreditNoteId++
            val duplicate = doc.copy(
                documentId = newId,
                documentNumber = TextFieldValue("AV-${newId.toString().padStart(3, '0')}"),
                documentTag = DocumentTag.DRAFT
            )
            creditNotes.add(duplicate)
        }
        creditNotesFlow.value = creditNotes.toList()
    }

    override suspend fun convertInvoiceToCreditNote(documents: List<InvoiceState>) {
        documents.forEach { invoice ->
            val newId = nextCreditNoteId++
            val todayFormatted = DateUtils.getCurrentDateFormatted()
            val newCreditNote = CreditNoteState(
                documentId = newId,
                documentNumber = TextFieldValue("AV-${newId.toString().padStart(3, '0')}"),
                documentDate = todayFormatted,
                documentTag = DocumentTag.DRAFT,
                documentClient = invoice.documentClient,
                documentIssuer = invoice.documentIssuer,
                documentProducts = invoice.documentProducts?.map { product ->
                    product.copy(
                        linkedDate = invoice.documentDate,
                        linkedDocNumber = invoice.documentNumber.text
                    )
                },
                linkedInvoice = invoice,
                createdDate = DateUtils.getCurrentTimestamp()
            )
            creditNotes.add(newCreditNote)
        }
        creditNotesFlow.value = creditNotes.toList()
    }

    override suspend fun update(document: CreditNoteState) {
        val index = creditNotes.indexOfFirst { it.documentId == document.documentId }
        if (index >= 0) {
            creditNotes[index] = document
            creditNotesFlow.value = creditNotes.toList()
        }
    }

    override suspend fun delete(documents: List<CreditNoteState>) {
        val idsToDelete = documents.mapNotNull { it.documentId }
        creditNotes.removeAll { it.documentId in idsToDelete }
        creditNotesFlow.value = creditNotes.toList()
    }

    override suspend fun updateDocumentProductsOrderInDb(
        documentId: Long,
        orderedProducts: List<DocumentProductState>
    ) {
        val index = creditNotes.indexOfFirst { it.documentId == documentId.toInt() }
        if (index >= 0) {
            val reorderedProducts = orderedProducts.mapIndexed { i, product ->
                product.copy(sortOrder = i)
            }
            creditNotes[index] = creditNotes[index].copy(documentProducts = reorderedProducts)
            creditNotesFlow.value = creditNotes.toList()
        }
    }
}
