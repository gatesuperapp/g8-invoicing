package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.a4a.g8invoicing.ui.screens.ExportPdfPlatform
import com.a4a.g8invoicing.ui.states.InvoiceState
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.ParametersDefinition

/**
 * Shared NavGraph for both Android and iOS.
 * Platform-specific callbacks (like email sending) should be passed as parameters.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    // Platform-specific callbacks
    onSendReminder: (InvoiceState) -> Unit = {},
    // On desktop, hide category button since we have sidebar
    showCategoryButton: Boolean = true,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.InvoiceList.name,
    ) {
        // About
        about(
            navController = navController,
            onClickCategory = {
                navController.navigateAndReplaceStartDestination(it)
            },
            onClickBack = {
                navigateBack(navController)
            },
            showCategoryButton = showCategoryButton
        )

        // Delivery Notes
        deliveryNoteList(
            navController = navController,
            onClickCategory = {
                navController.navigateAndReplaceStartDestination(it)
            },
            onClickListItem = {
                val params = "?itemId=$it"
                navController.navigate(Screen.DeliveryNoteAddEdit.name + params)
            },
            onClickNew = {
                navController.navigate(Screen.DeliveryNoteAddEdit.name)
            },
            onClickBack = {
                navigateBack(navController)
            },
            showCategoryButton = showCategoryButton
        )

        // Invoices
        invoiceList(
            navController = navController,
            onClickCategory = {
                navController.navigateAndReplaceStartDestination(it)
            },
            onClickListItem = {
                val params = "?itemId=$it"
                navController.navigate(Screen.InvoiceAddEdit.name + params)
            },
            onClickNew = {
                navController.navigate(Screen.InvoiceAddEdit.name)
            },
            onClickBack = {
                navigateBack(navController)
            },
            onSendReminder = onSendReminder,
            showCategoryButton = showCategoryButton,
            showBottomBar = showCategoryButton // Same logic: hide on desktop
        )

        // Credit Notes
        creditNoteList(
            navController = navController,
            onClickCategory = {
                navController.navigateAndReplaceStartDestination(it)
            },
            onClickListItem = {
                val params = "?itemId=$it"
                navController.navigate(Screen.CreditNoteAddEdit.name + params)
            },
            onClickNew = {
                navController.navigate(Screen.CreditNoteAddEdit.name)
            },
            onClickBack = {
                navigateBack(navController)
            },
            showCategoryButton = showCategoryButton
        )

        // Clients/Issuers
        clientOrIssuerList(
            navController = navController,
            onClickCategory = {
                navController.navigateAndReplaceStartDestination(it)
            },
            onClickListItem = {
                val params = "?itemId=${it.id}&type=client"
                navController.navigate(Screen.ClientAddEdit.name + params)
            },
            onClickNew = {
                val params = "?type=client"
                navController.navigate(Screen.ClientAddEdit.name + params)
            },
            onClickBack = {
                navigateBack(navController)
            },
            showCategoryButton = showCategoryButton
        )

        clientAddEdit(
            navController = navController,
            goToPreviousScreen = {
                navigateBack(navController)
            }
        )

        deliveryNoteAddEdit(
            navController = navController,
            onClickBack = {
                navigateBack(navController)
            },
            exportPdfContent = { document, onDismiss ->
                ExportPdfPlatform(document, onDismiss)
            }
        )

        invoiceAddEdit(
            navController = navController,
            onClickBack = {
                navigateBack(navController)
            },
            exportPdfContent = { document, onDismiss ->
                ExportPdfPlatform(document, onDismiss)
            }
        )

        creditNoteAddEdit(
            navController = navController,
            onClickBack = {
                navigateBack(navController)
            },
            exportPdfContent = { document, onDismiss ->
                ExportPdfPlatform(document, onDismiss)
            }
        )

        // Products
        productList(
            navController = navController,
            onClickCategory = {
                navController.navigateAndReplaceStartDestination(it)
            },
            onClickListItem = {
                val params = "?itemId=${it.id}&type=product"
                navController.navigate("ProductCreation$params") {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onClickNew = {
                navController.navigate("ProductCreation")
            },
            onClickBack = {
                // No-op for product list back
            },
            showCategoryButton = showCategoryButton
        )

        // Product Add/Edit NESTED NAVIGATION
        navigation(
            startDestination = Screen.ProductAddEdit.name + "?itemId={itemId}&type={type}",
            route = "ProductCreation?itemId={itemId}&type={type}",
            arguments = listOf(
                navArgument("itemId") { nullable = true },
                navArgument("type") { nullable = true },
            )
        ) {
            productAddEdit(
                navController = navController,
                onClickBack = {
                    navigateBack(navController)
                },
                onClickForward = {
                    navController.navigate(Screen.ProductTaxRates.name) {
                        popUpTo("ProductCreation") {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )

            productTaxRates(
                navController = navController,
                onClickBackOrSelect = {
                    navController.navigate(Screen.ProductAddEdit.name) {
                        popUpTo("ProductCreation") {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

private fun navigateBack(navController: NavController) {
    navController.popBackStack()
}

private fun NavHostController.navigateAndReplaceStartDestination(
    category: Category,
) {
    // Use route string instead of Int ID for KMP compatibility
    val currentStartRoute = graph.startDestinationRoute
    if (currentStartRoute != null) {
        popBackStack(currentStartRoute, true)
    }
    graph.setStartDestination(category.route)
    navigate(category.route)
}

/**
 * Helper to share ViewModel across nested navigation graph.
 */
@Composable
inline fun <reified T : ViewModel> androidx.navigation.NavBackStackEntry.sharedViewModel(
    navController: NavController,
    noinline parameters: ParametersDefinition? = null
): T {
    val navGraphRoute = destination.parent?.route ?: return koinViewModel(parameters = parameters)
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return koinViewModel(viewModelStoreOwner = parentEntry, parameters = parameters)
}
