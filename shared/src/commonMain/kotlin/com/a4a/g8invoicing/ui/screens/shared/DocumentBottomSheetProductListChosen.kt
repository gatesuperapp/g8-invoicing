package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.document_bottom_sheet_add_product
import com.a4a.g8invoicing.shared.resources.document_product_advice
import com.a4a.g8invoicing.ui.shared.ButtonAddOrChoose
import com.a4a.g8invoicing.ui.shared.animations.BatWavyArms
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.theme.textBodySmall
import org.jetbrains.compose.resources.stringResource

// Bottom sheet with the "Add a product" button (opens the picker with search + list)
// and the list of chosen products underneath.
@Composable
fun DocumentBottomSheetProductsChosen(
    list: List<DocumentProductState>,
    onClickChooseExisting: () -> Unit, // Opens the product picker bottom sheet
    onClickDocumentProduct: (DocumentProductState) -> Unit, // Edit an existing document product
    onClickDelete: (Int) -> Unit,
    onOrderChange: (List<DocumentProductState>) -> Unit
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        ButtonAddOrChoose(
            onClickChooseExisting,
            hasBorder = false,
            isPickerButton = true,
            stringResource(Res.string.document_bottom_sheet_add_product)
        )
        // Display the list of chosen products
        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
            DocumentBottomSheetProductListChosenContent(
                documentProducts = list,
                onClickItem = onClickDocumentProduct,
                onClickDelete = onClickDelete,
                onOrderChange = onOrderChange,
                modifier = Modifier.fillMaxSize(),
            )
        }

        if(list.size == 1) {
            DisplayBatHelperAdvice()
        }
    }
}


@Composable
private fun DisplayBatHelperAdvice() {
    var adviceVisible by remember { mutableStateOf(false) }
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
            visible = adviceVisible,
            enter = fadeIn(tween(500)),
            exit = fadeOut(tween(100)),
        ) {
            Text(
                text = stringResource(Res.string.document_product_advice),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.textBodySmall,
            )
        }

        Box(
            Modifier
                .padding(bottom = 32.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    adviceVisible = !adviceVisible
                    numberOfIterations.intValue += 1
                }
        ) {
            BatWavyArms(
                modifier = Modifier
                    .width(80.dp)
                    .height(50.dp)
                    .align(Alignment.Center),
                iterations = numberOfIterations.intValue
            )
        }
    }
}
