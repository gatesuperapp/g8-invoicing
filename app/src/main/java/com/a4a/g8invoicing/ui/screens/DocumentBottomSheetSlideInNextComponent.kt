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
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.ProductState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentBottomSheetSlideInNextComponent(
    pageElement: ScreenElement?,
    parameters: Any?,
    onClickBack: () -> Unit,
    clientUiState: DocumentClientOrIssuerState,
    issuerUiState: DocumentClientOrIssuerState,
    onClientOrIssuerClick: (ClientOrIssuerState) -> Unit,
    onProductClick: (ProductState) -> Unit,
    documentProductUiState: DocumentProductState,
    onDocumentProductClick: (DocumentProductState) -> Unit,
    onDocumentClientOrIssuerClick: (DocumentClientOrIssuerState) -> Unit,
    onClickDeleteDocumentProduct: (Int) -> Unit,
    onClickDeleteDocumentClientOrIssuer: (Int) -> Unit,
    datePickerState: DatePickerState,
    currentClientId: Int? = null,
    currentIssuerId: Int? = null,
    bottomFormOnValueChange: (ScreenElement, Any, ClientOrIssuerType?) -> Unit,
    productPlaceCursorAtTheEndOfText: (ScreenElement) -> Unit,
    onClickDoneForm: (DocumentBottomSheetTypeOfForm) -> Unit,
    onClickCancelForm: () -> Unit,
    onSelectTaxRate: (BigDecimal?) -> Unit,
) {
    var isProductListVisible by remember { mutableStateOf(false) }
    var isClientOrIssuerListVisible by remember { mutableStateOf(false) }
    var typeOfCreation: DocumentBottomSheetTypeOfForm by remember { mutableStateOf(DocumentBottomSheetTypeOfForm.ADD_PRODUCT) }
    var isDocumentFormVisible by remember { mutableStateOf(false) }
    var params: List<List<Any>?>? = null

    if (pageElement == ScreenElement.DOCUMENT_CLIENT || pageElement == ScreenElement.DOCUMENT_ISSUER) {
        parameters?.let {it ->
            val item = it as DocumentClientOrIssuerState
            DeliveryNoteBottomSheetDocumentClientOrIssuer(
                item =  item,
                onClickBack = onClickBack,
                onClickNewButton = {
                    typeOfCreation = if (pageElement == ScreenElement.DOCUMENT_CLIENT) {
                        DocumentBottomSheetTypeOfForm.NEW_CLIENT
                    } else DocumentBottomSheetTypeOfForm.NEW_ISSUER
                    isDocumentFormVisible = true
                },
                onClickChooseButton = { isClientOrIssuerListVisible = true },
                onClickItem = {
                    onDocumentClientOrIssuerClick(it)
                    typeOfCreation = DocumentBottomSheetTypeOfForm.EDIT_ITEM
                    isDocumentFormVisible = true
                },
                onClickDelete = onClickDeleteDocumentClientOrIssuer
            )

            if (isClientOrIssuerListVisible) {
                DeliveryNoteBottomSheetClientOrIssuerList(
                    list = parameters.let { it as List<ClientOrIssuerState> } ,
                    pageElement = pageElement,
                    onClickBack = onClickBack,
                    onClientOrIssuerClick =  {
                        onClientOrIssuerClick(it)
                        typeOfCreation = if (pageElement == ScreenElement.DOCUMENT_CLIENT) {
                            DocumentBottomSheetTypeOfForm.ADD_CLIENT
                        } else DocumentBottomSheetTypeOfForm.ADD_ISSUER
                        isDocumentFormVisible = true
                    },
                    currentClientId = currentClientId,
                    currentIssuerId = currentIssuerId
                )
            }
        }

    }

    if (pageElement == ScreenElement.DOCUMENT_DATE) {
        DeliveryNoteBottomSheetDatePicker(
            initialDate = parameters?.let { it as String } ?: "",
            datePickerState = datePickerState,
            onClickBack = onClickBack,
        )
    }

    if (pageElement == ScreenElement.DOCUMENT_PRODUCTS) {
        params = parameters as List<List<Any>?>

        DeliveryNoteBottomSheetDocumentProductList(
            list = params.first() as List<DocumentProductState>? ?: emptyList(),
            onClickBack = onClickBack,
            onClickNew = {
                typeOfCreation = DocumentBottomSheetTypeOfForm.NEW_PRODUCT
                isDocumentFormVisible = true
                CoroutineScope(Dispatchers.IO).launch {
                    delay(TimeUnit.MILLISECONDS.toMillis(500))
                    isProductListVisible = false
                }
            } ,
            onClickChooseExisting = { isProductListVisible = true },
            onClickDocumentProduct = {
                onDocumentProductClick(it)
                typeOfCreation = DocumentBottomSheetTypeOfForm.EDIT_ITEM
                isDocumentFormVisible = true
                /*CoroutineScope(Dispatchers.IO).launch {
                    delay(TimeUnit.MILLISECONDS.toMillis(500))
                    isProductListVisible = false
                }*/
            },
            onClickDelete = onClickDeleteDocumentProduct
        )

        if (isProductListVisible) {
            DeliveryNoteBottomSheetProductList(
                list = params[1] as List<ProductState>? ?: emptyList(),
                onClickBack = { isProductListVisible = false },
                onProductClick = {
                    onProductClick(it) // Update the ProductAddEditViewModel with the chosen product
                    // so we open bottom document form with the chosen product
                    typeOfCreation = DocumentBottomSheetTypeOfForm.ADD_PRODUCT
                    isDocumentFormVisible = true
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(TimeUnit.MILLISECONDS.toMillis(500))
                        // Waits for the bottom form to be opened,
                        // so previous screen change is in background
                        isProductListVisible = false
                    }
                }
            )
        }
    }
    if (isDocumentFormVisible) {
        DocumentBottomSheetFormModal(
            typeOfCreation = typeOfCreation,
            clientUiState = clientUiState,
            issuerUiState = issuerUiState,
            documentProduct = documentProductUiState,
            taxRates = params?.get(2) as List<BigDecimal>?,
            bottomFormOnValueChange = bottomFormOnValueChange,
            productPlaceCursorAtTheEndOfText = productPlaceCursorAtTheEndOfText,
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