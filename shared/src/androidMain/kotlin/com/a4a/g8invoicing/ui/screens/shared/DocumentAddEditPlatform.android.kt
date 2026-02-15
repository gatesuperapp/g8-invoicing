package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.ProductState
import com.ionspin.kotlin.bignum.decimal.BigDecimal

@Composable
actual fun DocumentAddEditPlatform(
    navController: NavController,
    document: DocumentState,
    onClickBack: () -> Unit,
    clientList: MutableList<ClientOrIssuerState>,
    issuerList: MutableList<ClientOrIssuerState>,
    documentClientUiState: ClientOrIssuerState,
    documentIssuerUiState: ClientOrIssuerState,
    documentProductUiState: DocumentProductState,
    taxRates: List<BigDecimal>,
    products: MutableList<ProductState>,
    onValueChange: (ScreenElement, Any) -> Unit,
    onSelectProduct: (ProductState, Int?) -> Unit,
    onClickNewDocumentProduct: () -> Unit,
    onClickEditDocumentProduct: (DocumentProductState) -> Unit,
    onClickDeleteDocumentProduct: (Int) -> Unit,
    onSelectClientOrIssuer: (ClientOrIssuerState) -> Unit,
    onClickNewDocumentClientOrIssuer: (ClientOrIssuerType) -> Unit,
    onClickDocumentClientOrIssuer: (ClientOrIssuerState) -> Unit,
    onClickDeleteDocumentClientOrIssuer: (ClientOrIssuerType) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    bottomFormOnValueChange: (ScreenElement, Any, ClientOrIssuerType?) -> Unit,
    bottomFormPlaceCursor: (ScreenElement, ClientOrIssuerType?) -> Unit,
    onClickDoneForm: (DocumentBottomSheetTypeOfForm) -> Unit,
    onClickCancelForm: () -> Unit,
    onSelectTaxRate: (BigDecimal?) -> Unit,
    showDocumentForm: Boolean,
    onShowDocumentForm: (Boolean) -> Unit,
    onClickDeleteAddress: (ClientOrIssuerType) -> Unit,
    onClickDeleteEmail: (ClientOrIssuerType, Int) -> Unit,
    onAddEmail: (ClientOrIssuerType, String) -> Unit,
    onPendingEmailValidationResult: (ClientOrIssuerType, Boolean) -> Unit,
    onOrderChange: (List<DocumentProductState>) -> Unit,
    onShowMessage: (String) -> Unit,
    exportPdfContent: @Composable (DocumentState, () -> Unit) -> Unit,
) {
    // Android uses the mobile DocumentAddEdit with bottom sheets
    DocumentAddEdit(
        navController = navController,
        document = document,
        onClickBack = onClickBack,
        clientList = clientList,
        issuerList = issuerList,
        documentClientUiState = documentClientUiState,
        documentIssuerUiState = documentIssuerUiState,
        documentProductUiState = documentProductUiState,
        taxRates = taxRates,
        products = products,
        onValueChange = onValueChange,
        onSelectProduct = onSelectProduct,
        onClickNewDocumentProduct = onClickNewDocumentProduct,
        onClickEditDocumentProduct = onClickEditDocumentProduct,
        onClickDeleteDocumentProduct = onClickDeleteDocumentProduct,
        onSelectClientOrIssuer = onSelectClientOrIssuer,
        onClickNewDocumentClientOrIssuer = onClickNewDocumentClientOrIssuer,
        onClickDocumentClientOrIssuer = onClickDocumentClientOrIssuer,
        onClickDeleteDocumentClientOrIssuer = onClickDeleteDocumentClientOrIssuer,
        placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
        bottomFormOnValueChange = bottomFormOnValueChange,
        bottomFormPlaceCursor = bottomFormPlaceCursor,
        onClickDoneForm = onClickDoneForm,
        onClickCancelForm = onClickCancelForm,
        onSelectTaxRate = onSelectTaxRate,
        showDocumentForm = showDocumentForm,
        onShowDocumentForm = onShowDocumentForm,
        onClickDeleteAddress = onClickDeleteAddress,
        onClickDeleteEmail = onClickDeleteEmail,
        onAddEmail = onAddEmail,
        onPendingEmailValidationResult = onPendingEmailValidationResult,
        onOrderChange = onOrderChange,
        onShowMessage = onShowMessage,
        exportPdfContent = exportPdfContent,
    )
}
