package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavController
import com.a4a.g8invoicing.data.AppLanguage
import com.a4a.g8invoicing.data.LocaleManager
import com.a4a.g8invoicing.data.auth.SubscriptionState
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.about_language_english
import com.a4a.g8invoicing.shared.resources.about_language_french
import com.a4a.g8invoicing.shared.resources.about_language_german
import com.a4a.g8invoicing.shared.resources.about_language_system
import com.a4a.g8invoicing.shared.resources.about_title_language
import com.a4a.g8invoicing.shared.resources.account_auth_about_link
import com.a4a.g8invoicing.shared.resources.account_auth_about_url
import com.a4a.g8invoicing.shared.resources.account_auth_email_label
import com.a4a.g8invoicing.shared.resources.account_auth_error
import com.a4a.g8invoicing.shared.resources.account_auth_link_sent
import com.a4a.g8invoicing.shared.resources.account_auth_send_link
import com.a4a.g8invoicing.shared.resources.account_auth_subtitle
import com.a4a.g8invoicing.shared.resources.account_auth_title
import com.a4a.g8invoicing.shared.resources.account_logged_in_as
import com.a4a.g8invoicing.shared.resources.account_add_company
import com.a4a.g8invoicing.shared.resources.account_logout
import com.a4a.g8invoicing.shared.resources.account_my_companies
import com.a4a.g8invoicing.shared.resources.account_manage_subscription
import com.a4a.g8invoicing.shared.resources.account_manage_subscription_url
import com.a4a.g8invoicing.shared.resources.account_cancellation_date
import com.a4a.g8invoicing.shared.resources.account_renewal_date
import com.a4a.g8invoicing.shared.resources.account_status_premium_fab
import com.a4a.g8invoicing.shared.resources.account_status_premium_fly
import com.a4a.g8invoicing.shared.resources.drawer_my_account
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.shared.GeneralBottomBar
import com.a4a.g8invoicing.ui.navigation.Screen
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.theme.ColorDarkGrayTransp
import com.a4a.g8invoicing.ui.theme.ColorLightGrey
import com.a4a.g8invoicing.ui.theme.ColorVioletLight
import com.a4a.g8invoicing.ui.theme.callForActions
import com.a4a.g8invoicing.ui.theme.textTitle
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerListViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun Account(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickBack: () -> Unit,
    onShareContent: (String) -> Unit = {},
    viewModel: AccountViewModel = koinViewModel(),
) {
    val uriHandler = LocalUriHandler.current
    val uiState = viewModel.uiState

    val isDimActive = remember { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (uiState.isLoggedIn) {
            viewModel.refreshSubscription()
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            com.a4a.g8invoicing.ui.navigation.TopBar(
                title = stringResource(Res.string.drawer_my_account),
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
        },
    ) { _ ->
        // Mirror the Infos screen: ignore Scaffold's content padding and lay out from
        // the top of the screen, the TopBar overlaying. Single 130dp top padding (not
        // stacked with the TopBar's ~64dp) keeps the visual rhythm identical to Infos.
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = 130.dp,
                        bottom = 140.dp,
                        start = 40.dp,
                        end = 40.dp,
                    )
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                // ============ Auth section (no title — per design) ============
                if (uiState.isLoggedIn) {
                    val subscriptionState by viewModel.subscriptionState.collectAsState()
                    LoggedInContent(
                        email = uiState.userEmail,
                        subscriptionState = subscriptionState,
                        onLogout = { viewModel.logout() },
                        onOpenManageSubscription = { fallbackUrl ->
                            viewModel.openCustomerPortal(fallbackUrl) { url ->
                                uriHandler.openUri(url)
                            }
                        },
                    )
                } else {
                    LoggedOutContent(
                        uiState = uiState,
                        onSubmit = { viewModel.requestMagicLink(it) },
                        uriHandler = uriHandler,
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                // ============ Mes entreprises ============
                MyCompaniesSection(navController = navController)

                Spacer(modifier = Modifier.height(30.dp))

                // ============ Langue section ============
                Text(
                    modifier = Modifier.padding(bottom = 8.dp),
                    text = stringResource(Res.string.about_title_language).uppercase(),
                    style = MaterialTheme.typography.textTitle,
                )
                LanguageSelector()
            }
        }
    }
}

@Composable
private fun LoggedOutContent(
    uiState: AccountUiState,
    onSubmit: (String) -> Unit,
    uriHandler: androidx.compose.ui.platform.UriHandler,
) {
    var email by remember { mutableStateOf("") }

    Text(
        text = stringResource(Res.string.account_auth_title),
        fontWeight = FontWeight.Bold,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = stringResource(Res.string.account_auth_subtitle),
    )

    Spacer(modifier = Modifier.height(16.dp))

    val violetSelectionColors = TextSelectionColors(
        handleColor = Color.Transparent,
        backgroundColor = ColorVioletLight.copy(alpha = 0.3f),
    )
    CompositionLocalProvider(LocalTextSelectionColors provides violetSelectionColors) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(Res.string.account_auth_email_label)) },
            singleLine = true,
            enabled = !uiState.isLoading && uiState.successMessage == null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ColorVioletLight,
                unfocusedBorderColor = Color.Black,
                focusedLabelColor = ColorVioletLight,
                unfocusedLabelColor = Color.Black,
                cursorColor = ColorVioletLight,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (uiState.successMessage != null) {
        Text(
            text = stringResource(Res.string.account_auth_link_sent),
            color = Color.Black,
            fontWeight = FontWeight.Medium,
        )
    } else {
        val isEnabled = !uiState.isLoading && email.isNotBlank()
        TextButton(
            onClick = { onSubmit(email.trim()) },
            enabled = isEnabled,
            contentPadding = PaddingValues(horizontal = 0.dp),
            colors = ButtonDefaults.textButtonColors(
                contentColor = ColorVioletLight,
                disabledContentColor = ColorDarkGrayTransp,
            ),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(18.dp).width(18.dp),
                    strokeWidth = 2.dp,
                    color = ColorVioletLight,
                )
            } else {
                Text(stringResource(Res.string.account_auth_send_link))
            }
        }

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(Res.string.account_auth_error),
                color = MaterialTheme.colorScheme.error,
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    val aboutLabel = stringResource(Res.string.account_auth_about_link)
    val aboutUrl = stringResource(Res.string.account_auth_about_url)
    Text(
        modifier = Modifier.clickable { uriHandler.openUri(aboutUrl) },
        text = aboutLabel,
        fontSize = 13.sp,
        color = Color.Black,
        textDecoration = TextDecoration.Underline,
    )
}

@Composable
private fun LoggedInContent(
    email: String?,
    subscriptionState: SubscriptionState,
    onLogout: () -> Unit,
    onOpenManageSubscription: (fallbackUrl: String) -> Unit,
) {
    if (!email.isNullOrBlank()) {
        Text(text = stringResource(Res.string.account_logged_in_as))
        Text(text = email)
        Spacer(modifier = Modifier.height(12.dp))
    }

    // Premium status. The "manage" link points to Stripe Customer Portal — managing an
    // existing subscription is explicitly allowed by Play Store and Apple (the rule only
    // forbids *selling* via external link).
    val known = subscriptionState as? SubscriptionState.Known
    val premiumStatusRes: StringResource? = when {
        known?.status != "active" -> null
        known.product == "fly" -> Res.string.account_status_premium_fly
        known.product == "fab" -> Res.string.account_status_premium_fab
        else -> null
    }

    if (premiumStatusRes != null) {
        PremiumBadge(label = stringResource(premiumStatusRes))
        Spacer(modifier = Modifier.height(8.dp))

        known?.currentPeriodEndMs?.let { ms ->
            val dateLabel = formatRenewalDate(ms)
            val text = if (known.cancelAtPeriodEnd) {
                stringResource(Res.string.account_cancellation_date, dateLabel)
            } else {
                stringResource(Res.string.account_renewal_date, dateLabel)
            }
            Text(
                fontSize = 13.sp,
                text = text,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        val manageLabel = stringResource(Res.string.account_manage_subscription)
        val manageFallbackUrl = stringResource(Res.string.account_manage_subscription_url)
        Text(
            modifier = Modifier.clickable { onOpenManageSubscription(manageFallbackUrl) },
            text = manageLabel,
            fontSize = 13.sp,
            color = ColorVioletLight,
            textDecoration = TextDecoration.Underline,
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    // No separator between manage subscription and logout — design choice.
    Text(
        modifier = Modifier.clickable { onLogout() },
        text = stringResource(Res.string.account_logout),
        fontSize = 13.sp,
        color = ColorVioletLight,
        textDecoration = TextDecoration.Underline,
    )
}

@Composable
private fun PremiumBadge(label: String) {
    Box(
        modifier = Modifier
            .background(
                color = ColorVioletLight.copy(alpha = 0.08f),
                shape = RoundedCornerShape(10.dp),
            )
            .border(
                width = 1.dp,
                color = ColorVioletLight.copy(alpha = 0.25f),
                shape = RoundedCornerShape(10.dp),
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            text = label,
            color = ColorVioletLight,
        )
    }
}

@Composable
private fun LanguageSelector(
    localeManager: LocaleManager = koinInject()
) {
    var expanded by remember { mutableStateOf(false) }
    val currentLanguage = localeManager.currentLanguage

    val systemLabel = stringResource(Res.string.about_language_system)
    val frenchLabel = stringResource(Res.string.about_language_french)
    val englishLabel = stringResource(Res.string.about_language_english)
    val germanLabel = stringResource(Res.string.about_language_german)

    fun getDisplayName(language: AppLanguage): String = when (language) {
        AppLanguage.SYSTEM -> systemLabel
        AppLanguage.FRENCH -> frenchLabel
        AppLanguage.ENGLISH -> englishLabel
        AppLanguage.GERMAN -> germanLabel
    }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(4.dp)
                )
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getDisplayName(currentLanguage),
                color = Color.Black
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.Gray
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            AppLanguage.values().forEach { language ->
                DropdownMenuItem(
                    text = { Text(getDisplayName(language)) },
                    onClick = {
                        localeManager.setLanguage(language)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun formatRenewalDate(epochMs: Long): String {
    val date = Instant.fromEpochMilliseconds(epochMs)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
    val day = date.dayOfMonth.toString().padStart(2, '0')
    val month = date.monthNumber.toString().padStart(2, '0')
    return "$day/$month/${date.year}"
}

@Composable
private fun MyCompaniesSection(
    navController: NavController,
    listViewModel: ClientOrIssuerListViewModel = koinViewModel(),
) {
    val issuersUiState by listViewModel.issuersUiState.collectAsState()
    val issuers = issuersUiState.clientsOrIssuerList.orEmpty()

    Text(
        modifier = Modifier.padding(bottom = 8.dp),
        text = stringResource(Res.string.account_my_companies).uppercase(),
        style = MaterialTheme.typography.textTitle,
    )

    issuers.forEach { issuer ->
        IssuerListRow(
            issuer = issuer,
            onClick = {
                navController.navigate(
                    Screen.ClientAddEdit.name + "?itemId=${issuer.id}&type=issuer"
                )
            },
            onDelete = {
                listViewModel.deleteClientsOrIssuers(listOf(issuer))
            },
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    // "+ Ajouter une entreprise" — same look as ProductAddEditForm's
    // AddPriceButton (light-grey rounded pill, callForActions typo).
    Box(
        modifier = Modifier
            .padding(top = if (issuers.isEmpty()) 0.dp else 4.dp)
            .background(
                color = Color(0xFFE8E8E8),
                shape = RoundedCornerShape(6.dp),
            )
            .clickable {
                navController.navigate(Screen.ClientAddEdit.name + "?type=issuer")
            }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            style = MaterialTheme.typography.callForActions,
            text = stringResource(Res.string.account_add_company),
        )
    }
}

@Composable
private fun IssuerListRow(
    issuer: ClientOrIssuerState,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(5.dp))
            .background(ColorLightGrey)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            modifier = Modifier.weight(1F),
            text = issuer.name.text + (issuer.firstName?.let { " " + it.text } ?: ""),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Icon(
            modifier = Modifier
                .size(18.dp)
                .clickable(onClick = onDelete),
            imageVector = Icons.Outlined.DeleteOutline,
            contentDescription = null,
        )
    }
}
