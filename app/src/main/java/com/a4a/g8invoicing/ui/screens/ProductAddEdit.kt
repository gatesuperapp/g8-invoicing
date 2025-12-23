package com.a4a.g8invoicing.ui.screens

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.viewmodels.ProductAddEditViewModel

@Composable
fun ProductAddEdit(
    navController: NavController,
    viewModel: ProductAddEditViewModel = hiltViewModel(),
    product: ProductState,
    onValueChange: (ScreenElement, Any,  String?) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    onClickDone: () -> Unit,
    onClickBack: () -> Unit,
    onClickForward: (ScreenElement) -> Unit,
    onClickDeletePrice: (String) -> Unit,
    onClickAddPrice: () -> Unit,

    ) {
    val isLoading by viewModel.isLoading.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            TopBar(
                ctaText = Strings.get(R.string.document_modal_product_save),
                ctaTextDisabled = product.name.text.isNotEmpty(),
                navController = navController,
                onClickBackArrow = {
                    keyboardController?.hide() // fermer le clavier
                    onClickBack()

                },
                isCancelCtaDisplayed = true,
                onClickCtaValidate = {
                    keyboardController?.hide() // fermer le clavier
                    onClickDone()
                }
            )
        }
    ) {
        it
        ProductAddEditForm(
            product,
            onValueChange,
            placeCursorAtTheEndOfText,
            onClickForward,
            onClickDeletePrice,
            onClickAddPrice,
            isLoading
        )
    }
}


