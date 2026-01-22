package com.a4a.g8invoicing.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.a4a.g8invoicing.ui.theme.MainBackground
import org.jetbrains.compose.resources.stringResource


/**
 * Menu displaying categories (Home, Products, Clients, Invoices...)
 * Accessed after clicking "Categories" icon
 */

@Composable
fun CategoriesDropdownMenu(
    navController: NavController,
    isExpanded: Boolean,
    dismissMenu: () -> Unit,
    onClickCategory: ((Category) -> Unit)?,
) {
    val categories = listOf(
        Category.G8,
        Category.Clients,
        Category.Products,
        Category.CreditNotes,
        Category.DeliveryNotes,
        Category.Invoices,
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    DropdownMenu(
        modifier = Modifier
            .fillMaxWidth(0.5f),
        expanded = isExpanded,
        onDismissRequest = { dismissMenu() }
    ) {

        categories.forEach { category ->
            val selected = currentDestination?.hierarchy?.any { it.route == category.route } == true

            DropdownMenuItem(
                modifier = if (selected) {
                    Modifier
                        .background(MainBackground)
                } else {
                    Modifier
                        .background(Color.Transparent)
                },
                text = {
                    Text(
                        stringResource(category.resourceId),
                        modifier = Modifier
                            .padding(start = 24.dp)
                    )
                },
                onClick = {
                    dismissMenu()
                    if (onClickCategory != null) {
                        onClickCategory(category)
                    }
                },
            )

            if (category is Category.G8 || category is Category.Products) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 24.dp, end = 16.dp),
                    thickness = 1.dp,
                    color = Color.LightGray.copy(alpha = 0.6f)
                )
            }
        }
    }
}
