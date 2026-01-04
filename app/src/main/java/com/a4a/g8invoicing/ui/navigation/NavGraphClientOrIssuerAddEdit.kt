package com.a4a.g8invoicing.ui.navigation

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.a4a.g8invoicing.ui.screens.ClientAddEdit
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType
import kotlinx.coroutines.launch

fun NavGraphBuilder.clientAddEdit(
    navController: NavController,
    goToPreviousScreen: () -> Unit,
) {
    composable(
        route = Screen.ClientAddEdit.name + "?itemId={itemId}&type={type}",
        arguments = listOf(
            navArgument("itemId") { nullable = true },
            navArgument("type") { nullable = true },
        ) // used in the ViewModel
    ) { backStackEntry ->
        val viewModel: ClientOrIssuerAddEditViewModel = hiltViewModel()
        val clientUiState by viewModel.clientUiState
        val isNew = backStackEntry.arguments?.getString("itemId") == null

        // Get the keyboard controller
        val keyboardController = LocalSoftwareKeyboardController.current
        val scope = rememberCoroutineScope()
        val scrollState = rememberScrollState()


        ClientAddEdit(
            navController = navController,
            clientOrIssuer = clientUiState,
            onValueChange = { pageElement, value ->
                clientUiState.type?.let {
                    viewModel.updateClientOrIssuerState(pageElement, value, it)
                }
            },
            placeCursorAtTheEndOfText = { pageElement ->
                viewModel.updateCursor(pageElement, ClientOrIssuerType.CLIENT)
            },
            onClickDone = {
                scope.launch {
                    keyboardController?.hide()

                    if (viewModel.validateInputs(ClientOrIssuerType.CLIENT)) {
                        val success = if (isNew) {
                            viewModel.createNew(ClientOrIssuerType.CLIENT)
                        } else {
                            viewModel.updateClientOrIssuerInLocalDb(ClientOrIssuerType.CLIENT)
                        }

                        if (success) {
                            goToPreviousScreen()
                        }
                    } else {
                        // Scroll to top to show validation error
                        scrollState.animateScrollTo(0)
                    }
                }
            },
            onClickBack = {
                goToPreviousScreen()
            },
            onClickDeleteAddress = {
                viewModel.removeAddressFromClientOrIssuerState(ClientOrIssuerType.CLIENT)
            },
            scrollState = scrollState
        )
    }
}