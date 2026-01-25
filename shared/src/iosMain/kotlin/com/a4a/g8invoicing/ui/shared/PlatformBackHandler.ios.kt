package com.a4a.g8invoicing.ui.shared

import androidx.compose.runtime.Composable
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS handles back navigation via swipe gestures, no explicit back handler needed
}

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

actual fun exitApp() {
    // iOS apps don't exit programmatically - this is a no-op
}

@Composable
actual fun showToast(message: String) {
    // TODO: Implement iOS toast/snackbar notification
    // For now this is a no-op - could use a Compose Snackbar or native UIKit alert
    println("Toast: $message")
}
