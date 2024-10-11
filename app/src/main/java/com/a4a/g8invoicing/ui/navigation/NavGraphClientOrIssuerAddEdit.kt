package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.a4a.g8invoicing.ui.screens.ClientAddEdit
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType

fun NavGraphBuilder.clientAddEdit(
    navController: NavController,
    goToPreviousScreen: (String, Pair<String, String>) -> Unit,
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
                if(viewModel.validateInputs(ClientOrIssuerType.CLIENT)) {
                    if (isNew) {
                        viewModel.createNew(ClientOrIssuerType.CLIENT)
                    } else {
                        viewModel.updateClientOrIssuerInLocalDb(ClientOrIssuerType.CLIENT)
                    }
                    goToPreviousScreen(
                        "result",
                        Pair("client", viewModel.getLastCreatedClientId().toString())
                    )
                }
            },
            onClickBack = {
                goToPreviousScreen(
                    "result",
                    Pair("", "")
                )
            }, // If the user went back, no need to pass value
            onClickDeleteAddress = {
                viewModel.removeAddressFromClientOrIssuerState(ClientOrIssuerType.CLIENT)
            }
        )
    }
}