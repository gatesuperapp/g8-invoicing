package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.a4a.g8invoicing.ui.screens.ClientOrIssuerAddEdit
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType
import com.a4a.g8invoicing.ui.viewmodels.ItemOrDocumentType

fun NavGraphBuilder.clientOrIssuerAddEdit(
    navController: NavController,
    goToPreviousScreen: (String, Pair<String, String>) -> Unit,
) {
    composable(
        route = Screen.ClientOrIssuerAddEdit.name + "?itemId={itemId}&type={type}",
        arguments = listOf(
            navArgument("itemId") { nullable = true }, // used in the ViewModel
            navArgument("type") { nullable = true },
        )
    ) { backStackEntry ->
        val viewModel: ClientOrIssuerAddEditViewModel = hiltViewModel()
        val clientUiState by viewModel.clientUiState
        val isNew = backStackEntry.arguments?.getString("itemId") == null

        ClientOrIssuerAddEdit(
            navController = navController,
            clientOrIssuer = clientUiState,
            isNew = backStackEntry.arguments?.getString("itemId") == null,
            onValueChange = { pageElement, value ->
                clientUiState.type?.let {
                    viewModel.updateClientOrIssuerState(pageElement, value, it)
                }
            },
            placeCursorAtTheEndOfText = { pageElement ->
                viewModel.updateCursor(pageElement, ItemOrDocumentType.CLIENT_OR_ISSUER)
            },
            onClickDone = {
                if(viewModel.validateInputs(ClientOrIssuerType.CLIENT)) {
                    if (isNew) {
                        viewModel.saveClientOrIssuerInLocalDb(ClientOrIssuerType.CLIENT)
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
        )
    }
}