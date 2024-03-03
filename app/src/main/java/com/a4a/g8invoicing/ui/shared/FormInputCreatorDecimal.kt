package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction

@Composable
fun FormInputCreatorDecimal(
    input: DecimalInput,
    keyboardOption: ImeAction = ImeAction.Go,
    formActions: KeyboardActions,
    focusRequester: FocusRequester?,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var text by remember { mutableStateOf(input.text) }
    val decimalFormatter = DecimalFormatter()

    var customModifier = Modifier
        .onFocusChanged {
            if (it.isFocused) {
                keyboardController?.show()
            }
        }
        .fillMaxWidth()

    focusRequester?.let {
        customModifier = customModifier.then(Modifier.focusRequester(focusRequester))
    }

    CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
        BasicTextField(
            modifier = customModifier,
            value = input.text ?: "",
            onValueChange = {
                text = decimalFormatter.cleanup(it)
                input.onValueChange(it)
            },
            textStyle = LocalTextStyle.current,
            keyboardOptions = KeyboardOptions(
                imeAction = keyboardOption,
                keyboardType = input.keyboardType
            ),
            keyboardActions = formActions
        ) { innerTextField ->
            val interactionSource = remember { MutableInteractionSource() }
            FormInputDefaultStyle(
                text,
                innerTextField,
                input.placeholder,
                interactionSource
            )
        }
    }
}
