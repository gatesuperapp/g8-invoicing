package com.a4a.g8invoicing.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import com.a4a.g8invoicing.data.ClientOrIssuerState
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.theme.textForDocuments
import com.a4a.g8invoicing.ui.theme.textForDocumentsImportant
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil


data class ProductWithPage(
    var documentProduct: DocumentProductState,
    var page: Int,
)

data class FooterRow(
    var rowDescription: String,
    var page: Int,
)

fun calculateNumberOfPages(
    numberOfItems: Int,
    maxItemsOnFirstPage: Int,
    maxItemsOnOtherPages: Int,
): Int {
    val numberOfPages: Int = if (numberOfItems <= maxItemsOnFirstPage) {
        1
    } else {
        val remainingProducts = numberOfItems - maxItemsOnFirstPage

        val additionalPages = ceil(remainingProducts.toDouble() / maxItemsOnOtherPages).toInt()
        1 + additionalPages
    }

    return numberOfPages
}

fun calculateLimits(
    numberOfPages: Int,
    maxItemsOnFirstPage: Int,
    maxItemsOnOtherPages: Int,
): MutableList<Int> {
    val arrayOfLimits = mutableStateListOf<Int>()
    arrayOfLimits.add(maxItemsOnFirstPage)
    if (numberOfPages > 1) {
        for (i in 1..<(numberOfPages - 1)) {
            arrayOfLimits.add((maxItemsOnFirstPage) + maxItemsOnOtherPages * i)
        }
    }
    return arrayOfLimits
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeliveryNoteBasicTemplate(
    uiState: DeliveryNoteState,
    onClickElement: (ScreenElement) -> Unit,
) {
    var zoom by remember { mutableStateOf(1f) }
    var animatableOffsetX by remember { mutableStateOf(Animatable(0f)) }
    var offsetY by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    var clickEnabled by remember { mutableStateOf(true) } // To disable clicking 2 items at a time

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    var numberOfPages = 1

    var firstCompositionForThisNumberOfItems by remember { mutableStateOf(true) }
    val pagerState = rememberPagerState { numberOfPages }

    val productArray = uiState.documentProducts?.map {
        ProductWithPage(it, 1)
    }
    val footerArray = mutableStateListOf(
        FooterRow(FooterRowName.TOTAL_WITHOUT_TAX.name, numberOfPages)
    )

    val taxRates = uiState.documentProducts?.mapNotNull { it.taxRate?.toInt() }?.distinct()?.sorted()
    taxRates?.forEach {
        footerArray.add(FooterRow("TAXES_" + it.toString(), numberOfPages))
    }
    footerArray.add(FooterRow(FooterRowName.TOTAL_WITH_TAX.name, numberOfPages))

    val arrayOfProductsAndFooterRows: MutableList<Any> =
        productArray?.toMutableList() ?: mutableListOf()
    footerArray.forEach { arrayOfProductsAndFooterRows.add(it) }

    val numberOfItems = arrayOfProductsAndFooterRows.size

    val maxItemsOnFirstPage = 11
    val maxItemsOnOtherPages = 20
    var limitsArray = mutableListOf<Int>()

    numberOfPages = calculateNumberOfPages(
        numberOfItems,
        maxItemsOnFirstPage,
        maxItemsOnOtherPages
    )
    limitsArray = calculateLimits(
        numberOfPages,
        maxItemsOnFirstPage,
        maxItemsOnOtherPages
    )

    if (numberOfPages > 1) {
        for (i in 2..numberOfPages) {
            val itemsToMoveToNextPage =
                arrayOfProductsAndFooterRows.slice(limitsArray[i - 2]..arrayOfProductsAndFooterRows.lastIndex)
            itemsToMoveToNextPage.filterIsInstance<FooterRow>()
                .forEach { it.page = i }
            itemsToMoveToNextPage.filterIsInstance<ProductWithPage>()
                .forEach { it.page = i }
        }
    }

    Column {
        HorizontalPager(
            state = pagerState
        ) { index ->
            Column {
                DeliveryNoteBasicTemplateContent(
                    uiState = uiState,
                    onClickElement = onClickElement,
                    screenWidth = screenWidth,
                    productArray = productArray?.filter { it.page == (index + 1) },
                    footerArray = footerArray.filter { it.page == (index + 1) }.toMutableList(),
                    index = index,
                    numberOfPages = numberOfPages
                )
            }
        }
    }
}


enum class FooterRowName {
    TOTAL_WITHOUT_TAX, TAXES_20, TAXES_10, TAXES_5, TOTAL_WITH_TAX
}

@Composable
fun BuildClientOrIssuerInTemplate(clientOrIssuer: ClientOrIssuerState?) {
    Text(
        modifier = Modifier
            .padding(bottom = 2.dp)
            .wrapContentHeight(),
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.textForDocumentsImportant,
        text = (clientOrIssuer?.firstName?.text ?: "") + " " + (clientOrIssuer?.name?.text ?: "")
    )
    clientOrIssuer?.address1?.let {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier.wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = it.text
        )
    }
    clientOrIssuer?.address2?.let {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier.wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = it.text
        )
    }
    clientOrIssuer?.zipCode?.let {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier.wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = it.text + " " + clientOrIssuer.city?.text
        )
    }
    clientOrIssuer?.phone?.let {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier.wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = it.text
        )
    }
    clientOrIssuer?.email?.let {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 3.dp)
                .wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = it.text
        )
    }
    clientOrIssuer?.companyId1Number?.let {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier.wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = clientOrIssuer.companyId1Label?.text + " : " +  it.text
        )
    }
    clientOrIssuer?.companyId2Number?.let {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier.wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = clientOrIssuer.companyId2Label?.text + " : " + it.text
        )
    }
}


private suspend fun PointerInputScope.customTransformGestures(
    panZoomLock: Boolean = false,
    consume: Boolean = true,
    pass: PointerEventPass = PointerEventPass.Main,
    onGesture: (
        centroid: Offset,
        pan: Offset,
        zoom: Float,
        rotation: Float,
        mainPointer: PointerInputChange,
        changes: List<PointerInputChange>,
    ) -> Unit,
    onGestureStart: (PointerInputChange) -> Unit = {},
    onGestureEnd: (PointerInputChange) -> Unit,
    onDoubleTouch: () -> Unit,
) {
    awaitEachGesture {
        var rotation = 0f
        var zoom = 1f
        var pan = Offset.Zero
        var pastTouchSlop = false
        val touchSlop = viewConfiguration.touchSlop
        var lockedToPanZoom = false

        // Wait for at least one pointer to press down, and set first contact position
        val down: PointerInputChange = awaitFirstDown(
            requireUnconsumed = false,
            pass = pass
        )
        onGestureStart(down)
        var pointer = down
        // Main pointer is the one that is down initially
        var pointerId = down.id

        do {
            val event = awaitPointerEvent(pass = pass)
            val canceled = event.changes.fastAny { it.isConsumed }
            if (!canceled) {
                // Get pointer that is down, if first pointer is up
                // get another and use it if other pointers are also down
                // event.changes.first() doesn't return same order
                val pointerInputChange =
                    event.changes.firstOrNull { it.id == pointerId }
                        ?: event.changes.first()

                // Next time will check same pointer with this id
                pointerId = pointerInputChange.id
                pointer = pointerInputChange

                val zoomChange = event.calculateZoom()
                val rotationChange = event.calculateRotation()
                val panChange = event.calculatePan()

                if (!pastTouchSlop) {
                    zoom *= zoomChange
                    rotation += rotationChange
                    pan += panChange

                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                    val zoomMotion = abs(1 - zoom) * centroidSize
                    val rotationMotion = abs(rotation * PI.toFloat() * centroidSize / 180f)
                    val panMotion = pan.getDistance()

                    if (zoomMotion > touchSlop ||
                        rotationMotion > touchSlop ||
                        panMotion > touchSlop
                    ) {
                        pastTouchSlop = true
                        lockedToPanZoom = panZoomLock && rotationMotion < touchSlop
                    }
                }

                if (pastTouchSlop) {
                    val centroid = event.calculateCentroid(useCurrent = false)
                    val effectiveRotation = if (lockedToPanZoom) 0f else rotationChange
                    if (effectiveRotation != 0f ||
                        zoomChange != 1f ||
                        panChange != Offset.Zero
                    ) {
                        onGesture(
                            centroid,
                            panChange,
                            zoomChange,
                            effectiveRotation,
                            pointer,
                            event.changes
                        )
                    }
                    if (consume) {
                        event.changes.fastForEach {
                            if (it.positionChanged()) {
                                it.consume()
                            }
                        }
                    }
                }

                // Disable clicking 2 items at once
                val pointerCount = event.changes.size
                if (pointerCount >= 2) {
                    onDoubleTouch()
                }
            }
        } while (!canceled && event.changes.fastAny { it.pressed })

        onGestureEnd(pointer)
    }
}

