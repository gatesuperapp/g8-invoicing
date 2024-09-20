package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.theme.ColorGreenPaidCompl


@Composable
fun DocumentBasicTemplateContent(
    document: DocumentState,
    onClickElement: (ScreenElement) -> Unit,
    screenWidth: Dp,
    productArray: List<DocumentProductState>?,
    prices: List<String>,
    selectedItem: ScreenElement? = null,
) {
    val pagePadding = 20.dp

    Box(
        modifier = Modifier
            .width(screenWidth)
            .padding(
                start = pagePadding,
                top = pagePadding,
                bottom = pagePadding,
                end = pagePadding
            )
            .background(Color.White)
            .heightIn(min = screenWidth * 1.28f)
        //.aspectRatio(1f / 1.414f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    top = 30.dp,
                    bottom = 20.dp,
                    end = 20.dp
                )
                .background(Color.White)
        ) {

            DocumentBasicTemplateHeader(document, onClickElement, selectedItem)

            document.reference?.let {
                if (it.text.isNotEmpty())
                    DocumentBasicTemplateReference(
                        it.text,
                        onClickElement,
                        selectedItem
                    )
            }
            document.freeField?.let {
                if(document.reference != null ) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
                if (it.text.isNotEmpty())
                    DocumentBasicTemplateFreeField(
                        it.text,
                        onClickElement,
                        selectedItem
                    )
            }

            if(document.reference == null && document.freeField == null) {
                Spacer(modifier = Modifier.height(20.dp))
            }

            Spacer(
                modifier = Modifier
                    .padding(bottom = 6.dp)
            )

            Box(
            ) {
                Column(
                    Modifier
                        .getBorder(ScreenElement.DOCUMENT_PRODUCT, selectedItem) // for selection
                        .customCombinedClickable(
                            onClick = {
                                onClickElement(ScreenElement.DOCUMENT_PRODUCT)
                            },
                            onLongClick = {
                            }
                        )
                        .fillMaxWidth()
                ) {
                    if (!productArray.isNullOrEmpty()) {
                        DocumentBasicTemplateDataTable(
                            productArray
                        )
                    }
                }
                if (document is InvoiceState && document.paymentStatus == 2) {
                    Image(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .width(100.dp),
                        painter = painterResource(R.drawable.img_paid),
                        contentDescription = Strings.get(R.string.invoice_paid),
                    )
                }
            }
            DocumentBasicTemplatePrices(document, prices)

            DocumentBasicTemplateFooter(
                document,
                onClickElement = { onClickElement(ScreenElement.DOCUMENT_FOOTER) }
            )

        }
    }
}

/*@Composable
fun DeliveryNoteBasicTemplatePageNumbering(index: Int, numberOfPages: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp, bottom = 10.dp, end = 10.dp)
    ) {
        Text(
            style = MaterialTheme.typography.textForDocuments,
            modifier = Modifier.align(Alignment.BottomEnd),
            text = if (numberOfPages == 1) {
                ""
            } else {
                "" + (index + 1) + "/" + numberOfPages
            }
        )
    }
}*/

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

