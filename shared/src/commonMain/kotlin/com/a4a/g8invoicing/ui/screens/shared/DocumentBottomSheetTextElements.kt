package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.shared.PlatformBackHandler
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.dismissKeyboardOnUnconsumedTap
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.ionspin.kotlin.bignum.decimal.BigDecimal


@Composable
fun DocumentBottomSheetTextElements(
    document: DocumentState,
    onDismissBottomSheet: () -> Unit,
    sheetContentHeight: Dp,
    // When true, the sheet is at its fullscreen mode. Content-drag downward
    // past the scroll-top edge should collapse back to half-height (via
    // onCollapseToHalf) rather than propagate to the sheet's anchoredDraggable
    // (which would dismiss the sheet in one shot).
    isSheetExpanded: Boolean,
    onCollapseToHalf: () -> Unit,
    onValueChange: (ScreenElement, Any) -> Unit,
    clients: MutableList<ClientOrIssuerState>,
    issuers: MutableList<ClientOrIssuerState>,
    documentClientUiState: ClientOrIssuerState,
    documentIssuerUiState: ClientOrIssuerState,
    taxRates: List<BigDecimal>,
    onSelectClientOrIssuer: (ClientOrIssuerState) -> Unit,
    onClickNewDocumentClientOrIssuer: (ClientOrIssuerType) -> Unit,
    onClickEditDocumentClientOrIssuer: (ClientOrIssuerState, openFormOnCompletion: Boolean) -> Unit,
    onClickDeleteDocumentClientOrIssuer: (ClientOrIssuerType) -> Unit,
    currentClientId: Int? = null,
    currentIssuerId: Int? = null,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    bottomFormOnValueChange: (ScreenElement, Any, ClientOrIssuerType?) -> Unit,
    bottomFormPlaceCursor: (ScreenElement, ClientOrIssuerType?) -> Unit,
    onClickDoneForm: (DocumentBottomSheetTypeOfForm, syncToMaster: Boolean) -> Unit,
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
    // NSC on the content column: when at fullscreen (isSheetExpanded=true)
    // and the internal verticalScroll surfaces a downward leftover (user
    // scrolled past the top of the content), we consume it and trigger the
    // half-height collapse. That way the fullscreen sheet's first
    // scroll-down step is a mode switch, not an immediate dismissal. Once
    // in half mode, this NSC is inert (condition false) → next leftover
    // propagates to the sheet's anchoredDraggable as usual (drag-to-dismiss).
    val collapseOnFullscreenScrollDown = remember(isSheetExpanded) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (isSheetExpanded && source == NestedScrollSource.UserInput &&
                    available.y > 0f
                ) {
                    onCollapseToHalf()
                    return available
                }
                return Offset.Zero
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(sheetContentHeight)
            .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
            .nestedScroll(collapseOnFullscreenScrollDown)
    ) {
        val slideOtherComponent: MutableState<ScreenElement?> = remember { mutableStateOf(null) }

        // Back inside a slid-in sub-screen (client/issuer list, date picker, footer…)
        // returns to the elements list instead of dismissing the whole sheet.
        PlatformBackHandler(enabled = slideOtherComponent.value != null) {
            slideOtherComponent.value = null
        }

        Box(
            modifier = Modifier
                .background(Color.Transparent)
                .fillMaxSize()
                .dismissKeyboardOnUnconsumedTap()
                .focusable(false)
        ) {
            val elementsScrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(elementsScrollState)
                    .padding(bottom = 24.dp)
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
                    localFocusManager = localFocusManager,
                    clients = clients,
                    issuers = issuers,
                    // The NavGraph's onClickEditDocumentClientOrIssuer already
                    // routes through checkVersionMismatch; passing
                    // openFormOnCompletion = false makes it a refresh-only
                    // action (no form pops after the dialog).
                    onRefreshClientOrIssuer = { snap ->
                        onClickEditDocumentClientOrIssuer(snap, false)
                    },
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
                    ScreenElement.DOCUMENT_VAT_EXEMPTION -> {
                        document.vatExemptionText ?: androidx.compose.ui.text.input.TextFieldValue()
                    }
                    ScreenElement.DOCUMENT_PAYMENT_MEANS -> when (document) {
                        // PaymentPickerParams — see the data class definition below.
                        // Bundles segments + hidden flag + issuer info (for the IBAN
                        // dropdown) so the picker branch can fetch banks + show the
                        // right pre-selection without extra plumbing.
                        is InvoiceState -> PaymentPickerParams(
                            segments = document.paymentMeansSegments,
                            hidden = document.paymentMeansHidden,
                            masterIssuerId = document.documentIssuer?.originalClientOrIssuerId?.toLong(),
                            selectedIban = document.documentIssuer?.paymentIban?.text,
                            bankHidden = document.paymentBankHidden,
                            hasIssuer = document.documentIssuer != null,
                            otherChecked = document.paymentMeansOtherChecked,
                            bankSegments = document.paymentBankSegments,
                            bankIban = document.documentIssuer?.paymentIban?.text?.trim().orEmpty(),
                            bankBic = document.documentIssuer?.paymentBic?.text?.trim().orEmpty(),
                            bankCountry = document.documentIssuer?.paymentCountry,
                        )
                        // Avoir dropped: the payment picker isn't reachable
                        // on a credit note anymore (row removed from the form).
                        else -> PaymentPickerParams(
                            segments = emptyList(),
                            hidden = false,
                            masterIssuerId = null,
                            selectedIban = null,
                            bankHidden = false,
                            hasIssuer = false,
                        )
                    }
                    // DOCUMENT_PAYMENT_TERMS opens the 3-row picker. The
                    // picker branch reads the 3 current values from
                    // PaymentTermsPickerParams to seed each sub-editor.
                    ScreenElement.DOCUMENT_PAYMENT_TERMS -> when (document) {
                        is InvoiceState -> PaymentTermsPickerParams(
                            recoveryFees = document.paymentTermsRecoveryFees,
                            lateFees = document.paymentTermsLateFees,
                            discount = document.paymentTermsDiscount,
                        )
                        else -> PaymentTermsPickerParams(
                            recoveryFees = androidx.compose.ui.text.input.TextFieldValue(),
                            lateFees = androidx.compose.ui.text.input.TextFieldValue(),
                            discount = androidx.compose.ui.text.input.TextFieldValue(),
                        )
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
                onClickDoneForm = { type, syncToMaster ->
                    slideOtherComponent.value = null
                    onClickDoneForm(type, syncToMaster)
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
