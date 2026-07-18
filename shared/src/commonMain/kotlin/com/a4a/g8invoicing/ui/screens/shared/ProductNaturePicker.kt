package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.data.models.ProductNature
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.product_type_goods
import com.a4a.g8invoicing.shared.resources.product_type_service
import org.jetbrains.compose.resources.stringResource

/**
 * Bottom-sheet picker for Product.type (SERVICE / GOODS). Kept intentionally minimal:
 * two rows, one per value. Selection calls [onSelect]; caller closes the sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductNaturePicker(
    current: ProductNature?,
    onSelect: (ProductNature) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            NatureRow(
                label = stringResource(Res.string.product_type_goods),
                isCurrent = current == ProductNature.GOODS,
                onClick = { onSelect(ProductNature.GOODS) },
            )
            NatureRow(
                label = stringResource(Res.string.product_type_service),
                isCurrent = current == ProductNature.SERVICE,
                onClick = { onSelect(ProductNature.SERVICE) },
            )
        }
    }
}

@Composable
private fun NatureRow(label: String, isCurrent: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}
