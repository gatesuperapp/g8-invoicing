package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.img_paid
import com.a4a.g8invoicing.shared.resources.invoice_paid
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


@Composable
fun DocumentBasicTemplateContent(
    document: DocumentState,
    onClickElement: (ScreenElement) -> Unit,
    onClickRestOfThePage: () -> Unit,
    screenWidth: Dp,
    productArray: List<DocumentProductState>?,
    prices: List<String>,
    selectedItem: ScreenElement? = null,
) {
    val pagePadding = 24.dp

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
            .customCombinedClickable(
                onClick = {
                    onClickRestOfThePage()
                }
            )
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

            if (!document.reference?.text.isNullOrEmpty()) {
                document.reference?.let {
                    Spacer(Modifier.height(14.dp))
                    if (it.text.isNotEmpty())
                        DocumentBasicTemplateReference(
                            it.text,
                            onClickElement,
                            selectedItem
                        )
                }
            }
            if (!document.freeField?.text.isNullOrEmpty()) {
                document.freeField?.let {
                    if (!document.reference?.text.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                    } else Spacer(Modifier.height(10.dp))
                    if (it.text.isNotEmpty())
                        DocumentBasicTemplateFreeField(
                            it.text,
                            onClickElement,
                            selectedItem
                        )
                }
            }

            if (document.freeField?.text.isNullOrEmpty() && document.reference?.text.isNullOrEmpty())
                Spacer(Modifier.height(20.dp))
            else Spacer(Modifier.height(10.dp))

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
                        DocumentBasicTemplateProductsTable(
                            productArray
                        )
                    }
                }
                if (document is InvoiceState && document.paymentStatus == 2) {
                    Image(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .width(100.dp),
                        painter = painterResource(Res.drawable.img_paid),
                        contentDescription = stringResource(Res.string.invoice_paid),
                    )
                }
            }
            DocumentBasicTemplateTotalPrices(document, prices)

            DocumentBasicTemplateFooter(
                document,
                onClickElement = { onClickElement(ScreenElement.DOCUMENT_FOOTER) }
            )
        }
    }
}
