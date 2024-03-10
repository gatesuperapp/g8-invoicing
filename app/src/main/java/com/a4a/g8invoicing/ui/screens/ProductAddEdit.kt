package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.navigation.actionDone
import com.a4a.g8invoicing.ui.shared.DecimalInput
import com.a4a.g8invoicing.ui.shared.FormInput
import com.a4a.g8invoicing.ui.shared.FormUI
import com.a4a.g8invoicing.ui.shared.ForwardElement
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.TextInput
import com.a4a.g8invoicing.ui.states.ProductState
import java.math.BigDecimal
import java.math.RoundingMode

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
            onClickForward,
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