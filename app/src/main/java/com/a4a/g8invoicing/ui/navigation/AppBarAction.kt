package com.a4a.g8invoicing.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.IndeterminateCheckBox
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.theme.ColorCancelled
import com.a4a.g8invoicing.ui.theme.ColorGreenPaid
import com.a4a.g8invoicing.ui.theme.ColorGreyDraft
import com.a4a.g8invoicing.ui.theme.ColorRedLate
import com.a4a.g8invoicing.ui.theme.ColorReminded
import com.a4a.g8invoicing.ui.theme.ColorSent


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
    val icon: ImageVector? = null,
    val iconColor: Color? = null,
    val iconBorder: Color? = null,
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
        icon = Icons.Filled.Apps,
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
        icon = Icons.Filled.PostAdd,
        description = R.string.appbar_new,
        label = R.string.appbar_new_label,
        onClick = onClick
    )

fun actionEdit() =
    AppBarAction(
        icon = Icons.Filled.Edit,
        description = R.string.appbar_edit,
        onClick = {
        }
    )

fun actionDuplicate(onClick: () -> Unit) =
    AppBarAction(
        icon = Icons.Filled.ContentCopy,
        description = R.string.appbar_duplicate,
        label = R.string.appbar_duplicate_label,
        onClick = onClick
    )

fun actionSavePayment(onClick: () -> Unit) =
    AppBarAction(
        icon = Icons.Filled.AttachMoney,
        description = R.string.appbar_save_payment,
        label = R.string.appbar_save_payment_label,
        onClick = onClick
    )

fun actionDelete(onClick: () -> Unit) =
    AppBarAction(
        icon = Icons.Outlined.DeleteOutline,
        description = R.string.appbar_delete,
        isSecondary = true,
        onClick = onClick
    )

fun actionArrowRight(onClick: () -> Unit) =
    AppBarAction(
        icon = Icons.AutoMirrored.Filled.ArrowForward,
        description = R.string.appbar_create_credit_note,
        isSecondary = true,
        onClick = onClick
    )

fun actionCreateCorrectedInvoice(onClick: () -> Unit) =
    AppBarAction(
        icon = Icons.AutoMirrored.Filled.ArrowForward,
        description = R.string.appbar_create_corrected_invoice,
        isSecondary = true,
        onClick = onClick
    )

fun actionConvert(onClick: () -> Unit) =
    AppBarAction(
        icon = Icons.AutoMirrored.Filled.ArrowForward,
        description = R.string.appbar_convert,
        label = R.string.appbar_convert_label,
        onClick = onClick
    )

fun actionMore() =
    AppBarAction(
        icon = Icons.Filled.MoreVert,
        description = R.string.appbar_more,
        label = R.string.appbar_more_label,
    )

fun actionExport(onClick: () -> Unit) =
    AppBarAction(
        icon = Icons.Filled.IosShare,
        description = R.string.appbar_export,
        isSecondary = false,
        onClick = onClick
    )

fun actionUnselectAll(onClick: () -> Unit) =
    AppBarAction(
        icon = Icons.Outlined.IndeterminateCheckBox,
        description = R.string.appbar_unselect_all,
        label = R.string.appbar_unselect_all_label,
        isSecondary = false,
        alignmentLeft = true,
        onClick = onClick
    )

fun actionTag() =
    AppBarAction(
        name = "TAG",
        icon = Icons.Outlined.Sell,
        description = R.string.appbar_label_description,
        label = R.string.appbar_tag,
        isSecondary = false,
    )

fun actionTagUndefined() =
    AppBarAction(
        tag = DocumentTag.UNDEFINED,
        icon = Icons.Filled.Circle,
        iconColor = Color.White,
        iconBorder = ColorGreyDraft,
        description = R.string.appbar_tag_draft,
        label = R.string.appbar_tag_draft,
        onClick = {} // see ButtonWithDropdownMenu
    )

fun actionTagDraft() =
    AppBarAction(
        tag = DocumentTag.DRAFT,
        icon = Icons.Filled.Circle,
        iconColor = ColorGreyDraft,
        description = R.string.appbar_tag_draft,
        label = R.string.appbar_tag_draft,
        onClick = {} // see ButtonWithDropdownMenu
    )


fun actionTagSent() =
    AppBarAction(
        tag = DocumentTag.SENT,
        icon = Icons.Filled.Circle,
        iconColor = ColorSent,
        description = R.string.appbar_tag_sent,
        label = R.string.appbar_tag_sent,
        onClick = {} // see ButtonWithDropdownMenu
    )


fun actionTagPaid() =
    AppBarAction(
        tag = DocumentTag.PAID,
        icon = Icons.Filled.Circle,
        iconColor = ColorGreenPaid,
        description = R.string.appbar_tag_paid,
        label = R.string.appbar_tag_paid,
        onClick = {} // see ButtonWithDropdownMenu
    )


fun actionTagLate() =
    AppBarAction(
        tag = DocumentTag.LATE,
        icon = Icons.Filled.Circle,
        iconColor = ColorRedLate,
        description = R.string.appbar_tag_late,
        label = R.string.appbar_tag_late,
        onClick = {} // see ButtonWithDropdownMenu
    )

fun actionTagReminded() =
    AppBarAction(
        tag = DocumentTag.REMINDED,
        icon = Icons.Filled.Circle,
        iconColor = ColorReminded,
        description = R.string.appbar_tag_reminded,
        label = R.string.appbar_tag_reminded,
        onClick = {} // see ButtonWithDropdownMenu
    )

fun actionTagCancelled() =
    AppBarAction(
        tag = DocumentTag.CANCELLED,
        icon = Icons.Filled.Circle,
        iconColor = ColorCancelled,
        description = R.string.appbar_tag_cancelled,
        label = R.string.appbar_tag_cancelled,
        onClick = {} // see ButtonWithDropdownMenu
    )

fun actionSendReminder(onClick: () -> Unit) =
    AppBarAction(
        icon = Icons.Outlined.Mail,
        description = R.string.appbar_send_reminder,
        label = R.string.appbar_send_reminder,
        isSecondary = false,
        onClick = onClick
    )

fun actionTextElements(onClick: () -> Unit) =
    AppBarAction(
        icon = Icons.Filled.TextFields,
        label = R.string.appbar_text_label,
        description = R.string.appbar_components,
        isSecondary = false,
        alignmentLeft = false,
        onClick = onClick
    )


fun actionItems(onClick: () -> Unit) =
    AppBarAction(
        icon = Icons.AutoMirrored.Filled.List,
        label = R.string.appbar_list_label,
        description = R.string.appbar_list,
        isSecondary = false,
        alignmentLeft = false,
        onClick = onClick
    )

fun actionStyle(onClick: () -> Unit) =
    AppBarAction(
        icon = Icons.Outlined.Brush,
        label = R.string.appbar_style_label,
        description = R.string.appbar_text,
        isSecondary = false,
        alignmentLeft = false,
        onClick = onClick
    )