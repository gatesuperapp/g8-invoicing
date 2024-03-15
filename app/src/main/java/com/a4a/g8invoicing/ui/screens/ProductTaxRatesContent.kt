package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.shared.Separator
import icons.IconDone
import java.math.BigDecimal

@Composable
fun ProductTaxRatesContent(
    taxRates: List<BigDecimal>,
    currentTaxRate: BigDecimal?,
    onSelectTaxRate: (BigDecimal?) -> Unit,
    isDisplayedInBottomSheet: Boolean = false
) {
    var chosenOption by remember { mutableStateOf(currentTaxRate) }

    var modifier = Modifier.fillMaxWidth()

    modifier = if (isDisplayedInBottomSheet)
        modifier.then(Modifier
            .fillMaxHeight(0.7f)
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
            val taxRatesIncludingNoTax = taxRates.toMutableList()
            taxRatesIncludingNoTax.add(0, BigDecimal(0))

            taxRatesIncludingNoTax.forEach { taxRate ->

                val isCurrentTaxRate = if(currentTaxRate == null) {
                    taxRatesIncludingNoTax == BigDecimal(0)
                } else taxRate.compareTo(chosenOption) == 0
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
                                        if (taxRate == BigDecimal(0)) {
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
                        text = if (taxRate == BigDecimal(0)) {
                            "-"
                        } else {
                            "$taxRate%"
                        },
                        style = LocalTextStyle.current
                    )
                    if (isCurrentTaxRate) {
                        Icon(
                            imageVector = IconDone,
                            contentDescription = "Selected tax rate"
                        )
                    }
                }

                if (taxRate != taxRates.last()) {
                    Separator()
                }
            }
        }
    }
}