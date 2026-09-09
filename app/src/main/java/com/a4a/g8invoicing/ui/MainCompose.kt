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
import app.cash.sqldelight.db.SqlDriver
import com.a4a.g8invoicing.data.RestoreManager
import org.koin.compose.koinInject
import g8invoicing.ClientOrIssuerQueries
import g8invoicing.DeliveryNoteQueries
import g8invoicing.InvoiceQueries
import g8invoicing.ProductQueries
import com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSourceInterface
import com.a4a.g8invoicing.data.CreditNoteLocalDataSourceInterface
import com.a4a.g8invoicing.data.CurrentCompanyRepository
import com.a4a.g8invoicing.data.DeliveryNoteLocalDataSourceInterface
import com.a4a.g8invoicing.data.InvoiceLocalDataSourceInterface
import com.a4a.g8invoicing.data.LocaleManager
import com.a4a.g8invoicing.data.PrefKeys
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.data.ProductTaxLocalDataSourceInterface
import com.a4a.g8invoicing.data.QuoteLocalDataSourceInterface
import com.a4a.g8invoicing.data.dataStore
import com.a4a.g8invoicing.data.initializeVersionTracking
import com.a4a.g8invoicing.data.models.CountryCodes
import com.a4a.g8invoicing.data.models.PersonType
import com.a4a.g8invoicing.data.resetOnboarding18Seen
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
import com.a4a.g8invoicing.ui.screens.DatabaseRestoreFlow
import com.a4a.g8invoicing.ui.screens.ExportPdfPlatform
import com.a4a.g8invoicing.ui.screens.ExportResult
import com.a4a.g8invoicing.ui.screens.exportDatabaseToDownloads
import com.a4a.g8invoicing.ui.screens.snapshotDatabaseInternally
import com.a4a.g8invoicing.ui.screens.sendDatabaseByEmail
import com.a4a.g8invoicing.ui.shared.FirstLaunchIssuerNameDialog
import com.a4a.g8invoicing.ui.shared.Migration19Actions
import com.a4a.g8invoicing.ui.shared.Migration19Context
import com.a4a.g8invoicing.ui.shared.OnboardingMigration19Dialog
import com.a4a.g8invoicing.ui.shared.OrphanRescueDialog
import com.a4a.g8invoicing.ui.shared.OrphanRescueItem
import com.a4a.g8invoicing.ui.shared.probeOrphanRescue
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
    val deliveryNoteDataSource: DeliveryNoteLocalDataSourceInterface = koinInject()
    val creditNoteDataSource: CreditNoteLocalDataSourceInterface = koinInject()
    val quoteDataSource: QuoteLocalDataSourceInterface = koinInject()
    val productDataSource: ProductLocalDataSourceInterface = koinInject()
    val productTaxDataSource: ProductTaxLocalDataSourceInterface = koinInject()
    val modulesRepo: ActivatedModulesRepository = koinInject()
    val sqlDriver: SqlDriver = koinInject()

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
    // Populated alongside migration19Context so the wizard knows whether to
    // show its ported client-country step. True when the user has never seen
    // the 1.8 wizard (pre-1.8 upgrade or restore); false when 1.8 already ran.
    var showClientCountryStepInMigration19 by remember { mutableStateOf(false) }
    // True when the wizard is firing because a restore just landed a backup
    // (not a plain version upgrade). Drives the per-issuer data check inside
    // the wizard instead of the version-based gate, since a restored backup
    // file has no reliable "source app version" metadata.
    var migration19FiredByRestore by remember { mutableStateOf(false) }
    // Orphan-rescue dialog: fires post-wizard (or at boot if the wizard was
    // already seen) when docs point at a deleted issuer — see
    // OrphanRescueDialog KDoc.
    var orphanRescueItems by remember { mutableStateOf<List<OrphanRescueItem>?>(null) }
    var orphanRescueIssuers by remember { mutableStateOf<List<ClientOrIssuerState>>(emptyList()) }

    suspend fun runOrphanProbe() {
        probeOrphanRescue(
            clientOrIssuerDataSource,
            invoiceDataSource,
            deliveryNoteDataSource,
            creditNoteDataSource,
            quoteDataSource,
        )?.let { payload ->
            orphanRescueIssuers = payload.candidates
            orphanRescueItems = payload.items
        }
    }

    // Restore flow — activated from Account > Sauvegarde. Rendered outside
    // NavGraph so its dialogs stack on top of every screen the user may be
    // on when they trigger the restore.
    var showRestoreFlow by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        localeManager.initializeLocale()

        // If a restore just landed a pre-current backup, clear the "wizard
        // seen" flag before we hit the migration-check block below. Without
        // this the restored issuers/clients/products stay unassigned to a
        // company (the 1.9 wizard would otherwise be silently skipped by
        // the stale flag from the previous session).
        if (RestoreManager.consumeMigrationWizardResetIfAny(context)) {
            modulesRepo.resetMigration19Seen()
            // Remember the restore trigger so the wizard swaps its version-
            // gated slides (country fix, ClientCountryPicker) for per-issuer
            // / per-client data checks. See the migration19FiredByRestore
            // wiring passed to OnboardingMigration19Dialog below.
            migration19FiredByRestore = true
        }
        // Pre-1.8 backup restored: consume the sentinel to clean the file up
        // but do NOT reset the "1.8 seen" pref anymore. The 1.8 wizard was
        // retired — its client-country step (and the auto-attach it enabled)
        // now live in the enhanced 1.9 wizard, which is re-fired by the
        // migration-wizard sentinel above.
        RestoreManager.consumeOnboarding18ResetIfAny(context)

        // Snapshot the "returning user" signal BEFORE initializeVersionTracking
        // runs — on a fresh install it seeds LAST_SEEN_VERSION itself, which
        // would then look identical to a real upgrade if we read it later.
        val bootPrefs = context.dataStore.data.first()
        val isReturningUser = bootPrefs[PrefKeys.LAST_SEEN_VERSION] != null
            || (bootPrefs[PrefKeys.HAS_SEEN_POPUP] ?: false)
        initializeVersionTracking(context)
        val lastIssuerId = clientOrIssuerDataSource.getLastCreatedIssuerId()
        if (lastIssuerId == null) {
            // A fully-empty DB (no issuers AND no clients AND no products)
            // routes to the fresh-install welcome — matches the user's rule:
            // "if the database is completely empty, show the first-install
            // onboarding, not the migration wizard". Applies to both real
            // fresh installs and returning users who cleared their data
            // (App Info → Clear storage, or a Restore that landed empty).
            val hasAnyClient = clientOrIssuerDataSource
                .fetchAll(PersonType.CLIENT).first().isNotEmpty()
            val hasAnyProduct = productDataSource.fetchAllProducts().first().isNotEmpty()
            val dbIsFullyEmpty = !hasAnyClient && !hasAnyProduct
            if (isReturningUser && !dbIsFullyEmpty) {
                // Existing user with orphan clients/products but a wiped
                // issuer table. Would only happen if migration 7.sqm's
                // "Mon entreprise" seed was skipped for some reason. Repair
                // silently — don't hit an upgrader with the fresh-install
                // welcome wizard, and don't drop their orphan data on the
                // floor by re-routing to first-install.
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
                // Best-effort silent snapshot before mutating anything. Lands
                // in filesDir/backups/ so it's not user-visible but recoverable
                // via ADB pull if something goes sideways downstream.
                snapshotDatabaseInternally(context, "before_1_9_safety_seed")
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
                    runOrphanProbe()
                } else {
                    // Unscoped fetches — the wizard MUST see every client and
                    // product regardless of which company the boot-time
                    // initIfMissing landed on. Was routing through the
                    // current-company-scoped Flow, which silently returned
                    // empty when migration 7's backfill (ORDER BY updated_at)
                    // and getLastInsertedIssuerId (ORDER BY id) picked
                    // different issuers on a pre-1.8 restore — the wizard
                    // then had nothing to attribute and the data landed
                    // randomly on the fallback issuer.
                    val clients = clientOrIssuerDataSource
                        .fetchAllUnscoped(PersonType.CLIENT)
                    val products = productDataSource.fetchAllProductsUnscoped()
                    val footersByIssuer = issuers.mapNotNull { issuer ->
                        val id = issuer.id?.toLong() ?: return@mapNotNull null
                        id to invoiceDataSource.getRecentFootersForCompany(id)
                    }.toMap()
                    // Silent snapshot before firing the wizard — user still gets
                    // the explicit "Sauvegarder" CTA in the Backup step to grab
                    // a copy in Downloads/, but this one guarantees a pre-wizard
                    // file exists in the app's internal dir even if they skip it.
                    snapshotDatabaseInternally(context, "before_1_9_migration")
                    // Show the ported ClientCountryPicker step only for users
                    // upgrading from a build strictly older than 1.8 —
                    // 1.8+ already asked the country question in its own
                    // wizard. Read LAST_SEEN_VERSION (set by What's New the
                    // last time the user saw the app on a previous release)
                    // rather than HAS_SEEN_ONBOARDING_1_8, which stays false
                    // if the user dismissed/skipped the 1.8 wizard and would
                    // then falsely re-ask the country. Fresh installs skip
                    // the migration wizard entirely a few lines above (empty
                    // issuers) so a null LAST_SEEN_VERSION here means a
                    // legacy pre-tracking install → treat as pre-1.8.
                    val lastSeen = bootPrefs[PrefKeys.LAST_SEEN_VERSION]
                    val cameFromPre18 = run {
                        if (lastSeen == null) return@run true
                        val parts = lastSeen.split(".")
                        val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
                        val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
                        major < 1 || (major == 1 && minor < 8)
                    }
                    migration19Context = Migration19Context(
                        issuers = issuers,
                        clients = clients,
                        products = products,
                        footersByIssuer = footersByIssuer,
                    )
                    showClientCountryStepInMigration19 = cameFromPre18
                    // 1.8 wizard is deprecated — its country-fill / attach flow
                    // now lives inside the enhanced 1.9 wizard (see the mono-
                    // issuer silent attach + client-country steps there). Mark
                    // 1.8 seen so its dialog doesn't gate the 1.9 dialog off
                    // the screen (see the onboarding18Pending guard below).
                    // A user upgrading from 1.8 already has this flag = true
                    // so this is a no-op for them.
                    setSeenOnboarding18(context)
                }
            } else {
                // Wizard already seen (upgrade from a buggy 1.9 → 1.9.1)
                // — no wizard to show but still probe for orphans left
                // behind by the previous cleanup step.
                runOrphanProbe()
            }
        }
        versionTrackingDone = true
    }

    // Note: [FirstLaunchIssuerNameDialog] used to be composed here; it now
    // renders after G8InvoicingTheme so it stacks on top of NavGraph. See
    // the block below the theme call.

    // What's New + Onboarding dialog state — declared here (before the 1.9
    // migration render) so the 1.9 dialog can gate itself on the 1.8 wizard
    // flag. Both wizards are surfaced by the same LaunchedEffect below;
    // ordering guarantees: 1.8 wizard fires first when both are pending, then
    // the 1.9 attribution wizard uses the country_code the 1.8 flow filled.
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

    // 1.8 wizard is retired — the 1.9 migration wizard now absorbs the
    // ClientCountryPicker step it used to own (see OnboardingMigration19Dialog).
    // A user upgrading from any pre-1.9 build lands straight on the 1.9 flow
    // regardless of whether they'd previously seen the 1.8 wizard. Kept as a
    // named constant (rather than inlined `false`) so search-and-read still
    // surfaces the historical gating logic.
    val onboarding18Pending = false
    if (!onboarding18Pending) migration19Context?.let { ctx ->
        OnboardingMigration19Dialog(
            context = ctx,
            actions = Migration19Actions(
                deleteIssuer = { issuer, reassignDocsTo ->
                    val fromId = issuer.id?.toLong()
                    if (fromId != null && reassignDocsTo != null) {
                        clientOrIssuerDataSource.reassignDocumentsToCompany(fromId, reassignDocsTo)
                    }
                    clientOrIssuerDataSource.deleteClientOrIssuer(issuer)
                },
                docsCountFor = { companyId ->
                    clientOrIssuerDataSource.countDocumentsForCompany(companyId)
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
                setClientCountry = { countryCode ->
                    clientOrIssuerDataSource.setCountryForClientsWithoutCountry(countryCode)
                },
                exportDatabase = {
                    try {
                        val file = exportDatabaseToDownloads(context, sqlDriver)
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
                    // Grandfather multi-entreprise users: they had >1 issuer
                    // before 1.9 (which is why the wizard fired with a Cleanup
                    // / Attach flow) so keep that UX turned on. Single-issuer
                    // users get the module off by default — they discover it
                    // in gStore if / when they need it.
                    if (ctx.issuers.size > 1) {
                        modulesRepo.forceActivate(ActivatedModulesRepository.MODULE_MULTI_ENTREPRISE)
                    }
                    // Pin the current-company pointer onto one of the user's
                    // real issuers (the first one). The pre-restore Settings
                    // value can outlive the DB swap and point at an id that
                    // no longer exists in the restored dataset, which shows
                    // up as the menu landing on the wrong (or seeded default
                    // "Mon entreprise") entreprise. Re-anchoring here after
                    // every wizard run — restore or plain upgrade — keeps
                    // the boot always positioned on a valid issuer.
                    ctx.issuers.firstOrNull()?.id?.toLong()?.let {
                        currentCompanyRepository.setCurrent(it)
                    }
                },
            ),
            showClientCountryStep = showClientCountryStepInMigration19,
            isRestore = migration19FiredByRestore,
            onDismiss = {
                migration19Context = null
                // Wizard just closed — probe for orphans left behind by
                // any earlier buggy cleanup pass.
                coroutineScope.launch { runOrphanProbe() }
            },
        )
    }

    orphanRescueItems?.takeIf { it.isNotEmpty() }?.let { items ->
        OrphanRescueDialog(
            initialItems = items,
            candidates = orphanRescueIssuers,
            onAssign = { item, companyId ->
                clientOrIssuerDataSource.assignDocToCompany(item.type, item.id, companyId)
            },
            onAllResolved = { orphanRescueItems = null },
        )
    }

    LaunchedEffect(shouldShow, shouldShowOnboarding, shouldShowEInvoice, versionTrackingDone, migration19Context) {
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
        // 1.8 onboarding is allowed to surface even while migration19 is
        // pending — the two run in sequence (1.8 → 1.9), gated by
        // `onboarding18Pending` at the render site. What's New / e-invoice /
        // backup nag stay suppressed until 1.9 clears so we don't stack four
        // modals on top of the migration wizard.
        val migration19Pending = migration19Context != null
        // 1.8 wizard retired — never surface it. If the DataStore flag still
        // reads "unseen" (upgrade from a pre-1.9 build, or a fresh install
        // that hasn't yet been marked), mark it seen so the boot check doesn't
        // resolve to true again next launch. The 1.9 migration wizard's own
        // markSeen block also does this, but it fires only after the user
        // completes 1.9 — this catches users who defer / dismiss 1.9.
        if (onboarding) setSeenOnboarding18(context)
        showOnboarding = false
        showWhatsNew = whatsNew && !migration19Pending
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
        if (eInvoice && !migration19Pending && systemCountry == "FR") {
            showEInvoiceIntro = true
            setSeenEInvoiceIntro(context)
        }
        if (!whatsNew && !migration19Pending && !showEInvoiceIntro) {
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
                            val file = exportDatabaseToDownloads(context, sqlDriver)
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
                    onRestoreDatabase = { showRestoreFlow = true },
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

    // Restore flow overlay — must sit outside NavGraph so its intermediate
    // "Terminer" AlertDialog survives any navigation the user triggers between
    // tapping "restaurer" and confirming. Killing the process from this
    // callback nukes any half-composed screen anyway.
    DatabaseRestoreFlow(
        active = showRestoreFlow,
        onDismiss = { showRestoreFlow = false },
    )

    // First-launch onboarding overlay. Rendered AFTER G8InvoicingTheme so it
    // stacks on top of NavGraph — the composable is no longer a Dialog (see
    // FirstLaunchIssuerNameDialog for the rationale), so composition order
    // now determines Z-order.
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
                // Replace the FR-flavoured 5.5/10/20 defaults baked into
                // TaxRate.sq with the shortlist for the picked country when
                // we have one on file. No-op if the user's country isn't in
                // the curated map or if the table has been touched before
                // (fresh-install path guarantees the latter is false).
                productTaxDataSource.seedDefaultsForCountryIfPristine(enteredCountry)
                // Fresh installs never see the 1.9 migration wizard.
                modulesRepo.markMigration19Seen()
                needsFirstLaunchIssuer = false
            }
        )
    }
}
