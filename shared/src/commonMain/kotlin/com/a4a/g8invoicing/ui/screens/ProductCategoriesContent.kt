package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.product_categories_add_cta
import com.a4a.g8invoicing.shared.resources.product_categories_empty_hint
import com.a4a.g8invoicing.shared.resources.product_categories_name_placeholder
import com.a4a.g8invoicing.ui.screens.shared.CATEGORY_COLOR_UNSET
import com.a4a.g8invoicing.ui.screens.shared.CategoryColorPickerDialog
import com.a4a.g8invoicing.ui.screens.shared.resolveCategoryColor
import com.a4a.g8invoicing.ui.shared.Separator
import com.a4a.g8invoicing.ui.states.ProductCategoryState
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.textBodySmall
import com.a4a.g8invoicing.ui.theme.textSecondary
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProductCategoriesContent(
    categories: List<ProductCategoryState>,
    currentCategoryId: Int?,
    onSelectCategory: (ProductCategoryState?) -> Unit,
    onClickEditCategories: (() -> Unit)? = null,
    // Bottom-sheet mode mirrors ProductTaxRatesContent: fills the same
    // half-height area as the product form so swapping between the form and
    // the picker doesn't resize the sheet (otherwise ModalBottomSheet
    // re-measures and the content area flashes white for a frame).
    isDisplayedInBottomSheet: Boolean = false,
    // Suppresses the "aucune catégorie" hint while the Flow hasn't emitted
    // yet. The surface still renders at its full half-height so the sheet
    // doesn't resize; only the hint text is deferred until data lands.
    isLoading: Boolean = false,
) {
    var chosenId by remember(currentCategoryId) { mutableStateOf(currentCategoryId) }
    val scrollState = rememberScrollState()
    // Picker always shows categories A→Z — the edit screen (ProductCategoriesEditContent)
    // keeps user order so rows don't shuffle while renaming.
    val sortedCategories = remember(categories) {
        categories.sortedBy { it.name.trim().lowercase() }
    }

    val containerModifier = if (isDisplayedInBottomSheet) {
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f)
            .background(AppColors.surfaceMuted.copy(alpha = 0.4f))
            .verticalScroll(scrollState)
            .padding(top = 30.dp, end = 60.dp, start = 60.dp, bottom = 30.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(12.dp)
    }

    Column(
        modifier = containerModifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Column(
            modifier = Modifier
                .background(color = AppColors.surface, shape = RoundedCornerShape(6.dp))
                .fillMaxWidth()
        ) {
            if (sortedCategories.isEmpty() && !isLoading) {
                // No categories yet — point the user at the gear icon rather
                // than offering an unusable dash row.
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    text = stringResource(Res.string.product_categories_empty_hint),
                    style = MaterialTheme.typography.textBodySmall.copy(
                        color = AppColors.textSecondary,
                    ),
                )
            } else {
                sortedCategories.forEachIndexed { index, category ->
                    CategoryRow(
                        isFirst = index == 0,
                        isLast = index == sortedCategories.lastIndex,
                        isSelected = chosenId == category.id,
                        leading = category.color,
                        label = category.name,
                        onClick = {
                            chosenId = category.id
                            onSelectCategory(category)
                        },
                    )
                }
            }
        }

        // Gear icon — toggles into edit/management mode. Pattern mirrors
        // ProductTaxRatesContent so users land on a familiar affordance.
        // Hidden while the Flow is still loading so the gear doesn't flash
        // at the top of the sheet before the categories list pushes it down.
        if (onClickEditCategories != null && !isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Box(
                    modifier = Modifier.background(
                        color = AppColors.surfaceMuted,
                        shape = RoundedCornerShape(6.dp),
                    )
                ) {
                    IconButton(
                        onClick = onClickEditCategories,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = AppColors.iconSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(
    isFirst: Boolean,
    isLast: Boolean,
    isSelected: Boolean,
    leading: Long?, // category colour (ARGB), or null for the dash row
    label: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) AppColors.accent.copy(alpha = 0.10f) else Color.Transparent,
            )
            .clickable(onClick = onClick),
    ) {
        if (!isFirst) Separator()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = if (isFirst) 24.dp else 16.dp,
                    bottom = if (isLast) 24.dp else 16.dp,
                )
        ) {
            if (leading != null) {
                // Chip — coloured background behind the category name, matching
                // the product-list and form chip style. Replaces the earlier
                // pastille-plus-plain-text row (too noisy once the chip itself
                // carries the colour signal).
                Box(
                    modifier = Modifier
                        .weight(1F, fill = false)
                        .clip(RoundedCornerShape(8.dp))
                        .background(resolveCategoryColor(leading))
                        .padding(horizontal = 10.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.textSecondary.copy(
                            color = Color(0xFF1A1A1A),
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            } else {
                Text(
                    modifier = Modifier.weight(1F),
                    text = label,
                    style = LocalTextStyle.current,
                )
            }
        }
    }
}

@Composable
fun ProductCategoriesEditContent(
    categories: List<ProductCategoryState>,
    onUpdateCategory: (Int, ProductCategoryState) -> Unit,
    onAddCategory: () -> Unit,
    onDeleteCategory: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Column(
            modifier = Modifier
                .background(color = AppColors.surface, shape = RoundedCornerShape(6.dp))
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
        ) {
            categories.forEachIndexed { index, category ->
                CategoryEditRow(
                    category = category,
                    autoFocus = index == 0 && category.name.isBlank() && category.id == null,
                    onUpdateCategory = { onUpdateCategory(index, it) },
                    onDeleteCategory = { onDeleteCategory(index) },
                )
                if (index < categories.lastIndex) Separator()
            }
        }

        // "Ajouter une catégorie" — right-aligned, same bubble as the tax-rate
        // add CTA so the two management screens feel like siblings.
        Box(
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 2.dp)
                .background(
                    color = AppColors.surfaceSubtle,
                    shape = RoundedCornerShape(6.dp),
                )
                .clickable(onClick = onAddCategory)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textPrimary),
                text = AnnotatedString(stringResource(Res.string.product_categories_add_cta)),
            )
        }
    }
}

@Composable
private fun CategoryEditRow(
    category: ProductCategoryState,
    onUpdateCategory: (ProductCategoryState) -> Unit,
    onDeleteCategory: () -> Unit,
    autoFocus: Boolean = false,
) {
    val initialName = category.name
    var textFieldValue by remember(category.id) {
        mutableStateOf(
            TextFieldValue(
                text = initialName,
                selection = TextRange(initialName.length),
            )
        )
    }
    var isEditing by remember(category.id) { mutableStateOf(category.name.isBlank()) }
    var colorPickerOpen by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Only one of the seeded rows should auto-request focus on initial
    // composition — otherwise both race and the last one wins. Fires once at
    // startup for the autoFocus row; the regular user "tap Edit" path below
    // handles later re-entries into edit mode.
    LaunchedEffect(Unit) {
        if (autoFocus && isEditing) focusRequester.requestFocus()
    }
    var seenEditingBefore by remember { mutableStateOf(isEditing) }
    LaunchedEffect(isEditing) {
        if (isEditing && seenEditingBefore.not()) {
            focusRequester.requestFocus()
        }
        seenEditingBefore = isEditing
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
    ) {
        // Colour square prefix — click opens the pastel picker. Default
        // (CATEGORY_COLOR_UNSET) renders as the palette's first swatch.
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(resolveCategoryColor(category.color))
                .clickable { colorPickerOpen = true },
        )
        Spacer(Modifier.width(12.dp))

        if (isEditing) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    onUpdateCategory(category.copy(name = newValue.text))
                },
                modifier = Modifier
                    .weight(1F)
                    .padding(end = 8.dp)
                    .focusRequester(focusRequester),
                textStyle = LocalTextStyle.current.copy(color = AppColors.textPrimary),
                cursorBrush = SolidColor(AppColors.textPrimary),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (textFieldValue.text.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.product_categories_name_placeholder),
                            style = LocalTextStyle.current.copy(color = AppColors.textMuted),
                        )
                    }
                    innerTextField()
                },
            )
            IconButton(
                onClick = { isEditing = false },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Done,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
        } else {
            Text(
                modifier = Modifier
                    .weight(1F)
                    .padding(end = 8.dp),
                text = category.name,
                style = LocalTextStyle.current,
            )
            IconButton(
                onClick = { isEditing = true },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = AppColors.iconSecondary,
                )
            }
        }

        Spacer(Modifier.width(4.dp))
        IconButton(
            onClick = onDeleteCategory,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = AppColors.iconSecondary,
            )
        }
    }

    if (colorPickerOpen) {
        CategoryColorPickerDialog(
            currentColor = category.color,
            onPick = { argb ->
                onUpdateCategory(category.copy(color = argb))
            },
            onDismiss = { colorPickerOpen = false },
        )
    }
}
