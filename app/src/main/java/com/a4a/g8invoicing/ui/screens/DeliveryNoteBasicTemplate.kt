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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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


data class ProductListWithPage(
    var documentProduct: DocumentProductState,
    var page: Int,
)

data class FooterRows(
    var rowNumber: Int,
    var page: Int,
)

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
    var maxPageForNewProduct by remember { mutableStateOf(1) }
    val pagerState = rememberPagerState { numberOfPages }

    val productArray = remember {
        mutableStateListOf(
            ProductListWithPage(DocumentProductState(), 1)
        )
    }
    productArray.clear()
    uiState.documentProducts?.forEach {
        productArray.add(ProductListWithPage(it, 1))
    }

    val footerArray = remember {
        mutableStateListOf(
            FooterRows(1, 1),
            FooterRows(2, 1),
            FooterRows(3, 1),
        )
    }

    Column {
        HorizontalPager(
            state = pagerState
        ) { index ->

            Column {
                Text("page n°" + (index + 1) + "/" + numberOfPages)
                Text("maxPageForNewProduct" + maxPageForNewProduct)

                DeliveryNoteBasicTemplateContent(
                    uiState = uiState,
                    onClickElement = onClickElement,
                    screenWidth = screenWidth,
                    productArray = productArray.filter { it.page == (index + 1) }.toMutableList(),
                    footerArray = footerArray.filter { it.page == (index + 1) }.toMutableList(),
                    isFirstPage = (index == 0),
                    onPageOverflow = {
                        val lastProductPage = productArray.last().page
                        if( footerArray.first().page != lastProductPage) {
                            productArray.last().page = (index + 1) + 1
                            maxPageForNewProduct += 1
                        } else {
                            val footerRowToMove = footerArray.last { it.page == lastProductPage }
                            footerRowToMove.page = (index + 1) + 1
                           // if(footerArray.map { it.page }.distinct().isEmpty()) {
                                numberOfPages += 1
                         //   }
                        }
                    }
                )
            }
        }
    }
}


@Composable
fun BuildClientOrIssuerInTemplate(clientOrIssuer: ClientOrIssuerState) {
    Text(
        modifier = Modifier
            .padding(bottom = 2.dp)
            .wrapContentHeight(),
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.textForDocumentsImportant,
        text = (clientOrIssuer.firstName?.text ?: "") + " " + clientOrIssuer.name.text
    )
    clientOrIssuer.address1?.let {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier.wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = it.text
        )
    }
    clientOrIssuer.address2?.let {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier.wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = it.text
        )
    }
    clientOrIssuer.zipCode?.let {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier.wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = it.text + " " + clientOrIssuer.city?.text
        )
    }
    clientOrIssuer.phone?.let {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier.wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = it.text
        )
    }
    clientOrIssuer.email?.let {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 3.dp)
                .wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = it.text
        )
    }
    clientOrIssuer.companyId1Label?.let {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier.wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = it.text + " : " + clientOrIssuer.companyId1Number?.text
        )
    }
    clientOrIssuer.companyId2Label?.let {
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

