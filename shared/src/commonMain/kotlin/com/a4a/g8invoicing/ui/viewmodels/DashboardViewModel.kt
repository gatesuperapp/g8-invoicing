package com.a4a.g8invoicing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.AppLocaleHolder
import com.a4a.g8invoicing.data.CreditNoteLocalDataSourceInterface
import com.a4a.g8invoicing.data.CurrencyManager
import com.a4a.g8invoicing.data.DeliveryNoteLocalDataSourceInterface
import com.a4a.g8invoicing.data.InvoiceLocalDataSourceInterface
import com.a4a.g8invoicing.data.QuoteLocalDataSourceInterface
import com.a4a.g8invoicing.data.SalesOrderLocalDataSourceInterface
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.states.DashboardClientStatement
import com.a4a.g8invoicing.ui.states.DashboardInvoicesUiState
import com.a4a.g8invoicing.ui.states.DashboardMonthRow
import com.a4a.g8invoicing.ui.states.DashboardPeriod
import com.a4a.g8invoicing.ui.states.DashboardProductLine
import com.a4a.g8invoicing.ui.states.DashboardProductMonthlyAmount
import com.a4a.g8invoicing.ui.states.DashboardProductsUiState
import com.a4a.g8invoicing.ui.states.DashboardStatementRow
import com.a4a.g8invoicing.ui.states.DashboardStatementsPeriod
import com.a4a.g8invoicing.ui.states.DashboardStatementsUiState
import com.a4a.g8invoicing.ui.states.DashboardUiState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.CreditNoteState
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.states.QuoteState
import com.a4a.g8invoicing.ui.states.SalesOrderState
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.a4a.g8invoicing.data.util.DispatcherProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DashboardViewModel(
    private val invoiceDataSource: InvoiceLocalDataSourceInterface,
    private val currencyManager: CurrencyManager,
    private val deliveryNoteDataSource: DeliveryNoteLocalDataSourceInterface,
    private val quoteDataSource: QuoteLocalDataSourceInterface,
    private val salesOrderDataSource: SalesOrderLocalDataSourceInterface,
    private val creditNoteDataSource: CreditNoteLocalDataSourceInterface,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Factures-tab window toggle. Lives on the VM so the dropdown's pick
    // survives recompositions and triggers a re-fold of the same cached
    // invoice list without re-hitting the DB.
    private val _invoicesPeriod = MutableStateFlow<DashboardPeriod>(DashboardPeriod.Rolling12)

    // Relevé-tab period picker — defaults to the current calendar month at
    // VM creation (fresh enough for a session that opens the Dashboard).
    private val _statementsPeriod = MutableStateFlow<DashboardStatementsPeriod>(
        currentDate().let { DashboardStatementsPeriod.Month(it.year, it.monthNumber) }
    )

    // Produits-tab top-products period picker — same contract as the Relevé
    // picker, same default (current month). Shares the DashboardStatementsPeriod
    // type since the shape (month + year) is identical.
    private val _productsPeriod = MutableStateFlow<DashboardStatementsPeriod>(
        currentDate().let { DashboardStatementsPeriod.Month(it.year, it.monthNumber) }
    )

    private var coreJob: Job? = null
    private var statementsJob: Job? = null
    private var cachedInvoices: List<InvoiceState>? = null

    fun setInvoicesPeriod(period: DashboardPeriod) {
        _invoicesPeriod.value = period
    }

    fun setStatementsPeriod(period: DashboardStatementsPeriod) {
        _statementsPeriod.value = period
    }

    fun setProductsPeriod(period: DashboardStatementsPeriod) {
        _productsPeriod.value = period
    }

    /**
     * Kick off the "core" fetch (invoices only — feeds Factures AND Produits).
     * Called from the DashboardScreen's LaunchedEffect on first compose so
     * nothing happens until the user actually opens the tab. Idempotent —
     * repeated calls while the collect is already active are no-ops.
     */
    fun onEnter() {
        if (coreJob?.isActive == true) return
        val invoiceFlow = invoiceDataSource.fetchAll() ?: run {
            _uiState.update { it.copy(isLoading = false) }
            return
        }
        coreJob = viewModelScope.launch(DispatcherProvider.IO) {
            try {
                combine(invoiceFlow, _invoicesPeriod, _productsPeriod) { invoices, ip, pp ->
                    Triple(invoices, ip, pp)
                }.collect { (invoices, ip, pp) ->
                    cachedInvoices = invoices
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            currencyCode = currencyManager.currentCurrency,
                            formatLocale = AppLocaleHolder.languageCode,
                            invoicesTab = computeInvoicesTab(invoices, ip),
                            productsTab = computeProductsTab(invoices, pp),
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Kick off the Relevé-tab fetch. Only runs the first time the user taps
     * the Relevé tab — the four extra flows (BL, Devis, BC, Avoirs) stay
     * dormant otherwise. Reuses the invoice flow from the core job's cache
     * when it's already populated; otherwise opens its own.
     */
    fun onEnterStatementsTab() {
        if (statementsJob?.isActive == true) return
        _uiState.update { it.copy(isStatementsLoading = true) }
        val blFlow = deliveryNoteDataSource.fetchAll()
            ?: kotlinx.coroutines.flow.flowOf(emptyList())
        val quoteFlow = quoteDataSource.fetchAll()
            ?: kotlinx.coroutines.flow.flowOf(emptyList())
        val soFlow = salesOrderDataSource.fetchAll()
            ?: kotlinx.coroutines.flow.flowOf(emptyList())
        val creditFlow = creditNoteDataSource.fetchAll()
            ?: kotlinx.coroutines.flow.flowOf(emptyList())
        val invoiceFlow = invoiceDataSource.fetchAll()
            ?: kotlinx.coroutines.flow.flowOf(cachedInvoices.orEmpty())
        statementsJob = viewModelScope.launch(DispatcherProvider.IO) {
            try {
                // kotlinx.coroutines.flow.combine's vararg overload caps at
                // 5 flows — bundle the two "extra" doc types + the picker
                // period so we stay under.
                val extrasFlow = kotlinx.coroutines.flow.combine(soFlow, creditFlow) { sos, cns ->
                    sos to cns
                }
                combine(invoiceFlow, blFlow, quoteFlow, extrasFlow, _statementsPeriod) { invs, bls, qs, extras, period ->
                    StatementsInput(invs, bls, qs, extras.first, extras.second, period)
                }.collect { s ->
                    _uiState.update { state ->
                        state.copy(
                            isStatementsLoading = false,
                            statementsTab = computeStatementsTab(
                                s.invoices, s.bls, s.quotes, s.salesOrders, s.creditNotes, s.period,
                            ),
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isStatementsLoading = false) }
            }
        }
    }

    private data class StatementsInput(
        val invoices: List<InvoiceState>,
        val bls: List<DeliveryNoteState>,
        val quotes: List<QuoteState>,
        val salesOrders: List<SalesOrderState>,
        val creditNotes: List<CreditNoteState>,
        val period: DashboardStatementsPeriod,
    )

    // --- Factures tab --------------------------------------------------

    private fun computeInvoicesTab(
        invoices: List<InvoiceState>,
        period: DashboardPeriod,
    ): DashboardInvoicesUiState {
        val today = currentDate()
        // Window the 12 months the UI displays based on the dropdown pick:
        // Rolling12 → last 12 months ending this month; Year(y) → Jan..Dec y.
        val months: List<LocalDate> = when (period) {
            DashboardPeriod.Rolling12 -> (11 downTo 0).map { back ->
                today.minusMonthsSafe(back).let { LocalDate(it.year, it.monthNumber, 1) }
            }
            is DashboardPeriod.Year -> (1..12).map { m -> LocalDate(period.year, m, 1) }
        }
        val windowKeys: Set<String> = months
            .map { formatYearMonthKey(it.year, it.monthNumber) }
            .toSet()

        val everyRelevant = invoices
            .filter { it.documentTag != DocumentTag.DRAFT && it.documentTag != DocumentTag.CANCELLED }
            .mapNotNull { inv ->
                val date = parseIssuingDate(inv.documentDate) ?: return@mapNotNull null
                InvoiceAgg(
                    date = date,
                    totalTtc = inv.documentTotalPrices?.totalPriceWithTax ?: BigDecimal.ZERO,
                    tag = inv.documentTag,
                )
            }

        // Years that have at least one qualifying invoice — feeds the
        // dropdown. Rolling12 is always offered on top of this list by the
        // UI, so we only expose explicit years here.
        val availableYears = everyRelevant
            .map { it.date.year }
            .distinct()
            .sortedDescending()

        val relevant = everyRelevant.filter {
            formatYearMonthKey(it.date.year, it.date.monthNumber) in windowKeys
        }

        val totalSales = relevant.sumAmount()
        val paid = relevant.filter { it.tag == DocumentTag.PAID }
        val toPay = relevant.filter { it.tag == DocumentTag.SENT }
        val late = relevant.filter { it.tag == DocumentTag.LATE || it.tag == DocumentTag.REMINDED }

        val monthlyMap: Map<String, List<InvoiceAgg>> = relevant.groupBy {
            formatYearMonthKey(it.date.year, it.date.monthNumber)
        }
        val monthlyRows = months
            .reversed()
            .map { month ->
                val key = formatYearMonthKey(month.year, month.monthNumber)
                val rows = monthlyMap[key].orEmpty()
                DashboardMonthRow(
                    yearMonth = key,
                    monthLabel = formatMonthLabel(month),
                    yearLabel = month.year.toString(),
                    totalAmount = rows.sumAmount(),
                    paidCount = rows.count { it.tag == DocumentTag.PAID },
                    pendingCount = rows.count {
                        it.tag == DocumentTag.SENT ||
                            it.tag == DocumentTag.LATE ||
                            it.tag == DocumentTag.REMINDED
                    },
                )
            }

        val salesSeries = months.map { month ->
            val key = formatYearMonthKey(month.year, month.monthNumber)
            val rows = monthlyMap[key].orEmpty()
            DashboardProductMonthlyAmount(
                yearMonth = key,
                monthLabel = shortMonthLabel(month),
                amount = rows.sumAmount(),
            )
        }

        return DashboardInvoicesUiState(
            totalSalesAmount = totalSales,
            totalSalesCount = relevant.size,
            paidAmount = paid.sumAmount(),
            paidCount = paid.size,
            toPayAmount = toPay.sumAmount(),
            toPayCount = toPay.size,
            lateAmount = late.sumAmount(),
            lateCount = late.size,
            monthlyRows = monthlyRows,
            salesSeries = salesSeries,
            selectedPeriod = period,
            availableYears = availableYears,
        )
    }

    // --- Produits tab --------------------------------------------------

    private fun computeProductsTab(
        invoices: List<InvoiceState>,
        period: DashboardStatementsPeriod,
    ): DashboardProductsUiState {
        val today = currentDate()
        val oldestAllowed = today.minusMonthsSafe(11).let {
            LocalDate(it.year, it.monthNumber, 1)
        }

        // Any invoice that landed in a client's hands counts as "sold" —
        // paid or still open (sent / reminded / late). Mirrors the Factures
        // tab summary so the two blocks reconcile. DRAFT (never sent) and
        // CANCELLED (voided) are deliberately excluded; the orthogonal
        // "Verrouillée" flag does NOT exclude — a locked paid invoice
        // still counts as revenue. BL / Devis / BC / Avoirs don't
        // participate — they're not revenue.
        val relevantInvoices = invoices.filter {
            it.documentTag != DocumentTag.DRAFT &&
                it.documentTag != DocumentTag.CANCELLED
        }

        // Walk every qualifying invoice once, keep the date so we can later
        // bucket both the rolling-12 chart AND the user-picked period list.
        data class Row(val date: LocalDate, val yearMonth: String, val name: String, val amount: BigDecimal)
        val allRows = relevantInvoices.flatMap { inv ->
            val date = parseIssuingDate(inv.documentDate) ?: return@flatMap emptyList()
            val ym = formatYearMonthKey(date.year, date.monthNumber)
            inv.documentProducts.orEmpty().map { p ->
                val price = p.priceWithoutTax ?: BigDecimal.ZERO
                val qty = p.quantity
                Row(date, ym, p.name.text, price * qty)
            }
        }
        if (allRows.isEmpty()) return DashboardProductsUiState(isEmpty = true)

        // Top products for the user-picked period (month or year). Collapsed
        // by name, ordered amount desc.
        val periodRows = allRows.filter { r ->
            when (period) {
                is DashboardStatementsPeriod.Month ->
                    r.date.year == period.year && r.date.monthNumber == period.monthNumber
                is DashboardStatementsPeriod.Year -> r.date.year == period.year
            }
        }
        val topProducts: List<DashboardProductLine> = periodRows
            .groupBy { it.name }
            .map { (name, bucket) -> DashboardProductLine(name = name, amount = bucket.sumOfBigDecimal { it.amount }) }
            .sortedByDescending { it.amount }

        // --- Chart (rolling 12 months) --------------------------------------
        val rowsIn12m = allRows.filter { it.date >= oldestAllowed }
        val monthlyAggregate = rowsIn12m
            .groupBy { it.yearMonth to it.name }
            .mapValues { (_, bucket) -> bucket.sumOfBigDecimal { it.amount } }
        val months = (11 downTo 0).map { back ->
            today.minusMonthsSafe(back).let { LocalDate(it.year, it.monthNumber, 1) }
        }
        val allNames = monthlyAggregate.keys.map { it.second }.toSortedSet().toList()
        val seriesByProduct: Map<String, List<DashboardProductMonthlyAmount>> = allNames
            .associateWith { name ->
                months.map { month ->
                    val ym = formatYearMonthKey(month.year, month.monthNumber)
                    DashboardProductMonthlyAmount(
                        yearMonth = ym,
                        monthLabel = shortMonthLabel(month),
                        amount = monthlyAggregate[ym to name] ?: BigDecimal.ZERO,
                    )
                }
            }
        val defaultProduct = seriesByProduct
            .mapValues { (_, serie) -> serie.sumOfBigDecimal { it.amount } }
            .maxByOrNull { it.value }
            ?.key

        // Available dropdown items: months going from today back to the
        // oldest paid invoice, with a "Année YYYY" closing each year.
        val minDate = allRows.minOfOrNull { it.date }
        val availablePeriods = buildAvailablePeriods(minDate)

        return DashboardProductsUiState(
            isEmpty = false,
            allProductNames = allNames,
            defaultProductName = defaultProduct,
            topProducts = topProducts,
            selectedPeriod = period,
            availablePeriods = availablePeriods,
            seriesByProduct = seriesByProduct,
        )
    }

    // --- Relevé tab ----------------------------------------------------

    private fun computeStatementsTab(
        invoices: List<InvoiceState>,
        bls: List<DeliveryNoteState>,
        quotes: List<QuoteState>,
        salesOrders: List<SalesOrderState>,
        creditNotes: List<CreditNoteState>,
        period: DashboardStatementsPeriod,
    ): DashboardStatementsUiState {
        // Group by (lastName, firstName) only — ignoring master id — so a BL
        // whose client never got linked to the master still buckets with the
        // same-named invoice. Edge case: two distinct masters with identical
        // names merge visually; acceptable for now.
        data class ClientKey(val lastName: String, val firstName: String) {
            val display: String get() =
                if (firstName.isNotEmpty()) "$lastName $firstName" else lastName
        }
        fun keyOf(c: ClientOrIssuerState?): ClientKey? {
            if (c == null) return null
            val last = c.name.text.trim()
            val first = c.firstName?.text?.trim().orEmpty()
            if (last.isEmpty() && first.isEmpty()) return null
            return ClientKey(lastName = last, firstName = first)
        }
        fun firstEmail(c: ClientOrIssuerState?): String? =
            c?.emails?.firstNotNullOfOrNull { it.email.text.trim().takeIf { s -> s.isNotEmpty() } }

        // Walk every doc once: dates feed the period picker options; the
        // selected period gates which rows actually land in the final list.
        fun dateOf(d: DocumentState): LocalDate? = parseIssuingDate(d.documentDate)
        fun DocumentState.inPeriod(): Boolean {
            val d = dateOf(this) ?: return false
            return when (period) {
                is DashboardStatementsPeriod.Month ->
                    d.year == period.year && d.monthNumber == period.monthNumber
                is DashboardStatementsPeriod.Year ->
                    d.year == period.year
            }
        }

        val everyDoc: List<DocumentState> =
            invoices + bls + quotes + salesOrders + creditNotes
        val minDate: LocalDate? = everyDoc.mapNotNull { dateOf(it) }.minOrNull()

        // Build the period picker list: months going from today's month back
        // to the oldest doc, with a "Année YYYY" entry closing each year.
        val availablePeriods = buildAvailablePeriods(minDate)

        // Capture the first non-blank email seen per client, from any doc.
        val emailsByKey = mutableMapOf<ClientKey, String>()
        fun captureEmail(c: ClientOrIssuerState?) {
            val k = keyOf(c) ?: return
            if (k in emailsByKey) return
            firstEmail(c)?.let { emailsByKey[k] = it }
        }
        invoices.forEach { captureEmail(it.documentClient) }
        bls.forEach { captureEmail(it.documentClient) }
        quotes.forEach { captureEmail(it.documentClient) }
        salesOrders.forEach { captureEmail(it.documentClient) }
        creditNotes.forEach { captureEmail(it.documentClient) }

        fun <T : DocumentState> groupInPeriod(docs: List<T>): Map<ClientKey, List<DashboardStatementRow>> =
            docs.filter { it.inPeriod() }
                .mapNotNull { it to (keyOf(it.documentClient) ?: return@mapNotNull null) }
                .groupBy({ it.second }, { it.first.toStatementRow() })
                .mapValues { it.value.sortedByDescending { r -> r.date } }

        val invoiceRows = groupInPeriod(invoices)
        val blRows = groupInPeriod(bls)
        val quoteRows = groupInPeriod(quotes)
        val soRows = groupInPeriod(salesOrders)
        val creditRows = groupInPeriod(creditNotes)

        val allKeys = (invoiceRows.keys + blRows.keys + quoteRows.keys + soRows.keys + creditRows.keys)
            .distinct()
            .sortedWith(
                compareBy({ it.lastName.lowercase() }, { it.firstName.lowercase() })
            )

        val clients = allKeys.map { key ->
            DashboardClientStatement(
                displayName = key.display,
                email = emailsByKey[key],
                invoices = invoiceRows[key].orEmpty(),
                creditNotes = creditRows[key].orEmpty(),
                deliveryNotes = blRows[key].orEmpty(),
                salesOrders = soRows[key].orEmpty(),
                quotes = quoteRows[key].orEmpty(),
            )
        }
        return DashboardStatementsUiState(
            clients = clients,
            selectedPeriod = period,
            availablePeriods = availablePeriods,
        )
    }

    private fun buildAvailablePeriods(minDate: LocalDate?): List<DashboardStatementsPeriod> {
        if (minDate == null) return emptyList()
        val today = currentDate()
        val result = mutableListOf<DashboardStatementsPeriod>()
        for (year in today.year downTo minDate.year) {
            val startMonth = if (year == today.year) today.monthNumber else 12
            val endMonth = if (year == minDate.year) minDate.monthNumber else 1
            for (m in startMonth downTo endMonth) {
                result.add(DashboardStatementsPeriod.Month(year, m))
            }
            result.add(DashboardStatementsPeriod.Year(year))
        }
        return result
    }

    private fun DocumentState.toStatementRow(): DashboardStatementRow = DashboardStatementRow(
        docId = documentId ?: 0,
        number = documentNumber.text,
        date = documentDate,
        totalAmount = documentTotalPrices?.totalPriceWithTax ?: BigDecimal.ZERO,
        tagName = documentTag.name.lowercase(),
    )

    private fun currentDate(): LocalDate =
        kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}

private data class InvoiceAgg(
    val date: LocalDate,
    val totalTtc: BigDecimal,
    val tag: DocumentTag,
)

private fun List<InvoiceAgg>.sumAmount(): BigDecimal =
    fold(BigDecimal.ZERO) { acc, item -> acc + item.totalTtc }

// Kotlin stdlib's sumOf has overloads for Int/Long/Double only, so we
// provide a BigDecimal one for the dashboard aggregations.
private inline fun <T> Iterable<T>.sumOfBigDecimal(selector: (T) -> BigDecimal): BigDecimal =
    fold(BigDecimal.ZERO) { acc, item -> acc + selector(item) }

private fun parseIssuingDate(raw: String): LocalDate? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null
    runCatching {
        val parts = trimmed.split('/')
        if (parts.size == 3) {
            return LocalDate(parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
        }
    }
    runCatching { return LocalDate.parse(trimmed) }
    return null
}

private fun LocalDate.minusMonthsSafe(months: Int): LocalDate {
    var y = year
    var m = monthNumber - months
    while (m <= 0) {
        m += 12
        y -= 1
    }
    while (m > 12) {
        m -= 12
        y += 1
    }
    val day = minOf(dayOfMonth, lastDayOfMonth(y, m))
    return LocalDate(y, m, day)
}

private fun lastDayOfMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) 29 else 28
    else -> 31
}

private fun formatMonthLabel(date: LocalDate): String {
    val name = when (date.monthNumber) {
        1 -> "Janvier"
        2 -> "Février"
        3 -> "Mars"
        4 -> "Avril"
        5 -> "Mai"
        6 -> "Juin"
        7 -> "Juillet"
        8 -> "Août"
        9 -> "Septembre"
        10 -> "Octobre"
        11 -> "Novembre"
        12 -> "Décembre"
        else -> ""
    }
    return "$name ${date.year}"
}

private fun shortMonthLabel(date: LocalDate): String = when (date.monthNumber) {
    1 -> "Jan"
    2 -> "Fév"
    3 -> "Mar"
    4 -> "Avr"
    5 -> "Mai"
    6 -> "Juin"
    7 -> "Juil"
    8 -> "Août"
    9 -> "Sept"
    10 -> "Oct"
    11 -> "Nov"
    12 -> "Déc"
    else -> ""
}

private fun formatYearMonthKey(year: Int, month: Int): String {
    val m = if (month < 10) "0$month" else month.toString()
    return "$year-$m"
}
