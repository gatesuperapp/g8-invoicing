package com.a4a.g8invoicing

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

actual fun getPlatformName(): String = "Android"

@Composable
actual fun getAppVersion(): String {
    val context = LocalContext.current
    return try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
    } catch (e: Exception) {
        "unknown"
    }
}
