package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.textBodySmall
import com.a4a.g8invoicing.ui.theme.textScreenTitle

// TODO(strings): move inline FR copy to strings.xml via the translations branch
// once this ships. Kept inline because the dialog only mounts for FR-locale
// users (see MainCompose gate).

/**
 * 1.8.1 e-invoicing intro — fullscreen non-dismissable wizard mirroring
 * [OnboardingDialog]. PR mascot walks the user through the September 2026 PA
 * obligation, SuperPDP, and g8's roadmap. Six steps with light branching (see
 * [EInvoiceStep]). SEEN=true is persisted at first mount so an accidental
 * process death can't re-prompt.
 */
@Composable
fun EInvoiceIntroDialog(
    onDismiss: () -> Unit,
    onOpenUrl: (String) -> Unit = {},
    onComposeEmail: (address: String, subject: String, body: String) -> Unit = { _, _, _ -> },
) {
    var step by rememberSaveable { mutableStateOf(EInvoiceStep.Intro) }
    // Back-nav history stack. Held in remember (not rememberSaveable) — worst
    // case on process death "Précédent" falls back to Intro.
    val history = remember { mutableStateListOf<EInvoiceStep>() }
    val goTo: (EInvoiceStep) -> Unit = { next ->
        history.add(step)
        step = next
    }
    val goBack: () -> Unit = {
        step = if (history.isNotEmpty()) history.removeAt(history.lastIndex)
               else EInvoiceStep.Intro
    }

    Dialog(
        onDismissRequest = { /* no-op — user must complete the flow */ },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            ) {
                // Reserved-height slot so the content below doesn't jump when
                // the "Précédent" button appears/disappears.
                Box(modifier = Modifier.height(40.dp).fillMaxWidth()) {
                    if (step != EInvoiceStep.Intro) {
                        TextButton(
                            onClick = goBack,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = AppColors.textLink,
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                        ) { Text("‹ Précédent") }
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center,
                ) {
                    EInvoiceStepContent(
                        step = step,
                        onGo = goTo,
                        onDismiss = onDismiss,
                        onOpenUrl = onOpenUrl,
                        onComposeEmail = onComposeEmail,
                    )
                }
            }
        }
    }
}

private enum class EInvoiceStep {
    Intro,          // PR speech-bubble intro + 4 branch buttons
    DejaEquipe,     // Only reachable from "J'ai déjà une PA"
    Compatis,       // Only reachable from "Dégoûté·e"
    Solution,       // "Une solution gratuite..."
    SuperPDP,       // The SuperPDP intro
    CoteG8,         // "Du côté de g8..."
    Infos,          // Toutes les infos + Fermer
}

@Composable
private fun EInvoiceStepContent(
    step: EInvoiceStep,
    onGo: (EInvoiceStep) -> Unit,
    onDismiss: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onComposeEmail: (String, String, String) -> Unit,
) {
    when (step) {
        EInvoiceStep.Intro -> IntroStep(onGo, onDismiss)
        EInvoiceStep.DejaEquipe -> DejaEquipeStep(onGo, onDismiss)
        EInvoiceStep.Compatis -> CompatisStep(onGo)
        EInvoiceStep.Solution -> SolutionStep(onGo)
        EInvoiceStep.SuperPDP -> SuperPDPStep(onGo, onOpenUrl)
        EInvoiceStep.CoteG8 -> CoteG8Step(onGo)
        EInvoiceStep.Infos -> InfosStep(onDismiss, onOpenUrl, onComposeEmail)
    }
}

// ============================================================================
// Reusable pieces (same shape as OnboardingDialog's private helpers)
// ============================================================================

/** Fixed-height slot for a mascot glyph. Keeps text below stable when the
 *  mascot changes across steps. */
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
private fun StepMascot(text: String) {
    MascotSlot {
        Text(
            text = text,
            fontSize = 22.sp,
            // Monospace prevents glyph-width jitter across kaomoji frames.
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StepTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.textScreenTitle,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun StepBody(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary),
        textAlign = TextAlign.Center,
        lineHeight = 22.sp,
    )
}

@Composable
private fun StepAnnotatedBody(
    build: androidx.compose.ui.text.AnnotatedString.Builder.() -> Unit,
) {
    Text(
        text = buildAnnotatedString(build),
        style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary),
        textAlign = TextAlign.Center,
        lineHeight = 22.sp,
    )
}

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.buttonActive,
            contentColor = AppColors.textOnAccent,
        ),
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 8.dp),
    ) { Text(label) }
}

@Composable
private fun SecondaryButton(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.textLink),
    ) { Text(label) }
}

@Composable
private fun TertiaryButton(label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(contentColor = AppColors.textLink),
    ) { Text(label) }
}

// ============================================================================
// Individual step composables
// ============================================================================

@Composable
private fun IntroStep(onGo: (EInvoiceStep) -> Unit, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StepMascot("( ´・・)ﾉ")
        Spacer(Modifier.height(24.dp))
        StepTitle("Coucou")
        Spacer(Modifier.height(24.dp))
        StepAnnotatedBody {
            append("Je voulais te parler de la ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("facturation électronique…")
            }
            append("\n\nTu le sais sûrement, ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("le 1er septembre 2026")
            }
            append(", le gouvernement oblige l'inscription sur une « Plateforme Agréée » (PA) pour pouvoir ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("recevoir des factures.")
            }
            append("\n\nCela te concerne si tu reçois des factures de professionnels, tes fournisseurs par exemple.")
        }
        Spacer(Modifier.height(24.dp))
        PrimaryButton("En savoir plus") { onGo(EInvoiceStep.Solution) }
        Spacer(Modifier.height(8.dp))
        SecondaryButton("J'ai déjà une PA") { onGo(EInvoiceStep.DejaEquipe) }
        Spacer(Modifier.height(4.dp))
        TertiaryButton("Je ne suis pas concerné·e") { onDismiss() }
        TertiaryButton("Je suis dégoûté·e \uD83D\uDE22") { onGo(EInvoiceStep.Compatis) }
    }
}

@Composable
private fun DejaEquipeStep(onGo: (EInvoiceStep) -> Unit, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StepMascot("( っ˶´ ˘ `)っ")
        Spacer(Modifier.height(24.dp))
        StepTitle("Top, tu es à l\'heure !")
        Spacer(Modifier.height(24.dp))
        StepBody(
            "Si tu veux, je peux quand même te parler de la plateforme " +
                "que je recommande, et de ce qui est prévu du côté de l'appli ?"
        )
        Spacer(Modifier.height(24.dp))
        PrimaryButton("Avec plaisir") { onGo(EInvoiceStep.Solution) }
        Spacer(Modifier.height(8.dp))
        SecondaryButton("Non merci, a+ !") { onDismiss() }
    }
}

@Composable
private fun CompatisStep(onGo: (EInvoiceStep) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StepMascot("( っ˶´   `)っ")
        Spacer(Modifier.height(24.dp))
        StepTitle("Je compatis…")
        Spacer(Modifier.height(24.dp))
        StepBody(
            "Une vraie mouise, partager nos données avec un opérateur privé pour se " +
                "« mettre en conformité »… Bon, on se console comme on peut : " +
                "on peut s'en sortir avec 2€."
        )
        Spacer(Modifier.height(24.dp))
        PrimaryButton("Ah bon ?") { onGo(EInvoiceStep.Solution) }
        Spacer(Modifier.height(8.dp))
        SecondaryButton("Tu mens") { onGo(EInvoiceStep.Solution) }
    }
}

@Composable
private fun SolutionStep(onGo: (EInvoiceStep) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StepMascot("( ´・・)ﾉ")
        Spacer(Modifier.height(24.dp))
        StepAnnotatedBody {
            append("Il existe de nombreuses PAs et c'est dur de savoir qui choisir. \n\n En l'absence de plateforme libre sur le marché, je t'en recommande une qui est française, a ses serveurs en région parisienne, et dont le support est rapide et humain. Elle est")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(" gratuite") }
            append(" (sauf 2€ pour la création du compte) et ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("sans engagement.* ") }
            append("\n\n La réception et l'émission sont comprises.")
            append("\n\n Le modèle économique repose sur le volume : le service devient payant à partir de 1000 factures/mois (et les appels API - utilisés par d'autres outils pour se \"brancher\" - le sont aussi).")

        }
        Spacer(Modifier.height(24.dp))
        PrimaryButton("C'est qui c'est qui") { onGo(EInvoiceStep.SuperPDP) }
        Spacer(Modifier.height(8.dp))
        SecondaryButton("Trop de suspense") { onGo(EInvoiceStep.SuperPDP) }
    }
}

@Composable
private fun SuperPDPStep(onGo: (EInvoiceStep) -> Unit, onOpenUrl: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StepMascot("( ´・ ֊ ・)ﾉ")
        Spacer(Modifier.height(24.dp))
        StepTitle("La PA SuperPDP")
        Spacer(Modifier.height(24.dp))
        StepAnnotatedBody {

            append("C'est l'une des 2 plateformes sélectionnées par le projet ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("PDPLibre")
            }
            append(" dont g8 fait partie.")
            append("\n\n Je te laisse te renseigner sur leur site si cela t'intéresse. Tu peux notamment lire la politique de confidentialité.")
        }
        Spacer(Modifier.height(16.dp))
        TertiaryButton("Ouvrir superpdp.tech") { onOpenUrl("https://www.superpdp.tech/") }
        TertiaryButton("Ouvrir pdplibre.org") { onOpenUrl("https://pdplibre.org/") }
        Spacer(Modifier.height(8.dp))
        PrimaryButton("Et g8 dans tout ça ?") { onGo(EInvoiceStep.CoteG8) }
    }
}

@Composable
private fun CoteG8Step(onGo: (EInvoiceStep) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StepMascot("( ´・・)⎠ ➜ ✉ ")
        Spacer(Modifier.height(24.dp))
        StepTitle("Du côté de g8")
        Spacer(Modifier.height(24.dp))
        StepAnnotatedBody {
            append("g8 se concentre sur ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("l'émission de factures électroniques") }
            append(", qui entrera en vigueur en ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("septembre 2027") }
            append(". Tu trouveras très bientôt dans le gStore un module d'")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("export de Factur-X") }
            append(" (un des formats standards) et un ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("connecteur SuperPDP") }
            append(".")
            append("\n\nSi à ce moment-là tu as déjà un compte sur SuperPDP, tu pourras ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("te connecter directement.") }
            append(" \n\n Sinon, tu pourras ouvrir un compte (plusieurs PAs par personne sont autorisées) ou exporter les Factur-X et les importer dans ta PA.")
        }
        Spacer(Modifier.height(24.dp))
        PrimaryButton("OK") { onGo(EInvoiceStep.Infos) }
        Spacer(Modifier.height(8.dp))
        SecondaryButton("Cool") { onGo(EInvoiceStep.Infos) }
    }
}

@Composable
private fun InfosStep(
    onDismiss: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onComposeEmail: (String, String, String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StepMascot("(ヽ(❤ ᴗ ❤ )ノ)")
        Spacer(Modifier.height(24.dp))
        StepTitle("\uD835\uDD53\uD835\uDD5D\uD835\uDD56\uD835\uDD64\uD835\uDD64 \uD835\uDD66\uD835\uDD61 ")
        Spacer(Modifier.height(24.dp))
        Spacer(Modifier.height(16.dp))
        TertiaryButton("Retrouver les infos sur www.the-gate.fr") {
            onOpenUrl("https://www.the-gate.fr/facturation-electronique")
        }
        TertiaryButton("Écrire à g8") {
            onComposeEmail("contact@the-gate.fr", "[g8] ", "")
        }
        Spacer(Modifier.height(8.dp))
        PrimaryButton("Fermer") { onDismiss() }
    }
}

