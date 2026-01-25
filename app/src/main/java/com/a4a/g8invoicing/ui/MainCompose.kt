package com.a4a.g8invoicing.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.a4a.g8invoicing.ui.navigation.NavGraph
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.theme.G8InvoicingTheme

@Composable
fun MainCompose(
    navController: NavHostController = rememberNavController(),
    onSendReminder: (InvoiceState) -> Unit = {},
) {
    G8InvoicingTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            NavGraph(
                navController = navController,
                onSendReminder = onSendReminder
            )
        }
    }
}
