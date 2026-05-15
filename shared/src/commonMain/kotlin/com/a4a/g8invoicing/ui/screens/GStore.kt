package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavController
import com.a4a.g8invoicing.data.auth.ActivatedModulesRepository
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.account_website_label
import com.a4a.g8invoicing.shared.resources.account_website_url
import com.a4a.g8invoicing.shared.resources.gstore_footer_free
import com.a4a.g8invoicing.shared.resources.gstore_module_watermark_desc
import com.a4a.g8invoicing.shared.resources.gstore_module_watermark_title
import com.a4a.g8invoicing.shared.resources.gstore_premium_badge
import com.a4a.g8invoicing.shared.resources.gstore_title
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.shared.GeneralBottomBar
import com.a4a.g8invoicing.ui.theme.ColorVioletLight
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private data class GStoreModule(
    val id: String,
    val titleRes: StringResource,
    val descRes: StringResource,
    val icon: ImageVector,
)

// Only watermark-removal is shipping at launch — other modules will be added when ready.
private val MODULES = listOf(
    GStoreModule(
        id = ActivatedModulesRepository.MODULE_WATERMARK_REMOVAL,
        titleRes = Res.string.gstore_module_watermark_title,
        descRes = Res.string.gstore_module_watermark_desc,
        icon = Icons.Outlined.WaterDrop,
    ),
)

private val IconBackground = Color(0xFFEFE3F0) // light lavender, matches mockup

@Composable
fun GStore(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickBack: () -> Unit,
    viewModel: GStoreViewModel = koinViewModel(),
) {
    val activated by viewModel.activatedState.collectAsState()
    // Refresh /v1/me on every screen resume so the switch state reflects the latest
    // backend truth (e.g. after a subscription change in the Stripe Portal, or to
    // correct a stale cache entry persisted before the date-parser fix).
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshSubscription()
    }
    // Reactive premium check — recomposes when /v1/me lands after screen open.
    val subscriptionState by viewModel.subscriptionState.collectAsState()
    val isPremium = remember(subscriptionState) {
        (subscriptionState as? com.a4a.g8invoicing.data.auth.SubscriptionState.Known)?.let { s ->
            s.status == "active" &&
                (s.currentPeriodEndMs ?: 0L) > kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        } ?: false
    }
    val isDimActive = remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val websiteUrl = stringResource(Res.string.account_website_url)
    val websiteLabel = stringResource(Res.string.account_website_label)

    Scaffold(
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
                isButtonNewDisplayed = false
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(top = 24.dp),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
            ) {
                items(MODULES) { module ->
                    GStoreModuleCard(
                        title = stringResource(module.titleRes),
                        description = stringResource(module.descRes),
                        icon = module.icon,
                        isPremium = isPremium,
                        isActivated = module.id in activated,
                        onToggle = { viewModel.toggleModule(module.id) },
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer: info pointer to the-gate.fr root — only for non-premium users.
            // Premium users don't see a "manage your account on …" line here because
            // account management lives in Mon Compte → Customer Portal.
            if (!isPremium) {
                Footer(
                    prefix = stringResource(Res.string.gstore_footer_free),
                    linkLabel = websiteLabel,
                    onClickLink = { uriHandler.openUri(websiteUrl) },
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun GStoreModuleCard(
    title: String,
    description: String,
    icon: ImageVector,
    isPremium: Boolean,
    isActivated: Boolean,
    onToggle: () -> Unit,
) {
    // Tall rectangular card. Vertical stack: icon top → big gap → title + desc → Switch bottom.
    // PREMIUM pill sits top-right only for non-premium users; once premium it disappears
    // (no need to reiterate what they already are).
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .background(Color.White, shape = RoundedCornerShape(14.dp))
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
                        tint = ColorVioletLight,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (!isPremium) {
                    PremiumPill()
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.Black,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.DarkGray,
                lineHeight = 16.sp,
            )

            Spacer(modifier = Modifier.weight(1f))

            // Switch at the bottom — disabled (grayed) when not premium so the affordance
            // stays visible but uninteractable.
            Switch(
                checked = isActivated,
                onCheckedChange = { onToggle() },
                enabled = isPremium,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ColorVioletLight,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFB8B5BC),
                    uncheckedBorderColor = Color.Transparent,
                    disabledUncheckedThumbColor = Color.White,
                    disabledUncheckedTrackColor = Color(0xFFE5E2E7),
                    disabledUncheckedBorderColor = Color.Transparent,
                ),
            )
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
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = ColorVioletLight,
        )
    }
}

@Composable
private fun Footer(
    prefix: String,
    linkLabel: String,
    onClickLink: () -> Unit,
) {
    val annotated = buildAnnotatedString {
        withStyle(SpanStyle(color = Color.DarkGray)) { append(prefix) }
        withStyle(SpanStyle(color = ColorVioletLight, fontWeight = FontWeight.SemiBold)) {
            append(linkLabel)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            modifier = Modifier.clickable { onClickLink() },
            text = annotated,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
    }
}
