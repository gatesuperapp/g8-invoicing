package com.a4a.g8invoicing.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.BottomAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.ui.theme.ColorLoudGrey

@Composable
fun BottomBarAction(
    navController: NavController,
    appBarActions: Array<AppBarAction>? = null,
    onClickCategory: (Category) -> Unit = { },
    onClickTag: (DocumentTag) -> Unit,
    onChangeBackground: () -> Unit,
    ) {
    BottomAppBar(
        modifier = Modifier
            .border(1.dp, ColorLoudGrey),
        actions = {
            BottomBarActionView(
                navController,
                appBarActions,
                onClickCategory,
                onClickTag,
                onChangeBackground
            )
        },
        contentPadding = PaddingValues(
            top = 12.dp, bottom = 12.dp, start = 24.dp, end = 12.dp
        )
    )
}
