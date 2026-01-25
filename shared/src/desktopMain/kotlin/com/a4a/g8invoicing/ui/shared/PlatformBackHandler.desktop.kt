package com.a4a.g8invoicing.ui.shared

import androidx.compose.runtime.Composable
import kotlin.system.exitProcess

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Desktop doesn't have a hardware back button
    // Back navigation is handled through UI buttons
}

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun exitApp() {
    exitProcess(0)
}

@Composable
actual fun showToast(message: String) {
    // For desktop, we could use a snackbar or notification
    // For now, just print to console
    println("Toast: $message")
}
