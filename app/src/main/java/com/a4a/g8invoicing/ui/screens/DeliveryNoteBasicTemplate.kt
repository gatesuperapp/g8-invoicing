package com.a4a.g8invoicing.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import com.a4a.g8invoicing.data.ClientOrIssuerEditable
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.theme.textForDocuments
import com.a4a.g8invoicing.ui.theme.textForDocumentsImportant
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs


@Composable
fun DeliveryNoteBasicTemplate(
    uiState: DeliveryNoteState,
    onClickDeliveryNoteNumber: () -> Unit,
    onClickDate: () -> Unit,
    onClickIssuer: () -> Unit,
    onClickClient: () -> Unit,
    onClickOrderNumber: () -> Unit,
    onClickDocumentProducts: () -> Unit,
    selectedItem: ScreenElement?,
) {
    var zoom by remember { mutableStateOf(1f) }
    var animatableOffsetX by remember { mutableStateOf(Animatable(0f)) }
    var offsetY by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    var clickEnabled by remember { mutableStateOf(true) } // To disable clicking 2 items at a time


    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f / 1.414f)
            .pointerInput(Unit) {
                customTransformGestures(
                    pass = PointerEventPass.Initial,
                    onDoubleTouch = { // Disable clicking 2 items in 1 time
                        clickEnabled = false
                    },
                    onGesture = {
                            centroid,
                            pan,
                            gestureZoom,
                            _,
                            pointerInput: PointerInputChange,
                            changes: List<PointerInputChange>,
                        ->

                        zoom = (zoom * gestureZoom).coerceIn(
                            1f,
                            3f
                        )  // Zoom limits: min 100%, max 200%

                        var newOffsetX = animatableOffsetX.value + pan.x.times(zoom)
                        var newOffsetY = offsetY + pan.y.times(zoom)

                        val maxX = (size.width * (zoom - 1) / 2f)
                        val maxY = (size.height * (zoom - 1) / 2f)

                        if (zoom > 1f) {
                            newOffsetX = newOffsetX.coerceIn(
                                -maxX,
                                maxX
                            ) // coercein will limit drag in bounds
                            newOffsetY = newOffsetY.coerceIn(-maxY, maxY)
                        }

                        animatableOffsetX = Animatable(newOffsetX)
                        offsetY = newOffsetY

                        // 🔥Consume touch when multiple fingers down
                        // This prevents click and long click if your finger touches a
                        // button while pinch gesture is being invoked
                        val size = changes.size
                        if (size > 1) {
                            changes.forEach { it.consume() }
                        }
                    },
                    onGestureEnd = {
                        // When no zoom only, do an animation to bring
                        // back to center when dragged along X axis
                        if (zoom == 1f) {
                            coroutineScope.launch {
                                animatableOffsetX.animateTo(
                                    0f, animationSpec = spring(
                                        //dampingRatio = 0.4f,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                        }
                    }
                )
            }
            .graphicsLayer {
                translationX = animatableOffsetX.value
                if (zoom > 1f) { // Y translation disabled when no zoom
                    translationY = offsetY
                }
                scaleX = zoom
                scaleY = zoom
            }

    ) {
        DeliveryNoteBasicTemplateContent(
            uiState,
            onClickDeliveryNoteNumber,
            onClickDate,
            onClickIssuer,
            onClickClient,
            onClickOrderNumber,
            onClickDocumentProducts,
            selectedItem
        )
    }
}

@Composable
fun BuildClientOrIssuerInTemplate(clientOrIssuer: ClientOrIssuerEditable) {
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
            modifier = Modifier.padding(bottom = 3.dp).wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = it.text
        )
    }
    clientOrIssuer.companyData?.forEach() {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier.wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = "N° " + it.label + " : " + it.number
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

