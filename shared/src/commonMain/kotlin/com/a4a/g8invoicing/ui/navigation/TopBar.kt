package com.a4a.g8invoicing.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.draw.alpha
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.currency_picker_search
import com.a4a.g8invoicing.shared.resources.document_modal_product_cancel
import com.a4a.g8invoicing.shared.resources.product_categories_none
import com.a4a.g8invoicing.ui.screens.shared.resolveCategoryColor
import com.a4a.g8invoicing.ui.shared.customTextSelectionColors
import com.a4a.g8invoicing.ui.shared.drawScrollThumb
import com.a4a.g8invoicing.ui.states.ProductCategoryState
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.textBodySmall
import com.a4a.g8invoicing.ui.theme.textCta
import org.jetbrains.compose.resources.stringResource

//Provides back arrow navigation and eventually screen titles.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String? = null,
    appBarAction: AppBarAction? = null,
    appBarActions: Array<AppBarAction>? = null, // Multiple actions for desktop
    ctaText: String? = null,
    ctaTextDisabled: Boolean? = null,
    onClickCtaValidate: () -> Unit = {},
    iconSize: Dp = 24.dp,
    navController: NavController,
    isCancelCtaDisplayed: Boolean,
    onClickBackArrow: () -> Unit,
    // Optional inline search. When [searchEnabled] is true, a magnifier icon
    // appears on the right of the app bar; tapping it swaps the title slot for
    // a TextField (same expand-horizontally + fade pattern as
    // ProductPickerBottomSheet). Leaving [searchEnabled] false keeps the app
    // bar identical to its legacy layout.
    searchEnabled: Boolean = false,
    searchExpanded: Boolean = false,
    searchQuery: TextFieldValue = TextFieldValue(""),
    onSearchToggle: () -> Unit = {},
    onSearchQueryChange: (TextFieldValue) -> Unit = {},
    // Optional category filter rendered just before the search icon. The
    // caller owns the state: pass [categoryFilterOptions] = empty to hide
    // the icon entirely. [selectedCategory] = null means "all categories".
    categoryFilterEnabled: Boolean = false,
    categoryFilterOptions: List<ProductCategoryState> = emptyList(),
    selectedCategoryFilter: ProductCategoryState? = null,
    onCategoryFilterChange: (ProductCategoryState?) -> Unit = {},
    // Optional status/tag filter rendered just before the search icon —
    // mirrors the category filter visually (same width cap, scroll thumb,
    // elevation) but shows document-status dots instead of category chips.
    // Caller passes the options list; selectedTagFilter = null means "all".
    tagFilterEnabled: Boolean = false,
    tagFilterOptions: List<TagFilterOption> = emptyList(),
    selectedTagFilter: DocumentTag? = null,
    onTagFilterChange: (DocumentTag?) -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }
    // Guards the "focus lost → collapse" reaction against the very first
    // onFocusChanged callback (which fires before the field ever gained focus
    // and would slam the search shut on the same frame it opens).
    var focusEverGained by remember(searchExpanded) { mutableStateOf(false) }
    LaunchedEffect(searchExpanded) {
        if (searchEnabled && searchExpanded) focusRequester.requestFocus()
    }

    // Drives the search field's AnimatedVisibility and lets the loupe observe
    // when the exit transition has *actually* finished (fieldState.isIdle +
    // !currentState). Cleaner than delay(220) — the guard survives spec
    // changes and rapid toggles.
    val fieldState = remember { MutableTransitionState(false) }
    fieldState.targetState = searchExpanded

    TopAppBar(
        title = {
            // Title stays put underneath. When the search opens, the opaque
            // TextField slides in from the right on top of it — the title
            // never fades or shifts. Closing reverses the same motion so the
            // title reappears wiped left-to-right from behind.
            // Fixed height matches the TextField (56.dp): without it the Box
            // would grow when the field appears and the TopAppBar would
            // recenter the whole slot, making the title jump upwards.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                title?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                if (searchEnabled) {
                    AnimatedVisibility(
                        visibleState = fieldState,
                        enter = slideInHorizontally(tween(220)) { it },
                        exit = slideOutHorizontally(tween(220)) { it },
                    ) {
                        val keyboardController = LocalSoftwareKeyboardController.current
                        CompositionLocalProvider(
                            LocalTextSelectionColors provides customTextSelectionColors
                        ) {
                            TextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    // Tap the loupe on the IME keyboard: just
                                    // dismiss the keyboard so the results are
                                    // fully visible. The search bar stays open,
                                    // the query stays intact.
                                    onSearch = { keyboardController?.hide() },
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    // Full TopAppBar height minus 4dp of vertical
                                    // breathing room, so descenders (g, p, y…) fit.
                                    .height(56.dp)
                                    .focusRequester(focusRequester)
                                    .onFocusChanged { state ->
                                        if (state.isFocused) {
                                            focusEverGained = true
                                        } else if (focusEverGained && searchExpanded) {
                                            // User tapped outside the field: collapse.
                                            onSearchQueryChange(TextFieldValue(""))
                                            onSearchToggle()
                                        }
                                    },
                                textStyle = MaterialTheme.typography.textBodySmall,
                                placeholder = {
                                    Text(
                                        text = stringResource(Res.string.currency_picker_search),
                                        style = MaterialTheme.typography.textBodySmall.copy(color = Color.Gray),
                                    )
                                },
                                leadingIcon = {
                                    val leadingInteractionSource = remember { MutableInteractionSource() }
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clickable(
                                                interactionSource = leadingInteractionSource,
                                                indication = null,
                                            ) {
                                                onSearchQueryChange(TextFieldValue(""))
                                                onSearchToggle()
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Search,
                                            contentDescription = null,
                                            tint = AppColors.iconSecondary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = AppColors.inputSurface,
                                    unfocusedContainerColor = AppColors.inputSurface,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    cursorColor = AppColors.accent,
                                ),
                            )
                        }
                    }
                }
            }
        },
        actions = {
            // Category filter — rendered to the LEFT of the search icon.
            // Hidden while the search field is expanded (clutters the row).
            // Caller controls visibility via categoryFilterEnabled.
            if (categoryFilterEnabled && !searchExpanded) {
                var menuOpen by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = null,
                            tint = if (selectedCategoryFilter != null) AppColors.accent
                            else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    // Dropdown matches the list's own surface colour (so the
                    // menu + list read as the same palette), then a bumped
                    // shadowElevation draws a soft grey halo around the menu
                    // to lift it visually above the list. drawScrollThumb on
                    // the DropdownMenu's own scrollState gives the user a
                    // right-edge indicator when the category list overflows.
                    val selectedRowBg = AppColors.accent.copy(alpha = 0.15f)
                    val filterScrollState = rememberScrollState()
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                        // widthIn matches the invoice "Marquer" status dropdown
                        // feel — floor so single-word categories don't make a
                        // skinny menu, cap so a long category name ellipses
                        // instead of blowing the dropdown wider than the row.
                        modifier = Modifier
                            .widthIn(min = 200.dp, max = 280.dp)
                            .heightIn(max = 320.dp)
                            .drawScrollThumb(filterScrollState),
                        containerColor = AppColors.surface,
                        shadowElevation = 8.dp,
                        scrollState = filterScrollState,
                    ) {
                        DropdownMenuItem(
                            modifier = Modifier.background(
                                if (selectedCategoryFilter == null) selectedRowBg else Color.Transparent,
                            ),
                            text = {
                                Text(
                                    text = stringResource(Res.string.product_categories_none),
                                    color = AppColors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            onClick = {
                                onCategoryFilterChange(null)
                                menuOpen = false
                            },
                        )
                        categoryFilterOptions.forEach { category ->
                            val isSelected = selectedCategoryFilter?.id == category.id
                            DropdownMenuItem(
                                modifier = Modifier.background(
                                    if (isSelected) selectedRowBg else Color.Transparent,
                                ),
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(resolveCategoryColor(category.color)),
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = category.name,
                                            color = AppColors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                },
                                onClick = {
                                    onCategoryFilterChange(category)
                                    menuOpen = false
                                },
                            )
                        }
                    }
                }
            }

            // Tag/status filter — same visual recipe as the category filter
            // above, but renders the document-tag options (DRAFT, SENT,
            // PAID…) each with its own coloured dot. Enabled by callers that
            // show lists of invoices / BLs / quotes.
            if (tagFilterEnabled && !searchExpanded) {
                var menuOpen by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = null,
                            tint = if (selectedTagFilter != null) AppColors.accent
                            else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    val selectedRowBg = AppColors.accent.copy(alpha = 0.15f)
                    val tagFilterScrollState = rememberScrollState()
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                        modifier = Modifier
                            .widthIn(min = 200.dp, max = 280.dp)
                            .heightIn(max = 320.dp)
                            .drawScrollThumb(tagFilterScrollState),
                        containerColor = AppColors.surface,
                        shadowElevation = 8.dp,
                        scrollState = tagFilterScrollState,
                    ) {
                        DropdownMenuItem(
                            modifier = Modifier.background(
                                if (selectedTagFilter == null) selectedRowBg else Color.Transparent,
                            ),
                            text = {
                                Text(
                                    text = stringResource(Res.string.product_categories_none),
                                    color = AppColors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            onClick = {
                                onTagFilterChange(null)
                                menuOpen = false
                            },
                        )
                        tagFilterOptions.forEach { option ->
                            val isSelected = selectedTagFilter == option.tag
                            DropdownMenuItem(
                                modifier = Modifier.background(
                                    if (isSelected) selectedRowBg else Color.Transparent,
                                ),
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(option.dotColor),
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = option.label,
                                            color = AppColors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                },
                                onClick = {
                                    onTagFilterChange(option.tag)
                                    menuOpen = false
                                },
                            )
                        }
                    }
                }
            }

            // Search magnifier appears before any other action so it stays in
            // the same on-screen spot regardless of selection-mode juggling.
            //
            // Handoff choreography with the TextField's slide animation:
            //   opening: loupe shrinks 22→20 (matches the grey loupe inside
            //     the TextField), then snapTo alpha=0 + fire the toggle, the
            //     TextField slides in over the now-invisible loupe
            //   closing: TextField slides out (fieldCloseMs), THEN the loupe
            //     is snapped back to 20/alpha=1 and animates its grow to 22
            //
            // Driven by Animatable + LaunchedEffect (not animateDpAsState +
            // delayMillis) so rapid clicks / back-button paths don't fight
            // over animation specs.
            //
            // No ripple: an IconButton's indication is baked in and can't be
            // nulled out — Box + clickable(indication = null) is the way.
            if (searchEnabled) {
                val restingSize = 22f
                val fieldIconSize = 20f     // grey loupe inside the TextField's leadingIcon
                val shrinkMs = 140

                val scope = rememberCoroutineScope()
                val iconSize = remember { Animatable(restingSize) }
                val iconAlpha = remember { Animatable(1f) }
                val interactionSource = remember { MutableInteractionSource() }

                // Reappearance: observe the field's transition instead of
                // waiting on a timer. isIdle + !currentState is true exactly
                // one frame after the exit animation completes and the
                // TextField leaves composition — no more "container still
                // there behind the loupe" flash.
                LaunchedEffect(fieldState.isIdle, fieldState.currentState) {
                    if (fieldState.isIdle && !fieldState.currentState && iconAlpha.value == 0f) {
                        iconSize.snapTo(fieldIconSize)
                        iconAlpha.snapTo(1f)
                        iconSize.animateTo(restingSize, tween(shrinkMs))
                    }
                }

                if (!searchExpanded) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                            ) {
                                if (iconSize.isRunning) return@clickable
                                scope.launch {
                                    iconSize.animateTo(fieldIconSize, tween(shrinkMs))
                                    iconAlpha.snapTo(0f)
                                    onSearchToggle()
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = stringResource(Res.string.currency_picker_search),
                            modifier = Modifier
                                .size(iconSize.value.dp)
                                .alpha(iconAlpha.value),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            // Suppress the rest of the actions row while the field is open —
            // it would clutter next to the search TextField.
            if (searchEnabled && searchExpanded) return@TopAppBar

            // Multiple actions (for desktop)
            if (appBarActions != null && appBarActions.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    appBarActions.forEachIndexed { index, action ->
                        // First action (usually "New") gets a prominent outlined button
                        if (index == 0 && action.label != null) {
                            OutlinedButton(
                                onClick = action.onClick,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = AppColors.accent
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = AppColors.accent
                                )
                            ) {
                                action.icon?.let {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = action.description,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(text = action.label)
                            }
                        } else {
                            // Other actions get text buttons
                            androidx.compose.material3.TextButton(
                                onClick = action.onClick,
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                action.icon?.let {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = action.description,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (action.label != null) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                }
                                action.label?.let {
                                    Text(
                                        text = it,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
            // Single action (legacy)
            else if (appBarAction != null) {
                TopBarActionView(
                    appBarAction,
                    iconSize
                )
            } else if (!searchEnabled) {
                TopBarCtaView(
                    onClickCtaValidate,
                    ctaText,
                    ctaTextDisabled
                )
            }
        },
        navigationIcon = {
            if (isCancelCtaDisplayed) {
                Text(
                    style = MaterialTheme.typography.textCta,
                    modifier = Modifier
                        .padding(start = 20.dp)
                        .clickable { onClickBackArrow() },
                    text = stringResource(Res.string.document_modal_product_cancel)
                )
            } else
                BackArrow(
                    navController,
                    onClickBackArrow
                )
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackArrow(
    navController: NavController,
    onClickBackArrow: () -> Unit,
) {
    val currentRoute = navController.currentDestination?.route
    // Main screens that should not display a back arrow
    val noBackArrowScreens = listOf(
        Screen.InvoiceList.name,
        Screen.DeliveryNoteList.name,
        Screen.CreditNoteList.name,
        Screen.QuoteList.name,
        Screen.ProductList.name,
        Screen.ClientOrIssuerList.name,
        Screen.About.name,
        Screen.Account.name,
        Screen.GStore.name
    )

    // Show back arrow if:
    // - Current screen is ProductTaxRates / ProductCategories, OR
    // - There's a previous screen AND current screen is NOT a main list screen
    val showBackButton =
        currentRoute == Screen.ProductTaxRates.name ||
        currentRoute == Screen.ProductCategories.name ||
        (navController.previousBackStackEntry != null &&
         currentRoute != null &&
         noBackArrowScreens.none { currentRoute.startsWith(it) })

    if (showBackButton) {
        IconButton(onClick = onClickBackArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "back button"
            )
        }
    }
}

/**
 * One row in the TopBar's tag/status filter dropdown. The caller supplies
 * the display [label] (localized) and the [dotColor] (status pastille) so
 * the TopBar stays agnostic of DocumentTag → label / colour mapping, which
 * differs between invoice / BL / quote lists (feminine vs masculine labels,
 * invoiced-only option, etc.).
 */
data class TagFilterOption(
    val tag: DocumentTag,
    val label: String,
    val dotColor: Color,
)
