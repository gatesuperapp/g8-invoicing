package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.theme.textForDocuments

@Composable
fun DocumentBasicTemplateFreeField(
    freeField: String,
    onClickElement: (ScreenElement) -> Unit,
    selectedItem: ScreenElement?,
) {
    Row(
        Modifier
            .getBorder(ScreenElement.DOCUMENT_REFERENCE, selectedItem)
            .customCombinedClickable(
                onClick = {
                    onClickElement(ScreenElement.DOCUMENT_REFERENCE)
                },
                onLongClick = {
                }
            )

    ) {
        Text(
            style = MaterialTheme.typography.textForDocuments,
            text = freeField
        )
    }
}
