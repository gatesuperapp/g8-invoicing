package com.a4a.g8invoicing.ui.shared

import androidx.compose.runtime.Composable

/**
 * Multiplatform back handler.
 * On Android: uses BackHandler from activity-compose
 * On iOS: no-op (iOS handles back differently via gestures)
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean = true, onBack: () -> Unit)

/**
 * Get current time in milliseconds (for double-back-to-exit logic)
 */
expect fun currentTimeMillis(): Long

/**
 * Exit the application.
 * On Android: finishes the activity
 * On iOS: no-op (iOS apps don't typically exit programmatically)
 */
expect fun exitApp()

/**
 * Show a short toast/notification message to the user.
 * On Android: uses Toast
 * On iOS: could use a snackbar or other notification
 */
@Composable
expect fun showToast(message: String)
