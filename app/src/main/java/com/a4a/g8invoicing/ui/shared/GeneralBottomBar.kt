package com.a4a.g8invoicing.ui.shared

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.a4a.g8invoicing.ui.navigation.BottomBarAction
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.navigation.actionCategories
import com.a4a.g8invoicing.ui.navigation.actionConvert
import com.a4a.g8invoicing.ui.navigation.actionCreateCorrectedInvoice
import com.a4a.g8invoicing.ui.navigation.actionCreateCreditNote
import com.a4a.g8invoicing.ui.navigation.actionDelete
import com.a4a.g8invoicing.ui.navigation.actionDuplicate
import com.a4a.g8invoicing.ui.navigation.actionNew
import com.a4a.g8invoicing.ui.navigation.actionSendReminder
import com.a4a.g8invoicing.ui.navigation.actionTag
import com.a4a.g8invoicing.ui.navigation.actionUnselectAll

@Composable
fun GeneralBottomBar(
    navController: NavController,
    isButtonNewDisplayed: Boolean = true,
    selectedMode: Boolean = false,
    numberOfItemsSelected: Int = 0,
    onClickDuplicate: () -> Unit = {},
    onClickDelete: () -> Unit = {},
    onClickCreateCreditNote: () -> Unit = {},
    onClickCreateCorrectedInvoice: () -> Unit = {},
    onClickUnselectAll: () -> Unit = {},
    onClickNew: () -> Unit = {},
    onClickCategory: (Category) -> Unit = {},
    onClickTag: (DocumentTag) -> Unit = {},
    onClickConvert: () -> Unit = {},
    onClickSendReminder: () -> Unit = {},
    isConvertible: Boolean = false,
    isInvoice: Boolean = false,
    onChangeBackground: () -> Unit = {},
    ) {
    BottomBarAction(
        navController,
        appBarActions =
        if (numberOfItemsSelected == 0) {
            if (isButtonNewDisplayed) {
                arrayOf(
                    actionNew(onClickNew),
                    actionCategories()
                )
            } else arrayOf(
                actionCategories()
            )
        } else if (isInvoice && numberOfItemsSelected > 1) {
            arrayOf(
                actionUnselectAll(onClickUnselectAll),
                actionDuplicate(onClickDuplicate),
                actionDelete(onClickDelete),
                actionCreateCreditNote(onClickCreateCreditNote),
                actionCreateCorrectedInvoice(onClickCreateCorrectedInvoice),
                actionTag()
            )
        } else if (isInvoice && numberOfItemsSelected == 1) {
            arrayOf(
                actionUnselectAll(onClickUnselectAll),
                actionDuplicate(onClickDuplicate),
                actionDelete(onClickDelete),
                actionCreateCreditNote(onClickCreateCreditNote),
                actionCreateCorrectedInvoice(onClickCreateCorrectedInvoice),
                actionTag(),
                actionSendReminder(onClickSendReminder)
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
        onClickCategory,
        onClickTag,
        onChangeBackground
    )
}