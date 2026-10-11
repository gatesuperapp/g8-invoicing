package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.document_modal_product_save
import com.a4a.g8invoicing.shared.resources.product_categories_title
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.screens.shared.CATEGORY_COLOR_UNSET
import com.a4a.g8invoicing.ui.screens.shared.CategoryPastelPalette
import com.a4a.g8invoicing.ui.states.ProductCategoryState
import com.a4a.g8invoicing.ui.theme.AppColors
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProductCategories(
    navController: NavController,
    categories: List<ProductCategoryState>,
    currentCategoryId: Int?,
    onSelectCategory: (ProductCategoryState?) -> Unit,
    onClickBack: () -> Unit,
    onSaveCategories: (List<ProductCategoryState>) -> Unit,
) {
    val scrollState = rememberScrollState()
    var isEditMode by remember { mutableStateOf(false) }

    // Local editable copy — same pattern as ProductTaxRates: Validate commits
    // the current list through onSaveCategories (which splits insert / update /
    // delete), Cancel resets to the latest snapshot of [categories].
    var editingCategories by remember(categories) {
        mutableStateOf(categories)
    }

    // When the user taps the gear from an empty picker, seed two blank rows
    // so the first-category creation feels like filling a form (not fighting
    // the "Ajouter une catégorie" button). Two visibly different swatches
    // (blue / green) so the user sees the colour-per-category concept
    // immediately — rows seeded with the same colour read like a mistake.
    val onClickEditWithSeed: () -> Unit = {
        if (editingCategories.isEmpty()) {
            editingCategories = listOf(
                ProductCategoryState(
                    id = null,
                    name = "",
                    color = CategoryPastelPalette[0], // blue
                ),
                ProductCategoryState(
                    id = null,
                    name = "",
                    color = CategoryPastelPalette[3], // green
                ),
            )
        }
        isEditMode = true
    }

    // First palette swatch not already used by an existing row, so a freshly
    // added category reads as visually distinct. Falls back to palette[0] when
    // every colour is already in use.
    fun pickUnusedColor(current: List<ProductCategoryState>): Long {
        val used = current.map {
            if (it.color == CATEGORY_COLOR_UNSET) CategoryPastelPalette.first() else it.color
        }.toSet()
        return CategoryPastelPalette.firstOrNull { it !in used } ?: CategoryPastelPalette.first()
    }

    Scaffold(
        topBar = {
            if (isEditMode) {
                CategoriesEditTopBar(
                    navController = navController,
                    onClickCancel = {
                        editingCategories = categories
                        isEditMode = false
                    },
                    onClickValidate = {
                        // Drop rows left with an empty name (user tapped + but
                        // never typed) — same silent-drop rule as tax rates.
                        onSaveCategories(editingCategories.filter { it.name.isNotBlank() })
                        isEditMode = false
                    }
                )
            } else {
                CategoriesTopBar(
                    navController = navController,
                    onClickBackArrow = onClickBack
                )
            }
        }
    ) {
        it
        Column(
            modifier = Modifier
                .background(AppColors.divider)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .padding(top = 110.dp)
        ) {
            if (isEditMode) {
                ProductCategoriesEditContent(
                    categories = editingCategories,
                    onUpdateCategory = { index, next ->
                        editingCategories = editingCategories.toMutableList().apply {
                            this[index] = next
                        }
                    },
                    onAddCategory = {
                        editingCategories = editingCategories +
                            ProductCategoryState(
                                id = null,
                                name = "",
                                color = pickUnusedColor(editingCategories),
                            )
                    },
                    onDeleteCategory = { index ->
                        editingCategories = editingCategories.toMutableList().apply {
                            removeAt(index)
                        }
                    }
                )
            } else {
                ProductCategoriesContent(
                    categories = categories,
                    currentCategoryId = currentCategoryId,
                    onSelectCategory = onSelectCategory,
                    onClickEditCategories = onClickEditWithSeed,
                )
            }

            Spacer(modifier = Modifier.height(200.dp))
        }
    }
}

@Composable
private fun CategoriesTopBar(
    navController: NavController,
    onClickBackArrow: () -> Unit,
) {
    TopBar(
        title = stringResource(Res.string.product_categories_title),
        navController = navController,
        onClickBackArrow = onClickBackArrow,
        isCancelCtaDisplayed = false
    )
}

@Composable
private fun CategoriesEditTopBar(
    navController: NavController,
    onClickCancel: () -> Unit,
    onClickValidate: () -> Unit,
) {
    TopBar(
        title = "",
        navController = navController,
        onClickBackArrow = onClickCancel,
        isCancelCtaDisplayed = true,
        ctaText = stringResource(Res.string.document_modal_product_save),
        ctaTextDisabled = true,
        onClickCtaValidate = onClickValidate
    )
}
