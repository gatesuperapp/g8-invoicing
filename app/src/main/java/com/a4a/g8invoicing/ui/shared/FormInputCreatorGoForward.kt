package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.theme.ColorGreyo
import com.ninetyninepercent.funfactu.icons.IconArrowRight

@Composable
fun ForwardInputCreatorGoForward(
    forwardInput: ForwardElement,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier
                .padding(end = 6.dp)
                .weight(1F)
                .fillMaxWidth(),
            text = forwardInput.text
        )
        Icon(
            modifier = Modifier
                .width(15.dp)
                .padding(bottom = 3.dp),
            tint = ColorGreyo,
            imageVector = IconArrowRight,
            contentDescription = "Right arrow"
        )
    }
}