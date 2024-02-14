package com.a4a.g8invoicing.ui.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.ui.screens.ClientOrIssuerListViewModel
import com.a4a.g8invoicing.ui.screens.InvoiceList

fun NavGraphBuilder.invoiceList(navController: NavController) {
    composable(route = Screen.InvoiceList.name) {
        val viewModel: ClientOrIssuerListViewModel = hiltViewModel()
        InvoiceList(navController)
    }
}