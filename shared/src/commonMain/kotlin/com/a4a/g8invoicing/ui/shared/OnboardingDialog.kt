package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.a4a.g8invoicing.data.models.CountryCodes
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.onboarding_cleanup_body_bold_delete_here
import com.a4a.g8invoicing.shared.resources.onboarding_cleanup_body_part1
import com.a4a.g8invoicing.shared.resources.onboarding_cleanup_body_part3
import com.a4a.g8invoicing.shared.resources.onboarding_cleanup_cta
import com.a4a.g8invoicing.shared.resources.onboarding_cleanup_delete_confirm_no
import com.a4a.g8invoicing.shared.resources.onboarding_cleanup_delete_confirm_title
import com.a4a.g8invoicing.shared.resources.onboarding_cleanup_delete_confirm_yes
import com.a4a.g8invoicing.shared.resources.onboarding_cleanup_title
import com.a4a.g8invoicing.shared.resources.onboarding_client_country_done_applied_body
import com.a4a.g8invoicing.shared.resources.onboarding_client_country_done_applied_title
import com.a4a.g8invoicing.shared.resources.onboarding_client_country_done_cta
import com.a4a.g8invoicing.shared.resources.onboarding_client_country_done_skipped_body
import com.a4a.g8invoicing.shared.resources.onboarding_client_country_done_skipped_title
import com.a4a.g8invoicing.shared.resources.onboarding_client_country_intro_body
import com.a4a.g8invoicing.shared.resources.onboarding_client_country_intro_no
import com.a4a.g8invoicing.shared.resources.onboarding_client_country_intro_title
import com.a4a.g8invoicing.shared.resources.onboarding_client_country_intro_yes
import com.a4a.g8invoicing.shared.resources.onboarding_client_country_picker_pick
import com.a4a.g8invoicing.shared.resources.onboarding_client_country_picker_subtitle
import com.a4a.g8invoicing.shared.resources.onboarding_devis_body_after_bold
import com.a4a.g8invoicing.shared.resources.onboarding_devis_body_bold
import com.a4a.g8invoicing.shared.resources.onboarding_devis_cta_activate
import com.a4a.g8invoicing.shared.resources.onboarding_devis_cta_later_gstore
import com.a4a.g8invoicing.shared.resources.onboarding_devis_cta_no_devis
import com.a4a.g8invoicing.shared.resources.onboarding_devis_title
import com.a4a.g8invoicing.shared.resources.account_backup_dialog_message
import com.a4a.g8invoicing.shared.resources.account_backup_dialog_no
import com.a4a.g8invoicing.shared.resources.account_backup_dialog_title
import com.a4a.g8invoicing.shared.resources.account_backup_dialog_yes
import com.a4a.g8invoicing.shared.resources.ok
import com.a4a.g8invoicing.shared.resources.onboarding_facturx_body_before_bold
import com.a4a.g8invoicing.shared.resources.onboarding_facturx_body_bold
import com.a4a.g8invoicing.shared.resources.onboarding_facturx_cta
import com.a4a.g8invoicing.shared.resources.onboarding_facturx_title
import com.a4a.g8invoicing.shared.resources.onboarding_privacy_body_p1
import com.a4a.g8invoicing.shared.resources.onboarding_privacy_body_p2
import com.a4a.g8invoicing.shared.resources.onboarding_privacy_cta_backup
import com.a4a.g8invoicing.shared.resources.onboarding_privacy_title
import com.a4a.g8invoicing.shared.resources.onboarding_intraeu_subtitle
import com.a4a.g8invoicing.shared.resources.onboarding_issuers_done_body_plural
import com.a4a.g8invoicing.shared.resources.onboarding_issuers_done_body_singular
import com.a4a.g8invoicing.shared.resources.onboarding_issuers_done_title
import com.a4a.g8invoicing.shared.resources.onboarding_issuer_country_pick
import com.a4a.g8invoicing.shared.resources.onboarding_issuer_country_subtitle
import com.a4a.g8invoicing.shared.resources.onboarding_mixed_hint_body
import com.a4a.g8invoicing.shared.resources.onboarding_mixed_hint_title
import com.a4a.g8invoicing.shared.resources.onboarding_nature_intro
import com.a4a.g8invoicing.shared.resources.onboarding_nature_mixed
import com.a4a.g8invoicing.shared.resources.onboarding_nature_only_goods
import com.a4a.g8invoicing.shared.resources.onboarding_nature_only_services
import com.a4a.g8invoicing.shared.resources.onboarding_nature_question
import com.a4a.g8invoicing.shared.resources.onboarding_nature_title
import com.a4a.g8invoicing.shared.resources.onboarding_next
import com.a4a.g8invoicing.shared.resources.onboarding_no
import com.a4a.g8invoicing.shared.resources.onboarding_previous
import com.a4a.g8invoicing.shared.resources.onboarding_thanks_body
import com.a4a.g8invoicing.shared.resources.onboarding_thanks_cta
import com.a4a.g8invoicing.shared.resources.onboarding_thanks_title
import com.a4a.g8invoicing.shared.resources.onboarding_unitcode_bold1
import com.a4a.g8invoicing.shared.resources.onboarding_unitcode_bold2
import com.a4a.g8invoicing.shared.resources.onboarding_unitcode_bold3
import com.a4a.g8invoicing.shared.resources.onboarding_unitcode_cta
import com.a4a.g8invoicing.shared.resources.onboarding_unitcode_part1
import com.a4a.g8invoicing.shared.resources.onboarding_unitcode_part2
import com.a4a.g8invoicing.shared.resources.onboarding_unitcode_part3
import com.a4a.g8invoicing.shared.resources.onboarding_unitcode_part4
import com.a4a.g8invoicing.shared.resources.onboarding_unitcode_title
import com.a4a.g8invoicing.shared.resources.onboarding_vat_dontknow_body
import com.a4a.g8invoicing.shared.resources.onboarding_vat_dontknow_cta
import com.a4a.g8invoicing.shared.resources.onboarding_vat_dontknow_title
import com.a4a.g8invoicing.shared.resources.onboarding_vat_option_dontknow
import com.a4a.g8invoicing.shared.resources.onboarding_vat_option_franchise
import com.a4a.g8invoicing.shared.resources.onboarding_vat_option_tva
import com.a4a.g8invoicing.shared.resources.onboarding_vat_subtitle
import com.a4a.g8invoicing.shared.resources.onboarding_welcome_body
import com.a4a.g8invoicing.shared.resources.onboarding_welcome_cta
import com.a4a.g8invoicing.shared.resources.onboarding_yes
import com.a4a.g8invoicing.shared.resources.whats_new_close
import com.a4a.g8invoicing.ui.screens.shared.CountryPicker
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.OnboardingIssuerAnswers
import com.a4a.g8invoicing.ui.states.OnboardingStep
import com.a4a.g8invoicing.ui.states.ProductNatureAnswer
import com.a4a.g8invoicing.ui.states.VatAnswer
import com.a4a.g8invoicing.ui.theme.ColorVioletLink
import com.a4a.g8invoicing.ui.viewmodels.OnboardingViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Fullscreen non-dismissable onboarding wizard for the 1.8 upgrade.
 *
 * Content is vertically centered in the viewport (short steps sit mid-screen).
 * A shared [MascotSlot] fixes the emoji/kaomoji footprint so animated frames
 * don't push the text below them around.
 */
@Composable
fun OnboardingDialog(
    onDismiss: () -> Unit,
    onExportDatabase: () -> com.a4a.g8invoicing.ui.screens.ExportResult =
        { com.a4a.g8invoicing.ui.screens.ExportResult.Error("Not available on this platform") },
    onSendDatabaseByEmail: (String) -> Unit = {},
) {
    val vm: OnboardingViewModel = koinViewModel()
    val step by vm.currentStep.collectAsState()
    val issuers by vm.issuers.collectAsState()
    val issuersLoaded by vm.issuersLoaded.collectAsState()
    val perIssuerAnswers by vm.perIssuerAnswers.collectAsState()
    val clientCountrySame by vm.clientCountrySameForAll.collectAsState()
    val clientCountry by vm.clientCountry.collectAsState()

    // Database backup flow state — driven from the FacturX intro step's
    // "Sauvegarder ma base de données" CTA. Mirrors the Account → Sauvegarde
    // pattern (export → optional email dialog) instead of navigating away
    // from the wizard, since the wizard is non-dismissable.
    var exportedFilePath by remember { mutableStateOf<String?>(null) }
    var showSendByEmailDialog by remember { mutableStateOf(false) }
    var exportErrorMessage by remember { mutableStateOf<String?>(null) }
    val onBackupClick: () -> Unit = {
        when (val result = onExportDatabase()) {
            is com.a4a.g8invoicing.ui.screens.ExportResult.Success -> {
                exportedFilePath = result.filePath
                showSendByEmailDialog = true
            }
            is com.a4a.g8invoicing.ui.screens.ExportResult.Error -> {
                exportErrorMessage = result.message
            }
        }
    }

    Dialog(
        onDismissRequest = { /* no-op — cannot escape the wizard */ },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center,
                ) {
                    OnboardingStepContent(
                        step = step,
                        issuers = issuers,
                        perIssuerAnswers = perIssuerAnswers,
                        clientCountrySame = clientCountrySame,
                        clientCountry = clientCountry,
                        onDeleteIssuer = { vm.deleteIssuer(it) },
                        onDiscover = { vm.next() },
                        onActivateQuote = { vm.activateQuoteModule(); vm.next() },
                        onLaterGstore = { vm.next() },
                        onNoDevis = { vm.next() },
                        onFacturxAllonsY = { vm.next() },
                        onFacturxBackup = onBackupClick,
                        onSetIssuerCountry = { id, code -> vm.setIssuerCountry(id, code) },
                        onSetVatAnswer = { id, v ->
                            vm.setVatAnswer(id, v); vm.next()
                        },
                        onSetIntraEu = { id, v ->
                            vm.setIntraEu(id, v); vm.next()
                        },
                        onSetNature = { id, n ->
                            vm.setProductNature(id, n); vm.next()
                        },
                        onSetClientCountrySame = { v ->
                            vm.setClientCountrySameForAll(v); vm.next()
                        },
                        onSetClientCountry = { code -> vm.setClientCountry(code) },
                    )
                }
                Spacer(Modifier.height(12.dp))
                OnboardingNavRow(
                    step = step,
                    issuersLoaded = issuersLoaded,
                    perIssuerAnswers = perIssuerAnswers,
                    issuers = issuers,
                    clientCountry = clientCountry,
                    onPrevious = { vm.previous() },
                    onNext = { vm.next() },
                    onFinish = { vm.commit { onDismiss() } },
                )
            }
        }
    }

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
                    exportedFilePath?.let { onSendDatabaseByEmail(it) }
                }) { Text(stringResource(Res.string.account_backup_dialog_yes), color = ColorVioletLink) }
            },
            dismissButton = {
                TextButton(onClick = { showSendByEmailDialog = false }) {
                    Text(stringResource(Res.string.account_backup_dialog_no), color = ColorVioletLink)
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
                    Text(stringResource(Res.string.ok), color = ColorVioletLink)
                }
            },
        )
    }
}

@Composable
private fun OnboardingStepContent(
    step: OnboardingStep,
    issuers: List<ClientOrIssuerState>,
    perIssuerAnswers: Map<Int, OnboardingIssuerAnswers>,
    clientCountrySame: Boolean?,
    clientCountry: String?,
    onDeleteIssuer: (ClientOrIssuerState) -> Unit,
    onDiscover: () -> Unit,
    onActivateQuote: () -> Unit,
    onLaterGstore: () -> Unit,
    onNoDevis: () -> Unit,
    onFacturxAllonsY: () -> Unit,
    onFacturxBackup: () -> Unit,
    onSetIssuerCountry: (Int, String) -> Unit,
    onSetVatAnswer: (Int, VatAnswer) -> Unit,
    onSetIntraEu: (Int, Boolean) -> Unit,
    onSetNature: (Int, ProductNatureAnswer) -> Unit,
    onSetClientCountrySame: (Boolean) -> Unit,
    onSetClientCountry: (String) -> Unit,
) {
    when (step) {
        OnboardingStep.Welcome -> WelcomeStep(onDiscover)
        OnboardingStep.DevisIntro -> DevisIntroStep(onActivateQuote, onLaterGstore, onNoDevis)
        OnboardingStep.FacturXIntro -> FacturXIntroStep(onFacturxAllonsY)
        OnboardingStep.Privacy -> PrivacyStep(onBackup = onFacturxBackup, onNext = onFacturxAllonsY)
        OnboardingStep.IssuerCleanup -> IssuerCleanupStep(issuers, onDeleteIssuer)
        is OnboardingStep.IssuerCountry -> {
            val issuer = issuers.getOrNull(step.issuerIndex)
            if (issuer != null) IssuerCountryStep(
                issuer = issuer,
                pickedCountry = perIssuerAnswers[issuer.id]?.countryCode,
                onPick = { code -> onSetIssuerCountry(issuer.id!!, code) },
            )
        }
        is OnboardingStep.VatExempt -> {
            val issuer = issuers.getOrNull(step.issuerIndex)
            if (issuer != null) VatExemptStep(issuer, onSetVatAnswer)
        }
        is OnboardingStep.VatDontKnowHint -> VatDontKnowHintStep()
        is OnboardingStep.IntraEu -> {
            val issuer = issuers.getOrNull(step.issuerIndex)
            if (issuer != null) IntraEuStep(issuer, onSetIntraEu)
        }
        is OnboardingStep.ProductNature -> {
            val issuer = issuers.getOrNull(step.issuerIndex)
            if (issuer != null) ProductNatureStep(
                issuer = issuer,
                current = perIssuerAnswers[issuer.id]?.productNature,
                onAnswer = onSetNature,
            )
        }
        is OnboardingStep.MixedHint -> MixedHintStep()
        OnboardingStep.IssuersDone -> IssuersDoneStep(issuerCount = issuers.size)
        OnboardingStep.ClientCountryQuestion -> ClientCountryQuestionStep(onSetClientCountrySame)
        OnboardingStep.ClientCountryPicker -> ClientCountryPickerStep(
            currentCountry = clientCountry,
            onPick = onSetClientCountry,
        )
        OnboardingStep.ClientCountryDoneApplied -> ClientCountryDoneAppliedStep()
        OnboardingStep.ClientCountryDoneSkipped -> ClientCountryDoneSkippedStep()
        OnboardingStep.UnitCodeInfo -> UnitCodeInfoStep()
        OnboardingStep.ThankYou -> ThankYouStep()
    }
}

@Composable
private fun OnboardingNavRow(
    step: OnboardingStep,
    issuersLoaded: Boolean,
    perIssuerAnswers: Map<Int, OnboardingIssuerAnswers>,
    issuers: List<ClientOrIssuerState>,
    clientCountry: String?,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit,
) {
    // Steps that own their own inline CTAs don't render a bottom bar.
    val showPrev = when (step) {
        OnboardingStep.Welcome,
        OnboardingStep.DevisIntro,
        OnboardingStep.FacturXIntro,
        OnboardingStep.Privacy,
        -> false
        else -> true
    }
    val isFinal = step == OnboardingStep.ThankYou
    val showNextButton = when (step) {
        OnboardingStep.Welcome,
        OnboardingStep.DevisIntro,
        OnboardingStep.FacturXIntro,
        OnboardingStep.Privacy,
        is OnboardingStep.VatExempt,
        is OnboardingStep.IntraEu,
        is OnboardingStep.ProductNature,
        OnboardingStep.ClientCountryQuestion,
        -> false
        else -> true
    }
    val nextEnabled = when (step) {
        is OnboardingStep.IssuerCountry -> {
            val issuerId = issuers.getOrNull(step.issuerIndex)?.id
            !perIssuerAnswers[issuerId]?.countryCode.isNullOrBlank()
        }
        OnboardingStep.ClientCountryPicker -> !clientCountry.isNullOrBlank()
        else -> true
    }
    val nextLabel = when (step) {
        OnboardingStep.IssuerCleanup -> stringResource(Res.string.onboarding_cleanup_cta)
        is OnboardingStep.VatDontKnowHint -> stringResource(Res.string.onboarding_vat_dontknow_cta)
        OnboardingStep.ClientCountryDoneApplied,
        OnboardingStep.ClientCountryDoneSkipped,
        -> stringResource(Res.string.onboarding_client_country_done_cta)
        OnboardingStep.UnitCodeInfo -> stringResource(Res.string.onboarding_unitcode_cta)
        OnboardingStep.ThankYou -> stringResource(Res.string.onboarding_thanks_cta)
        else -> stringResource(Res.string.onboarding_next)
    }

    if (!showPrev && !showNextButton) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (showPrev) Arrangement.SpaceBetween else Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showPrev) {
            TextButton(
                onClick = onPrevious,
                colors = ButtonDefaults.textButtonColors(contentColor = ColorVioletLink),
            ) { Text(stringResource(Res.string.onboarding_previous)) }
        }
        if (showNextButton) {
            Button(
                onClick = { if (isFinal) onFinish() else onNext() },
                enabled = nextEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorVioletLink,
                    contentColor = Color.White,
                ),
            ) { Text(nextLabel) }
        }
    }
}

// ============================================================================
// Reusable pieces
// ============================================================================

/** Fixed-height slot for a mascot glyph. Prevents the surrounding text from
 *  shifting position when the AnimatedKaomoji frames vary in intrinsic height. */
@Composable
private fun MascotSlot(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun StepTitle(text: String) {
    Text(
        text = text,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun StepEmoji(emoji: String) {
    MascotSlot {
        Text(text = emoji, fontSize = 40.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun AnimatedMascot() {
    MascotSlot { AnimatedKaomoji() }
}

@Composable
private fun AnimatedMascotThanks() {
    MascotSlot { AnimatedKaomojiThanks() }
}

// ============================================================================
// Individual step composables
// ============================================================================

@Composable
private fun WelcomeStep(onDiscover: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedMascot()
        Spacer(Modifier.height(40.dp))
        Text(
            text = stringResource(Res.string.onboarding_welcome_body),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 30.sp,
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onDiscover,
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorVioletLink,
                contentColor = Color.White,
            ),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 8.dp),
        ) { Text(stringResource(Res.string.onboarding_welcome_cta)) }
    }
}

@Composable
private fun DevisIntroStep(
    onActivate: () -> Unit,
    onLaterGstore: () -> Unit,
    onNoDevis: () -> Unit,
) {
    val bold = SpanStyle(fontWeight = FontWeight.Bold, color = Color.DarkGray)
    val body = buildAnnotatedString {
        withStyle(bold) { append(stringResource(Res.string.onboarding_devis_body_bold)) }
        append(stringResource(Res.string.onboarding_devis_body_after_bold))
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StepEmoji("📄")
        Spacer(Modifier.height(24.dp))
        StepTitle(stringResource(Res.string.onboarding_devis_title))
        Spacer(Modifier.height(32.dp))
        Text(
            text = body,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            color = Color.DarkGray,
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onActivate,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorVioletLink,
                contentColor = Color.White,
            ),
        ) { Text(stringResource(Res.string.onboarding_devis_cta_activate)) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onLaterGstore,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorVioletLink),
        ) { Text(stringResource(Res.string.onboarding_devis_cta_later_gstore)) }
        Spacer(Modifier.height(4.dp))
        TextButton(
            onClick = onNoDevis,
            colors = ButtonDefaults.textButtonColors(contentColor = ColorVioletLink),
        ) { Text(stringResource(Res.string.onboarding_devis_cta_no_devis)) }
    }
}

@Composable
private fun FacturXIntroStep(onNext: () -> Unit) {
    val bold = SpanStyle(fontWeight = FontWeight.Bold, color = Color.DarkGray)
    val body = buildAnnotatedString {
        append(stringResource(Res.string.onboarding_facturx_body_before_bold))
        withStyle(bold) { append(stringResource(Res.string.onboarding_facturx_body_bold)) }
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedMascot()
        Spacer(Modifier.height(24.dp))
        StepTitle(stringResource(Res.string.onboarding_facturx_title))
        Spacer(Modifier.height(32.dp))
        Text(
            text = body,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            color = Color.DarkGray,
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorVioletLink,
                contentColor = Color.White,
            ),
        ) { Text(stringResource(Res.string.onboarding_facturx_cta)) }
    }
}

@Composable
private fun PrivacyStep(onBackup: () -> Unit, onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Material "Shield" icon reads as privacy/protection; kept as an icon
        // (not an emoji) to visually differentiate this reassurance screen from
        // the other emoji-anchored info steps.
        MascotSlot {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                tint = ColorVioletLink,
                modifier = Modifier.size(44.dp),
            )
        }
        Spacer(Modifier.height(24.dp))
        StepTitle(stringResource(Res.string.onboarding_privacy_title))
        Spacer(Modifier.height(32.dp))
        Text(
            text = stringResource(Res.string.onboarding_privacy_body_p1),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            color = Color.DarkGray,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.onboarding_privacy_body_p2),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            color = Color.DarkGray,
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onBackup,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorVioletLink,
                contentColor = Color.White,
            ),
        ) { Text(stringResource(Res.string.onboarding_privacy_cta_backup)) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorVioletLink),
        ) { Text(stringResource(Res.string.onboarding_next)) }
    }
}

@Composable
private fun IssuerCleanupStep(
    issuers: List<ClientOrIssuerState>,
    onDelete: (ClientOrIssuerState) -> Unit,
) {
    var pendingDelete by remember { mutableStateOf<ClientOrIssuerState?>(null) }
    var pendingDetails by remember { mutableStateOf<ClientOrIssuerState?>(null) }
    val bold = SpanStyle(fontWeight = FontWeight.Bold, color = Color.DarkGray)
    val body = buildAnnotatedString {
        append(stringResource(Res.string.onboarding_cleanup_body_part1))
        withStyle(bold) { append(stringResource(Res.string.onboarding_cleanup_body_bold_delete_here)) }
        append(stringResource(Res.string.onboarding_cleanup_body_part3))
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StepEmoji("🏢")
        Spacer(Modifier.height(24.dp))
        StepTitle(stringResource(Res.string.onboarding_cleanup_title))
        Spacer(Modifier.height(32.dp))
        Text(text = body, fontSize = 15.sp, lineHeight = 22.sp, color = Color.DarkGray)
        Spacer(Modifier.height(20.dp))
        issuers.forEach { issuer ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                    .clickable { pendingDetails = issuer }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = issuer.name.text.ifBlank { "—" },
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                )
                IconButton(onClick = { pendingDelete = issuer }) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        tint = Color.DarkGray,
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }

    pendingDetails?.let { issuer ->
        IssuerDetailsDialog(issuer = issuer, onDismiss = { pendingDetails = null })
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

/** Read-only detail sheet triggered by tapping an entreprise card in the
 *  cleanup step. Shows the name, address block, phone/email and up to three
 *  company IDs. Editing lives outside the wizard — Mon Compte → Mes entreprises. */
@Composable
private fun IssuerDetailsDialog(
    issuer: ClientOrIssuerState,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(Color.White, shape = RoundedCornerShape(14.dp))
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = issuer.name.text.ifBlank { "—" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
                issuer.firstName?.text?.takeIf { it.isNotBlank() }?.let {
                    Text(text = it, fontSize = 14.sp, color = Color.DarkGray)
                }
                Spacer(Modifier.height(16.dp))
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
                            Text(text = it, fontSize = 14.sp, color = Color.DarkGray)
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
                issuer.phone?.text?.takeIf { it.isNotBlank() }?.let {
                    Text(text = it, fontSize = 14.sp, color = Color.DarkGray)
                }
                issuer.emails?.firstOrNull()?.email?.text?.takeIf { it.isNotBlank() }?.let {
                    Text(text = it, fontSize = 14.sp, color = Color.DarkGray)
                }
                val companyIds = listOfNotNull(
                    issuer.companyId1Label?.text to issuer.companyId1Number?.text,
                    issuer.companyId2Label?.text to issuer.companyId2Number?.text,
                    issuer.companyId3Label?.text to issuer.companyId3Number?.text,
                ).filter { (_, num) -> !num.isNullOrBlank() }
                if (companyIds.isNotEmpty()) Spacer(Modifier.height(10.dp))
                companyIds.forEach { (label, number) ->
                    val prefix = label?.takeIf { it.isNotBlank() }?.let { "$it : " } ?: ""
                    Text(
                        text = "$prefix$number",
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                    )
                }
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorVioletLink,
                        contentColor = Color.White,
                    ),
                ) { Text(stringResource(Res.string.whats_new_close)) }
            }
        }
    }
}

// --- Per-issuer question steps ----------------------------------------------

@Composable
private fun IssuerCountryStep(
    issuer: ClientOrIssuerState,
    pickedCountry: String?,
    onPick: (String) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    val name = issuer.name.text.ifBlank { "—" }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StepEmoji("🌍")
        Spacer(Modifier.height(20.dp))
        StepTitle(name)
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(Res.string.onboarding_issuer_country_subtitle, name),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
        )
        Spacer(Modifier.height(28.dp))
        CountryDropdownField(
            currentCountry = pickedCountry,
            placeholder = stringResource(Res.string.onboarding_issuer_country_pick),
            onClick = { showPicker = true },
        )
    }

    if (showPicker) {
        CountryPicker(
            currentCode = pickedCountry,
            onSelect = { code -> onPick(code); showPicker = false },
            onDismiss = { showPicker = false },
        )
    }
}

/** Dropdown-style clickable field for a country: light background, black text,
 *  violet chevron on the right. Replaces the earlier violet outlined-button look
 *  which felt heavy for a value picker. */
@Composable
private fun CountryDropdownField(
    currentCountry: String?,
    placeholder: String,
    onClick: () -> Unit,
) {
    val label = currentCountry?.let { CountryCodes.displayNameOf(it) } ?: placeholder
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF5F2F8))
            .border(BorderStroke(1.dp, Color(0xFFE4DEED)), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = if (currentCountry != null) Color.Black else Color.DarkGray,
            fontSize = 15.sp,
        )
        Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            contentDescription = null,
            tint = ColorVioletLink,
        )
    }
}

@Composable
private fun VatExemptStep(
    issuer: ClientOrIssuerState,
    onAnswer: (Int, VatAnswer) -> Unit,
) {
    val id = issuer.id ?: return
    val name = issuer.name.text.ifBlank { "—" }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StepTitle(name)
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(Res.string.onboarding_vat_subtitle),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = { onAnswer(id, VatAnswer.FRANCHISE) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorVioletLink,
                contentColor = Color.White,
            ),
        ) { Text(stringResource(Res.string.onboarding_vat_option_franchise)) }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { onAnswer(id, VatAnswer.TVA) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorVioletLink,
                contentColor = Color.White,
            ),
        ) { Text(stringResource(Res.string.onboarding_vat_option_tva)) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { onAnswer(id, VatAnswer.DONT_KNOW) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorVioletLink),
        ) { Text(stringResource(Res.string.onboarding_vat_option_dontknow)) }
    }
}

@Composable
private fun VatDontKnowHintStep() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StepEmoji("🤝")
        Spacer(Modifier.height(24.dp))
        StepTitle(stringResource(Res.string.onboarding_vat_dontknow_title))
        Spacer(Modifier.height(32.dp))
        Text(
            text = stringResource(Res.string.onboarding_vat_dontknow_body),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            color = Color.DarkGray,
        )
    }
}

@Composable
private fun IntraEuStep(
    issuer: ClientOrIssuerState,
    onAnswer: (Int, Boolean) -> Unit,
) {
    val id = issuer.id ?: return
    val name = issuer.name.text.ifBlank { "—" }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StepTitle(name)
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(Res.string.onboarding_intraeu_subtitle),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = { onAnswer(id, true) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorVioletLink,
                contentColor = Color.White,
            ),
        ) { Text(stringResource(Res.string.onboarding_yes)) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { onAnswer(id, false) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorVioletLink),
        ) { Text(stringResource(Res.string.onboarding_no)) }
    }
}

// --- Product nature ---------------------------------------------------------

@Composable
private fun ProductNatureStep(
    issuer: ClientOrIssuerState,
    current: ProductNatureAnswer?,
    onAnswer: (Int, ProductNatureAnswer) -> Unit,
) {
    val id = issuer.id ?: return
    val name = issuer.name.text.ifBlank { "—" }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StepTitle(name)
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(Res.string.onboarding_nature_intro),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            color = Color.DarkGray,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.onboarding_nature_question),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { onAnswer(id, ProductNatureAnswer.ONLY_SERVICES) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorVioletLink,
                contentColor = Color.White,
            ),
        ) { Text(stringResource(Res.string.onboarding_nature_only_services)) }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { onAnswer(id, ProductNatureAnswer.ONLY_GOODS) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorVioletLink,
                contentColor = Color.White,
            ),
        ) { Text(stringResource(Res.string.onboarding_nature_only_goods)) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { onAnswer(id, ProductNatureAnswer.MIXED) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorVioletLink),
        ) { Text(stringResource(Res.string.onboarding_nature_mixed)) }
    }
}

@Composable
private fun MixedHintStep() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StepEmoji("🤝")
        Spacer(Modifier.height(24.dp))
        StepTitle(stringResource(Res.string.onboarding_mixed_hint_title))
        Spacer(Modifier.height(32.dp))
        Text(
            text = stringResource(Res.string.onboarding_mixed_hint_body),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            color = Color.DarkGray,
        )
    }
}

// --- Client country flow ----------------------------------------------------

@Composable
private fun ClientCountryQuestionStep(onAnswer: (Boolean) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StepEmoji("👥")
        Spacer(Modifier.height(24.dp))
        StepTitle(stringResource(Res.string.onboarding_client_country_intro_title))
        Spacer(Modifier.height(32.dp))
        Text(
            text = stringResource(Res.string.onboarding_client_country_intro_body),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            color = Color.DarkGray,
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = { onAnswer(true) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorVioletLink,
                contentColor = Color.White,
            ),
        ) { Text(stringResource(Res.string.onboarding_client_country_intro_yes)) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { onAnswer(false) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorVioletLink),
        ) { Text(stringResource(Res.string.onboarding_client_country_intro_no)) }
    }
}

@Composable
private fun ClientCountryPickerStep(
    currentCountry: String?,
    onPick: (String) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StepEmoji("🌍")
        Spacer(Modifier.height(24.dp))
        StepTitle(stringResource(Res.string.onboarding_client_country_picker_subtitle))
        Spacer(Modifier.height(28.dp))
        CountryDropdownField(
            currentCountry = currentCountry,
            placeholder = stringResource(Res.string.onboarding_client_country_picker_pick),
            onClick = { showPicker = true },
        )
    }

    if (showPicker) {
        CountryPicker(
            currentCode = currentCountry,
            onSelect = { code -> onPick(code); showPicker = false },
            onDismiss = { showPicker = false },
        )
    }
}

@Composable
private fun ClientCountryDoneAppliedStep() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StepEmoji("✅")
        Spacer(Modifier.height(24.dp))
        StepTitle(stringResource(Res.string.onboarding_client_country_done_applied_title))
        Spacer(Modifier.height(32.dp))
        Text(
            text = stringResource(Res.string.onboarding_client_country_done_applied_body),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            color = Color.DarkGray,
        )
    }
}

@Composable
private fun ClientCountryDoneSkippedStep() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StepEmoji("🤝")
        Spacer(Modifier.height(24.dp))
        StepTitle(stringResource(Res.string.onboarding_client_country_done_skipped_title))
        Spacer(Modifier.height(32.dp))
        Text(
            text = stringResource(Res.string.onboarding_client_country_done_skipped_body),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            color = Color.DarkGray,
        )
    }
}

@Composable
private fun UnitCodeInfoStep() {
    val bold = SpanStyle(fontWeight = FontWeight.Bold, color = Color.DarkGray)
    val body = buildAnnotatedString {
        append(stringResource(Res.string.onboarding_unitcode_part1))
        withStyle(bold) { append(stringResource(Res.string.onboarding_unitcode_bold1)) }
        append(stringResource(Res.string.onboarding_unitcode_part2))
        withStyle(bold) { append(stringResource(Res.string.onboarding_unitcode_bold2)) }
        append(stringResource(Res.string.onboarding_unitcode_part3))
        withStyle(bold) { append(stringResource(Res.string.onboarding_unitcode_bold3)) }
        append(stringResource(Res.string.onboarding_unitcode_part4))
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StepEmoji("🏷️")
        Spacer(Modifier.height(24.dp))
        StepTitle(stringResource(Res.string.onboarding_unitcode_title))
        Spacer(Modifier.height(32.dp))
        Text(
            text = body,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            color = Color.DarkGray,
        )
    }
}

@Composable
private fun IssuersDoneStep(issuerCount: Int) {
    val body = if (issuerCount <= 1)
        stringResource(Res.string.onboarding_issuers_done_body_singular)
    else
        stringResource(Res.string.onboarding_issuers_done_body_plural)
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StepEmoji("✅")
        Spacer(Modifier.height(24.dp))
        StepTitle(stringResource(Res.string.onboarding_issuers_done_title))
        Spacer(Modifier.height(32.dp))
        Text(
            text = body,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            color = Color.DarkGray,
        )
    }
}

@Composable
private fun ThankYouStep() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedMascotThanks()
        Spacer(Modifier.height(24.dp))
        StepTitle(stringResource(Res.string.onboarding_thanks_title))
        Spacer(Modifier.height(32.dp))
        Text(
            text = stringResource(Res.string.onboarding_thanks_body),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
        )
    }
}
