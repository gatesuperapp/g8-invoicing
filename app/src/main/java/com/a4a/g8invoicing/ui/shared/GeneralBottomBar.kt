package com.a4a.g8invoicing.ui.shared

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.a4a.g8invoicing.ui.navigation.BottomBarAction
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.navigation.actionCategories
import com.a4a.g8invoicing.ui.navigation.actionConvert
import com.a4a.g8invoicing.ui.navigation.actionDelete
import com.a4a.g8invoicing.ui.navigation.actionDuplicate
import com.a4a.g8invoicing.ui.navigation.actionNew
import com.a4a.g8invoicing.ui.navigation.actionTag
import com.a4a.g8invoicing.ui.navigation.actionUnselectAll

@Composable
fun GeneralBottomBar(
    navController: NavController,
    isButtonNewDisplayed: Boolean = true,
    isListItemSelected: Boolean = false,
    onClickDuplicate: () -> Unit = {},
    onClickDelete: () -> Unit = {},
    onClickTag: () -> Unit = {},
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
                    actionNew(onClickNew),
                    actionCategories()
                )
            } else arrayOf(
                actionCategories()
            )
        } else if (isInvoice) {
            arrayOf(
                actionUnselectAll(onClickUnselectAll),
                actionDuplicate(onClickDuplicate),
                actionDelete(onClickDelete),
                actionTag(onClickTag)
            )
        } else if (isConvertible) {
            arrayOf(
                actionUnselectAll(onClickUnselectAll),
                actionDuplicate(onClickDuplicate),
                actionDelete(onClickDelete),
                actionConvert(onClickConvert)
            )
        } else {
            arrayOf(
                actionUnselectAll(onClickUnselectAll),
                actionDuplicate(onClickDuplicate),
                actionDelete(onClickDelete)
            )
        },
        onClickCategory
    )
}