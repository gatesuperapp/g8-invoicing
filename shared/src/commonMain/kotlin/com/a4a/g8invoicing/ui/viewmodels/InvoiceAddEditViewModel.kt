package com.a4a.g8invoicing.ui.viewmodels

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.InvoiceLocalDataSourceInterface
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.data.calculateDocumentPrices
import com.a4a.g8invoicing.data.models.defaultRetentionsForIssuer
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.retention_default_label
import com.a4a.g8invoicing.shared.resources.retention_default_mx_isr
import com.a4a.g8invoicing.shared.resources.retention_default_mx_iva
import org.jetbrains.compose.resources.getString
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InvoiceAddEditViewModel(
    private val documentDataSource: InvoiceLocalDataSourceInterface,
    private val documentProductDataSource: ProductLocalDataSourceInterface,
    private val itemId: String?,
) : ViewModel() {
    private var fetchJob: Job? = null
    private var saveJob: Job? = null
    private var updateJob: Job? = null
    private var deleteJob: Job? = null
    private var autoSaveJob: Job? = null

    private val _documentUiState = MutableStateFlow(InvoiceState())
    val documentUiState: StateFlow<InvoiceState> = _documentUiState

    init {
        autoSaveInLocalDb()
        try {
            itemId?.let {
                fetchInvoiceFromLocalDb(it.toLong())
            } ?: viewModelScope.launch {
                createNewInvoiceInVM()?.let {
                    fetchInvoiceFromLocalDb(it)
                }
            }
        } catch (e: Exception) {
            // Error handling
        }
    }

    @OptIn(FlowPreview::class)
    private fun autoSaveInLocalDb() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            _documentUiState.debounce(300)
                .collect {
                    updateInvoiceInLocalDb()
                }
        }
    }

    private fun fetchInvoiceFromLocalDb(id: Long) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                documentDataSource.fetch(id)?.let {
                    _documentUiState.value = it
                }
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    fun reloadDocument() {
        _documentUiState.value.documentId?.let {
            fetchInvoiceFromLocalDb(it.toLong())
        }
    }

    // Called by the NavGraph when EDIT_ISSUER turns off the tax-withholding
    // switch: the direct-to-DB update path doesn't go through
    // saveDocumentClientOrIssuerInUiState, so the retention wipe must be
    // done explicitly before reloadDocument reads DB back into state.
    suspend fun clearRetentionsInDb() {
        _documentUiState.value.documentId?.toLong()?.let { id ->
            documentDataSource.deleteAllRetentions(id)
        }
    }

    // ON transition on EDIT_ISSUER: seed the country default(s) directly to DB
    // (same rule as createNew / the picker), so the retention row shows up
    // immediately after reloadDocument.
    suspend fun seedDefaultRetentionsInDb(issuer: ClientOrIssuerState) {
        val id = _documentUiState.value.documentId?.toLong() ?: return
        val defaults = defaultRetentionsForIssuer(
            issuer,
            getString(Res.string.retention_default_label),
            getString(Res.string.retention_default_mx_isr),
            getString(Res.string.retention_default_mx_iva),
        )
        if (defaults.isNotEmpty()) {
            documentDataSource.saveRetentions(id, defaults)
        }
    }

    private suspend fun createNewInvoiceInVM(): Long? {
        var documentId: Long? = null
        val createNewJob = viewModelScope.launch {
            try {
                documentId = documentDataSource.createNew()
            } catch (e: Exception) {
                // Error handling
            }
        }
        createNewJob.join()
        return documentId
    }

    fun updateUiState(screenElement: ScreenElement, value: Any) {
        _documentUiState.value =
            updateInvoiceUiState(_documentUiState.value, screenElement, value)
    }

    // Retention CRUD. Every mutation also refreshes documentTotalPrices so
    // the totals block updates in the same frame; autoSave persists on debounce.
    fun updateRetentionAt(index: Int, retention: com.a4a.g8invoicing.ui.states.RetentionState) {
        val current = _documentUiState.value.retentions.toMutableList()
        if (index in current.indices) {
            current[index] = retention.copy(sortOrder = index)
            _documentUiState.value = _documentUiState.value.copy(
                retentions = current,
                documentTotalPrices = calculateDocumentPrices(
                    _documentUiState.value.documentProducts ?: emptyList(),
                    current,
                ),
            )
        }
    }

    fun toggleRetentionHiddenAt(index: Int) {
        val current = _documentUiState.value.retentions.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(hidden = !current[index].hidden)
            _documentUiState.value = _documentUiState.value.copy(
                retentions = current,
                documentTotalPrices = calculateDocumentPrices(
                    _documentUiState.value.documentProducts ?: emptyList(),
                    current,
                ),
            )
        }
    }

    // Flip the "hide linked source headers" bit on the current invoice: mirror
    // it into the UI state immediately so the eye + preview update in the same
    // frame, then persist in the background.
    fun toggleHideLinkedSourceHeaders() {
        val current = _documentUiState.value
        val next = !current.hideLinkedSourceHeaders
        _documentUiState.value = current.copy(hideLinkedSourceHeaders = next)
        val id = current.documentId?.toLong() ?: return
        viewModelScope.launch {
            documentDataSource.updateHideLinkedSourceHeaders(id, next)
        }
    }

    private fun updateInvoiceInLocalDb() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            try {
                documentDataSource.update(documentUiState.value)
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            updateInvoiceInLocalDb()
        }
    }

    suspend fun saveDocumentProductInLocalDbAndGetId(documentProduct: DocumentProductState): Int? {
        val currentDocumentId = _documentUiState.value.documentId?.toLong()
        if (currentDocumentId == null) {
            return null
        }

        return try {
            documentDataSource.saveDocumentProductInDbAndLinkToDocument(
                documentProduct = documentProduct,
                documentId = currentDocumentId
            )
        } catch (e: Exception) {
            null
        }
    }

    fun removeDocumentProductFromLocalDb(documentProductId: Int) {
        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            try {
                _documentUiState.value.documentId?.let {
                    documentDataSource.deleteDocumentProduct(
                        it.toLong(),
                        documentProductId.toLong()
                    )
                }
                documentProductDataSource.deleteDocumentProducts(listOf(documentProductId.toLong()))
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    fun removeDocumentProductFromUiState(documentProductId: Int) {
        try {
            val list = _documentUiState.value.documentProducts
                ?.filterNot { it.id == documentProductId }?.toMutableList()
            _documentUiState.value = _documentUiState.value.copy(
                documentProducts = list
            )

            // Recalculate the prices
            _documentUiState.value.documentProducts?.let {
                _documentUiState.value =
                    _documentUiState.value.copy(
                        documentTotalPrices = calculateDocumentPrices(it, _documentUiState.value.retentions)
                    )
            }
        } catch (e: Exception) {
            // Error handling
        }
    }

    fun saveDocumentProductInUiState(documentProduct: DocumentProductState) {
        if (documentProduct.id == null) {
            println("Warning: Attempting to save DocumentProduct in db.")
        }

        val currentList = _documentUiState.value.documentProducts ?: emptyList()
        val newList = ArrayList(currentList)

        // Ajouter comme nouveau produit
        newList.add(documentProduct)

        _documentUiState.update { currentState ->
            currentState.copy(
                documentProducts = newList.toList(),
                documentTotalPrices = calculateDocumentPrices(newList.toList(), currentState.retentions)
            )
        }
    }

    fun updateDocumentProductsOrderInUiStateAndDb(updatedProducts: List<DocumentProductState>) {
        viewModelScope.launch {
            try {
                // 1. Mettre à jour sortOrder dans la liste pour l'UI et pour la BDD
                val productsWithUpdatedSortOrder = updatedProducts.mapIndexed { index, product ->
                    product.copy(sortOrder = index)
                }

                // 2. Mettre à jour l'état de l'UI
                _documentUiState.value = _documentUiState.value.copy(
                    documentProducts = productsWithUpdatedSortOrder
                )

                // 3. Mettre à jour l'ordre dans la base de données locale
                val documentId = _documentUiState.value.documentId?.toLong()
                if (documentId != null) {
                    documentDataSource.updateDocumentProductsOrderInDb(
                        documentId = documentId,
                        orderedProducts = productsWithUpdatedSortOrder
                    )
                }
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    fun saveDocumentClientOrIssuerInLocalDb(documentClientOrIssuer: ClientOrIssuerState) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            try {
                // Drop the previous document snapshot of the same role first so the
                // fetch below can't hand back the stale row (firstOrNull on the join
                // would otherwise return the older insert, causing the picker to
                // flicker back to the old selection).
                val documentType = when (documentClientOrIssuer.type) {
                    ClientOrIssuerType.CLIENT, ClientOrIssuerType.DOCUMENT_CLIENT ->
                        ClientOrIssuerType.DOCUMENT_CLIENT
                    ClientOrIssuerType.ISSUER, ClientOrIssuerType.DOCUMENT_ISSUER ->
                        ClientOrIssuerType.DOCUMENT_ISSUER
                    else -> null
                }
                _documentUiState.value.documentId?.let { docId ->
                    documentType?.let {
                        documentDataSource.deleteDocumentClientOrIssuer(docId.toLong(), it)
                    }
                }
                documentDataSource.saveDocumentClientOrIssuerInDbAndLinkToDocument(
                    documentClientOrIssuer = documentClientOrIssuer,
                    id = _documentUiState.value.documentId?.toLong()
                )
                // Reload the document to get the correct DocumentClientOrIssuer ID
                _documentUiState.value.documentId?.let {
                    fetchInvoiceFromLocalDb(it.toLong())
                }
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    fun removeDocumentClientOrIssuerFromLocalDb(type: ClientOrIssuerType) {
        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            try {
                _documentUiState.value.documentId?.let {
                    documentDataSource.deleteDocumentClientOrIssuer(
                        it.toLong(),
                        type
                    )
                }

                _documentUiState.value.documentId?.let {
                    fetchInvoiceFromLocalDb(it.toLong())
                }

            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    fun removeDocumentClientOrIssuerFromUiState(type: ClientOrIssuerType) {
        if (type == ClientOrIssuerType.DOCUMENT_CLIENT)
            _documentUiState.value = _documentUiState.value.copy(
                documentClient = null
            )
        else _documentUiState.value = _documentUiState.value.copy(
            documentIssuer = null
        )
    }

    fun saveDocumentClientOrIssuerInUiState(documentClientOrIssuer: ClientOrIssuerState) {
        if (documentClientOrIssuer.type == ClientOrIssuerType.DOCUMENT_CLIENT)
            _documentUiState.value = _documentUiState.value.copy(
                documentClient = documentClientOrIssuer
            )
        else {
            _documentUiState.value = _documentUiState.value.copy(
                documentIssuer = documentClientOrIssuer
            )
            // Toggle-driven: ON seeds country defaults, OFF wipes. The switch
            // is the only way to remove lines (no per-line delete in the UI).
            val hasRetentions = _documentUiState.value.retentions.isNotEmpty()
            if (documentClientOrIssuer.taxWithholdingEnabled && !hasRetentions) {
                viewModelScope.launch {
                    val defaults = defaultRetentionsForIssuer(
                        documentClientOrIssuer,
                        getString(Res.string.retention_default_label),
                        getString(Res.string.retention_default_mx_isr),
                        getString(Res.string.retention_default_mx_iva),
                    )
                    if (_documentUiState.value.retentions.isEmpty() &&
                        _documentUiState.value.documentIssuer?.taxWithholdingEnabled == true
                    ) {
                        _documentUiState.value = _documentUiState.value.copy(
                            retentions = defaults,
                            documentTotalPrices = calculateDocumentPrices(
                                _documentUiState.value.documentProducts ?: emptyList(),
                                defaults,
                            ),
                        )
                    }
                }
            } else if (!documentClientOrIssuer.taxWithholdingEnabled && hasRetentions) {
                _documentUiState.value = _documentUiState.value.copy(
                    retentions = emptyList(),
                    documentTotalPrices = calculateDocumentPrices(
                        _documentUiState.value.documentProducts ?: emptyList(),
                        emptyList(),
                    ),
                )
            }
        }
    }

    fun updateTextFieldCursorOfInvoiceState(pageElement: ScreenElement) {
        val text = when (pageElement) {
            ScreenElement.DOCUMENT_NUMBER -> documentUiState.value.documentNumber.text
            ScreenElement.DOCUMENT_REFERENCE -> documentUiState.value.reference?.text
            else -> null
        }

        _documentUiState.value = updateInvoiceUiState(
            _documentUiState.value, pageElement, TextFieldValue(
                text = text ?: "",
                selection = TextRange(text?.length ?: 0)
            )
        )
    }

    private fun updateInvoiceUiState(
        document: InvoiceState,
        element: ScreenElement,
        value: Any,
    ): InvoiceState {
        var doc = document
        when (element) {
            ScreenElement.DOCUMENT_NUMBER -> {
                doc = doc.copy(documentNumber = value as TextFieldValue)
            }

            ScreenElement.DOCUMENT_DATE -> {
                doc = doc.copy(documentDate = value as String)
            }

            ScreenElement.DOCUMENT_CLIENT -> {
                doc = doc.copy(documentClient = value as ClientOrIssuerState)
            }

            ScreenElement.DOCUMENT_ISSUER -> {
                doc = doc.copy(documentIssuer = value as ClientOrIssuerState)
            }

            ScreenElement.DOCUMENT_REFERENCE -> {
                doc = doc.copy(reference = value as TextFieldValue)
            }

            ScreenElement.DOCUMENT_FREE_FIELD -> {
                doc = doc.copy(freeField = value as TextFieldValue)
            }

            ScreenElement.DOCUMENT_PRODUCT -> {
                updateDocumentProductList(
                    value as DocumentProductState,
                    doc
                )?.let {
                    doc = doc.copy(documentProducts = it)
                    doc = doc.copy(documentTotalPrices = calculateDocumentPrices(it, doc.retentions))
                }
            }

            ScreenElement.DOCUMENT_CURRENCY -> {
                doc = doc.copy(currency = value as TextFieldValue)
            }

            ScreenElement.DOCUMENT_DUE_DATE -> {
                doc = doc.copy(dueDate = value as String)
            }

            ScreenElement.DOCUMENT_FOOTER -> {
                doc = doc.copy(footerText = value as TextFieldValue)
            }

            else -> {}
        }
        return doc
    }
}

fun updateDocumentProductList(
    value: DocumentProductState,
    doc: DocumentState,
): MutableList<DocumentProductState>? {
    val productIndex = doc.documentProducts?.indexOfFirst { it.id == value.id } ?: 0
    val list =
        doc.documentProducts?.filterNot { it.id == value.id }?.toMutableList()

    list?.add(productIndex, value)
    return list
}
