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
import com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSourceInterface
import com.a4a.g8invoicing.data.CurrentCompanyRepository
import com.a4a.g8invoicing.data.InvoiceLocalDataSourceInterface
import com.a4a.g8invoicing.data.LocaleManager
import com.a4a.g8invoicing.data.PrefKeys
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.data.dataStore
import com.a4a.g8invoicing.data.initializeVersionTracking
import com.a4a.g8invoicing.data.models.CountryCodes
import com.a4a.g8invoicing.data.models.PersonType
import com.a4a.g8invoicing.data.setSeenEInvoiceIntro
import com.a4a.g8invoicing.data.setSeenOnboarding18
import com.a4a.g8invoicing.data.setSeenWhatsNew
import com.a4a.g8invoicing.data.shouldShowBackupPopupNow
import com.a4a.g8invoicing.data.shouldShowEInvoiceIntro
import com.a4a.g8invoicing.data.shouldShowOnboarding18
import com.a4a.g8invoicing.data.shouldShowWhatsNew
import com.a4a.g8invoicing.data.auth.ActivatedModulesRepository
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
import com.a4a.g8invoicing.ui.shared.FirstLaunchIssuerNameDialog
import com.a4a.g8invoicing.ui.shared.Migration19Actions
import com.a4a.g8invoicing.ui.shared.Migration19Context
import com.a4a.g8invoicing.ui.shared.OnboardingMigration19Dialog
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.platform.LocalUriHandler
import android.content.Intent
import android.net.Uri
import java.io.File
import com.a4a.g8invoicing.ui.theme.G8InvoicingTheme
import kotlinx.coroutines.flow.first
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
    val currentCompanyRepository: CurrentCompanyRepository = koinInject()
    val clientOrIssuerDataSource: ClientOrIssuerLocalDataSourceInterface = koinInject()
    val invoiceDataSource: InvoiceLocalDataSourceInterface = koinInject()
    val productDataSource: ProductLocalDataSourceInterface = koinInject()
    val modulesRepo: ActivatedModulesRepository = koinInject()

    // Initialize locale and version tracking on first composition. The done
    // flag gates the popup-firing LaunchedEffect below — without it, a fresh
    // install races: shouldShowEInvoiceIntro emits `true` (HAS_SEEN=false) a
    // fraction of a second before initializeVersionTracking has had time to
    // flip HAS_SEEN=true, and the popup fires anyway.
    var versionTrackingDone by remember { mutableStateOf(false) }
    // Also: hydrate the "current entreprise" from the most-recent issuer, or
    // if no issuer exists (fresh install), flip the first-launch dialog on.
    // Without a hydrated currentCompanyId, doc-list flows take the getAll()
    // fallback (no company filter) and any client created from the picker
    // attaches to company_id=NULL — which double-inserts once master lists
    // filter by company.
    var needsFirstLaunchIssuer by remember { mutableStateOf(false) }
    // 1.9 migration wizard state — surfaced only for existing installs (has
    // at least one issuer) that never went through the wizard before. Fresh
    // installs mark the flag straight after FirstLaunchIssuerNameDialog
    // completes so the wizard never surfaces there.
    var migration19Context by remember { mutableStateOf<Migration19Context?>(null) }
    LaunchedEffect(Unit) {
        localeManager.initializeLocale()
        // Snapshot the "returning user" signal BEFORE initializeVersionTracking
        // runs — on a fresh install it seeds LAST_SEEN_VERSION itself, which
        // would then look identical to a real upgrade if we read it later.
        val bootPrefs = context.dataStore.data.first()
        val isReturningUser = bootPrefs[PrefKeys.LAST_SEEN_VERSION] != null
            || (bootPrefs[PrefKeys.HAS_SEEN_POPUP] ?: false)
        initializeVersionTracking(context)
        val lastIssuerId = clientOrIssuerDataSource.getLastCreatedIssuerId()
        if (lastIssuerId == null) {
            if (isReturningUser) {
                // Existing user with a wiped or never-populated issuer table.
                // Would only happen if migration 7.sqm's "Mon entreprise" seed
                // was skipped for some reason. Repair silently — don't hit an
                // upgrader with the fresh-install welcome wizard.
                val defaultCountry = CountryCodes.pickDefaultForNewAddress(null)
                val seededIssuer = ClientOrIssuerState(
                    type = ClientOrIssuerType.ISSUER,
                    name = TextFieldValue("Mon entreprise"),
                    addresses = listOf(
                        com.a4a.g8invoicing.ui.states.AddressState(
                            countryCode = defaultCountry,
                        )
                    ),
                )
                clientOrIssuerDataSource.createNewAndReturnId(seededIssuer)
                    ?.let { currentCompanyRepository.setCurrent(it) }
                // Silent-repair path: nothing pops on screen, so consume every
                // legacy onboarding flag here too. Otherwise the 1.8 Devis
                // wizard / e-invoice popup / generic What's New would surface
                // on the very next boot, right after we quietly booted the
                // user straight into the app.
                modulesRepo.markMigration19Seen()
                setSeenOnboarding18(context)
                setSeenEInvoiceIntro(context)
                setSeenWhatsNew(context)
            } else {
                needsFirstLaunchIssuer = true
            }
        } else {
            currentCompanyRepository.initIfMissing { lastIssuerId }
            if (!modulesRepo.hasSeenMigration19()) {
                val issuers = clientOrIssuerDataSource
                    .fetchAll(PersonType.ISSUER).first()
                if (issuers.isEmpty()) {
                    modulesRepo.markMigration19Seen()
                } else {
                    val clients = clientOrIssuerDataSource
                        .fetchAll(PersonType.CLIENT).first()
                    val products = productDataSource.fetchAllProducts().first()
                    val footersByIssuer = issuers.mapNotNull { issuer ->
                        val id = issuer.id?.toLong() ?: return@mapNotNull null
                        id to invoiceDataSource.getRecentFootersForCompany(id)
                    }.toMap()
                    migration19Context = Migration19Context(
                        issuers = issuers,
                        clients = clients,
                        products = products,
                        footersByIssuer = footersByIssuer,
                    )
                }
            }
        }
        versionTrackingDone = true
    }

    if (needsFirstLaunchIssuer) {
        FirstLaunchIssuerNameDialog(
            onSubmit = { enteredName, enteredCountry ->
                val issuer = ClientOrIssuerState(
                    type = ClientOrIssuerType.ISSUER,
                    name = TextFieldValue(enteredName),
                    addresses = listOf(
                        com.a4a.g8invoicing.ui.states.AddressState(
                            countryCode = enteredCountry,
                        )
                    ),
                )
                val newId = clientOrIssuerDataSource.createNewAndReturnId(issuer)
                // setCurrent (not initIfMissing) so a stale Settings entry
                // from a previous session — Settings survives a DB wipe —
                // doesn't leave currentCompanyId pointing at a now-nonexistent
                // issuer.
                newId?.let { currentCompanyRepository.setCurrent(it) }
                // Fresh installs never see the 1.9 migration wizard.
                modulesRepo.markMigration19Seen()
                needsFirstLaunchIssuer = false
            }
        )
    }

    migration19Context?.let { ctx ->
        OnboardingMigration19Dialog(
            context = ctx,
            actions = Migration19Actions(
                deleteIssuer = { issuer ->
                    clientOrIssuerDataSource.deleteClientOrIssuer(issuer)
                },
                attachClients = { ids, issuerId ->
                    clientOrIssuerDataSource.bulkAttachToCompany(ids, issuerId)
                },
                attachProducts = { ids, issuerId ->
                    productDataSource.bulkAttachToCompany(ids, issuerId)
                },
                saveIssuerBank = { issuer, iban, bic ->
                    // Master ClientOrIssuer has no payment_iban / payment_bic
                    // columns — those live on DocumentClientOrIssuer as a
                    // frozen doc-side snapshot. Persist to the master by
                    // building an IssuerBankState and letting updateClientOrIssuer
                    // upsert the IssuerBank table via saveIssuerBanks(banks).
                    if (iban.isNotBlank() || bic.isNotBlank()) {
                        val newBank = com.a4a.g8invoicing.ui.states.IssuerBankState(
                            id = null,
                            countryCode = issuer.addresses?.firstOrNull()?.countryCode,
                            identifier = TextFieldValue(iban),
                            bic = TextFieldValue(bic),
                            sortOrder = 0,
                        )
                        val updated = issuer.copy(banks = listOf(newBank))
                        clientOrIssuerDataSource.updateClientOrIssuer(updated)
                    }
                },
                updateIssuerName = { issuer, newName ->
                    clientOrIssuerDataSource.updateClientOrIssuer(
                        issuer.copy(name = TextFieldValue(newName.trim()))
                    )
                },
                updateIssuerCountry = { issuer, countryCode ->
                    val existing = issuer.addresses?.firstOrNull()
                    val updatedAddress = existing?.copy(countryCode = countryCode)
                        ?: com.a4a.g8invoicing.ui.states.AddressState(countryCode = countryCode)
                    val otherAddresses = issuer.addresses?.drop(1).orEmpty()
                    clientOrIssuerDataSource.updateClientOrIssuer(
                        issuer.copy(addresses = listOf(updatedAddress) + otherAddresses)
                    )
                },
                exportDatabase = {
                    try {
                        val file = exportDatabaseToDownloads(context)
                        ExportResult.Success(file.absolutePath)
                    } catch (e: Exception) {
                        ExportResult.Error(e.message ?: "Unknown error")
                    }
                },
                sendDatabaseByEmail = { filePath ->
                    coroutineScope.launch {
                        sendDatabaseByEmail(context, File(filePath))
                    }
                },
                markSeen = {
                    // Completing the migration wizard is the user's definitive
                    // acknowledgement of the 1.9 upgrade — mark every legacy
                    // onboarding / popup flag as seen so nothing else pops on
                    // subsequent boots. Without this the 1.8 Devis wizard, the
                    // e-invoice intro and the generic What's New would still
                    // fire on the next launch because their flags predate 1.9.
                    modulesRepo.markMigration19Seen()
                    setSeenOnboarding18(context)
                    setSeenEInvoiceIntro(context)
                    setSeenWhatsNew(context)
                },
            ),
            onDismiss = { migration19Context = null },
        )
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

    LaunchedEffect(shouldShow, shouldShowOnboarding, shouldShowEInvoice, versionTrackingDone, migration19Context) {
        // Wait until every DataStore flag has emitted its real value —
        // guarding against the initial=null race that used to flip
        // showBackupDialog on a version upgrade before the onboarding flag
        // resolved. versionTrackingDone gates the fresh-install path so
        // shouldShowEInvoice is read after HAS_SEEN_EINVOICE_INTRO has been
        // flipped for fresh installs.
        if (!versionTrackingDone) return@LaunchedEffect
        // Migration19 wizard is the definitive "welcome to 1.9" experience.
        // Skip every other popup while it's pending — otherwise a 1.8.2 → 1.9
        // upgrader gets stacked with "Bienvenue 1.8", What's New and the
        // e-invoice intro on top of the wizard.
        if (migration19Context != null) return@LaunchedEffect
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

    // Session-expired modal — fired only when AuthRepository.forceLogout runs
    // (refresh-token dead, server revoke, replay detection). Voluntary logout goes
    // through the Account button and never emits here.
    var showSessionExpired by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        authRepository.sessionExpired.collect { showSessionExpired = true }
    }
    if (showSessionExpired) {
        // TODO(strings): move the FR literals below to composeResources/values/strings.xml
        // on the `translations` branch. Suggested keys:
        //   account_session_expired_title = "Session expirée"
        //   account_session_expired_body  = "Reconnectez-vous pour retrouver vos fonctions premium."
        //   account_session_expired_cta   = "D'accord"
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSessionExpired = false },
            title = { androidx.compose.material3.Text("Session expirée") },
            text = {
                androidx.compose.material3.Text(
                    "Reconnectez-vous pour retrouver vos fonctions premium.",
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showSessionExpired = false }) {
                    androidx.compose.material3.Text("D'accord")
                }
            },
        )
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
