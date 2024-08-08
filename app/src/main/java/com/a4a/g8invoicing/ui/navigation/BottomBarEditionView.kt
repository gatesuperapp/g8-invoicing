package com.a4a.g8invoicing.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.ui.theme.ColorButtonBar
import com.a4a.g8invoicing.ui.theme.ColorDarkBar

@Composable
fun BottomBarEditionView(
    actions: Array<AppBarAction>?,
) {
    ViewWithLayout {
        actions?.let {  actions ->
            Row(
                modifier = Modifier
                    .background(ColorDarkBar)
                    .padding(start = 24.dp, end = 12.dp, top = 14.dp )
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
            ) {
                // Icons on the left side
                actions.forEach { action ->
                    Button(
                        onClick = action.onClick,
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Transparent)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                action.icon,
                                modifier = Modifier
                                    .size(24.dp),
                                tint = ColorButtonBar,
                                contentDescription = stringResource(id = action.description)
                            )
                            action.label?.let {
                                Text(
                                    text = stringResource(id = it),
                                    fontSize = 10.sp,
                                    color = ColorButtonBar
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}