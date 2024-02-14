package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.a4a.g8invoicing.ui.theme.ColorLoudGrey
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue


// For Client Add/Edit & Product Add/Edit
// Low level state holder so we avoid recomposing all form when only one input field changes
@Composable
fun FormInputCreatorTextStateful(
    input: TextInput,
    keyboardOption: ImeAction = ImeAction.Go,
    formActions: KeyboardActions = KeyboardActions(
        onGo = {
        }
    ),
) {
    //  The state holder is here
    var text by remember { mutableStateOf(input.text) }

    // The handle is the "drop" shaped under the cursor -> we don't want to display it
    // The background is the color when text is selected
    val customTextSelectionColors = TextSelectionColors(
        handleColor = Color.Transparent,
        backgroundColor = ColorLoudGrey
    )

    CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
        Text("stateful")
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value =  input.text ?: TextFieldValue(""),
            onValueChange = {
                // Update the remembered state (so that the values on screen
                // are updated):
                text = it
               /* // Update the viewmodel (so when user saves the document,
                // the viewmodel knows latest values):
                input.onValueChange(it)*/
            },
            textStyle = LocalTextStyle.current,
            keyboardOptions = KeyboardOptions(
                imeAction = keyboardOption,
                keyboardType = KeyboardType.Text
                /*keyboardType = if (input.isText) {
                    KeyboardType.Text
                } else {
                    KeyboardType.Number
                }*/
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
