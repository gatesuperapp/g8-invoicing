package com.a4a.g8invoicing.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.a4a.g8invoicing.R

@Composable
fun BottomBarActionView(
    navController: NavController,
    appBarActions: Array<AppBarAction>?,
    onClickCategory: ((Category) -> Unit)?,
) {
    var isExpanded by remember { mutableStateOf(false) }

    ViewWithLayout {
        if (appBarActions !== null) {
            val (secondaryIcons, primaryIcons) = appBarActions.partition { it.isInDropDownMenu }
            val (primaryIconsLeft, primaryIconsRight) = primaryIcons.partition { it.alignmentLeft }

            Row(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Icons on the left side
                primaryIconsLeft.forEach { action ->
                    // Categories dropdown menu when clicking "Categories" icon
                    if (action.description == R.string.appbar_categories) {
                        Button(
                            contentPadding = PaddingValues(0.dp),
                            onClick = {
                                isExpanded = true
                            },
                        ) {
                            AddIconAndLabel(action)
                            BottomBarMenu(
                                navController,
                                isExpanded,
                                dismissMenu = { isExpanded = false },
                                onClickCategory
                            )
                        }
                    } else {  // Other icons handled "normally"
                        Button(
                            contentPadding = PaddingValues(0.dp),
                            onClick = action.onClick
                        ) {
                            AddIconAndLabel(action, 24.dp, MaterialTheme.colorScheme.onBackground)
                        }
                    }

                    // Will not apply when there's no left icons so items can be centered
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Icons on the right side
                primaryIconsRight.forEach { action ->
                    Button(
                        contentPadding = PaddingValues(0.dp),
                        onClick = action.onClick
                    ) {
                        AddIconAndLabel(action, 24.dp, MaterialTheme.colorScheme.onBackground)
                    }
                }
                // Dropdown menu "More"
                if (secondaryIcons.isNotEmpty()) {
                    MoreOptionsDropdownMenu(secondaryIcons)
                }
            }
        }
    }

}

@Composable
fun AddIconAndLabel(action: AppBarAction, iconSize: Dp?  = null, color: Color? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            action.icon,
            modifier = if (iconSize != null) {
                Modifier
                    .size(iconSize)
            } else Modifier,
            tint = color ?: LocalContentColor.current,
            contentDescription = stringResource(id = action.description)
        )
        action.label?.let {
            Text(
                text = stringResource(id = it),
                fontSize = 10.sp,
            )
        }
    }
}


//Used to get a RowScope (without it it's not possible to modify the weight)
@Composable
fun ViewWithLayout(content: @Composable RowScope.() -> Unit) {
    Row {
        content()
    }
}