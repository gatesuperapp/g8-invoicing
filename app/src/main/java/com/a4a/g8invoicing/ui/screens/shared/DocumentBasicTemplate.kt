package com.a4a.g8invoicing.ui.screens.shared

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.DocumentState
import kotlin.math.PI
import kotlin.math.abs


@SuppressLint("UnrememberedMutableState")
@Composable
fun DocumentBasicTemplate(
    uiState: DocumentState,
    onClickElement: (ScreenElement) -> Unit,
    onClickRestOfThePage: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
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

enum class PricesRowName {
    TOTAL_WITHOUT_TAX, TAXES_20, TAXES_10, TAXES_5, TOTAL_WITH_TAX
}

