package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.theme.textForDocuments
import com.a4a.g8invoicing.ui.theme.textForDocumentsBold

@Composable
fun DocumentBasicTemplateReference(
    reference: String,
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
            style = MaterialTheme.typography.textForDocumentsBold,
            text = stringResource(id = R.string.document_reference) + " : "
        )
        Text(
            style = MaterialTheme.typography.textForDocuments,
            text = reference
        )
    }
}