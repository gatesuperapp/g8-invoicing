package com.a4a.g8invoicing.fakes

import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.data.DeliveryNoteLocalDataSourceInterface
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.util.DateUtils
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeDeliveryNoteDataSource : DeliveryNoteLocalDataSourceInterface {

    private val deliveryNotes = mutableListOf<DeliveryNoteState>()
    private val deliveryNotesFlow = MutableStateFlow<List<DeliveryNoteState>>(emptyList())
    private var nextDeliveryNoteId = 1
    private var nextProductId = 1

    // For testing: access internal state
    fun getDeliveryNotes(): List<DeliveryNoteState> = deliveryNotes.toList()
    fun getDeliveryNoteCount(): Int = deliveryNotes.size
    fun clear() {
        deliveryNotes.clear()
        deliveryNotesFlow.value = emptyList()
        nextDeliveryNoteId = 1
        nextProductId = 1
    }

    override suspend fun fetch(id: Long): DeliveryNoteState? {
        return deliveryNotes.find { it.documentId == id.toInt() }
    }

    override fun fetchAll(): Flow<List<DeliveryNoteState>> {
        return deliveryNotesFlow
    }

    override suspend fun createNew(): Long {
        val todayFormatted = DateUtils.getCurrentDateFormatted()
        val newDeliveryNote = DeliveryNoteState(
            documentId = nextDeliveryNoteId,
            documentNumber = TextFieldValue("BL-${nextDeliveryNoteId.toString().padStart(3, '0')}"),
            documentDate = todayFormatted,
            documentTag = DocumentTag.DRAFT,
            createdDate = DateUtils.getCurrentTimestamp()
        )
        deliveryNotes.add(newDeliveryNote)
        deliveryNotesFlow.value = deliveryNotes.toList()
        return nextDeliveryNoteId++.toLong()
    }

    override suspend fun saveDocumentProductInDbAndLinkToDocument(
        documentProduct: DocumentProductState,
        documentId: Long
    ): Int {
        val deliveryNote = deliveryNotes.find { it.documentId == documentId.toInt() }
        deliveryNote?.let {
            val productId = nextProductId++
            val product = documentProduct.copy(id = productId)
            val currentProducts = it.documentProducts?.toMutableList() ?: mutableListOf()
            currentProducts.add(product)
            val index = deliveryNotes.indexOf(it)
            deliveryNotes[index] = it.copy(documentProducts = currentProducts)
            deliveryNotesFlow.value = deliveryNotes.toList()
            return productId
        }
        return -1
    }

    override suspend fun deleteDocumentProduct(documentId: Long, documentProductId: Long) {
        val deliveryNote = deliveryNotes.find { it.documentId == documentId.toInt() }
        deliveryNote?.let {
            val updatedProducts = it.documentProducts?.filterNot { product ->
                product.id == documentProductId.toInt()
            }
            val index = deliveryNotes.indexOf(it)
            deliveryNotes[index] = it.copy(documentProducts = updatedProducts)
            deliveryNotesFlow.value = deliveryNotes.toList()
        }
    }

    override suspend fun saveDocumentClientOrIssuerInDbAndLinkToDocument(
        documentClientOrIssuer: ClientOrIssuerState,
        documentId: Long?
    ) {
        val deliveryNote = deliveryNotes.find { it.documentId == documentId?.toInt() }
        deliveryNote?.let {
            val index = deliveryNotes.indexOf(it)
            if (documentClientOrIssuer.type == ClientOrIssuerType.CLIENT) {
                deliveryNotes[index] = it.copy(documentClient = documentClientOrIssuer)
            } else {
                deliveryNotes[index] = it.copy(documentIssuer = documentClientOrIssuer)
            }
            deliveryNotesFlow.value = deliveryNotes.toList()
        }
    }

    override suspend fun deleteDocumentClientOrIssuer(documentId: Long, type: ClientOrIssuerType) {
        val deliveryNote = deliveryNotes.find { it.documentId == documentId.toInt() }
        deliveryNote?.let {
            val index = deliveryNotes.indexOf(it)
            if (type == ClientOrIssuerType.CLIENT) {
                deliveryNotes[index] = it.copy(documentClient = null)
            } else {
                deliveryNotes[index] = it.copy(documentIssuer = null)
            }
            deliveryNotesFlow.value = deliveryNotes.toList()
        }
    }

    override suspend fun duplicate(documents: List<DeliveryNoteState>) {
        documents.forEach { doc ->
            val newId = nextDeliveryNoteId++
            val duplicate = doc.copy(
                documentId = newId,
                documentNumber = TextFieldValue("BL-${newId.toString().padStart(3, '0')}"),
                documentTag = DocumentTag.DRAFT
            )
            deliveryNotes.add(duplicate)
        }
        deliveryNotesFlow.value = deliveryNotes.toList()
    }

    override suspend fun update(document: DeliveryNoteState) {
        val index = deliveryNotes.indexOfFirst { it.documentId == document.documentId }
        if (index >= 0) {
            deliveryNotes[index] = document
            deliveryNotesFlow.value = deliveryNotes.toList()
        }
    }

    override suspend fun delete(documents: List<DeliveryNoteState>) {
        val idsToDelete = documents.mapNotNull { it.documentId }
        deliveryNotes.removeAll { it.documentId in idsToDelete }
        deliveryNotesFlow.value = deliveryNotes.toList()
    }

    override suspend fun updateDocumentProductsOrderInDb(
        documentId: Long,
        orderedProducts: List<DocumentProductState>
    ) {
        val index = deliveryNotes.indexOfFirst { it.documentId == documentId.toInt() }
        if (index >= 0) {
            val reorderedProducts = orderedProducts.mapIndexed { i, product ->
                product.copy(sortOrder = i)
            }
            deliveryNotes[index] = deliveryNotes[index].copy(documentProducts = reorderedProducts)
            deliveryNotesFlow.value = deliveryNotes.toList()
        }
    }
}
