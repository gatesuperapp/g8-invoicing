package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.DocumentState


@Composable
fun DocumentBasicTemplate(
    uiState: DocumentState,
    onClickElement: (ScreenElement) -> Unit,
    onClickRestOfThePage: () -> Unit,
) {
    BoxWithConstraints {
        val screenWidth = maxWidth
        val productArray = uiState.documentProducts
        /*    val pageNumber = productArray?.last()?.page ?: 1
            val pagerState = rememberPagerState { pageNumber }*/
        val footerArray = mutableStateListOf(PricesRowName.TOTAL_WITHOUT_TAX.name)
        val taxRates =
            uiState.documentProducts?.mapNotNull { it.taxRate?.intValue(false) }?.distinct()?.sorted()
        taxRates?.forEach {
            footerArray.add("TAXES_$it")
        }
        footerArray.add(PricesRowName.TOTAL_WITH_TAX.name)

        Column {
            DocumentBasicTemplateContent(
                document = uiState,
                onClickElement = onClickElement,
                onClickRestOfThePage = onClickRestOfThePage,
                screenWidth = screenWidth,
                productArray = productArray,
                prices = footerArray,
                //numberOfPages = pagerState.pageCount,
            )
        }
    }
}

enum class PricesRowName {
    TOTAL_WITHOUT_TAX, TAXES_20, TAXES_10, TAXES_5, TOTAL_WITH_TAX
}
