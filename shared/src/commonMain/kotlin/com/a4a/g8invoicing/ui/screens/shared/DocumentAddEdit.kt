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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import com.a4a.g8invoicing.ui.navigation.actionFont
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
import com.a4a.g8invoicing.ui.theme.ColorLightGrey
import com.a4a.g8invoicing.ui.theme.DocumentFont
import com.a4a.g8invoicing.ui.theme.LocalDocumentFont
import com.a4a.g8invoicing.ui.theme.textBodySmall
import com.a4a.g8invoicing.ui.theme.textScreenTitle
import com.a4a.g8invoicing.data.auth.ActivatedModulesRepository
import com.a4a.g8invoicing.data.auth.isPremium
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
import com.a4a.g8invoicing.shared.resources.export_error_premium_font_message
import com.a4a.g8invoicing.shared.resources.export_vat_exempt_conflict_message
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
    // Font picker: fired when the user picks a font in the Police sheet.
    // Persists the DocumentFont.id on the doc row. Default no-op so callers
    // that haven't wired it yet still compile.
    onFontSelect: (DocumentFont) -> Unit = {},
) {
    // ModalBottomSheet lives in a separate window (Dialog), so it naturally
    // draws over the DocumentAddEditBottomBar with no z-order gymnastics —
    // and each nested sheet (issuer / client / date / footer picker) stacks
    // in its own window too.
    var currentSheet by remember { mutableStateOf<BottomSheetType?>(null) }
    // Toggled by a drag gesture on the DragHandle. False = half-height (default
    // when a sheet opens); true = full-height. Wrapped in an explicit
    // MutableState (rather than a `by` delegate) so the confirmValueChange
    // lambda below can read the live value.
    val expandedByHandleState = remember(currentSheet) { mutableStateOf(false) }
    var expandedByHandle by expandedByHandleState
    val scope = rememberCoroutineScope()

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = androidx.compose.ui.platform.LocalDensity.current

    // Store string for callback (can't use stringResource in lambda)
    val comingSoonMessage = stringResource(Res.string.feature_coming_soon)

    // --- Dismissal hardening -------------------------------------------------
    // Material3 1.5.0-alpha19 doesn't expose positionalThreshold /
    // velocityThreshold. Both are hard-coded to 56.dp and 125.dp px/s inside
    // AnchoredDraggableState. So a fast flick or a >56dp drag validates
    // Hidden — too easy to close by mistake.
    //
    // We hijack confirmValueChange (the only public lever) to reject the
    // Hidden target unless the user has physically dragged the sheet down by
    // more than 200dp. AnchoredDraggableState then bounces back to Expanded.
    // Velocity never wins on its own because we filter the *result*, not the
    // gesture.
    //
    // Side-effect: programmatic hide() calls (bouton, back, drag-handle path)
    // would also be rejected. `allowProgrammaticHide` bypasses the filter for
    // that narrow window; set true right before hide(), reset in
    // invokeOnCompletion.
    //
    // Chicken-and-egg: the lambda needs sheetState.requireOffset(), but
    // sheetState is being created. `sheetStateRef` breaks the loop.
    val allowProgrammaticHide = remember { mutableStateOf(false) }
    val sheetStateRef = remember {
        mutableStateOf<androidx.compose.material3.SheetState?>(null)
    }
    // Latest measured screen height in px. Updated from inside
    // BoxWithConstraints via SideEffect below.
    val layoutHeightPx = remember { androidx.compose.runtime.mutableFloatStateOf(0f) }

    val confirmValueChange = remember(density) {
        val dismissThresholdPx = with(density) { 200.dp.toPx() }
        val fullscreenTopInsetPx = with(density) { 50.dp.toPx() }
        val halfBottomOffsetPx = with(density) { 30.dp.toPx() }
        fun(target: androidx.compose.material3.SheetValue): Boolean {
            // Always allow: non-Hidden targets, and programmatic hide().
            if (target != androidx.compose.material3.SheetValue.Hidden ||
                allowProgrammaticHide.value
            ) return true
            val currentOffset = sheetStateRef.value?.let {
                runCatching { it.requireOffset() }.getOrNull()
            } ?: return true
            val h = layoutHeightPx.floatValue
            if (h == 0f) return true
            // Mirror the animated sheet height math from below.
            val expandedContentPx = if (expandedByHandleState.value) {
                h - fullscreenTopInsetPx
            } else {
                h / 2f - halfBottomOffsetPx
            }
            val expandedOffsetPx = h - expandedContentPx
            val delta = currentOffset - expandedOffsetPx
            // Allow when:
            // - delta <= 0 → sheet hasn't been dragged (tap-outside, back-press
            //   check happens with the sheet still at Expanded position)
            // - delta > threshold → user dragged far enough to confirm
            // Reject the middle "dragged a bit but not enough" range → sheet
            // bounces back to Expanded.
            return delta <= 0f || delta > dismissThresholdPx
        }
    }

    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = confirmValueChange,
    )
    LaunchedEffect(sheetState) { sheetStateRef.value = sheetState }

    // Shared close routine: bypass the dismissal filter, animate the sheet
    // away, then flip currentSheet back to null so the ModalBottomSheet
    // unmounts.
    val dismissSheet: () -> Unit = {
        allowProgrammaticHide.value = true
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            allowProgrammaticHide.value = false
            focusManager.clearFocus()
            keyboardController?.hide()
            currentSheet = null
        }
    }

    // System back closes the sheet instead of popping the document.
    PlatformBackHandler(enabled = currentSheet != null) { dismissSheet() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val sheetLayoutHeight = maxHeight
    // Feed the layout height to the confirmValueChange lambda above.
    androidx.compose.runtime.SideEffect {
        layoutHeightPx.floatValue = with(density) { sheetLayoutHeight.toPx() }
    }

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
        // Unified "Oups" export blocker: aggregates the three pre-flight
        // failures that can prevent exporting (premium font w/o subscription,
        // franchise-de-TVA line with a non-zero rate, EN 16931 mandatory fields
        // missing on Facturx/CII). Rendered as a single dialog by [OupsDialog]
        // so the user sees the full punch list at once instead of fixing one
        // and re-hitting Export to discover the next.
        var oupsFontBlock by rememberSaveable { mutableStateOf(false) }
        var oupsVatBlock by rememberSaveable { mutableStateOf(false) }
        var ciiValidationIssues by remember { mutableStateOf(emptyList<CiiValidationIssue>()) }
        val fontModuleOn = ActivatedModulesRepository.MODULE_FONT in activated
        val subscriptionRepo: com.a4a.g8invoicing.data.auth.SubscriptionRepository = koinInject()
        val subscription by subscriptionRepo.state.collectAsState()
        val isPremium: Boolean = subscription.isPremium()
        val currentFont = DocumentFont.fromId(document.fontFamily)
        val exportChooserTitle = stringResource(Res.string.export_chooser_title)
        val exportChooserDescription = stringResource(Res.string.export_chooser_description)
        val pdfLabel = stringResource(Res.string.export_chooser_pdf)
        val ciiLabel = stringResource(Res.string.export_chooser_cii)
        val facturxLabel = stringResource(Res.string.export_chooser_facturx)
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
        CompositionLocalProvider(LocalDocumentFont provides currentFont) {
        Scaffold(
            topBar = {
                DeliveryNoteAddEditTopBar(
                    navController = navController,
                    onClickBack = onClickBack,
                    onClickExport = {
                        if (ciiExportUnlocked) {
                            // Facturx/CII active — chooser first, then per-format
                            // gates are applied in its callbacks below (so a user
                            // picking PDF isn't hit with a Facturx-only warning).
                            showExportChooser = true
                        } else {
                            val fontBlocks = currentFont.isPremium && !isPremium
                            val vatBlocks = hasVatExemptConflict(document)
                            if (fontBlocks || vatBlocks) {
                                oupsFontBlock = fontBlocks
                                oupsVatBlock = vatBlocks
                            } else {
                                showPopup = true
                            }
                        }
                    }
                )
            },
            bottomBar = {
                DocumentAddEditBottomBar(
                    onClickElements = {
                        currentSheet = BottomSheetType.ELEMENTS
                    },
                    onClickItems = {
                        currentSheet = BottomSheetType.ITEMS
                    },
                    onClickStyle = {
                        onShowMessage(comingSoonMessage)
                    },
                    onClickFont = if (fontModuleOn) {
                        { currentSheet = BottomSheetType.FONT }
                    } else null,
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
                        val fontBlocks = currentFont.isPremium && !isPremium
                        val vatBlocks = hasVatExemptConflict(document)
                        val issues = CiiPreflightValidator.validate(document)
                        if (fontBlocks || vatBlocks || issues.isNotEmpty()) {
                            oupsFontBlock = fontBlocks
                            oupsVatBlock = vatBlocks
                            ciiValidationIssues = issues
                        } else {
                            showCiiPopup = true
                        }
                    },
                    onPickFacturx = {
                        showExportChooser = false
                        val fontBlocks = currentFont.isPremium && !isPremium
                        val vatBlocks = hasVatExemptConflict(document)
                        val issues = CiiPreflightValidator.validate(document)
                        if (fontBlocks || vatBlocks || issues.isNotEmpty()) {
                            oupsFontBlock = fontBlocks
                            oupsVatBlock = vatBlocks
                            ciiValidationIssues = issues
                        } else {
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
                        }
                    },
                    onPickPdf = {
                        showExportChooser = false
                        // Plain PDF path — font + VAT gates still apply (both
                        // print on the PDF); no CII validation because there's
                        // no XML to conform to.
                        val fontBlocks = currentFont.isPremium && !isPremium
                        val vatBlocks = hasVatExemptConflict(document)
                        if (fontBlocks || vatBlocks) {
                            oupsFontBlock = fontBlocks
                            oupsVatBlock = vatBlocks
                        } else {
                            showPopup = true
                        }
                    },
                )
            }

            if (oupsFontBlock || oupsVatBlock || ciiValidationIssues.isNotEmpty()) {
                OupsDialog(
                    showFont = oupsFontBlock,
                    showVat = oupsVatBlock,
                    ciiIssues = ciiValidationIssues,
                    onDismiss = {
                        oupsFontBlock = false
                        oupsVatBlock = false
                        ciiValidationIssues = emptyList()
                    },
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
                        .padding(innerPadding)
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
                            if (currentSheet != null) {
                                dismissSheet()
                            } else {
                                currentSheet = if (it == ScreenElement.DOCUMENT_HEADER ||
                                    it == ScreenElement.DOCUMENT_NUMBER ||
                                    it == ScreenElement.DOCUMENT_DATE ||
                                    it == ScreenElement.DOCUMENT_ISSUER ||
                                    it == ScreenElement.DOCUMENT_CLIENT ||
                                    it == ScreenElement.DOCUMENT_FOOTER ||
                                    it == ScreenElement.DOCUMENT_REFERENCE
                                ) {
                                    BottomSheetType.ELEMENTS
                                } else {
                                    BottomSheetType.ITEMS
                                }
                            }
                        },
                        onClickRestOfThePage = {
                            if (currentSheet != null) dismissSheet()
                        },
                    )
                }
            }

        }

    // ModalBottomSheet lives in its own window → paints above the Scaffold's
    // bottomBar automatically, no cross-layer z-order fight. currentSheet
    // gates mounting; sheetState animates in/out; onDismissRequest handles
    // the scrim tap + swipe-to-dismiss.
    // Animated content height. Toggling expandedByHandle triggers a smooth
    // interpolation between the two heights rather than an instant snap.
    // - Fullscreen : stops 50dp below the top so the handle stays reachable
    //   (otherwise it slides up under the top system bar).
    // - Half       : 30dp lower than the geometric mid-screen — the sheet
    //   Surface starts a bit further down, which feels less imposing on
    //   short forms.
    val animatedSheetHeight by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (expandedByHandle) sheetLayoutHeight - 50.dp
        else sheetLayoutHeight / 2 - 30.dp,
        label = "sheet-content-height",
    )
    currentSheet?.let { sheet ->
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { dismissSheet() },
            sheetState = sheetState,
            // Kill the sheet-wide draggable. At Expanded (fullscreen) it eats
            // pointer events across the whole Surface, which was making every
            // field un-tappable until the sheet was collapsed back to half.
            // Our custom drag handle handles half↔full, the outer NSC handles
            // full→half via scroll-leftover, and dismissal goes through
            // dismissSheet() (tap-outside, back, buttons). No path relies on
            // dragging the sheet body itself.
            sheetGesturesEnabled = false,
            // Custom drag handle: vertical drag toggles expandedByHandle.
            // - Half → full : drag up past ~20dp of accumulated delta.
            // - Full → half : drag down past ~20dp of accumulated delta.
            // Consumes the gesture on trigger so the sheet's anchoredDraggable
            // doesn't try to fight the height animation. Downward drag past
            // the handle when already at half still passes through untouched
            // → anchoredDraggable dismisses (Expanded → Hidden).
            dragHandle = {
                // Custom drag handle (not BottomSheetDefaults.DragHandle) so
                // we avoid its internal 22dp vertical padding — that padding
                // was the "vide au-dessus du numéro" the user was seeing, and
                // squeezed the pill out of view when the outer Box was tight.
                //
                // Box = full width × 30dp for a comfortable touch zone; the
                // small grey pill sits centered inside.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .pointerInput(expandedByHandle) {
                            var totalY = 0f
                            var handled = false
                            detectVerticalDragGestures(
                                onDragStart = { totalY = 0f; handled = false },
                                onDragEnd = { totalY = 0f; handled = false },
                                onDragCancel = { totalY = 0f; handled = false },
                            ) { change: PointerInputChange, dragAmount: Float ->
                                totalY += dragAmount
                                if (!handled) {
                                    if (!expandedByHandle && totalY < -20f) {
                                        expandedByHandle = true
                                        handled = true
                                        change.consume()
                                    } else if (expandedByHandle && totalY > 20f) {
                                        expandedByHandle = false
                                        handled = true
                                        change.consume()
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(ColorLightGrey),
                    )
                }
            },
            // Square corners — the sheet reads as a flush edge over the doc
            // preview instead of a floating card.
            shape = androidx.compose.ui.graphics.RectangleShape,
            scrimColor = Color.Transparent,
            contentWindowInsets = { WindowInsets(0) },
        ) {
            when (sheet) {
                BottomSheetType.ELEMENTS -> DocumentBottomSheetTextElements(
                    document = document,
                    onDismissBottomSheet = { dismissSheet() },
                    sheetContentHeight = animatedSheetHeight,
                    isSheetExpanded = expandedByHandle,
                    onCollapseToHalf = { expandedByHandle = false },
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
                BottomSheetType.ITEMS -> DocumentBottomSheetProducts(
                    document = document,
                    onDismissBottomSheet = { dismissSheet() },
                    sheetContentHeight = animatedSheetHeight,
                    isSheetExpanded = expandedByHandle,
                    onCollapseToHalf = { expandedByHandle = false },
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
                BottomSheetType.FONT -> DocumentBottomSheetFont(
                    sheetContentHeight = animatedSheetHeight,
                    isSheetExpanded = expandedByHandle,
                    onCollapseToHalf = { expandedByHandle = false },
                    selected = currentFont,
                    isPremiumUser = isPremium,
                    onSelect = { picked ->
                        onFontSelect(picked)
                        dismissSheet()
                    },
                )
                BottomSheetType.STYLE, BottomSheetType.IMAGES -> {} // never surfaces as a sheet
            }
        }
    }
    } // CompositionLocalProvider(LocalDocumentFont)
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
    onClickFont: (() -> Unit)? = null,
) {
    DocumentBottomBar(
        actions = buildList {
            add(actionTextElements(onClickElements))
            add(actionItems(onClickItems))
            // Only surfaced when the Font module is activated in gStore —
            // the DocumentAddEdit callsite gates this via
            // MODULE_FONT ∈ activatedModules.
            onClickFont?.let { add(actionFont(it)) }
        }.toTypedArray()
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
 * Aggregated export blocker. Shows any combination of:
 * - 🪄 premium font picked while user isn't a subscriber
 * - 💸 franchise-de-TVA issuer with a taxed line
 * - 📄 EN 16931 mandatory fields missing (Facturx/CII path)
 *
 * User fixes the whole punch list at once rather than dismissing one modal
 * per issue and re-hitting Export.
 */
@Composable
private fun OupsDialog(
    showFont: Boolean,
    showVat: Boolean,
    ciiIssues: List<CiiValidationIssue>,
    onDismiss: () -> Unit,
) {
    val title = stringResource(Res.string.cii_validation_title)
    val fontLine = stringResource(Res.string.export_error_premium_font_message)
    val vatLine = stringResource(Res.string.export_vat_exempt_conflict_message)
    val ciiIntro = stringResource(Res.string.cii_validation_intro)
    val confirmLabel = stringResource(Res.string.cii_validation_confirm)

    // Resolve every CII issue to its localized label up-front — stringResource
    // must run inside the Composable, not inside a when-expression that would
    // compose lazily.
    val ciiMessages: List<String> = ciiIssues.map { issue -> issue.resolveMessage() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        textContentColor = Color.Black,
        text = {
            // Scrollable so the modal stays usable on short devices when
            // all three blockers fire at once (🪄 + 💸 + a long list of
            // missing Facturx fields easily overflows).
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (showFont) {
                    Text("🪄  $fontLine")
                    if (showVat || ciiMessages.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                    }
                }
                if (showVat) {
                    Text("💸  $vatLine")
                    if (ciiMessages.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                    }
                }
                if (ciiMessages.isNotEmpty()) {
                    Text("📄  $ciiIntro")
                    Spacer(Modifier.height(12.dp))
                    ciiMessages.forEach { line ->
                        Text("• $line")
                        Spacer(Modifier.height(4.dp))
                    }
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
    ELEMENTS, ITEMS, IMAGES, STYLE, FONT
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
