package com.a4a.g8invoicing.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onClickCategory: (Category) -> Unit,
    onClickTag: (DocumentTag) -> Unit,
    onChangeBackground: () -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }

    ViewWithLayout {
        if (appBarActions !== null) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Icons on the left side
                appBarActions.filter { !it.isSecondary && it.alignmentLeft }
                    .forEach { action ->
                        // Categories dropdown menu when clicking "Categories" icon
                        if (action.description == R.string.appbar_categories) {
                            Button(
                                contentPadding = PaddingValues(0.dp),
                                onClick = {
                                    onChangeBackground()
                                    isExpanded = true
                                },
                            ) {
                                AddIconAndLabelInColumn(action)
                                CategoriesDropdownMenu(
                                    navController,
                                    isExpanded,
                                    dismissMenu = {
                                        onChangeBackground()
                                        isExpanded = false
                                    },
                                    onClickCategory
                                )
                            }
                        } else {  // Other icons handled "normally"
                            Button(
                                contentPadding = PaddingValues(0.dp),
                                onClick = action.onClick
                            ) {
                                AddIconAndLabelInColumn(
                                    action,
                                    24.dp
                                )
                            }
                        }

                        // Will not apply when there's no left icons so items can be centered
                        Spacer(modifier = Modifier.weight(1f))
                    }

                // Icons on the right side
                appBarActions.filter { !it.isSecondary && !it.alignmentLeft }
                    .forEach { action ->
                        Button(
                            contentPadding = PaddingValues(0.dp),
                            onClick = action.onClick
                        ) {
                            if (action.name == "TAG") {
                                ButtonWithDropdownMenu(
                                    action,
                                    listOf(
                                        actionTagDraft(),
                                        actionTagSent(),
                                        actionTagPaid(),
                                        actionTagLate(),
                                        actionTagReminded(),
                                        actionTagCancelled(),
                                    ),
                                    iconSize = 16.dp,
                                    onClickTag = {
                                        onClickTag(it)
                                    },
                                    onChangeBackground = onChangeBackground
                                )
                            } else {
                                AddIconAndLabelInColumn(
                                    action,
                                    24.dp
                                )
                            }
                        }
                    }
                // Dropdown menu "More"
                if (appBarActions.any { it.isSecondary }) {
                    ButtonWithDropdownMenu(
                        actionMore(),
                        appBarActions.filter { it.isSecondary },
                        onChangeBackground = onChangeBackground
                    )
                }
            }
        }
    }

}

@Composable
fun AddIconAndLabelInColumn(action: AppBarAction, iconSize: Dp? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        action.icon?.let {
            Icon(
                it,
                modifier = if (iconSize != null) {
                    Modifier
                        .size(iconSize)
                } else Modifier,
                tint = action.iconColor ?: LocalContentColor.current,
                contentDescription = stringResource(id = action.description)
            )
        }

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