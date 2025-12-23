package com.a4a.g8invoicing.ui.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    Scaffold(
        bottomBar = {
            HomeBottomBar(
                navController
            )

        }
    ) {
        it
        Text("Home")
    }
}

@Composable
private fun HomeBottomBar(
    navController: NavController
) {

}
