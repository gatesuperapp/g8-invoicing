package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.info_tooltip_ok
import com.a4a.g8invoicing.shared.resources.product_legacy_type_warning_content
import com.a4a.g8invoicing.shared.resources.product_legacy_type_warning_title
import com.a4a.g8invoicing.ui.theme.callForActionsViolet
import com.a4a.g8invoicing.ui.viewmodels.ProductAddEditViewModel
import org.jetbrains.compose.resources.stringResource

/**
 * One-shot modal fired when the user adds a product to a document whose `type` is null
 * (legacy product created pre-1.8). Reminds the user that Product.type is a new field
 * and worth checking. Persisted-dismissed via `com.russhwolf.settings.Settings`.
 */
@Composable
fun LegacyProductTypeWarningDialog(viewModel: ProductAddEditViewModel) {
    val show by viewModel.showLegacyProductTypeWarning.collectAsState()
    if (!show) return
    AlertDialog(
        onDismissRequest = { viewModel.dismissLegacyProductTypeWarning() },
        title = { Text(stringResource(Res.string.product_legacy_type_warning_title)) },
        text = {
            Text(
                text = stringResource(Res.string.product_legacy_type_warning_content),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        textContentColor = Color.Black,
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Button(onClick = { viewModel.dismissLegacyProductTypeWarning() }) {
                    Text(
                        text = stringResource(Res.string.info_tooltip_ok),
                        style = MaterialTheme.typography.callForActionsViolet,
                    )
                }
            }
        },
        modifier = Modifier.padding(16.dp),
    )
}
