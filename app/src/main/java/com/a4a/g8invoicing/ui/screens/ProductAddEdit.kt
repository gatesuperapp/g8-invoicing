package com.a4a.g8invoicing.ui.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.viewmodels.ProductAddEditViewModel

@Composable
fun ProductAddEdit(
    navController: NavController,
    viewModel: ProductAddEditViewModel,
    product: ProductState,
    onValueChange: (ScreenElement, Any, String?) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    onClickDone: () -> Unit,
    onClickBack: () -> Unit,
    onClickForward: (ScreenElement) -> Unit,
    onClickOpenClientSelection: (String) -> Unit,
    onClickRemoveClient: (priceId: String, clientId: Int) -> Unit,
    onClickDeletePrice: (String) -> Unit,
    onClickAddPrice: () -> Unit,
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val clientSelectionDialogState by viewModel.clientSelectionDialogState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val localFocusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopBar(
                ctaText = Strings.get(R.string.document_modal_product_save),
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
            // Filtrer les clients déjà sélectionnés dans d'autres prix
            val alreadySelectedClientIds = product.additionalPrices
                ?.filter { it.idStr != dialogState.priceId } // Exclure le prix actuel
                ?.flatMap { it.clients.map { client -> client.id } }
                ?.toSet() ?: emptySet()

            val availableClients = dialogState.availableClients.filter { client ->
                client.id !in alreadySelectedClientIds
            }

            ClientMultiSelectSheet(
                allClients = availableClients,
                totalClientsInDb = dialogState.availableClients.size,
                selectedClients = dialogState.selectedClients,
                onToggleClient = { clientRef ->
                    viewModel.toggleClientSelection(clientRef)
                },
                onConfirm = {
                    viewModel.confirmClientSelection()
                },
                onDismiss = {
                    viewModel.confirmClientSelection()
                    viewModel.closeClientSelectionDialog()
                }
            )
        }
    }
}
