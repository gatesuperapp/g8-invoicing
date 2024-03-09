package com.a4a.g8invoicing.ui.screens

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.navigation.actionDone
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ProductState

// Can be Product or DocumentProduct. Names are "Product" to simplify.
@Composable
fun ProductAddEdit(
    navController: NavController,
    product: ProductState,
    isNew: Boolean,
    onValueChange: (ScreenElement, Any) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    onClickDone: () -> Unit,
    onClickBack: () -> Unit,
    onClickForward: (ScreenElement) -> Unit,
) {
    Scaffold(
        topBar = {
            TopBar(
                isNewProduct = isNew,
                navController = navController,
                onClickDone = onClickDone,
                onClickBackArrow = onClickBack
            )
        }
    ) {
        it
        ProductAddEditForm(
            product,
            onValueChange,
            placeCursorAtTheEndOfText,
            onClickForward
        )
    }
}


@Composable
private fun TopBar(
    isNewProduct: Boolean,
    navController: NavController,
    onClickDone: () -> Unit,
    onClickBackArrow: () -> Unit,
) {
    TopBar(
        title = if (isNewProduct) {
            R.string.appbar_product_add
        } else {
            R.string.appbar_product_detail
        },
        actionDone(
            onClick = onClickDone
        ),
        navController = navController,
        onClickBackArrow = onClickBackArrow
    )
}