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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.a4a.g8invoicing.ui.screens.shared.ScaffoldWithDimmedOverlay
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.a4a.g8invoicing.data.auth.isPremium
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.about_language_english
import com.a4a.g8invoicing.shared.resources.about_terms_of_service_url_1
import com.a4a.g8invoicing.shared.resources.about_terms_of_service_url_2
import com.a4a.g8invoicing.shared.resources.account_legal_line
import com.a4a.g8invoicing.shared.resources.account_legal_privacy_label
import com.a4a.g8invoicing.shared.resources.account_legal_terms_label
import com.a4a.g8invoicing.shared.resources.about_language_french
import com.a4a.g8invoicing.shared.resources.about_language_german
import com.a4a.g8invoicing.shared.resources.about_language_spanish
import com.a4a.g8invoicing.shared.resources.about_language_system
import com.a4a.g8invoicing.shared.resources.about_title_language
import com.a4a.g8invoicing.shared.resources.account_currency_title
import com.a4a.g8invoicing.shared.resources.account_currency_and_language_title
import com.a4a.g8invoicing.ui.shared.AlertDialogDeleteDocument
import com.a4a.g8invoicing.ui.shared.CollapsibleSection
import com.a4a.g8invoicing.ui.shared.FormInputsValidator
import com.a4a.g8invoicing.ui.shared.WebsiteFooter
import com.a4a.g8invoicing.shared.resources.account_auth_email_label
import com.a4a.g8invoicing.shared.resources.account_website_label
import com.a4a.g8invoicing.shared.resources.account_website_url
import com.a4a.g8invoicing.shared.resources.gstore_footer_free
import com.a4a.g8invoicing.shared.resources.account_auth_error
import com.a4a.g8invoicing.shared.resources.account_auth_invalid_email
import com.a4a.g8invoicing.shared.resources.account_auth_link_sent
import com.a4a.g8invoicing.shared.resources.account_auth_send_link
import com.a4a.g8invoicing.shared.resources.account_auth_subtitle
import com.a4a.g8invoicing.shared.resources.account_auth_title
import com.a4a.g8invoicing.shared.resources.account_add_company
import com.a4a.g8invoicing.shared.resources.account_logout
import com.a4a.g8invoicing.shared.resources.account_my_companies
import com.a4a.g8invoicing.shared.resources.document_bottom_sheet_picker_edit_link
import com.a4a.g8invoicing.shared.resources.drawer_my_company
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
import com.a4a.g8invoicing.shared.resources.account_auth_login_failed
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
import com.a4a.g8invoicing.shared.resources.entreprise_delete_blocked_message
import com.a4a.g8invoicing.shared.resources.entreprise_delete_blocked_title
import com.a4a.g8invoicing.shared.resources.ok
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.shared.GeneralBottomBar
import com.a4a.g8invoicing.ui.navigation.Screen
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.ColorDarkGrayTransp
import com.a4a.g8invoicing.ui.theme.ColorHotPink
import com.a4a.g8invoicing.ui.theme.ColorOrange
import com.a4a.g8invoicing.ui.theme.ColorRedLate
import com.a4a.g8invoicing.ui.theme.ColorVioletLight
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.theme.ColorVioletLink
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerListViewModel
import com.a4a.g8invoicing.ui.theme.textBodyBold
import com.a4a.g8invoicing.ui.theme.textBodySmall
import com.a4a.g8invoicing.ui.theme.textCaption
import com.a4a.g8invoicing.ui.theme.textScreenTitle
import com.a4a.g8invoicing.ui.theme.textSecondary
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import com.a4a.g8invoicing.data.auth.ActivatedModulesRepository
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
    onRestoreDatabase: () -> Unit = {},
    isCategoriesMenuOpen: Boolean = false,
    onCategoriesMenuOpenChange: (Boolean) -> Unit = {},
    // Optional hint from the nav arg (?section=my_companies) — when set to a
    // known section id, the matching CollapsibleSection lands expanded.
    expandSection: String? = null,
    viewModel: AccountViewModel = koinViewModel(),
    issuersListViewModel: ClientOrIssuerListViewModel = koinViewModel(),
) {
    val uriHandler = LocalUriHandler.current
    val uiState = viewModel.uiState
    val issuersUiState by issuersListViewModel.issuersUiState.collectAsState()
    val issuersCount = issuersUiState.clientsOrIssuerList.orEmpty().size

    val isDimActive = remember { mutableStateOf(false) }

    // Backup section local state (export + send-by-email + error dialogs).
    var exportedFilePath by remember { mutableStateOf<String?>(null) }
    var exportErrorMessage by remember { mutableStateOf<String?>(null) }
    var showExportErrorDialog by remember { mutableStateOf(false) }
    var showSendDatabaseByEmailDialog by remember { mutableStateOf(false) }

    // Delete-account confirmation dialog (Avancé section).
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    // Result dialog opens immediately on confirm and morphs between loader → success →
    // error as the DELETE /v1/account call resolves. Keeping it a single dialog means the
    // user sees an instant response to their click instead of "nothing happens for 3s".
    var showDeletionResult by remember { mutableStateOf(false) }

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

    ScaffoldWithDimmedOverlay(
        isDimmed = isDimActive.value,
        onDismissDim = { isDimActive.value = false },
        containerColor = AppColors.surface,
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
                isButtonNewDisplayed = false,
                isCategoriesMenuOpen = isCategoriesMenuOpen,
                onCategoriesMenuOpenChange = onCategoriesMenuOpenChange,
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

                    // Secondary CTA under the download button — opens the
                    // restore flow (SAF picker → validation → kill process →
                    // Application.onCreate applies the swap on next launch).
                    // TODO(i18n): "Restaurer" hardcoded; extract via the
                    // translations branch (e.g. `account_restore_database`).
                    TextButton(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = onRestoreDatabase,
                    ) {
                        Text(
                            text = "Restaurer",
                            color = ColorVioletLink,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                val myCompaniesTitle = if (issuersCount <= 1) {
                    stringResource(Res.string.drawer_my_company)
                } else {
                    stringResource(Res.string.account_my_companies)
                }
                CollapsibleSection(
                    title = myCompaniesTitle,
                    initiallyExpanded = expandSection == "my_companies",
                ) {
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
                            style = MaterialTheme.typography.textSecondary.copy(
                                color = ColorRedLate,
                                textDecoration = TextDecoration.Underline,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))

                val websiteUrl = stringResource(Res.string.account_website_url)
                WebsiteFooter(
                    prefix = stringResource(Res.string.gstore_footer_free),
                    linkLabel = stringResource(Res.string.account_website_label),
                    onClickLink = { uriHandler.openUri(websiteUrl) },
                )
                Spacer(modifier = Modifier.height(8.dp))
                LegalLinksFooter(uriHandler = uriHandler)
            }
        }

        if (showDeleteAccountDialog) {
            com.a4a.g8invoicing.ui.shared.AppConfirmDialog(
                title = stringResource(Res.string.account_delete_dialog_title),
                body = stringResource(Res.string.account_delete_dialog_message),
                confirmText = stringResource(Res.string.account_delete_dialog_confirm),
                cancelText = stringResource(Res.string.account_delete_dialog_cancel),
                destructive = true,
                onConfirm = {
                    showDeleteAccountDialog = false
                    showDeletionResult = true
                    viewModel.deleteAccount()
                },
                onDismiss = { showDeleteAccountDialog = false },
            )
        }

        if (showDeletionResult) {
            val contactEmail = stringResource(Res.string.about_contact_email)
            val errorPrefix = stringResource(Res.string.account_delete_error)

            // Errors include a tappable mailto: contact@the-gate.fr — same construction
            // as AuthMessageDialog, inlined here so we can drop it into the same dialog
            // body as the loader/success state.
            val errorAnnotated = remember(uiState.deleteErrorMessage) {
                if (uiState.deleteErrorMessage == null) null else buildAnnotatedString {
                    append(errorPrefix)
                    pushStringAnnotation(tag = "email", annotation = "mailto:$contactEmail")
                    withStyle(style = SpanStyle(color = ColorVioletLink)) { append(contactEmail) }
                    pop()
                }
            }

            val closeAndReset: () -> Unit = {
                showDeletionResult = false
                viewModel.clearAccountDeleted()
                viewModel.clearDeleteError()
            }

            AlertDialog(
                // Block dismissal (back button / tap outside) while the call is in flight
                // so the user can't accidentally close mid-delete and end up unsure
                // whether it succeeded.
                onDismissRequest = { if (!uiState.isDeleting) closeAndReset() },
                title = {
                    if (uiState.accountDeleted) {
                        Text(stringResource(Res.string.account_delete_success_title))
                    }
                },
                text = {
                    when {
                        uiState.isDeleting -> Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = ColorVioletLight)
                        }
                        uiState.accountDeleted -> Text(
                            stringResource(Res.string.account_delete_success_message)
                        )
                        errorAnnotated != null -> ClickableText(
                            text = errorAnnotated,
                            style = MaterialTheme.typography.bodyLarge,
                            onClick = { offset ->
                                errorAnnotated
                                    .getStringAnnotations(tag = "email", start = offset, end = offset)
                                    .firstOrNull()
                                    ?.let { uriHandler.openUri(it.item) }
                            },
                        )
                    }
                },
                confirmButton = {
                    // Hide the OK while loading — there's nothing to confirm yet, and
                    // we don't want the user to dismiss the dialog mid-call.
                    if (!uiState.isDeleting) {
                        TextButton(onClick = closeAndReset) {
                            Text(stringResource(Res.string.ok), color = ColorVioletLink)
                        }
                    }
                },
            )
        }

        if (showSendDatabaseByEmailDialog && exportedFilePath != null) {
            com.a4a.g8invoicing.ui.shared.AppConfirmDialog(
                title = stringResource(Res.string.account_backup_dialog_title),
                body = stringResource(Res.string.account_backup_dialog_message),
                confirmText = stringResource(Res.string.account_backup_dialog_yes),
                cancelText = stringResource(Res.string.account_backup_dialog_no),
                onConfirm = {
                    showSendDatabaseByEmailDialog = false
                    exportedFilePath?.let { onSendDatabaseByEmail(it) }
                },
                onDismiss = { showSendDatabaseByEmailDialog = false },
            )
        }

        if (showExportErrorDialog) {
            com.a4a.g8invoicing.ui.shared.AppInfoDialog(
                body = exportErrorMessage ?: "",
                confirmText = stringResource(Res.string.ok),
                onDismiss = { showExportErrorDialog = false },
            )
        }
    }
}

@Composable
fun AuthMessageDialog(
    messagePrefix: String,
    contactEmail: String?,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    onDismiss: () -> Unit,
    showOhNoHeader: Boolean = false,
) {
    val annotatedString = buildAnnotatedString {
        append(messagePrefix)
        if (contactEmail != null) {
            pushStringAnnotation(tag = "email", annotation = "mailto:$contactEmail")
            withStyle(style = SpanStyle(color = ColorVioletLink)) {
                append(contactEmail)
            }
            pop()
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Column {
                if (showOhNoHeader) {
                    // Same "oh no" + kaomoji block as the export error dialog
                    // (alert_dialog_error) — kept at the modal's body text size so
                    // it stays consistent across the app.
                    Text(
                        text = OH_NO_HEADER,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                ClickableText(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodyLarge,
                    onClick = { offset ->
                        annotatedString
                            .getStringAnnotations(tag = "email", start = offset, end = offset)
                            .firstOrNull()?.let { uriHandler.openUri(it.item) }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.ok), color = ColorVioletLink)
            }
        },
    )
}

// TODO(i18n): move these to strings.xml via the `translations` branch post-1.9.
// Kept as Kotlin constants for now so release/1.9 doesn't have to round-trip through
// Weblate for shipping.
private const val OH_NO_HEADER = "𝕠𝕙 𝕟𝕠\n((˃ᯅ˂)ノ)"
private const val OFFLINE_MESSAGE_FR =
    "Tu sembles hors-ligne.. ou ta connexion internet (Wi-Fi ou données mobile) est instable."

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
        style = MaterialTheme.typography.textBodyBold,
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
            modifier = Modifier.fillMaxWidth(),
        )
    }
    // Rendered outside the OutlinedTextField (instead of via supportingText)
    // so the error text sits flush with the field's left edge — Material3's
    // default supportingText adds a 16.dp start padding we don't want.
    if (showInvalidEmailError) {
        Text(
            text = stringResource(Res.string.account_auth_invalid_email),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp),
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
        // Stays enabled as soon as the user has typed *something* (even invalid):
        // clicking with a malformed address shows the inline error rather than
        // silently freezing. Disabling on empty is intentional — there's nothing
        // to react to until the user has put a character in the field.
        enabled = !uiState.isLoading
            && uiState.successMessage == null
            && trimmedEmail.isNotEmpty(),
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
    val serverErrorPrefix = stringResource(Res.string.account_auth_error)
    if (uiState.errorMessage != null) {
        val isOffline = uiState.errorMessage == "magic_link_offline"
        AuthMessageDialog(
            messagePrefix = if (isOffline) OFFLINE_MESSAGE_FR else serverErrorPrefix,
            contactEmail = if (isOffline) null else contactEmail,
            uriHandler = uriHandler,
            onDismiss = onClearError,
            showOhNoHeader = true,
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

}

@Composable
private fun LoggedInContent(
    email: String?,
    subscriptionState: SubscriptionState,
    onLogout: () -> Unit,
    onOpenManageSubscription: (fallbackUrl: String) -> Unit,
) {
    if (!email.isNullOrBlank()) {
        Text(text = email)
        Spacer(modifier = Modifier.height(12.dp))
    }

    // Subscription section. Stripe status is the source of truth:
    // - "active"                                → premium badge + renewal/cancellation date + portal link
    // - "past_due" / "unpaid" / "incomplete"    → payment-failure card + update-card CTA (Stripe Portal)
    // - "canceled"                              → expired card + resubscribe CTA (Stripe Portal)
    // - null / anything else                    → nothing (never subscribed, or unknown/offline state)
    // Managing an existing subscription via Stripe Portal is explicitly allowed by Play
    // Store and Apple; only *selling* through an external link is forbidden.
    val known = subscriptionState as? SubscriptionState.Known
    val manageFallbackUrl = stringResource(Res.string.account_manage_subscription_url)

    when (known?.status) {
        "active" -> {
            val premiumStatusRes: StringResource? = when (known.product) {
                "fly" -> Res.string.account_status_premium_fly
                "fab" -> Res.string.account_status_premium_fab
                else -> null
            }
            if (premiumStatusRes != null) {
                PremiumBadge(label = stringResource(premiumStatusRes))
                Spacer(modifier = Modifier.height(8.dp))

                known.currentPeriodEndMs?.let { ms ->
                    val dateLabel = formatRenewalDate(ms)
                    val text = if (known.cancelAtPeriodEnd) {
                        stringResource(Res.string.account_cancellation_date, dateLabel)
                    } else {
                        stringResource(Res.string.account_renewal_date, dateLabel)
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.textSecondary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                val manageLabel = stringResource(Res.string.account_manage_subscription)
                Text(
                    modifier = Modifier.clickable { onOpenManageSubscription(manageFallbackUrl) },
                    text = manageLabel,
                    style = MaterialTheme.typography.textSecondary.copy(
                        color = ColorVioletLight,
                        textDecoration = TextDecoration.Underline,
                    ),
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        "past_due", "unpaid", "incomplete" -> {
            // CTA reuses account_manage_subscription — Apple/Play forbid external links to
            // *sell* a subscription; managing an existing one via Stripe Portal is allowed.
            // TODO(strings): move the FR literals to composeResources/values/strings.xml
            // on the `translations` branch. Suggested keys:
            //   account_status_payment_failed_title = "Paiement en échec"
            //   account_status_payment_failed_body  = "On n'a pas pu prélever ta carte. Mets à jour ton moyen de paiement pour garder tes fonctions premium."
            SubscriptionAlertBadge(
                title = "Paiement en échec",
                body = "On n'a pas pu prélever ta carte. Mets à jour ton moyen de paiement pour garder tes fonctions premium.",
                ctaLabel = stringResource(Res.string.account_manage_subscription),
                onCtaClick = { onOpenManageSubscription(manageFallbackUrl) },
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        "canceled" -> {
            // TODO(strings): move the FR literals to composeResources/values/strings.xml
            // on the `translations` branch. Suggested keys:
            //   account_status_canceled_title = "Abonnement expiré"
            //   account_status_canceled_body  = "Ton abonnement premium est terminé. Gère ton abonnement pour retrouver l'accès aux fonctions premium."
            SubscriptionAlertBadge(
                title = "Abonnement expiré",
                body = "Ton abonnement premium est terminé. Gère ton abonnement pour retrouver l'accès aux fonctions premium.",
                ctaLabel = stringResource(Res.string.account_manage_subscription),
                onCtaClick = { onOpenManageSubscription(manageFallbackUrl) },
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        else -> Unit
    }

    // Confirm logout for premium users. Losing premium at logout is a real functional
    // change (creation buttons block, watermark comes back on new documents), so we
    // ask before pulling the rug. Non-premium logout stays a single-click action —
    // asking there would be noise since nothing changes for them.
    val isPremiumEntitlement = subscriptionState.isPremium()
    var showLogoutConfirm by remember { mutableStateOf(false) }

    // No separator between manage subscription and logout — design choice.
    Text(
        modifier = Modifier.clickable {
            if (isPremiumEntitlement) showLogoutConfirm = true else onLogout()
        },
        text = stringResource(Res.string.account_logout),
        style = MaterialTheme.typography.textSecondary.copy(
            color = ColorVioletLight,
            textDecoration = TextDecoration.Underline,
        ),
    )

    if (showLogoutConfirm) {
        // TODO(strings): move the FR literals below to composeResources/values/strings.xml
        // on the `translations` branch. Suggested keys:
        //   account_logout_premium_confirm_title  = "Se déconnecter ?"
        //   account_logout_premium_confirm_body   = "Vos fonctions premium seront indisponibles jusqu'à reconnexion. Vos documents restent sur cet appareil."
        //   account_logout_premium_confirm_ok     = "Se déconnecter"
        //   account_logout_premium_confirm_cancel = "Annuler"
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Se déconnecter ?") },
            text = {
                Text(
                    "Vos fonctions premium seront indisponibles jusqu'à reconnexion. Vos documents restent sur cet appareil.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    onLogout()
                }) { Text("Se déconnecter") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Annuler") }
            },
        )
    }
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

// Card shown when the subscription is in a bad state (past_due, unpaid, incomplete,
// canceled). Same rounded-container language as PremiumBadge, tinted orange to signal
// "action needed". The CTA opens the Stripe Customer Portal so the user can update
// their card or restart their subscription.
@Composable
private fun SubscriptionAlertBadge(
    title: String,
    body: String,
    ctaLabel: String,
    onCtaClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = ColorOrange.copy(alpha = 0.10f),
                shape = RoundedCornerShape(10.dp),
            )
            .border(
                width = 1.dp,
                color = ColorOrange.copy(alpha = 0.35f),
                shape = RoundedCornerShape(10.dp),
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.textBodyBold,
            color = ColorOrange,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.textSecondary,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            modifier = Modifier.clickable { onCtaClick() },
            text = ctaLabel,
            style = MaterialTheme.typography.textSecondary.copy(
                color = ColorVioletLight,
                textDecoration = TextDecoration.Underline,
            ),
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
            tint = AppColors.iconSecondary
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
    val spanishLabel = stringResource(Res.string.about_language_spanish)

    fun getDisplayName(language: AppLanguage): String = when (language) {
        AppLanguage.SYSTEM -> systemLabel
        AppLanguage.FRENCH -> frenchLabel
        AppLanguage.ENGLISH -> englishLabel
        AppLanguage.GERMAN -> germanLabel
        AppLanguage.SPANISH -> spanishLabel
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
            tint = AppColors.iconSecondary
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
    modulesRepo: ActivatedModulesRepository = koinInject(),
) {
    val issuersUiState by listViewModel.issuersUiState.collectAsState()
    val issuers = issuersUiState.clientsOrIssuerList.orEmpty()
    val activated by modulesRepo.state.collectAsState()
    val multiEntrepriseOn = ActivatedModulesRepository.MODULE_MULTI_ENTREPRISE in activated
    val scope = rememberCoroutineScope()
    // Non-null when the user tapped delete on an entreprise that still has
    // clients/products/documents attached. Triggers the alert; user can only
    // dismiss (destructive path is deliberately not offered — data must be
    // detached or deleted first).
    var deleteBlocked by remember { mutableStateOf(false) }
    // Non-null when the user tapped delete on an empty entreprise: hold until
    // the user confirms via the AlertDialogDeleteDocument prompt. Deleting an
    // entreprise is irreversible, so the single-tap trash affordance always
    // routes through the shared confirm dialog first.
    var pendingDeleteIssuer by remember { mutableStateOf<ClientOrIssuerState?>(null) }

    // Deleting the last remaining entreprise would leave the doc-edit flows
    // with no issuer to attach — the picker + numbering counter both rely on
    // at least one existing issuer row. Hide the trash affordance in that
    // case so the state simply can't be reached from this screen.
    val canDeleteRow = issuers.size > 1
    issuers.forEach { issuer ->
        IssuerListRow(
            issuer = issuer,
            showDelete = canDeleteRow,
            onClick = {
                navController.navigate(
                    Screen.ClientAddEdit.name + "?itemId=${issuer.id}&type=issuer"
                )
            },
            onDelete = {
                val issuerId = issuer.id?.toLong()
                if (issuerId == null) {
                    pendingDeleteIssuer = issuer
                } else {
                    scope.launch {
                        val attached = listViewModel.countAttachedForIssuer(issuerId)
                        if (attached == 0L) {
                            pendingDeleteIssuer = issuer
                        } else {
                            deleteBlocked = true
                        }
                    }
                }
            },
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    pendingDeleteIssuer?.let { issuer ->
        AlertDialogDeleteDocument(
            onDismissRequest = { pendingDeleteIssuer = null },
            onConfirmation = {
                listViewModel.deleteClientsOrIssuers(listOf(issuer))
                pendingDeleteIssuer = null
            },
        )
    }

    if (deleteBlocked) {
        com.a4a.g8invoicing.ui.shared.AppInfoDialog(
            title = stringResource(Res.string.entreprise_delete_blocked_title),
            body = stringResource(Res.string.entreprise_delete_blocked_message),
            confirmText = stringResource(Res.string.ok),
            onDismiss = { deleteBlocked = false },
        )
    }

    // "+ Ajouter une entreprise" — hidden when the multi-entreprise module is off
    // (single-entreprise UX). The gStore card is where users go to opt in.
    if (multiEntrepriseOn) {
        Text(
            style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary),
            color = ColorVioletLink,
            modifier = Modifier
                .padding(start = 4.dp, top = 4.dp)
                .clickable {
                    navController.navigate(Screen.ClientAddEdit.name + "?type=issuer")
                },
            text = stringResource(Res.string.account_add_company),
        )
    }
}

@Composable
private fun IssuerListRow(
    issuer: ClientOrIssuerState,
    showDelete: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(5.dp))
            .background(AppColors.surfaceMuted)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1F)) {
            Text(
                text = issuer.name.text + (issuer.firstName?.let { " " + it.text } ?: ""),
                style = MaterialTheme.typography.textBodySmall.copy(fontWeight = FontWeight.SemiBold),
            )
            // "Éditer" affordance mirrors the picker's edit hint (same textCaption
            // size, underline) — but black instead of the picker's violet since
            // this block sits on a grey background where violet washes out.
            // Tapping it fires the same onClick as the row body, so it's purely
            // a discoverability nudge (the whole surface is already tappable).
            Text(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable(onClick = onClick),
                text = stringResource(Res.string.document_bottom_sheet_picker_edit_link),
                style = MaterialTheme.typography.textCaption.copy(
                    color = AppColors.textPrimary,
                    textDecoration = TextDecoration.Underline,
                ),
            )
        }
        if (showDelete) {
            Icon(
                modifier = Modifier
                    .size(18.dp)
                    .clickable(onClick = onDelete),
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun LegalLinksFooter(uriHandler: androidx.compose.ui.platform.UriHandler) {
    val termsUrl = stringResource(Res.string.about_terms_of_service_url_1)
    val privacyUrl = stringResource(Res.string.about_terms_of_service_url_2)
    val termsLabel = stringResource(Res.string.account_legal_terms_label)
    val privacyLabel = stringResource(Res.string.account_legal_privacy_label)
    // Template like "Lire les %1$s et la %2$s" (FR) or "%1$s und %2$s lesen" (DE).
    // We walk the placeholders in appearance order to preserve grammar per locale.
    val template = stringResource(Res.string.account_legal_line)

    val annotated = buildAnnotatedString {
        var cursor = 0
        while (cursor < template.length) {
            val nextTerms = template.indexOf("%1\$s", cursor)
            val nextPrivacy = template.indexOf("%2\$s", cursor)
            val next = listOf(nextTerms, nextPrivacy).filter { it >= 0 }.minOrNull() ?: -1
            if (next < 0) {
                withStyle(SpanStyle(color = Color.DarkGray)) {
                    append(template.substring(cursor))
                }
                break
            }
            if (next > cursor) {
                withStyle(SpanStyle(color = Color.DarkGray)) {
                    append(template.substring(cursor, next))
                }
            }
            if (next == nextTerms) {
                pushStringAnnotation(tag = "terms", annotation = termsUrl)
                withStyle(SpanStyle(color = ColorVioletLight, fontWeight = FontWeight.SemiBold)) {
                    append(termsLabel)
                }
                pop()
            } else {
                pushStringAnnotation(tag = "policy", annotation = privacyUrl)
                withStyle(SpanStyle(color = ColorVioletLight, fontWeight = FontWeight.SemiBold)) {
                    append(privacyLabel)
                }
                pop()
            }
            cursor = next + 4 // length of "%1$s" or "%2$s"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        ClickableText(
            text = annotated,
            style = MaterialTheme.typography.textSecondary.copy(textAlign = TextAlign.Center),
            onClick = { offset ->
                annotated.getStringAnnotations(tag = "terms", start = offset, end = offset)
                    .firstOrNull()?.let { uriHandler.openUri(it.item) }
                annotated.getStringAnnotations(tag = "policy", start = offset, end = offset)
                    .firstOrNull()?.let { uriHandler.openUri(it.item) }
            },
        )
    }
}
