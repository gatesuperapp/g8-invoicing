package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

@Composable
fun DocumentBottomSheetElementsAfterSlide(
    pageElement: ScreenElement?,
    parameters: Any?,
    onClickBack: () -> Unit,
    documentClientUiState: ClientOrIssuerState,
    documentIssuerUiState: ClientOrIssuerState,
    taxRates: List<BigDecimal>,
    onClickClientOrIssuer: (ClientOrIssuerState) -> Unit,
    onClickNewDocumentClientOrIssuer: (ClientOrIssuerType) -> Unit,
    onClickDocumentClientOrIssuer: (ClientOrIssuerState) -> Unit,
    onClickDeleteDocumentClientOrIssuer: (ClientOrIssuerType) -> Unit,
    currentClientId: Int? = null,
    currentIssuerId: Int? = null,
    bottomFormOnValueChange: (ScreenElement, Any, ClientOrIssuerType?) -> Unit,
    bottomFormPlaceCursor: (ScreenElement, ClientOrIssuerType?) -> Unit,
    onClickDoneForm: (DocumentBottomSheetTypeOfForm) -> Unit,
    onClickCancelForm: () -> Unit,
    onSelectTaxRate: (BigDecimal?) -> Unit,
    showDocumentForm: Boolean = false,
    onShowDocumentForm: (Boolean) -> Unit,
    onValueChange: (ScreenElement, Any) -> Unit,
    onClickDeleteAddress: (ClientOrIssuerType) -> Unit,

    ) {
    var isClientOrIssuerListVisible by remember { mutableStateOf(false) }
    var typeOfCreation: DocumentBottomSheetTypeOfForm by remember {
        mutableStateOf(
            DocumentBottomSheetTypeOfForm.ADD_EXISTING_PRODUCT
        )
    }

    if (pageElement == ScreenElement.DOCUMENT_CLIENT || pageElement == ScreenElement.DOCUMENT_ISSUER) {
        DocumentBottomSheetClientOrIssuerPreview(
            pageElement = pageElement,
            clientOrIssuer = (parameters as Pair<ClientOrIssuerState?, List<ClientOrIssuerState>>).first,
            onClickBack = onClickBack,
            onClickNewButton = {
                onClickNewDocumentClientOrIssuer(
                    if (pageElement == ScreenElement.DOCUMENT_CLIENT) ClientOrIssuerType.DOCUMENT_CLIENT
                    else ClientOrIssuerType.DOCUMENT_ISSUER
                )
                typeOfCreation = if (pageElement == ScreenElement.DOCUMENT_CLIENT) {
                    DocumentBottomSheetTypeOfForm.NEW_CLIENT
                } else DocumentBottomSheetTypeOfForm.NEW_ISSUER
                onShowDocumentForm(true)
            },
            onClickChooseButton = { isClientOrIssuerListVisible = true },
            onClickItem = {
                onClickDocumentClientOrIssuer(it)
                typeOfCreation = if (pageElement == ScreenElement.DOCUMENT_CLIENT)
                    DocumentBottomSheetTypeOfForm.EDIT_CLIENT else DocumentBottomSheetTypeOfForm.EDIT_ISSUER
                onShowDocumentForm(true)
            },
            onClickDelete = onClickDeleteDocumentClientOrIssuer,
            isClientOrIssuerListEmpty = parameters.second.isEmpty()
        )

        if (isClientOrIssuerListVisible) {
            DocumentBottomSheetClientOrIssuerList(
                list = parameters.second,
                pageElement = pageElement,
                onClickBack = { isClientOrIssuerListVisible = false },
                onClientOrIssuerClick = {
                    onClickClientOrIssuer(it) // Update the AddEditViewModel with the chosen item
                    // so we open bottom document form with data
                    typeOfCreation = if (pageElement == ScreenElement.DOCUMENT_CLIENT) {
                        DocumentBottomSheetTypeOfForm.ADD_EXISTING_CLIENT
                    } else DocumentBottomSheetTypeOfForm.ADD_EXISTING_ISSUER
                    onShowDocumentForm(true)
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(TimeUnit.MILLISECONDS.toMillis(500))
                        // Waits for the bottom form to be opened,
                        // so previous screen change is in background
                        isClientOrIssuerListVisible = false
                    }
                },
                currentClientId = currentClientId,
                currentIssuerId = currentIssuerId
            )
        }
    }

    if (pageElement == ScreenElement.DOCUMENT_DATE) {
        DocumentBottomSheetFormSimple(
            onClickCancel = onClickBack,
            bottomSheetTitle = Strings.get(R.string.document_date_emitting),
            content = {
                DocumentBottomSheetDatePicker(
                    initialDate = parameters.let { it as String },
                    onValueChange = {
                        onValueChange(ScreenElement.DOCUMENT_DATE, it)
                        onClickBack()
                    }
                )
            },
            isDatePicker = true,
            screenElement = ScreenElement.DOCUMENT_DATE
        )
    }

    if (pageElement == ScreenElement.DOCUMENT_DUE_DATE) {
        DocumentBottomSheetFormSimple(
            onClickCancel = onClickBack,
            bottomSheetTitle = Strings.get(R.string.document_due_date),
            content = {
                DocumentBottomSheetDatePicker(
                    initialDate = parameters.let { it as String },
                    onValueChange = {
                        onValueChange(ScreenElement.DOCUMENT_DUE_DATE, it)
                        onClickBack()
                    }
                )
            },
            isDatePicker = true,
            screenElement = ScreenElement.DOCUMENT_DUE_DATE
        )
    }

    if (pageElement == ScreenElement.DOCUMENT_FOOTER) {
        var footerText by remember { mutableStateOf(TextFieldValue("")) }
        var showBottomSheet by remember { mutableStateOf(true) }

        if (showBottomSheet)
            DocumentBottomSheetFormSimple(
                onClickCancel = {
                    onClickBack()
                    showBottomSheet = false
                },
                onClickDone = {
                    onValueChange(it, footerText)
                  //  onClickBack()
                    showBottomSheet = false
                },
                bottomSheetTitle = Strings.get(R.string.document_footer),
                content = {
                    DocumentBottomSheetLargeText(
                        initialText = parameters.let { it as TextFieldValue },
                        onValueChange = {
                            footerText = it
                        }
                    )
                },
                screenElement = ScreenElement.DOCUMENT_FOOTER
            )
    }

    if (showDocumentForm) {
        DocumentBottomSheetForm(
            typeOfCreation = typeOfCreation,
            documentClientUiState = documentClientUiState,
            documentIssuerUiState = documentIssuerUiState,
            taxRates = taxRates,
            bottomFormOnValueChange = bottomFormOnValueChange,
            bottomFormPlaceCursor = bottomFormPlaceCursor,
            onClickCancel = { // Re-initialize
                onClickCancelForm()
                onShowDocumentForm(false)
            },
            onClickDone = {
                onClickDoneForm(typeOfCreation)
            },
            onSelectTaxRate = onSelectTaxRate,
            onClickDeleteAddress = onClickDeleteAddress
        )
    }
}