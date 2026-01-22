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
    val currentDate = if (initialDate.isNotBlank()) {
        parseDate(initialDate) ?: currentTimeMillis()
    } else {
        currentTimeMillis()
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDate,
        initialDisplayMode = DisplayMode.Picker
    )

    var selectedDate by remember {
        mutableStateOf(datePickerState.selectedDateMillis?.let {
            formatDate(it)
        })
    }

    // Only way i found to execute function when new date is picked..There must be a better way?
    selectedDate =
        datePickerState.selectedDateMillis?.let { formatDate(it) }?.substring(0,10) ?: ""
    val initialDateTrimmed = if (initialDate.length >= 10) initialDate.substring(0,10) else ""
    if (initialDateTrimmed != selectedDate) { // Substring to keep only the date, not the time
        onValueChange(
            datePickerState.selectedDateMillis?.let { formatDate(it) } ?: "")
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
