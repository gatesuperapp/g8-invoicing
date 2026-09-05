package com.a4a.g8invoicing.ui.viewmodels

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.QuoteLocalDataSourceInterface
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.data.calculateDocumentPrices
import com.a4a.g8invoicing.data.models.defaultRetentionsForIssuer
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.retention_default_label
import com.a4a.g8invoicing.shared.resources.retention_default_mx_isr
import com.a4a.g8invoicing.shared.resources.retention_default_mx_iva
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.QuoteState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import org.jetbrains.compose.resources.getString
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class QuoteAddEditViewModel(
    private val documentDataSource: QuoteLocalDataSourceInterface,
    private val documentProductDataSource: ProductLocalDataSourceInterface,
    private val itemId: String?,
) : ViewModel() {
    private var fetchJob: Job? = null
    private var createNewJob: Job? = null
    private var saveJob: Job? = null
    private var updateJob: Job? = null
    private var deleteJob: Job? = null
    private var autoSaveJob: Job? = null

    private val _documentUiState = MutableStateFlow(QuoteState())
    val quoteUiState: StateFlow<QuoteState> = _documentUiState

    init {
        autoSaveInLocalDb()
        try {
            itemId?.let {
                fetchQuoteFromLocalDb(it.toLong())
            } ?: viewModelScope.launch {
                createNewQuoteInVM()?.let {
                    fetchQuoteFromLocalDb(it)
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
                .collect { updateQuoteInLocalDb() }
        }
    }

    private fun fetchQuoteFromLocalDb(id: Long) {
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
            fetchQuoteFromLocalDb(it.toLong())
        }
    }

    // See InvoiceAddEditViewModel.clearRetentionsInDb.
    suspend fun clearRetentionsInDb() {
        _documentUiState.value.documentId?.toLong()?.let { id ->
            documentDataSource.deleteAllRetentions(id)
        }
    }

    // See InvoiceAddEditViewModel.seedDefaultRetentionsInDb.
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

    // Retention CRUD. Mirrors the invoice/creditnote hooks — every mutation
    // refreshes documentTotalPrices so the totals block updates in the same
    // frame; the autoSave debounce then persists to DB.
    fun updateRetentionAt(
        index: Int,
        retention: com.a4a.g8invoicing.ui.states.RetentionState,
    ) {
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

    private suspend fun createNewQuoteInVM(): Long? {
        var quoteId: Long? = null
        val createNewJob = viewModelScope.launch {
            try {
                quoteId = documentDataSource.createNew()
            } catch (e: Exception) {
                // Error handling
            }
        }
        createNewJob.join()
        return quoteId
    }

    fun updateUiState(screenElement: ScreenElement, value: Any) {
        _documentUiState.value =
            updateQuoteUiState(_documentUiState.value, screenElement, value)
    }

    private fun updateQuoteInLocalDb() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            try {
                documentDataSource.update(quoteUiState.value)
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            updateQuoteInLocalDb()
        }
    }

    // Mirror of InvoiceAddEditViewModel.setDocumentFont — persists font_family
    // on the quote row via the standard update path.
    fun setDocumentFont(fontId: String?) {
        _documentUiState.value = _documentUiState.value.copy(fontFamily = fontId)
        updateQuoteInLocalDb()
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

    fun updateDocumentProductsOrderInUiStateAndDb(updatedProducts: List<DocumentProductState>) {
        viewModelScope.launch {
            try {
                val productsWithUpdatedSortOrder = updatedProducts.mapIndexed { index, product ->
                    product.copy(sortOrder = index)
                }

                _documentUiState.value = _documentUiState.value.copy(
                    documentProducts = productsWithUpdatedSortOrder
                )

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
        try {
            val list = _documentUiState.value.documentProducts
            var maxId = 1

            if (!list.isNullOrEmpty()) {
                maxId = list.mapNotNull { it.id }.max()
            }

            if (documentProduct.id == null) {
                documentProduct.id = maxId + 1
            }

            val newList: List<DocumentProductState> = (list ?: emptyList()) + documentProduct

            _documentUiState.value = _documentUiState.value.copy(
                documentProducts = newList
            )
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

    fun saveDocumentClientOrIssuerInLocalDb(documentClientOrIssuer: ClientOrIssuerState) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            try {
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
                    documentId = _documentUiState.value.documentId?.toLong()
                )
                // Reload the document to get the correct DocumentClientOrIssuer ID
                _documentUiState.value.documentId?.let {
                    fetchQuoteFromLocalDb(it.toLong())
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
                    fetchQuoteFromLocalDb(it.toLong())
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
        else _documentUiState.value = _documentUiState.value.copy(
            documentIssuer = documentClientOrIssuer
        )
    }

    fun updateTextFieldCursorOfQuoteState(pageElement: ScreenElement) {
        val text = when (pageElement) {
            ScreenElement.DOCUMENT_NUMBER -> quoteUiState.value.documentNumber.text
            ScreenElement.DOCUMENT_REFERENCE -> quoteUiState.value.reference?.text
            else -> null
        }

        _documentUiState.value = updateQuoteUiState(
            _documentUiState.value, pageElement, TextFieldValue(
                text = text ?: "",
                selection = TextRange(text?.length ?: 0)
            )
        )
    }
}

fun updateQuoteUiState(
    quote: QuoteState,
    element: ScreenElement,
    value: Any,
): QuoteState {
    var doc = quote
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

        ScreenElement.DOCUMENT_FOOTER -> {
            doc = doc.copy(footerText = value as TextFieldValue)
        }

        // Payment blocks — parity with InvoiceAddEditViewModel. Same
        // ScreenElement identifiers are dispatched via the shared
        // DocumentBottomSheetTextElements / picker branches, and the
        // shared VM contract routes the update event to whichever VM
        // owns the current doc type.
        ScreenElement.DOCUMENT_PAYMENT_MEANS_LABEL -> {
            @Suppress("UNCHECKED_CAST")
            val newSegments = value as List<com.a4a.g8invoicing.data.models.PaymentLabelSegment>
            doc = doc.copy(
                paymentMeansSegments = newSegments,
                paymentMeansSelections = com.a4a.g8invoicing.data.models
                    .chipIdsFromSegments(newSegments)
                    .takeIf { it.isNotEmpty() },
            )
        }
        ScreenElement.DOCUMENT_PAYMENT_MEANS_HIDDEN -> {
            doc = doc.copy(paymentMeansHidden = value as Boolean)
        }
        ScreenElement.DOCUMENT_PAYMENT_MEANS_OTHER -> {
            doc = doc.copy(paymentMeansOtherChecked = value as Boolean)
        }
        ScreenElement.DOCUMENT_PAYMENT_BANK_HIDDEN -> {
            doc = doc.copy(paymentBankHidden = value as Boolean)
        }
        ScreenElement.DOCUMENT_PAYMENT_BANK_LABEL -> {
            @Suppress("UNCHECKED_CAST")
            doc = doc.copy(
                paymentBankSegments = value as List<com.a4a.g8invoicing.data.models.PaymentBankSegment>,
            )
        }
        ScreenElement.DOCUMENT_ISSUER_BANK_PICKED -> {
            val bank = value as com.a4a.g8invoicing.ui.states.IssuerBankState
            doc.documentIssuer?.let { currentIssuer ->
                doc = doc.copy(
                    documentIssuer = currentIssuer.copy(
                        paymentIban = bank.identifier.text.takeIf { it.isNotEmpty() }
                            ?.let { TextFieldValue(text = it) },
                        paymentBic = bank.bic.text.takeIf { it.isNotEmpty() }
                            ?.let { TextFieldValue(text = it) },
                        paymentCountry = bank.countryCode?.takeIf { it.isNotEmpty() },
                    )
                )
            }
        }
        ScreenElement.DOCUMENT_PAYMENT_TERMS_RECOVERY_FEES -> {
            doc = doc.copy(paymentTermsRecoveryFees = value as TextFieldValue)
        }
        ScreenElement.DOCUMENT_PAYMENT_TERMS_LATE_FEES -> {
            doc = doc.copy(paymentTermsLateFees = value as TextFieldValue)
        }
        ScreenElement.DOCUMENT_PAYMENT_TERMS_DISCOUNT -> {
            doc = doc.copy(paymentTermsDiscount = value as TextFieldValue)
        }
        ScreenElement.DOCUMENT_VAT_EXEMPTION -> {
            doc = doc.copy(vatExemptionText = (value as TextFieldValue))
        }

        else -> {}
    }
    return doc
}
