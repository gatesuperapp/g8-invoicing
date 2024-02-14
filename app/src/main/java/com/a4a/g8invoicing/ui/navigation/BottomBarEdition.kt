package com.a4a.g8invoicing.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.ui.theme.ColorButtonBar
import com.a4a.g8invoicing.ui.theme.ColorDarkBar
import com.a4a.g8invoicing.ui.theme.ColorLoudGrey

@Composable
fun BottomBarEdition(
    actions: Array<AppBarAction>?,
) {
    BottomAppBar(
        modifier = Modifier
            .background(ColorDarkBar),
        contentPadding = PaddingValues(0.dp),
        containerColor = ColorDarkBar,
        contentColor = ColorDarkBar,
        tonalElevation = 0.dp ,
        actions = {
            BottomBarEditionView(
                actions
            )
        }
    )
}

