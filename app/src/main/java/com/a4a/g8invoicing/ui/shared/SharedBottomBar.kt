package com.a4a.g8invoicing.ui.shared

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.a4a.g8invoicing.ui.navigation.BottomBarAction
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.navigation.actionCategories
import com.a4a.g8invoicing.ui.navigation.actionDelete
import com.a4a.g8invoicing.ui.navigation.actionDuplicate
import com.a4a.g8invoicing.ui.navigation.actionNew
import com.a4a.g8invoicing.ui.navigation.actionUnselectAll

@Composable
fun SharedBottomBar(
    navController: NavController,
    selectedMode: Boolean = false,
    onClickDuplicate: () -> Unit = {},
    onClickDelete: () -> Unit = {},
    onClickUnselectAll: () -> Unit = {},
    onClickNew: () -> Unit = {},
    onClickCategory: (Category) -> Unit = {},
) {
    BottomBarAction(
        navController,
        appBarActions = if (!selectedMode) {
            // no client selected
            arrayOf(
                actionNew(
                    onClick = { onClickNew() }
                ),
                actionCategories()
            )
        } else {
            arrayOf(
                // TODO add tags
                actionUnselectAll(
                    onClick = { onClickUnselectAll() }
                ),
                actionDuplicate(
                    onClick = { onClickDuplicate() }
                ),
                actionDelete(
                    onClick = { onClickDelete() }
                )
            )
        },
        onClickCategory
    )
}