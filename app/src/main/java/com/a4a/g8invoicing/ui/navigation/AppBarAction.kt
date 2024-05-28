package com.a4a.g8invoicing.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.shared.icons.IconBrush
import com.a4a.g8invoicing.ui.shared.icons.IconComponents
import com.a4a.g8invoicing.ui.shared.icons.IconExport
import com.a4a.g8invoicing.ui.shared.icons.IconText
import com.ninetyninepercent.funfactu.icons.IconApps
import com.ninetyninepercent.funfactu.icons.IconCheckboxUnselect
import com.ninetyninepercent.funfactu.icons.IconDuplicate
import com.ninetyninepercent.funfactu.icons.IconLabel

import icons.IconDelete
import icons.IconDone
import icons.IconEdit
import icons.IconNew

/**
 * Contains:
 * + a data class with the properties of an action button
 * + a list of all action buttons.
 *
 * Action buttons can be clicked by user in app bars.
 *
 * onClick methods are defined in callers.
 *
 */

data class AppBarAction(
    val icon: ImageVector,
    @StringRes val label: Int? = null,
    @StringRes val description: Int,
    val isInDropDownMenu: Boolean,
    val alignmentLeft: Boolean = false,
    val onClick: () -> Unit,
)

@Composable
fun actionCategories() =
    AppBarAction(
        icon = IconApps,
        description = R.string.appbar_categories,
        isInDropDownMenu = false,
        alignmentLeft = true,
        onClick = {
            // see CategoriesDropdownMenu
        }
    )

//onClickCategory: (Category) -> Unit = null
@Composable
fun actionNew(onClick: () -> Unit) =
    AppBarAction(
        icon = IconNew,
        description = R.string.appbar_new,
        isInDropDownMenu = false,
        onClick = onClick
    )

@Composable
fun actionEdit() =
    AppBarAction(
        icon = IconEdit,
        description = R.string.appbar_edit,
        isInDropDownMenu = false,
        onClick = {
        }
    )

@Composable
fun actionDuplicate(onClick: () -> Unit) =
    AppBarAction(
        icon = IconDuplicate,
        description = R.string.appbar_duplicate,
        isInDropDownMenu = false,
        onClick = onClick
    )

@Composable
fun actionDelete(onClick: () -> Unit) =
    AppBarAction(
        icon = IconDelete,
        description = R.string.appbar_delete,
        isInDropDownMenu = true,
        onClick = onClick
    )

@Composable
fun actionDone(onClick: () -> Unit) =
    AppBarAction(
        icon = IconDone,
        description = R.string.appbar_save,
        isInDropDownMenu = false,
        onClick = onClick
    )

@Composable
fun actionExport(onClick: () -> Unit) =
    AppBarAction(
        icon = IconExport,
        description = R.string.appbar_export,
        isInDropDownMenu = false,
        onClick = onClick
    )

@Composable
fun actionUnselectAll(onClick: () -> Unit) =
    AppBarAction(
        icon = IconCheckboxUnselect,
        description = R.string.appbar_unselect_all,
        isInDropDownMenu = false,
        alignmentLeft = true,
        onClick = onClick
    )

@Composable
fun actionTag(onClick: () -> Unit) =
    AppBarAction(
        icon = IconLabel,
        description = R.string.appbar_label,
        isInDropDownMenu = false,
        onClick = onClick
    )

@Composable
fun actionComponents(onClick: () -> Unit) =
    AppBarAction(
        icon = IconText,
        label = R.string.appbar_text_label,
        description = R.string.appbar_components,
        isInDropDownMenu = false,
        alignmentLeft = false,
        onClick = onClick
    )

@Composable
fun actionStyle(onClick: () -> Unit) =
    AppBarAction(
        icon = IconBrush,
        label = R.string.appbar_style_label,
        description = R.string.appbar_text,
        isInDropDownMenu = false,
        alignmentLeft = false,
        onClick = onClick
    )