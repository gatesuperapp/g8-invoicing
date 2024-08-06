package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentClientOrIssuerState
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.reflect.Modifier
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
    onClickDeleteDocumentClientOrIssuer: (ClientOrIssuerType) -> Unit,
    datePickerState: DatePickerState,
    dueDatePickerState: DatePickerState?,
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
) {
    var isClientOrIssuerListVisible by remember { mutableStateOf(false) }
    var typeOfCreation: DocumentBottomSheetTypeOfForm by remember {
        mutableStateOf(
            DocumentBottomSheetTypeOfForm.ADD_PRODUCT
        )
    }

    if (pageElement == ScreenElement.DOCUMENT_CLIENT || pageElement == ScreenElement.DOCUMENT_ISSUER) {
        val params = parameters as Pair<DocumentClientOrIssuerState?, List<ClientOrIssuerState>>
        DocumentBottomSheetDocumentClientOrIssuer(
            item = params.first,
            onClickBack = onClickBack,
            onClickNewButton = {
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
            onClickDelete = onClickDeleteDocumentClientOrIssuer
        )

        if (isClientOrIssuerListVisible) {
            DocumentBottomSheetClientOrIssuerList(
                list = params.second,
                pageElement = pageElement,
                onClickBack = { isClientOrIssuerListVisible = false },
                onClientOrIssuerClick = {
                    onClickClientOrIssuer(it) // Update the AddEditViewModel with the chosen item
                    // so we open bottom document form with data
                    typeOfCreation = if (pageElement == ScreenElement.DOCUMENT_CLIENT) {
                        DocumentBottomSheetTypeOfForm.ADD_CLIENT
                    } else DocumentBottomSheetTypeOfForm.ADD_ISSUER
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
        DocumentBottomSheetDatePicker(
            initialDate = parameters.let { it as String },
            datePickerState = datePickerState,
            onClickBack = onClickBack,
        )
    }

    if (pageElement == ScreenElement.DOCUMENT_DUE_DATE) {
        dueDatePickerState?.let {
            DocumentBottomSheetDatePicker(
                initialDate = parameters.let { it as String },
                datePickerState = it,
                onClickBack = onClickBack,
            )
        }
    }

    if (pageElement == ScreenElement.DOCUMENT_FOOTER) {
        DocumentBottomSheetFooter(
            text = parameters.let { it as TextFieldValue },
            onValueChange = onValueChange,
            onClickBack = onClickBack,
        )
    }



    if (showDocumentForm) {
        DocumentBottomSheetFormModal(
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
            onSelectTaxRate = onSelectTaxRate
        )
    }

}