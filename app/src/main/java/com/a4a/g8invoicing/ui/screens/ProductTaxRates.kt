package com.a4a.g8invoicing.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
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
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.shared.Separator
import icons.IconDone
import java.math.BigDecimal


@Composable
fun ProductTaxRates(
    navController: NavController,
    taxRates: List<BigDecimal>,
    currentTaxRate: BigDecimal,
    onSelectTaxRate: (BigDecimal?) -> Unit,
    onClickBack: () -> Unit,
) {
    val scrollState = rememberScrollState()
    var chosenOption by remember { mutableStateOf(currentTaxRate) }

    // When going back with system navigation, we save the state (same as with back arrow).
    // It's because we use nested navigation
    BackHandler {
        onClickBack()
    }

    Scaffold(
        topBar = {
            TaxRatesTopBar(
                navController = navController,
                onClickBackArrow = onClickBack
            )
        }
    ) { it

        Column(
            modifier = Modifier
                .background(Color.LightGray.copy(alpha = 0.4f))
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .padding(
                    top = 80.dp,
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp),
                //.fillMaxHeight(0.9f),
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
                    // Add the "no Tax" value to the list
                    val taxRatesIncludingNoTax = taxRates.toMutableList()
                    taxRatesIncludingNoTax.add(0, BigDecimal(0))

                    taxRatesIncludingNoTax.forEach { taxRate ->

                        val isCurrentTaxRate = taxRate == chosenOption

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
                            /* //TODO allow to delete taxrates when edit mode on
                               else {
                                     Icon(
                                         imageVector = IconCancel,
                                         contentDescription = "Selected tax rate",
                                         tint = ColorLightGrayGreen
                                     )
                                 }*/
                        }

                        if (taxRate != taxRates.last()) {
                            Separator()
                        }
                    }
                }

                /*          //TODO add this text to trigger edition
                  Text(
                                    modifier = Modifier
                                        .clickable(
                                            onClick = { isEditMode = true }
                                        )
                                        .align(Alignment.End)
                                        .fillMaxWidth(0.4f)
                                        .padding(

                                        ),
                                    text = stringResource(id = R.string.tax_rate_change_rates) ,
                                )*/

            }
        }
    }
}


@Composable
private fun TaxRatesTopBar(
    navController: NavController,
    onClickBackArrow: () -> Unit,
) {
    TopBar(
        title = R.string.tax_rate_screen_title,
        navController = navController,
        onClickBackArrow = onClickBackArrow
    )
}

