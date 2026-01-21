package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// For Documents, where state is shared between components (UI template & bottom sheet)
// We need to use unidirectional flow to recompose all UI when input field changes
/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormInputCreatorDate(
    input: DateInput
) {
    DatePicker(
        state = input.datePickerState,
        showModeToggle = false,
        modifier = Modifier
            .padding(0.dp)
            .background(Color.Blue),
        title = null,
        headline = null
    )
}
*/
