package com.a4a.g8invoicing.ui.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
                        IconButton(
                            onClick = {
                                isExpanded = true
                            },
                        ) {
                            Icon(
                                action.icon,
                                contentDescription = stringResource(id = action.description)
                            )
                            BottomBarMenu(
                                navController,
                                isExpanded,
                                dismissMenu = { isExpanded = false },
                                onClickCategory
                            )
                        }
                    } else {  // Other icons handled "normally"
                        IconButton(
                            onClick = action.onClick
                        ) {
                            Icon(
                                action.icon,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onBackground,
                                contentDescription = stringResource(id = action.description)
                            )
                        }
                    }

                    // Will not apply when there's no left icons so items can be centered
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Icons on the right side
                primaryIconsRight.forEach { action ->
                    IconButton(
                        onClick = action.onClick
                    ) {
                        Icon(
                            action.icon,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onBackground,
                            contentDescription = stringResource(id = action.description)
                        )
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


//Used to get a RowScope (without it it's not possible to modify the weight)
@Composable
fun ViewWithLayout(content: @Composable RowScope.() -> Unit) {
    Row {
        content()
    }
}