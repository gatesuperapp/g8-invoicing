package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.shared.BatAnimation
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.shared.ButtonAddOrChoose
import com.a4a.g8invoicing.ui.theme.textSmall

// Bottom sheet with "New product" and "Choose in list" buttons
// And the list of chosen products
@Composable
fun DocumentBottomSheetDocumentProductListChosen(
    list: List<DocumentProductState>,
    onClickNew: () -> Unit, // Add a new product to the document (product list)
    onClickChooseExisting: () -> Unit, // Add a new product to the document (product list)
    onClickDocumentProduct: (DocumentProductState) -> Unit, // Edit an existing document product (add/edit screen)
    onClickDelete: (Int) -> Unit, // Delete a document product,
    isClientOrIssuerListEmpty: Boolean,
    onOrderChange: (List<DocumentProductState>) -> Unit
    ) {
    Column(
        modifier = Modifier
            .fillMaxHeight(0.5f)
            .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
    ) {
        // Header: display "back" button
        Spacer(modifier = Modifier.height(50.dp))

        ButtonAddOrChoose(
            onClickNew,
            hasBorder = true,
            isPickerButton = false,
            stringResource(id = R.string.document_bottom_sheet_list_add_new_product)
        )
        if(!isClientOrIssuerListEmpty) {
            ButtonAddOrChoose( // Choosing a product to add to the document
                onClickChooseExisting,
                hasBorder = false,
                isPickerButton = true,
                stringResource(id = R.string.document_bottom_sheet_document_product_add)
            )
        }
        // Display the list of chosen products
        DocumentBottomSheetProductListChosenContent(
            documentProducts = list,
            onClickItem = onClickDocumentProduct,
            onClickDelete = onClickDelete,
            onOrderChange = onOrderChange
        )

        if(list.size == 1) {
            DisplayBatHelperAdvice()
        }
    }
}


@Composable
private fun DisplayBatHelperAdvice() {
    var visibleText by remember { mutableIntStateOf(0) }
    val numberOfIterations = remember { mutableIntStateOf(4) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(top = 40.dp)
            .padding(
                start = 40.dp,
                end = 40.dp,
                bottom = 20.dp
            )
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = visibleText == 1,
            enter = fadeIn(tween(1000)),
            exit = fadeOut(tween(100)),
        ) {
            Text(
                text = Strings.get(R.string.document_product_advice),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.textSmall
            )
        }

        AnimatedVisibility(
            visible = visibleText == 2,
            enter = fadeIn(
                tween(
                    2000,
                    delayMillis = 100,
                    easing = LinearOutSlowInEasing
                )
            ),
            exit = fadeOut(tween(100)),
        ) {
            Text(
                text = Strings.get(R.string.document_product_advice_2),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.textSmall

            )
        }
        AnimatedVisibility(
            visible = visibleText == 3,
            enter = fadeIn(
                tween(
                    2000,
                    delayMillis = 100,
                    easing = LinearOutSlowInEasing
                )
            ),
            exit = fadeOut(tween(100)),
        ) {
            Text(
                text = Strings.get(R.string.document_product_advice_3),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.textSmall

            )
        }

        Box(
            Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() } // This is mandatory
            ) {
                if (visibleText < 3) {
                    visibleText += 1
                } else visibleText = 0
                numberOfIterations.intValue += 1
            }
        ) {
            BatAnimation(
                modifier = Modifier
                    .width(80.dp)
                    .height(50.dp)
                    .align(Alignment.Center),
                file = R.raw.bat_wavy_arms,
                numberOfIteration = numberOfIterations.intValue
            )
        }


    }
}
