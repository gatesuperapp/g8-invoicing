package com.a4a.g8invoicing.ui.shared

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.a4a.g8invoicing.ui.navigation.BottomBarAction
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.navigation.actionArrowRight
import com.a4a.g8invoicing.ui.navigation.actionCategories
import com.a4a.g8invoicing.ui.navigation.actionConvert
import com.a4a.g8invoicing.ui.navigation.actionCreateCorrectedInvoice
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
    onChangeBackground: () -> Unit,
    isCategoriesMenuOpen: Boolean = false,
    onCategoriesMenuOpenChange: (Boolean) -> Unit = {},
    showCategoryButton: Boolean = true, // Hidden on desktop when sidebar is shown
) {
    BottomBarAction(
        navController,
        appBarActions =
        if (numberOfItemsSelected == 0) {
            if (isButtonNewDisplayed && showCategoryButton) {
                arrayOf(
                    actionNew(onClickNew),
                    actionCategories()
                )
            } else if (isButtonNewDisplayed) {
                arrayOf(
                    actionNew(onClickNew)
                )
            } else if (showCategoryButton) {
                arrayOf(
                    actionCategories()
                )
            } else {
                arrayOf()
            }
        } else if (isInvoice && numberOfItemsSelected > 1) {
            arrayOf(
                actionUnselectAll(onClickUnselectAll),
                actionDuplicate(onClickDuplicate),
                actionDelete(onClickDelete),
                actionArrowRight(onClickCreateCreditNote),
                actionCreateCorrectedInvoice(onClickCreateCorrectedInvoice),
                actionTag()
            )
        } else if (isInvoice && numberOfItemsSelected == 1) {
            arrayOf(
                actionUnselectAll(onClickUnselectAll),
                actionDuplicate(onClickDuplicate),
                actionDelete(onClickDelete),
                actionArrowRight(onClickCreateCreditNote),
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
        onChangeBackground,
        isCategoriesMenuOpen,
        onCategoriesMenuOpenChange
    )
}
