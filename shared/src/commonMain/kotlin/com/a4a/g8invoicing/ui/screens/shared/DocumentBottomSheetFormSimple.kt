package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
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
import com.a4a.g8invoicing.ui.theme.callForActionsViolet
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
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden }
    )

    ModalBottomSheet(
        onDismissRequest = onClickCancel,
        sheetState = sheetState,
        dragHandle = null,
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
                    Text(
                        style = MaterialTheme.typography.callForActionsViolet,
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
                    Text(
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                            .align(Alignment.TopCenter),
                        style = MaterialTheme.typography.titleMedium,
                        text = bottomSheetTitle
                    )

                    if (!isDatePicker)
                        Text(
                            style = MaterialTheme.typography.callForActionsViolet,
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
            content()
        }
    }
}
