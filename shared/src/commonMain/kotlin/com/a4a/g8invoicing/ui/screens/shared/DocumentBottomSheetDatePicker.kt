package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.a4a.g8invoicing.ui.theme.ColorLightGreenTransp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.theme.ColorVeryLightGreyo


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentBottomSheetDatePicker(
    initialDate: String,
    onValueChange: (String) -> Unit,
) {
    // Use UTC-aware parsing for DatePicker compatibility
    // DatePicker uses UTC internally, so we need midnight UTC of the desired date
    val currentDate = if (initialDate.isNotBlank()) {
        parseDateForPicker(initialDate) ?: todayForPicker()
    } else {
        todayForPicker()
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDate,
        initialDisplayMode = DisplayMode.Picker
    )

    // Track the initial date to detect user changes
    val initialDateTrimmed = remember(initialDate) {
        if (initialDate.length >= 10) initialDate.substring(0, 10) else ""
    }

    // Track the last emitted date to avoid duplicate emissions
    var lastEmittedDate by remember { mutableStateOf(initialDateTrimmed) }

    // Use UTC-aware formatting for DatePicker compatibility
    val selectedDate = datePickerState.selectedDateMillis?.let { formatDateFromPicker(it) } ?: ""

    // Only emit when user actually selects a different date
    if (selectedDate.isNotEmpty() && selectedDate != lastEmittedDate) {
        lastEmittedDate = selectedDate
        onValueChange(selectedDate)
    }

    // As soon as the user selects a new date, the new value is updated through datePickerState:
    // see "rememberDatePickerState" in DocumentAddEdit
    Column(
        modifier = Modifier
            .padding(16.dp)
            .background(color = ColorVeryLightGreyo, shape = RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
                //.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DatePickerView(
                datePickerState = datePickerState,
                modifier = Modifier
                  //  .width(datePickerWidth)
                  //  .padding(horizontal = 16.dp)
                    .align(Alignment.CenterHorizontally)

            )
        }
    }
}

@Composable
@ExperimentalMaterial3Api
fun DatePickerView(
    datePickerState: DatePickerState,
    modifier: Modifier = Modifier,
) {
    DatePicker(
        state = datePickerState,
        showModeToggle = false,
        modifier = modifier,
        title = null,
        headline = null,
        colors = DatePickerDefaults.colors(
            todayContentColor = Color.Black,
            todayDateBorderColor = Color.LightGray,
            dayInSelectionRangeContainerColor = ColorLightGreenTransp,
            selectedDayContainerColor = ColorLightGreenTransp,
            selectedYearContainerColor = ColorLightGreenTransp,
            currentYearContentColor =  Color.Black
        )
    )
}
