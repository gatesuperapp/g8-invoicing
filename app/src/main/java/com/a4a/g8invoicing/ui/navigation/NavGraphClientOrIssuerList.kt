package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.data.ClientOrIssuerEditable
import com.a4a.g8invoicing.ui.screens.ClientOrIssuerList
import com.a4a.g8invoicing.ui.screens.ClientOrIssuerListViewModel
import com.a4a.g8invoicing.ui.screens.PersonType

fun NavGraphBuilder.clientOrIssuerList(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (ClientOrIssuerEditable) -> Unit,
    onClickNew: () -> Unit,
    onClickBack: () -> Unit,
) {
    composable(route = Screen.ClientOrIssuerList.name) {
        val viewModel: ClientOrIssuerListViewModel = hiltViewModel()
        val clientsUiState by viewModel.clientsUiState
            .collectAsStateWithLifecycle()

        ClientOrIssuerList(
            navController = navController,
            uiState = clientsUiState,
            onClickDelete = viewModel::deleteClientsOrIssuers,
            onClickDuplicate = viewModel::duplicateClientsOrIssuers,
            onClickNew = { onClickNew() },
            onClickCategory = onClickCategory,
            onClickListItem = onClickListItem,
            onClickBack = { onClickBack() }
        )
    }
}