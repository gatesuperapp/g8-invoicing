package com.a4a.g8invoicing.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavController
import com.a4a.g8invoicing.data.AppLanguage
import com.a4a.g8invoicing.data.CurrencyManager
import com.a4a.g8invoicing.data.LocaleManager
import com.a4a.g8invoicing.data.currencyDisplayName
import com.a4a.g8invoicing.data.currencySymbol
import com.a4a.g8invoicing.ui.screens.shared.CurrencyPicker
import com.a4a.g8invoicing.data.auth.SubscriptionState
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.about_language_english
import com.a4a.g8invoicing.shared.resources.about_language_french
import com.a4a.g8invoicing.shared.resources.about_language_german
import com.a4a.g8invoicing.shared.resources.about_language_system
import com.a4a.g8invoicing.shared.resources.about_title_language
import com.a4a.g8invoicing.shared.resources.account_currency_title
import com.a4a.g8invoicing.shared.resources.account_currency_and_language_title
import com.a4a.g8invoicing.ui.shared.CollapsibleSection
import com.a4a.g8invoicing.ui.shared.FormInputsValidator
import com.a4a.g8invoicing.shared.resources.account_auth_about_link
import com.a4a.g8invoicing.shared.resources.account_auth_about_url
import com.a4a.g8invoicing.shared.resources.account_auth_email_label
import com.a4a.g8invoicing.shared.resources.account_auth_error
import com.a4a.g8invoicing.shared.resources.account_auth_invalid_email
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
import com.a4a.g8invoicing.shared.resources.about_backup_text
import com.a4a.g8invoicing.shared.resources.about_contact_email
import com.a4a.g8invoicing.shared.resources.about_download_database
import com.a4a.g8invoicing.shared.resources.about_title_backup
import com.a4a.g8invoicing.shared.resources.account_auth_link_expired
import com.a4a.g8invoicing.shared.resources.account_backup_dialog_message
import com.a4a.g8invoicing.shared.resources.account_backup_dialog_no
import com.a4a.g8invoicing.shared.resources.account_backup_dialog_title
import com.a4a.g8invoicing.shared.resources.account_backup_dialog_yes
import com.a4a.g8invoicing.shared.resources.account_delete_cta
import com.a4a.g8invoicing.shared.resources.account_delete_dialog_cancel
import com.a4a.g8invoicing.shared.resources.account_delete_dialog_confirm
import com.a4a.g8invoicing.shared.resources.account_delete_dialog_message
import com.a4a.g8invoicing.shared.resources.account_delete_dialog_title
import com.a4a.g8invoicing.shared.resources.account_delete_error
import com.a4a.g8invoicing.shared.resources.account_delete_success_message
import com.a4a.g8invoicing.shared.resources.account_delete_success_title
import com.a4a.g8invoicing.shared.resources.account_section_advanced
import com.a4a.g8invoicing.shared.resources.drawer_my_account
import com.a4a.g8invoicing.shared.resources.ok
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.shared.GeneralBottomBar
import com.a4a.g8invoicing.ui.navigation.Screen
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.theme.ColorDarkGrayTransp
import com.a4a.g8invoicing.ui.theme.ColorHotPink
import com.a4a.g8invoicing.ui.theme.ColorLightGrey
import com.a4a.g8invoicing.ui.theme.ColorRedLate
import com.a4a.g8invoicing.ui.theme.ColorVioletLight
import com.a4a.g8invoicing.ui.theme.ColorVioletLink
import com.a4a.g8invoicing.ui.theme.callForActions
import com.a4a.g8invoicing.ui.theme.textNormalBold
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
    onExportDatabase: () -> ExportResult = { ExportResult.Error("Not available on this platform") },
    onSendDatabaseByEmail: (String) -> Unit = {},
    pendingMagicLinkToken: String? = null,
    onMagicLinkTokenConsumed: () -> Unit = {},
    viewModel: AccountViewModel = koinViewModel(),
) {
    val uriHandler = LocalUriHandler.current
    val uiState = viewModel.uiState

    val isDimActive = remember { mutableStateOf(false) }

    // Backup section local state (export + send-by-email + error dialogs).
    var exportedFilePath by remember { mutableStateOf<String?>(null) }
    var exportErrorMessage by remember { mutableStateOf<String?>(null) }
    var showExportErrorDialog by remember { mutableStateOf(false) }
    var showSendDatabaseByEmailDialog by remember { mutableStateOf(false) }

    // Delete-account confirmation dialog (Avancé section).
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    // Animated violet/pink brush, shared by call-to-action buttons in this screen.
    val infiniteTransition = rememberInfiniteTransition(label = "border")
    val targetOffset = with(LocalDensity.current) { 1000.dp.toPx() }
    val brushOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = targetOffset,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )
    val brushSize = 400f
    val ctaBrush = Brush.linearGradient(
        colors = listOf(ColorVioletLight, ColorHotPink),
        start = Offset(brushOffset, brushOffset),
        end = Offset(brushOffset + brushSize, brushOffset + brushSize),
        tileMode = TileMode.Mirror
    )

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (uiState.isLoggedIn) {
            viewModel.refreshSubscription()
        }
    }

    // Consume the magic link token from the deep link on this screen's own VM so the
    // success (logged-in UI) or error (expired-link dialog) lands on the same instance
    // that renders here — calling consume from MainCompose would target the Activity-
    // scoped VM, leaving this NavBackStackEntry-scoped one with stale null state.
    LaunchedEffect(pendingMagicLinkToken) {
        if (pendingMagicLinkToken != null) {
            viewModel.consumeMagicLink(pendingMagicLinkToken)
            onMagicLinkTokenConsumed()
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
                        onClearError = { viewModel.clearError() },
                        onClearSuccess = { viewModel.clearSuccess() },
                        uriHandler = uriHandler,
                    )
                }

                // Shown regardless of login state — a stale magic link clicked while
                // already logged in still gets explained, instead of silently doing nothing.
                if (uiState.consumeErrorMessage != null) {
                    AuthMessageDialog(
                        messagePrefix = stringResource(Res.string.account_auth_link_expired),
                        contactEmail = stringResource(Res.string.about_contact_email),
                        uriHandler = uriHandler,
                        onDismiss = { viewModel.clearConsumeError() },
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                CollapsibleSection(title = stringResource(Res.string.about_title_backup)) {
                    Text(
                        modifier = Modifier.padding(bottom = 16.dp),
                        text = stringResource(Res.string.about_backup_text)
                    )

                    Button(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .border(
                                BorderStroke(width = 4.dp, brush = ctaBrush),
                                shape = RoundedCornerShape(50)
                            ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                        onClick = {
                            val result = onExportDatabase()
                            when (result) {
                                is ExportResult.Success -> {
                                    exportedFilePath = result.filePath
                                    showSendDatabaseByEmailDialog = true
                                }
                                is ExportResult.Error -> {
                                    exportErrorMessage = result.message
                                    showExportErrorDialog = true
                                }
                            }
                        },
                    ) {
                        Text(stringResource(Res.string.about_download_database))
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                CollapsibleSection(title = stringResource(Res.string.account_my_companies)) {
                    MyCompaniesSection(navController = navController)
                }

                Spacer(modifier = Modifier.height(30.dp))

                CollapsibleSection(title = stringResource(Res.string.account_currency_and_language_title)) {
                    CurrencySelector()
                    Spacer(modifier = Modifier.height(8.dp))
                    LanguageSelector()
                }

                // "Avancé" — only relevant while logged in, since the only action
                // (account deletion) targets the server-side user record.
                if (uiState.isLoggedIn) {
                    Spacer(modifier = Modifier.height(30.dp))

                    CollapsibleSection(title = stringResource(Res.string.account_section_advanced)) {
                        Text(
                            modifier = Modifier
                                .clickable(enabled = !uiState.isDeleting) {
                                    showDeleteAccountDialog = true
                                },
                            text = stringResource(Res.string.account_delete_cta),
                            fontSize = 13.sp,
                            color = ColorRedLate,
                            textDecoration = TextDecoration.Underline,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        if (showDeleteAccountDialog) {
            AlertDialog(
                onDismissRequest = {
                    if (!uiState.isDeleting) showDeleteAccountDialog = false
                },
                title = { Text(stringResource(Res.string.account_delete_dialog_title)) },
                text = { Text(stringResource(Res.string.account_delete_dialog_message)) },
                confirmButton = {
                    TextButton(
                        enabled = !uiState.isDeleting,
                        onClick = {
                            showDeleteAccountDialog = false
                            viewModel.deleteAccount()
                        },
                    ) {
                        if (uiState.isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(18.dp).width(18.dp),
                                strokeWidth = 2.dp,
                                color = ColorRedLate,
                            )
                        } else {
                            Text(
                                stringResource(Res.string.account_delete_dialog_confirm),
                                color = ColorRedLate,
                            )
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        enabled = !uiState.isDeleting,
                        onClick = { showDeleteAccountDialog = false },
                    ) {
                        Text(
                            stringResource(Res.string.account_delete_dialog_cancel),
                            color = ColorVioletLink,
                        )
                    }
                },
            )
        }

        if (uiState.deleteErrorMessage != null) {
            AuthMessageDialog(
                messagePrefix = stringResource(Res.string.account_delete_error),
                contactEmail = stringResource(Res.string.about_contact_email),
                uriHandler = uriHandler,
                onDismiss = { viewModel.clearDeleteError() },
            )
        }

        if (uiState.accountDeleted) {
            AlertDialog(
                onDismissRequest = { viewModel.clearAccountDeleted() },
                title = { Text(stringResource(Res.string.account_delete_success_title)) },
                text = { Text(stringResource(Res.string.account_delete_success_message)) },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearAccountDeleted() }) {
                        Text(stringResource(Res.string.ok), color = ColorVioletLink)
                    }
                },
            )
        }

        if (showSendDatabaseByEmailDialog && exportedFilePath != null) {
            AlertDialog(
                onDismissRequest = { showSendDatabaseByEmailDialog = false },
                title = { Text(stringResource(Res.string.account_backup_dialog_title)) },
                text = { Text(stringResource(Res.string.account_backup_dialog_message)) },
                confirmButton = {
                    TextButton(onClick = {
                        showSendDatabaseByEmailDialog = false
                        exportedFilePath?.let { onSendDatabaseByEmail(it) }
                    }) {
                        Text(stringResource(Res.string.account_backup_dialog_yes), color = ColorVioletLink)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSendDatabaseByEmailDialog = false }) {
                        Text(stringResource(Res.string.account_backup_dialog_no), color = ColorVioletLink)
                    }
                }
            )
        }

        if (showExportErrorDialog) {
            AlertDialog(
                onDismissRequest = { showExportErrorDialog = false },
                text = { Text(exportErrorMessage ?: "") },
                confirmButton = {
                    TextButton(onClick = { showExportErrorDialog = false }) {
                        Text(stringResource(Res.string.ok), color = ColorVioletLink)
                    }
                }
            )
        }
    }
}

@Composable
private fun AuthMessageDialog(
    messagePrefix: String,
    contactEmail: String,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    onDismiss: () -> Unit,
) {
    val annotatedString = buildAnnotatedString {
        append(messagePrefix)
        pushStringAnnotation(tag = "email", annotation = "mailto:$contactEmail")
        withStyle(style = SpanStyle(color = ColorVioletLink)) {
            append(contactEmail)
        }
        pop()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            ClickableText(
                text = annotatedString,
                style = MaterialTheme.typography.bodyLarge,
                onClick = { offset ->
                    annotatedString
                        .getStringAnnotations(tag = "email", start = offset, end = offset)
                        .firstOrNull()?.let { uriHandler.openUri(it.item) }
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.ok), color = ColorVioletLink)
            }
        },
    )
}

@Composable
private fun ColumnScope.LoggedOutContent(
    uiState: AccountUiState,
    onSubmit: (String) -> Unit,
    onClearError: () -> Unit,
    onClearSuccess: () -> Unit,
    uriHandler: androidx.compose.ui.platform.UriHandler,
) {
    var email by remember { mutableStateOf("") }
    // Stays false until the user clicks "Receive my link" with an invalid
    // value. We don't want to flash a red error while they're still typing.
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    Text(
        text = stringResource(Res.string.account_auth_title),
        fontWeight = FontWeight.Bold,
    )

    Spacer(modifier = Modifier.height(16.dp))

    val violetSelectionColors = TextSelectionColors(
        handleColor = Color.Transparent,
        backgroundColor = ColorVioletLight.copy(alpha = 0.3f),
    )
    val trimmedEmail = email.trim()
    val emailIsValid = FormInputsValidator.isEmailValid(trimmedEmail)
    val showInvalidEmailError = hasAttemptedSubmit && !emailIsValid

    CompositionLocalProvider(LocalTextSelectionColors provides violetSelectionColors) {
        OutlinedTextField(
            value = email,
            onValueChange = { newValue ->
                // Cap at RFC 5321 max so we don't ever submit a 1MB string.
                email = if (newValue.length > FormInputsValidator.EMAIL_MAX_LENGTH) {
                    newValue.take(FormInputsValidator.EMAIL_MAX_LENGTH)
                } else newValue
            },
            label = { Text(stringResource(Res.string.account_auth_email_label)) },
            singleLine = true,
            enabled = !uiState.isLoading && uiState.successMessage == null,
            isError = showInvalidEmailError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ColorVioletLight,
                unfocusedBorderColor = Color.Black,
                focusedLabelColor = ColorVioletLight,
                unfocusedLabelColor = Color.Black,
                cursorColor = ColorVioletLight,
            ),
            supportingText = if (showInvalidEmailError) {
                { Text(stringResource(Res.string.account_auth_invalid_email)) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    TextButton(
        onClick = {
            if (emailIsValid) {
                hasAttemptedSubmit = false
                onSubmit(trimmedEmail)
            } else {
                hasAttemptedSubmit = true
            }
        },
        enabled = !uiState.isLoading && uiState.successMessage == null,
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
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

    val contactEmail = stringResource(Res.string.about_contact_email)
    if (uiState.errorMessage != null) {
        AuthMessageDialog(
            messagePrefix = stringResource(Res.string.account_auth_error),
            contactEmail = contactEmail,
            uriHandler = uriHandler,
            onDismiss = onClearError,
        )
    }
    if (uiState.successMessage != null) {
        AuthMessageDialog(
            messagePrefix = stringResource(Res.string.account_auth_link_sent),
            contactEmail = contactEmail,
            uriHandler = uriHandler,
            onDismiss = onClearSuccess,
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    val aboutLabel = stringResource(Res.string.account_auth_about_link)
    val aboutUrl = stringResource(Res.string.account_auth_about_url)
    Text(
        modifier = Modifier.clickable { uriHandler.openUri(aboutUrl) },
        text = aboutLabel,
        fontSize = 13.sp,
        color = ColorVioletLink,
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
private fun CurrencySelector(
    currencyManager: CurrencyManager = koinInject(),
    localeManager: LocaleManager = koinInject(),
) {
    var showPicker by remember { mutableStateOf(false) }
    val currentCode = currencyManager.currentCurrency
    val recent = currencyManager.recentCurrencies

    val uiLanguageCode = when (localeManager.currentLanguage) {
        AppLanguage.SYSTEM -> null
        else -> localeManager.currentLanguage.code
    }

    val displayLabel = remember(currentCode, uiLanguageCode) {
        val name = currencyDisplayName(currentCode, uiLanguageCode)
        val symbol = currencySymbol(currentCode)
        "$currentCode — $name ($symbol)"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.Gray,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { showPicker = true }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = displayLabel, color = Color.Black)
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }

    if (showPicker) {
        CurrencyPicker(
            currentCode = currentCode,
            recentCodes = recent,
            onSelect = { code ->
                currencyManager.setCurrency(code)
                showPicker = false
            },
            onDismiss = { showPicker = false },
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSelector(
    localeManager: LocaleManager = koinInject()
) {
    var showSheet by remember { mutableStateOf(false) }
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.Gray,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { showSheet = true }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = getDisplayName(currentLanguage),
            color = Color.Black
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }

    if (showSheet) {
        androidx.compose.material3.ModalBottomSheet(onDismissRequest = { showSheet = false }) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                AppLanguage.values().forEach { language ->
                    Text(
                        text = getDisplayName(language),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                localeManager.setLanguage(language)
                                showSheet = false
                            }
                            .padding(vertical = 14.dp),
                        fontWeight = if (language == currentLanguage)
                            androidx.compose.ui.text.font.FontWeight.SemiBold
                        else androidx.compose.ui.text.font.FontWeight.Normal,
                    )
                }
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

    // "+ Ajouter une entreprise" — violet plain CTA, no fill, tight to the list.
    Text(
        style = MaterialTheme.typography.callForActions,
        color = ColorVioletLink,
        modifier = Modifier
            .padding(start = 4.dp, top = 4.dp)
            .clickable {
                navController.navigate(Screen.ClientAddEdit.name + "?type=issuer")
            },
        text = stringResource(Res.string.account_add_company),
    )
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
