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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.theme.textForDocumentsVerySmall
import com.a4a.g8invoicing.ui.theme.textForDocumentsVerySmallBold
import java.math.BigDecimal

@Composable
fun DocumentBasicTemplatePrices(
    uiState: DocumentState,
    footerArray: List<FooterRow>,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        Spacer(
            modifier = Modifier
                .weight(1f)
        )
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(
                end = 8.dp
            )
        ) {
            // Will display the following:
            // Total HT
            // TVA 20% :
            // TVA 5% :
            // Total TTC :

            if (footerArray.any { it.rowDescription == FooterRowName.TOTAL_WITHOUT_TAX.name }) {
                Text(
                    modifier = Modifier
                        .padding(bottom = 3.dp),
                    style = MaterialTheme.typography.textForDocumentsVerySmall,
                    text = stringResource(id = R.string.document_total_without_tax) + " "
                )
            }
            if (footerArray.any { it.rowDescription.contains("TAXES") }) {
                val taxes = footerArray
                    .filter { it.rowDescription.contains("TAXES") }
                    .map { it.rowDescription }.toMutableList()

                val taxesAmount = mutableListOf<BigDecimal>()
                taxes.forEach {
                    taxesAmount += it.removePrefix("TAXES_").toBigDecimal()
                }

                taxesAmount.forEach { tax ->
                    uiState.documentPrices?.totalAmountsOfEachTax?.first {
                        it.first.stripTrailingZeros() == tax.stripTrailingZeros()
                    }?.let {
                        Text(
                            modifier = Modifier
                                .padding(bottom = 3.dp),
                            style = MaterialTheme.typography.textForDocumentsVerySmall,
                            text = stringResource(id = R.string.document_tax) + " " + it.first.toString() + "% : "
                        )
                    }
                }
            }
            if (footerArray.any { it.rowDescription == FooterRowName.TOTAL_WITH_TAX.name }) {
                Text(
                    style = MaterialTheme.typography.textForDocumentsVerySmallBold,
                    text = stringResource(id = R.string.document_total_with_tax) + " "
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            // Will display the values as follows:
            // Total HT : 12€
            // TVA 20% : 1€
            // TVA 5% : 1€
            // Total TTC : 14€
            if (footerArray.any { it.rowDescription == FooterRowName.TOTAL_WITHOUT_TAX.name }) {
                Text(
                    modifier = Modifier
                        .padding(bottom = 3.dp, end = 3.dp),
                    style = MaterialTheme.typography.textForDocumentsVerySmall,
                    text = (uiState.documentPrices?.totalPriceWithoutTax?.toString()
                        ?: " - ") + stringResource(id = R.string.currency)
                )
            }
            if (footerArray.any { it.rowDescription.contains("TAXES") }) {
                val taxes = footerArray
                    .filter { it.rowDescription.contains("TAXES") }
                    .map { it.rowDescription }.toMutableList()

                val taxesAmount = mutableListOf<BigDecimal>()
                taxes.forEach {
                    taxesAmount += it.removePrefix("TAXES_").toBigDecimal()
                }

                taxesAmount.forEach { tax ->
                    uiState.documentPrices?.totalAmountsOfEachTax?.first {
                        it.first.stripTrailingZeros() == tax.stripTrailingZeros()
                    }?.let {
                        Text(
                            modifier = Modifier
                                .padding(bottom = 3.dp, end = 3.dp),
                            style = MaterialTheme.typography.textForDocumentsVerySmall,
                            text = it.second.toString() + stringResource(id = R.string.currency)
                        )
                    }
                }
            }
            if (footerArray.any { it.rowDescription == FooterRowName.TOTAL_WITH_TAX.name }) {
                Text(
                    modifier = Modifier
                        .padding(end = 3.dp),
                    style = MaterialTheme.typography.textForDocumentsVerySmallBold,
                    text = (uiState.documentPrices?.totalPriceWithTax?.toString()
                        ?: " - ") + stringResource(id = R.string.currency)
                )
            }
        }
    }
}