package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.shared.PlatformBackHandler
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.ionspin.kotlin.bignum.decimal.BigDecimal


@Composable
fun DocumentBottomSheetTextElements(
    document: DocumentState,
    onDismissBottomSheet: () -> Unit,
    sheetMaxHeight: Dp,
    isSheetFullScreen: Boolean,
    onSheetDragUp: () -> Unit,
    onSheetStepDown: () -> Unit,
    onValueChange: (ScreenElement, Any) -> Unit,
    clients: MutableList<ClientOrIssuerState>,
    issuers: MutableList<ClientOrIssuerState>,
    documentClientUiState: ClientOrIssuerState,
    documentIssuerUiState: ClientOrIssuerState,
    taxRates: List<BigDecimal>,
    onSelectClientOrIssuer: (ClientOrIssuerState) -> Unit,
    onClickNewDocumentClientOrIssuer: (ClientOrIssuerType) -> Unit,
    onClickEditDocumentClientOrIssuer: (ClientOrIssuerState) -> Unit,
    onClickDeleteDocumentClientOrIssuer: (ClientOrIssuerType) -> Unit,
    currentClientId: Int? = null,
    currentIssuerId: Int? = null,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    bottomFormOnValueChange: (ScreenElement, Any, ClientOrIssuerType?) -> Unit,
    bottomFormPlaceCursor: (ScreenElement, ClientOrIssuerType?) -> Unit,
    onClickDoneForm: (DocumentBottomSheetTypeOfForm) -> Unit,
    onClickCancelForm: () -> Unit,
    onSelectTaxRate: (BigDecimal?) -> Unit,
    localFocusManager: FocusManager,
    showDocumentForm: Boolean,
    onShowDocumentForm: (Boolean) -> Unit,
    onClickDeleteAddress: (ClientOrIssuerType) -> Unit,
    onClickDeleteEmail: (ClientOrIssuerType, Int) -> Unit = { _, _ -> },
    onAddEmail: (ClientOrIssuerType, String) -> Unit = { _, _ -> },
    onPendingEmailValidationResult: (ClientOrIssuerType, Boolean) -> Unit = { _, _ -> },
    showProductType: Boolean = false,
) {
    val density = LocalDensity.current
    val topInsetDp = with(density) { WindowInsets.safeDrawing.getTop(density).toDp() }
    val sheetMaxContentHeight = sheetMaxHeight - topInsetDp
    val visibleContentHeight by animateDpAsState(
        targetValue = if (isSheetFullScreen) sheetMaxContentHeight else sheetMaxHeight / 2,
        label = "text-sheet-content-height",
    )

    Box(
        modifier = Modifier
            .height(sheetMaxContentHeight)
            .imePadding()
    ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(visibleContentHeight)
    ) {
        val slideOtherComponent: MutableState<ScreenElement?> = remember { mutableStateOf(null) }

        // Back inside a slid-in sub-screen (client/issuer list, date picker, footer…)
        // returns to the elements list instead of dismissing the whole sheet.
        PlatformBackHandler(enabled = slideOtherComponent.value != null) {
            slideOtherComponent.value = null
        }

        SheetDragHandle(
            onDragUp = onSheetDragUp,
            onDragDown = onSheetStepDown,
            onTap = onSheetStepDown,
        )

        Box(
            modifier = Modifier
                .background(Color.Transparent)
                .fillMaxWidth() // Prend toute la largeur
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        localFocusManager.clearFocus() // Efface le focus, ce qui devrait cacher le clavier
                    }
                )
                .focusable(false)
        ) {
            // Keep the main elements list rendered even when a slide-in is open, so
            // ModalBottomSheet sub-sheets (date, footer…) show it greyed under their
            // scrim. Non-modal sub-sheets below must fillMaxSize so they cover it.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 50.dp)
            ) {
                // MAIN ELEMENTS
                DocumentBottomSheetElementsContent(
                    document = document,
                    onValueChange = onValueChange,
                    onClickForward = {
                        //  localFocusManager.clearFocus(force = true)
                        slideOtherComponent.value = it
                    },
                    placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                    localFocusManager = localFocusManager
                )
            }

            // SLIDING ELEMENTS (ISSUER, SENDER, DATE..)
            DocumentBottomSheetElementsAfterSlide(
                pageElement = slideOtherComponent.value,
                parameters = when (slideOtherComponent.value) {
                    ScreenElement.DOCUMENT_ISSUER -> Pair(
                        document.documentIssuer,
                        issuers
                    )

                    ScreenElement.DOCUMENT_CLIENT -> Pair(
                        document.documentClient,
                        clients
                    )

                    ScreenElement.DOCUMENT_DATE -> document.documentDate
                    ScreenElement.DOCUMENT_DUE_DATE -> (document as InvoiceState).dueDate
                    ScreenElement.DOCUMENT_FOOTER -> {
                        document.footerText
                    }

                    else -> {}
                },
                onClickBack = {
                    slideOtherComponent.value = null
                },
                documentClientUiState = documentClientUiState,
                documentIssuerUiState = documentIssuerUiState,
                taxRates = taxRates,
                onSelectClientOrIssuer = {
                    slideOtherComponent.value = null
                    onSelectClientOrIssuer(it)
                },
                onClickNewDocumentClientOrIssuer = onClickNewDocumentClientOrIssuer,
                onClickEditDocumentClientOrIssuer = onClickEditDocumentClientOrIssuer,
                onClickDeleteDocumentClientOrIssuer = onClickDeleteDocumentClientOrIssuer,
                currentClientId = currentClientId,
                currentIssuerId = currentIssuerId,
                bottomFormOnValueChange = bottomFormOnValueChange,
                bottomFormPlaceCursor = bottomFormPlaceCursor,
                onClickDoneForm = {
                    slideOtherComponent.value = null
                    onClickDoneForm(it)
                },
                onClickCancelForm = onClickCancelForm,
                onSelectTaxRate = onSelectTaxRate,
                showDocumentForm = showDocumentForm,
                onShowDocumentForm = onShowDocumentForm,
                onValueChange = onValueChange,
                onClickDeleteAddress = onClickDeleteAddress,
                onClickDeleteEmail = onClickDeleteEmail,
                onAddEmail = onAddEmail,
                onPendingEmailValidationResult = onPendingEmailValidationResult,
                showProductType = showProductType,
            )
        }
    }
    }
}


//TODO animation: slide elements from right to left on open, and left to right on close
/*AnimatedContent(
    targetState = pageElement,
    transitionSpec = {
        ContentTransform(
            targetContentEnter = slideInHorizontally(
                animationSpec = tween(
                    durationMillis= 14000,
                    easing = LinearEasing
                )
            ),
            initialContentExit = slideOutHorizontally(
                animationSpec = tween(
                    durationMillis= 14000,
                    easing = LinearEasing
                )
            ),
            sizeTransform = SizeTransform(sizeAnimationSpec = { _, _ -> tween() })
        )
    }, label = ""
) { targetState ->
    if (targetState == PageElement.CLIENT) {
        DeliveryNoteBottomSheetList(
            // clients = parameters?.let { it as List<ClientOrIssuerEditable> } ?: emptyList(),
            onClickBack = onClickBack,
            onItemClick = onItemClick
        )
    }
}*/


/*
if (pageElement == PageElement.CLIENT) {
    // Animated visibility can contain only one
    //composable, unless the exit won't work (that's why the if(pageElement..) is outside)
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(
            initialOffsetX = { it }, // it = fullWidth
            animationSpec = tween(
                easing = LinearEasing
            )
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(
                easing = LinearEasing
            )
        )
    ) {
        DeliveryNoteBottomSheetList(
            clients = parameters?.let { it as List<ClientOrIssuerEditable> } ?: emptyList(),
            onClickBack = onClickBack,
            onItemClick = onItemClick
        )
    }
}
if (pageElement == PageElement.DATE) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(
            initialOffsetX = { it }, // it = fullWidth
            animationSpec = tween(
                easing = LinearEasing
            )
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(
                easing = LinearEasing
            )
        )
    ) {
        DeliveryNoteBottomSheetDatePicker(
            initialDate = parameters?.let { it as String } ?: "",
            datePickerState = datePickerState,
            onClickBack = onClickBack,
        )
    }
}
*/
