package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.navigation.NavController
import com.a4a.g8invoicing.data.auth.ActivatedModulesRepository
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.appbar_products
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.screens.shared.ScaffoldWithDimmedOverlay
import com.a4a.g8invoicing.ui.screens.shared.filterProductsByQuery
import com.a4a.g8invoicing.ui.shared.AlertDialogDeleteDocument
import com.a4a.g8invoicing.ui.shared.GeneralBottomBar
import com.a4a.g8invoicing.ui.shared.PlatformBackHandler
import com.a4a.g8invoicing.ui.states.ProductCategoryState
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.states.ProductsUiState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject


@Composable
fun ProductList(
    navController: NavController,
    productsUiState: ProductsUiState,
    onClickDelete: (List<ProductState>) -> Unit,
    onClickDuplicate: (List<ProductState>) -> Unit,
    onClickNew: () -> Unit,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (ProductState) -> Unit,
    onClickBack: () -> Unit,
    // Bulk-marker action. Non-null + categoriesModuleOn → the "Marquer" entry
    // appears in the selection bottom bar and opens the category picker dialog.
    onBulkMarkCategory: ((List<ProductState>, ProductCategoryState?) -> Unit)? = null,
    fetchCategories: suspend () -> List<ProductCategoryState> = { emptyList() },
    // Callbacks for platform-specific back handling
    onCategoriesMenuOpen: () -> Unit = {},
    isCategoriesMenuOpen: Boolean = false,
    onCategoriesMenuOpenChange: (Boolean) -> Unit = {},
    showCategoryButton: Boolean = true,
) {
    val modulesRepo: ActivatedModulesRepository = koinInject()
    val activatedModules by modulesRepo.state.collectAsState()
    val categoriesModuleOn =
        ActivatedModulesRepository.MODULE_PRODUCT_CATEGORIES in activatedModules
    // Category filter — hoisted here so the TopBar dropdown and the list
    // filtering stay in sync. Refreshed whenever the module toggle flips or
    // the backing products list changes (a rename ripples through
    // product.category via the Product/Category combined flow).
    var allCategories by remember { mutableStateOf<List<ProductCategoryState>>(emptyList()) }
    // Store the id (not the whole state) via rememberSaveable so the filter
    // survives the detail round-trip: the NavBackStackEntry stays alive while
    // the user is on the product detail, and SavedStateHandle persists the
    // primitive across the composition disposal. The full ProductCategoryState
    // is derived from [allCategories] at read time.
    var selectedCategoryFilterId by rememberSaveable { mutableStateOf<Int?>(null) }
    val selectedCategoryFilter: ProductCategoryState? = remember(
        selectedCategoryFilterId, allCategories,
    ) {
        selectedCategoryFilterId?.let { id -> allCategories.firstOrNull { it.id == id } }
    }
    LaunchedEffect(categoriesModuleOn, productsUiState.products) {
        allCategories = if (categoriesModuleOn) fetchCategories() else emptyList()
    }
    // Main list to handle actions with selected items
    val selectedItems = remember { mutableStateListOf<ProductState>() }

    // Will recompose the BottomBar (only) when an item is selected, or when all items are unselected
    val selectedMode = remember { mutableStateOf(false) }
    // Will recompose all the items when clicking "unselect all"
    val keyToResetCheckboxes = remember { mutableStateOf(false) }
    // On delete document
    val openAlertDialog = remember { mutableStateOf(false) }

    // Add background when bottom menu expanded
    val isDimActive = remember { mutableStateOf(false) }

    // Search state — lives at the screen level so the query survives item
    // taps + list recompositions but resets when leaving the tab.
    var searchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    // System back closes the search bar first if it's open.
    PlatformBackHandler(enabled = searchExpanded) {
        searchQuery = TextFieldValue("")
        searchExpanded = false
    }

    // System back clears selection before navigating.
    PlatformBackHandler(enabled = selectedItems.isNotEmpty()) {
        resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
    }

    ScaffoldWithDimmedOverlay(
        isDimmed = isDimActive.value,
        onDismissDim = { isDimActive.value = false },
        topBar = {
            TopBar(
                title = stringResource(Res.string.appbar_products),
                navController = navController,
                onClickBackArrow = {
                    if (selectedItems.isNotEmpty()) {
                        resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
                    } else {
                        onClickBack()
                    }
                },
                isCancelCtaDisplayed = false,
                searchEnabled = selectedItems.isEmpty(),
                searchExpanded = searchExpanded,
                searchQuery = searchQuery,
                categoryFilterEnabled = categoriesModuleOn && selectedItems.isEmpty(),
                categoryFilterOptions = allCategories,
                selectedCategoryFilter = selectedCategoryFilter,
                onCategoryFilterChange = { picked -> selectedCategoryFilterId = picked?.id },
                onSearchToggle = { searchExpanded = !searchExpanded },
                onSearchQueryChange = { searchQuery = it },
            )
        },
        bottomBar = {
            GeneralBottomBar(
                navController = navController,
                numberOfItemsSelected = selectedItems.size,
                onClickDelete = {
                    isDimActive.value = !isDimActive.value
                    openAlertDialog.value = true
                },
                onClickDuplicate = {
                    onClickDuplicate(selectedItems.toList())
                    resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
                },
                onClickUnselectAll = {
                    resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
                },
                onClickNew = { onClickNew() },
                onClickCategory = onClickCategory,
                isProductCategoryPickerEnabled = categoriesModuleOn && onBulkMarkCategory != null,
                productCategoriesForBulk = allCategories,
                onPickBulkCategory = { picked ->
                    onBulkMarkCategory?.invoke(selectedItems.toList(), picked)
                    resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
                },
                onChangeBackground = {
                    isDimActive.value = !isDimActive.value
                },
                isCategoriesMenuOpen = isCategoriesMenuOpen,
                onCategoriesMenuOpenChange = onCategoriesMenuOpenChange,
                showCategoryButton = showCategoryButton
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(
                    padding
                )
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val filteredProducts = remember(
                    productsUiState.products,
                    searchQuery.text,
                    selectedCategoryFilter,
                ) {
                    productsUiState.products
                        .filterProductsByQuery(searchQuery.text)
                        .let { list ->
                            val filterId = selectedCategoryFilter?.id ?: return@let list
                            list.filter { it.category?.id == filterId }
                        }
                }
                // No need to have pull to refresh because it's a flow,
                // thus the list is updated when anything changes in db
                ProductListContent(
                    products = filteredProducts,
                    onProductClick = onClickListItem,
                    addToSelectedList = {
                        selectedItems.add(it)
                        selectedMode.value = true
                    },
                    removeFromSelectedList = {
                        selectedItems.remove(it)
                        if (selectedItems.isEmpty()) {
                            selectedMode.value = false
                        }
                    },
                    keyToUnselectAll = keyToResetCheckboxes.value
                )
            }
        }

        when {
            openAlertDialog.value -> {
                AlertDialogDeleteDocument(
                    onDismissRequest = {
                        openAlertDialog.value = false
                        isDimActive.value = !isDimActive.value
                    },
                    onConfirmation = {
                        openAlertDialog.value = false
                        onClickDelete(selectedItems.toList())
                        selectedItems.clear()
                        selectedMode.value = false
                        isDimActive.value = !isDimActive.value
                    }
                )
            }
        }

    }
}

private fun resetSelectedItems(
    selectedItems: MutableList<ProductState>,
    selectedMode: MutableState<Boolean>,
    triggerRecompose: MutableState<Boolean>,
) {
    selectedItems.clear()
    selectedMode.value = false
    // To "reset" the checkbox rememberValue to false in ProductListItem when recomposing
    triggerRecompose.value = !triggerRecompose.value
}
