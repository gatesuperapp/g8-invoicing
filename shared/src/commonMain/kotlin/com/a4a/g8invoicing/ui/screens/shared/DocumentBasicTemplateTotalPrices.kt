package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.document_tax_label
import com.a4a.g8invoicing.shared.resources.document_total_with_tax
import com.a4a.g8invoicing.shared.resources.document_total_without_tax
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.theme.textForDocuments
import com.a4a.g8invoicing.ui.theme.textForDocumentsBold
import com.a4a.g8invoicing.data.formatAmount
import com.a4a.g8invoicing.data.stripTrailingZeros
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import org.jetbrains.compose.resources.stringResource

@Composable
fun DocumentBasicTemplateTotalPrices(
    uiState: DocumentState,
    footerArray: List<String>,
    labels: Map<String, String>? = null,
) {
    val currencyCode = uiState.currency.text.ifEmpty { "EUR" }
    val formatLocale = uiState.formatLocale
    val paddingBottom = 5.dp

    data class Line(val label: String, val amount: String, val bold: Boolean)
    val lines = buildList {
        if (footerArray.any { it == PricesRowName.TOTAL_WITHOUT_TAX.name }) {
            add(Line(
                label = documentLabel(labels, "document_total_without_tax", Res.string.document_total_without_tax),
                amount = uiState.documentTotalPrices?.totalPriceWithoutTax?.let { formatAmount(it, currencyCode, formatLocale) } ?: " - ",
                bold = false,
            ))
        }
        if (footerArray.any { it.contains("TAXES") }) {
            val taxesAmount = footerArray
                .filter { it.contains("TAXES") }
                .map { BigDecimal.parseString(it.removePrefix("TAXES_")) }
            taxesAmount.forEach { tax ->
                uiState.documentTotalPrices?.totalAmountsOfEachTax?.firstOrNull {
                    tax.stripTrailingZeros().toPlainString() in it.first.stripTrailingZeros().toPlainString()
                }?.let {
                    val taxRate = "${it.first.stripTrailingZeros().toPlainString().replace(".", ",")}%"
                    val taxLabel = labels?.get("document_tax_label")?.replace("%1\$s", taxRate)
                        ?: stringResource(Res.string.document_tax_label, taxRate)
                    add(Line(label = taxLabel, amount = formatAmount(it.second, currencyCode, formatLocale), bold = false))
                }
            }
        }
        if (footerArray.any { it == PricesRowName.TOTAL_WITH_TAX.name }) {
            add(Line(
                label = documentLabel(labels, "document_total_with_tax", Res.string.document_total_with_tax),
                amount = uiState.documentTotalPrices?.totalPriceWithTax?.let { formatAmount(it, currencyCode, formatLocale) } ?: " - ",
                bold = true,
            ))
        }
    }
    if (lines.isEmpty()) return

    val boldStyle = MaterialTheme.typography.textForDocumentsBold
    val regularStyle = MaterialTheme.typography.textForDocuments

    // Measure the widest amount so the amount column can be pinned to just
    // that width (right-aligned). Otherwise weight(1f) would hand each cell
    // an equal 50% slice and leave a wide gap between the label's ":" and a
    // short amount like "5.00 €". The label side keeps weight(1f) so it grows
    // to fill whatever's left, and both sides right-align.
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val amountColumnWidthDp = remember(lines, regularStyle, boldStyle, density) {
        val maxPx = lines.maxOf { line ->
            measurer.measure(line.amount, style = if (line.bold) boldStyle else regularStyle).size.width
        }
        with(density) { maxPx.toDp() }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp, end = 3.dp),
    ) {
        Spacer(modifier = Modifier.weight(1f))
        // IntrinsicSize.Max on the Column so every Row shares the widest
        // (label + gap + amount) natural size. Each Row then fillMaxWidth to
        // stretch to that shared width, letting the label side (weight 1f,
        // textAlign End) push its ":" against the amount and letting every
        // label ":" land on the same vertical.
        Column(modifier = Modifier.width(IntrinsicSize.Max)) {
            lines.forEachIndexed { index, line ->
                val isLast = index == lines.lastIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (isLast) 0.dp else paddingBottom),
                ) {
                    val style = if (line.bold) boldStyle else regularStyle
                    Text(
                        text = line.label + " ",
                        style = style,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f).alignByBaseline(),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = line.amount,
                        style = style,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(amountColumnWidthDp).alignByBaseline(),
                    )
                }
            }
        }
    }
}
