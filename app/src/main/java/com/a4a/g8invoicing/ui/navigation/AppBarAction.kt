package com.a4a.g8invoicing.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.shared.icons.IconArrowForward
import com.a4a.g8invoicing.ui.shared.icons.IconBrush
import com.a4a.g8invoicing.ui.shared.icons.IconCircle
import com.a4a.g8invoicing.ui.shared.icons.IconExport
import com.a4a.g8invoicing.ui.shared.icons.IconList
import com.a4a.g8invoicing.ui.shared.icons.IconText
import com.a4a.g8invoicing.ui.theme.ColorCancelled
import com.a4a.g8invoicing.ui.theme.ColorReminded
import com.a4a.g8invoicing.ui.theme.ColorGreenPaid
import com.a4a.g8invoicing.ui.theme.ColorGreyDraft
import com.a4a.g8invoicing.ui.theme.ColorRedLate
import com.a4a.g8invoicing.ui.theme.ColorSent
import com.ninetyninepercent.funfactu.icons.IconApps
import com.ninetyninepercent.funfactu.icons.IconCheckboxUnselect
import com.ninetyninepercent.funfactu.icons.IconDollar
import com.ninetyninepercent.funfactu.icons.IconDuplicate
import com.ninetyninepercent.funfactu.icons.IconLabel

import icons.IconDelete
import icons.IconDone
import icons.IconEdit
import icons.IconMoreThreeDots
import icons.IconNew

/**
 * Contains:
 * + a data class with the properties of an action button
 * + methods for all action buttons.
 *
 * Action buttons can be clicked by user in app bars.
 *
 * onClick methods are defined in callers.
 *
 */

data class AppBarAction(
    val name: String = "",
    val icon: ImageVector,
    val iconColor: Color? = null,
    @StringRes val label: Int? = null,
    @StringRes val description: Int,
    val tag: DocumentTag? = null,
    val isSecondary: Boolean = false,
    val alignmentLeft: Boolean = false,
    val onClick: () -> Unit = {},
    val onClickOpenModal: @Composable () -> Unit = {},
)

fun actionCategories() =
    AppBarAction(
        icon = IconApps,
        description = R.string.appbar_categories,
        label = R.string.appbar_menu_label,
        alignmentLeft = true,
        onClick = {
            // see CategoriesDropdownMenu
        }
    )

//onClickCategory: (Category) -> Unit = null
fun actionNew(onClick: () -> Unit) =
    AppBarAction(
        icon = IconNew,
        description = R.string.appbar_new,
        label = R.string.appbar_new_label,
        onClick = onClick
    )

fun actionEdit() =
    AppBarAction(
        icon = IconEdit,
        description = R.string.appbar_edit,
        onClick = {
        }
    )

fun actionDuplicate(onClick: () -> Unit) =
    AppBarAction(
        icon = IconDuplicate,
        description = R.string.appbar_duplicate,
        label = R.string.appbar_duplicate_label,
        onClick = onClick
    )

fun actionSavePayment(onClick: () -> Unit) =
    AppBarAction(
        icon = IconDollar,
        description = R.string.appbar_save_payment,
        label = R.string.appbar_save_payment_label,
        onClick = onClick
    )

fun actionDelete(onClick: () -> Unit) =
    AppBarAction(
        icon = IconDelete,
        description = R.string.appbar_delete,
        isSecondary = true,
        onClick = onClick
    )

fun actionCreateCreditNote(onClick: () -> Unit) =
    AppBarAction(
        icon = IconArrowForward,
        description = R.string.appbar_create_credit_note,
        isSecondary = true,
        onClick = onClick
    )

fun actionCreateCorrectedInvoice(onClick: () -> Unit) =
    AppBarAction(
        icon = IconArrowForward,
        description = R.string.appbar_create_corrected_invoice,
        isSecondary = true,
        onClick = onClick
    )

fun actionConvert(onClick: () -> Unit) =
    AppBarAction(
        icon = IconArrowForward,
        description = R.string.appbar_convert,
        label = R.string.appbar_convert_label,
        onClick = onClick
    )

fun actionMore() =
    AppBarAction(
        icon = IconMoreThreeDots,
        description = R.string.appbar_more,
        label = R.string.appbar_more_label,
    )

fun actionDone(onClick: () -> Unit) =
    AppBarAction(
        icon = IconDone,
        description = R.string.appbar_save,
        isSecondary = false,
        onClick = onClick
    )

fun actionExport(onClick: () -> Unit) =
    AppBarAction(
        icon = IconExport,
        description = R.string.appbar_export,
        isSecondary = false,
        onClick = onClick
    )

fun actionUnselectAll(onClick: () -> Unit) =
    AppBarAction(
        icon = IconCheckboxUnselect,
        description = R.string.appbar_unselect_all,
        label = R.string.appbar_unselect_all_label,
        isSecondary = false,
        alignmentLeft = true,
        onClick = onClick
    )

fun actionTag() =
    AppBarAction(
        name = "TAG",
        icon = IconLabel,
        description = R.string.appbar_label_description,
        label = R.string.appbar_label,
        isSecondary = false,
    )

fun actionTagDraft() =
    AppBarAction(
        tag = DocumentTag.DRAFT,
        icon = IconCircle,
        iconColor = ColorGreyDraft,
        description = R.string.appbar_tag_draft,
        label = R.string.appbar_tag_draft,
        onClick = {} // see ButtonWithDropdownMenu
    )


fun actionTagSent() =
    AppBarAction(
        tag = DocumentTag.SENT,
        icon = IconCircle,
        iconColor = ColorSent,
        description = R.string.appbar_tag_sent,
        label = R.string.appbar_tag_sent,
        onClick = {} // see ButtonWithDropdownMenu
    )


fun actionTagPaid() =
    AppBarAction(
        tag = DocumentTag.PAID,
        icon = IconCircle,
        iconColor = ColorGreenPaid,
        description = R.string.appbar_tag_paid,
        label = R.string.appbar_tag_paid,
        onClick = {} // see ButtonWithDropdownMenu
    )


fun actionTagLate() =
    AppBarAction(
        tag = DocumentTag.LATE,
        icon = IconCircle,
        iconColor = ColorRedLate,
        description = R.string.appbar_tag_late,
        label = R.string.appbar_tag_late,
        onClick = {} // see ButtonWithDropdownMenu
    )

fun actionTagReminded() =
    AppBarAction(
        tag = DocumentTag.REMINDED,
        icon = IconCircle,
        iconColor = ColorReminded,
        description = R.string.appbar_tag_reminded,
        label = R.string.appbar_tag_reminded,
        onClick = {} // see ButtonWithDropdownMenu
    )

fun actionTagCancelled() =
    AppBarAction(
        tag = DocumentTag.CANCELLED,
        icon = IconCircle,
        iconColor = ColorCancelled,
        description = R.string.appbar_tag_cancelled,
        label = R.string.appbar_tag_cancelled,
        onClick = {} // see ButtonWithDropdownMenu
    )


fun actionTextElements(onClick: () -> Unit) =
    AppBarAction(
        icon = IconText,
        label = R.string.appbar_text_label,
        description = R.string.appbar_components,
        isSecondary = false,
        alignmentLeft = false,
        onClick = onClick
    )


fun actionItems(onClick: () -> Unit) =
    AppBarAction(
        icon = IconList,
        label = R.string.appbar_list_label,
        description = R.string.appbar_list,
        isSecondary = false,
        alignmentLeft = false,
        onClick = onClick
    )


fun actionStyle(onClick: () -> Unit) =
    AppBarAction(
        icon = IconBrush,
        label = R.string.appbar_style_label,
        description = R.string.appbar_text,
        isSecondary = false,
        alignmentLeft = false,
        onClick = onClick
    )