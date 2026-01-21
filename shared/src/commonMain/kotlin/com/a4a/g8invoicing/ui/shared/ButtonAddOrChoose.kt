package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.theme.ColorLightGrey

@Composable
fun ButtonAddOrChoose(
    onClickNew: () -> Unit,
    hasBorder: Boolean,
    isPickerButton: Boolean,
    buttonText: String,
) {
    var myModifier = Modifier
        .clickable(
            onClick = onClickNew
        )
        .padding(
            start = 20.dp,
            end = 20.dp,
            top = 10.dp,
        )
        .fillMaxWidth()

    if (hasBorder) {
        myModifier = myModifier.then(
            Modifier.border(
                border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                shape = RoundedCornerShape(2.dp)
            )
        )
    }
    if (isPickerButton) {
        myModifier = myModifier.then(
            Modifier.background(
                ColorLightGrey
            )
        )
    }

    Row(
        modifier = myModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = 20.dp,
                    top = 10.dp,
                    bottom = 10.dp
                ),
            text = buttonText,
        )
        if (isPickerButton)
            Icon(
                modifier = Modifier
                    .width(32.dp)
                    .padding(
                        end = 20.dp,
                        top = 10.dp,
                        bottom = 10.dp
                    ),
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Choose existing"
            )
        else Icon(
                modifier = Modifier
                    .width(36.dp)
                    .padding(
                        end = 20.dp,
                        top = 10.dp,
                        bottom = 10.dp
                    ),
                imageVector = Icons.Filled.Add,
                contentDescription = "Add new"
            )
    }
}
