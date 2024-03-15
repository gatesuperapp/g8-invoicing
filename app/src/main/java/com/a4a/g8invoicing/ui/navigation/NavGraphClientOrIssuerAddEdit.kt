package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.a4a.g8invoicing.ui.screens.ClientOrIssuerAddEdit
import com.a4a.g8invoicing.ui.screens.ClientOrIssuerAddEditViewModel

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
                viewModel.updateClientOrIssuerState(pageElement, value)
            },
            placeCursorAtTheEndOfText = { pageElement ->
                viewModel.updateCursorOfClientOrIssuerState(pageElement)
            },
            onClickDone = {
                //  we don't have to pass the object as the
                //  ViewModel is already updated with latest values
                if (isNew) {
                    viewModel.saveInLocalDb("client")
                } else {
                    viewModel.updateClientInInLocalDb()
                }
                goToPreviousScreen(
                    "result",
                    Pair("client", viewModel.getLastCreated().toString())
                ) // TODO ?
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