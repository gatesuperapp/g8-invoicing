package com.a4a.g8invoicing.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.BottomAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.ui.states.ProductCategoryState

@Composable
fun BottomBarAction(
    navController: NavController,
    appBarActions: Array<AppBarAction>? = null,
    onClickCategory: (Category) -> Unit = { },
    onClickTag: (DocumentTag) -> Unit,
    onChangeBackground: () -> Unit,
    isCategoriesMenuOpen: Boolean = false,
    onCategoriesMenuOpenChange: (Boolean) -> Unit = {},
    productCategoriesForBulk: List<ProductCategoryState> = emptyList(),
    onPickBulkCategory: (ProductCategoryState?) -> Unit = {},
) {
    BottomAppBar(
        actions = {
            BottomBarActionView(
                navController,
                appBarActions,
                onClickCategory,
                onClickTag,
                onChangeBackground,
                isCategoriesMenuOpen,
                onCategoriesMenuOpenChange,
                productCategoriesForBulk,
                onPickBulkCategory,
            )
        },
        contentPadding = PaddingValues(
            top = 12.dp, bottom = 12.dp, start = 24.dp, end = 12.dp
        )
    )
}
