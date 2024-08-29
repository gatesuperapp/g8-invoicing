package com.a4a.g8invoicing.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.BottomAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.theme.ColorDarkBar

@Composable
fun DocumentBottomBar(
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
            DocumentBottomBarView(
                actions
            )
        }
    )
}

