package com.a4a.g8invoicing.ui.shared

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.a4a.g8invoicing.ui.navigation.BottomBarAction
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.navigation.actionCategories
import com.a4a.g8invoicing.ui.navigation.actionConvert
import com.a4a.g8invoicing.ui.navigation.actionDelete
import com.a4a.g8invoicing.ui.navigation.actionDuplicate
import com.a4a.g8invoicing.ui.navigation.actionMarkAsPaid
import com.a4a.g8invoicing.ui.navigation.actionNew
import com.a4a.g8invoicing.ui.navigation.actionUnselectAll

@Composable
fun BottomBar(
    navController: NavController,
    isButtonNewDisplayed: Boolean = true,
    isListItemSelected: Boolean = false,
    onClickDuplicate: () -> Unit = {},
    onClickMarkAsPaid: () -> Unit = {},
    onClickDelete: () -> Unit = {},
    onClickUnselectAll: () -> Unit = {},
    onClickNew: () -> Unit = {},
    onClickCategory: (Category) -> Unit = {},
    onClickConvert: () -> Unit = {},
    isConvertible: Boolean = false,
    isInvoice: Boolean = false,
) {
    BottomBarAction(
        navController,
        appBarActions = if (!isListItemSelected) {
            if (isButtonNewDisplayed) {
                arrayOf(
                    actionNew(onClick = { onClickNew() }),
                    actionCategories()
                )
            } else arrayOf(
                actionCategories()
            )
        } else if (isInvoice) {
            arrayOf(
                actionUnselectAll(
                    onClick = { onClickUnselectAll() }
                ),
                actionDuplicate(
                    onClick = { onClickDuplicate() }
                ),
                actionDelete(
                    onClick = { onClickDelete() }
                ),
                actionMarkAsPaid(
                    onClick = { onClickMarkAsPaid() }
                )
            )
        } else if (isConvertible) {
            arrayOf(
                actionUnselectAll(
                    onClick = { onClickUnselectAll() }
                ),
                actionDuplicate(
                    onClick = { onClickDuplicate() }
                ),
                actionDelete(
                    onClick = { onClickDelete() }
                ),
                actionConvert(
                    onClick = { onClickConvert() }
                )
            )
        } else {
        arrayOf(
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