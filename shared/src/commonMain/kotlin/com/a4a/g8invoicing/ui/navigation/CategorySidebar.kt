package com.a4a.g8invoicing.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.a4a.g8invoicing.data.CurrentCompanyRepository
import com.a4a.g8invoicing.data.auth.ActivatedModulesRepository
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.account_add_company
import com.a4a.g8invoicing.shared.resources.account_manage_companies
import com.a4a.g8invoicing.shared.resources.account_manage_company
import com.a4a.g8invoicing.ui.shared.ImageStorage
import com.a4a.g8invoicing.ui.shared.InitImageContext
import com.a4a.g8invoicing.ui.shared.loadLogoBitmap
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.ColorVioletLight
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerListViewModel
import kotlin.math.abs
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Permanent sidebar for desktop showing all categories.
 * Hidden on mobile where the dropdown menu is used instead.
 *
 * The entreprise block runs a two-phase animation on toggle: the current
 * body fully shrinks/fades out first (200 ms), then the new body expands
 * in (200 ms). Sequencing is enforced by the delayMillis on the enter
 * transitions of both AnimatedVisibility blocks.
 */
@Composable
fun CategorySidebar(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    val modulesRepo = koinInject<ActivatedModulesRepository>()
    val activatedModules by modulesRepo.state.collectAsState()
    val everActivated by modulesRepo.everActivated.collectAsState()
    val currentCompanyRepository = koinInject<CurrentCompanyRepository>()
    val currentCompanyId by currentCompanyRepository.state.collectAsState()
    val listViewModel = koinViewModel<ClientOrIssuerListViewModel>()
    val issuersUiState by listViewModel.issuersUiState.collectAsState()
    val issuers = issuersUiState.clientsOrIssuerList.orEmpty()

    val currentIssuer = issuers.firstOrNull { it.id?.toLong() == currentCompanyId }
    val fallbackName = stringResource(Category.MyCompany.resourceId)
    val displayName = currentIssuer?.let { displayNameFor(it) } ?: fallbackName
    val currentLogoPath = currentIssuer?.logoPath

    val hasQuoteAccess =
        ActivatedModulesRepository.MODULE_QUOTE in activatedModules ||
            ActivatedModulesRepository.MODULE_QUOTE in everActivated ||
            ActivatedModulesRepository.MODULE_QUOTE_TRIAL in activatedModules

    val topCategories = listOf(Category.G8, Category.MyAccount, Category.GStore)
    val subCategories = buildList {
        add(Category.Clients)
        add(Category.Products)
        add(Category.CreditNotes)
        if (hasQuoteAccess) add(Category.Quotes)
        if (ActivatedModulesRepository.MODULE_DELIVERY_NOTE in activatedModules) add(Category.DeliveryNotes)
        add(Category.Invoices)
    }

    var pickerExpanded by rememberSaveable { mutableStateOf(false) }

    // Snapshot the picker's row ordering the moment it opens so a click that
    // changes the current entreprise (which flips the head of the ordered list)
    // doesn't visually reshuffle the rows under the user during the Crossfade.
    // Snapshot is refreshed each time the picker re-opens.
    var pickerIssuers by remember { mutableStateOf<List<ClientOrIssuerState>>(emptyList()) }
    LaunchedEffect(pickerExpanded) {
        if (pickerExpanded) {
            val (current, others) = issuers.partition { it.id?.toLong() == currentCompanyId }
            pickerIssuers = current + others
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 30dp wider when MULTI_ENTREPRISE is on so the entreprise picker's
    // caret + expanded name row have breathing room without truncating.
    // The mono-entreprise sidebar shows just the doc-type list and can
    // stay compact.
    val sidebarWidth = if (ActivatedModulesRepository.MODULE_MULTI_ENTREPRISE in activatedModules) {
        250.dp
    } else {
        220.dp
    }
    Surface(
        modifier = modifier
            .width(sidebarWidth)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
        ) {
            topCategories.forEach { category ->
                val selected = currentDestination?.hierarchy?.any { it.route?.substringBefore("?") == category.route } == true
                TopMenuRow(
                    label = stringResource(category.resourceId),
                    selected = selected,
                    onClick = { onClickCategory(category) },
                )
            }

            val multiEntrepriseOn = ActivatedModulesRepository.MODULE_MULTI_ENTREPRISE in activatedModules

            // Single-entreprise → skip the entire grey block + company header
            // wrapper. Render sub-categories flat on white with a hairline
            // separator before them (mirrors the pre-multi-entreprise layout).
            // The grey block only comes back once MULTI_ENTREPRISE is activated
            // from gStore.
            if (!multiEntrepriseOn) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    thickness = 1.dp,
                    color = Color.LightGray.copy(alpha = 0.6f),
                )
                subCategories.forEach { category ->
                    val selected = currentDestination?.hierarchy?.any { it.route?.substringBefore("?") == category.route } == true
                    TopMenuRow(
                        label = stringResource(category.resourceId),
                        selected = selected,
                        onClick = { onClickCategory(category) },
                    )
                    if (category is Category.Products) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            thickness = 1.dp,
                            color = Color.LightGray.copy(alpha = 0.6f),
                        )
                    }
                }
                return@Column
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .background(
                        color = BlockBackground,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .padding(vertical = 8.dp),
            ) {
                val manageLabel = if (issuers.size <= 1) {
                    stringResource(Res.string.account_manage_company)
                } else {
                    stringResource(Res.string.account_manage_companies)
                }
                // Pin the Crossfade container to the collapsed state's height
                // (header + all sub-cats). Empty space below when the picker
                // is shorter — better than a jumpy resize during the swap.
                // 48dp = header row, 40dp per PillRow (10dp+text+10dp).
                val blockMinHeight = 48.dp + 40.dp * subCategories.size
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = blockMinHeight),
                ) {
                    Crossfade(
                        targetState = pickerExpanded,
                        animationSpec = tween(180),
                        modifier = Modifier.fillMaxWidth(),
                        label = "companyBlockContent",
                    ) { expanded ->
                    if (expanded) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Cap the list so a long roster stays scrollable
                            // inside the block instead of pushing the sidebar
                            // into a scroll of its own.
                            Column(
                                modifier = Modifier
                                    .heightIn(max = 220.dp)
                                    .verticalScroll(rememberScrollState()),
                            ) {
                                pickerIssuers.forEach { issuer ->
                                    val id = issuer.id?.toLong() ?: return@forEach
                                    val name = displayNameFor(issuer)
                                    val isCurrent = id == currentCompanyId
                                    IssuerPickerRow(
                                        name = name,
                                        logoPath = issuer.logoPath,
                                        isCurrent = isCurrent,
                                        onClick = {
                                            currentCompanyRepository.setCurrent(id)
                                            pickerExpanded = false
                                        },
                                    )
                                }
                            }
                            PickerActionRow(
                                icon = Icons.Filled.Add,
                                label = stringResource(Res.string.account_add_company)
                                    .trimStart('+', ' '),
                                onClick = {
                                    pickerExpanded = false
                                    navController.navigate(Screen.ClientAddEdit.name + "?type=issuer")
                                },
                            )
                            PickerActionRow(
                                icon = Icons.Filled.Settings,
                                label = manageLabel,
                                onClick = {
                                    pickerExpanded = false
                                    navController.navigate(Screen.Account.name + "?section=my_companies")
                                },
                            )
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Reached only when MULTI_ENTREPRISE is on (the flat
                            // white layout early-returns above), so the header
                            // always has a chevron and opens the picker.
                            CompanyHeaderRow(
                                displayName = displayName,
                                logoPath = currentLogoPath,
                                showChevron = true,
                                onClick = { pickerExpanded = true },
                            )
                            subCategories.forEach { category ->
                                val selected = currentDestination?.hierarchy?.any { it.route?.substringBefore("?") == category.route } == true
                                BlockMenuRow(
                                    label = stringResource(category.resourceId),
                                    selected = selected,
                                    onClick = { onClickCategory(category) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

/**
 * Top-level pill row (G8/MyAccount/gStore). Label stays muted-grey when
 * unselected so the group visually detaches from the entreprise block;
 * selected state adds the shared violet pill and bumps the text to black.
 */
@Composable
private fun TopMenuRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    PillRow(
        label = label,
        selected = selected,
        onClick = onClick,
        outerHorizontal = 4.dp,
        innerHorizontal = 16.dp,
        unselectedColor = AppColors.textSecondary,
    )
}

/**
 * Sub-cat pill row inside the entreprise block. Reduced inner padding so
 * the label lines up with the header avatar's left edge (12dp from the
 * block edge = same 20dp from the sidebar edge as gStore above).
 */
@Composable
private fun BlockMenuRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    PillRow(
        label = label,
        selected = selected,
        onClick = onClick,
        outerHorizontal = 4.dp,
        innerHorizontal = 8.dp,
    )
}

@Composable
private fun PillRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    outerHorizontal: Dp,
    innerHorizontal: Dp,
    unselectedColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    val textColor = if (selected) MaterialTheme.colorScheme.onSurface else unselectedColor
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = outerHorizontal)
            .then(
                if (selected) {
                    Modifier.background(
                        color = ColorVioletLight.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(8.dp),
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = innerHorizontal, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
        )
    }
}

/**
 * Entreprise header (collapsed state): avatar + BOLD company name + small
 * UnfoldMore chevron on the right. Chevron disappears with the whole
 * header when the block flips to the picker view.
 */
@Composable
private fun CompanyHeaderRow(
    displayName: String,
    logoPath: String?,
    onClick: () -> Unit,
    showChevron: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 9.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompanyAvatar(displayName, logoPath, useCurrentTint = true)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (showChevron) {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = Icons.Filled.UnfoldMore,
                contentDescription = null,
                tint = AppColors.textSecondary,
            )
        }
    }
}

/**
 * 32dp rounded-square avatar. Shows the entreprise logo when one is set on
 * disk; falls back to the two-letter initials on a coloured background
 * (violet for the current entreprise, deterministic pastel otherwise).
 */
@Composable
private fun CompanyAvatar(
    name: String,
    logoPath: String?,
    useCurrentTint: Boolean = false,
) {
    val size = 32.dp
    val cornerShape = RoundedCornerShape(8.dp)

    InitImageContext()
    val imageStorage = remember { ImageStorage() }
    var logoBitmap by remember(logoPath) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(logoPath) {
        logoBitmap = if (!logoPath.isNullOrBlank() && imageStorage.logoExists(logoPath)) {
            loadLogoBitmap(imageStorage.getAbsolutePath(logoPath))
        } else {
            null
        }
    }

    val bitmap = logoBitmap
    if (bitmap != null) {
        Image(
            modifier = Modifier
                .size(size)
                .clip(cornerShape),
            bitmap = bitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
        return
    }

    val (bg, fg) = if (useCurrentTint) VioletPalette else avatarPalette(name)
    Box(
        modifier = Modifier
            .size(size)
            .background(color = bg, shape = cornerShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initialsOf(name),
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun IssuerPickerRow(
    name: String,
    logoPath: String?,
    isCurrent: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .then(
                if (isCurrent) {
                    Modifier.background(
                        color = ColorVioletLight.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(8.dp),
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompanyAvatar(name, logoPath, useCurrentTint = isCurrent)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isCurrent) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (isCurrent) {
            // UnfoldLess (arrows pointing inward) signals "click to collapse
            // back to the sub-cats view". Replaces the earlier checkmark.
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = Icons.Filled.UnfoldLess,
                contentDescription = null,
                tint = AppColors.textSecondary,
            )
        }
    }
}

@Composable
private fun PickerActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.iconSecondary,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun displayNameFor(issuer: ClientOrIssuerState): String {
    val base = issuer.name.text
    val first = issuer.firstName?.text?.takeIf { it.isNotBlank() }
    return if (first != null) "$base $first" else base
}

private fun initialsOf(name: String): String {
    val words = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        words.isEmpty() -> "?"
        words.size == 1 -> words[0].take(2).uppercase()
        else -> (words.first().first().toString() + words.last().first().toString()).uppercase()
    }
}

// Near-white panel background — significantly lighter than surfaceSubtle so
// the block reads as a subtle grouping without ever looking like a "card".
private val BlockBackground = Color(0xFFFCFCFB)

// Violet tint reserved for the CURRENT entreprise avatar so it matches the
// same violet used to mark the selected row across the menu.
private val VioletPalette = Color(0xFFEDE7F6) to Color(0xFF5E35B1)

// Deterministic pastel palette for non-current entreprises. Colour choice
// is a pure function of the name so a company keeps the same tint across
// sessions.
private val AvatarPalette = listOf(
    Color(0xFFE0F2F1) to Color(0xFF00695C), // teal
    Color(0xFFFCE4EC) to Color(0xFFAD1457), // pink
    Color(0xFFFFF3E0) to Color(0xFFE65100), // orange
    Color(0xFFE8F5E9) to Color(0xFF2E7D32), // green
    Color(0xFFE3F2FD) to Color(0xFF1565C0), // blue
)

private fun avatarPalette(name: String): Pair<Color, Color> {
    val idx = abs(name.hashCode()) % AvatarPalette.size
    return AvatarPalette[idx]
}
