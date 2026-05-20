package com.a4a.g8invoicing.ui.navigation

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.ui.screens.ClientAddEdit
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerAddEditViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.clientAddEdit(
    navController: NavController,
    goToPreviousScreen: () -> Unit,
) {
    composable(
        route = Screen.ClientAddEdit.name + "?itemId={itemId}&type={type}",
        arguments = listOf(
            navArgument("itemId") { nullable = true },
            navArgument("type") { nullable = true },
        )
    ) { backStackEntry ->
        val itemId = backStackEntry.arguments?.getString("itemId")
        val type = backStackEntry.arguments?.getString("type")
        // Same route is reused for both clients ("type=client" or absent) and
        // issuers ("type=issuer"). The full-screen issuer form is reached from
        // Mon Compte → Mes entreprises, with the "pied de page par défaut"
        // field visible because we're NOT in bottom-sheet mode here.
        val isIssuer = type == ClientOrIssuerType.ISSUER.name.lowercase()
        val effectiveType = if (isIssuer) ClientOrIssuerType.ISSUER else ClientOrIssuerType.CLIENT

        val viewModel: ClientOrIssuerAddEditViewModel = koinViewModel(
            parameters = { parametersOf(itemId, type) }
        )
        val clientUiState by viewModel.clientUiState
        val issuerUiState by viewModel.issuerUiState
        val currentState = if (isIssuer) issuerUiState else clientUiState
        val isNew = itemId == null

        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        val scope = rememberCoroutineScope()
        val scrollState = rememberScrollState()

        ClientAddEdit(
            navController = navController,
            clientOrIssuer = currentState,
            onValueChange = { pageElement, value ->
                currentState.type?.let {
                    viewModel.updateClientOrIssuerState(pageElement, value, it)
                }
            },
            placeCursorAtTheEndOfText = { pageElement ->
                viewModel.updateCursor(pageElement, effectiveType)
            },
            onClickDone = {
                scope.launch {
                    // Clear focus first to trigger onFocusChanged in email field
                    // This will add any pending email before validation
                    focusManager.clearFocus()
                    keyboardController?.hide()

                    if (viewModel.validateInputs(effectiveType)) {
                        val success = if (isNew) {
                            viewModel.createNew(effectiveType) != null
                        } else {
                            viewModel.updateClientOrIssuerInLocalDb(effectiveType)
                        }

                        if (success) {
                            goToPreviousScreen()
                        }
                    } else {
                        scrollState.animateScrollTo(0)
                    }
                }
            },
            onClickBack = {
                goToPreviousScreen()
            },
            onClickDeleteAddress = {
                viewModel.removeAddressFromClientOrIssuerState(effectiveType)
            },
            onClickDeleteEmail = { index ->
                viewModel.removeEmailFromClientOrIssuerState(effectiveType, index)
            },
            onAddEmail = { email ->
                viewModel.addEmailToClientOrIssuerState(effectiveType, email)
            },
            scrollState = scrollState,
            onPendingEmailValidationResult = { isValid ->
                viewModel.setPendingEmailValidationResult(isValid)
            }
        )
    }
}
