package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.theme.textForDocuments
import com.a4a.g8invoicing.ui.theme.textForDocumentsBold
import java.math.BigDecimal
import java.math.RoundingMode

// We use leftBorder, rightBorder, BottomBorder & TopBorder, because if we used simple "border"
// and apply it to the rows, borders would be doubled (except on top, left and right)
// It would give a thicker border in the table middle, we don't want that

val borderWidth = 0.7.dp
@Composable
fun DocumentBasicTemplateDataTable(
    tableData: List<DocumentProductState>,
) {
    val descriptionColumnWeight = .8f
    val quantityColumnWeight = .15f
    val taxColumnWeight = .15f
    val linkedNoteColumnWeight = .100f

    TitleRows(descriptionColumnWeight, quantityColumnWeight)

    val linkedDeliveryNotes = getLinkedDeliveryNotes(tableData)

    if (linkedDeliveryNotes.isNotEmpty()) {
        linkedDeliveryNotes.forEach {docNumberAndDate ->
            LinkedDeliveryNoteRow(linkedNoteColumnWeight, docNumberAndDate)
            DocumentProductsRows(
                tableData.filter { it.linkedDocNumber == docNumberAndDate.first },
                descriptionColumnWeight,
                quantityColumnWeight,
                taxColumnWeight
            )
        }
    } else {
        DocumentProductsRows(
            tableData,
            descriptionColumnWeight,
            quantityColumnWeight,
            taxColumnWeight
        )
    }
}

@Composable
fun TitleRows(
    descriptionColumnWeight: Float,
    quantityColumnWeight: Float,
) {
    Row(
        Modifier
            .topBorder(borderWidth, Color.LightGray)
            .fillMaxWidth()
            .height(IntrinsicSize.Min), // without it we can't add fillMaxHeight to columns
        horizontalArrangement = Arrangement.End
    ) {
        TableCell(
            text = stringResource(id = R.string.document_table_description),
            weight = descriptionColumnWeight,
            alignEnd = false,
            isBold = true
        )
        TableCell(
            text = stringResource(id = R.string.document_table_quantity),
            weight = quantityColumnWeight,
            alignEnd = true,
            isBold = true
        )
        TableCell(
            text = stringResource(id = R.string.document_table_unit),
            alignEnd = true,
            isBold = true
        )
        TableCell(
            text = stringResource(id = R.string.document_table_tax_rate),
            weight = quantityColumnWeight,
            alignEnd = true,
            isBold = true
        )
        TableCell(
            text = stringResource(id = R.string.document_table_unit_price_without_tax),
            alignEnd = true,
            isBold = true
        )
        TableCell(
            text = stringResource(id = R.string.document_table_total_price_without_tax),
            alignEnd = true,
            isBold = true
        )
    }
}

fun getLinkedDeliveryNotes(tableData: List<DocumentProductState>): MutableList<Pair<String?, String?>> {
    val linkedDeliveryNotes: MutableList<Pair<String?, String?>> = mutableListOf()

    if (tableData.any { !it.linkedDocNumber.isNullOrEmpty() }) {
        tableData.map { it.linkedDocNumber }.distinct().forEach { docNumber ->
            linkedDeliveryNotes.add(
                Pair(
                    docNumber,
                    tableData.firstOrNull { it.linkedDocNumber == docNumber }?.linkedDate
                )
            )
        }
    }
    return linkedDeliveryNotes
}

@Composable
fun LinkedDeliveryNoteRow(
    linkedNoteColumnWeight: Float,
    docNumberAndDate: Pair<String?, String?>
) {
    Row(
        Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TableCell(
            text = docNumberAndDate.first + " - " + docNumberAndDate.second,
            weight = linkedNoteColumnWeight,
            alignEnd = false,
            isBold = true
        )
    }
}


@Composable
fun DocumentProductsRows(
    tableData: List<DocumentProductState>,
    descriptionColumnWeight: Float,
    quantityColumnWeight: Float,
    taxColumnWeight: Float,
) {
    tableData.forEach {
        var priceWithoutTax = BigDecimal(0)
        it.priceWithTax?.let { priceWithTax ->
            priceWithoutTax =
                (priceWithTax - priceWithTax * (it.taxRate ?: BigDecimal(0)) / BigDecimal(100))
        }

        Row(
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),// without it we can't add fillMaxHeight to columns
            horizontalArrangement = Arrangement.End
        ) {
            TableCell(
                text = it.name.text,
                weight = descriptionColumnWeight,
                alignEnd = false,
                subText = it.description?.text
            )
            TableCell(
                text = it.quantity.toString() + " ",
                weight = quantityColumnWeight,
                alignEnd = true
            )
            TableCell(
                text = if (!it.unit?.text.isNullOrEmpty()) it.unit?.text!! else " - ",
                alignEnd = true
            )
            TableCell(
                text = it.taxRate?.let { it.setScale(0, RoundingMode.HALF_UP).toString() + "%" }
                    ?: " - ",
                weight = taxColumnWeight,
                alignEnd = true
            )
            TableCell(
                text = priceWithoutTax.setScale(2, RoundingMode.HALF_UP)
                    .toString() + stringResource(id = R.string.currency),
                alignEnd = true
            )
            TableCell(
                text = (priceWithoutTax * it.quantity).setScale(2, RoundingMode.HALF_UP)
                    .toString() + stringResource(id = R.string.currency),
                alignEnd = true
            )

            Spacer(Modifier.padding(bottom = 2.dp))
        }
    }
}


@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float = .22f, // Width of all the cells except the first row ones
    alignEnd: Boolean,
    subText: String? = null,
    isBold: Boolean = false,
) {
    Column(
        modifier = Modifier
            .bottomBorder(borderWidth, Color.LightGray)
            .rightBorder(borderWidth, Color.LightGray)
            .leftBorder(borderWidth, Color.LightGray)
            .weight(weight)
            .padding(start = 2.dp, end = 4.dp, top = 2.dp)
            .fillMaxHeight(),
        horizontalAlignment = if (alignEnd) {
            Alignment.End
        } else Alignment.Start
    ) {

        Text(
            text = text,
            style = if (isBold) MaterialTheme.typography.textForDocumentsBold
            else MaterialTheme.typography.textForDocuments,
            // have to specify it (in addition to column alignment)
            // because without it, multilines text aren't aligned
            textAlign = if (alignEnd) TextAlign.Right else TextAlign.Start,
        )
        if (!subText.isNullOrEmpty()) {
            Text(
                text = subText,
                style = MaterialTheme.typography.textForDocuments,
                fontStyle = FontStyle.Italic
                // maxLines = 1
            )
        }
        Spacer(Modifier.padding(2.dp))
        // Spacer avoids subtext to be cut in half horizontally (due to column weight, don't know why)
    }
}

fun Modifier.topBorder(strokeWidth: Dp, color: Color) = composed(
    factory = {
        val density = LocalDensity.current
        val strokeWidthPx = density.run { strokeWidth.toPx() }

        Modifier.drawBehind {
            val width = size.width

            drawLine(
                color = color,
                start = Offset(x = 0f, y = 0f),
                end = Offset(x = width, y = 0f),
                strokeWidth = strokeWidthPx
            )
        }
    }
)

fun Modifier.bottomBorder(strokeWidth: Dp, color: Color) = composed(
    factory = {
        val density = LocalDensity.current
        val strokeWidthPx = density.run { strokeWidth.toPx() }

        Modifier.drawBehind {
            val width = size.width
            val height = size.height - strokeWidthPx / 2

            drawLine(
                color = color,
                start = Offset(x = 0f, y = height),
                end = Offset(x = width, y = height),
                strokeWidth = strokeWidthPx
            )
        }
    }
)

fun Modifier.rightBorder(strokeWidth: Dp, color: Color) = composed(
    factory = {
        val density = LocalDensity.current
        val strokeWidthPx = density.run { strokeWidth.toPx() }

        Modifier.drawBehind {
            val width = size.width
            val height = size.height - strokeWidthPx / 2

            drawLine(
                color = color,
                start = Offset(x = width, y = 0f),
                end = Offset(x = width, y = height),
                strokeWidth = strokeWidthPx
            )
        }
    }
)

fun Modifier.leftBorder(strokeWidth: Dp, color: Color) = composed(
    factory = {
        val density = LocalDensity.current
        val strokeWidthPx = density.run { strokeWidth.toPx() }

        Modifier.drawBehind {
            val height = size.height - strokeWidthPx / 2

            drawLine(
                color = color,
                start = Offset(x = 0f, y = 0f),
                end = Offset(x = 0f, y = height),
                strokeWidth = strokeWidthPx
            )
        }
    }
)

