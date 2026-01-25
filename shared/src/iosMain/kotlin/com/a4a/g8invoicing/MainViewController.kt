package com.a4a.g8invoicing

import androidx.compose.ui.window.ComposeUIViewController
import com.a4a.g8invoicing.ui.theme.G8InvoicingTheme

fun MainViewController() = ComposeUIViewController {
    G8InvoicingTheme {
        App()
    }
}
