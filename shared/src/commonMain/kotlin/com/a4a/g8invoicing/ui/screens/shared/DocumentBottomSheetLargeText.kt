package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.shared.customTextSelectionColors
import com.a4a.g8invoicing.ui.theme.ColorLightGreyo

// User can either select an item (client or product) in the list, or add a new item
@Composable
fun DocumentBottomSheetLargeText(
    initialText: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
) {
    var text by remember { mutableStateOf(initialText) }
    val scrollState = rememberScrollState()

    CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
        Column(
            Modifier
                .background(ColorLightGreyo)
                .padding(start = 30.dp, end = 30.dp, top = 20.dp, bottom = 30.dp)
                .fillMaxHeight(0.5f)
        ) {
            BasicTextField(
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(30.dp),
                value = text,
                onValueChange = {
                    text = it
                    onValueChange(it)
                }
            )
        }
    }
}
