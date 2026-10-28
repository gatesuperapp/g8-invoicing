package com.a4a.g8invoicing.ui.screens

import com.a4a.g8invoicing.ui.states.DashboardStatementsUiState

/**
 * Render the "Relevé client" tab to a PDF byte array. iText-backed on JVM
 * (Android + Desktop); iOS returns an empty array until a PDFKit port is
 * added. The caller writes the bytes out through [DashboardExportFileManager].
 */
expect fun writeStatementsPdfBytes(
    state: DashboardStatementsUiState,
    currencyCode: String,
    formatLocale: String?,
    exportDateLabel: String,
): ByteArray
