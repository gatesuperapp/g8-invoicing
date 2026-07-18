package com.a4a.g8invoicing.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.a4a.g8invoicing.data.auth.ActivatedModulesRepository
import com.a4a.g8invoicing.ui.theme.ColorVioletLight
import org.jetbrains.compose.resources.stringResource
import androidx.compose.runtime.collectAsState
import org.koin.compose.koinInject

/**
 * Permanent sidebar for desktop showing all categories.
 * Hidden on mobile where the dropdown menu is used instead.
 */
@Composable
fun CategorySidebar(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    val activatedModules by koinInject<ActivatedModulesRepository>().state.collectAsState()
    val categories = buildList {
        add(Category.G8)
        add(Category.MyAccount)
        add(Category.GStore)
        add(Category.Clients)
        add(Category.Products)
        add(Category.CreditNotes)
        if (
            ActivatedModulesRepository.MODULE_QUOTE in activatedModules ||
            ActivatedModulesRepository.MODULE_QUOTE_TRIAL in activatedModules
        ) add(Category.Quotes)
        if (ActivatedModulesRepository.MODULE_DELIVERY_NOTE in activatedModules) add(Category.DeliveryNotes)
        add(Category.Invoices)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Surface(
        modifier = modifier
            .width(200.dp)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            categories.forEach { category ->
                val selected = currentDestination?.hierarchy?.any { it.route == category.route } == true

                CategorySidebarItem(
                    category = category,
                    selected = selected,
                    onClick = { onClickCategory(category) }
                )

                if (category is Category.GStore || category is Category.Products) {
                    // Calibrated against the 8dp vertical item padding so the visible
                    // gap around section separators stays consistent with the original.
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        thickness = 1.dp,
                        color = Color.LightGray.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategorySidebarItem(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val icon = getCategoryIcon(category)
    val selectedColor = ColorVioletLight
    val normalColor = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .then(
                if (selected) {
                    Modifier
                        .background(
                            color = selectedColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = selectedColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp), // was vertical=12.dp; tighter while still tappable
        verticalAlignment = Alignment.CenterVertically
    ) {
        // G8 category doesn't have an icon
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) selectedColor else normalColor.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(
            text = stringResource(category.resourceId),
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) selectedColor else normalColor
        )
    }
}

private fun getCategoryIcon(category: Category): ImageVector? {
    return when (category) {
        Category.G8 -> null // No icon for G8
        Category.MyAccount -> Icons.Filled.AccountCircle
        Category.GStore -> Icons.Filled.Star
        Category.Clients -> Icons.Filled.People
        Category.Products -> Icons.Filled.ShoppingBasket
        Category.Invoices -> Icons.Filled.Receipt
        Category.CreditNotes -> Icons.Filled.Description
        Category.DeliveryNotes -> Icons.Filled.LocalShipping
        Category.Quotes -> Icons.Filled.RequestQuote
    }
}
