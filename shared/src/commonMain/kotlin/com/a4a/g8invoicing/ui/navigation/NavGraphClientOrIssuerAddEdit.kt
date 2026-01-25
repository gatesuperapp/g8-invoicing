package com.a4a.g8invoicing.ui.navigation

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
        val viewModel: ClientOrIssuerAddEditViewModel = koinViewModel(
            parameters = { parametersOf(itemId, type) }
        )
        val clientUiState by viewModel.clientUiState
        val isNew = itemId == null

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
