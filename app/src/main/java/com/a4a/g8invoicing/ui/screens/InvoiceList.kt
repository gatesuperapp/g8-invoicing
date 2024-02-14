package com.a4a.g8invoicing.ui.screens

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun InvoiceList(navController: NavController) {
    Scaffold(
        bottomBar = {
            InvoiceListBottomBar(
                navController
            )

        }
    ) {
        it
        Text("Factures")
    }
}

@Composable
private fun InvoiceListBottomBar(
    navController: NavController
) {

}
