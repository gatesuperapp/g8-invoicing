package com.a4a.g8invoicing.ui.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.ui.states.ProductCategoryState
import com.a4a.g8invoicing.ui.navigation.BottomBarAction
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.navigation.DocumentTag
import com.a4a.g8invoicing.ui.navigation.actionArrowRight
import com.a4a.g8invoicing.ui.navigation.actionCategories
import com.a4a.g8invoicing.ui.navigation.actionConvert
import com.a4a.g8invoicing.ui.navigation.actionConvertSalesOrder
import com.a4a.g8invoicing.ui.navigation.actionCreateCorrectedInvoice
import com.a4a.g8invoicing.ui.navigation.actionDelete
import com.a4a.g8invoicing.ui.navigation.actionDuplicate
import com.a4a.g8invoicing.ui.navigation.actionLock
import com.a4a.g8invoicing.ui.navigation.actionNew
import com.a4a.g8invoicing.ui.navigation.actionSendReminder
import com.a4a.g8invoicing.ui.navigation.actionTag
import com.a4a.g8invoicing.ui.navigation.actionTagBLDevis
import com.a4a.g8invoicing.ui.navigation.actionTagCategory
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
    onClickConvertToInvoice: () -> Unit = {},
    onClickConvertToDeliveryNote: () -> Unit = {},
    onClickSendReminder: () -> Unit = {},
    isConvertible: Boolean = false,
    isSalesOrder: Boolean = false,
    isInvoice: Boolean = false,
    // BL/Devis-only. When true, adds the "Marquer" bottom-bar entry on the
    // selection bar. Gated by the corresponding gStore module in the caller.
    isTagPickerEnabled: Boolean = false,
    // Products-only (2.1 premium module). Adds a "Marquer" bottom-bar entry
    // that opens the product-category picker instead of a status dropdown.
    isProductCategoryPickerEnabled: Boolean = false,
    productCategoriesForBulk: List<ProductCategoryState> = emptyList(),
    onPickBulkCategory: (ProductCategoryState?) -> Unit = {},
    onChangeBackground: () -> Unit,
    isCategoriesMenuOpen: Boolean = false,
    onCategoriesMenuOpenChange: (Boolean) -> Unit = {},
    showCategoryButton: Boolean = true, // Hidden on desktop when sidebar is shown
    // Invoice-only: fired when the user picks the "Verrouillée" row inside the
    // Marquer dropdown. Toggles Invoice.is_locked independently of the tag.
    onClickLock: (() -> Unit)? = null,
) {
    // On narrow phones the invoice selection bar runs out of room:
    // unselect + duplicate + delete + arrow + corrected + tag + lock
    // (+ send reminder on 1-selection) is 7–8 icons on the right side
    // alone, and the row starts clipping. Move the "Verrouiller" entry
    // into the Plus overflow once the window drops below the threshold —
    // tag + the destructive icons stay primary so the common paths don't
    // move. Threshold tuned on Redmi (fits at ~390–412dp) + Galaxy S8
    // (360dp needs the overflow).
    val windowInfo = androidx.compose.ui.platform.LocalWindowInfo.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val lockInOverflow = with(density) {
        windowInfo.containerSize.width.toDp()
    } < 380.dp
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
                actionTag(),
                actionLock(isSecondary = lockInOverflow) { onClickLock?.invoke() },
            )
        } else if (isInvoice && numberOfItemsSelected == 1) {
            arrayOf(
                actionUnselectAll(onClickUnselectAll),
                actionDuplicate(onClickDuplicate),
                actionDelete(onClickDelete),
                actionArrowRight(onClickCreateCreditNote),
                actionCreateCorrectedInvoice(onClickCreateCorrectedInvoice),
                actionTag(),
                actionLock(isSecondary = lockInOverflow) { onClickLock?.invoke() },
                actionSendReminder(onClickSendReminder)
            )
        } else if (isSalesOrder && isTagPickerEnabled) {
            arrayOf(
                actionUnselectAll(onClickUnselectAll),
                actionDuplicate(onClickDuplicate),
                actionDelete(onClickDelete),
                actionConvertSalesOrder(),
                actionTagBLDevis()
            )
        } else if (isSalesOrder) {
            arrayOf(
                actionUnselectAll(onClickUnselectAll),
                actionDuplicate(onClickDuplicate),
                actionDelete(onClickDelete),
                actionConvertSalesOrder()
            )
        } else if (isConvertible && isTagPickerEnabled) {
            arrayOf(
                actionUnselectAll(onClickUnselectAll),
                actionDuplicate(onClickDuplicate),
                actionDelete(onClickDelete),
                actionConvert(onClickConvert),
                actionTagBLDevis()
            )
        } else if (isConvertible) {
            arrayOf(
                actionUnselectAll(onClickUnselectAll),
                actionDuplicate(onClickDuplicate),
                actionDelete(onClickDelete),
                actionConvert(onClickConvert)
            )
        } else if (isProductCategoryPickerEnabled) {
            arrayOf(
                actionUnselectAll(onClickUnselectAll),
                actionDuplicate(onClickDuplicate),
                actionDelete(onClickDelete),
                // Marquer button renders ButtonWithCategoryDropdown in
                // BottomBarActionView — the outer onClick is dead-weight,
                // the nested dropdown button handles the tap.
                actionTagCategory {},
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
        onCategoriesMenuOpenChange,
        productCategoriesForBulk,
        onPickBulkCategory,
        onClickConvertToInvoice,
        onClickConvertToDeliveryNote,
    )
}
