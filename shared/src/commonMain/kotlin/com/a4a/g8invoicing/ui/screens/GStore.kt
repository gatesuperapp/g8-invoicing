package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.a4a.g8invoicing.ui.screens.shared.ScaffoldWithDimmedOverlay
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavController
import com.a4a.g8invoicing.data.auth.ActivatedModulesRepository
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.account_website_label
import com.a4a.g8invoicing.shared.resources.account_website_url
import com.a4a.g8invoicing.shared.resources.gstore_footer_free
import com.a4a.g8invoicing.shared.resources.gstore_module_cii_desc
import com.a4a.g8invoicing.shared.resources.gstore_module_cii_detail
import com.a4a.g8invoicing.shared.resources.gstore_module_cii_title
import com.a4a.g8invoicing.shared.resources.gstore_module_facturx_desc
import com.a4a.g8invoicing.shared.resources.gstore_module_facturx_detail
import com.a4a.g8invoicing.shared.resources.gstore_module_facturx_title
import com.a4a.g8invoicing.shared.resources.gstore_module_font_desc
import com.a4a.g8invoicing.shared.resources.gstore_module_font_detail
import com.a4a.g8invoicing.shared.resources.gstore_module_font_title
import com.a4a.g8invoicing.shared.resources.gstore_module_delivery_note_desc
import com.a4a.g8invoicing.shared.resources.gstore_module_delivery_note_detail
import com.a4a.g8invoicing.shared.resources.gstore_module_delivery_note_title
import com.a4a.g8invoicing.shared.resources.gstore_module_delivery_note_tagging_desc
import com.a4a.g8invoicing.shared.resources.gstore_module_delivery_note_tagging_detail
import com.a4a.g8invoicing.shared.resources.gstore_module_delivery_note_tagging_title
import com.a4a.g8invoicing.shared.resources.gstore_module_quote_tagging_desc
import com.a4a.g8invoicing.shared.resources.gstore_module_quote_tagging_detail
import com.a4a.g8invoicing.shared.resources.gstore_module_quote_tagging_title
import com.a4a.g8invoicing.shared.resources.gstore_module_multi_entreprise_desc
import com.a4a.g8invoicing.shared.resources.gstore_module_multi_entreprise_detail
import com.a4a.g8invoicing.shared.resources.gstore_module_multi_entreprise_title
import com.a4a.g8invoicing.shared.resources.gstore_module_quote_desc
import com.a4a.g8invoicing.shared.resources.gstore_module_quote_detail
import com.a4a.g8invoicing.shared.resources.gstore_module_quote_title
import com.a4a.g8invoicing.shared.resources.gstore_module_quote_trial_desc
import com.a4a.g8invoicing.shared.resources.gstore_module_quote_trial_detail
import com.a4a.g8invoicing.shared.resources.gstore_module_quote_trial_title
import com.a4a.g8invoicing.shared.resources.gstore_module_watermark_desc
import com.a4a.g8invoicing.shared.resources.gstore_module_watermark_detail
import com.a4a.g8invoicing.shared.resources.gstore_module_watermark_title
import com.a4a.g8invoicing.shared.resources.gstore_premium_badge
import com.a4a.g8invoicing.shared.resources.gstore_premium_only_message
import com.a4a.g8invoicing.shared.resources.gstore_title
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.shared.GeneralBottomBar
import com.a4a.g8invoicing.ui.shared.WebsiteFooter
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.ColorVioletLight
import com.a4a.g8invoicing.ui.theme.textBodyBold
import com.a4a.g8invoicing.ui.theme.textBodySmall
import com.a4a.g8invoicing.ui.theme.textCaption
import com.a4a.g8invoicing.ui.theme.textScreenTitle
import com.a4a.g8invoicing.ui.theme.textTiny
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private data class GStoreModule(
    val id: String,
    val titleRes: StringResource,
    val descRes: StringResource,
    val detailRes: StringResource,
    val icon: ImageVector,
    // A module marked isFree = true is togglable by every user (no premium check) and
    // its card/dialog hide the PREMIUM pill. Kept per-module (not a global list) so a
    // future free-tier module needs only a flag flip, no cross-file wiring.
    val isFree: Boolean = false,
)

private val MODULES = listOf(
    GStoreModule(
        id = ActivatedModulesRepository.MODULE_DELIVERY_NOTE,
        titleRes = Res.string.gstore_module_delivery_note_title,
        descRes = Res.string.gstore_module_delivery_note_desc,
        detailRes = Res.string.gstore_module_delivery_note_detail,
        icon = Icons.Outlined.LocalShipping,
        isFree = true,
    ),
    GStoreModule(
        id = ActivatedModulesRepository.MODULE_QUOTE,
        titleRes = Res.string.gstore_module_quote_title,
        descRes = Res.string.gstore_module_quote_desc,
        detailRes = Res.string.gstore_module_quote_detail,
        icon = Icons.Outlined.RequestQuote,
    ),
    // Free discovery variant of MODULE_QUOTE. Only surfaced to non-premium
    // users (see `visibleModules` in GStore composable). Premium users already
    // have unlimited via MODULE_QUOTE, so seeing a "5 free quotes" card would
    // be confusing.
    GStoreModule(
        id = ActivatedModulesRepository.MODULE_QUOTE_TRIAL,
        titleRes = Res.string.gstore_module_quote_trial_title,
        descRes = Res.string.gstore_module_quote_trial_desc,
        detailRes = Res.string.gstore_module_quote_trial_detail,
        icon = Icons.Outlined.RequestQuote,
        isFree = true,
    ),
    // Raw CII XML export for international e-invoicing platforms that don't
    // accept Factur-X yet. Premium-only.
    GStoreModule(
        id = ActivatedModulesRepository.MODULE_CII_XML_EXPORT,
        titleRes = Res.string.gstore_module_cii_title,
        descRes = Res.string.gstore_module_cii_desc,
        detailRes = Res.string.gstore_module_cii_detail,
        icon = Icons.Outlined.Code,
        isFree = true,
    ),
    // Factur-X hybrid PDF: visual PDF invoice + embedded CII XML
    // (factur-x.xml, AFRelationship=Data). Same free treatment as CII
    // XML for pre-launch, will move behind the premium gate before
    // general rollout.
    GStoreModule(
        id = ActivatedModulesRepository.MODULE_FACTURX_EXPORT,
        titleRes = Res.string.gstore_module_facturx_title,
        descRes = Res.string.gstore_module_facturx_desc,
        detailRes = Res.string.gstore_module_facturx_detail,
        icon = Icons.Outlined.Code,
        isFree = true,
    ),
    GStoreModule(
        id = ActivatedModulesRepository.MODULE_WATERMARK_REMOVAL,
        titleRes = Res.string.gstore_module_watermark_title,
        descRes = Res.string.gstore_module_watermark_desc,
        detailRes = Res.string.gstore_module_watermark_detail,
        icon = Icons.Outlined.WaterDrop,
        isFree = true,
    ),
    GStoreModule(
        id = ActivatedModulesRepository.MODULE_FONT,
        titleRes = Res.string.gstore_module_font_title,
        descRes = Res.string.gstore_module_font_desc,
        detailRes = Res.string.gstore_module_font_detail,
        icon = Icons.Outlined.TextFields,
        isFree = true,
    ),
    GStoreModule(
        id = ActivatedModulesRepository.MODULE_DELIVERY_NOTE_TAGGING,
        titleRes = Res.string.gstore_module_delivery_note_tagging_title,
        descRes = Res.string.gstore_module_delivery_note_tagging_desc,
        detailRes = Res.string.gstore_module_delivery_note_tagging_detail,
        icon = Icons.Outlined.Sell,
        isFree = true,
    ),
    GStoreModule(
        id = ActivatedModulesRepository.MODULE_QUOTE_TAGGING,
        titleRes = Res.string.gstore_module_quote_tagging_title,
        descRes = Res.string.gstore_module_quote_tagging_desc,
        detailRes = Res.string.gstore_module_quote_tagging_detail,
        icon = Icons.Outlined.Sell,
        isFree = true,
    ),
    // Pinned last per product ordering — feature-y modules read first, then
    // watermark, then multi-entreprise (which reshapes the whole app menu).
    GStoreModule(
        id = ActivatedModulesRepository.MODULE_MULTI_ENTREPRISE,
        titleRes = Res.string.gstore_module_multi_entreprise_title,
        descRes = Res.string.gstore_module_multi_entreprise_desc,
        detailRes = Res.string.gstore_module_multi_entreprise_detail,
        icon = Icons.Outlined.Business,
        isFree = true,
    ),
)

private val IconBackground = Color(0xFFEFE3F0) // light lavender, matches mockup

@Composable
fun GStore(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickBack: () -> Unit,
    isCategoriesMenuOpen: Boolean = false,
    onCategoriesMenuOpenChange: (Boolean) -> Unit = {},
    viewModel: GStoreViewModel = koinViewModel(),
) {
    val activated by viewModel.activatedState.collectAsState()
    // Refresh /v1/account on every screen resume so the switch state reflects the latest
    // backend truth (e.g. after a subscription change in the Stripe Portal, or to
    // correct a stale cache entry persisted before the date-parser fix).
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshSubscription()
    }
    // Reactive premium check — recomposes when /v1/account lands after screen open.
    val subscriptionState by viewModel.subscriptionState.collectAsState()
    val isPremium = remember(subscriptionState) {
        (subscriptionState as? com.a4a.g8invoicing.data.auth.SubscriptionState.Known)?.let { s ->
            s.status == "active" &&
                (s.currentPeriodEndMs ?: 0L) > kotlin.time.Clock.System.now().toEpochMilliseconds()
        } ?: false
    }
    val isDimActive = remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val websiteUrl = stringResource(Res.string.account_website_url)
    val websiteLabel = stringResource(Res.string.account_website_label)

    // Small centered dialog shown when a non-premium user taps a premium-only toggle.
    // Kept as a Dialog rather than a Scaffold snackbar so it stacks on top of the
    // ModuleDetailDialog when the tap originates from there.
    var showPremiumHint by remember { mutableStateOf(false) }
    val premiumOnlyMessage = stringResource(Res.string.gstore_premium_only_message)
    val onPremiumHint: () -> Unit = { showPremiumHint = true }

    // Tapping a card opens a fullscreen detail dialog for that module. Null = no dialog.
    var selectedModule: GStoreModule? by remember { mutableStateOf(null) }

    ScaffoldWithDimmedOverlay(
        isDimmed = isDimActive.value,
        onDismissDim = { isDimActive.value = false },
        topBar = {
            com.a4a.g8invoicing.ui.navigation.TopBar(
                title = stringResource(Res.string.gstore_title),
                navController = navController,
                onClickBackArrow = onClickBack,
                isCancelCtaDisplayed = false
            )
        },
        bottomBar = {
            GeneralBottomBar(
                navController = navController,
                onClickCategory = onClickCategory,
                onChangeBackground = { isDimActive.value = !isDimActive.value },
                isButtonNewDisplayed = false,
                isCategoriesMenuOpen = isCategoriesMenuOpen,
                onCategoriesMenuOpenChange = onCategoriesMenuOpenChange,
            )
        }
    ) { padding ->
        // The trial "Devis découverte" card is hidden for premium users — they
        // already have unlimited quotes via MODULE_QUOTE, so surfacing a "5 free"
        // tile alongside the paid one would be confusing.
        // Show every module regardless of subscription. A premium user with
        // MODULE_QUOTE_TRIAL previously activated (from before they upgraded)
        // needs the tile to stay in the gStore so they can turn it off — the
        // trial counter otherwise keeps ticking under the hood next to their
        // paid MODULE_QUOTE, and the tile disappearing left no way to reach it.
        val visibleModules = MODULES
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 28.dp,
                bottom = 4.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            items(visibleModules) { module ->
                GStoreModuleCard(
                    title = stringResource(module.titleRes),
                    description = stringResource(module.descRes),
                    icon = module.icon,
                    isPremium = isPremium,
                    isFree = module.isFree,
                    isActivated = module.id in activated,
                    onToggle = { viewModel.toggleModule(module.id) },
                    onPremiumHint = onPremiumHint,
                    onClick = { selectedModule = module },
                )
            }

            // Footer scrolls with the grid — sits just after the last card row
            // with breathing space, so it's only in view once the user has
            // scrolled past all modules. Only shown to non-premium users;
            // premium account management lives in Mon Compte → Customer Portal.
            if (!isPremium) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        Spacer(modifier = Modifier.height(24.dp))
                        WebsiteFooter(
                            prefix = stringResource(Res.string.gstore_footer_free),
                            linkLabel = websiteLabel,
                            onClickLink = { uriHandler.openUri(websiteUrl) },
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }

    selectedModule?.let { module ->
        ModuleDetailDialog(
            title = stringResource(module.titleRes),
            detail = stringResource(module.detailRes),
            icon = module.icon,
            isPremium = isPremium,
            isFree = module.isFree,
            isActivated = module.id in activated,
            onToggle = { viewModel.toggleModule(module.id) },
            onPremiumHint = onPremiumHint,
            onDismiss = { selectedModule = null },
        )
    }

    if (showPremiumHint) {
        PremiumHintDialog(
            message = premiumOnlyMessage,
            onDismiss = { showPremiumHint = false },
        )
    }
}

@Composable
private fun PremiumHintDialog(
    message: String,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clickable(onClick = onDismiss)
                .background(AppColors.surface, shape = RoundedCornerShape(14.dp))
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.textBodySmall.copy(color = Color.DarkGray),
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun GStoreModuleCard(
    title: String,
    description: String,
    icon: ImageVector,
    isPremium: Boolean,
    isFree: Boolean,
    isActivated: Boolean,
    onToggle: () -> Unit,
    onPremiumHint: () -> Unit,
    onClick: () -> Unit,
) {
    val isUnlocked = isFree || isPremium
    // Tall rectangular card. Vertical stack: icon top → big gap → title + desc → Switch bottom.
    // PREMIUM pill sits top-right and stays visible even for premium users — the GStore
    // will mix premium and free modules, so the pill labels the *module*, not the *user*.
    // The card itself is clickable to open the detail dialog ; the Switch keeps its own
    // tap target (Compose's nested clickables resolve to the innermost handler).
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .clickable(onClick = onClick)
            .background(AppColors.surface, shape = RoundedCornerShape(14.dp))
            .padding(14.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top row: icon + (optional) PREMIUM pill on the right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(IconBackground, shape = RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = AppColors.accent,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (!isFree) PremiumPill()
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.textBodyBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.textCaption,
                lineHeight = 16.sp,
            )

            Spacer(modifier = Modifier.weight(1f))

            // Switch at the bottom. Always enabled so non-premium taps can surface the
            // premium-only hint snackbar instead of being silently swallowed by a
            // disabled Switch. The grayed uncheckedTrack color for non-premium keeps the
            // "off / not-yours" affordance without hiding the tap target.
            // The visible checked state combines the preference (isActivated) with the
            // entitlement (isUnlocked) — a locked module retombes visually to OFF at
            // logout / subscription loss without erasing the preference.
            Switch(
                checked = isActivated && isUnlocked,
                onCheckedChange = { if (isUnlocked) onToggle() else onPremiumHint() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ColorVioletLight,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = if (isUnlocked) Color(0xFFB8B5BC) else Color(0xFFE5E2E7),
                    uncheckedBorderColor = Color.Transparent,
                ),
            )
        }
    }
}

@Composable
private fun ModuleDetailDialog(
    title: String,
    detail: String,
    icon: ImageVector,
    isPremium: Boolean,
    isFree: Boolean,
    isActivated: Boolean,
    onToggle: () -> Unit,
    onPremiumHint: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isUnlocked = isFree || isPremium
    // Centered modal taking ~2/3 of the screen height, so the GStore TopBar and
    // BottomBar stay visible around it — the user keeps the context that they're still
    // on the GStore screen. usePlatformDefaultWidth=false lets us widen beyond the
    // built-in modal width.
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .fillMaxHeight(2f / 3f)
                .background(AppColors.surface, shape = RoundedCornerShape(16.dp)),
        ) {
            // Close (X) in the top-right — mirrors the WhatsNewDialog pattern so the
            // dismissal affordance stays consistent across the app.
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = AppColors.iconSecondary,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp, bottom = 20.dp, start = 20.dp, end = 20.dp),
            ) {
                // Top row mirrors the card: icon left, PREMIUM pill right. Same rationale
                // as on the card — the pill labels the module, not the user.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(IconBackground, shape = RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = AppColors.accent,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (!isFree) PremiumPill()
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.textScreenTitle,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = detail,
                    style = MaterialTheme.typography.textBodySmall.copy(color = Color.DarkGray),
                    lineHeight = 20.sp,
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Switch sits directly under the text — not pushed to the bottom of the
                // modal, per design. Same colors + always-enabled behaviour as on the
                // card so non-premium taps consistently trigger the hint snackbar.
                Switch(
                    checked = isActivated && isUnlocked,
                    onCheckedChange = { if (isUnlocked) onToggle() else onPremiumHint() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ColorVioletLight,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = if (isUnlocked) Color(0xFFB8B5BC) else Color(0xFFE5E2E7),
                        uncheckedBorderColor = Color.Transparent,
                    ),
                )
            }
        }
    }
}

@Composable
private fun PremiumPill() {
    Box(
        modifier = Modifier
            .background(IconBackground, shape = RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = stringResource(Res.string.gstore_premium_badge),
            style = MaterialTheme.typography.textTiny.copy(
                fontWeight = FontWeight.SemiBold,
                color = ColorVioletLight,
            ),
        )
    }
}

