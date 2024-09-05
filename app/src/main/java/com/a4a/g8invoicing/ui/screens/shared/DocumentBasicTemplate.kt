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
) {

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val productArray = uiState.documentProducts
    /*    val pageNumber = productArray?.last()?.page ?: 1
        val pagerState = rememberPagerState { pageNumber }*/
    val footerArray = mutableStateListOf(FooterRowName.TOTAL_WITHOUT_TAX.name)
    val taxRates =
        uiState.documentProducts?.mapNotNull { it.taxRate?.toInt() }?.distinct()?.sorted()
    taxRates?.forEach {
        footerArray.add("TAXES_$it")
    }
    footerArray.add(FooterRowName.TOTAL_WITH_TAX.name)


    Column(

    ) {

        DocumentBasicTemplateContent(
            document = uiState,
            onClickElement = onClickElement,
            screenWidth = screenWidth,
            productArray = productArray,
            prices = footerArray,
            //numberOfPages = pagerState.pageCount,
        )

    }
}


enum class FooterRowName {
    TOTAL_WITHOUT_TAX, TAXES_20, TAXES_10, TAXES_5, TOTAL_WITH_TAX
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


