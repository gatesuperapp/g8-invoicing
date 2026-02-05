package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.data.stripTrailingZeros
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.tax_rate_add_rate
import com.a4a.g8invoicing.shared.resources.tax_rate_edit_rates
import com.a4a.g8invoicing.ui.shared.Separator
import com.a4a.g8invoicing.ui.theme.callForActions
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProductTaxRatesContent(
    taxRates: List<BigDecimal>?,
    currentTaxRate: BigDecimal?,
    onSelectTaxRate: (BigDecimal?) -> Unit,
    isDisplayedInBottomSheet: Boolean = false,
    onClickEditRates: (() -> Unit)? = null
) {
    var chosenOption by remember { mutableStateOf(currentTaxRate) }

    var modifier = Modifier.fillMaxWidth()

    modifier = if (isDisplayedInBottomSheet)
        modifier.then(Modifier
            .fillMaxHeight(0.5f)
            .background(Color.LightGray.copy(alpha = 0.4f))
            .padding(top = 30.dp, end = 60.dp, start = 60.dp))
    else
        modifier.then(Modifier
            .padding(12.dp)
        )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    )
    {
        Column(
            modifier = Modifier
                .background(color = Color.White, shape = RoundedCornerShape(6.dp))
                .fillMaxWidth()
                .padding(
                    top = 8.dp,
                    bottom = 8.dp
                )
        ) {
            // Adds the no tax ("-") choice to the list
            val taxRatesIncludingNoTax = taxRates?.toMutableList()
            taxRatesIncludingNoTax?.add(0, BigDecimal.ZERO)

            taxRatesIncludingNoTax?.forEach { taxRate ->
                val currentChosenOption = chosenOption
                val isCurrentTaxRate = if(currentTaxRate == null) {
                    taxRate == BigDecimal.ZERO
                } else currentChosenOption != null && taxRate.compareTo(currentChosenOption) == 0
                //We use compareTo() because if we use taxRate == chosenOption it compares 20 with 20.0 and returns false

                Row(
                    verticalAlignment = Alignment.Top, // For label to stay on top when multiline text
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 20.dp,
                            end = 20.dp,
                            top = 16.dp,
                            bottom = 16.dp
                        )
                ) {
                    Text(
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    chosenOption = taxRate
                                    onSelectTaxRate(
                                        if (taxRate == BigDecimal.ZERO) {
                                            null
                                        } else taxRate
                                    )
                                }
                            )
                            .weight(1F)
                            .fillMaxWidth(0.4f)
                            .padding(
                                start = 20.dp,
                                end = 8.dp
                            ),
                        text = if (taxRate == BigDecimal.ZERO) {
                            "-"
                        } else {
                            "${taxRate.stripTrailingZeros().toPlainString().replace(".", ",")}%"
                        },
                        style = LocalTextStyle.current
                    )
                    if (isCurrentTaxRate) {
                        Icon(
                            imageVector = Icons.Outlined.Done,
                            contentDescription = "Selected tax rate"
                        )
                    }
                }

                if (taxRate != taxRates.last()) {
                    Separator()
                }
            }
        }

        // "Modifier les taux" link - same style as "Ajouter une adresse"
        if (onClickEditRates != null && !isDisplayedInBottomSheet) {
            Text(
                style = MaterialTheme.typography.callForActions,
                modifier = Modifier
                    .padding(start = 4.dp, top = 4.dp)
                    .clickable(enabled = true) {
                        onClickEditRates()
                    },
                text = AnnotatedString(stringResource(Res.string.tax_rate_edit_rates)),
            )
        }
    }
}

@Composable
fun ProductTaxRatesEditContent(
    taxRates: List<Pair<Long?, BigDecimal>>,
    onUpdateRate: (Int, BigDecimal) -> Unit,
    onAddRate: () -> Unit,
    onDeleteRate: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(
            modifier = Modifier
                .background(color = Color.White, shape = RoundedCornerShape(6.dp))
                .fillMaxWidth()
                .padding(
                    top = 8.dp,
                    bottom = 8.dp
                )
        ) {
            taxRates.forEachIndexed { index, (id, rate) ->
                TaxRateEditRow(
                    rate = rate,
                    onUpdateRate = { newRate -> onUpdateRate(index, newRate) },
                    onDeleteRate = { onDeleteRate(index) },
                    canDelete = taxRates.size > 1
                )

                if (index < taxRates.size - 1) {
                    Separator()
                }
            }
        }

        // "Ajouter un taux" link
        Text(
            style = MaterialTheme.typography.callForActions,
            modifier = Modifier
                .padding(start = 4.dp, top = 4.dp)
                .clickable(enabled = true) {
                    onAddRate()
                },
            text = AnnotatedString(stringResource(Res.string.tax_rate_add_rate)),
        )
    }
}

@Composable
private fun TaxRateEditRow(
    rate: BigDecimal,
    onUpdateRate: (BigDecimal) -> Unit,
    onDeleteRate: () -> Unit,
    canDelete: Boolean
) {
    var isEditing by remember { mutableStateOf(rate == BigDecimal.ZERO) }
    var textValue by remember(rate) {
        mutableStateOf(
            if (rate == BigDecimal.ZERO) ""
            else rate.stripTrailingZeros().toPlainString().replace(".", ",")
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 20.dp,
                end = 8.dp,
                top = 12.dp,
                bottom = 12.dp
            )
    ) {
        if (isEditing) {
            BasicTextField(
                value = textValue,
                onValueChange = { newValue ->
                    // Allow only numbers and comma
                    val filtered = newValue.filter { it.isDigit() || it == ',' }
                    // Only allow one comma
                    val commaCount = filtered.count { it == ',' }
                    if (commaCount <= 1) {
                        textValue = filtered
                        // Update the rate
                        val normalizedValue = filtered.replace(",", ".")
                        if (normalizedValue.isNotEmpty() && normalizedValue != ".") {
                            try {
                                val newRate = BigDecimal.parseString(normalizedValue)
                                onUpdateRate(newRate)
                            } catch (e: Exception) {
                                // Invalid number, ignore
                            }
                        }
                    }
                },
                modifier = Modifier
                    .weight(1F)
                    .padding(start = 20.dp, end = 8.dp),
                textStyle = LocalTextStyle.current,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        innerTextField()
                        Text("%", style = LocalTextStyle.current)
                    }
                }
            )
            IconButton(
                onClick = { isEditing = false },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Done,
                    contentDescription = "Valider",
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Text(
                modifier = Modifier
                    .weight(1F)
                    .padding(start = 20.dp, end = 8.dp),
                text = "${rate.stripTrailingZeros().toPlainString().replace(".", ",")}%",
                style = LocalTextStyle.current
            )
            IconButton(
                onClick = { isEditing = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Modifier",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (canDelete) {
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onDeleteRate,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Supprimer",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Gray
                )
            }
        }
    }
}
