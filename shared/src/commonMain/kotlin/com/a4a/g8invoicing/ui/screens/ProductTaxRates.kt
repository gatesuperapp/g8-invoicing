package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.document_modal_product_save
import com.a4a.g8invoicing.shared.resources.tax_rate_screen_title
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.theme.ColorBackgroundGrey
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import org.jetbrains.compose.resources.stringResource


@Composable
fun ProductTaxRates(
    navController: NavController,
    taxRates: List<BigDecimal>,
    taxRatesWithIds: List<Pair<Long, BigDecimal>>,
    currentTaxRate: BigDecimal?,
    onSelectTaxRate: (BigDecimal?) -> Unit,
    onClickBack: () -> Unit,
    onSaveTaxRates: (List<Pair<Long?, BigDecimal>>) -> Unit,
) {
    val scrollState = rememberScrollState()
    var isEditMode by remember { mutableStateOf(false) }

    // Local state for editing - copy of taxRatesWithIds that can be modified
    var editingRates by remember(taxRatesWithIds) {
        mutableStateOf(taxRatesWithIds.map { Pair(it.first as Long?, it.second) })
    }

    Scaffold(
        topBar = {
            if (isEditMode) {
                TaxRatesEditTopBar(
                    navController = navController,
                    onClickCancel = {
                        // Reset editing rates and exit edit mode
                        editingRates = taxRatesWithIds.map { Pair(it.first as Long?, it.second) }
                        isEditMode = false
                    },
                    onClickValidate = {
                        onSaveTaxRates(editingRates)
                        isEditMode = false
                    }
                )
            } else {
                TaxRatesTopBar(
                    navController = navController,
                    onClickBackArrow = onClickBack
                )
            }
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
            if (isEditMode) {
                ProductTaxRatesEditContent(
                    taxRates = editingRates,
                    onUpdateRate = { index, newRate ->
                        editingRates = editingRates.toMutableList().apply {
                            this[index] = Pair(this[index].first, newRate)
                        }
                    },
                    onAddRate = {
                        editingRates = editingRates + Pair(null, BigDecimal.ZERO)
                    },
                    onDeleteRate = { index ->
                        editingRates = editingRates.toMutableList().apply {
                            removeAt(index)
                        }
                    }
                )
            } else {
                ProductTaxRatesContent(
                    taxRates = taxRates,
                    currentTaxRate = currentTaxRate,
                    onSelectTaxRate = onSelectTaxRate,
                    onClickEditRates = { isEditMode = true }
                )
            }

            // Bottom spacer to ensure content can scroll above keyboard/bottom of screen
            Spacer(modifier = Modifier.height(200.dp))
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

@Composable
private fun TaxRatesEditTopBar(
    navController: NavController,
    onClickCancel: () -> Unit,
    onClickValidate: () -> Unit,
) {
    TopBar(
        title = "",
        navController = navController,
        onClickBackArrow = onClickCancel,
        isCancelCtaDisplayed = true,
        ctaText = stringResource(Res.string.document_modal_product_save),
        ctaTextDisabled = true,
        onClickCtaValidate = onClickValidate
    )
}
