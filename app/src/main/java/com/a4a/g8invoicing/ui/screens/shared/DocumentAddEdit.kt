package com.a4a.g8invoicing.ui.screens.shared

import android.R.attr.scaleX
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.a4a.g8invoicing.ui.navigation.DocumentBottomBar
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.navigation.actionExport
import com.a4a.g8invoicing.ui.navigation.actionItems
import com.a4a.g8invoicing.ui.navigation.actionTextElements
import com.a4a.g8invoicing.ui.screens.ExportPdf
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.theme.ColorLightGreyo
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentAddEdit(
    navController: NavController,
    document: DocumentState,
    clientList: MutableList<ClientOrIssuerState>,
    issuerList: MutableList<ClientOrIssuerState>,
    documentClientUiState: ClientOrIssuerState,
    documentIssuerUiState: ClientOrIssuerState,
    documentProductUiState: DocumentProductState,
    taxRates: List<BigDecimal>,
    products: MutableList<ProductState>,
    onClickBack: () -> Unit,
    onValueChange: (ScreenElement, Any) -> Unit, // OUT : update ui state with user input
    onSelectProduct: (ProductState) -> Unit,
    onClickNewDocumentProduct: () -> Unit,
    onSelectClientOrIssuer: (ClientOrIssuerState) -> Unit,
    onClickEditDocumentProduct: (DocumentProductState) -> Unit,
    onClickNewDocumentClientOrIssuer: (ClientOrIssuerType) -> Unit,
    onClickDocumentClientOrIssuer: (ClientOrIssuerState) -> Unit,
    onClickDeleteDocumentProduct: (Int) -> Unit,
    onClickDeleteDocumentClientOrIssuer: (ClientOrIssuerType) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    bottomFormOnValueChange: (ScreenElement, Any, ClientOrIssuerType?) -> Unit,
    bottomFormPlaceCursor: (ScreenElement, ClientOrIssuerType?) -> Unit,
    onClickDoneForm: (DocumentBottomSheetTypeOfForm) -> Unit,
    onClickCancelForm: () -> Unit,
    onSelectTaxRate: (BigDecimal?) -> Unit,
    showDocumentForm: Boolean,
    onShowDocumentForm: (Boolean) -> Unit,
    onClickDeleteAddress: (ClientOrIssuerType) -> Unit,
    onOrderChange: (List<DocumentProductState>) -> Unit,
) {
    // Know if a category in the bottom bar has been selected
    val sheetVisible = remember { mutableStateOf(false) }

    // We use BottomSheetScaffold to open a bottom sheet modal
    // (We could use ModalBottomSheet but there are issues with overlapping system navigation)
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            skipHiddenState = false
        )
    )
    val bottomSheetType = remember { mutableStateOf(BottomSheetType.ITEMS) }
    val scope = rememberCoroutineScope()

       /*    // Keyboard: if it was open and the user swipes down the bottom sheet:
        // close the keyboard (if we close keyboard before sheet, there is a weird effect)
        val keyboardController = LocalSoftwareKeyboardController.current
        if (!scaffoldState.bottomSheetState.isVisible) {
            keyboardController?.hide()
        }*/
    val keyboardController = LocalSoftwareKeyboardController.current

    // Handling native navigation back action
    BackHandler {
        // We check on bottomSheetState == "Expanded" and not on "bottomSheetState.isVisible"
        // Because of a bug: even when the bottomSheet is hidden, its state is "PartiallyExpanded"
        if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
            hideBottomSheet(scope, scaffoldState)
        } else {
            onClickBack()
        }
    }

    BottomSheetScaffold(
        sheetSwipeEnabled = false,
        sheetDragHandle = null,
        sheetShape = RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp
        ),// Remove rounded corners (must be a better way..)
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            if (sheetVisible.value) {
                if (bottomSheetType.value == BottomSheetType.ELEMENTS) {

                    DocumentBottomSheetTextElements(
                        document = document,
                        onDismissBottomSheet = {
                            hideBottomSheet(scope, scaffoldState)
                        },
                        clients = clientList,
                        issuers = issuerList,
                        documentClientUiState = documentClientUiState,
                        documentIssuerUiState = documentIssuerUiState,
                        taxRates = taxRates,
                        onValueChange = onValueChange,
                        onSelectClientOrIssuer = onSelectClientOrIssuer,
                        onClickNewDocumentClientOrIssuer = onClickNewDocumentClientOrIssuer,
                        onClickEditDocumentClientOrIssuer = onClickDocumentClientOrIssuer,
                        onClickDeleteDocumentClientOrIssuer = onClickDeleteDocumentClientOrIssuer,
                        currentClientId = document.documentClient?.id,
                        currentIssuerId = document.documentIssuer?.id,
                        placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                        bottomFormOnValueChange = bottomFormOnValueChange,
                        bottomFormPlaceCursor = bottomFormPlaceCursor,
                        onClickDoneForm = onClickDoneForm,
                        onClickCancelForm = onClickCancelForm,
                        onSelectTaxRate = onSelectTaxRate,
                        localFocusManager = LocalFocusManager.current,
                        showDocumentForm = showDocumentForm,
                        onShowDocumentForm = onShowDocumentForm,
                        onClickDeleteAddress = onClickDeleteAddress
                    )
                } else {
                    DocumentBottomSheetProducts(
                        document = document,
                        onDismissBottomSheet = {
                            hideBottomSheet(scope, scaffoldState)
                        },
                        documentProductUiState = documentProductUiState,
                        products = products,
                        taxRates = taxRates,
                        onClickProduct = onSelectProduct,
                        onClickNewProduct = onClickNewDocumentProduct,
                        onClickDocumentProduct = onClickEditDocumentProduct,
                        onClickDeleteDocumentProduct = onClickDeleteDocumentProduct,
                        bottomFormOnValueChange = bottomFormOnValueChange,
                        bottomFormPlaceCursor = bottomFormPlaceCursor,
                        onClickDoneForm = onClickDoneForm,
                        onClickCancelForm = onClickCancelForm,
                        onSelectTaxRate = onSelectTaxRate,
                        showDocumentForm = showDocumentForm,
                        onShowDocumentForm = onShowDocumentForm,
                        onOrderChange = onOrderChange
                    )
                }
            }
        },
        sheetShadowElevation = 30.dp
    )
    {
        val context = LocalContext.current
        var showPopup by rememberSaveable {
            mutableStateOf(false)
        }


        // As it's not possible to have a bottom bar inside a BottomSheetScaffold,
        // as a temporary solution, we use Scaffold inside BottomSheetScaffold
        Scaffold(
            modifier = Modifier.background(ColorLightGreyo),
            topBar = {
                DeliveryNoteAddEditTopBar(
                    navController = navController,
                    onClickBack = onClickBack,
                    onClickExport = {
                        showPopup = true
                    }
                )
            },
            bottomBar = {
                DocumentAddEditBottomBar(
                    onClickElements = {
                        sheetVisible.value = true
                        bottomSheetType.value = BottomSheetType.ELEMENTS
                        expandBottomSheet(scope, scaffoldState)
                    },
                    onClickItems = {
                        sheetVisible.value = true
                        bottomSheetType.value = BottomSheetType.ITEMS
                        expandBottomSheet(scope, scaffoldState)
                    },
                    onClickStyle = {
                        bottomSheetType.value = BottomSheetType.STYLE
                        Toast.makeText(
                            context,
                            "Bientôt disponible :)",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        ) { innerPadding ->
            if (showPopup) {
                ExportPopup(document, onDismissRequest = { showPopup = false })
            }

            var zoom by remember { mutableFloatStateOf(1f) }
            var animatableOffsetX by remember { mutableStateOf(Animatable(0f)) }
            var animatableOffsetY by remember { mutableStateOf(Animatable(0f)) }
            var offsetY by remember { mutableFloatStateOf(0f) }
            val coroutineScope = rememberCoroutineScope()
            var clickEnabled by remember { mutableStateOf(true) } // To disable clicking 2 items at a time
            var newOffsetY by remember { mutableFloatStateOf(0f) }

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .background(ColorLightGreyo)
                    .fillMaxSize()
                    .padding(
                        innerPadding
                    )
                    .pointerInput(Unit) {
                        customTransformGestures(
                            pass = PointerEventPass.Initial,
                            onDoubleTouch = { // Disable clicking 2 items at the same time
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
                                newOffsetY = animatableOffsetY.value + pan.y.times(zoom)

                                val maxX = (size.width * (zoom - 1) / 2f)
                                val maxY = (size.height * (zoom - 1) / 2f)
                                val minY = -(size.height * (zoom - 1))

                                if (zoom > 1f) {
                                    newOffsetX = newOffsetX.coerceIn(-maxX, maxX)
                                    // coerceIn limits dragging in bounds
                                    newOffsetY = newOffsetY.coerceIn(minY, maxY)
                                }

                                animatableOffsetX = Animatable(newOffsetX)
                                animatableOffsetY = Animatable(newOffsetY)
                                //offsetY = newOffsetY

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

                                if (zoom == 1f && animatableOffsetY.value > 0f) { // dragging above the document
                                    coroutineScope.launch {
                                        animatableOffsetY.animateTo(
                                            0f, animationSpec = spring(
                                                //dampingRatio = 0.4f,
                                                stiffness = Spring.StiffnessLow
                                            )
                                        )
                                    }
                                }

                                if (zoom == 1f && -animatableOffsetY.value > size.height.toFloat()) { // dragging below the document
                                    coroutineScope.launch {
                                        animatableOffsetY.animateTo(
                                            -size.height.toFloat() + size.height.toFloat() / 2f,
                                            animationSpec = spring(
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
                        translationY = animatableOffsetY.value
                        /*    if (zoom > 1f) { // Y translation disabled when no zoom
                                translationY = offsetY
                            }*/
                        scaleX = zoom
                        scaleY = zoom
                    }

            ) {
                DocumentBasicTemplate(
                    uiState = document,
                    onClickElement = {
                        if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                            hideBottomSheet(scope, scaffoldState)
                        } else {
                            if (it == ScreenElement.DOCUMENT_HEADER ||
                                it == ScreenElement.DOCUMENT_NUMBER ||
                                it == ScreenElement.DOCUMENT_DATE ||
                                it == ScreenElement.DOCUMENT_ISSUER ||
                                it == ScreenElement.DOCUMENT_CLIENT ||
                                it == ScreenElement.DOCUMENT_FOOTER ||
                                it == ScreenElement.DOCUMENT_REFERENCE
                            ) {
                                bottomSheetType.value = BottomSheetType.ELEMENTS
                            } else {
                                bottomSheetType.value = BottomSheetType.ITEMS
                            }
                            expandBottomSheet(scope, scaffoldState)
                            /*                        when(it) {
                                                        ScreenElement.DOCUMENT_NUMBER ->
                                                         selectedItem = ScreenElement.DOCUMENT_ORDER_NUMBER
                                                        ScreenElement.DOCUMENT_DATE ->
                                                        ScreenElement.DOCUMENT_ISSUER ->
                                                        ScreenElement.DOCUMENT_CLIENT ->
                                                        ScreenElement.DOCUMENT_ORDER_NUMBER ->
                                                        ScreenElement.DOCUMENT_PRODUCTS ->*/
                        }
                    },
                    onClickRestOfThePage = {
                        if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                            hideBottomSheet(scope, scaffoldState)
                        }
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun expandBottomSheet(scope: CoroutineScope, scaffoldState: BottomSheetScaffoldState) {
    scope.launch { scaffoldState.bottomSheetState.expand() }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
private fun hideBottomSheet(
    scope: CoroutineScope,
    scaffoldState: BottomSheetScaffoldState,
  //  keyboardController: SoftwareKeyboardController?,
) {
    scope.launch {
        scaffoldState.bottomSheetState.hide()
     //   keyboardController?.hide()
    }
}

@Composable
private fun DeliveryNoteAddEditTopBar(
    navController: NavController,
    onClickBack: () -> Unit,
    onClickExport: () -> Unit,
) {
    TopBar(
        title = null,
        actionExport(
            onClick = onClickExport
        ),
        navController = navController,
        onClickBackArrow = onClickBack,
        isCancelCtaDisplayed = false
    )
}

@Composable
private fun DocumentAddEditBottomBar(
    onClickElements: () -> Unit,
    onClickItems: () -> Unit,
    onClickStyle: () -> Unit,
    onClickSavePayment: () -> Unit = {},
) {
    DocumentBottomBar(
        actions = arrayOf(
            actionTextElements(onClickElements),
            actionItems(onClickItems),
            //actionStyle(onClickStyle),
            // actionSavePayment(onClickSavePayment)
        )
    )
}

@Composable
fun ExportPopup(
    deliveryNote: DocumentState,
    onDismissRequest: () -> Unit,
) {
    // full screen background
    Dialog(
        onDismissRequest = {},
        DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10F),
            contentAlignment = Alignment.Center
        ) {
            ExportPdf(deliveryNote, onDismissRequest)
        }
    }
}

enum class BottomSheetType {
    ELEMENTS, ITEMS, IMAGES, STYLE
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

fun getDateFormatter(pattern: String = "dd/MM/yyyy HH:mm:ss"): SimpleDateFormat {
    val formatter = SimpleDateFormat(pattern, Locale.ROOT)
    val calendar: Calendar = Calendar.getInstance()
    formatter.timeZone = calendar.timeZone
    return formatter
}


