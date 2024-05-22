package com.a4a.g8invoicing.ui.screens

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
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.theme.textForDocuments
import com.a4a.g8invoicing.ui.theme.textForDocumentsImportant
import java.math.BigDecimal

@Composable
fun DeliveryNoteBasicTemplateFooter(
    uiState: DeliveryNoteState,
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

            if (footerArray.any { it.rowDescription == FooterRowName.TOTAL_WITHOUT_TAX.name }) { // Handle the page on which it's displayed
                Text(
                    modifier = Modifier
                        .padding(bottom = 3.dp),
                    style = MaterialTheme.typography.textForDocuments,
                    text = stringResource(id = R.string.delivery_note_total_without_tax) + " "
                )
            }
            if (footerArray.any { it.rowDescription.contains(FooterRowName.TAXES_5.name) }) {
                uiState.documentPrices?.totalAmountsOfEachTax?.first { it.first == BigDecimal(5) }?.let {
                    Text(
                        modifier = Modifier
                            .padding(bottom = 3.dp),
                        style = MaterialTheme.typography.textForDocuments,
                        text = stringResource(id = R.string.delivery_note_tax) + " " + it.first.toString() + "% : "
                    )
                }
            }
            if (footerArray.any { it.rowDescription.contains(FooterRowName.TAXES_10.name) }) {
                uiState.documentPrices?.totalAmountsOfEachTax?.first { it.first == BigDecimal(10) }?.let {
                    Text(
                        modifier = Modifier
                            .padding(bottom = 3.dp),
                        style = MaterialTheme.typography.textForDocuments,
                        text = stringResource(id = R.string.delivery_note_tax) + " " + it.first.toString() + "% : "
                    )
                }
            }
            if (footerArray.any { it.rowDescription.contains(FooterRowName.TAXES_20.name) }) {
                uiState.documentPrices?.totalAmountsOfEachTax?.first { it.first == BigDecimal(20) }?.let {
                    Text(
                        modifier = Modifier
                            .padding(bottom = 3.dp),
                        style = MaterialTheme.typography.textForDocuments,
                        text = stringResource(id = R.string.delivery_note_tax) + " " + it.first.toString() + "% : "
                    )
                }
            }
            if (footerArray.any { it.rowDescription == FooterRowName.TOTAL_WITH_TAX.name }) {
                Text(
                    style = MaterialTheme.typography.textForDocumentsImportant,
                    text = stringResource(id = R.string.delivery_note_total_with_tax) + " "
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
                    style = MaterialTheme.typography.textForDocuments,
                    text = (uiState.documentPrices?.totalPriceWithoutTax?.toString()
                        ?: " - ") + stringResource(id = R.string.currency)
                )
            }
            if (footerArray.any { it.rowDescription.contains(FooterRowName.TAXES_5.name) }) {
                uiState.documentPrices?.totalAmountsOfEachTax?.first { it.first == BigDecimal(5) }?.let {
                    Text(
                        modifier = Modifier
                            .padding(bottom = 3.dp, end = 3.dp),
                        style = MaterialTheme.typography.textForDocuments,
                        text = it.second.toString() + stringResource(id = R.string.currency)
                    )
                }
            }
            if (footerArray.any { it.rowDescription.contains(FooterRowName.TAXES_10.name) }) {
                uiState.documentPrices?.totalAmountsOfEachTax?.first { it.first == BigDecimal(10) }?.let {
                    Text(
                        modifier = Modifier
                            .padding(bottom = 3.dp, end = 3.dp),
                        style = MaterialTheme.typography.textForDocuments,
                        text = it.second.toString() + stringResource(id = R.string.currency)
                    )
                }
            }
            if (footerArray.any { it.rowDescription.contains(FooterRowName.TAXES_20.name) }) {
                uiState.documentPrices?.totalAmountsOfEachTax?.first { it.first == BigDecimal(20) }?.let {
                    Text(
                        modifier = Modifier
                            .padding(bottom = 3.dp, end = 3.dp),
                        style = MaterialTheme.typography.textForDocuments,
                        text = it.second.toString() + stringResource(id = R.string.currency)
                    )
                }
            }

            if (footerArray.any { it.rowDescription == FooterRowName.TOTAL_WITH_TAX.name }) {
                Text(
                    modifier = Modifier
                        .padding(end = 3.dp),
                    style = MaterialTheme.typography.textForDocumentsImportant,
                    text = (uiState.documentPrices?.totalPriceWithTax?.toString()
                        ?: " - ") + stringResource(id = R.string.currency)
                )
            }
        }
    }
}