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
import com.a4a.g8invoicing.ui.screens.ExportResult
import com.a4a.g8invoicing.ui.states.DocumentState
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
    // PDF export - passed from app module (Android) or platform-specific implementation
    exportPdfContent: @Composable (DocumentState, () -> Unit) -> Unit,
    // What's New dialog
    showWhatsNew: Boolean = false,
    onWhatsNewDismissed: () -> Unit = {},
    // 1.8 Onboarding wizard (fullscreen, non-dismissable)
    showOnboarding: Boolean = false,
    onOnboardingDismissed: () -> Unit = {},
    // About screen platform callbacks
    onShareContent: (String) -> Unit = {},
    onExportDatabase: () -> ExportResult = { ExportResult.Error("Not available on this platform") },
    onSendDatabaseByEmail: (String) -> Unit = {},
    onComposeEmail: (String, String, String) -> Unit = { _, _, _ -> },
    // Magic link token from a deep link (Android only — null on desktop/iOS). Forwarded
    // to the Account screen so its own scoped VM consumes it and owns the result UI.
    pendingMagicLinkToken: String? = null,
    onMagicLinkTokenConsumed: () -> Unit = {},
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
            onShareContent = onShareContent,
            onComposeEmail = onComposeEmail,
            showCategoryButton = showCategoryButton
        )

        // Account (magic link auth + subscription)
        account(
            navController = navController,
            onClickCategory = {
                navController.navigateAndReplaceStartDestination(it)
            },
            onClickBack = {
                navigateBack(navController)
            },
            onShareContent = onShareContent,
            onExportDatabase = onExportDatabase,
            onSendDatabaseByEmail = onSendDatabaseByEmail,
            pendingMagicLinkToken = pendingMagicLinkToken,
            onMagicLinkTokenConsumed = onMagicLinkTokenConsumed,
        )

        // gStore (premium modules showcase, no commercial CTA)
        gStore(
            navController = navController,
            onClickCategory = {
                navController.navigateAndReplaceStartDestination(it)
            },
            onClickBack = {
                navigateBack(navController)
            },
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
            onClickViewCreatedInvoice = { invoiceId ->
                val params = "?itemId=$invoiceId"
                navController.navigate(Screen.InvoiceAddEdit.name + params)
            },
            showCategoryButton = showCategoryButton
        )

        // Quotes
        quoteList(
            navController = navController,
            onClickCategory = {
                navController.navigateAndReplaceStartDestination(it)
            },
            onClickListItem = {
                val params = "?itemId=$it"
                navController.navigate(Screen.QuoteAddEdit.name + params)
            },
            onClickNew = {
                navController.navigate(Screen.QuoteAddEdit.name)
            },
            onClickBack = {
                navigateBack(navController)
            },
            onClickViewCreatedInvoice = { invoiceId ->
                val params = "?itemId=$invoiceId"
                navController.navigate(Screen.InvoiceAddEdit.name + params)
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
            showBottomBar = showCategoryButton, // Same logic: hide on desktop
            initialShowWhatsNew = showWhatsNew,
            onWhatsNewDismissed = onWhatsNewDismissed,
            initialShowOnboarding = showOnboarding,
            onOnboardingDismissed = onOnboardingDismissed,
            onExportDatabase = onExportDatabase,
            onSendDatabaseByEmail = onSendDatabaseByEmail,
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
                exportPdfContent(document, onDismiss)
            }
        )

        quoteAddEdit(
            navController = navController,
            onClickBack = {
                navigateBack(navController)
            },
            exportPdfContent = { document, onDismiss ->
                exportPdfContent(document, onDismiss)
            }
        )

        invoiceAddEdit(
            navController = navController,
            onClickBack = {
                navigateBack(navController)
            },
            exportPdfContent = { document, onDismiss ->
                exportPdfContent(document, onDismiss)
            }
        )

        creditNoteAddEdit(
            navController = navController,
            onClickBack = {
                navigateBack(navController)
            },
            exportPdfContent = { document, onDismiss ->
                exportPdfContent(document, onDismiss)
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
    // Protect against double-click: only pop if there's a valid back stack entry
    if (navController.currentBackStackEntry != null &&
        navController.previousBackStackEntry != null) {
        navController.popBackStack()
    }
}

private fun NavHostController.navigateAndReplaceStartDestination(
    category: Category,
) {
    // Pop the current top of the backstack and push [category] so back button
    // from any category exits the app. We deliberately do NOT mutate
    // graph.startDestination — a NavGraph recomposition (e.g. after the async
    // setSeenOnboarding18 DataStore write emits a new value) reinstantiates the
    // NavHost builder lambda, invalidates `remember(startDestination, builder)`
    // and rebuilds the graph with the hard-coded InvoiceList start, which
    // would silently teleport the user back to InvoiceList.
    val current = currentBackStackEntry?.destination?.route
    navigate(category.route) {
        if (current != null && current != category.route) {
            popUpTo(current) { inclusive = true }
        }
        launchSingleTop = true
    }
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
