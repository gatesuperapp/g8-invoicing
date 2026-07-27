package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.navigation.actionTagCancelled
import com.a4a.g8invoicing.ui.navigation.actionTagDraft
import com.a4a.g8invoicing.ui.navigation.actionTagLate
import com.a4a.g8invoicing.ui.navigation.actionTagPaid
import com.a4a.g8invoicing.ui.navigation.actionTagReminded
import com.a4a.g8invoicing.ui.navigation.actionTagSent
import com.a4a.g8invoicing.ui.navigation.actionTagUndefined
import com.a4a.g8invoicing.ui.shared.CheckboxFace
import com.a4a.g8invoicing.ui.shared.DocumentType
import com.a4a.g8invoicing.ui.shared.FlippyCheckBox
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.textBody
import com.a4a.g8invoicing.ui.theme.textBodyBold
import com.a4a.g8invoicing.ui.theme.textSecondary
import com.a4a.g8invoicing.data.formatAmount
import org.jetbrains.compose.resources.stringResource

@Composable
fun DocumentListItem(
    document: DocumentState,
    onItemClick: () -> Unit = {},
    onItemCheckboxClick: (Boolean) -> Unit = {},
    keyToResetCheckbox: Boolean,
) {
    // Get the action based on document tag - computed in composable context
    val action = when (document.documentTag) {
        DocumentTag.DRAFT -> actionTagDraft()
        DocumentTag.SENT -> actionTagSent()
        DocumentTag.PAID -> actionTagPaid()
        DocumentTag.LATE -> actionTagLate()
        DocumentTag.REMINDED -> actionTagReminded()
        DocumentTag.CANCELLED -> actionTagCancelled()
        else -> actionTagUndefined()
    }
    var isPressed = remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    // Re-triggers remember calculation when key changes
    val checkedState = remember(keyToResetCheckbox) { mutableStateOf(false) }

    Row(
        verticalAlignment = CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.Black, bounded = false),
            ) {
                //onItemClick()
            }
            .pointerInput(keyToResetCheckbox) {
                detectTapGestures(
                    onPress = { offset ->
                        val press = PressInteraction.Press(offset)
                        isPressed.value = true
                        interactionSource.emit(press)
                        tryAwaitRelease()
                        interactionSource.emit(PressInteraction.Release(press))
                        isPressed.value = false
                    },
                    onTap = {
                        onItemClick()
                    },
                    onLongPress = {
                        checkedState.value = !checkedState.value
                        onItemCheckboxClick(checkedState.value)
                    }
                )
            }
            .background(if (checkedState.value) AppColors.divider else AppColors.surface)
    ) {

        // Adding padding in the inside row, to keep the click & the ripple in all row
        // (NB: putting padding on the checkbox works, but then when name is on 2 lines it's
        // not centered anymore)
        Row(
            modifier = Modifier
                .padding(
                    end = 20.dp,
                    top = 14.dp,
                    bottom = 14.dp
                ),
            verticalAlignment = CenterVertically,
        ) {

            // Cancelled invoices are visually greyed out: white pill (not the
            // yellow "cancelled" fill), primary text in a light muted grey,
            // price struck-through. The tag lookup still returns
            // actionTagCancelled() so the tag dropdown / bottom bar keep their
            // pale-yellow chip semantics elsewhere.
            val isCancelled = document is InvoiceState &&
                document.documentTag == DocumentTag.CANCELLED

            val statusColor: Color = when (document.documentTag) {
                DocumentTag.PAID -> AppColors.statusPaid
                DocumentTag.LATE -> AppColors.statusLate
                else -> AppColors.textPrimary
            }
            // Body text greys out when the invoice is cancelled so the whole
            // row reads as "no longer relevant". textMuted (light grey) rather
            // than textSecondary (dark grey) because cancelled shouldn't
            // compete with active rows for attention.
            val bodyColor: Color = if (isCancelled) AppColors.textMuted else AppColors.textPrimary

            // Hoisted here so both the left column (countdown text) and the
            // right column (LATE status label wants the same day count) can
            // share it — otherwise the right column has no way to see the
            // value computed inside the left column's scope.
            val invoice = document as? InvoiceState
            val daysUntilDue = invoice?.let { daysUntilDueDate(it.dueDate) }

            Column {
                FlippyCheckBox(
                    fillColorWhenSelectionOff = if (isCancelled) AppColors.surface else action.iconColor,
                    backgroundColorWhenSelectionOn = if (checkedState.value) AppColors.divider else AppColors.surface,
                    onItemCheckboxClick = {
                        checkedState.value = !checkedState.value
                        onItemCheckboxClick(checkedState.value)
                    },
                    checkboxFace = if (checkedState.value) CheckboxFace.Front
                    else CheckboxFace.Back,
                    checkedState = checkedState.value,
                    displayBorder = document.documentType != DocumentType.INVOICE || isCancelled,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1F)
                    .padding(end = 6.dp),
                verticalArrangement = Arrangement.spacedBy(space = 2.dp)
            ) {
                Text(
                    text = document.documentNumber.text,
                    style = MaterialTheme.typography.textBodyBold.copy(color = bodyColor),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                document.documentClient?.let {
                    Text(
                        text = it.name.text + (it.firstName?.let { " " + it.text } ?: ""),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.textBody.copy(
                            color = bodyColor,
                            textDecoration = if (isCancelled) TextDecoration.LineThrough else null,
                        ),
                    )
                } ?: Text(" - ")

                // Creation date, optionally followed by " · Éch. dans X
                // jour(s)" for invoices whose deadline is still ahead. Late
                // invoices skip the left-hand countdown because their overdue
                // signal already sits on the right, under the price. Draft
                // is included — customers still like the deadline reminder
                // even before the invoice is sent. Colour is textSecondary
                // (dark grey) — cancelled falls back to textMuted so the
                // whole row still greys out.
                val leftCountdownDays = when {
                    invoice == null -> null
                    invoice.documentTag == DocumentTag.PAID -> null
                    invoice.documentTag == DocumentTag.CANCELLED -> null
                    invoice.documentTag == DocumentTag.LATE -> null
                    daysUntilDue == null || daysUntilDue < 0 -> null
                    else -> daysUntilDue
                }
                val dateColor: Color = if (isCancelled) AppColors.textMuted else AppColors.textSecondary

                Row(verticalAlignment = CenterVertically) {
                    Text(
                        text = dateWithoutYear(document.documentDate),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.textSecondary.copy(color = dateColor),
                    )
                    if (leftCountdownDays != null) {
                        Text(
                            text = " · ",
                            style = MaterialTheme.typography.textSecondary.copy(color = dateColor),
                        )
                        Text(
                            text = stringResource(
                                countdownStringFor(leftCountdownDays),
                                leftCountdownDays,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.textSecondary.copy(color = dateColor),
                        )
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(space = 2.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = document.documentTotalPrices?.totalPriceWithTax?.let {
                        formatAmount(it, document.currency.text.ifEmpty { "EUR" })
                    } ?: "",
                    style = MaterialTheme.typography.textBodyBold.copy(
                        color = if (isCancelled) AppColors.textMuted else statusColor,
                        textDecoration = if (isCancelled) TextDecoration.LineThrough else null,
                    ),
                )
                if (document is InvoiceState) {
                    // Status label under the price. For late invoices the
                    // flat 'En retard' label swells to 'En retard de X jour(s)'
                    // so the row surfaces exactly how overdue it is; the
                    // colour still matches the price so paid/late read as one
                    // green / one red signal.
                    val overdueDays = if (document.documentTag == DocumentTag.LATE &&
                        daysUntilDue != null && daysUntilDue < 0) -daysUntilDue else null
                    val labelText = when {
                        overdueDays != null -> stringResource(
                            countdownStringFor(-overdueDays),
                            overdueDays,
                        )
                        else -> action.label
                    }
                    labelText?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.textSecondary.copy(
                                color = if (isCancelled) AppColors.textMuted else statusColor,
                            ),
                        )
                    }
                }
                // Non-invoice types: no second line on the right — the price
                // sits alone and centres vertically with the left column.
            }
        }
    }
}
