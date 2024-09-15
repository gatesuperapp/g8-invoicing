package com.a4a.g8invoicing.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument

/*
Process to add a new screen/VM/datasource:
1. Adding screen to navgraph
- Add the screen name to the Screen enum class
- Add the extension (fun NavGraphBuilder.newScreen)
- call extension from NavHost
2. Annotate the VM with hilt stuff
3. Add datasource in AppModule
*/

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController,
        //startDestination = (Screen.About.name),
          startDestination = (Screen.InvoiceList.name),
        // startDestination = (Screen.CreditNoteList.name),
        //startDestination = (Screen.DeliveryNoteList.name),
        //startDestination = (Screen.ProductList.name),
        //  startDestination = (Screen.DeliveryNoteAddEdit.name),
        // startDestination = (Screen.ClientOrIssuerAddEdit.name),
        // startDestination = (Screen.ClientOrIssuerList.name),
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        about(
            navController = navController,
            onClickCategory = {
                navController.navigateAndReplaceStartDestination(it)
            },
            onClickBack = {
                navigateBack(navController)
            }
        )
        account(
            navController = navController,
            onClickCategory = {
                navController.navigateAndReplaceStartDestination(it)
            },
            onClickBack = {
                navigateBack(navController)
            }
        )

        deliveryNoteList(
            navController = navController,
            onClickCategory = {
                navController.navigateAndReplaceStartDestination(it)
            },
            onClickListItem = {
                val params = ("?itemId=${it}")
                navController.navigate(Screen.DeliveryNoteAddEdit.name + params)
            },
            onClickNew = {
                navController.navigate(Screen.DeliveryNoteAddEdit.name)
            },
            onClickBack = {
                navigateBack(navController)
            }
        )
        invoiceList(
            navController = navController,
            onClickCategory = {
                navController.navigateAndReplaceStartDestination(it)
            },
            onClickListItem = {
                val params = ("?itemId=${it}")
                navController.navigate(Screen.InvoiceAddEdit.name + params)
            },
            onClickNew = {
                navController.navigate(Screen.InvoiceAddEdit.name)
            },
            onClickBack = {
                navigateBack(navController)
            }
        )
        creditNoteList(
            navController = navController,
            onClickCategory = {
                navController.navigateAndReplaceStartDestination(it)
            },
            onClickListItem = {
                val params = ("?itemId=${it}")
                navController.navigate(Screen.CreditNoteAddEdit.name + params)
            },
            onClickNew = {
                navController.navigate(Screen.CreditNoteAddEdit.name)
            },
            onClickBack = {
                navigateBack(navController)
            }
        )
        clientOrIssuerList(
            navController = navController,
            onClickCategory = {
                navController.navigateAndReplaceStartDestination(it)
            },
            onClickListItem = {
                val params = ("?itemId=${it.id}&type=client")
                navController.navigate(Screen.ClientAddEdit.name + params)
            },
            onClickNew = {
                val params = ("?type=client")
                navController.navigate(Screen.ClientAddEdit.name + params)
            },
            onClickBack = {
                navigateBack(navController)
            }
        )
        clientAddEdit(
            navController = navController,
            goToPreviousScreen = { key, result ->
                navigateBackWithResult(navController, key, result)
            }
        )
        deliveryNoteAddEdit(
            navController = navController,
            onClickBack = {
                navigateBack(navController)
            }
        )
        invoiceAddEdit(
            navController = navController,
            onClickBack = {
                navigateBack(navController)
            }
        )
        creditNoteAddEdit(
            navController = navController,
            onClickBack = {
                navigateBack(navController)
            }
        )
        productList(
            navController = navController,
            onClickCategory = {
                navController.navigateAndReplaceStartDestination(it)
            },
            onClickListItem = {
                val params = ("?itemId=${it.productId}&type=product")
                //TODO remove as useless, the start destination is already the product dest..
                navController.navigate("ProductCreation$params") {
                    // Pop up to the start destination of the graph to
                    // avoid building up a large stack of destinations
                    // on the back stack as users select items
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    // Avoid multiple copies of the same destination when
                    // re-selecting the same item
                    launchSingleTop = true
                    // Restore state when reselecting a previously selected item
                    restoreState = true
                }
            },
            onClickNew = {
                navController.navigate("ProductCreation") {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onClickBack = {
                navigateBack(navController)
            }
        )

        // Product Add/Edit NESTED NAVIGATION.
        // ****The parent/start destination route is named "ProductCreation"****
        // and contains "ProductAddEdit" & "TaxRates"
        // The goal is to share the ViewModel between those 2 screens
        // The argument "isNewProduct" indicates if it's accessed through the "Add new" button,
        // or if it's an update of an existing product
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
                    navigateToPreviousNestedScreen(
                        navController,
                        Screen.ProductAddEdit.name,
                        "ProductCreation"
                    )
                }
            )
        }
    }
}

private fun navigateBack(navController: NavController) {
    navController.popBackStack()
}

private fun navigateBackWithResult(
    navController: NavController,
    key: String,
    value: Pair<String, String>,
) {
    navController.previousBackStackEntry
        ?.savedStateHandle
        ?.set(key, value)
    navController.popBackStack()
}

private fun navigateToPreviousNestedScreen(
    navController: NavController,
    previousScreen: String,
    route: String,
) {
    navController.navigate(previousScreen) {
        popUpTo(route) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavHostController.navigateAndReplaceStartDestination(
    category: Category,
) {
    // The chosen category becomes the new start destination
    popBackStack(graph.startDestinationId, true)
    graph.setStartDestination(category.route)
    navigate(category.route)
}


@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navController: NavController): T {
    val navGraphRoute = destination.parent?.route ?: return hiltViewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return hiltViewModel(parentEntry)
}