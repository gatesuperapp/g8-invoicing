package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.document_modal_product_back
import com.a4a.g8invoicing.shared.resources.document_modal_product_cancel
import com.a4a.g8invoicing.shared.resources.document_modal_product_save
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.theme.textCta
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentBottomSheetFormSimple(
    onClickCancel: () -> Unit,
    onClickDone: (ScreenElement) -> Unit = {},
    bottomSheetTitle: String,
    content: @Composable () -> Unit,
    isDatePicker: Boolean = false,
    screenElement: ScreenElement,
    // showActions = false → header shows only the title (no Cancel / Save).
    // Used by auto-save pickers where each change fires onValueChange
    // immediately, so there's nothing to Save and nothing to revert.
    // Dismiss goes through swipe-down / system back press → onDismissRequest.
    showActions: Boolean = true,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    // With Cancel/Save actions the user's edits are pending until they
    // explicitly confirm — shouldDismissOnClickOutside=false blocks the scrim
    // so an accidental outside tap can't discard them. Back press still
    // triggers onDismissRequest → onClickCancel, and swipe-down does the
    // same by transitioning to Hidden. Auto-save sheets keep the default.
    ModalBottomSheet(
        onDismissRequest = onClickCancel,
        sheetState = sheetState,
        dragHandle = null,
        properties = if (showActions) {
            ModalBottomSheetProperties(shouldDismissOnClickOutside = false)
        } else {
            ModalBottomSheetProperties()
        },
    ) {
        Column() {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 40.dp, bottom = 16.dp, end = 30.dp, start = 30.dp
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    if (showActions) {
                        Text(
                            style = MaterialTheme.typography.textCta,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(top = 20.dp)
                                .clickable {
                                    onClickCancel()
                                },
                            text = stringResource(
                                if (!isDatePicker)
                                    Res.string.document_modal_product_cancel
                                else Res.string.document_modal_product_back
                            )
                        )
                    }
                    Text(
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                            .align(Alignment.TopCenter),
                        style = MaterialTheme.typography.titleMedium,
                        text = bottomSheetTitle
                    )

                    if (showActions && !isDatePicker)
                        Text(
                            style = MaterialTheme.typography.textCta,
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .clickable {
                                    onClickDone(screenElement)
                                }
                                .align(Alignment.TopEnd),
                            text = stringResource(Res.string.document_modal_product_save)
                        )
                }
            }
            // Extra breathing room below the header row. Kept small because
            // most content composables (payment picker, footer text) also add
            // their own top padding via a surfaceMuted band, so this Spacer +
            // content padding together produce the visible gap.
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
