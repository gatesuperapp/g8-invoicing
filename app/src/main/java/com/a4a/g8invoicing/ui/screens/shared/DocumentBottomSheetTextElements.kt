package com.a4a.g8invoicing.ui.screens.shared

import android.R.attr.contentDescription
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.keyboardAsState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType
import java.math.BigDecimal


@Composable
fun DocumentBottomSheetTextElements(
    document: DocumentState,
    onDismissBottomSheet: () -> Unit,
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
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val keyboard by keyboardAsState()

    Column(
        // We add this column to be able to apply "fillMaxHeight" to the components that slide in
        // If we don't constrain the parent (=this column) width, components that slide in
        // fill the screen full height
    ) {
        val slideOtherComponent: MutableState<ScreenElement?> = remember { mutableStateOf(null) }

        Box(
            modifier = Modifier.background(Color.Transparent)
        ) {
            // The order of these conditions is important for the focus requesters
            if (slideOtherComponent.value == null) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight(0.5f)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box( // CONTAINS "VALIDATE" ICON / "CLOSE SHEET" ICON
                            modifier = Modifier
                                .height(50.dp)
                                .width(70.dp)
                                .clickable {
                                    // Hides keyboard if it was opened
                                    if (keyboard.name == "Opened") {
                                        keyboardController?.hide()
                                    } else { // Hides bottom sheet
                                        onDismissBottomSheet()
                                    }
                                }) {
                            Icon(
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .size(30.dp)
                                    .align(alignment = Alignment.CenterEnd),
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "Close bottom sheet"
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
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
                }


            } else {
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
                    onClickDeleteAddress = onClickDeleteAddress
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
