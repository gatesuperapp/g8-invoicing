package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.theme.ColorBackgroundGrey
import com.ninetyninepercent.funfactu.icons.IconArrowBack

// User can either select an item (client or product) in the list, or add a new item
@Composable
fun DocumentBottomSheetFooter(
    text: TextFieldValue,
    onValueChange: (ScreenElement, Any) -> Unit,
    onClickBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight(0.5f)
            .background(Color.White)
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
                    contentDescription = "Back"
                )
            }
        }

        Row(
            Modifier
                .padding(30.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            BasicTextField(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ColorBackgroundGrey)
                    .padding(30.dp)
                    ,
                value = text,
                onValueChange = {
                    onValueChange(ScreenElement.DOCUMENT_FOOTER, it)
                })
        }
    }
}