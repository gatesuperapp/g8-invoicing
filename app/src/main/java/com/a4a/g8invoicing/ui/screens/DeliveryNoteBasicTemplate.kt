package com.a4a.g8invoicing.ui.screens

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
import androidx.compose.ui.unit.sp
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
    var rowDescription: FooterRowName,
    var page: Int,
)

fun calculateNumberOfPages(
    numberOfProducts: Int,
    maxProductsOnFirstPage: Int,
    maxProductsOnOtherPages: Int,
): Int {
    val numberOfPages: Int = if (numberOfProducts <= maxProductsOnFirstPage) {
        1
    } else {
        val remainingProducts = numberOfProducts - maxProductsOnFirstPage

        val additionalPages = ceil(remainingProducts.toDouble() / maxProductsOnOtherPages).toInt()
        1 + additionalPages
    }

    return numberOfPages
}

fun calculateLimits(
    numberOfProducts: Int,
    maxProductsOnFirstPage: Int,
    maxProductsOnOtherPages: Int,
): MutableList<Int> {
    val arrayOfLimits = mutableStateListOf<Int>()

    if (numberOfProducts >= maxProductsOnFirstPage) {
        val remainingProducts = numberOfProducts - maxProductsOnFirstPage
        arrayOfLimits.add(maxProductsOnFirstPage + 1)

        val additionalPages = ceil(remainingProducts.toDouble() / maxProductsOnOtherPages).toInt()
        if(additionalPages != 0) {
            for (i in 1..<additionalPages) {
                arrayOfLimits.add((maxProductsOnFirstPage + 1) + maxProductsOnOtherPages * i)
            }
        }

        println("arrayOfLimits" + arrayOfLimits)
    }

    return arrayOfLimits
}

fun checkIfIsALimitNumberOfProducts(x: Int): Boolean { // 10-11 (on first page) then 29-30 (page 2), 48-49 (p3), 67-68...
    return x == 10 || (x - 1) == 10 || x % 19 == 10 || (x - 1) % 19 == 10
}

fun checkIfIsTheFirstLimitNumber(x: Int): Boolean { // 10 then 29, 48, 67...
    return x == 10 || x % 19 == 10
}

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

    var numberOfPages by remember { mutableStateOf(1) }
    var firstCompositionForThisNumberOfProducts by remember { mutableStateOf(true) }
    val pagerState = rememberPagerState { numberOfPages }

    val productArray = uiState.documentProducts?.map {
        ProductWithPage(it, 1)
    }

    val footerArray = mutableStateListOf(
        FooterRow(FooterRowName.TOTAL_WITHOUT_TAX, numberOfPages),
        FooterRow(FooterRowName.TAXES, numberOfPages),
        FooterRow(FooterRowName.TOTAL_WITH_TAX, numberOfPages),
    )

    Column {
        Text(
            text = " nb pdts = " + uiState.documentProducts?.size,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            // maxLines = 1,
            //overflow = TextOverflow.Ellipsis
        )
        HorizontalPager(
            state = pagerState
        ) { index ->
            Column {
                val numberOfProducts = uiState.documentProducts?.size ?: 0
                val maxProductsOnFirstPage = 10
                val maxProductsOnOtherPages = 19
                var limitsArray = mutableListOf<Int>()

                if (firstCompositionForThisNumberOfProducts) {
                    numberOfPages = calculateNumberOfPages(
                        numberOfProducts,
                        maxProductsOnFirstPage,
                        maxProductsOnOtherPages
                    )
                    println("limitsArray" + limitsArray.toList())
                }

                limitsArray = calculateLimits(
                    numberOfProducts,
                    maxProductsOnFirstPage,
                    maxProductsOnOtherPages
                )

                if (checkIfIsALimitNumberOfProducts(numberOfProducts)) {
                    for (i in 2..numberOfPages) {
                        productArray?.let { productArray ->
                            val productsToMoveToNextPage =
                                productArray.slice(limitsArray[i - 2]..productArray.lastIndex)
                            productsToMoveToNextPage.forEach { it.page = i }
                        }
                    }

                    if (checkIfIsTheFirstLimitNumber(numberOfProducts) && firstCompositionForThisNumberOfProducts) {
                        numberOfPages += 1
                    }
                    println("IS A LIMIT & limits =" + limitsArray.toList())

                    val footerRowsToMove =
                        footerArray.slice((footerArray.lastIndex - (numberOfProducts - (limitsArray.last() - 1)))..footerArray.lastIndex)
                    footerRowsToMove.forEach { it.page = numberOfPages }
                    val otherFooterRows =
                        footerArray.slice((0..<footerArray.lastIndex - (numberOfProducts - (limitsArray.last() - 1))))
                    otherFooterRows.forEach { it.page = numberOfPages - 1 }

                    firstCompositionForThisNumberOfProducts = false

                } else if (limitsArray.isNotEmpty()) {
                    println("IS NOT A LIMIT")
                    firstCompositionForThisNumberOfProducts = true
                    footerArray.forEach { it.page = numberOfPages }
                    for (i in 2..numberOfPages) {
                        productArray?.let { productArray ->
                            val productsToMoveToNextPage =
                                productArray.slice(limitsArray[i - 2]..productArray.lastIndex)
                            productsToMoveToNextPage.forEach { it.page = i }
                        }
                    }
                }

                /* if (numberOfProducts in 10..11) {
                     numberOfPages = 2
                     val footerRowsToMove =
                         footerArray.slice(footerArray.lastIndex - (numberOfProducts - 10)..footerArray.lastIndex)
                     footerRowsToMove.forEach { it.page = 2 }
                 } else if (numberOfProducts in 12..28) {
                     numberOfPages = 2
                     footerArray.forEach { it.page = 2 }
                     productArray?.let { productArray ->
                         val productsToMoveToNextPage =
                             productArray.slice(12..productArray.lastIndex)
                         productsToMoveToNextPage.forEach { it.page = 2 }
                     }
                 } else if (numberOfProducts in 29..30) {
                     productArray?.let { productArray ->
                         val productsToMoveToNextPage =
                             productArray.slice(12..productArray.lastIndex)
                         productsToMoveToNextPage.forEach { it.page = 2 }
                     }
                     footerArray.forEach { it.page = 2 }
                     numberOfPages = 3
                     val footerRowsToMove =
                         footerArray.slice(footerArray.lastIndex - (numberOfProducts - 29)..footerArray.lastIndex)
                     footerRowsToMove.forEach { it.page = 3 }

                 } else if (numberOfProducts in 31..47) {
                     numberOfPages = 3
                     footerArray.forEach { it.page = 3 }
                     productArray?.let { productArray ->
                         val productsToMoveToPage2 =
                             productArray.slice(12..productArray.lastIndex)
                         productsToMoveToPage2.forEach { it.page = 2 }
                         val productsToMoveToPage3 =
                             productArray.slice(31..productArray.lastIndex)
                         productsToMoveToPage3.forEach { it.page = 3 }
                     }
                 } else if (numberOfProducts in 48..49) {
                     productArray?.let { productArray ->
                         val productsToMoveToPage2 =
                             productArray.slice(12..productArray.lastIndex)
                         productsToMoveToPage2.forEach { it.page = 2 }
                         val productsToMoveToPage3 =
                             productArray.slice(31..productArray.lastIndex)
                         productsToMoveToPage3.forEach { it.page = 3 }
                     }
                     footerArray.forEach { it.page = 3 }
                     numberOfPages = 4
                     val footerRowsToMove =
                         footerArray.slice(footerArray.lastIndex - (numberOfProducts - 48)..footerArray.lastIndex)
                     footerRowsToMove.forEach { it.page = 4 }

                 } else if (numberOfProducts in 50..66) {
                     numberOfPages = 4
                     footerArray.forEach { it.page = 4 }
                     productArray?.let { productArray ->
                         val productsToMoveToPage2 =
                             productArray.slice(12..productArray.lastIndex)
                         productsToMoveToPage2.forEach { it.page = 2 }
                         val productsToMoveToPage3 =
                             productArray.slice(31..productArray.lastIndex)
                         productsToMoveToPage3.forEach { it.page = 3 }
                         val productsToMoveToPage4 =
                             productArray.slice(50..productArray.lastIndex)
                         productsToMoveToPage4.forEach { it.page = 4 }
                     }
                 }*/

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
    TOTAL_WITHOUT_TAX, TAXES, TOTAL_WITH_TAX
}

@Composable
fun BuildClientOrIssuerInTemplate(clientOrIssuer: ClientOrIssuerState?) {
    Text(
        modifier = Modifier
            .padding(bottom = 2.dp)
            .wrapContentHeight(),
        textAlign = TextAlign.Center,
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
    clientOrIssuer?.companyId1Label?.let {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier.wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = it.text + " : " + clientOrIssuer.companyId1Number?.text
        )
    }
    clientOrIssuer?.companyId2Label?.let {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier.wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = it.text + " : " + clientOrIssuer.companyId2Number?.text
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

