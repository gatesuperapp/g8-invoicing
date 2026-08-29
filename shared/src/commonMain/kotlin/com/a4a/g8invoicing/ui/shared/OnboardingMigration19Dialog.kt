package com.a4a.g8invoicing.ui.shared

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.a4a.g8invoicing.data.models.CountryCodes
import com.a4a.g8invoicing.data.stripTrailingZeros
import com.a4a.g8invoicing.facturx.extractBankInfoFromFooters
import com.a4a.g8invoicing.ui.screens.ExportResult
import com.a4a.g8invoicing.ui.screens.shared.CountryPicker
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.account_backup_dialog_message
import com.a4a.g8invoicing.shared.resources.account_backup_dialog_no
import com.a4a.g8invoicing.shared.resources.account_backup_dialog_title
import com.a4a.g8invoicing.shared.resources.account_backup_dialog_yes
import com.a4a.g8invoicing.shared.resources.ok
import com.a4a.g8invoicing.shared.resources.onboarding_19_attach_clients_body
import com.a4a.g8invoicing.shared.resources.onboarding_19_backup_body
import com.a4a.g8invoicing.shared.resources.onboarding_19_backup_title
import com.a4a.g8invoicing.shared.resources.onboarding_19_backup_cta
import com.a4a.g8invoicing.shared.resources.onboarding_19_confirm_body
import com.a4a.g8invoicing.shared.resources.onboarding_19_confirm_cta
import com.a4a.g8invoicing.shared.resources.onboarding_19_confirm_no_clients
import com.a4a.g8invoicing.shared.resources.onboarding_19_confirm_no_products
import com.a4a.g8invoicing.shared.resources.onboarding_19_confirm_section_clients
import com.a4a.g8invoicing.shared.resources.onboarding_19_confirm_section_products
import com.a4a.g8invoicing.shared.resources.onboarding_19_confirm_title
import com.a4a.g8invoicing.shared.resources.onboarding_next
import com.a4a.g8invoicing.shared.resources.whats_new_close
import com.a4a.g8invoicing.shared.resources.onboarding_19_attach_cta
import com.a4a.g8invoicing.shared.resources.onboarding_19_attach_intro_body
import com.a4a.g8invoicing.shared.resources.onboarding_19_attach_intro_cta
import com.a4a.g8invoicing.shared.resources.onboarding_19_attach_products_body
import com.a4a.g8invoicing.shared.resources.onboarding_19_attach_step_title
import com.a4a.g8invoicing.shared.resources.onboarding_19_bank_bic_label
import com.a4a.g8invoicing.shared.resources.onboarding_19_bank_body
import com.a4a.g8invoicing.shared.resources.onboarding_19_bank_cta
import com.a4a.g8invoicing.shared.resources.onboarding_19_bank_iban_label
import com.a4a.g8invoicing.shared.resources.onboarding_19_bank_not_found_hint
import com.a4a.g8invoicing.shared.resources.onboarding_19_bank_prefilled_hint
import com.a4a.g8invoicing.shared.resources.onboarding_19_bank_title
import com.a4a.g8invoicing.shared.resources.onboarding_19_bank_title_with_issuer
import com.a4a.g8invoicing.shared.resources.onboarding_19_cleanup_body
import com.a4a.g8invoicing.shared.resources.onboarding_19_cleanup_cta
import com.a4a.g8invoicing.shared.resources.onboarding_19_cleanup_title
import com.a4a.g8invoicing.shared.resources.onboarding_19_einvoice_body
import com.a4a.g8invoicing.shared.resources.onboarding_19_einvoice_cta
import com.a4a.g8invoicing.shared.resources.onboarding_19_einvoice_intro
import com.a4a.g8invoicing.shared.resources.onboarding_19_einvoice_sub
import com.a4a.g8invoicing.shared.resources.onboarding_19_einvoice_title
import com.a4a.g8invoicing.shared.resources.onboarding_19_final_title
import com.a4a.g8invoicing.shared.resources.onboarding_19_new_fields_body
import com.a4a.g8invoicing.shared.resources.onboarding_19_new_fields_cta
import com.a4a.g8invoicing.shared.resources.onboarding_19_new_fields_hint
import com.a4a.g8invoicing.shared.resources.onboarding_19_new_fields_title
import com.a4a.g8invoicing.shared.resources.onboarding_19_orphans_clients_body
import com.a4a.g8invoicing.shared.resources.onboarding_19_orphans_clients_title
import com.a4a.g8invoicing.shared.resources.onboarding_19_orphans_cta
import com.a4a.g8invoicing.shared.resources.onboarding_19_orphans_products_body
import com.a4a.g8invoicing.shared.resources.onboarding_19_orphans_products_title
import com.a4a.g8invoicing.shared.resources.onboarding_19_welcome_body
import com.a4a.g8invoicing.shared.resources.onboarding_19_welcome_cta
import com.a4a.g8invoicing.shared.resources.onboarding_19_welcome_title
import com.a4a.g8invoicing.shared.resources.onboarding_cleanup_delete_confirm_no
import com.a4a.g8invoicing.shared.resources.onboarding_cleanup_delete_confirm_title
import com.a4a.g8invoicing.shared.resources.onboarding_cleanup_delete_confirm_yes
import com.a4a.g8invoicing.shared.resources.onboarding_previous
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.textBody
import com.a4a.g8invoicing.ui.theme.textBodyBold
import com.a4a.g8invoicing.ui.theme.textBodySmall
import com.a4a.g8invoicing.ui.theme.textScreenTitle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * Everything the 1.9 migration wizard needs to run its flow, wrapped in
 * one data bag so [OnboardingMigration19Dialog] takes a single argument.
 * All lists are snapshots taken at boot — the wizard doesn't observe
 * subsequent DB writes.
 *
 * [footersByIssuer] maps master issuer id → the last N invoice footers
 * of that issuer, in most-recent-first order. Used to auto-detect an
 * IBAN/BIC to seed the bank-details slide.
 */
data class Migration19Context(
    val issuers: List<ClientOrIssuerState>,
    val clients: List<ClientOrIssuerState>,
    val products: List<ProductState>,
    val footersByIssuer: Map<Long, List<String>>,
)

/**
 * Callbacks the wizard uses to persist decisions. All are suspend so the
 * caller can flush to the DB before we jump to the next step.
 */
class Migration19Actions(
    val deleteIssuer: suspend (ClientOrIssuerState) -> Unit,
    val attachClients: suspend (clientIds: List<Long>, issuerId: Long) -> Unit,
    val attachProducts: suspend (productIds: List<Long>, issuerId: Long) -> Unit,
    val saveIssuerBank: suspend (issuer: ClientOrIssuerState, iban: String, bic: String) -> Unit,
    val updateIssuerName: suspend (issuer: ClientOrIssuerState, newName: String) -> Unit,
    val updateIssuerCountry: suspend (issuer: ClientOrIssuerState, countryCode: String) -> Unit,
    // Database export + email plumbing for the Backup step. Same pair the
    // 1.8 OnboardingDialog uses on its Privacy step — the wizard exports
    // locally then optionally offers to send the file by email, so the
    // user never leaves this non-dismissable modal.
    val exportDatabase: () -> ExportResult,
    val sendDatabaseByEmail: (filePath: String) -> Unit,
    val markSeen: suspend () -> Unit,
)

/**
 * 1.9 migration wizard — shown once when upgrading from 1.8.x. Two
 * branches share a common welcome/tail:
 *  - Single-issuer: Welcome → BankDetails → NewFieldsRecap → Final
 *  - Multi-issuer:  Welcome → Cleanup → AttachIntro → (per issuer:
 *                   AttachClients → AttachProducts → BankDetails) →
 *                   OrphansClients → OrphansProducts → NewFieldsRecap
 *                   → Final
 *
 * Non-dismissable: the wizard must be completed once. Any orphan-slide
 * is skipped when there's nothing to sort.
 */
@Composable
fun OnboardingMigration19Dialog(
    context: Migration19Context,
    actions: Migration19Actions,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    // --- Mutable state ------------------------------------------------------
    // Issuers are pared down as the user deletes duplicates in the cleanup
    // step. `remainingIssuers` is what the per-issuer loop iterates on.
    var remainingIssuers by remember(context) { mutableStateOf(context.issuers) }
    var currentIssuerIdx by remember { mutableStateOf(0) }

    // client id → issuer id and product id → issuer id, built up as the user
    // walks the per-issuer loop and the orphan slides.
    var clientAssignments by remember { mutableStateOf<Map<Long, Long>>(emptyMap()) }
    var productAssignments by remember { mutableStateOf<Map<Long, Long>>(emptyMap()) }

    // IBAN + BIC per issuer, seeded from footers on first entry.
    var bankByIssuer by remember { mutableStateOf<Map<Long, Pair<String, String>>>(emptyMap()) }

    var step by remember { mutableStateOf(Step19.Welcome) }
    var submitting by remember { mutableStateOf(false) }

    val isMulti: Boolean = remainingIssuers.size > 1

    // Bootstrap detection: post-migration 7.sqm seeds a "Mon entreprise" issuer
    // (no address) whenever the 1.8.x DB had none. Route those users through
    // two extra steps (name + country) before BankDetails, so the wizard
    // doubles as a first-time-setup flow instead of quietly locking in the
    // placeholder name.
    // Narrowed to the exact auto-seeded fingerprint: name literally
    // "Mon entreprise" (hardcoded in 7.sqm + MainCompose's safety net) AND no
    // address. A real user-created issuer with a name they picked never
    // matches — the user shouldn't be re-prompted for information they've
    // already filled in.
    // Frozen at wizard open — computing this off `remainingIssuers` would flip
    // to false as soon as the IssuerCountry step commits an address, breaking
    // the Back navigation from BankDetails.
    val needsIssuerBootstrap = remember(context.issuers) {
        context.issuers.size == 1 &&
            context.issuers.first().addresses.isNullOrEmpty() &&
            context.issuers.first().name.text == "Mon entreprise"
    }

    // Database backup flow state — driven from the Backup step's
    // "Sauvegarder ma base de données" CTA. Same pattern as
    // OnboardingDialog: export → optional email dialog. Kept inside the
    // wizard's own state so the non-dismissable modal never has to yield
    // the screen to a nav destination.
    var exportedFilePath by remember { mutableStateOf<String?>(null) }
    var showSendByEmailDialog by remember { mutableStateOf(false) }
    var exportErrorMessage by remember { mutableStateOf<String?>(null) }
    val onBackupClick: () -> Unit = {
        when (val result = actions.exportDatabase()) {
            is ExportResult.Success -> {
                exportedFilePath = result.filePath
                showSendByEmailDialog = true
            }
            is ExportResult.Error -> {
                exportErrorMessage = result.message
            }
        }
    }

    // --- Step transitions ---------------------------------------------------
    fun goForwardFromWelcome() {
        step = Step19.Backup
    }

    fun goForwardFromBackup() {
        step = when {
            needsIssuerBootstrap -> Step19.IssuerName
            isMulti -> Step19.Cleanup
            else -> Step19.BankDetails
        }
    }

    fun goForwardFromIssuerName() {
        step = Step19.IssuerCountry
    }

    fun goForwardFromIssuerCountry() {
        step = Step19.BankDetails
    }

    fun goForwardFromCleanup() {
        step = if (isMulti) Step19.AttachIntro else Step19.BankDetails
    }

    fun goForwardFromAttachIntro() {
        currentIssuerIdx = 0
        step = Step19.AttachClients
    }

    fun goForwardFromAttachClients() {
        step = Step19.AttachProducts
    }

    fun goForwardFromAttachProducts() {
        step = Step19.BankDetails
    }

    fun goForwardFromBankDetails() {
        if (isMulti) {
            if (currentIssuerIdx + 1 < remainingIssuers.size) {
                currentIssuerIdx += 1
                step = Step19.AttachClients
            } else {
                // Per-issuer loop is done; check orphans. Anything with no
                // orphans still routes to Confirm — user gets one final
                // review-and-X-remove pass before commit.
                val orphanClients = context.clients.filter { it.id?.toLong() !in clientAssignments.keys }
                step = if (orphanClients.isNotEmpty()) Step19.OrphansClients else Step19.OrphansProducts
                val orphanProducts = context.products.filter { it.id?.toLong() !in productAssignments.keys }
                if (step == Step19.OrphansProducts && orphanProducts.isEmpty()) {
                    step = Step19.Confirm
                }
            }
        } else {
            step = Step19.NewFieldsRecap
        }
    }

    fun goForwardFromOrphansClients() {
        val orphanProducts = context.products.filter { it.id?.toLong() !in productAssignments.keys }
        step = if (orphanProducts.isNotEmpty()) Step19.OrphansProducts else Step19.Confirm
    }

    fun goForwardFromOrphansProducts() {
        step = Step19.Confirm
    }

    fun goForwardFromConfirm() {
        step = Step19.NewFieldsRecap
    }

    fun goForwardFromNewFieldsRecap() {
        step = Step19.EInvoice
    }

    fun goForwardFromEInvoice() {
        step = Step19.Final
    }

    // Commit fires only on tap of the Final CTA. Writes are grouped so a
    // process-death mid-flow leaves the DB in a consistent state (all-or-
    // nothing per DB write — no cross-table transaction because these
    // touch multiple .sq files).
    val commit = commit@ {
        if (submitting) return@commit
        submitting = true
        scope.launch {
            // Client attachments
            clientAssignments.entries.groupBy { it.value }.forEach { (issuerId, entries) ->
                actions.attachClients(entries.map { it.key }, issuerId)
            }
            // Product attachments
            productAssignments.entries.groupBy { it.value }.forEach { (issuerId, entries) ->
                actions.attachProducts(entries.map { it.key }, issuerId)
            }
            // Bank details per issuer
            bankByIssuer.forEach { (issuerId, ibanBic) ->
                val issuer = remainingIssuers.firstOrNull { it.id?.toLong() == issuerId } ?: return@forEach
                actions.saveIssuerBank(issuer, ibanBic.first, ibanBic.second)
            }
            actions.markSeen()
            onDismiss()
        }
    }

    // --- Rendering ----------------------------------------------------------
    val previous = { s: Step19 ->
        step = previousStep19(s, isMulti, needsIssuerBootstrap, currentIssuerIdx) { newIdx -> currentIssuerIdx = newIdx }
    }

    Dialog(
        onDismissRequest = { /* non-dismissable */ },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.surface),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopBar19(
                    // Confirm step is the "point of no return" review — going
                    // back would put the user right where they came from without
                    // a way to fix anything the current step doesn't already
                    // offer. Keep it forward-only.
                    canGoBack = step != Step19.Welcome &&
                        step != Step19.Confirm &&
                        step != Step19.Final,
                    onBack = { previous(step) },
                )
                // Skip verticalScroll on the Final step. The confetti Canvas
                // uses fillMaxSize(), and inside a scrollable parent (infinite
                // height constraint) it resolves to a degenerate size — the
                // particles collapse into a narrow horizontal band instead of
                // filling the screen.
                val contentModifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .let { if (step != Step19.Final) it.verticalScroll(rememberScrollState()) else it }
                Box(
                    modifier = contentModifier,
                    contentAlignment = Alignment.Center,
                ) {
                    when (step) {
                        Step19.Welcome -> WelcomeStep19(onNext = { goForwardFromWelcome() })
                        Step19.Backup -> BackupStep19(
                            onBackup = onBackupClick,
                            onNext = { goForwardFromBackup() },
                        )
                        Step19.IssuerName -> {
                            val issuer = remainingIssuers.firstOrNull() ?: run {
                                step = Step19.BankDetails
                                return@Box
                            }
                            IssuerNameStep19(
                                initial = issuer.name.text.takeIf {
                                    // Placeholder from migration seed; blank the field so the
                                    // user types their real entreprise name from scratch.
                                    it != "Mon entreprise"
                                }.orEmpty(),
                                onSubmit = { entered ->
                                    scope.launch {
                                        actions.updateIssuerName(issuer, entered)
                                        // Mutate local list so downstream steps see the new name
                                        // without waiting for a re-fetch.
                                        remainingIssuers = remainingIssuers.map { i ->
                                            if (i.id == issuer.id) i.copy(name = TextFieldValue(entered.trim())) else i
                                        }
                                        goForwardFromIssuerName()
                                    }
                                },
                            )
                        }
                        Step19.IssuerCountry -> {
                            val issuer = remainingIssuers.firstOrNull() ?: run {
                                step = Step19.BankDetails
                                return@Box
                            }
                            IssuerCountryStep19(
                                initial = issuer.addresses?.firstOrNull()?.countryCode
                                    ?: CountryCodes.pickDefaultForNewAddress(null),
                                onSubmit = { picked ->
                                    scope.launch {
                                        actions.updateIssuerCountry(issuer, picked)
                                        remainingIssuers = remainingIssuers.map { i ->
                                            if (i.id == issuer.id) {
                                                val existing = i.addresses?.firstOrNull()
                                                val updated = existing?.copy(countryCode = picked)
                                                    ?: com.a4a.g8invoicing.ui.states.AddressState(countryCode = picked)
                                                val rest = i.addresses?.drop(1).orEmpty()
                                                i.copy(addresses = listOf(updated) + rest)
                                            } else i
                                        }
                                        goForwardFromIssuerCountry()
                                    }
                                },
                            )
                        }
                        Step19.Cleanup -> CleanupStep19(
                            issuers = remainingIssuers,
                            onDelete = { issuer ->
                                scope.launch {
                                    actions.deleteIssuer(issuer)
                                    remainingIssuers = remainingIssuers.filter { it.id != issuer.id }
                                }
                            },
                            onNext = { goForwardFromCleanup() },
                        )
                        Step19.AttachIntro -> AttachIntroStep19(onNext = { goForwardFromAttachIntro() })
                        Step19.AttachClients -> {
                            val issuer = remainingIssuers.getOrNull(currentIssuerIdx) ?: run {
                                goForwardFromBankDetails()
                                return@Box
                            }
                            val available = context.clients.filter { it.id?.toLong() !in clientAssignments.keys }
                            var previewClient by remember(currentIssuerIdx) { mutableStateOf<ClientOrIssuerState?>(null) }
                            AttachStep19(
                                issuer = issuer,
                                issuerIdx = currentIssuerIdx + 1,
                                bodyText = stringResource(Res.string.onboarding_19_attach_clients_body),
                                items = available.map { AttachItem(it.id!!.toLong(), it.name.text, it.emails?.firstOrNull()?.email?.text.orEmpty()) },
                                onDetailsClick = { id ->
                                    previewClient = available.firstOrNull { it.id?.toLong() == id }
                                },
                                onConfirm = { ids ->
                                    clientAssignments = clientAssignments + ids.associateWith { issuer.id!!.toLong() }
                                    goForwardFromAttachClients()
                                },
                            )
                            previewClient?.let { c ->
                                ClientDetailsDialog19(client = c, onDismiss = { previewClient = null })
                            }
                        }
                        Step19.AttachProducts -> {
                            val issuer = remainingIssuers.getOrNull(currentIssuerIdx) ?: run {
                                goForwardFromBankDetails()
                                return@Box
                            }
                            val available = context.products.filter { it.id?.toLong() !in productAssignments.keys }
                            var previewProduct by remember(currentIssuerIdx) { mutableStateOf<ProductState?>(null) }
                            AttachStep19(
                                issuer = issuer,
                                issuerIdx = currentIssuerIdx + 1,
                                bodyText = stringResource(Res.string.onboarding_19_attach_products_body),
                                // Secondary shows the description (was the default price) — user
                                // needs to disambiguate products that share a name but not
                                // their description. Full details live in the info dialog.
                                items = available.map {
                                    AttachItem(
                                        it.id!!.toLong(),
                                        it.name.text,
                                        it.description?.text.orEmpty(),
                                    )
                                },
                                onDetailsClick = { id ->
                                    previewProduct = available.firstOrNull { it.id?.toLong() == id }
                                },
                                onConfirm = { ids ->
                                    productAssignments = productAssignments + ids.associateWith { issuer.id!!.toLong() }
                                    goForwardFromAttachProducts()
                                },
                            )
                            previewProduct?.let { p ->
                                ProductDetailsDialog19(product = p, onDismiss = { previewProduct = null })
                            }
                        }
                        Step19.BankDetails -> {
                            val issuer = if (isMulti) remainingIssuers.getOrNull(currentIssuerIdx) else remainingIssuers.firstOrNull()
                            if (issuer == null) {
                                step = Step19.NewFieldsRecap
                                return@Box
                            }
                            val id = issuer.id!!.toLong()
                            val footers = context.footersByIssuer[id].orEmpty()
                            val detected = remember(footers) { extractBankInfoFromFooters(footers) }
                            val current = bankByIssuer[id]
                            val ibanInit = current?.first
                                ?: issuer.paymentIban?.text?.trim()?.ifEmpty { null }
                                ?: detected.iban.orEmpty()
                            val bicInit = current?.second
                                ?: issuer.paymentBic?.text?.trim()?.ifEmpty { null }
                                ?: detected.bic.orEmpty()
                            BankDetailsStep19(
                                issuer = issuer,
                                issuerIdx = if (isMulti) currentIssuerIdx + 1 else null,
                                initialIban = ibanInit,
                                initialBic = bicInit,
                                prefilled = detected.iban != null,
                                onNext = { ibanValue, bicValue ->
                                    bankByIssuer = bankByIssuer + (id to (ibanValue to bicValue))
                                    goForwardFromBankDetails()
                                },
                            )
                        }
                        Step19.OrphansClients -> {
                            val orphans = context.clients.filter { it.id?.toLong() !in clientAssignments.keys }
                            OrphansStep19(
                                title = stringResource(Res.string.onboarding_19_orphans_clients_title),
                                body = stringResource(Res.string.onboarding_19_orphans_clients_body),
                                items = orphans.map { OrphanItem(it.id!!.toLong(), it.name.text, it.emails?.firstOrNull()?.email?.text.orEmpty()) },
                                issuers = remainingIssuers,
                                onAssign = { itemId, issuerId ->
                                    clientAssignments = clientAssignments + (itemId to issuerId)
                                },
                                assignments = clientAssignments,
                                onNext = { goForwardFromOrphansClients() },
                            )
                        }
                        Step19.OrphansProducts -> {
                            val orphans = context.products.filter { it.id?.toLong() !in productAssignments.keys }
                            OrphansStep19(
                                title = stringResource(Res.string.onboarding_19_orphans_products_title),
                                body = stringResource(Res.string.onboarding_19_orphans_products_body),
                                // Match the AttachStep19 products list — secondary = description
                                // (was default price) so the same disambiguation applies here too.
                                items = orphans.map { OrphanItem(it.id!!.toLong(), it.name.text, it.description?.text.orEmpty()) },
                                issuers = remainingIssuers,
                                onAssign = { itemId, issuerId ->
                                    productAssignments = productAssignments + (itemId to issuerId)
                                },
                                assignments = productAssignments,
                                onNext = { goForwardFromOrphansProducts() },
                            )
                        }
                        Step19.Confirm -> ConfirmStep19(
                            issuers = remainingIssuers,
                            clientsById = remember(context.clients) {
                                context.clients.mapNotNull { c -> c.id?.toLong()?.let { it to c } }.toMap()
                            },
                            productsById = remember(context.products) {
                                context.products.mapNotNull { p -> p.id?.toLong()?.let { it to p } }.toMap()
                            },
                            clientAssignments = clientAssignments,
                            productAssignments = productAssignments,
                            onRemoveClient = { clientId ->
                                clientAssignments = clientAssignments - clientId
                            },
                            onRemoveProduct = { productId ->
                                productAssignments = productAssignments - productId
                            },
                            onNext = { goForwardFromConfirm() },
                        )
                        Step19.NewFieldsRecap -> NewFieldsRecapStep19(onNext = { goForwardFromNewFieldsRecap() })
                        Step19.EInvoice -> EInvoiceStep19(onNext = { goForwardFromEInvoice() })
                        Step19.Final -> FinalStep19(onDone = commit)
                    }
                }
            }
        }
    }

    // Post-export dialogs. Mirrors OnboardingDialog's success/email-offer +
    // error handling so a backup failure surfaces instead of failing silently.
    if (showSendByEmailDialog && exportedFilePath != null) {
        AlertDialog(
            onDismissRequest = { showSendByEmailDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                )
            },
            title = { Text(stringResource(Res.string.account_backup_dialog_title)) },
            text = { Text(stringResource(Res.string.account_backup_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showSendByEmailDialog = false
                    exportedFilePath?.let { actions.sendDatabaseByEmail(it) }
                }) {
                    Text(
                        stringResource(Res.string.account_backup_dialog_yes),
                        color = AppColors.textLink,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSendByEmailDialog = false }) {
                    Text(
                        stringResource(Res.string.account_backup_dialog_no),
                        color = AppColors.textLink,
                    )
                }
            },
        )
    }

    exportErrorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { exportErrorMessage = null },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { exportErrorMessage = null }) {
                    Text(stringResource(Res.string.ok), color = AppColors.textLink)
                }
            },
        )
    }
}

// ============================================================================
// Step enum + navigation
// ============================================================================

private enum class Step19 {
    Welcome,
    Backup,
    IssuerName,
    IssuerCountry,
    Cleanup,
    AttachIntro,
    AttachClients,
    AttachProducts,
    BankDetails,
    OrphansClients,
    OrphansProducts,
    Confirm,
    NewFieldsRecap,
    EInvoice,
    Final,
}

private fun previousStep19(
    step: Step19,
    isMulti: Boolean,
    needsIssuerBootstrap: Boolean,
    currentIssuerIdx: Int,
    setCurrentIssuerIdx: (Int) -> Unit,
): Step19 = when (step) {
    Step19.Welcome -> Step19.Welcome
    Step19.Backup -> Step19.Welcome
    Step19.IssuerName -> Step19.Backup
    Step19.IssuerCountry -> Step19.IssuerName
    Step19.Cleanup -> Step19.Backup
    Step19.AttachIntro -> Step19.Cleanup
    Step19.AttachClients -> {
        if (currentIssuerIdx > 0) {
            // Previous issuer's bank details slide
            setCurrentIssuerIdx(currentIssuerIdx - 1)
            Step19.BankDetails
        } else {
            Step19.AttachIntro
        }
    }
    Step19.AttachProducts -> Step19.AttachClients
    Step19.BankDetails -> when {
        isMulti -> Step19.AttachProducts
        needsIssuerBootstrap -> Step19.IssuerCountry
        else -> Step19.Backup
    }
    Step19.OrphansClients -> Step19.BankDetails
    Step19.OrphansProducts -> Step19.OrphansClients
    // Confirm has no back button (canGoBack excludes it) — the X-remove
    // controls inside the step are the escape hatch for wrong assignments.
    Step19.Confirm -> Step19.Confirm
    Step19.NewFieldsRecap -> if (isMulti) Step19.Confirm else Step19.BankDetails
    Step19.EInvoice -> Step19.NewFieldsRecap
    Step19.Final -> Step19.EInvoice
}

// ============================================================================
// Steps
// ============================================================================

@Composable
private fun BackupStep19(
    onBackup: () -> Unit,
    onNext: () -> Unit,
) {
    // Shield icon (not EmojiSlot) reuses the 1.8 OnboardingDialog PrivacyStep
    // visual anchor — the "reassurance / take a moment" beat should read the
    // same in both wizards. Primary CTA = backup (filled), secondary = skip
    // (outlined) so the safer path is the more prominent one.
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MascotSlot {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                tint = AppColors.accent,
                modifier = Modifier.size(44.dp),
            )
        }
        Spacer(Modifier.height(24.dp))
        StepTitle(stringResource(Res.string.onboarding_19_backup_title))
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(Res.string.onboarding_19_backup_body),
            style = MaterialTheme.typography.textBody,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onBackup,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.buttonActive,
                contentColor = AppColors.textOnAccent,
            ),
        ) { Text(stringResource(Res.string.onboarding_19_backup_cta)) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.textLink),
        ) { Text(stringResource(Res.string.onboarding_next)) }
    }
}

@Composable
private fun WelcomeStep19(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MascotSlot { AnimatedKaomojiThanks(fontSize = 22.sp, static = true) }
        Spacer(Modifier.height(24.dp))
        StepTitle(stringResource(Res.string.onboarding_19_welcome_title))
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(Res.string.onboarding_19_welcome_body),
            style = MaterialTheme.typography.textBody,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(40.dp))
        PrimaryCta19(text = stringResource(Res.string.onboarding_19_welcome_cta), onClick = onNext)
    }
}

@Composable
private fun CleanupStep19(
    issuers: List<ClientOrIssuerState>,
    onDelete: (ClientOrIssuerState) -> Unit,
    onNext: () -> Unit,
) {
    var pendingDelete by remember { mutableStateOf<ClientOrIssuerState?>(null) }
    var pendingDetails by remember { mutableStateOf<ClientOrIssuerState?>(null) }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmojiSlot("🏢")
        Spacer(Modifier.height(24.dp))
        StepTitle(stringResource(Res.string.onboarding_19_cleanup_title, issuers.size))
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(Res.string.onboarding_19_cleanup_body),
            style = MaterialTheme.typography.textBody,
            textAlign = TextAlign.Start,
            lineHeight = 24.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(20.dp))
        issuers.forEach { issuer ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F2F8))
                    // Tap anywhere on the row (outside the delete icon) opens a
                    // read-only details modal — mirrors the 1.8 OnboardingDialog
                    // IssuerCleanupStep so users have a way to disambiguate
                    // similarly-named issuers before choosing which to delete.
                    .clickable { pendingDetails = issuer }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = issuer.name.text.ifBlank { "—" },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.textBody,
                )
                IconButton(onClick = { pendingDelete = issuer }) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        tint = AppColors.iconSecondary,
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
        }
        Spacer(Modifier.height(24.dp))
        PrimaryCta19(text = stringResource(Res.string.onboarding_19_cleanup_cta), onClick = onNext)
    }
    pendingDetails?.let { issuer ->
        IssuerDetailsDialog19(issuer = issuer, onDismiss = { pendingDetails = null })
    }
    pendingDelete?.let { toDelete ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(Res.string.onboarding_cleanup_delete_confirm_title)) },
            confirmButton = {
                Button(onClick = { onDelete(toDelete); pendingDelete = null }) {
                    Text(stringResource(Res.string.onboarding_cleanup_delete_confirm_yes))
                }
            },
            dismissButton = {
                Button(onClick = { pendingDelete = null }) {
                    Text(stringResource(Res.string.onboarding_cleanup_delete_confirm_no))
                }
            },
        )
    }
}

@Composable
private fun AttachIntroStep19(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmojiSlot("🔗")
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(Res.string.onboarding_19_attach_intro_body),
            style = MaterialTheme.typography.textBody,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(40.dp))
        PrimaryCta19(text = stringResource(Res.string.onboarding_19_attach_intro_cta), onClick = onNext)
    }
}

private data class AttachItem(val id: Long, val primary: String, val secondary: String)

@Composable
private fun AttachStep19(
    issuer: ClientOrIssuerState,
    issuerIdx: Int,
    bodyText: String,
    items: List<AttachItem>,
    // Tapping the info icon opens a per-item details dialog. Rendered by the
    // caller since the payload type differs (ClientOrIssuerState vs ProductState)
    // and the dialog composables are shape-specific.
    onDetailsClick: (Long) -> Unit,
    onConfirm: (List<Long>) -> Unit,
) {
    var selectedIds by remember(issuer.id, items.size) { mutableStateOf<Set<Long>>(emptySet()) }
    val issuerName = issuer.name.text.ifBlank { "—" }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmojiSlot("🔗")
        Spacer(Modifier.height(24.dp))
        StepTitle(stringResource(Res.string.onboarding_19_attach_step_title, issuerIdx, issuerName))
        Spacer(Modifier.height(20.dp))
        Text(
            text = bodyText,
            style = MaterialTheme.typography.textBody,
            textAlign = TextAlign.Start,
            lineHeight = 24.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        if (items.isEmpty()) {
            Text(
                text = "—",
                style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary),
                modifier = Modifier.padding(vertical = 24.dp),
            )
        } else {
            items.forEach { item ->
                val checked = item.id in selectedIds
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (checked) Color(0xFFEDE7F6) else Color(0xFFF5F2F8))
                        .clickable {
                            selectedIds = if (checked) selectedIds - item.id else selectedIds + item.id
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (checked) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (checked) AppColors.accent else AppColors.iconSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.primary.ifBlank { "—" },
                            style = MaterialTheme.typography.textBody,
                        )
                        if (item.secondary.isNotBlank()) {
                            Text(
                                text = item.secondary,
                                style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary),
                                maxLines = 2,
                            )
                        }
                    }
                    IconButton(onClick = { onDetailsClick(item.id) }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = AppColors.iconSecondary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
        PrimaryCta19(
            text = stringResource(Res.string.onboarding_19_attach_cta, issuerName),
            onClick = { onConfirm(selectedIds.toList()) },
        )
    }
}

@Composable
private fun BankDetailsStep19(
    issuer: ClientOrIssuerState,
    issuerIdx: Int?,
    initialIban: String,
    initialBic: String,
    prefilled: Boolean,
    onNext: (String, String) -> Unit,
) {
    var iban by remember(issuer.id) {
        mutableStateOf(TextFieldValue(initialIban, TextRange(initialIban.length)))
    }
    var bic by remember(issuer.id) {
        mutableStateOf(TextFieldValue(initialBic, TextRange(initialBic.length)))
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        EmojiSlot("🏦")
        Spacer(Modifier.height(24.dp))
        val title = if (issuerIdx != null) {
            stringResource(
                Res.string.onboarding_19_bank_title_with_issuer,
                issuerIdx,
                issuer.name.text.ifBlank { "—" },
            )
        } else {
            stringResource(Res.string.onboarding_19_bank_title)
        }
        StepTitle(title)
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(Res.string.onboarding_19_bank_body),
            style = MaterialTheme.typography.textBody,
            textAlign = TextAlign.Start,
            lineHeight = 24.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        // Hint moved above the fields (was at the bottom of the step) so the
        // "I found it in your footer" / "I couldn't find it" context lands
        // before the user starts scanning the pre-filled inputs.
        Text(
            text = if (prefilled)
                stringResource(Res.string.onboarding_19_bank_prefilled_hint)
            else
                stringResource(Res.string.onboarding_19_bank_not_found_hint),
            style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary),
            lineHeight = 20.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(24.dp))
        FieldLabel19(stringResource(Res.string.onboarding_19_bank_iban_label))
        Spacer(Modifier.height(6.dp))
        CompactTextField19(
            value = iban,
            onValueChange = { iban = it },
            placeholder = "FR76 …",
            imeAction = ImeAction.Next,
        )
        Spacer(Modifier.height(16.dp))
        FieldLabel19(stringResource(Res.string.onboarding_19_bank_bic_label))
        Spacer(Modifier.height(6.dp))
        CompactTextField19(
            value = bic,
            onValueChange = { bic = it },
            placeholder = "BNPAFRPP",
            imeAction = ImeAction.Done,
        )
        Spacer(Modifier.height(32.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            PrimaryCta19(
                text = stringResource(Res.string.onboarding_19_bank_cta),
                onClick = { onNext(iban.text.trim(), bic.text.trim()) },
            )
        }
    }
}

private data class OrphanItem(val id: Long, val primary: String, val secondary: String)

@Composable
private fun OrphansStep19(
    title: String,
    body: String,
    items: List<OrphanItem>,
    issuers: List<ClientOrIssuerState>,
    assignments: Map<Long, Long>,
    onAssign: (itemId: Long, issuerId: Long) -> Unit,
    onNext: () -> Unit,
) {
    val allAssigned = items.all { it.id in assignments.keys }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmojiSlot("🔍")
        Spacer(Modifier.height(24.dp))
        StepTitle(title)
        Spacer(Modifier.height(20.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.textBody,
            textAlign = TextAlign.Start,
            lineHeight = 24.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        items.forEach { item ->
            val currentAssignment = assignments[item.id]
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F2F8))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Text(
                    text = item.primary.ifBlank { "—" },
                    style = MaterialTheme.typography.textBody,
                )
                if (item.secondary.isNotBlank()) {
                    Text(
                        text = item.secondary,
                        style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary),
                    )
                }
                Spacer(Modifier.height(8.dp))
                // Full-text chips per issuer — no truncation so users with
                // similarly-named entreprises can still tell them apart.
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    issuers.forEach { issuer ->
                        val id = issuer.id?.toLong() ?: return@forEach
                        val selected = currentAssignment == id
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    if (selected) AppColors.accent
                                    else Color.White,
                                )
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (selected) AppColors.accent else Color(0xFFE4DEED),
                                    ),
                                    RoundedCornerShape(999.dp),
                                )
                                .clickable { onAssign(item.id, id) }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = issuer.name.text.ifBlank { "—" },
                                style = MaterialTheme.typography.textBodySmall.copy(
                                    color = if (selected) AppColors.textOnAccent else AppColors.textPrimary,
                                ),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(24.dp))
        PrimaryCta19(
            text = stringResource(Res.string.onboarding_19_orphans_cta),
            enabled = allAssigned,
            onClick = onNext,
        )
    }
}

@Composable
private fun NewFieldsRecapStep19(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmojiSlot("✨")
        Spacer(Modifier.height(24.dp))
        StepTitle(stringResource(Res.string.onboarding_19_new_fields_title))
        Spacer(Modifier.height(24.dp))
        Text(
            text = boldMarkdown(stringResource(Res.string.onboarding_19_new_fields_body)),
            style = MaterialTheme.typography.textBody,
            textAlign = TextAlign.Start,
            lineHeight = 24.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(20.dp))
        // "Si tu avais mis ces informations…" — demoted to a hint so the
        // three bolded field labels above stay the focal point.
        Text(
            text = stringResource(Res.string.onboarding_19_new_fields_hint),
            style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary),
            textAlign = TextAlign.Start,
            lineHeight = 22.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(32.dp))
        PrimaryCta19(text = stringResource(Res.string.onboarding_19_new_fields_cta), onClick = onNext)
    }
}

@Composable
private fun EInvoiceStep19(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmojiSlot("🧞")
        Spacer(Modifier.height(24.dp))
        StepTitle(stringResource(Res.string.onboarding_19_einvoice_title))
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(Res.string.onboarding_19_einvoice_intro),
            style = MaterialTheme.typography.textBody,
            textAlign = TextAlign.Start,
            lineHeight = 24.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.onboarding_19_einvoice_body),
            style = MaterialTheme.typography.textBody,
            textAlign = TextAlign.Start,
            lineHeight = 24.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(Res.string.onboarding_19_einvoice_sub),
            style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary),
            textAlign = TextAlign.Start,
            lineHeight = 22.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(32.dp))
        PrimaryCta19(
            text = stringResource(Res.string.onboarding_19_einvoice_cta),
            onClick = onNext,
        )
    }
}

@Composable
private fun FinalStep19(onDone: () -> Unit) {
    // Auto-dismiss celebration: kaomoji + confetti + title. 5 s hold, then a
    // 700 ms fade. Tapping anywhere on the slide fast-forwards to the fade —
    // mirrors FirstLaunchIssuerNameDialog.CompletionStep so the two "you're
    // done" moments feel identical whether the user came in via fresh install
    // or upgrade.
    var fadingOut by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (fadingOut) 0f else 1f,
        animationSpec = tween(durationMillis = 700),
        label = "migration19FinalFade",
    )
    // Split into two effects so a tap-to-skip triggers onDone right after the
    // fade — the previous single-effect version stayed blocked on delay(5000)
    // even after the user tapped, leaving a ~4 s blank screen post-fade.
    LaunchedEffect(Unit) {
        delay(5000L)
        if (!fadingOut) fadingOut = true
    }
    LaunchedEffect(fadingOut) {
        if (fadingOut) {
            delay(700L)
            onDone()
        }
    }
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) { if (!fadingOut) fadingOut = true },
    ) {
        ConfettiBurst(modifier = Modifier.fillMaxSize(), durationMs = 5000)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            MascotSlot { AnimatedKaomojiThanks(fontSize = 22.sp, loop = true) }
            Spacer(Modifier.height(24.dp))
            StepTitle(stringResource(Res.string.onboarding_19_final_title))
        }
    }
}

// Render a plain string with **bold** markers as an AnnotatedString — a tiny
// subset of Markdown that lets a stringResource carry lightweight emphasis
// without pulling in a full HTML/markdown parser. Odd-index parts (between
// paired `**`) get FontWeight.Bold, even-index parts stay unstyled.
private fun boldMarkdown(text: String): AnnotatedString = buildAnnotatedString {
    val parts = text.split("**")
    parts.forEachIndexed { index, part ->
        if (index % 2 == 1) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(part) }
        } else {
            append(part)
        }
    }
}

// ============================================================================
// Read-only details dialogs + review/confirm step
// ============================================================================

@Composable
private fun IssuerDetailsDialog19(
    issuer: ClientOrIssuerState,
    onDismiss: () -> Unit,
) {
    DetailsDialogShell(onDismiss = onDismiss, title = issuer.name.text.ifBlank { "—" }) {
        issuer.firstName?.text?.takeIf { it.isNotBlank() }?.let {
            Text(it, style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary))
        }
        Spacer(Modifier.height(12.dp))
        issuer.addresses.orEmpty().forEach { addr ->
            val lines = listOfNotNull(
                addr.addressLine1?.text?.takeIf { it.isNotBlank() },
                addr.addressLine2?.text?.takeIf { it.isNotBlank() },
                listOfNotNull(
                    addr.zipCode?.text?.takeIf { it.isNotBlank() },
                    addr.city?.text?.takeIf { it.isNotBlank() },
                ).joinToString(" ").takeIf { it.isNotBlank() },
                addr.countryCode?.takeIf { it.isNotBlank() }?.let(CountryCodes::displayNameOf),
            )
            if (lines.isNotEmpty()) {
                lines.forEach {
                    Text(it, style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary))
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        issuer.phone?.text?.takeIf { it.isNotBlank() }?.let {
            Text(it, style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary))
        }
        issuer.emails?.firstOrNull()?.email?.text?.takeIf { it.isNotBlank() }?.let {
            Text(it, style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary))
        }
        val companyIds = listOfNotNull(
            issuer.companyId1Label?.text to issuer.companyId1Number?.text,
            issuer.companyId2Label?.text to issuer.companyId2Number?.text,
            issuer.companyId3Label?.text to issuer.companyId3Number?.text,
        ).filter { (_, num) -> !num.isNullOrBlank() }
        if (companyIds.isNotEmpty()) Spacer(Modifier.height(8.dp))
        companyIds.forEach { (label, number) ->
            val prefix = label?.takeIf { it.isNotBlank() }?.let { "$it : " } ?: ""
            Text("$prefix$number", style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary))
        }
    }
}

@Composable
private fun ClientDetailsDialog19(
    client: ClientOrIssuerState,
    onDismiss: () -> Unit,
) {
    // Same shape as IssuerDetailsDialog19 — the underlying state is the same
    // ClientOrIssuerState type, so the dialog just delegates.
    IssuerDetailsDialog19(issuer = client, onDismiss = onDismiss)
}

@Composable
private fun ProductDetailsDialog19(
    product: ProductState,
    onDismiss: () -> Unit,
) {
    DetailsDialogShell(onDismiss = onDismiss, title = product.name.text.ifBlank { "—" }) {
        product.description?.text?.takeIf { it.isNotBlank() }?.let {
            Text(it, style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary))
            Spacer(Modifier.height(12.dp))
        }
        val price = product.defaultPriceWithoutTax?.stripTrailingZeros()?.toPlainString()
        val unit = product.unit?.text?.takeIf { it.isNotBlank() }
        if (price != null) {
            val priceLine = buildString {
                append(price)
                if (unit != null) append(" / ").append(unit)
            }
            Text(priceLine, style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary))
        }
        product.taxRate?.let {
            val rate = it.stripTrailingZeros().toPlainString().replace(".", ",")
            Text("TVA $rate%", style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary))
        }
    }
}

@Composable
private fun DetailsDialogShell(
    onDismiss: () -> Unit,
    title: String,
    body: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(AppColors.surface, shape = RoundedCornerShape(14.dp))
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = title, style = MaterialTheme.typography.textScreenTitle)
                Spacer(Modifier.height(16.dp))
                body()
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.buttonActive,
                        contentColor = AppColors.textOnAccent,
                    ),
                ) { Text(stringResource(Res.string.whats_new_close)) }
            }
        }
    }
}

@Composable
private fun ConfirmStep19(
    issuers: List<ClientOrIssuerState>,
    clientsById: Map<Long, ClientOrIssuerState>,
    productsById: Map<Long, ProductState>,
    clientAssignments: Map<Long, Long>,
    productAssignments: Map<Long, Long>,
    onRemoveClient: (Long) -> Unit,
    onRemoveProduct: (Long) -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmojiSlot("✅")
        Spacer(Modifier.height(24.dp))
        StepTitle(stringResource(Res.string.onboarding_19_confirm_title))
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(Res.string.onboarding_19_confirm_body),
            style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary),
            textAlign = TextAlign.Start,
            lineHeight = 22.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(20.dp))
        issuers.forEach { issuer ->
            val issuerId = issuer.id?.toLong() ?: return@forEach
            val myClients = clientAssignments.filter { it.value == issuerId }
                .keys.mapNotNull { clientsById[it] }
                .sortedBy { it.name.text.lowercase() }
            val myProducts = productAssignments.filter { it.value == issuerId }
                .keys.mapNotNull { productsById[it] }
                .sortedBy { it.name.text.lowercase() }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF5F2F8))
                    .padding(horizontal = 12.dp, vertical = 12.dp),
            ) {
                Text(
                    text = issuer.name.text.ifBlank { "—" },
                    style = MaterialTheme.typography.textBodyBold,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(Res.string.onboarding_19_confirm_section_clients),
                    style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary),
                )
                Spacer(Modifier.height(4.dp))
                if (myClients.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.onboarding_19_confirm_no_clients),
                        style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textMuted),
                    )
                } else {
                    myClients.forEach { c ->
                        ConfirmAssignmentRow(
                            primary = c.name.text.ifBlank { "—" },
                            secondary = c.emails?.firstOrNull()?.email?.text.orEmpty(),
                            onRemove = { onRemoveClient(c.id!!.toLong()) },
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(Res.string.onboarding_19_confirm_section_products),
                    style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary),
                )
                Spacer(Modifier.height(4.dp))
                if (myProducts.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.onboarding_19_confirm_no_products),
                        style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textMuted),
                    )
                } else {
                    myProducts.forEach { p ->
                        ConfirmAssignmentRow(
                            primary = p.name.text.ifBlank { "—" },
                            secondary = p.description?.text.orEmpty(),
                            onRemove = { onRemoveProduct(p.id!!.toLong()) },
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height(20.dp))
        PrimaryCta19(
            text = stringResource(Res.string.onboarding_19_confirm_cta),
            onClick = onNext,
        )
    }
}

@Composable
private fun ConfirmAssignmentRow(
    primary: String,
    secondary: String,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(primary, style = MaterialTheme.typography.textBodySmall)
            if (secondary.isNotBlank()) {
                Text(
                    text = secondary,
                    style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textMuted),
                    maxLines = 1,
                )
            }
        }
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = null,
                tint = AppColors.iconSecondary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

// ============================================================================
// Bootstrap steps (only when the auto-seeded "Mon entreprise" is detected)
// ============================================================================

// TODO(strings): move the FR literals in IssuerNameStep19 + IssuerCountryStep19
// to composeResources/values/strings.xml on the `translations` branch. Suggested
// keys:
//   onboarding_19_issuer_name_title    = "Comment s'appelle votre entreprise ?"
//   onboarding_19_issuer_name_body     = "Vous pourrez la modifier à tout moment dans « Mon entreprise »."
//   onboarding_19_issuer_name_label    = "Nom de l'entreprise"
//   onboarding_19_issuer_name_cta      = "Suivant"
//   onboarding_19_issuer_country_title = "Dans quel pays est-elle établie ?"
//   onboarding_19_issuer_country_body  = "Ce choix pilote l'IBAN / BBAN attendu et le texte d'exonération de TVA si vous êtes en franchise en base."
//   onboarding_19_issuer_country_label = "Pays"
//   onboarding_19_issuer_country_cta   = "Suivant"

@Composable
private fun IssuerNameStep19(
    initial: String,
    onSubmit: (String) -> Unit,
) {
    var name by remember {
        mutableStateOf(TextFieldValue(initial, TextRange(initial.length)))
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        EmojiSlot("🏢")
        Spacer(Modifier.height(24.dp))
        StepTitle("Comment s'appelle votre entreprise ?")
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Vous pourrez la modifier à tout moment dans « Mon entreprise ».",
            style = MaterialTheme.typography.textBody,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(24.dp))
        FieldLabel19("Nom de l'entreprise")
        Spacer(Modifier.height(6.dp))
        CompactTextField19(
            value = name,
            onValueChange = { name = it },
            placeholder = "Ma boîte",
            imeAction = ImeAction.Done,
        )
        Spacer(Modifier.height(32.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            PrimaryCta19(
                text = "Suivant",
                enabled = name.text.trim().isNotEmpty(),
                onClick = { onSubmit(name.text.trim()) },
            )
        }
    }
}

@Composable
private fun IssuerCountryStep19(
    initial: String,
    onSubmit: (String) -> Unit,
) {
    var country by remember { mutableStateOf(initial) }
    var pickerOpen by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        EmojiSlot("🌍")
        Spacer(Modifier.height(24.dp))
        StepTitle("Dans quel pays est-elle établie ?")
        Spacer(Modifier.height(24.dp))
        FieldLabel19("Pays")
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF5F2F8))
                .border(BorderStroke(1.dp, Color(0xFFE4DEED)), RoundedCornerShape(10.dp))
                .clickable { pickerOpen = true }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = CountryCodes.displayNameOf(country),
                style = MaterialTheme.typography.textBody.copy(color = AppColors.textPrimary),
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = AppColors.accent,
            )
        }
        Spacer(Modifier.height(32.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            PrimaryCta19(
                text = "Suivant",
                onClick = { onSubmit(country) },
            )
        }
    }
    if (pickerOpen) {
        CountryPicker(
            currentCode = country,
            onSelect = { picked -> country = picked; pickerOpen = false },
            onDismiss = { pickerOpen = false },
        )
    }
}

// ============================================================================
// Reusable local UI bits
// ============================================================================

@Composable
private fun TopBar19(canGoBack: Boolean, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(top = 16.dp, start = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (canGoBack) {
            TextButton(
                onClick = onBack,
                colors = ButtonDefaults.textButtonColors(contentColor = AppColors.textLink),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(2.dp))
                Text(stringResource(Res.string.onboarding_previous))
            }
        }
    }
}

@Composable
private fun MascotSlot(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) { content() }
}

// Fixed-height slot for a single big emoji, sized to match MascotSlot so the
// title sits at the same vertical position regardless of which anchor a given
// step uses. Font is large enough to read as a "hero" glyph, not inline text.
@Composable
private fun EmojiSlot(emoji: String) {
    Box(
        modifier = Modifier
            .height(64.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = emoji,
            fontSize = 48.sp,
        )
    }
}

@Composable
private fun StepTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.textScreenTitle,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun FieldLabel19(text: String) {
    Text(text = text, style = MaterialTheme.typography.textBodySmall)
}

@Composable
private fun CompactTextField19(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    imeAction: ImeAction,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.textBody.copy(color = AppColors.textPrimary),
        cursorBrush = SolidColor(AppColors.accent),
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        keyboardActions = KeyboardActions(),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF5F2F8))
            .border(BorderStroke(1.dp, Color(0xFFE4DEED)), RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        decorationBox = { innerTextField ->
            if (value.text.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.textBody.copy(color = AppColors.textMuted),
                )
            }
            innerTextField()
        },
    )
}

@Composable
private fun PrimaryCta19(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.buttonActive,
            contentColor = AppColors.textOnAccent,
        ),
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 8.dp),
    ) { Text(text) }
}
