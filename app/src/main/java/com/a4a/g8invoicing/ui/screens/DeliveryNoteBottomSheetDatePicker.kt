package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.a4a.g8invoicing.ui.theme.ColorLightGreenTransp
import com.ninetyninepercent.funfactu.icons.IconArrowBack


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryNoteBottomSheetDatePicker(
    initialDate: String,
    datePickerState: DatePickerState,
    onClickBack: () -> Unit
) {
    // As soon as the user selects a new date, the new value is updated through datePickerState:
    // see "rememberDatePickerState" in DeliveryNoteAddEdit
    Column(
        modifier = Modifier
            .background(Color.White)
            //.fillMaxHeight()
    ) {
        // Header: display "back" button
        Row(
            Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onClickBack) {
                Icon(
                    imageVector = IconArrowBack,
                    contentDescription = "Validate"
                )
            }
        }

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
