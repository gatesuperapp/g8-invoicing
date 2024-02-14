package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.a4a.g8invoicing.ui.theme.ColorLoudGrey

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FormInputCreatorTextStateless(
    input: TextInput,
    keyboardOption: ImeAction = ImeAction.Go,
    formActions: KeyboardActions = KeyboardActions(
        onGo = {
        }
    ),
    focusRequester: FocusRequester?,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

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
            value = input.text ?: TextFieldValue(""),
            onValueChange = {
                input.onValueChange(it)
            },
            textStyle = LocalTextStyle.current,
            keyboardOptions = KeyboardOptions(
                imeAction = keyboardOption,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = formActions
        ) { innerTextField ->
            val interactionSource = remember { MutableInteractionSource() }
            FormInputDefaultStyle(
                input.text?.text,
                innerTextField,
                input.placeholder,
                interactionSource
            )
        }
    }
}

// The handle is the "drop" shaped under the cursor -> we don't want to display it
// The background is the color when text is selected
val customTextSelectionColors = TextSelectionColors(
    handleColor = Color.Transparent,
    backgroundColor = ColorLoudGrey
)