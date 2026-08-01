package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.form_label_edit
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.ColorGreyo
import org.jetbrains.compose.resources.stringResource
import com.a4a.g8invoicing.ui.theme.ColorLoudGrey
import com.a4a.g8invoicing.ui.theme.ColorVioletLight
import com.a4a.g8invoicing.ui.theme.textBodyBold
import com.a4a.g8invoicing.ui.theme.textCaption

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
    isEditableLabel: Boolean = false, // Used for editable labels,
    onClickExpandFullScreen: () -> Unit = {},  // Used to expand product description field
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    var customModifier = Modifier
        .onFocusChanged {
            if (it.isFocused) {
                keyboardController?.show()
            }
        }

    focusRequester?.let {
        customModifier = customModifier.then(Modifier.focusRequester(focusRequester))
    }

    var columnModifier =
        Modifier.background(Color.Transparent) // Just so we can use the custom modifier
    columnModifier = if (isEditableLabel)
        columnModifier.then(
            Modifier
                .fillMaxWidth(0.4f)
        ) else columnModifier



    CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
        Column(modifier = columnModifier) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = if (input.displayFullScreenIcon)
                    Alignment.Top else
                    Alignment.CenterVertically
            ) {
                BasicTextField(
                    modifier = customModifier // focusing on textfield when clinking on label
                        .weight(1F)
                        .padding(end = if (input.displayFullScreenIcon) 4.dp else 0.dp)
                        // Autofill disabled on every FormInput text field.
                        // Compose MP 1.8 registered every BasicTextField with the
                        // Android autofill framework by default; the multi-address
                        // form was catching cross-field fills (picking a postal
                        // code overwrote the street, etc.) and the per-field
                        // ContentType tagging couldn't stop it cleanly. Strip
                        // all semantics on the input so autofill leaves it alone.
                        .clearAndSetSemantics {},
                    //  .horizontalScroll(rememberScrollState()),
                    value = input.text ?: TextFieldValue(""),
                    onValueChange = {
                        input.onValueChange(it)
                    },
                    textStyle = if (isEditableLabel) MaterialTheme.typography.textBodyBold
                    else LocalTextStyle.current,
                    keyboardOptions = KeyboardOptions(
                        // Multiline inputs (client notes) opt out of the
                        // Next/Done imeAction so Enter inserts a newline
                        // instead of jumping to the next field.
                        imeAction = if (input.isMultiline) ImeAction.Default else keyboardOption,
                        keyboardType = input.keyboardType,
                    ),
                    keyboardActions = formActions,
                    minLines = input.minLines,

                ) { innerTextField ->
                    val interactionSource = remember { MutableInteractionSource() }
                    FormInputDefaultStyle(
                        input.text?.text,
                        innerTextField,
                        input.placeholder,
                        interactionSource
                    )
                }

                if (input.displayFullScreenIcon) {
                    Icon(
                        modifier = Modifier
                            .width(20.dp)
                            .clickable(
                                onClick = onClickExpandFullScreen
                            ),
                        imageVector = Icons.Outlined.Fullscreen,
                        contentDescription = "Icon for description in full screen",
                        tint = AppColors.iconSecondary
                    )
                }

                // Clear-field button. Only rendered when opted in AND the
                // field has content. Same warm-grey hue as the placeholder
                // (textDisabled) but with alpha dialed down from 54% to 35%
                // so the chip stays visible without competing with the
                // field text next to it. White cross on top for legibility.
                if (input.displayClearIcon && !input.text?.text.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(AppColors.textDisabled.copy(alpha = 0.35f))
                            .clickable {
                                input.onValueChange(TextFieldValue(""))
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Clear field",
                            modifier = Modifier.size(12.dp),
                            tint = AppColors.textOnAccent,
                        )
                    }
                }
            }

            errorMessage?.let {
                Text(
                    color = Color.Red,
                    text = it
                )
            }

            if (isEditableLabel) {
                Row(modifier = Modifier.padding(top = 3.dp)) {
                    Icon(
                        modifier = Modifier
                            .width(10.dp),
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit label",
                        tint = AppColors.iconSecondary
                    )
                    Text(
                        color = ColorGreyo,
                        style = MaterialTheme.typography.textCaption.copy(fontSize = 9.sp),
                        text = stringResource(Res.string.form_label_edit)
                    )
                }
            }
        }
    }
}

// The handle is the "drop" shaped under the cursor, useful to navigate in the text
// The background is the color when text is selected
val customTextSelectionColors = TextSelectionColors(
    handleColor = ColorVioletLight,
    backgroundColor = ColorLoudGrey
)
