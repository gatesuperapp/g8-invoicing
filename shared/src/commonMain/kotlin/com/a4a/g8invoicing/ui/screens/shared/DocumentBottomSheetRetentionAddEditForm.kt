package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.retention_edit_field_label
import com.a4a.g8invoicing.shared.resources.retention_edit_field_rate
import com.a4a.g8invoicing.ui.shared.DecimalInput
import com.a4a.g8invoicing.ui.shared.FormInput
import com.a4a.g8invoicing.ui.shared.FormUI
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.TextInput
import com.a4a.g8invoicing.ui.theme.AppColors
import org.jetbrains.compose.resources.stringResource

@Composable
fun DocumentBottomSheetRetentionAddEditForm(
    label: TextFieldValue,
    onLabelChange: (TextFieldValue) -> Unit,
    rateText: String,
    onRateTextChange: (String) -> Unit,
) {
    val localFocusManager = LocalFocusManager.current
    val labelFieldLabel = stringResource(Res.string.retention_edit_field_label)
    val rateFieldLabel = stringResource(Res.string.retention_edit_field_rate)

    Column(
        modifier = Modifier
            .fillMaxHeight(0.5f)
            .background(AppColors.surfaceMuted.copy(alpha = 0.4f))
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .background(color = AppColors.surface, shape = RoundedCornerShape(6.dp))
                .fillMaxWidth()
                .padding(top = 18.dp, start = 12.dp, end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val inputList = remember(label, rateText) {
                listOf(
                    FormInput(
                        label = labelFieldLabel,
                        inputType = TextInput(
                            text = label,
                            onValueChange = { onLabelChange(it as TextFieldValue) },
                        ),
                        pageElement = ScreenElement.DOCUMENT_RETENTION_LABEL,
                    ),
                    FormInput(
                        label = rateFieldLabel,
                        inputType = DecimalInput(
                            text = rateText,
                            placeholder = "15",
                            keyboardType = KeyboardType.Decimal,
                            onValueChange = { onRateTextChange(it as String) },
                        ),
                        pageElement = ScreenElement.DOCUMENT_RETENTION_RATE,
                    ),
                )
            }
            FormUI(
                inputList = inputList,
                localFocusManager = localFocusManager,
                placeCursorAtTheEndOfText = {},
            )
        }
    }
}
