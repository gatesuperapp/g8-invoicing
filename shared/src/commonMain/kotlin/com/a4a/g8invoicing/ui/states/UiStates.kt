package com.a4a.g8invoicing.ui.states

data class InvoicesUiState(
    val documentStates: List<InvoiceState> = listOf(),
    val userMessages: List<Message> = listOf(),
    val isFetchingDeliveryNotes: Boolean = false,
    val createdCreditNoteId: Long? = null,
    val createdCorrectedInvoiceId: Long? = null,
)

data class DeliveryNotesUiState(
    val deliveryNoteStates: List<DeliveryNoteState> = listOf(),
    val userMessages: List<Message> = listOf(),
    val isFetchingDeliveryNotes: Boolean = false,
    val createdInvoiceId: Long? = null
)

data class SalesOrdersUiState(
    val salesOrderStates: List<SalesOrderState> = listOf(),
    val userMessages: List<Message> = listOf(),
    val isFetchingSalesOrders: Boolean = false,
    val createdInvoiceId: Long? = null,
    val createdDeliveryNoteId: Long? = null,
)

// ---- Dashboard ---------------------------------------------------------

data class DashboardUiState(
    // True while the "core" fetch (invoices) is in flight — gates the
    // Factures and Produits tabs' overlay spinner.
    val isLoading: Boolean = true,
    // Statements-tab fetch (BL + devis + BC + avoirs) is deliberately lazy
    // so switching to the Dashboard doesn't block on data most users never
    // look at. True once the user visits the Relevé tab until the second
    // combined flow emits.
    val isStatementsLoading: Boolean = false,
    val currencyCode: String = "EUR",
    val formatLocale: String? = null,
    val invoicesTab: DashboardInvoicesUiState = DashboardInvoicesUiState(),
    val productsTab: DashboardProductsUiState = DashboardProductsUiState(),
    val statementsTab: DashboardStatementsUiState = DashboardStatementsUiState(),
)

data class DashboardInvoicesUiState(
    val totalSalesAmount: com.ionspin.kotlin.bignum.decimal.BigDecimal = com.ionspin.kotlin.bignum.decimal.BigDecimal.ZERO,
    val totalSalesCount: Int = 0,
    val paidAmount: com.ionspin.kotlin.bignum.decimal.BigDecimal = com.ionspin.kotlin.bignum.decimal.BigDecimal.ZERO,
    val paidCount: Int = 0,
    val toPayAmount: com.ionspin.kotlin.bignum.decimal.BigDecimal = com.ionspin.kotlin.bignum.decimal.BigDecimal.ZERO,
    val toPayCount: Int = 0,
    val lateAmount: com.ionspin.kotlin.bignum.decimal.BigDecimal = com.ionspin.kotlin.bignum.decimal.BigDecimal.ZERO,
    val lateCount: Int = 0,
    val monthlyRows: List<DashboardMonthRow> = emptyList(),
    // 12-point TTC-sum series across the window (oldest → newest), used by the
    // chart under the per-month list. Same shape as the Produits series so the
    // same LineChart renders both.
    val salesSeries: List<DashboardProductMonthlyAmount> = emptyList(),
    // Current window shown by the Factures tab — defaults to the rolling
    // 12-month view, overridable to a specific calendar year via the
    // dropdown that replaced the "12 derniers mois" header.
    val selectedPeriod: DashboardPeriod = DashboardPeriod.Rolling12,
    // Distinct calendar years with ≥1 invoice; drives the dropdown items.
    // Sorted newest-first.
    val availableYears: List<Int> = emptyList(),
)

sealed class DashboardPeriod {
    data object Rolling12 : DashboardPeriod()
    data class Year(val year: Int) : DashboardPeriod()
}

data class DashboardMonthRow(
    // "2026-10" — key for ordering/grouping only, not shown.
    val yearMonth: String,
    // "Octobre 2026" — localised, ready for display.
    val monthLabel: String,
    // "2026" — used to break the section header above the first row of a year.
    val yearLabel: String,
    val totalAmount: com.ionspin.kotlin.bignum.decimal.BigDecimal,
    val paidCount: Int,
    // SENT + REMINDED + LATE — anything emitted and not yet marked paid.
    val pendingCount: Int,
)

// ---- Dashboard — Produits ---------------------------------------------

data class DashboardProductsUiState(
    val isEmpty: Boolean = true,
    // Flat alphabetised list used by the chart's product picker. Includes
    // every product name that landed on a paid invoice in the last 12 months.
    val allProductNames: List<String> = emptyList(),
    // Product name charted by default — the top earner across the 12-month
    // window. Null when there's nothing to show.
    val defaultProductName: String? = null,
    // Top products — ordered amount desc — for the user-picked period (month
    // or year). Default picker value = current month, overridable from the
    // picker above the list on the Produits tab.
    val topProducts: List<DashboardProductLine> = emptyList(),
    // Period powering the top-products list. Defaults to the current calendar
    // month at VM init; the dropdown lets the user switch to a previous
    // month or a whole year — same picker contract as the Relevé tab.
    val selectedPeriod: DashboardStatementsPeriod = DashboardStatementsPeriod.Month(1970, 1),
    val availablePeriods: List<DashboardStatementsPeriod> = emptyList(),
    // 12-point series keyed by product name — the chart just pulls the
    // selected product's entry on render. Each row's list length = 12,
    // oldest first, newest last.
    val seriesByProduct: Map<String, List<DashboardProductMonthlyAmount>> = emptyMap(),
)

data class DashboardProductLine(
    val name: String,
    val amount: com.ionspin.kotlin.bignum.decimal.BigDecimal,
)

data class DashboardProductMonthlyAmount(
    val yearMonth: String,
    val monthLabel: String,
    val amount: com.ionspin.kotlin.bignum.decimal.BigDecimal,
)

// ---- Dashboard — Relevé -----------------------------------------------

data class DashboardStatementsUiState(
    val clients: List<DashboardClientStatement> = emptyList(),
    // Period the accordion is currently filtered on. Defaults to the current
    // month at VM init; the dropdown at the top of the Relevé tab lets the
    // user switch to a previous month or a whole year.
    val selectedPeriod: DashboardStatementsPeriod = DashboardStatementsPeriod.Month(1970, 1),
    // Months + Year rolls going from today back to the first doc date,
    // oldest-first drops off the list. Drives the dropdown items.
    val availablePeriods: List<DashboardStatementsPeriod> = emptyList(),
)

data class DashboardClientStatement(
    // Display name — "Prénom Nom" or "Société" depending on the client type.
    val displayName: String,
    // First non-blank email captured across the client's docs — rendered as
    // the subtitle under the name on the card. Null when the client has no
    // email on file.
    val email: String? = null,
    val invoices: List<DashboardStatementRow> = emptyList(),
    val creditNotes: List<DashboardStatementRow> = emptyList(),
    val deliveryNotes: List<DashboardStatementRow> = emptyList(),
    val salesOrders: List<DashboardStatementRow> = emptyList(),
    val quotes: List<DashboardStatementRow> = emptyList(),
)

sealed class DashboardStatementsPeriod {
    data class Month(val year: Int, val monthNumber: Int) : DashboardStatementsPeriod()
    data class Year(val year: Int) : DashboardStatementsPeriod()
}

data class DashboardStatementRow(
    val docId: Int,
    val number: String,
    // "dd/MM/yyyy" — raw as-stored, display layer can reformat if needed.
    val date: String,
    val totalAmount: com.ionspin.kotlin.bignum.decimal.BigDecimal,
    // Lowercased tag name (e.g. "paid", "late") for colour mapping. Nullable
    // for BL/Devis rows that predate the tagging module.
    val tagName: String? = null,
)

data class QuotesUiState(
    val quoteStates: List<QuoteState> = listOf(),
    val userMessages: List<Message> = listOf(),
    val isFetchingQuotes: Boolean = false,
    val createdInvoiceId: Long? = null
)

data class CreditNotesUiState(
    val documentStates: List<CreditNoteState> = listOf(),
    val userMessages: List<Message> = listOf(),
    val isFetchingCreditNotes: Boolean = false
)

data class ProductsUiState(
    val products: List<ProductState> = listOf(),
    val userMessages: List<Message> = listOf(),
    val isFetchingProducts: Boolean = false
)

data class ClientsOrIssuerUiState(
    val clientsOrIssuerList: List<ClientOrIssuerState> = listOf(),
    val userMessages: List<Message> = listOf(),
    val isFetchingClientsOrIssuers: Boolean = false
)
