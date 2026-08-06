package com.a4a.g8invoicing.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSourceInterface
import com.a4a.g8invoicing.data.models.PersonType
import com.a4a.g8invoicing.data.models.ProductNature
import com.a4a.g8invoicing.data.models.UnitCode
import com.a4a.g8invoicing.data.models.UnitCodeRepository
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.data.ProductTaxLocalDataSourceInterface
import com.a4a.g8invoicing.ui.shared.FormInputsValidator
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.data.util.calculatePriceWithTax
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.ClientRef
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.ProductPrice
import com.a4a.g8invoicing.ui.states.ProductState
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.ionspin.kotlin.bignum.decimal.BigDecimal

class ProductAddEditViewModel(
    private val dataSource: ProductLocalDataSourceInterface,
    private val taxDataSource: ProductTaxLocalDataSourceInterface,
    private val clientOrIssuerDataSource: ClientOrIssuerLocalDataSourceInterface,
    private val unitCodeRepository: UnitCodeRepository,
    private val itemId: String?,
    private val type: String?,
) : ViewModel() {

    private var saveJob: Job? = null
    private var updateJob: Job? = null
    private var autoSaveJob: Job? = null
    private var taxRates: List<BigDecimal> = listOf()

    // Counter to trigger recomposition after saving tax rates
    private val _taxRatesRefreshCounter = MutableStateFlow(0)
    val taxRatesRefreshCounter: StateFlow<Int> = _taxRatesRefreshCounter.asStateFlow()

    private val _productUiState = mutableStateOf(ProductState())
    val productUiState: State<ProductState> = _productUiState

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _documentProductUiState = MutableStateFlow(DocumentProductState())
    val documentProductUiState: StateFlow<DocumentProductState> = _documentProductUiState

    private val _availableClients = MutableStateFlow<List<ClientOrIssuerState>>(emptyList())
    val availableClients: StateFlow<List<ClientOrIssuerState>> = _availableClients.asStateFlow()

    private val _clientSelectionDialogState = MutableStateFlow<ClientSelectionDialogState?>(null)
    val clientSelectionDialogState: StateFlow<ClientSelectionDialogState?> =
        _clientSelectionDialogState.asStateFlow()

    // Type visibility flag: driven by the CURRENT document's issuer, not by any
    // global "last issuer" state. Multi-company users have some issuers with
    // intra-EU sales enabled and some without — the doc bottom-sheet form must
    // reflect the actual issuer selected for THIS document. NavGraphs push the
    // value via setShowProductType() based on documentIssuerUiState.intraEuSales.
    // Type is not shown on the master Product form at all (see comment on the
    // "Type is a transaction attribute" removal in ProductAddEditForm).
    private val _showProductType = MutableStateFlow(false)
    val showProductType: StateFlow<Boolean> = _showProductType.asStateFlow()

    fun setShowProductType(value: Boolean) {
        _showProductType.value = value
    }

    init {
        // type peut arriver null lors d'un "nouveau produit" depuis l'onglet Produits.
        // On considère qu'un type absent = Product master (le seul flow qui n'est PAS
        // DocumentProduct est Product master).
        val isDocumentProduct = type == ProductType.DOCUMENT_PRODUCT.name.lowercase()
        if (isDocumentProduct) {
            _isLoading.value = false
        } else if (itemId != null) {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    dataSource.fetchProduct(itemId.toLong())?.let { fetched ->
                        // Auto-heal for legacy products (created before the unitCode field
                        // existed): try to match the free-text unit ("heure", "kg"…) to a
                        // UNECE code. Pure UI convenience — the healed value is written to
                        // state so hitting Save persists it.
                        val healed = if (fetched.unitCode == null) {
                            fetched.copy(
                                unitCode = fetched.unit?.text
                                    ?.let { unitCodeRepository.matchTextToCode(it).code },
                            )
                        } else fetched
                        _productUiState.value = healed
                    }
                } finally {
                    _isLoading.value = false
                }
            }
        } else {
            _isLoading.value = false
            // Sticky on new master product: inherit unit, unitCode, taxRate from
            // the last created product. Type is NOT inherited here — the Type row
            // is not shown on the master form in 1.8 (it can't be gated on a
            // specific issuer's intraEuSales flag without per-product company
            // scoping, which comes in 1.9).
            viewModelScope.launch {
                val last = dataSource.fetchLastCreatedProduct()
                _productUiState.value = _productUiState.value.copy(
                    unit = last?.unit ?: _productUiState.value.unit,
                    unitCode = last?.unitCode ?: _productUiState.value.unitCode,
                    taxRate = last?.taxRate ?: _productUiState.value.taxRate,
                )
            }
        }

        viewModelScope.launch {
            clientOrIssuerDataSource.fetchAll(type = PersonType.CLIENT).collect { clients ->
                _availableClients.value = clients
            }
        }
    }

    private val _last5UnitCodes = MutableStateFlow<List<String>>(emptyList())
    val last5UnitCodes: StateFlow<List<String>> = _last5UnitCodes.asStateFlow()

    fun refreshLast5UnitCodes() {
        viewModelScope.launch {
            _last5UnitCodes.value = dataSource.fetchLast5UnitCodes()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            updateInLocalDb(ProductType.DOCUMENT_PRODUCT)
        }
    }

    fun setDocumentProductUiState(documentProduct: DocumentProductState) {
        _documentProductUiState.value = documentProduct
    }

    fun setDocumentProductUiStateWithProduct(product: ProductState, clientId: Int? = null) {
        val (priceWithoutTax, priceWithTax) = if (clientId != null) {
            val clientPrice = product.additionalPrices?.find { price ->
                price.clients.any { it.id == clientId }
            }
            if (clientPrice != null) {
                Pair(clientPrice.priceWithoutTax, clientPrice.priceWithTax)
            } else {
                Pair(product.defaultPriceWithoutTax, product.defaultPriceWithTax)
            }
        } else {
            Pair(product.defaultPriceWithoutTax, product.defaultPriceWithTax)
        }

        // Set the state immediately with what's synchronously known. The unit
        // code auto-heal (only needed for pre-1.8 products with unit text but
        // no unitCode) runs asynchronously and patches state a moment later.
        _documentProductUiState.value = DocumentProductState(
            id = null,
            name = product.name,
            description = product.description,
            priceWithoutTax = priceWithoutTax,
            priceWithTax = priceWithTax,
            taxRate = product.taxRate,
            quantity = BigDecimal.ONE,
            unit = product.unit,
            unitCode = product.unitCode,
            type = product.type,
            productId = product.id,
        )
        if (product.unitCode == null && !product.unit?.text.isNullOrBlank()) {
            viewModelScope.launch {
                val matched = unitCodeRepository.matchTextToCode(product.unit?.text).code
                if (_documentProductUiState.value.unitCode == null) {
                    _documentProductUiState.value =
                        _documentProductUiState.value.copy(unitCode = matched)
                }
            }
        }

        if (product.type == null && _showProductType.value) {
            // Le produit pické (créé pre-1.8) n'a pas de type. Fallback dernier produit
            // → GOODS + warning one-shot. Skip entièrement si l'émetteur n'a pas coché
            // "Ventes intra-UE" (le champ Type n'est pas affiché de toute façon).
            viewModelScope.launch {
                val stickyType = dataSource.fetchLastCreatedProduct()?.type
                    ?: ProductNature.GOODS
                if (_documentProductUiState.value.type == null) {
                    _documentProductUiState.value =
                        _documentProductUiState.value.copy(type = stickyType)
                }
            }
        }
    }

    fun setProductUiState() {
        _productUiState.value = ProductState(
            id = null,
            name = _documentProductUiState.value.name,
            description = _documentProductUiState.value.description,
            defaultPriceWithoutTax = _documentProductUiState.value.priceWithoutTax,
            defaultPriceWithTax = _documentProductUiState.value.priceWithTax,
            taxRate = _documentProductUiState.value.taxRate,
            unit = _documentProductUiState.value.unit,
            unitCode = _documentProductUiState.value.unitCode,
            type = _documentProductUiState.value.type,
        )
    }

    fun clearProductUiState() {
        _productUiState.value = ProductState()
        _documentProductUiState.value = DocumentProductState()
        // Sticky sur inline DocumentProduct : au reset complet, on ré-hydrate depuis
        // le dernier produit créé (unit + unitCode + taxRate + type). Sinon type reste
        // null et l'user tombe sur "-" sur chaque nouvelle ligne de facture inline.
        viewModelScope.launch {
            val last = dataSource.fetchLastCreatedProduct()
            _documentProductUiState.value = _documentProductUiState.value.copy(
                unit = last?.unit,
                unitCode = last?.unitCode,
                taxRate = last?.taxRate,
                // Type seulement si l'émetteur a coché "Ventes intra-UE" — sinon inutile.
                type = if (_showProductType.value)
                    last?.type ?: ProductNature.GOODS
                else null,
            )
        }
    }

    fun clearProductNameAndDescription() {
        _productUiState.value = _productUiState.value.copy(
            name = TextFieldValue(),
            description = TextFieldValue(),
            defaultPriceWithoutTax = null,
            defaultPriceWithTax = null
        )

        // On first "+ Nouveau produit" for an intra-EU issuer, previous state is
        // empty (nothing to preserve as sticky) → type would land as null and
        // display as "-". Default to GOODS so the user has a sensible starting
        // point that they can override to SERVICE via the picker.
        val currentType = _documentProductUiState.value.type
        val defaultedType = if (_showProductType.value && currentType == null)
            ProductNature.GOODS
        else currentType

        _documentProductUiState.value = _documentProductUiState.value.copy(
            name = TextFieldValue(),
            description = TextFieldValue(),
            priceWithoutTax = null,
            priceWithTax = null,
            quantity = BigDecimal.ONE,
            type = defaultedType,
            // unit / unitCode intentionally NOT reset — sticky entre créations
        )
    }

    fun updateTaxRate(taxRate: BigDecimal?, type: ProductType) {
        if (type == ProductType.PRODUCT) {
            val updatedAdditionalPrices = _productUiState.value.additionalPrices?.map { price ->
                price.copy(
                    priceWithTax = price.priceWithoutTax?.let { ht ->
                        taxRate?.let { tax -> calculatePriceWithTax(ht, tax) }
                    }
                )
            }

            _productUiState.value = _productUiState.value.copy(
                taxRate = taxRate,
                defaultPriceWithTax = _productUiState.value.defaultPriceWithoutTax?.let { priceWithoutTax ->
                    taxRate?.let { tax ->
                        calculatePriceWithTax(priceWithoutTax, tax)
                    } ?: BigDecimal.ZERO
                },
                additionalPrices = updatedAdditionalPrices
            )
        } else {
            _documentProductUiState.value = _documentProductUiState.value.copy(
                taxRate = taxRate,
                priceWithTax = _documentProductUiState.value.priceWithoutTax?.let { priceWithoutTax ->
                    taxRate?.let { tax ->
                        calculatePriceWithTax(priceWithoutTax, tax)
                    } ?: BigDecimal.ZERO
                }
            )
        }
    }

    fun fetchTaxRatesFromLocalDb(): List<BigDecimal> {
        taxRates = taxDataSource.fetchProductTaxes()
        return taxRates
    }

    fun fetchTaxRatesWithIdsFromLocalDb(): List<Pair<Long, BigDecimal>> {
        return taxDataSource.fetchProductTaxesWithIds()
    }

    fun saveTaxRates(rates: List<Pair<Long?, BigDecimal>>) {
        viewModelScope.launch {
            try {
                // Get existing rates to determine what to update/add/delete
                val existingRates = taxDataSource.fetchProductTaxesWithIds()
                val existingIds = existingRates.map { it.first }.toSet()
                val newRateIds = rates.mapNotNull { it.first }.toSet()

                // Delete rates that are no longer in the list
                // First, clear the tax rate from products to avoid cascade delete
                existingIds.forEach { existingId ->
                    if (existingId !in newRateIds) {
                        // Clear the tax rate from products before deleting
                        dataSource.clearTaxRateFromProducts(existingId)
                        taxDataSource.deleteProductTax(existingId)
                    }
                }

                // Update existing rates and add new ones
                rates.forEach { (id, amount) ->
                    if (id != null && id in existingIds) {
                        // Update existing rate
                        taxDataSource.updateProductTax(id, amount)
                    } else if (id == null) {
                        // Add new rate
                        taxDataSource.saveProductTax(amount)
                    }
                }

                // Increment counter to trigger recomposition
                _taxRatesRefreshCounter.value++
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    fun saveProductInLocalDb() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            try {
                dataSource.saveProduct(productUiState.value)
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    /** Same as [saveProductInLocalDb] but awaits the insert and returns the new
     * master Product id. Used by the "create product from a document" flow so
     * the caller can backfill DocumentProduct.product_id in the same
     * transaction batch and keep the master ↔ document link intact from row 1. */
    suspend fun saveProductInLocalDbAndGetId(): Long? {
        return try {
            dataSource.saveProduct(productUiState.value)
        } catch (_: Exception) {
            null
        }
    }

    fun updateInLocalDb(type: ProductType) {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            try {
                if (type == ProductType.PRODUCT) {
                    dataSource.updateProduct(productUiState.value)
                } else {
                    dataSource.updateDocumentProduct(documentProductUiState.value)
                }
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    /** Called after saving a DocumentProduct edit when the user ticked the
     *  "Also apply to the product card" switch. Pushes the edited fields back
     *  to the master Product row. Runs on the same scope as updateInLocalDb
     *  so callers can just await both in order. */
    suspend fun syncDocumentProductToMaster() {
        try {
            dataSource.syncMasterFromDocumentProduct(documentProductUiState.value)
        } catch (_: Exception) {
        }
    }

    fun updateProductState(
        pageElement: ScreenElement,
        value: Any,
        productType: ProductType,
        idStr: String? = null,
    ) {
        // Unit-related edits go through the async path so we can hit the
        // suspend UnitCodeRepository (index-load is suspend). The rest stays
        // synchronous.
        when (pageElement) {
            ScreenElement.PRODUCT_UNIT, ScreenElement.DOCUMENT_PRODUCT_UNIT ->
                onUnitFreeTextChange(pageElement, value as TextFieldValue, productType)
            ScreenElement.PRODUCT_UNIT_CODE, ScreenElement.DOCUMENT_PRODUCT_UNIT_CODE ->
                onUnitCodePicked(pageElement, value as String, productType)
            else -> {
                if (productType == ProductType.PRODUCT) {
                    _productUiState.value =
                        updateProductUiState(_productUiState.value, pageElement, value, idStr)
                } else {
                    _documentProductUiState.value =
                        updateDocumentProductUiState(_documentProductUiState.value, pageElement, value)
                }
            }
        }
    }

    // Free-text unit sync: set the text field immediately, then resolve the
    // Factur-X code in the background. Clearing the code up-front avoids a
    // "ghost code" from a previous keystroke sticking after the user retyped
    // something that no longer matches — the async resolve will fill in C62
    // (EN 16931 default) if nothing else matches.
    private fun onUnitFreeTextChange(
        pageElement: ScreenElement,
        text: TextFieldValue,
        productType: ProductType,
    ) {
        if (productType == ProductType.PRODUCT) {
            _productUiState.value = _productUiState.value.copy(unit = text, unitCode = null)
        } else {
            _documentProductUiState.value = _documentProductUiState.value.copy(unit = text, unitCode = null)
        }
        viewModelScope.launch {
            val matched = unitCodeRepository.matchTextToCode(text.text).code
            if (productType == ProductType.PRODUCT) {
                // Guard against a stale coroutine: only commit if the text
                // field hasn't changed since we launched.
                if (_productUiState.value.unit?.text == text.text) {
                    _productUiState.value = _productUiState.value.copy(unitCode = matched)
                }
            } else {
                if (_documentProductUiState.value.unit?.text == text.text) {
                    _documentProductUiState.value =
                        _documentProductUiState.value.copy(unitCode = matched)
                }
            }
        }
    }

    // Pick from the bottom-sheet: code arrives ready. Sync the text field to
    // the localized short form / symbol so what the user sees matches what
    // they picked.
    private fun onUnitCodePicked(
        pageElement: ScreenElement,
        code: String,
        productType: ProductType,
    ) {
        if (productType == ProductType.PRODUCT) {
            _productUiState.value = _productUiState.value.copy(unitCode = code)
        } else {
            _documentProductUiState.value = _documentProductUiState.value.copy(unitCode = code)
        }
        viewModelScope.launch {
            val label = unitCodeRepository.resolveShort(code) ?: code
            if (productType == ProductType.PRODUCT) {
                if (_productUiState.value.unitCode == code) {
                    _productUiState.value = _productUiState.value.copy(unit = TextFieldValue(label))
                }
            } else {
                if (_documentProductUiState.value.unitCode == code) {
                    _documentProductUiState.value =
                        _documentProductUiState.value.copy(unit = TextFieldValue(label))
                }
            }
        }
    }

    fun updateCursor(pageElement: ScreenElement, productType: ProductType) {
        if (productType == ProductType.PRODUCT) {
            updateCursorOfProductState(pageElement)
        } else {
            updateCursorOfDocumentProductState(pageElement)
        }
    }

    private fun updateCursorOfProductState(pageElement: ScreenElement) {
        val text = when (pageElement) {
            ScreenElement.PRODUCT_NAME -> productUiState.value.name.text
            ScreenElement.PRODUCT_DESCRIPTION -> productUiState.value.description?.text ?: ""
            else -> ""
        }
        _productUiState.value = updateProductUiState(
            _productUiState.value, pageElement, TextFieldValue(
                text = text,
                selection = TextRange(text.length)
            )
        )
    }

    private fun updateCursorOfDocumentProductState(pageElement: ScreenElement) {
        val input = when (pageElement) {
            ScreenElement.DOCUMENT_PRODUCT_NAME -> documentProductUiState.value.name.text
            ScreenElement.DOCUMENT_PRODUCT_QUANTITY -> documentProductUiState.value.quantity
            ScreenElement.DOCUMENT_PRODUCT_DESCRIPTION -> documentProductUiState.value.description?.text ?: ""
            ScreenElement.DOCUMENT_PRODUCT_UNIT -> documentProductUiState.value.unit?.text ?: ""
            else -> ""
        }
        _documentProductUiState.value = updateDocumentProductUiState(
            _documentProductUiState.value,
            pageElement,
            TextFieldValue(
                text = input.toString(),
                selection = TextRange(input?.toString()?.length ?: 0)
            )
        )
    }

    fun validateInputs(type: ProductType): Boolean {
        val listOfErrors: MutableList<Pair<ScreenElement, String?>> = mutableListOf()
        when (type) {
            ProductType.PRODUCT -> {
                FormInputsValidator.validateName(_productUiState.value.name.text)?.let {
                    listOfErrors.add(Pair(ScreenElement.PRODUCT_NAME, it))
                }
                _productUiState.value = _productUiState.value.copy(errors = listOfErrors)
            }

            ProductType.DOCUMENT_PRODUCT -> {
                FormInputsValidator.validateName(_documentProductUiState.value.name.text)?.let {
                    listOfErrors.add(Pair(ScreenElement.DOCUMENT_PRODUCT_NAME, it))
                }
                _documentProductUiState.value = _documentProductUiState.value.copy(errors = listOfErrors)
            }
        }
        return listOfErrors.isEmpty()
    }

    fun clearValidateInputErrors(type: ProductType) {
        when (type) {
            ProductType.PRODUCT -> productUiState.value.errors.clear()
            ProductType.DOCUMENT_PRODUCT -> documentProductUiState.value.errors.clear()
        }
    }

    fun addPrice() {
        val currentProduct = _productUiState.value
        val newPrices = currentProduct.additionalPrices?.plus(ProductPrice()) ?: listOf(ProductPrice())
        _productUiState.value = _productUiState.value.copy(additionalPrices = newPrices)
    }

    fun deletePrice(priceId: String) {
        val currentProduct = _productUiState.value
        val updatedPrices = currentProduct.additionalPrices?.filterNot { it.idStr == priceId }
        val finalPrices = if (updatedPrices.isNullOrEmpty()) null else updatedPrices
        _productUiState.value = _productUiState.value.copy(additionalPrices = finalPrices)
    }

    fun openClientSelectionDialog(priceId: String) {
        val price = _productUiState.value.additionalPrices?.find { it.idStr == priceId } ?: return

        val allClients = _availableClients.value.mapNotNull {
            val id = it.id ?: return@mapNotNull null
            ClientRef(
                id = id,
                firstName = it.firstName?.text,
                name = it.name.text
            )
        }
        _clientSelectionDialogState.value = ClientSelectionDialogState(
            priceId = priceId,
            selectedClients = price.clients,
            availableClients = allClients
        )
    }

    fun closeClientSelectionDialog() {
        _clientSelectionDialogState.value = null
    }

    fun toggleClientSelection(client: ClientRef) {
        val state = _clientSelectionDialogState.value ?: return

        val updated = if (state.selectedClients.any { it.id == client.id }) {
            state.selectedClients.filterNot { it.id == client.id }
        } else {
            state.selectedClients + client
        }

        _clientSelectionDialogState.value = state.copy(selectedClients = updated)
    }

    fun confirmClientSelection() {
        val state = _clientSelectionDialogState.value ?: return

        _productUiState.value = updateProductUiState(
            _productUiState.value,
            ScreenElement.PRODUCT_OTHER_PRICE_CLIENTS, state.selectedClients, state.priceId
        )

        _clientSelectionDialogState.value = null
    }

    fun removeClientFromPrice(priceId: String, clientId: Int) {
        val updatedPrices = _productUiState.value.additionalPrices?.map { price ->
            if (price.idStr == priceId) {
                price.copy(clients = price.clients.filterNot { it.id == clientId })
            } else price
        }

        _productUiState.value = _productUiState.value.copy(additionalPrices = updatedPrices)
    }

}

private fun updateProductUiState(
    currentProductState: ProductState,
    element: ScreenElement,
    value: Any,
    priceId: String? = null,
): ProductState {
    var updatedProductState = currentProductState
    when (element) {
        ScreenElement.PRODUCT_NAME -> {
            updatedProductState = updatedProductState.copy(name = value as TextFieldValue)
        }

        ScreenElement.PRODUCT_DESCRIPTION -> {
            updatedProductState = updatedProductState.copy(description = value as TextFieldValue)
        }

        ScreenElement.PRODUCT_DEFAULT_PRICE_WITHOUT_TAX -> {
            val priceWithoutTax = value as String
            updatedProductState = if (priceWithoutTax.isNotEmpty()) {
                updatedProductState.copy(
                    defaultPriceWithoutTax = priceWithoutTax.replace(",", ".")
                        .toDoubleOrNull()?.let { BigDecimal.fromDouble(it) }
                        ?: BigDecimal.ZERO
                )
            } else updatedProductState.copy(defaultPriceWithoutTax = null)
        }

        ScreenElement.PRODUCT_DEFAULT_PRICE_WITH_TAX -> {
            val priceWithTax = value as String
            updatedProductState = if (priceWithTax.isNotEmpty()) {
                updatedProductState.copy(
                    defaultPriceWithTax = priceWithTax.replace(",", ".")
                        .toDoubleOrNull()?.let { BigDecimal.fromDouble(it) }
                        ?: BigDecimal.ZERO
                )
            } else updatedProductState.copy(defaultPriceWithTax = null)
        }

        ScreenElement.PRODUCT_OTHER_PRICE_WITHOUT_TAX -> {
            if (priceId != null) {
                val priceWithoutTaxStr = value as String

                val newPrices = updatedProductState.additionalPrices?.map { price ->
                    if (price.idStr == priceId) {
                        val ht = priceWithoutTaxStr
                            .replace(",", ".")
                            .toDoubleOrNull()?.let { BigDecimal.fromDouble(it) }

                        val ttc = ht?.let {
                            updatedProductState.taxRate?.let { tax ->
                                calculatePriceWithTax(it, tax)
                            }
                        }

                        price.copy(priceWithoutTax = ht, priceWithTax = ttc)
                    } else price
                }

                updatedProductState = updatedProductState.copy(additionalPrices = newPrices)
            }
        }

        ScreenElement.PRODUCT_OTHER_PRICE_WITH_TAX -> {
            if (priceId != null) {
                val priceWithTaxStr = value as String
                val newPrices = updatedProductState.additionalPrices?.map { price ->
                    if (price.idStr == priceId) {
                        price.copy(
                            priceWithTax = if (priceWithTaxStr.isNotEmpty()) {
                                priceWithTaxStr.replace(",", ".").toDoubleOrNull()?.let { BigDecimal.fromDouble(it) }
                                    ?: BigDecimal.ZERO
                            } else null
                        )
                    } else price
                }
                updatedProductState = updatedProductState.copy(additionalPrices = newPrices)
            }
        }

        // PRODUCT_UNIT and PRODUCT_UNIT_CODE are handled asynchronously via
        // onUnitFreeTextChange / onUnitCodePicked in the ViewModel, so this
        // synchronous path never sees them.

        ScreenElement.PRODUCT_TYPE -> {
            updatedProductState = updatedProductState.copy(type = value as ProductNature)
        }

        ScreenElement.PRODUCT_OTHER_PRICE_CLIENTS -> {
            val newPrices = updatedProductState.additionalPrices?.map { price ->
                if (price.idStr == priceId) {
                    price.copy(clients = value as List<ClientRef>)
                } else price
            }
            updatedProductState = updatedProductState.copy(additionalPrices = newPrices)
        }

        else -> {}
    }
    return updatedProductState
}

private fun updateDocumentProductUiState(
    documentProduct: DocumentProductState,
    element: ScreenElement,
    value: Any,
): DocumentProductState {
    var updated = documentProduct
    when (element) {
        ScreenElement.DOCUMENT_PRODUCT_NAME -> {
            updated = updated.copy(name = value as TextFieldValue)
        }

        ScreenElement.DOCUMENT_PRODUCT_QUANTITY -> {
            updated = updated.copy(
                quantity = (value as String).replace(",", ".").toDoubleOrNull()?.let { BigDecimal.fromDouble(it) } ?: BigDecimal.ZERO
            )
        }

        ScreenElement.DOCUMENT_PRODUCT_DESCRIPTION -> {
            updated = updated.copy(description = value as TextFieldValue)
        }

        ScreenElement.DOCUMENT_PRODUCT_PRICE_WITHOUT_TAX -> {
            val priceWithoutTax = value as String
            updated = if (priceWithoutTax.isNotEmpty()) {
                updated.copy(
                    priceWithoutTax = priceWithoutTax.replace(",", ".")
                        .toDoubleOrNull()?.let { BigDecimal.fromDouble(it) }
                        ?: BigDecimal.ZERO
                )
            } else updated.copy(priceWithoutTax = null)
        }

        ScreenElement.DOCUMENT_PRODUCT_PRICE_WITH_TAX -> {
            val priceWithTax = value as String
            updated = if (priceWithTax.isNotEmpty()) {
                updated.copy(
                    priceWithTax = priceWithTax.replace(",", ".")
                        .toDoubleOrNull()?.let { BigDecimal.fromDouble(it) }
                        ?: BigDecimal.ZERO
                )
            } else updated.copy(priceWithTax = null)
        }

        // DOCUMENT_PRODUCT_UNIT and DOCUMENT_PRODUCT_UNIT_CODE run through the
        // async path in the ViewModel — see onUnitFreeTextChange / onUnitCodePicked.

        ScreenElement.DOCUMENT_PRODUCT_TYPE -> {
            updated = updated.copy(type = value as ProductNature)
        }

        else -> {}
    }
    return updated
}

data class ClientSelectionDialogState(
    val priceId: String,
    val selectedClients: List<ClientRef>,
    val availableClients: List<ClientRef>,
)
