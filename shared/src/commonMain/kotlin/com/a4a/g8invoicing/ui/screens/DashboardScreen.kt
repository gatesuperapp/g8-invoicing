package com.a4a.g8invoicing.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import com.a4a.g8invoicing.ui.shared.drawScrollThumb
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.data.formatAmount
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.appbar_dashboard
import com.a4a.g8invoicing.shared.resources.appbar_credit_notes
import com.a4a.g8invoicing.shared.resources.appbar_delivery_notes
import com.a4a.g8invoicing.shared.resources.appbar_quotes
import com.a4a.g8invoicing.shared.resources.appbar_sales_orders
import com.a4a.g8invoicing.shared.resources.dashboard_products_chart_title
import com.a4a.g8invoicing.shared.resources.dashboard_products_empty
import com.a4a.g8invoicing.shared.resources.dashboard_products_see_less
import com.a4a.g8invoicing.shared.resources.dashboard_products_see_more
import com.a4a.g8invoicing.shared.resources.dashboard_products_top_section
import com.a4a.g8invoicing.shared.resources.dashboard_sales_chart_title
import com.a4a.g8invoicing.shared.resources.dashboard_statements_empty
import com.a4a.g8invoicing.shared.resources.dashboard_statements_empty_period
import com.a4a.g8invoicing.shared.resources.dashboard_statements_export_csv
import com.a4a.g8invoicing.shared.resources.dashboard_statements_export_pdf
import com.a4a.g8invoicing.shared.resources.dashboard_section_months
import com.a4a.g8invoicing.shared.resources.dashboard_summary_late
import com.a4a.g8invoicing.shared.resources.dashboard_summary_paid
import com.a4a.g8invoicing.shared.resources.dashboard_summary_to_pay
import com.a4a.g8invoicing.shared.resources.dashboard_summary_total
import com.a4a.g8invoicing.shared.resources.dashboard_tab_invoices
import com.a4a.g8invoicing.shared.resources.dashboard_tab_products
import com.a4a.g8invoicing.shared.resources.dashboard_tab_statements
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.screens.shared.ScaffoldWithDimmedOverlay
import com.a4a.g8invoicing.ui.shared.GeneralBottomBar
import com.a4a.g8invoicing.ui.states.DashboardClientStatement
import com.a4a.g8invoicing.ui.states.DashboardInvoicesUiState
import com.a4a.g8invoicing.ui.states.DashboardMonthRow
import com.a4a.g8invoicing.ui.states.DashboardPeriod
import com.a4a.g8invoicing.ui.states.DashboardProductMonthlyAmount
import com.a4a.g8invoicing.ui.states.DashboardProductsUiState
import com.a4a.g8invoicing.ui.states.DashboardStatementRow
import com.a4a.g8invoicing.ui.states.DashboardStatementsPeriod
import com.a4a.g8invoicing.ui.states.DashboardStatementsUiState
import com.a4a.g8invoicing.ui.states.DashboardUiState
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.ColorCancelled
import com.a4a.g8invoicing.ui.theme.ColorGreenPaid
import com.a4a.g8invoicing.ui.theme.ColorGreyDraft
import com.a4a.g8invoicing.ui.theme.ColorRedLate
import com.a4a.g8invoicing.ui.theme.ColorReminded
import com.a4a.g8invoicing.ui.theme.ColorSent
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.Canvas
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.StrokeCap

@Composable
fun DashboardScreen(
    navController: NavController,
    uiState: DashboardUiState,
    onClickCategory: (Category) -> Unit,
    onClickBack: () -> Unit,
    onClickExportCsv: () -> Unit = {},
    onClickExportPdf: () -> Unit = {},
    onPickInvoicesPeriod: (DashboardPeriod) -> Unit = {},
    onPickStatementsPeriod: (DashboardStatementsPeriod) -> Unit = {},
    onPickProductsPeriod: (DashboardStatementsPeriod) -> Unit = {},
    onEnterStatementsTab: () -> Unit = {},
    isExporting: Boolean = false,
    isCategoriesMenuOpen: Boolean = false,
    onCategoriesMenuOpenChange: (Boolean) -> Unit = {},
    showCategoryButton: Boolean = true,
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // Lazy-fire the Relevé fetch only when the user actually lands on that
    // tab — avoids loading BL/Devis/BC/Avoirs on Dashboard entry.
    LaunchedEffect(selectedTab) {
        if (selectedTab == 2) onEnterStatementsTab()
    }

    ScaffoldWithDimmedOverlay(
        isDimmed = false,
        onDismissDim = {},
        topBar = {
            TopBar(
                title = stringResource(Res.string.appbar_dashboard),
                navController = navController,
                onClickBackArrow = onClickBack,
                isCancelCtaDisplayed = false,
            )
        },
        bottomBar = {
            GeneralBottomBar(
                navController = navController,
                numberOfItemsSelected = 0,
                isButtonNewDisplayed = false,
                onClickCategory = onClickCategory,
                onChangeBackground = {},
                isCategoriesMenuOpen = isCategoriesMenuOpen,
                onCategoriesMenuOpenChange = onCategoriesMenuOpenChange,
                showCategoryButton = showCategoryButton,
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = AppColors.surface,
                contentColor = AppColors.textPrimary,
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(Res.string.dashboard_tab_invoices)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(Res.string.dashboard_tab_products)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(stringResource(Res.string.dashboard_tab_statements)) }
                )
            }

            // Render the picked tab unconditionally — its default state is
            // all-zero amounts + empty lists, which is harmless to paint
            // while the VM's first flow emission is still in flight. The
            // user sees the Dashboard structure (TabRow, SummaryCard with
            // 0 €, empty-state messages) immediately on tab-switch, and
            // the overlay spinner below signals that data is still being
            // crunched.
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> DashboardInvoicesTab(
                        data = uiState.invoicesTab,
                        currencyCode = uiState.currencyCode,
                        formatLocale = uiState.formatLocale,
                        onPickInvoicesPeriod = onPickInvoicesPeriod,
                    )
                    1 -> DashboardProductsTab(
                        data = uiState.productsTab,
                        currencyCode = uiState.currencyCode,
                        formatLocale = uiState.formatLocale,
                        onPickProductsPeriod = onPickProductsPeriod,
                    )
                    2 -> DashboardStatementsTab(
                        data = uiState.statementsTab,
                        currencyCode = uiState.currencyCode,
                        formatLocale = uiState.formatLocale,
                        onClickExportCsv = onClickExportCsv,
                        onClickExportPdf = onClickExportPdf,
                        onPickStatementsPeriod = onPickStatementsPeriod,
                        isBusy = uiState.isStatementsLoading || isExporting,
                    )
                }
                // Per-tab loader: Factures + Produits share the core fetch;
                // Relevé has its own job that only runs after the first tap
                // on the tab. Mirrors the slim horizontal loader on the
                // document export modal — 64dp wide, text-colour track —
                // instead of the heavier circular spinner.
                // Overlay loader only for the core fetch (Factures / Produits).
                // The Relevé-tab loader lives inline inside DashboardStatementsTab
                // so it anchors right under the export buttons where the user
                // tapped — see the `isBusy` param plumbed above.
                val showCoreSpinner = uiState.isLoading && (selectedTab == 0 || selectedTab == 1)
                if (showCoreSpinner) {
                    // Centered on the content area — lands in the still-empty
                    // space below the summary card / picker / export row and
                    // never overlaps a filled block.
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier.width(64.dp),
                            color = AppColors.textPrimary,
                            trackColor = AppColors.textPrimary.copy(alpha = 0.24f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardInvoicesTab(
    data: DashboardInvoicesUiState,
    currencyCode: String,
    formatLocale: String?,
    onPickInvoicesPeriod: (DashboardPeriod) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
    ) {
        // --- Summary card -------------------------------------------------
        SummaryCard(data, currencyCode, formatLocale)

        // Dropdown lives outside the "has data" guard so the user can
        // always switch between windows (rolling 12, 2025, 2024 …) even
        // when the currently selected one happens to be empty.
        if (data.totalSalesCount > 0 || data.availableYears.isNotEmpty()) {
            Spacer(Modifier.height(28.dp))
            PeriodPickerHeader(
                selectedPeriod = data.selectedPeriod,
                availableYears = data.availableYears,
                onPickPeriod = onPickInvoicesPeriod,
            )
        }

        // Fold the monthly breakdown + chart whenever the chosen window has
        // ≥1 emitted invoice (SENT / REMINDED / LATE / PAID). Using
        // totalSalesCount here instead of paidCount means picking a year
        // that only contains open invoices still shows the breakdown; the
        // old guard made the whole block vanish for those years, which
        // read as a bug.
        if (data.totalSalesCount > 0) {
            data.monthlyRows.forEach { row ->
                MonthlyRow(row, currencyCode, formatLocale)
            }

            Spacer(Modifier.height(28.dp))

            SectionTitle(stringResource(Res.string.dashboard_sales_chart_title))
            ChartCard(data.salesSeries, currencyCode, formatLocale)
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun SummaryCard(
    data: DashboardInvoicesUiState,
    currencyCode: String,
    formatLocale: String?,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AppColors.surfaceSubtle,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            SummaryRow(
                label = stringResource(Res.string.dashboard_summary_total),
                amount = formatAmount(data.totalSalesAmount, currencyCode, formatLocale),
                count = data.totalSalesCount,
                dotColor = null,
                isBold = true,
            )
            SummaryDivider()
            SummaryRow(
                label = stringResource(Res.string.dashboard_summary_paid),
                amount = formatAmount(data.paidAmount, currencyCode, formatLocale),
                count = data.paidCount,
                dotColor = ColorGreenPaid,
            )
            SummaryDivider()
            SummaryRow(
                label = stringResource(Res.string.dashboard_summary_to_pay),
                amount = formatAmount(data.toPayAmount, currencyCode, formatLocale),
                count = data.toPayCount,
                dotColor = ColorSent,
            )
            SummaryDivider()
            SummaryRow(
                label = stringResource(Res.string.dashboard_summary_late),
                amount = formatAmount(data.lateAmount, currencyCode, formatLocale),
                count = data.lateCount,
                dotColor = ColorRedLate,
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    amount: String,
    count: Int,
    dotColor: Color?,
    isBold: Boolean = false,
    withPadding: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (withPadding) Modifier.padding(vertical = 10.dp) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (dotColor != null) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, RoundedCornerShape(50)),
            )
            Spacer(Modifier.size(10.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
            )
            Text(
                text = "$count ${pluraliseInvoice(count)}",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.textSecondary,
            )
        }
        Text(
            text = amount,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SummaryDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AppColors.textSecondary.copy(alpha = 0.1f))
    )
}

@Composable
private fun MonthlyRow(
    row: DashboardMonthRow,
    currencyCode: String,
    formatLocale: String?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.monthLabel,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            val sub = buildString {
                append(row.paidCount)
                append(' ')
                append(pluralisePaid(row.paidCount))
                if (row.pendingCount > 0) {
                    append(", ")
                    append(row.pendingCount)
                    append(" en attente")
                }
            }
            Text(
                text = sub,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.textSecondary,
            )
        }
        Text(
            text = formatAmount(row.totalAmount, currencyCode, formatLocale),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
    }
}

private fun pluraliseInvoice(count: Int): String = if (count > 1) "factures" else "facture"
private fun pluralisePaid(count: Int): String = if (count > 1) "payées" else "payée"

// ----------------------------------------------------------------------
// Produits tab
// ----------------------------------------------------------------------

@Composable
private fun DashboardProductsTab(
    data: DashboardProductsUiState,
    currencyCode: String,
    formatLocale: String?,
    onPickProductsPeriod: (DashboardStatementsPeriod) -> Unit = {},
) {
    if (data.isEmpty) {
        Box(
            modifier = Modifier.fillMaxSize().padding(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(Res.string.dashboard_products_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.textSecondary,
            )
        }
        return
    }

    // Chart product selector lives at the screen level so the user's pick
    // survives recompositions and dropdown toggles below.
    var selectedProduct by remember(data.defaultProductName) {
        mutableStateOf(data.defaultProductName.orEmpty())
    }
    // Expand/collapse state for the top-N products list — defaults to top 3
    // visible, user can expand to see the full tail.
    var topExpanded by remember(data.selectedPeriod) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
    ) {
        // --- Top products for the picked period ---------------------------
        SectionTitle(stringResource(Res.string.dashboard_products_top_section))

        MonthOrYearPicker(
            selectedPeriod = data.selectedPeriod,
            availablePeriods = data.availablePeriods,
            onPick = onPickProductsPeriod,
        )

        Spacer(Modifier.height(12.dp))

        if (data.topProducts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.dashboard_statements_empty_period),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.textSecondary,
                )
            }
        } else {
            val visible = if (topExpanded) data.topProducts else data.topProducts.take(3)
            visible.forEach { line ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = line.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = formatAmount(line.amount, currencyCode, formatLocale),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            if (data.topProducts.size > 3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { topExpanded = !topExpanded }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    val rotation by animateFloatAsState(if (topExpanded) 180f else 0f)
                    Text(
                        text = if (topExpanded) stringResource(Res.string.dashboard_products_see_less)
                        else stringResource(Res.string.dashboard_products_see_more),
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.textSecondary,
                    )
                    Spacer(Modifier.size(4.dp))
                    Icon(
                        Icons.Filled.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp).rotate(rotation),
                        tint = AppColors.textSecondary,
                    )
                }
            }
        }

        Spacer(Modifier.height(36.dp))

        // --- Chart --------------------------------------------------------
        SectionTitle(stringResource(Res.string.dashboard_products_chart_title))
        ProductPickerDropdown(
            names = data.allProductNames,
            selected = selectedProduct,
            onSelect = { selectedProduct = it },
        )
        Spacer(Modifier.height(12.dp))
        val serie = data.seriesByProduct[selectedProduct].orEmpty()
        ChartCard(serie, currencyCode, formatLocale)

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun PeriodPickerHeader(
    selectedPeriod: DashboardPeriod,
    availableYears: List<Int>,
    onPickPeriod: (DashboardPeriod) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val currentLabel = when (selectedPeriod) {
        DashboardPeriod.Rolling12 -> stringResource(Res.string.dashboard_section_months)
        is DashboardPeriod.Year -> selectedPeriod.year.toString()
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 14.dp),
    ) {
        Row(
            modifier = Modifier.clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = currentLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary,
            )
            Spacer(Modifier.size(4.dp))
            Icon(
                Icons.Filled.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = AppColors.textPrimary,
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.dashboard_section_months)) },
                onClick = {
                    expanded = false
                    onPickPeriod(DashboardPeriod.Rolling12)
                },
            )
            availableYears.forEach { year ->
                DropdownMenuItem(
                    text = { Text(year.toString()) },
                    onClick = {
                        expanded = false
                        onPickPeriod(DashboardPeriod.Year(year))
                    },
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = AppColors.textPrimary,
        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
    )
}

@Composable
private fun ChartCard(
    serie: List<DashboardProductMonthlyAmount>,
    currencyCode: String,
    formatLocale: String?,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AppColors.surfaceSubtle,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            LineChart(
                serie = serie,
                currencyCode = currencyCode,
                formatLocale = formatLocale,
                modifier = Modifier.fillMaxWidth().aspectRatio(1.6f),
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Label indices 1, 3, 5, 7, 9, 11 — ensures the newest month
                // (position 11 on a 12-point series) always gets a label.
                // Painting every month would overlap on narrow phones.
                serie.forEachIndexed { index, point ->
                    if (index % 2 == 1) {
                        Text(
                            text = point.monthLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.textSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductPickerDropdown(
    names: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            shape = RoundedCornerShape(12.dp),
            color = AppColors.surfaceSubtle,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selected.ifEmpty { "—" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Filled.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = AppColors.textSecondary,
                )
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            names.forEach { name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelect(name)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun LineChart(
    serie: List<DashboardProductMonthlyAmount>,
    currencyCode: String,
    formatLocale: String?,
    modifier: Modifier = Modifier,
) {
    if (serie.isEmpty()) {
        Box(modifier.height(120.dp)) {}
        return
    }
    val maxAmount = serie.maxOf { it.amount }
    val maxDouble = maxAmount.doubleValue(false).coerceAtLeast(1.0)
    val lineColor = AppColors.textPrimary
    val gridColor = AppColors.textSecondary.copy(alpha = 0.15f)
    val fillColor = lineColor.copy(alpha = 0.1f)
    val labelColor = AppColors.textPrimary

    // Pre-measure the per-point labels at a first guess font size, then
    // shrink if they'd overlap on the current chart width. Measured once
    // per layout pass — cheap even on 12 points.
    val textMeasurer = rememberTextMeasurer()
    val labels = remember(serie, currencyCode, formatLocale) {
        serie.map { p ->
            if (p.amount == BigDecimal.ZERO) null
            else formatAmount(p.amount, currencyCode, formatLocale)
        }
    }

    Canvas(modifier = modifier) {
        if (serie.size < 2) return@Canvas
        val w = size.width
        val h = size.height
        // Reserve the top strip for the per-point labels so the chart
        // itself doesn't sit under its own text.
        val labelReserve = 20.dp.toPx()
        val chartTop = labelReserve
        val chartHeight = h - chartTop

        // Horizontal grid lines (3 bands) across the chart area only.
        for (i in 0..3) {
            val y = chartTop + chartHeight * i / 3f
            drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
        }

        val step = w / (serie.size - 1).coerceAtLeast(1)
        val points = serie.mapIndexed { i, p ->
            val x = i * step
            val norm = (p.amount.doubleValue(false) / maxDouble).toFloat()
            val y = chartTop + chartHeight - (chartHeight * norm.coerceIn(0f, 1f))
            Offset(x, y)
        }

        // Filled area under the line.
        val fillPath = Path().apply {
            moveTo(points.first().x, chartTop + chartHeight)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, chartTop + chartHeight)
            close()
        }
        drawPath(fillPath, color = fillColor)
        // Stroke path.
        val strokePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(
            strokePath,
            color = lineColor,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round),
        )
        // Dots.
        points.forEach { pt ->
            drawCircle(color = lineColor, radius = 3f, center = pt)
        }

        // Per-point value labels. Start at a comfortable size and shrink
        // down to a floor if the widest label would collide with its
        // neighbour's slot — one in two points gets a label if even that
        // doesn't fit at the floor size.
        val slotWidth = step
        var fontSizeSp = 10f
        var measured: List<androidx.compose.ui.text.TextLayoutResult?>
        var paintEveryOther = false
        while (true) {
            val style = androidx.compose.ui.text.TextStyle(fontSize = fontSizeSp.sp)
            measured = labels.map { if (it == null) null else textMeasurer.measure(it, style) }
            val widest = measured.maxOfOrNull { it?.size?.width ?: 0 } ?: 0
            if (widest <= slotWidth || fontSizeSp <= 7f) {
                // Still too wide at floor font — fall back to painting
                // one in two labels so adjacent slots have 2× the room.
                if (widest > slotWidth) paintEveryOther = true
                break
            }
            fontSizeSp -= 1f
        }
        measured.forEachIndexed { i, layout ->
            if (layout == null) return@forEachIndexed
            if (paintEveryOther && i % 2 == 0) return@forEachIndexed
            val x = (points[i].x - layout.size.width / 2f).coerceIn(0f, w - layout.size.width)
            // 4dp above the dot — keeps the label clear of the fill area.
            val y = (points[i].y - layout.size.height - 4.dp.toPx()).coerceAtLeast(0f)
            drawText(layout, color = labelColor, topLeft = Offset(x, y))
        }
    }
}

// ----------------------------------------------------------------------
// Relevé tab
// ----------------------------------------------------------------------

@Composable
private fun DashboardStatementsTab(
    data: DashboardStatementsUiState,
    currencyCode: String,
    formatLocale: String?,
    onClickExportCsv: () -> Unit = {},
    onClickExportPdf: () -> Unit = {},
    onPickStatementsPeriod: (DashboardStatementsPeriod) -> Unit = {},
    isBusy: Boolean = false,
) {
    // Expansion state. We default to EXPANDED, so the map stores the inverse
    // (collapsed-when-true) to keep "absent key" meaning "expanded by default".
    val collapsedMap = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
    ) {
        // Period picker — hidden until the first flow emission populates
        // availablePeriods; otherwise the default sentinel "Jan 1970"
        // flashes at the top of the tab during the Relevé fetch.
        if (data.availablePeriods.isNotEmpty()) {
            MonthOrYearPicker(
                selectedPeriod = data.selectedPeriod,
                availablePeriods = data.availablePeriods,
                onPick = onPickStatementsPeriod,
            )
            Spacer(Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onClickExportPdf,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(Res.string.dashboard_statements_export_pdf),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            OutlinedButton(
                onClick = onClickExportCsv,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(Res.string.dashboard_statements_export_csv),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }

        // Inline loader anchored right under the export buttons — both for
        // the Relevé fetch and the CSV/PDF export generation. Sits above
        // the first client card so the user sees the click register where
        // they tapped. Reserves its own vertical slot so the content below
        // doesn't jump when the loader toggles on/off.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .padding(bottom = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (isBusy) {
                LinearProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = AppColors.textPrimary,
                    trackColor = AppColors.textPrimary.copy(alpha = 0.24f),
                )
            }
        }

        if (data.clients.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.dashboard_statements_empty_period),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.textSecondary,
                )
            }
        } else {
            data.clients.forEach { client ->
                val expanded = collapsedMap[client.displayName] != true
                ClientStatementCard(
                    client = client,
                    currencyCode = currencyCode,
                    formatLocale = formatLocale,
                    expanded = expanded,
                    onToggle = {
                        collapsedMap[client.displayName] = expanded
                    },
                )
                Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun MonthOrYearPicker(
    selectedPeriod: DashboardStatementsPeriod,
    availablePeriods: List<DashboardStatementsPeriod>,
    onPick: (DashboardStatementsPeriod) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = labelForStatementsPeriod(selectedPeriod)
    val scrollState = rememberScrollState()
    // Cap the dropdown height at 30 % of the window so a long history
    // (several years' worth of months) doesn't paint across the screen.
    // Fallback to 240.dp during the first compose pass before the window
    // reports its size.
    val windowInfo = androidx.compose.ui.platform.LocalWindowInfo.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val maxHeight = with(density) {
        val heightPx = windowInfo.containerSize.height
        if (heightPx > 0) (heightPx * 0.3f).toDp() else 240.dp
    }
    Box {
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            shape = RoundedCornerShape(12.dp),
            color = AppColors.surfaceSubtle,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Filled.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = AppColors.textSecondary,
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            scrollState = scrollState,
            modifier = Modifier
                .heightIn(max = maxHeight)
                .drawScrollThumb(scrollState),
        ) {
            availablePeriods.forEach { period ->
                DropdownMenuItem(
                    // Leave room on the right so the item label doesn't
                    // overlap the scrollbar thumb.
                    modifier = Modifier.padding(end = 14.dp),
                    text = { Text(labelForStatementsPeriod(period)) },
                    onClick = {
                        expanded = false
                        onPick(period)
                    },
                )
            }
        }
    }
}

private fun labelForStatementsPeriod(period: DashboardStatementsPeriod): String = when (period) {
    is DashboardStatementsPeriod.Month -> "${shortFrenchMonth(period.monthNumber)} ${period.year}"
    is DashboardStatementsPeriod.Year -> "Année ${period.year}"
}

private fun shortFrenchMonth(m: Int): String = when (m) {
    1 -> "Jan"; 2 -> "Fév"; 3 -> "Mar"; 4 -> "Avr"; 5 -> "Mai"; 6 -> "Juin"
    7 -> "Juil"; 8 -> "Août"; 9 -> "Sept"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Déc"
    else -> ""
}

@Composable
private fun ClientStatementCard(
    client: DashboardClientStatement,
    currencyCode: String,
    formatLocale: String?,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = AppColors.surfaceSubtle,
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = client.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    client.email?.let { email ->
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.textSecondary,
                        )
                    }
                    val summary = buildList {
                        if (client.invoices.isNotEmpty()) {
                            add("${client.invoices.size} ${if (client.invoices.size > 1) "factures" else "facture"}")
                        }
                        if (client.creditNotes.isNotEmpty()) {
                            add("${client.creditNotes.size} ${if (client.creditNotes.size > 1) "avoirs" else "avoir"}")
                        }
                        if (client.deliveryNotes.isNotEmpty()) add("${client.deliveryNotes.size} BL")
                        if (client.salesOrders.isNotEmpty()) add("${client.salesOrders.size} BC")
                        if (client.quotes.isNotEmpty()) add("${client.quotes.size} devis")
                    }.joinToString(" • ")
                    if (summary.isNotEmpty()) {
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.textSecondary,
                        )
                    }
                }
                val rotation by animateFloatAsState(if (expanded) 180f else 0f)
                Icon(
                    Icons.Filled.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp).rotate(rotation),
                    tint = AppColors.textSecondary,
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    if (client.invoices.isNotEmpty()) {
                        StatementSection(
                            title = stringResource(Res.string.dashboard_tab_invoices),
                            rows = client.invoices,
                            currencyCode = currencyCode,
                            formatLocale = formatLocale,
                        )
                    }
                    if (client.creditNotes.isNotEmpty()) {
                        StatementSection(
                            title = stringResource(Res.string.appbar_credit_notes),
                            rows = client.creditNotes,
                            currencyCode = currencyCode,
                            formatLocale = formatLocale,
                        )
                    }
                    if (client.deliveryNotes.isNotEmpty()) {
                        StatementSection(
                            title = stringResource(Res.string.appbar_delivery_notes),
                            rows = client.deliveryNotes,
                            currencyCode = currencyCode,
                            formatLocale = formatLocale,
                        )
                    }
                    if (client.salesOrders.isNotEmpty()) {
                        StatementSection(
                            title = stringResource(Res.string.appbar_sales_orders),
                            rows = client.salesOrders,
                            currencyCode = currencyCode,
                            formatLocale = formatLocale,
                        )
                    }
                    if (client.quotes.isNotEmpty()) {
                        StatementSection(
                            title = stringResource(Res.string.appbar_quotes),
                            rows = client.quotes,
                            currencyCode = currencyCode,
                            formatLocale = formatLocale,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatementSection(
    title: String,
    rows: List<DashboardStatementRow>,
    currencyCode: String,
    formatLocale: String?,
) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = AppColors.textSecondary,
        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
    )
    rows.forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = row.number,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = row.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary,
                )
            }
            val tagColor = statusDotColor(row.tagName)
            if (tagColor != null) {
                Box(Modifier.size(8.dp).background(tagColor, RoundedCornerShape(50)))
                Spacer(Modifier.size(8.dp))
            }
            Text(
                text = formatAmount(row.totalAmount, currencyCode, formatLocale),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun statusDotColor(tagName: String?): Color? = when (tagName) {
    "paid" -> ColorGreenPaid
    "sent" -> ColorSent
    "late" -> ColorRedLate
    "reminded" -> ColorReminded
    "cancelled" -> ColorCancelled
    "draft" -> ColorGreyDraft
    else -> null
}
