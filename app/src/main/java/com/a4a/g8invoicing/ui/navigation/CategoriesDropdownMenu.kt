package com.a4a.g8invoicing.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.a4a.g8invoicing.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import com.a4a.g8invoicing.ui.theme.MainBackground


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
        //  Category.Home,
        Category.About,
        Category.Clients,
        Category.Products,
        Category.CreditNotes,
        Category.DeliveryNotes,
        Category.Invoices,
        //Category.MyAccount,

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
                text = { Text(stringResource(category.resourceId)) },
                onClick = {
                    dismissMenu()
                    if (onClickCategory != null) {
                        onClickCategory(category)
                    }
                },
                leadingIcon = {
                    category.icon?.let {
                        Icon(
                            modifier = Modifier
                                .padding(end = 10.dp),
                            //.size(30.dp),
                            imageVector = it,
                            contentDescription = category.iconDescription
                        )
                    }
                }
            )
        }
    }
}

sealed class Category(
    val route: String,
    @StringRes val resourceId: Int,
    val icon: ImageVector?,
    val iconDescription: String?,
) {
    //object Home : Category(Screen.HomeScreen.name, R.string.home)
    data object Clients : Category(Screen.ClientOrIssuerList.name, R.string.appbar_client_list, null, null)

    data object Products : Category(Screen.ProductList.name, R.string.appbar_products, null, null)
    data object Invoices : Category(Screen.InvoiceList.name, R.string.appbar_invoices, null, null)
    data object CreditNotes : Category(Screen.CreditNoteList.name, R.string.appbar_credit_notes, null, null)
    data object DeliveryNotes : Category(Screen.DeliveryNoteList.name, R.string.appbar_delivery_notes, null, null)
    data object MyAccount : Category(Screen.Account.name, R.string.appbar_account, null, null)
    data object About : Category(Screen.About.name, R.string.appbar_about, null, null)

}


