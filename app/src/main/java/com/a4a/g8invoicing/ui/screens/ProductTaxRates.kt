package com.a4a.g8invoicing.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.theme.ColorBackgroundGrey
import java.math.BigDecimal


@Composable
fun ProductTaxRates(
    navController: NavController,
    taxRates: List<BigDecimal>,
    currentTaxRate: BigDecimal?,
    onSelectTaxRate: (BigDecimal?) -> Unit,
    onClickBack: () -> Unit,
) {
    val scrollState = rememberScrollState()

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
    ) {
        it
        Column(
            modifier = Modifier
                .background(ColorBackgroundGrey)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .padding(
                    top = 80.dp,
                )
        ) {
            ProductTaxRatesContent(taxRates, currentTaxRate, onSelectTaxRate)
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

