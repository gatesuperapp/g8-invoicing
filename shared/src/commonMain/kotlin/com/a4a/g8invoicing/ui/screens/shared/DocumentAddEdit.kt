package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
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
import com.a4a.g8invoicing.ui.shared.PlatformBackHandler
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.textBodySmall
import com.a4a.g8invoicing.ui.theme.textScreenTitle
import com.a4a.g8invoicing.data.auth.ActivatedModulesRepository
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.models.PaymentMeans
import com.a4a.g8invoicing.facturx.CiiPreflightValidator
import com.a4a.g8invoicing.facturx.CiiValidationIssue
import com.a4a.g8invoicing.facturx.CiiXmlBuilder
import com.a4a.g8invoicing.facturx.buildBankInfoText
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.cii_validation_client_address
import com.a4a.g8invoicing.shared.resources.cii_validation_client_email
import com.a4a.g8invoicing.shared.resources.cii_validation_client_missing
import com.a4a.g8invoicing.shared.resources.cii_validation_client_name
import com.a4a.g8invoicing.shared.resources.cii_validation_client_siren
import com.a4a.g8invoicing.shared.resources.cii_validation_client_siren_format
import com.a4a.g8invoicing.shared.resources.cii_validation_client_type
import com.a4a.g8invoicing.shared.resources.cii_validation_confirm
import com.a4a.g8invoicing.shared.resources.cii_validation_due_date
import com.a4a.g8invoicing.shared.resources.cii_validation_vat_exemption_text
import com.a4a.g8invoicing.shared.resources.cii_validation_intro
import com.a4a.g8invoicing.shared.resources.cii_validation_issuer_address
import com.a4a.g8invoicing.shared.resources.cii_validation_issuer_missing
import com.a4a.g8invoicing.shared.resources.cii_validation_issuer_name
import com.a4a.g8invoicing.shared.resources.cii_validation_issuer_siren
import com.a4a.g8invoicing.shared.resources.cii_validation_issuer_siren_format
import com.a4a.g8invoicing.shared.resources.cii_validation_issuer_vat
import com.a4a.g8invoicing.shared.resources.cii_validation_line_name
import com.a4a.g8invoicing.shared.resources.cii_validation_line_price
import com.a4a.g8invoicing.shared.resources.cii_validation_line_tax_rate
import com.a4a.g8invoicing.shared.resources.cii_validation_line_tax_rate_invalid
import com.a4a.g8invoicing.shared.resources.cii_validation_products_empty
import com.a4a.g8invoicing.shared.resources.cii_validation_title
import com.a4a.g8invoicing.shared.resources.export_chooser_cii
import com.a4a.g8invoicing.shared.resources.export_chooser_description
import com.a4a.g8invoicing.shared.resources.export_chooser_facturx
import com.a4a.g8invoicing.shared.resources.export_chooser_pdf
import com.a4a.g8invoicing.shared.resources.export_chooser_title
import com.a4a.g8invoicing.shared.resources.export_vat_exempt_conflict_message
import com.a4a.g8invoicing.shared.resources.export_vat_exempt_conflict_title
import com.a4a.g8invoicing.shared.resources.feature_coming_soon
import com.a4a.g8invoicing.shared.resources.issuer_bank_identifier_generic
import com.a4a.g8invoicing.shared.resources.issuer_bank_identifier_iban
import com.a4a.g8invoicing.shared.resources.ok
import com.a4a.g8invoicing.ui.screens.ExportCiiPlatform
import com.a4a.g8invoicing.ui.screens.ExportPdfPlatform
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
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
    onSelectProduct: (ProductState, Int?) -> Unit, // ProductState + clientId for pricing
    onClickNewDocumentProduct: () -> Unit,
    onSelectClientOrIssuer: (ClientOrIssuerState) -> Unit,
    onClickEditDocumentProduct: (DocumentProductState) -> Unit,
    onClickNewDocumentClientOrIssuer: (ClientOrIssuerType) -> Unit,
    onClickDocumentClientOrIssuer: (ClientOrIssuerState, openFormOnCompletion: Boolean) -> Unit,
    onClickDeleteDocumentProduct: (Int) -> Unit,
    onClickDeleteDocumentClientOrIssuer: (ClientOrIssuerType) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    bottomFormOnValueChange: (ScreenElement, Any, ClientOrIssuerType?) -> Unit,
    bottomFormPlaceCursor: (ScreenElement, ClientOrIssuerType?) -> Unit,
    onClickDoneForm: (DocumentBottomSheetTypeOfForm, syncToMaster: Boolean) -> Unit,
    onClickCancelForm: () -> Unit,
    onSelectTaxRate: (BigDecimal?) -> Unit,
    showDocumentForm: Boolean,
    onShowDocumentForm: (Boolean) -> Unit,
    onClickDeleteAddress: (ClientOrIssuerType) -> Unit,
    onClickDeleteEmail: (ClientOrIssuerType, Int) -> Unit = { _, _ -> },
    onAddEmail: (ClientOrIssuerType, String) -> Unit = { _, _ -> },
    onPendingEmailValidationResult: (ClientOrIssuerType, Boolean) -> Unit = { _, _ -> },
    onOrderChange: (List<DocumentProductState>) -> Unit,
    onShowMessage: (String) -> Unit, // For showing toast/snackbar messages
    exportPdfContent: @Composable (DocumentState, () -> Unit) -> Unit, // Slot for ExportPdf
    showProductType: Boolean = false,
    hideLinkedSourceHeaders: Boolean = false,
    onToggleHideLinkedSourceHeaders: (() -> Unit)? = null,
    // Retention CRUD, only wired non-noop by Invoice + CreditNote NavGraphs.
    onSaveRetention: (Int, com.a4a.g8invoicing.ui.states.RetentionState) -> Unit = { _, _ -> },
    onToggleRetentionHidden: (Int) -> Unit = {},
) {
    // We use BottomSheetScaffold to open a bottom sheet modal
    // (We could use ModalBottomSheet but there are issues with overlapping system navigation)
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )
    val bottomSheetType = remember { mutableStateOf(BottomSheetType.ITEMS) }
    val scope = rememberCoroutineScope()

    val focusManager = LocalFocusManager.current // Obtenir le FocusManager
    val keyboardController =
        LocalSoftwareKeyboardController.current // Obtenir le KeyboardController

    // Store string for callback (can't use stringResource in lambda)
    val comingSoonMessage = stringResource(Res.string.feature_coming_soon)

    // When the bottom sheet is open (either partially expanded or fully expanded)
    // intercept system back to close it instead of popping back to the document list.
    val isSheetVisible = scaffoldState.bottomSheetState.currentValue != SheetValue.Hidden
    PlatformBackHandler(enabled = isSheetVisible) {
        hideBottomSheet(scope, scaffoldState, focusManager, keyboardController)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val sheetLayoutHeight = maxHeight
    val partialPeekHeight = sheetLayoutHeight / 2

    BottomSheetScaffold(
        sheetSwipeEnabled = false,
        sheetDragHandle = null,
        sheetShape = RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp
        ),// Remove rounded corners (must be a better way..)
        scaffoldState = scaffoldState,
        sheetPeekHeight = partialPeekHeight,
        sheetContent = {
            if (bottomSheetType.value == BottomSheetType.ELEMENTS) {
                DocumentBottomSheetTextElements(
                    document = document,
                    onDismissBottomSheet = {
                        hideBottomSheet(scope, scaffoldState, focusManager, keyboardController)
                    },
                    sheetMaxHeight = sheetLayoutHeight,
                    isSheetFullScreen = scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded,
                    onSheetDragUp = {
                        scope.launch { scaffoldState.bottomSheetState.expand() }
                    },
                    onSheetStepDown = {
                        scope.launch {
                            if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                                scaffoldState.bottomSheetState.partialExpand()
                            } else {
                                hideBottomSheet(scope, scaffoldState, focusManager, keyboardController)
                            }
                        }
                    },
                    // Overscroll from the content only ever collapses Expanded→Partial
                    // (never chains to Hidden). Prevents the race where a fast swipe
                    // dispatches two overscroll frames back-to-back and the second one
                    // reads currentValue after partialExpand settled, dismissing the sheet.
                    onSheetCollapseToPartial = {
                        scope.launch {
                            if (scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded) {
                                scaffoldState.bottomSheetState.partialExpand()
                            }
                        }
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
                    onClickDeleteAddress = onClickDeleteAddress,
                    onClickDeleteEmail = onClickDeleteEmail,
                    onAddEmail = onAddEmail,
                    onPendingEmailValidationResult = onPendingEmailValidationResult,
                    showProductType = showProductType,
                )
            } else {
                DocumentBottomSheetProducts(
                    document = document,
                    onDismissBottomSheet = {
                        hideBottomSheet(scope, scaffoldState, focusManager, keyboardController)
                    },
                    sheetMaxHeight = sheetLayoutHeight,
                    isSheetFullScreen = scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded,
                    onSheetDragUp = {
                        scope.launch { scaffoldState.bottomSheetState.expand() }
                    },
                    onSheetStepDown = {
                        scope.launch {
                            if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                                scaffoldState.bottomSheetState.partialExpand()
                            } else {
                                hideBottomSheet(scope, scaffoldState, focusManager, keyboardController)
                            }
                        }
                    },
                    onSheetCollapseToPartial = {
                        scope.launch {
                            if (scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded) {
                                scaffoldState.bottomSheetState.partialExpand()
                            }
                        }
                    },
                    documentProductUiState = documentProductUiState,
                    products = products,
                    taxRates = taxRates,
                    onClickProduct = { product ->
                        onSelectProduct(product, document.documentClient?.originalClientOrIssuerId)
                    },
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
                    onOrderChange = onOrderChange,
                    showProductType = showProductType,
                    hideLinkedSourceHeaders = hideLinkedSourceHeaders,
                    onToggleHideLinkedSourceHeaders = onToggleHideLinkedSourceHeaders,
                    onSaveRetention = onSaveRetention,
                    onToggleRetentionHidden = onToggleRetentionHidden,
                )
            }
        },
        sheetShadowElevation = 30.dp
    )
    { paddingValues ->

        var showPopup by rememberSaveable {
            mutableStateOf(false)
        }
        // CII XML export gate — surface the chooser when the user has activated
        // the gStore module. Free for now (pre-launch); will be re-gated behind
        // premium later. Non-invoice types (BL / avoir / devis) always route
        // straight to the PDF popup since CiiXmlBuilder is invoice-only for now.
        val modulesRepo: ActivatedModulesRepository = koinInject()
        val activated by modulesRepo.state.collectAsState()
        val ciiUnlocked = document is InvoiceState &&
            ActivatedModulesRepository.MODULE_CII_XML_EXPORT in activated
        val facturxUnlocked = document is InvoiceState &&
            ActivatedModulesRepository.MODULE_FACTURX_EXPORT in activated
        val ciiExportUnlocked = ciiUnlocked || facturxUnlocked
        var showExportChooser by rememberSaveable { mutableStateOf(false) }
        var showCiiPopup by rememberSaveable { mutableStateOf(false) }
        // Non-null while the Factur-X export dialog is on screen. Holds the
        // pre-built CII XML bytes so ExportPdfPlatform can embed them as
        // `factur-x.xml` in the PDF/A-3 hybrid without re-computing on
        // recomposition.
        var facturxXmlBytes by remember { mutableStateOf<ByteArray?>(null) }
        // Blocks any export (PDF or CII) when the issuer is in the franchise en
        // base regime but products still carry a non-zero VAT rate. Franchise is
        // per-issuer legal status; taxed lines would produce a PDF that shows VAT
        // the issuer isn't allowed to collect, and a CII XML whose category=E +
        // rate>0 breaks EN16931.
        var showVatExemptConflict by rememberSaveable { mutableStateOf(false) }
        // Pre-flight validation issues surfaced when the user picks CII from the
        // chooser but the invoice is missing EN 16931 mandatory fields. Empty =
        // OK to export; non-empty = block export, show the list.
        var ciiValidationIssues by remember { mutableStateOf(emptyList<CiiValidationIssue>()) }
        val exportChooserTitle = stringResource(Res.string.export_chooser_title)
        val exportChooserDescription = stringResource(Res.string.export_chooser_description)
        val pdfLabel = stringResource(Res.string.export_chooser_pdf)
        val ciiLabel = stringResource(Res.string.export_chooser_cii)
        val facturxLabel = stringResource(Res.string.export_chooser_facturx)
        val vatConflictTitle = stringResource(Res.string.export_vat_exempt_conflict_title)
        val vatConflictMessage = stringResource(Res.string.export_vat_exempt_conflict_message)
        val okLabel = stringResource(Res.string.ok)
        // Preloaded so building the CII XML for Factur-X doesn't have to
        // suspend on stringResource in an onClick callback.
        val paymentMeansLabelsForFacturx: Map<String, String> = PaymentMeans.entries.associate {
            it.chipId to stringResource(it.labelRes)
        }
        val bankIbanLabel = stringResource(Res.string.issuer_bank_identifier_iban)
        val bankGenericLabel = stringResource(Res.string.issuer_bank_identifier_generic)
        // As it's not possible to have a bottom bar inside a BottomSheetScaffold,
        // as a temporary solution, we use Scaffold inside BottomSheetScaffold
        Scaffold(
            topBar = {
                DeliveryNoteAddEditTopBar(
                    navController = navController,
                    onClickBack = onClickBack,
                    onClickExport = {
                        if (hasVatExemptConflict(document)) {
                            showVatExemptConflict = true
                        } else if (ciiExportUnlocked) {
                            showExportChooser = true
                        } else {
                            showPopup = true
                        }
                    }
                )
            },
            bottomBar = {
                DocumentAddEditBottomBar(
                    onClickElements = {
                        bottomSheetType.value = BottomSheetType.ELEMENTS
                        expandBottomSheet(scope, scaffoldState)
                    },
                    onClickItems = {
                        bottomSheetType.value = BottomSheetType.ITEMS
                        expandBottomSheet(scope, scaffoldState)
                    },
                    onClickStyle = {
                        bottomSheetType.value = BottomSheetType.STYLE
                        onShowMessage(comingSoonMessage)
                    }
                )
            }
        ) { innerPadding ->
            if (showPopup) {
                ExportPopup(
                    document = document,
                    onDismissRequest = { showPopup = false },
                    exportPdfContent = exportPdfContent
                )
            }

            if (showVatExemptConflict) {
                AlertDialog(
                    onDismissRequest = { showVatExemptConflict = false },
                    title = { Text(vatConflictTitle) },
                    text = { Text(vatConflictMessage) },
                    textContentColor = Color.Black,
                    confirmButton = {
                        Button(onClick = { showVatExemptConflict = false }) {
                            Text(okLabel)
                        }
                    },
                )
            }

            if (showExportChooser && document is InvoiceState) {
                ExportFormatChooserDialog(
                    title = exportChooserTitle,
                    description = exportChooserDescription,
                    ciiLabel = ciiLabel,
                    facturxLabel = facturxLabel,
                    pdfLabel = pdfLabel,
                    showCii = ciiUnlocked,
                    showFacturx = facturxUnlocked,
                    onDismiss = { showExportChooser = false },
                    onPickCii = {
                        showExportChooser = false
                        val issues = CiiPreflightValidator.validate(document)
                        if (issues.isEmpty()) {
                            showCiiPopup = true
                        } else {
                            ciiValidationIssues = issues
                        }
                    },
                    onPickFacturx = {
                        showExportChooser = false
                        val issues = CiiPreflightValidator.validate(document)
                        if (issues.isEmpty()) {
                            val bankInfoText = buildBankInfoText(
                                invoice = document,
                                ibanLabel = bankIbanLabel,
                                genericLabel = bankGenericLabel,
                            )
                            val xml = CiiXmlBuilder.build(
                                invoice = document,
                                paymentMeansLabels = paymentMeansLabelsForFacturx,
                                bankInfoText = bankInfoText,
                            )
                            facturxXmlBytes = xml.encodeToByteArray()
                        } else {
                            ciiValidationIssues = issues
                        }
                    },
                    onPickPdf = {
                        showExportChooser = false
                        showPopup = true
                    },
                )
            }

            if (ciiValidationIssues.isNotEmpty()) {
                CiiValidationDialog(
                    issues = ciiValidationIssues,
                    onDismiss = { ciiValidationIssues = emptyList() },
                )
            }

            if (showCiiPopup && document is InvoiceState) {
                Dialog(
                    onDismissRequest = {},
                    properties = DialogProperties(usePlatformDefaultWidth = false),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(10F),
                        contentAlignment = Alignment.Center,
                    ) {
                        ExportCiiPlatform(
                            invoice = document,
                            onDismissRequest = { showCiiPopup = false },
                        )
                    }
                }
            }

            facturxXmlBytes?.let { bytes ->
                if (document is InvoiceState) {
                    Dialog(
                        onDismissRequest = {},
                        properties = DialogProperties(usePlatformDefaultWidth = false),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(10F),
                            contentAlignment = Alignment.Center,
                        ) {
                            ExportPdfPlatform(
                                document = document,
                                onDismissRequest = { facturxXmlBytes = null },
                                facturxXmlBytes = bytes,
                            )
                        }
                    }
                }
            }

            var zoom by remember { mutableFloatStateOf(1f) }
            var animatableOffsetX by remember { mutableStateOf(Animatable(0f)) }
            var animatableOffsetY by remember { mutableStateOf(Animatable(0f)) }
            var offsetY by remember { mutableFloatStateOf(0f) }
            val coroutineScope = rememberCoroutineScope()
            var clickEnabled by remember { mutableStateOf(true) } // To disable clicking 2 items at a time
            var newOffsetY by remember { mutableFloatStateOf(0f) }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.divider)
            ) {
                // A4 aspect ratio: 210mm / 297mm ≈ 0.707
                val a4AspectRatio = 210f / 297f
                // On desktop (wider screens), constrain to A4 format
                val isWideScreen = maxWidth > 600.dp
                val documentMaxWidth = if (isWideScreen) 500.dp else maxWidth

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .then(
                            if (isWideScreen) {
                                Modifier
                                    .widthIn(max = documentMaxWidth)
                                    .align(Alignment.TopCenter)
                            } else {
                                Modifier.fillMaxSize()
                            }
                        )
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

                                    // Consume touch when multiple fingers down
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
                            if (scaffoldState.bottomSheetState.currentValue != SheetValue.Hidden) {
                                hideBottomSheet(
                                    scope,
                                    scaffoldState,
                                    focusManager,
                                    keyboardController
                                )

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
                            if (scaffoldState.bottomSheetState.currentValue != SheetValue.Hidden) {
                                hideBottomSheet(
                                    scope,
                                    scaffoldState,
                                    focusManager,
                                    keyboardController
                                )
                            }
                        },
                    )
                }
            }
        }
    }
    }
}


// Franchise en base (issuer.vatExempt) is a per-issuer legal status: no VAT is
// ever collected. If the doc still has products with a non-zero rate, exporting
// as PDF would print VAT the issuer can't collect, and CII would emit
// category=E + rate>0 which breaks EN16931. Block both routes and ask the user
// to clear the rates first.
private fun hasVatExemptConflict(document: DocumentState): Boolean {
    if (document.documentIssuer?.vatExempt != true) return false
    val products = document.documentProducts ?: return false
    return products.any { (it.taxRate ?: BigDecimal.ZERO) > BigDecimal.ZERO }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun expandBottomSheet(scope: CoroutineScope, scaffoldState: BottomSheetScaffoldState) {
    scope.launch { scaffoldState.bottomSheetState.partialExpand() }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
private fun hideBottomSheet(
    scope: CoroutineScope,
    scaffoldState: BottomSheetScaffoldState,
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?,
) {
    scope.launch {
        focusManager.clearFocus() // Effacer le focus d'abord
        keyboardController?.hide() // Puis cacher le clavier explicitement
        scaffoldState.bottomSheetState.hide()
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
        appBarAction = actionExport(
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
    document: DocumentState,
    onDismissRequest: () -> Unit,
    exportPdfContent: @Composable (DocumentState, () -> Unit) -> Unit,
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
            exportPdfContent(document, onDismissRequest)
        }
    }
}

/**
 * Lists every EN 16931 mandatory field that's missing from the current
 * invoice so the user fixes the whole set at once instead of hitting
 * "Export CII" repeatedly. Modal — user has to acknowledge before
 * touching anything else.
 */
@Composable
private fun CiiValidationDialog(
    issues: List<CiiValidationIssue>,
    onDismiss: () -> Unit,
) {
    val title = stringResource(Res.string.cii_validation_title)
    val intro = stringResource(Res.string.cii_validation_intro)
    val confirmLabel = stringResource(Res.string.cii_validation_confirm)

    // Resolve every issue to its localized label up-front — stringResource
    // has to run inside the Composable, not inside a when-expression that
    // would compose lazily on click.
    val messages: List<String> = issues.map { issue -> issue.resolveMessage() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        textContentColor = Color.Black,
        text = {
            Column {
                Text(intro)
                Spacer(Modifier.height(12.dp))
                messages.forEach { line ->
                    Text("• $line")
                    Spacer(Modifier.height(4.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text(confirmLabel) }
        },
    )
}

@Composable
private fun CiiValidationIssue.resolveMessage(): String = when (this) {
    CiiValidationIssue.IssuerMissing -> stringResource(Res.string.cii_validation_issuer_missing)
    CiiValidationIssue.IssuerName -> stringResource(Res.string.cii_validation_issuer_name)
    CiiValidationIssue.IssuerSiren -> stringResource(Res.string.cii_validation_issuer_siren)
    CiiValidationIssue.IssuerSirenFormat -> stringResource(Res.string.cii_validation_issuer_siren_format)
    CiiValidationIssue.IssuerVat -> stringResource(Res.string.cii_validation_issuer_vat)
    CiiValidationIssue.IssuerAddress -> stringResource(Res.string.cii_validation_issuer_address)
    CiiValidationIssue.ClientMissing -> stringResource(Res.string.cii_validation_client_missing)
    CiiValidationIssue.ClientName -> stringResource(Res.string.cii_validation_client_name)
    CiiValidationIssue.ClientTypeUnspecified -> stringResource(Res.string.cii_validation_client_type)
    CiiValidationIssue.ClientSiren -> stringResource(Res.string.cii_validation_client_siren)
    CiiValidationIssue.ClientSirenFormat -> stringResource(Res.string.cii_validation_client_siren_format)
    CiiValidationIssue.ClientEmail -> stringResource(Res.string.cii_validation_client_email)
    CiiValidationIssue.ClientAddress -> stringResource(Res.string.cii_validation_client_address)
    CiiValidationIssue.ProductsEmpty -> stringResource(Res.string.cii_validation_products_empty)
    is CiiValidationIssue.LineName -> stringResource(Res.string.cii_validation_line_name, lineNumber)
    is CiiValidationIssue.LinePrice -> stringResource(Res.string.cii_validation_line_price, lineNumber)
    is CiiValidationIssue.LineTaxRate -> stringResource(Res.string.cii_validation_line_tax_rate, lineNumber)
    is CiiValidationIssue.LineTaxRateInvalid -> stringResource(Res.string.cii_validation_line_tax_rate_invalid, lineNumber, rate)
    CiiValidationIssue.InvoiceDueDate -> stringResource(Res.string.cii_validation_due_date)
    CiiValidationIssue.VatExemptionTextMissing -> stringResource(Res.string.cii_validation_vat_exemption_text)
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

@Composable
private fun ExportFormatChooserDialog(
    title: String,
    description: String,
    ciiLabel: String,
    facturxLabel: String,
    pdfLabel: String,
    showCii: Boolean,
    showFacturx: Boolean,
    onDismiss: () -> Unit,
    onPickCii: () -> Unit,
    onPickFacturx: () -> Unit,
    onPickPdf: () -> Unit,
) {
    // Primary CTA priority: Factur-X > CII. Whichever is the "highest"
    // structured format the user has unlocked wears the violet button; the
    // remaining structured button (if any) sits below as outlined; PDF is
    // always outlined at the bottom.
    val primaryIsFacturx = showFacturx
    val primaryIsCii = !showFacturx && showCii
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .background(AppColors.surface, shape = RoundedCornerShape(16.dp))
                .padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.textScreenTitle.copy(fontSize = 18.sp),
                    textAlign = TextAlign.Start,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary),
                    textAlign = TextAlign.Start,
                    lineHeight = 20.sp,
                )
                Spacer(Modifier.height(24.dp))
                if (primaryIsFacturx) {
                    Button(
                        onClick = onPickFacturx,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.buttonActive,
                            contentColor = AppColors.textOnAccent,
                        ),
                    ) { Text(facturxLabel) }
                    Spacer(Modifier.height(8.dp))
                }
                if (primaryIsCii) {
                    Button(
                        onClick = onPickCii,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.buttonActive,
                            contentColor = AppColors.textOnAccent,
                        ),
                    ) { Text(ciiLabel) }
                    Spacer(Modifier.height(8.dp))
                }
                if (showFacturx && !primaryIsFacturx) {
                    OutlinedButton(
                        onClick = onPickFacturx,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.textLink),
                    ) { Text(facturxLabel) }
                    Spacer(Modifier.height(8.dp))
                }
                if (showCii && !primaryIsCii) {
                    OutlinedButton(
                        onClick = onPickCii,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.textLink),
                    ) { Text(ciiLabel) }
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedButton(
                    onClick = onPickPdf,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.textLink),
                ) { Text(pdfLabel) }
            }
        }
    }
}
