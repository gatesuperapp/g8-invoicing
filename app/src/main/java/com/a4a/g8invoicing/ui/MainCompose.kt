package com.a4a.g8invoicing.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import g8invoicing.ClientOrIssuerQueries
import g8invoicing.DeliveryNoteQueries
import g8invoicing.InvoiceQueries
import g8invoicing.ProductQueries
import com.a4a.g8invoicing.data.LocaleManager
import com.a4a.g8invoicing.data.initializeVersionTracking
import com.a4a.g8invoicing.data.setSeenEInvoiceIntro
import com.a4a.g8invoicing.data.setSeenOnboarding18
import com.a4a.g8invoicing.data.setSeenWhatsNew
import com.a4a.g8invoicing.data.shouldShowBackupPopupNow
import com.a4a.g8invoicing.data.shouldShowEInvoiceIntro
import com.a4a.g8invoicing.data.shouldShowOnboarding18
import com.a4a.g8invoicing.data.shouldShowWhatsNew
import com.a4a.g8invoicing.data.auth.AuthRepository
import com.a4a.g8invoicing.data.auth.AuthResult
import com.a4a.g8invoicing.data.auth.AuthState
import com.a4a.g8invoicing.data.auth.SubscriptionRepository
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.about_contact_email
import com.a4a.g8invoicing.shared.resources.account_auth_link_expired
import com.a4a.g8invoicing.shared.resources.account_auth_login_failed
import com.a4a.g8invoicing.ui.navigation.NavGraph
import com.a4a.g8invoicing.ui.navigation.Screen
import com.a4a.g8invoicing.ui.screens.AuthMessageDialog
import com.a4a.g8invoicing.ui.screens.DatabaseEmailDialog
import com.a4a.g8invoicing.ui.screens.DatabaseExportDialog
import com.a4a.g8invoicing.ui.screens.ExportPdfPlatform
import com.a4a.g8invoicing.ui.screens.ExportResult
import com.a4a.g8invoicing.ui.screens.exportDatabaseToDownloads
import com.a4a.g8invoicing.ui.screens.sendDatabaseByEmail
import com.a4a.g8invoicing.ui.states.InvoiceState
import androidx.compose.ui.platform.LocalUriHandler
import android.content.Intent
import android.net.Uri
import java.io.File
import com.a4a.g8invoicing.ui.theme.G8InvoicingTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun MainCompose(
    onSendReminder: (InvoiceState) -> Unit = {},
    pendingMagicLinkToken: String? = null,
    onMagicLinkTokenConsumed: () -> Unit = {},
    localeManager: LocaleManager = koinInject()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authRepository: AuthRepository = koinInject()
    val subscriptionRepository: SubscriptionRepository = koinInject()
    val authState by authRepository.authState.collectAsState()
    val invoiceQueries: InvoiceQueries = koinInject()
    val deliveryNoteQueries: DeliveryNoteQueries = koinInject()
    val productQueries: ProductQueries = koinInject()
    val clientOrIssuerQueries: ClientOrIssuerQueries = koinInject()

    // Initialize locale and version tracking on first composition. The done
    // flag gates the popup-firing LaunchedEffect below — without it, a fresh
    // install races: shouldShowEInvoiceIntro emits `true` (HAS_SEEN=false) a
    // fraction of a second before initializeVersionTracking has had time to
    // flip HAS_SEEN=true, and the popup fires anyway.
    var versionTrackingDone by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        localeManager.initializeLocale()
        initializeVersionTracking(context)
        versionTrackingDone = true
    }

    // What's New + Onboarding dialog state. The 1.8 onboarding takes priority
    // over the generic What's New — the onboarding's welcome screen already
    // mentions the Devis feature, so showing both would be redundant.
    // `initial = null` so the LaunchedEffect can distinguish "DataStore hasn't
    // emitted yet" from "flag is legitimately false" — see the LaunchedEffect
    // below for the race the null guard prevents.
    val shouldShow by shouldShowWhatsNew(context).collectAsState(initial = null)
    val shouldShowOnboarding by shouldShowOnboarding18(context).collectAsState(initial = null)
    val shouldShowEInvoice by shouldShowEInvoiceIntro(context).collectAsState(initial = null)
    var showWhatsNew by remember { mutableStateOf(false) }
    var showOnboarding by remember { mutableStateOf(false) }
    var showEInvoiceIntro by remember { mutableStateOf(false) }
    // Backup reminder: shown once when the user has >3 rows in any main table.
    // Suppressed while onboarding / what's new are pending to avoid stacking
    // modals at cold start.
    var showBackupDialog by remember { mutableStateOf(false) }
    var backupExportedFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(shouldShow, shouldShowOnboarding, shouldShowEInvoice, versionTrackingDone) {
        // Wait until every DataStore flag has emitted its real value —
        // guarding against the initial=null race that used to flip
        // showBackupDialog on a version upgrade before the onboarding flag
        // resolved. versionTrackingDone gates the fresh-install path so
        // shouldShowEInvoice is read after HAS_SEEN_EINVOICE_INTRO has been
        // flipped for fresh installs.
        if (!versionTrackingDone) return@LaunchedEffect
        val whatsNew = shouldShow ?: return@LaunchedEffect
        val onboarding = shouldShowOnboarding ?: return@LaunchedEffect
        val eInvoice = shouldShowEInvoice ?: return@LaunchedEffect
        showOnboarding = onboarding
        showWhatsNew = whatsNew && !onboarding
        // 1.8.1 e-invoice popup: only when the device is set to country=FR.
        // Country (not language) — the e-invoice obligation follows where the
        // phone is used, not which UI language the user picked. Reads a
        // snapshot taken in G8Invoicing.onCreate, BEFORE anything can call
        // Locale.setDefault; that's the only reliable way to read the Android
        // 13+ "Regional preferences → Region" setting (Resources.getSystem()
        // only carries the Language picker). Suppressed while the 1.8
        // onboarding wizard is pending. Mark SEEN=true immediately per product
        // decision.
        val systemCountry = com.a4a.g8invoicing.SystemRegionSnapshot.formatCountry
        if (eInvoice && !onboarding && systemCountry == "FR") {
            showEInvoiceIntro = true
            setSeenEInvoiceIntro(context)
        }
        if (!whatsNew && !onboarding && !showEInvoiceIntro) {
            showBackupDialog = shouldShowBackupPopupNow(
                context,
                invoiceQueries,
                deliveryNoteQueries,
                productQueries,
                clientOrIssuerQueries,
            )
        }
    }

    if (showEInvoiceIntro) {
        val uriHandler = LocalUriHandler.current
        com.a4a.g8invoicing.ui.shared.EInvoiceIntroDialog(
            onDismiss = { showEInvoiceIntro = false },
            onOpenUrl = { url ->
                try {
                    uriHandler.openUri(url)
                } catch (_: Exception) {
                    // Fallback: Android Intent if the ComposeUriHandler chokes
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            },
            onComposeEmail = { address, subject, body ->
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, body)
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                }
            },
        )
    }

    if (showBackupDialog) {
        DatabaseExportDialog(
            context = context,
            onDismiss = { showBackupDialog = false },
            onResult = { file ->
                showBackupDialog = false
                backupExportedFile = file
            },
        )
    }

    backupExportedFile?.let { file ->
        DatabaseEmailDialog(
            context = context,
            onDismiss = { backupExportedFile = null },
            file = file,
        )
    }

    // Track navController for deep link navigation
    var navControllerRef by remember { mutableStateOf<NavHostController?>(null) }

    // Error surfaced by the magic-link consume call — kept here (not in the Account
    // VM) so the dialog outlives Account's composition. Account can be destroyed
    // for reasons unrelated to auth (locale switch, NavGraph rebuild after DataStore
    // emits a new value, a stacked nav.navigate) and any state carried on its VM
    // would disappear with it — which used to make the dialog flash for ~2s and
    // then vanish.
    var consumeErrorMessage by remember { mutableStateOf<String?>(null) }

    // Navigate to Account when a deep-link token arrives, so a successful consume
    // lands the user directly on the logged-in Account view. navControllerRef is
    // part of the key so we wait until the NavHost has wired it up.
    LaunchedEffect(pendingMagicLinkToken, navControllerRef) {
        val nav = navControllerRef
        if (pendingMagicLinkToken != null && nav != null) {
            nav.navigate(Screen.Account.name)
        }
    }

    // Consume the deep-link token at the app root. Clearing pendingMagicLinkToken
    // is deferred until AFTER consumeMagicLink returns — clearing it earlier would
    // change the LaunchedEffect key mid-call and cancel the in-flight network
    // request.
    LaunchedEffect(pendingMagicLinkToken) {
        val token = pendingMagicLinkToken ?: return@LaunchedEffect
        val result = authRepository.consumeMagicLink(token)
        onMagicLinkTokenConsumed()
        if (result is AuthResult.Error) {
            consumeErrorMessage = result.message
        }
    }

    // Backend distinguishes "Lien invalide ou expiré" (401, link itself dead) from
    // generic 500s (other failures, e.g. user-creation conflicts) — pick the right
    // copy based on the message so a 500 doesn't get mislabelled as "link expired".
    consumeErrorMessage?.let { msg ->
        val isLinkExpired = msg.contains("expir", ignoreCase = true)
            || msg.contains("invalide", ignoreCase = true)
            || msg.contains("invalid", ignoreCase = true)
            || msg.contains("abgelaufen", ignoreCase = true)
        AuthMessageDialog(
            messagePrefix = stringResource(
                if (isLinkExpired) Res.string.account_auth_link_expired
                else Res.string.account_auth_login_failed
            ),
            contactEmail = stringResource(Res.string.about_contact_email),
            uriHandler = LocalUriHandler.current,
            onDismiss = { consumeErrorMessage = null },
        )
    }

    // Sync subscription status when auth state changes:
    // - On LoggedIn (boot or after magic-link consume), refresh /v1/me. The cache
    //   short-circuits if the data is < 6h old, so this is cheap on every cold start.
    // - On LoggedOut, clear the cached subscription so a stale "premium" doesn't bleed
    //   across sessions.
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.LoggedIn -> subscriptionRepository.refresh()
            is AuthState.LoggedOut -> subscriptionRepository.clear()
        }
    }

    G8InvoicingTheme {
        // Use Crossfade for smooth transition when language changes
        Crossfade(
            targetState = localeManager.currentLanguage,
            animationSpec = tween(300),
            label = "language_transition"
        ) { _ ->
            val navController: NavHostController = rememberNavController()

            LaunchedEffect(navController) {
                navControllerRef = navController
            }

            Surface(
                modifier = Modifier.fillMaxSize()
            ) {
                NavGraph(
                    navController = navController,
                    onSendReminder = onSendReminder,
                    exportPdfContent = { document, onDismiss ->
                        ExportPdfPlatform(document, onDismiss)
                    },
                    showWhatsNew = showWhatsNew,
                    onWhatsNewDismissed = {
                        showWhatsNew = false
                        coroutineScope.launch {
                            setSeenWhatsNew(context)
                        }
                    },
                    showOnboarding = showOnboarding,
                    onOnboardingDismissed = {
                        showOnboarding = false
                        coroutineScope.launch {
                            setSeenOnboarding18(context)
                            // Also mark What's New as seen — the onboarding
                            // already covered the same ground and we don't
                            // want it to fire on the next launch.
                            setSeenWhatsNew(context)
                        }
                    },
                    onShareContent = { content ->
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, content)
                        }
                        context.startActivity(Intent.createChooser(intent, null))
                    },
                    onExportDatabase = {
                        try {
                            val file = exportDatabaseToDownloads(context)
                            ExportResult.Success(file.absolutePath)
                        } catch (e: Exception) {
                            ExportResult.Error(e.message ?: "Unknown error")
                        }
                    },
                    onSendDatabaseByEmail = { filePath ->
                        coroutineScope.launch {
                            sendDatabaseByEmail(context, File(filePath))
                        }
                    },
                    onComposeEmail = { address, subject, body ->
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
                            putExtra(Intent.EXTRA_SUBJECT, subject)
                            putExtra(Intent.EXTRA_TEXT, body)
                        }
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        }
                    },
                )
            }
        }
    }
}
