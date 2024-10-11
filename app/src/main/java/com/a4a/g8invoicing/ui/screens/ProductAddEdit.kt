package com.a4a.g8invoicing.ui.screens

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ProductState

@Composable
fun ProductAddEdit(
    navController: NavController,
    product: ProductState,
    onValueChange: (ScreenElement, Any) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    onClickDone: () -> Unit,
    onClickBack: () -> Unit,
    onClickForward: (ScreenElement) -> Unit,
) {
    Scaffold(
        topBar = {
            ProductAddEditTopBar(
                navController = navController,
                onClickDone = onClickDone,
                onClickBackArrow = onClickBack,
                isValidateCtaDisabled = product.name.text.isNotEmpty()
            )
        }
    ) {
        it
        ProductAddEditForm(
            product,
            onValueChange,
            placeCursorAtTheEndOfText,
            onClickForward,
        )
    }
}


@Composable
private fun ProductAddEditTopBar(
    navController: NavController,
    onClickDone: () -> Unit,
    onClickBackArrow: () -> Unit,
    isValidateCtaDisabled: Boolean,

    ) {
    TopBar(
        ctaText = Strings.get(R.string.document_modal_product_save),
        ctaTextDisabled = isValidateCtaDisabled,
        navController = navController,
        onClickBackArrow = onClickBackArrow,
        isCancelCtaDisplayed = true,
        onClickCtaValidate = onClickDone
    )
}