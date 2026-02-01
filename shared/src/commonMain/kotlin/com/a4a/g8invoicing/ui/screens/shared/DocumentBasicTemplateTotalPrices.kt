package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.currency
import com.a4a.g8invoicing.shared.resources.document_tax
import com.a4a.g8invoicing.shared.resources.document_total_with_tax
import com.a4a.g8invoicing.shared.resources.document_total_without_tax
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.theme.textForDocuments
import com.a4a.g8invoicing.ui.theme.textForDocumentsBold
import com.a4a.g8invoicing.data.stripTrailingZeros
import com.a4a.g8invoicing.data.toStringWithTwoDecimals
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import org.jetbrains.compose.resources.stringResource

@Composable
fun DocumentBasicTemplateTotalPrices(
    uiState: DocumentState,
    footerArray: List<String>,
) {
    val currencySymbol = stringResource(Res.string.currency)

    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp, end = 3.dp)
    ) {
        val paddingBottom = 5.dp
        Spacer(
            modifier = Modifier
                .weight(1f)
        )
        Column(
            horizontalAlignment = Alignment.End,
           modifier = Modifier
                .padding(end = 3.dp)
        ) {
            // Will display the following:
            // Total HT
            // TVA 20% :
            // TVA 5% :
            // Total TTC :

            if (footerArray.any { it == PricesRowName.TOTAL_WITHOUT_TAX.name }) {
                Text(
                    modifier = Modifier
                        .padding(bottom = paddingBottom),
                    style = MaterialTheme.typography.textForDocuments,
                    text = stringResource(Res.string.document_total_without_tax) + " "
                )
            }
            if (footerArray.any { it.contains("TAXES") }) {
                val taxes = footerArray
                    .filter { it.contains("TAXES") }
                    .map { it }.toMutableList()

                val taxesAmount = mutableListOf<BigDecimal>()
                taxes.forEach {
                    taxesAmount.add(BigDecimal.parseString(it.removePrefix("TAXES_")))
                }

                taxesAmount.forEach { tax ->
                    uiState.documentTotalPrices?.totalAmountsOfEachTax?.firstOrNull {
                        tax.stripTrailingZeros().toPlainString() in it.first.stripTrailingZeros().toPlainString()
                    }?.let {
                        Text(
                            modifier = Modifier
                                .padding(bottom = paddingBottom),
                            style = MaterialTheme.typography.textForDocuments,
                            text = stringResource(Res.string.document_tax) + " " + it.first.stripTrailingZeros().toPlainString().replace(".", ",") + "% : "
                        )
                    }
                }
            }
            if (footerArray.any { it == PricesRowName.TOTAL_WITH_TAX.name }) {
                Text(
                    style = MaterialTheme.typography.textForDocumentsBold,
                    text = stringResource(Res.string.document_total_with_tax) + " "
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            // Will display the values:
            //  12€
            //  1€
             // 1€
            //  14€
            if (footerArray.any { it == PricesRowName.TOTAL_WITHOUT_TAX.name }) {
                Text(
                    modifier = Modifier
                        .padding(bottom = paddingBottom),
                    style = MaterialTheme.typography.textForDocuments,
                    text = (uiState.documentTotalPrices?.totalPriceWithoutTax?.toStringWithTwoDecimals()?.replace(".", ",")
                        ?: " - ") + currencySymbol
                )
            }
            if (footerArray.any { it.contains("TAXES") }) {
                val taxes = footerArray
                    .filter { it.contains("TAXES") }
                    .map { it }.toMutableList()

                val taxesAmount = mutableListOf<BigDecimal>()
                taxes.forEach {
                    taxesAmount.add(BigDecimal.parseString(it.removePrefix("TAXES_")))
                }

                taxesAmount.forEach { tax ->
                    uiState.documentTotalPrices?.totalAmountsOfEachTax?.firstOrNull {
                        tax.stripTrailingZeros().toPlainString() in it.first.stripTrailingZeros().toPlainString()
                    }?.let {
                        Text(
                            modifier = Modifier
                                .padding(bottom = paddingBottom),
                            style = MaterialTheme.typography.textForDocuments,
                            text = it.second.toStringWithTwoDecimals().replace(".", ",") + currencySymbol
                        )
                    }
                }
            }
            if (footerArray.any { it == PricesRowName.TOTAL_WITH_TAX.name }) {
                Text(
                    style = MaterialTheme.typography.textForDocumentsBold,
                    text = (uiState.documentTotalPrices?.totalPriceWithTax?.toStringWithTwoDecimals()?.replace(".", ",")
                        ?: " - ") + currencySymbol
                )
            }
        }
    }
}
