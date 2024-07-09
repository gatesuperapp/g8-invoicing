package com.a4a.g8invoicing.ui.screens

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentClientOrIssuerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentBottomSheetElementsAfterSlide(
    pageElement: ScreenElement?,
    parameters: Any?,
    onClickBack: () -> Unit,
    documentClientUiState: DocumentClientOrIssuerState,
    documentIssuerUiState: DocumentClientOrIssuerState,
    taxRates: List<BigDecimal>,
    onClickClientOrIssuer: (ClientOrIssuerState) -> Unit,
    onClickDocumentClientOrIssuer: (DocumentClientOrIssuerState) -> Unit,
    onClickDeleteDocumentClientOrIssuer: (Int) -> Unit,
    datePickerState: DatePickerState,
    currentClientId: Int? = null,
    currentIssuerId: Int? = null,
    bottomFormOnValueChange: (ScreenElement, Any, ClientOrIssuerType?) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    onClickDoneForm: (DocumentBottomSheetTypeOfForm) -> Unit,
    onClickCancelForm: () -> Unit,
    onSelectTaxRate: (BigDecimal?) -> Unit,
) {
    var isClientOrIssuerListVisible by remember { mutableStateOf(false) }
    var typeOfCreation: DocumentBottomSheetTypeOfForm by remember {
        mutableStateOf(
            DocumentBottomSheetTypeOfForm.ADD_PRODUCT
        )
    }
    var isDocumentFormVisible by remember { mutableStateOf(false) }

    if (pageElement == ScreenElement.DOCUMENT_CLIENT || pageElement == ScreenElement.DOCUMENT_ISSUER) {
        val params = parameters as Pair<DocumentClientOrIssuerState?, List<ClientOrIssuerState>>
        DeliveryNoteBottomSheetDocumentClientOrIssuer(
            item = params.first,
            onClickBack = onClickBack,
            onClickNewButton = {
                typeOfCreation = if (pageElement == ScreenElement.DOCUMENT_CLIENT) {
                    DocumentBottomSheetTypeOfForm.NEW_CLIENT
                } else DocumentBottomSheetTypeOfForm.NEW_ISSUER
                isDocumentFormVisible = true
            },
            onClickChooseButton = { isClientOrIssuerListVisible = true },
            onClickItem = {
                onClickDocumentClientOrIssuer(it)
                typeOfCreation = if (pageElement == ScreenElement.DOCUMENT_CLIENT)
                    DocumentBottomSheetTypeOfForm.EDIT_CLIENT else  DocumentBottomSheetTypeOfForm.EDIT_ISSUER
                isDocumentFormVisible = true
            },
            onClickDelete = onClickDeleteDocumentClientOrIssuer
        )

        if (isClientOrIssuerListVisible) {
            DeliveryNoteBottomSheetClientOrIssuerList(
                list = params.second,
                pageElement = pageElement,
                onClickBack = { isClientOrIssuerListVisible = false },
                onClientOrIssuerClick = {
                    onClickClientOrIssuer(it) // Update the AddEditViewModel with the chosen item
                    // so we open bottom document form with data
                    typeOfCreation = if (pageElement == ScreenElement.DOCUMENT_CLIENT) {
                        DocumentBottomSheetTypeOfForm.ADD_CLIENT
                    } else DocumentBottomSheetTypeOfForm.ADD_ISSUER
                    isDocumentFormVisible = true
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
        DeliveryNoteBottomSheetDatePicker(
            initialDate = parameters.let { it as String },
            datePickerState = datePickerState,
            onClickBack = onClickBack,
        )
    }



    if (isDocumentFormVisible) {
        DocumentBottomSheetFormModal(
            typeOfCreation = typeOfCreation,
            documentClientUiState = documentClientUiState,
            documentIssuerUiState = documentIssuerUiState,
            taxRates = taxRates,
            bottomFormOnValueChange = bottomFormOnValueChange,
            placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
            onClickCancel = { // Re-initialize
                isDocumentFormVisible = false
                onClickCancelForm()
            },
            onClickDone = {
                onClickDoneForm(typeOfCreation)
                isDocumentFormVisible = false
            },
            onSelectTaxRate = onSelectTaxRate
        )
    }

}