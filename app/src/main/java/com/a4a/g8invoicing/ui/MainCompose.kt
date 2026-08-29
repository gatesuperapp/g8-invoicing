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
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.data.initializeVersionTracking
import com.a4a.g8invoicing.data.models.PersonType
import com.a4a.g8invoicing.data.setSeenOnboarding18
import com.a4a.g8invoicing.data.setSeenWhatsNew
import com.a4a.g8invoicing.data.shouldShowBackupPopupNow
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

    // Initialize locale and version tracking on first composition.
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
        // Pour les nouvelles installations, enregistre la version actuelle
        // Ainsi lors de la prochaine mise à jour, la modale WhatsNew s'affichera
        initializeVersionTracking(context)
        val lastIssuerId = clientOrIssuerDataSource.getLastCreatedIssuerId()
        if (lastIssuerId == null) {
            needsFirstLaunchIssuer = true
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
                    val updated = issuer.copy(
                        paymentIban = if (iban.isNotBlank()) TextFieldValue(iban) else issuer.paymentIban,
                        paymentBic = if (bic.isNotBlank()) TextFieldValue(bic) else issuer.paymentBic,
                    )
                    clientOrIssuerDataSource.updateClientOrIssuer(updated)
                },
                markSeen = { modulesRepo.markMigration19Seen() },
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
    var showWhatsNew by remember { mutableStateOf(false) }
    var showOnboarding by remember { mutableStateOf(false) }
    // Backup reminder: shown once when the user has >3 rows in any main table.
    // Suppressed while onboarding / what's new are pending to avoid stacking
    // modals at cold start.
    var showBackupDialog by remember { mutableStateOf(false) }
    var backupExportedFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(shouldShow, shouldShowOnboarding) {
        // Wait until BOTH DataStore flags have emitted their real value.
        // Without this guard, the very first composition fires the effect
        // with initial=null on both, which used to pass the "!shouldShow &&
        // !shouldShowOnboarding" check and briefly flip showBackupDialog on
        // — even during a version upgrade where the onboarding was actually
        // due. The onboarding then displayed a moment later, but the backup
        // dialog was already open behind it.
        val whatsNew = shouldShow ?: return@LaunchedEffect
        val onboarding = shouldShowOnboarding ?: return@LaunchedEffect
        showOnboarding = onboarding
        showWhatsNew = whatsNew && !onboarding
        if (!whatsNew && !onboarding) {
            showBackupDialog = shouldShowBackupPopupNow(
                context,
                invoiceQueries,
                deliveryNoteQueries,
                productQueries,
                clientOrIssuerQueries,
            )
        }
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
