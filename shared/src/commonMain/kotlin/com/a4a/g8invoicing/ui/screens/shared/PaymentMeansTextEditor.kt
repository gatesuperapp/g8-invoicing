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
import com.a4a.g8invoicing.data.models.PaymentLabelSegment
import com.a4a.g8invoicing.data.models.flattenPaymentLabel
import com.a4a.g8invoicing.data.models.paymentLabelTokenSpans
import com.a4a.g8invoicing.data.models.rebuildSegmentsFromEditedText
import com.a4a.g8invoicing.ui.shared.customTextSelectionColors
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.ColorVioletLink

/**
 * Protected text editor for the payment-means label. The user types freely
 * around the Token segments (mode names like "virement", "chèque") which
 * appear as violet-tinted pills and cannot be deleted or split. Any edit
 * that would remove a token — backspace inside it, big selection replace
 * that swallows it, cut — is rejected and the previous value is restored.
 *
 * Persistence flows via [onSegmentsChange], which fires every time the edit
 * lands as a valid new segment list. Cancel/Done on the outer sheet do not
 * matter for the state — auto-save owns it.
 *
 * Implementation notes:
 *  - The buffer text is the flattened rendered string, so the
 *    [VisualTransformation] uses [OffsetMapping.Identity]. No bidirectional
 *    offset mapping means no cursor-drift bug.
 *  - Rejection uses classic controlled input (don't update local state on
 *    invalid edits). Compose leaves the field at its previous value on the
 *    next composition, which visually reads as "the token bounced the edit".
 */
@Composable
fun PaymentMeansTextEditor(
    segments: List<PaymentLabelSegment>,
    labelsByChip: Map<String, String>,
    onSegmentsChange: (List<PaymentLabelSegment>) -> Unit,
) {
    val scrollState = rememberScrollState()
    val flattened = flattenPaymentLabel(segments, labelsByChip)
    // Initialise the buffer once, on the first composition of this editor
    // instance (i.e. when the sub-sheet opens). Keying the remember on
    // [flattened] rebuilt the TextFieldValue after every accepted keystroke
    // — the selection reset back to the end of the text, so the caret
    // teleported away from wherever the user was typing. The modal is the
    // sole editor while it's open (chip taps are behind and inaccessible),
    // so there's no risk of an external mutation racing this state.
    var value by remember {
        mutableStateOf(
            TextFieldValue(
                text = flattened,
                selection = androidx.compose.ui.text.TextRange(flattened.length),
            )
        )
    }
    val tokenSpans = remember(segments, labelsByChip) {
        paymentLabelTokenSpans(segments, labelsByChip)
    }
    val visualTransformation = remember(tokenSpans) {
        PaymentMeansTokenVisualTransformation(tokenSpans)
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
                    // Autofill/keyboard suggestion contexts noisy on this small
                    // free-form field — clear the semantics to keep it minimal.
                    .clearAndSetSemantics {},
                value = value,
                onValueChange = { new ->
                    val rebuilt = rebuildSegmentsFromEditedText(new.text, segments, labelsByChip)
                    if (rebuilt != null) {
                        value = new
                        onSegmentsChange(rebuilt)
                    }
                    // else: silently reject. Compose keeps `value` unchanged
                    // and BasicTextField reverts to the previous displayed text
                    // on the next composition. The cursor lands wherever the
                    // previous `value.selection` had it — no manual reposition.
                },
                cursorBrush = SolidColor(Color.Black),
                visualTransformation = visualTransformation,
            )
        }
    }
}

/**
 * Highlights each token range with a violet SpanStyle. Identity offset
 * mapping — the buffer text is exactly what's rendered, we just add color.
 */
private class PaymentMeansTokenVisualTransformation(
    private val tokenSpans: List<com.a4a.g8invoicing.data.models.PaymentLabelTokenSpan>,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val builder = AnnotatedString.Builder(text)
        for (span in tokenSpans) {
            // Safety clamp: the underlying string might have shrunk in a
            // frame race — never over-run the addStyle range.
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
