package com.a4a.g8invoicing.ui.screens

import com.a4a.g8invoicing.ui.states.DashboardStatementsUiState

/**
 * iOS stub — the Dashboard isn't surfaced on the iOS entry point and
 * iText isn't available in iosMain. PDFKit port lands when Dashboard
 * goes on iOS.
 */
actual fun writeStatementsPdfBytes(
    state: DashboardStatementsUiState,
    currencyCode: String,
    formatLocale: String?,
    exportDateLabel: String,
): ByteArray = ByteArray(0)
