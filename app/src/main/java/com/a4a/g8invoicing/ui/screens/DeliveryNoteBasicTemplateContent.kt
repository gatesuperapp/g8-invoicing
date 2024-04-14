package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.theme.ColorGreenPaidCompl
import com.a4a.g8invoicing.ui.theme.textForDocumentsImportant
import java.math.BigDecimal

@Composable
fun DeliveryNoteBasicTemplateContent(
    uiState: DeliveryNoteState,
    onClickElement: (ScreenElement) -> Unit,
    screenWidth: Dp,
    productArray: MutableList<ProductListWithPage>,
    footerArray: MutableList<FooterRows>,
    isFirstPage: Boolean = false,
    onPageOverflow: () -> Unit,
    selectedItem: ScreenElement? = null,
) {
    val pagePadding = 20.dp
    var pageHeightDp by remember {
        mutableStateOf(0.dp)
    }
    var pageContentHeightDp by remember {
        mutableStateOf(0.dp)
    }
    val localDensity = LocalDensity.current

    Box(
        modifier = Modifier
            .width(screenWidth)
            .padding(
                start = pagePadding,
                top = pagePadding,
                bottom = pagePadding,
                end = pagePadding
            )
            .background(Color.LightGray)
            .aspectRatio(1f / 1.414f)
            .onGloballyPositioned { coordinates ->
                pageHeightDp = with(localDensity) { coordinates.size.height.toDp() }
            }
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    top = 20.dp,
                    bottom = 20.dp,
                    end = 20.dp
                )
                .background(Color.White)
                .onGloballyPositioned { coordinates ->
                    pageContentHeightDp = with(localDensity) { coordinates.size.height.toDp() }

                }
        ) {
            if (pageContentHeightDp != 0.dp && (pageContentHeightDp > pageHeightDp - 42.dp)) {
                pageContentHeightDp = 0.dp
                onPageOverflow()
            }

            if (isFirstPage) {
                DeliveryNoteBasicTemplateHeader(uiState, onClickElement, selectedItem)
                DeliveryNoteBasicTemplateDocNumber(
                    uiState.orderNumber,
                    onClickElement,
                    selectedItem
                )
            }

            Spacer(
                modifier = Modifier
                    .padding(bottom = 6.dp)
            )

            Column(
                Modifier
                    .getBorder(ScreenElement.DOCUMENT_PRODUCTS, selectedItem)
                    .customCombinedClickable(
                        onClick = {
                            onClickElement(ScreenElement.DOCUMENT_PRODUCTS)
                        },
                        onLongClick = {
                        }
                    )
                    .fillMaxWidth()
            ) {
                // The table with all line items
                DeliveryNoteBasicTemplateDataTable(
                    productArray.map { it.documentProduct } ?: fakeDocumentProducts()
                )
            }

            DeliveryNoteBasicTemplateFooter(uiState, footerArray)

        }
    }
}

fun Modifier.getBorder(item: ScreenElement, selectedItem: ScreenElement?) = then(
    border(
        if (item == selectedItem) {
            BorderStroke(1.dp, ColorGreenPaidCompl)
        } else {
            BorderStroke(0.dp, Color.Transparent)
        }
    )
)


// Remove the indicator on click
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.customCombinedClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "combinedClickable"
        properties["enabled"] = enabled
        properties["onClickLabel"] = onClickLabel
        properties["role"] = role
        properties["onClick"] = onClick
        properties["onDoubleClick"] = onDoubleClick
        properties["onLongClick"] = onLongClick
        properties["onLongClickLabel"] = onLongClickLabel
    }
) {
    Modifier.combinedClickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        onLongClickLabel = onLongClickLabel,
        onLongClick = onLongClick,
        onDoubleClick = onDoubleClick,
        onClick = onClick,
        role = role,
        indication = null, // Removing the indicator on click
        interactionSource = remember { MutableInteractionSource() }
    )
}


@Composable
private fun fakeDocumentProducts() =
    listOf(
        DocumentProductState(
            id = 1,
            name = TextFieldValue(stringResource(id = R.string.delivery_note_default_document_product_name)),
            description = TextFieldValue(stringResource(id = R.string.delivery_note_default_document_product_description)),
            priceWithTax = BigDecimal(4),
            taxRate = BigDecimal(20),
            quantity = BigDecimal(2),
            unit = TextFieldValue(stringResource(id = R.string.delivery_note_default_document_product_unit)),
            productId = 1
        )
    )