package com.a4a.g8invoicing.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.ui.theme.textTiny

@Composable
fun BottomBarActionView(
    navController: NavController,
    appBarActions: Array<AppBarAction>?,
    onClickCategory: (Category) -> Unit,
    onClickTag: (DocumentTag) -> Unit,
    onChangeBackground: () -> Unit,
    isCategoriesMenuOpen: Boolean = false,
    onCategoriesMenuOpenChange: (Boolean) -> Unit = {},
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Synchroniser avec l'état externe
    LaunchedEffect(isCategoriesMenuOpen) {
        if (isCategoriesMenuOpen && !isExpanded) {
            onChangeBackground()
            isExpanded = true
        }
    }

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
                        if (action.name == "CATEGORIES") {
                            Button(
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(4.dp),
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
                                        onCategoriesMenuOpenChange(false)
                                    },
                                    onClickCategory
                                )
                            }
                        } else {  // Other icons handled "normally"
                            Button(
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(4.dp),
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
                            shape = RoundedCornerShape(4.dp),
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
                                        actionTagLocked(),
                                    ),
                                    iconSize = 16.dp,
                                    onClickTag = {
                                        onClickTag(it)
                                    },
                                    onChangeBackground = onChangeBackground
                                )
                            } else if (action.name == "TAG_BLDEVIS") {
                                ButtonWithDropdownMenu(
                                    action,
                                    listOf(
                                        actionTagDraft(),
                                        actionTagSentMasc(),
                                        actionTagCancelledMasc(),
                                        actionTagInvoiced(),
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
    // Fixed-height icon slot so every label lands on the same baseline no matter
    // how big or small the individual icon is (e.g. the tag icon on selection is
    // 16dp while categories/more/duplicate are 24dp — without the slot the small
    // ones float up and the labels stagger).
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            action.icon?.let {
                Icon(
                    it,
                    modifier = if (iconSize != null) Modifier.size(iconSize) else Modifier,
                    tint = action.iconColor ?: LocalContentColor.current,
                    contentDescription = action.description
                )
            }
        }

        action.label?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.textTiny,
            )
        }
    }
}
