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

/**
 * Platform-specific document add/edit screen.
 * - On Android: uses DocumentAddEdit (mobile UI with bottom sheets)
 * - On Desktop: uses DocumentAddEditDesktop (side panel UI)
 */
@Composable
expect fun DocumentAddEditPlatform(
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
)
