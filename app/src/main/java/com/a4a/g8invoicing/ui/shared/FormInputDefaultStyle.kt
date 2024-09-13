package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.theme.inputField
import com.a4a.g8invoicing.ui.theme.inputLabel


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun FormInputDefaultStyle(
    textToDisplay: String?,
    innerTextField: @Composable () -> Unit,
    placeholder: String? = null,
    interactionSource: MutableInteractionSource,
) {
    TextFieldDefaults.DecorationBox(
        value = textToDisplay ?: "",
        innerTextField = innerTextField,
        singleLine = false,
        enabled = true,
        visualTransformation = VisualTransformation.None,
        placeholder = {
            Text(
                modifier = Modifier
                    .padding(
                        top = 0.dp,
                        bottom = 0.dp
                    ),
                text = placeholder ?: "",
                style = MaterialTheme.typography.inputField
            )
        },
        interactionSource = interactionSource,
        // change the start padding
        contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(
            start = 0.dp,
            end = 0.dp,
            top = 0.dp,
            bottom = 0.dp
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = Color.Transparent,
        )
    )
}