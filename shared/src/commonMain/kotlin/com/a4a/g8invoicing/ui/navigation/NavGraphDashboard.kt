package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.data.util.DispatcherProvider
import com.a4a.g8invoicing.ui.screens.DashboardExportFileManager
import com.a4a.g8invoicing.ui.screens.DashboardScreen
import com.a4a.g8invoicing.ui.screens.buildStatementsCsv
import com.a4a.g8invoicing.ui.screens.writeStatementsPdfBytes
import com.a4a.g8invoicing.ui.shared.PlatformBackHandler
import com.a4a.g8invoicing.ui.shared.currentTimeMillis
import com.a4a.g8invoicing.ui.viewmodels.DashboardViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.dashboard(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickBack: () -> Unit,
    showCategoryButton: Boolean = true,
) {
    composable(route = Screen.Dashboard.name) {
        val viewModel: DashboardViewModel = koinViewModel()
        val uiState by viewModel.uiState.collectAsState()
        val exportFileManager = koinInject<DashboardExportFileManager>()
        val scope = rememberCoroutineScope()
        // Visible-feedback flag for the CSV / PDF export buttons — flipped
        // on at the click so the loader shows immediately, flipped off
        // once the file has been written and the OS share sheet launched.
        var isExporting by remember { mutableStateOf(false) }

        // Kick the core fetch now that the destination is actually open —
        // nothing runs during VM init so the composable paints its chrome
        // (TopBar + TabRow + empty-state) before any heavy DB work starts.
        LaunchedEffect(Unit) { viewModel.onEnter() }

        var isCategoriesMenuOpen by remember { mutableStateOf(false) }
        var lastBackPressTime by remember { mutableStateOf(0L) }

        PlatformBackHandler {
            val currentTime = currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000) {
                // Double back - exit (handled by platform)
            } else {
                lastBackPressTime = currentTime
                isCategoriesMenuOpen = true
            }
        }

        DashboardScreen(
            navController = navController,
            uiState = uiState,
            onClickCategory = onClickCategory,
            onClickBack = onClickBack,
            onPickInvoicesPeriod = viewModel::setInvoicesPeriod,
            onPickStatementsPeriod = viewModel::setStatementsPeriod,
            onPickProductsPeriod = viewModel::setProductsPeriod,
            onEnterStatementsTab = viewModel::onEnterStatementsTab,
            isExporting = isExporting,
            onClickExportCsv = {
                if (!isExporting) {
                    isExporting = true
                    // Snapshot the UI state values before hopping off the
                    // main thread — Compose state reads outside a Composable
                    // scope aren't safe on a background dispatcher.
                    val stateSnap = uiState.statementsTab
                    val currency = uiState.currencyCode
                    val locale = uiState.formatLocale
                    scope.launch {
                        try {
                            withContext(DispatcherProvider.IO) {
                                val csv = buildStatementsCsv(stateSnap, currency, locale)
                                exportFileManager.exportCsv(buildFileName("csv"), csv)
                            }
                        } finally {
                            isExporting = false
                        }
                    }
                }
            },
            onClickExportPdf = {
                if (!isExporting) {
                    isExporting = true
                    val stateSnap = uiState.statementsTab
                    val currency = uiState.currencyCode
                    val locale = uiState.formatLocale
                    val dateLabel = "Exporté le " + isoDate(
                        kotlin.time.Clock.System.now()
                            .toLocalDateTime(TimeZone.currentSystemDefault()).date
                    )
                    scope.launch {
                        try {
                            withContext(DispatcherProvider.IO) {
                                val bytes = writeStatementsPdfBytes(
                                    state = stateSnap,
                                    currencyCode = currency,
                                    formatLocale = locale,
                                    exportDateLabel = dateLabel,
                                )
                                exportFileManager.exportPdf(buildFileName("pdf"), bytes)
                            }
                        } finally {
                            isExporting = false
                        }
                    }
                }
            },
            isCategoriesMenuOpen = isCategoriesMenuOpen,
            onCategoriesMenuOpenChange = { isCategoriesMenuOpen = it },
            showCategoryButton = showCategoryButton,
        )
    }
}

private fun buildFileName(extension: String): String {
    val today = kotlin.time.Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "releve-${isoDate(today)}.$extension"
}

private fun isoDate(d: LocalDate): String {
    val mm = if (d.monthNumber < 10) "0${d.monthNumber}" else d.monthNumber.toString()
    val dd = if (d.dayOfMonth < 10) "0${d.dayOfMonth}" else d.dayOfMonth.toString()
    return "${d.year}-$mm-$dd"
}
