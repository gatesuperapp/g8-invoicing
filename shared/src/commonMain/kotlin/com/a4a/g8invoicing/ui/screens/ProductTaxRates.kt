package com.a4a.g8invoicing.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.tax_rate_screen_title
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.theme.ColorBackgroundGrey
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import org.jetbrains.compose.resources.stringResource


@Composable
fun ProductTaxRates(
    navController: NavController,
    taxRates: List<BigDecimal>,
    currentTaxRate: BigDecimal?,
    onSelectTaxRate: (BigDecimal?) -> Unit,
    onClickBack: () -> Unit,
) {
    val scrollState = rememberScrollState()

    // Note: BackHandler is Android-specific. The caller (NavGraph) should handle back navigation.

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
                    top = 110.dp,
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
        title = stringResource(Res.string.tax_rate_screen_title),
        navController = navController,
        onClickBackArrow = onClickBackArrow,
        isCancelCtaDisplayed = false
    )
}
