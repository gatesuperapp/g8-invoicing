package com.a4a.g8invoicing

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.a4a.g8invoicing.di.desktopModule
import com.a4a.g8invoicing.ui.theme.G8InvoicingTheme
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

fun main() {
    // Initialize Koin BEFORE application starts (only once)
    if (GlobalContext.getOrNull() == null) {
        startKoin {
            modules(desktopModule)
        }
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "G8 Invoicing",
            state = rememberWindowState(width = 1200.dp, height = 800.dp)
        ) {
            G8InvoicingTheme {
                App()
            }
        }
    }
}
