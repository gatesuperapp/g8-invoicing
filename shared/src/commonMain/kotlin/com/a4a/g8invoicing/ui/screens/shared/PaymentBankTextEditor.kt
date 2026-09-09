package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.data.models.PaymentBankSegment
import com.a4a.g8invoicing.data.models.flattenPaymentBank
import com.a4a.g8invoicing.data.models.paymentBankTokenSpans
import com.a4a.g8invoicing.data.models.rebuildPaymentBankSegments
import com.a4a.g8invoicing.ui.shared.customTextSelectionColors
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.ColorVioletLink

/**
 * Protected text editor for the bank-details block. IBAN and BIC lines are
 * atomic tokens ("IBAN : FR76…" / "BIC : XXXX") — the user can freely type
 * around them but cannot split or delete a token half-way. Same mechanics as
 * [PaymentMeansTextEditor], different token model.
 */
@Composable
fun PaymentBankTextEditor(
    segments: List<PaymentBankSegment>,
    identifierLabel: String,
    iban: String,
    bic: String,
    onSegmentsChange: (List<PaymentBankSegment>) -> Unit,
) {
    val scrollState = rememberScrollState()
    val flattened = flattenPaymentBank(segments, identifierLabel, iban, bic)
    var value by remember {
        mutableStateOf(
            TextFieldValue(
                text = flattened,
                selection = androidx.compose.ui.text.TextRange(flattened.length),
            )
        )
    }
    val tokenSpans = remember(segments, identifierLabel, iban, bic) {
        paymentBankTokenSpans(segments, identifierLabel, iban, bic)
    }
    val visualTransformation = remember(tokenSpans) {
        PaymentBankTokenVisualTransformation(tokenSpans)
    }

    CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
        Column(
            Modifier
                .background(AppColors.surfaceMuted)
                .padding(start = 30.dp, end = 30.dp, top = 20.dp, bottom = 30.dp)
                .fillMaxHeight(0.5f),
        ) {
            BasicTextField(
                modifier = Modifier
                    .background(AppColors.surface)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 15.dp, vertical = 14.dp)
                    .clearAndSetSemantics {},
                value = value,
                onValueChange = { new ->
                    val rebuilt = rebuildPaymentBankSegments(
                        new.text, segments, identifierLabel, iban, bic,
                    )
                    if (rebuilt != null) {
                        value = new
                        onSegmentsChange(rebuilt)
                    }
                },
                cursorBrush = SolidColor(Color.Black),
                visualTransformation = visualTransformation,
            )
        }
    }
}

private class PaymentBankTokenVisualTransformation(
    private val tokenSpans: List<com.a4a.g8invoicing.data.models.PaymentBankTokenSpan>,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val builder = AnnotatedString.Builder(text)
        for (span in tokenSpans) {
            val end = minOf(span.range.last, builder.length)
            val start = minOf(span.range.first, end)
            if (start >= end) continue
            builder.addStyle(
                style = SpanStyle(
                    background = ColorVioletLink.copy(alpha = 0.12f),
                    color = ColorVioletLink,
                    fontWeight = FontWeight.SemiBold,
                ),
                start = start,
                end = end,
            )
        }
        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}
