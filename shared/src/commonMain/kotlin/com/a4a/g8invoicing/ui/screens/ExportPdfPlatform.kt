package com.a4a.g8invoicing.ui.screens

import androidx.compose.runtime.Composable
import com.a4a.g8invoicing.ui.states.DocumentState

/**
 * Platform-specific PDF export composable.
 * - Android: Uses iText with Android Context, MediaStore, FileProvider
 * - Desktop: Uses iText with JVM file APIs
 *
 * When [facturxXmlBytes] is provided, generates a Factur-X PDF instead
 * of a plain PDF: same visual output, with the CII XML embedded as an
 * associated file (`factur-x.xml`, AFRelationship=Data) so the file
 * doubles as a structured payload for e-invoicing platforms.
 */
@Composable
expect fun ExportPdfPlatform(
    document: DocumentState,
    onDismissRequest: () -> Unit,
    facturxXmlBytes: ByteArray? = null,
)
