package com.a4a.g8invoicing.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.IndeterminateCheckBox
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.*
import com.a4a.g8invoicing.ui.theme.ColorCancelled
import com.a4a.g8invoicing.ui.theme.ColorGreenPaid
import com.a4a.g8invoicing.ui.theme.ColorGreyDraft
import com.a4a.g8invoicing.ui.theme.ColorRedLate
import com.a4a.g8invoicing.ui.theme.ColorReminded
import com.a4a.g8invoicing.ui.theme.ColorSent
import org.jetbrains.compose.resources.stringResource

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
    val label: String? = null,
    val description: String = "",
    val tag: DocumentTag? = null,
    val isSecondary: Boolean = false,
    val alignmentLeft: Boolean = false,
    val onClick: () -> Unit = {},
    val onClickOpenModal: @Composable () -> Unit = {},
)

@Composable
fun actionCategories() =
    AppBarAction(
        name = "CATEGORIES",
        icon = Icons.Filled.Apps,
        description = stringResource(Res.string.appbar_categories),
        label = stringResource(Res.string.appbar_menu_label),
        alignmentLeft = true,
        onClick = {
            // see CategoriesDropdownMenu
        }
    )

@Composable
fun actionNew(onClick: () -> Unit) =
    AppBarAction(
        icon = Icons.Filled.PostAdd,
        description = stringResource(Res.string.appbar_new),
        label = stringResource(Res.string.appbar_new_label),
        onClick = onClick
    )

@Composable
fun actionEdit() =
    AppBarAction(
        icon = Icons.Filled.Edit,
        description = stringResource(Res.string.appbar_edit),
        onClick = {}
    )

@Composable
fun actionDuplicate(onClick: () -> Unit) =
    AppBarAction(
        icon = Icons.Filled.ContentCopy,
        description = stringResource(Res.string.appbar_duplicate),
        label = stringResource(Res.string.appbar_duplicate_label),
        onClick = onClick
    )

@Composable
fun actionSavePayment(onClick: () -> Unit) =
    AppBarAction(
        icon = Icons.Filled.AttachMoney,
        description = stringResource(Res.string.appbar_save_payment),
        label = stringResource(Res.string.appbar_save_payment_label),
        onClick = onClick
    )

@Composable
fun actionDelete(onClick: () -> Unit) =
    AppBarAction(
        icon = Icons.Outlined.DeleteOutline,
        description = stringResource(Res.string.appbar_delete),
        label = stringResource(Res.string.appbar_delete), // Use same string for label
        isSecondary = true,
        onClick = onClick
    )

@Composable
fun actionArrowRight(onClick: () -> Unit) =
    AppBarAction(
        icon = Icons.AutoMirrored.Filled.ArrowForward,
        description = stringResource(Res.string.appbar_create_credit_note),
        isSecondary = true,
        onClick = onClick
    )

@Composable
fun actionCreateCorrectedInvoice(onClick: () -> Unit) =
    AppBarAction(
        icon = Icons.AutoMirrored.Filled.ArrowForward,
        description = stringResource(Res.string.appbar_create_corrected_invoice),
        isSecondary = true,
        onClick = onClick
    )

@Composable
fun actionConvert(onClick: () -> Unit) =
    AppBarAction(
        icon = Icons.AutoMirrored.Filled.ArrowForward,
        description = stringResource(Res.string.appbar_convert),
        label = stringResource(Res.string.appbar_convert_label),
        onClick = onClick
    )

@Composable
fun actionMore() =
    AppBarAction(
        icon = Icons.Filled.MoreVert,
        description = stringResource(Res.string.appbar_more),
        label = stringResource(Res.string.appbar_more_label),
    )

@Composable
fun actionExport(onClick: () -> Unit) =
    AppBarAction(
        icon = Icons.Filled.IosShare,
        description = stringResource(Res.string.appbar_export),
        isSecondary = false,
        onClick = onClick
    )

@Composable
fun actionUnselectAll(onClick: () -> Unit) =
    AppBarAction(
        icon = Icons.Outlined.IndeterminateCheckBox,
        description = stringResource(Res.string.appbar_unselect_all),
        label = stringResource(Res.string.appbar_unselect_all_label),
        isSecondary = false,
        alignmentLeft = true,
        onClick = onClick
    )

@Composable
fun actionTag() =
    AppBarAction(
        name = "TAG",
        icon = Icons.Outlined.Sell,
        description = stringResource(Res.string.appbar_label_description),
        label = stringResource(Res.string.appbar_tag),
        isSecondary = false,
    )

@Composable
fun actionTagUndefined() =
    AppBarAction(
        tag = DocumentTag.UNDEFINED,
        icon = Icons.Filled.Circle,
        iconColor = Color.White,
        iconBorder = ColorGreyDraft,
        description = stringResource(Res.string.appbar_tag_draft),
        label = stringResource(Res.string.appbar_tag_draft),
        onClick = {} // see ButtonWithDropdownMenu
    )

@Composable
fun actionTagDraft() =
    AppBarAction(
        tag = DocumentTag.DRAFT,
        icon = Icons.Filled.Circle,
        iconColor = ColorGreyDraft,
        description = stringResource(Res.string.appbar_tag_draft),
        label = stringResource(Res.string.appbar_tag_draft),
        onClick = {} // see ButtonWithDropdownMenu
    )

@Composable
fun actionTagSent() =
    AppBarAction(
        tag = DocumentTag.SENT,
        icon = Icons.Filled.Circle,
        iconColor = ColorSent,
        description = stringResource(Res.string.appbar_tag_sent),
        label = stringResource(Res.string.appbar_tag_sent),
        onClick = {} // see ButtonWithDropdownMenu
    )

@Composable
fun actionTagPaid() =
    AppBarAction(
        tag = DocumentTag.PAID,
        icon = Icons.Filled.Circle,
        iconColor = ColorGreenPaid,
        description = stringResource(Res.string.appbar_tag_paid),
        label = stringResource(Res.string.appbar_tag_paid),
        onClick = {} // see ButtonWithDropdownMenu
    )

@Composable
fun actionTagLate() =
    AppBarAction(
        tag = DocumentTag.LATE,
        icon = Icons.Filled.Circle,
        iconColor = ColorRedLate,
        description = stringResource(Res.string.appbar_tag_late),
        label = stringResource(Res.string.appbar_tag_late),
        onClick = {} // see ButtonWithDropdownMenu
    )

@Composable
fun actionTagReminded() =
    AppBarAction(
        tag = DocumentTag.REMINDED,
        icon = Icons.Filled.Circle,
        iconColor = ColorReminded,
        description = stringResource(Res.string.appbar_tag_reminded),
        label = stringResource(Res.string.appbar_tag_reminded),
        onClick = {} // see ButtonWithDropdownMenu
    )

@Composable
fun actionTagCancelled() =
    AppBarAction(
        tag = DocumentTag.CANCELLED,
        icon = Icons.Filled.Circle,
        iconColor = ColorCancelled,
        description = stringResource(Res.string.appbar_tag_cancelled),
        label = stringResource(Res.string.appbar_tag_cancelled),
        onClick = {} // see ButtonWithDropdownMenu
    )

@Composable
fun actionSendReminder(onClick: () -> Unit) =
    AppBarAction(
        icon = Icons.Outlined.Email,
        description = stringResource(Res.string.appbar_send_reminder),
        label = stringResource(Res.string.appbar_send_reminder),
        isSecondary = false,
        onClick = onClick
    )

@Composable
fun actionTextElements(onClick: () -> Unit) =
    AppBarAction(
        icon = Icons.Filled.TextFields,
        label = stringResource(Res.string.appbar_text_label),
        description = stringResource(Res.string.appbar_components),
        isSecondary = false,
        alignmentLeft = false,
        onClick = onClick
    )

@Composable
fun actionItems(onClick: () -> Unit) =
    AppBarAction(
        icon = Icons.AutoMirrored.Filled.List,
        label = stringResource(Res.string.appbar_list_label),
        description = stringResource(Res.string.appbar_list),
        isSecondary = false,
        alignmentLeft = false,
        onClick = onClick
    )

@Composable
fun actionStyle(onClick: () -> Unit) =
    AppBarAction(
        icon = Icons.Outlined.Brush,
        label = stringResource(Res.string.appbar_style_label),
        description = stringResource(Res.string.appbar_text),
        isSecondary = false,
        alignmentLeft = false,
        onClick = onClick
    )
