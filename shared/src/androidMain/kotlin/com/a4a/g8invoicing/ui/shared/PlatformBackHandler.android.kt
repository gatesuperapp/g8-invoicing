package com.a4a.g8invoicing.ui.shared

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun exitApp() {
    // This is a no-op here - the actual exit logic should be handled
    // in the composable using LocalContext
}

@Composable
actual fun showToast(message: String) {
    val context = LocalContext.current
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
