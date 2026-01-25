package com.a4a.g8invoicing.ui.screens

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.navigation.NavController
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.document_modal_product_save
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ClientRef
import com.a4a.g8invoicing.ui.states.ProductState
import org.jetbrains.compose.resources.stringResource

// Data class for client selection dialog state
data class ClientSelectionDialogState(
    val priceId: String,
    val availableClients: List<ClientRef>,
    val selectedClients: List<ClientRef>
)

@Composable
fun ProductAddEdit(
    navController: NavController,
    product: ProductState,
    isLoading: Boolean,
    clientSelectionDialogState: ClientSelectionDialogState?,
    onValueChange: (ScreenElement, Any, String?) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    onClickDone: () -> Unit,
    onClickBack: () -> Unit,
    onClickForward: (ScreenElement) -> Unit,
    onClickOpenClientSelection: (String) -> Unit,
    onClickRemoveClient: (priceId: String, clientId: Int) -> Unit,
    onClickDeletePrice: (String) -> Unit,
    onClickAddPrice: () -> Unit,
    onToggleClientSelection: (ClientRef) -> Unit,
    onConfirmClientSelection: () -> Unit,
    onCloseClientSelectionDialog: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val localFocusManager = LocalFocusManager.current

    val ctaText = stringResource(Res.string.document_modal_product_save)

    Scaffold(
        topBar = {
            TopBar(
                ctaText = ctaText,
                ctaTextDisabled = product.name.text.isNotEmpty(),
                navController = navController,
                onClickBackArrow = {
                    keyboardController?.hide()
                    onClickBack()
                },
                isCancelCtaDisplayed = true,
                onClickCtaValidate = {
                    keyboardController?.hide()
                    onClickDone()
                }
            )
        }
    ) { _ ->

        ProductAddEditForm(
            product = product,
            onValueChange = onValueChange,
            placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
            onClickForward = onClickForward,
            onClickDeletePrice = onClickDeletePrice,
            onClickAddPrice = onClickAddPrice,
            onClickSelectClients = { priceId ->
                // Fermer le clavier avant d'ouvrir la bottom sheet
                keyboardController?.hide()
                localFocusManager.clearFocus()
                onClickOpenClientSelection(priceId)
            },
            onRemoveClient = onClickRemoveClient,
            isLoading = isLoading
        )

        clientSelectionDialogState?.let { dialogState ->
            // Utiliser la fonction utilitaire pour filtrer les clients
            val selectionState = filterClientsForAdditionalPrice(
                allClientsInDatabase = dialogState.availableClients,
                additionalPrices = product.additionalPrices,
                currentPriceId = dialogState.priceId
            )

            ClientMultiSelectSheet(
                allClients = selectionState.availableClients,
                totalClientsInDatabase = selectionState.totalClientsInDatabase,
                selectedClients = dialogState.selectedClients,
                onToggleClient = { clientRef ->
                    onToggleClientSelection(clientRef)
                },
                onConfirm = {
                    onConfirmClientSelection()
                },
                onDismiss = {
                    onConfirmClientSelection()
                    onCloseClientSelectionDialog()
                }
            )
        }
    }
}
