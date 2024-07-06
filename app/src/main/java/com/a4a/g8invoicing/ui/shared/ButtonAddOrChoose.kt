package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.ui.shared.icons.IconPlus
import com.a4a.g8invoicing.ui.theme.ColorLightGrey

@Composable
fun ButtonAddOrChoose(
    onClickNew: () -> Unit,
    hasBorder: Boolean,
    hasBackground: Boolean,
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
                border = ButtonDefaults.outlinedButtonBorder,
                shape = RoundedCornerShape(2.dp)
            )
        )
    }
    if (hasBackground) {
        myModifier = myModifier.then(
            Modifier.background(
                ColorLightGrey
            )
        )
    }

    Row(
        modifier = myModifier
    ) {
        Icon(
            modifier = Modifier
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 20.dp,
                    bottom = 20.dp
                ),
            imageVector = IconPlus,
            contentDescription = "Add new"
        )
        Text(
            modifier = Modifier.padding(top = 20.dp, bottom = 20.dp),
            text = buttonText,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
