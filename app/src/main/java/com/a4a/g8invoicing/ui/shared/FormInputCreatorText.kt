package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.theme.ColorGreyo
import com.a4a.g8invoicing.ui.theme.ColorLoudGrey
import com.a4a.g8invoicing.ui.theme.textVerySmall
import icons.IconEdit

@Composable
fun FormInputCreatorText(
    input: TextInput,
    keyboardOption: ImeAction = ImeAction.Go,
    formActions: KeyboardActions = KeyboardActions(
        onGo = {
        }
    ),
    focusRequester: FocusRequester?,
    errorMessage: String?, // Used for email and name validation
    isEditableLabel: Boolean = false, // Used for editable labels
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    var customModifier = Modifier
        .onFocusChanged {
            if (it.isFocused) {
                keyboardController?.show()
            }
        }

    var rowModifier = Modifier.background(Color.Transparent) // Just so we can use the custom modifier
    rowModifier = if (isEditableLabel)
        rowModifier.then(
            Modifier
                .fillMaxWidth(0.4f)
        ) else rowModifier


    focusRequester?.let {
        customModifier = customModifier.then(Modifier.focusRequester(focusRequester))
    }

    CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
        Column(modifier = rowModifier) {
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

            errorMessage?.let {
                Text(
                    color = Color.Red,
                    text = it
                )
            }

            if (isEditableLabel) {
                Row( modifier = Modifier.padding(top = 3.dp)) {
                    Icon(
                        modifier = Modifier
                            .width(10.dp),
                        imageVector = IconEdit,
                        contentDescription = "Edit label",
                        tint = ColorGreyo
                    )
                    Text(
                        color = ColorGreyo,
                        style = MaterialTheme.typography.textVerySmall,
                        text = stringResource(id = R.string.form_label_edit)
                    )
                }
            }
        }
    }
}

// The handle is the "drop" shaped under the cursor -> we don't want to display it
// The background is the color when text is selected
val customTextSelectionColors = TextSelectionColors(
    handleColor = Color.Transparent,
    backgroundColor = ColorLoudGrey
)