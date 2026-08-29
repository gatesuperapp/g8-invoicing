package com.a4a.g8invoicing

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.TextFieldValue
import androidx.navigation.compose.rememberNavController
import com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSourceInterface
import com.a4a.g8invoicing.data.CurrentCompanyRepository
import com.a4a.g8invoicing.data.InvoiceLocalDataSourceInterface
import com.a4a.g8invoicing.data.LocaleManager
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.data.auth.ActivatedModulesRepository
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.data.models.PersonType
import com.a4a.g8invoicing.ui.navigation.CategorySidebar
import com.a4a.g8invoicing.ui.navigation.NavGraph
import com.a4a.g8invoicing.ui.screens.ExportPdfPlatform
import com.a4a.g8invoicing.ui.shared.FirstLaunchIssuerNameDialog
import com.a4a.g8invoicing.ui.shared.Migration19Actions
import com.a4a.g8invoicing.ui.shared.Migration19Context
import com.a4a.g8invoicing.ui.shared.OnboardingMigration19Dialog
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.InvoiceState
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

/**
 * Shared App composable - entry point for iOS and potentially other platforms.
 * Uses the shared NavGraph for navigation.
 */
@Composable
fun App(
    // Platform-specific callbacks can be passed here
    onSendReminder: (InvoiceState) -> Unit = {},
    localeManager: LocaleManager = koinInject(),
    currentCompanyRepository: CurrentCompanyRepository = koinInject(),
    clientOrIssuerDataSource: ClientOrIssuerLocalDataSourceInterface = koinInject(),
    invoiceDataSource: InvoiceLocalDataSourceInterface = koinInject(),
    productDataSource: ProductLocalDataSourceInterface = koinInject(),
    modulesRepo: ActivatedModulesRepository = koinInject(),
) {
    // Boot-time initialization: locale + current-entreprise hydration.
    // If no issuer exists (fresh install), we surface a first-launch dialog
    // instead of silently seeding one. A null currentCompanyId cascades into
    // subtle bugs: doc-list flows fall back to unfiltered getAll(), clients
    // created from the invoice picker attach to company_id=NULL, master lists
    // that later filter by company see them twice.
    var needsFirstLaunchIssuer by remember { mutableStateOf(false) }
    // 1.9 migration wizard — surfaced only for existing installs (has at
    // least one issuer) that never went through the wizard before. Fresh
    // installs skip it entirely: we mark the flag as seen right after the
    // FirstLaunchIssuerNameDialog completes, so a brand-new user never
    // sees "welcome to 1.9 upgrade" copy that doesn't apply to them.
    var migration19Context by remember { mutableStateOf<Migration19Context?>(null) }
    LaunchedEffect(Unit) {
        localeManager.initializeLocale()
        val lastIssuerId = clientOrIssuerDataSource.getLastCreatedIssuerId()
        if (lastIssuerId == null) {
            needsFirstLaunchIssuer = true
        } else {
            currentCompanyRepository.initIfMissing { lastIssuerId }
            if (!modulesRepo.hasSeenMigration19()) {
                val issuers = clientOrIssuerDataSource.fetchAll(PersonType.ISSUER).first()
                if (issuers.isEmpty()) {
                    // No issuer to hang the wizard on — bail out silently
                    // and mark the flag so we don't retry every boot.
                    modulesRepo.markMigration19Seen()
                } else {
                    val clients = clientOrIssuerDataSource.fetchAll(PersonType.CLIENT).first()
                    val products = productDataSource.fetchAllProducts().first()
                    // Scan the last 10 invoice footers per issuer once so the
                    // wizard doesn't hit the DB again mid-flow.
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

    // Use Crossfade for smooth transition when language changes
    Crossfade(
        targetState = localeManager.currentLanguage,
        animationSpec = tween(300),
        label = "language_transition"
    ) { _ ->
        AppContent(onSendReminder = onSendReminder)
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
                // Fresh installs never see the 1.9 migration wizard — mark it
                // as done so we don't ambush them on their second boot.
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
}

@Composable
private fun AppContent(
    onSendReminder: (InvoiceState) -> Unit = {},
) {
    val navController = rememberNavController()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Desktop: show sidebar when screen is wide enough (>900dp)
        val isDesktopLayout = maxWidth > 900.dp

        if (isDesktopLayout) {
            Row(modifier = Modifier.fillMaxSize()) {
                CategorySidebar(
                    navController = navController,
                    onClickCategory = { category ->
                        // Navigate to category and set as start destination
                        val currentStartRoute = navController.graph.startDestinationRoute
                        if (currentStartRoute != null) {
                            navController.popBackStack(currentStartRoute, true)
                        }
                        navController.graph.setStartDestination(category.route)
                        navController.navigate(category.route)
                    }
                )
                NavGraph(
                    navController = navController,
                    onSendReminder = onSendReminder,
                    showCategoryButton = false, // Hide on desktop since we have sidebar
                    exportPdfContent = { document, onDismiss ->
                        ExportPdfPlatform(document, onDismiss)
                    }
                )
            }
        } else {
            NavGraph(
                navController = navController,
                onSendReminder = onSendReminder,
                showCategoryButton = true, // Show on mobile
                exportPdfContent = { document, onDismiss ->
                    ExportPdfPlatform(document, onDismiss)
                }
            )
        }
    }
}
