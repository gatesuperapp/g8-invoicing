package com.a4a.g8invoicing.ui.screens

import androidx.compose.runtime.Composable
import com.a4a.g8invoicing.ui.states.DocumentState

/**
 * Platform-specific PDF export composable.
 * - Android: Uses iText with Android Context, MediaStore, FileProvider
 * - Desktop: Uses iText with JVM file APIs
 */
@Composable
expect fun ExportPdfPlatform(
    document: DocumentState,
    onDismissRequest: () -> Unit,
)
