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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.a4a.g8invoicing.data.models.CountryCodes
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.first_launch_completion_title
import com.a4a.g8invoicing.shared.resources.first_launch_gstore_intro
import com.a4a.g8invoicing.shared.resources.first_launch_gstore_premium
import com.a4a.g8invoicing.shared.resources.first_launch_issuer_default_name
import com.a4a.g8invoicing.shared.resources.first_launch_issuer_placeholder
import com.a4a.g8invoicing.shared.resources.first_launch_issuer_skip
import com.a4a.g8invoicing.shared.resources.first_launch_liberty_body1
import com.a4a.g8invoicing.shared.resources.first_launch_liberty_body2
import com.a4a.g8invoicing.shared.resources.first_launch_next
import com.a4a.g8invoicing.shared.resources.first_launch_settings_country_label
import com.a4a.g8invoicing.shared.resources.first_launch_settings_cta
import com.a4a.g8invoicing.shared.resources.first_launch_settings_intro
import com.a4a.g8invoicing.shared.resources.first_launch_settings_name_label
import com.a4a.g8invoicing.shared.resources.first_launch_welcome_amigo
import com.a4a.g8invoicing.shared.resources.first_launch_welcome_title
import com.a4a.g8invoicing.shared.resources.onboarding_issuer_country_pick
import com.a4a.g8invoicing.shared.resources.onboarding_previous
import com.a4a.g8invoicing.ui.screens.shared.CountryPicker
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.shared.animations.BatSmilingEyes
import com.a4a.g8invoicing.ui.theme.textBody
import com.a4a.g8invoicing.ui.theme.textBodySmall
import com.a4a.g8invoicing.ui.theme.textScreenTitle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * Fresh-install onboarding — 5 slides that funnel the user into a valid
 * first-issuer state before the app proper opens.
 *
 * 1. Welcome — animated rhino + brand line + amigo tag.
 * 2. Liberty — ✌️ + "you can manage everything from your phone" +
 *    open-source / offline reassurance.
 * 3. gStore — 🕹️ + a-la-carte modules pitch.
 * 4. Settings — single form with the 2 configuration fields (business
 *    name + country) and a discreet "Plus tard" link.
 * 5. Completion — confetti burst + "Bonne facturation !" then dismiss.
 *
 * Non-dismissable: the outer app can't render until we return an issuer.
 * Both the country field and the skip link still produce a saved issuer —
 * falling back to "Mon entreprise" for the name and the system country
 * for the locale — because we cannot leave the app without one.
 */
@Composable
fun FirstLaunchIssuerNameDialog(
    onSubmit: suspend (name: String, countryCode: String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val defaultCountry = remember { CountryCodes.pickDefaultForNewAddress(null) }
    val namePlaceholder = stringResource(Res.string.first_launch_issuer_placeholder)
    val nameFallback = stringResource(Res.string.first_launch_issuer_default_name)

    var step by remember { mutableStateOf(Step.Welcome) }
    var name by remember {
        mutableStateOf(TextFieldValue(text = "", selection = TextRange(0)))
    }
    var country by remember { mutableStateOf(defaultCountry) }
    var submitting by remember { mutableStateOf(false) }
    var showCountryPicker by remember { mutableStateOf(false) }

    // The Settings step branches into Completion instead of firing onSubmit
    // directly, so the confetti has time to play. finishOnboarding is the
    // shared exit — called from the completion timer or an early tap.
    val finishOnboarding = {
        if (!submitting) {
            submitting = true
            val trimmedName = name.text.trim().ifBlank { nameFallback }
            scope.launch { onSubmit(trimmedName, country) }
        }
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
                // Top bar — a discreet "Précédent" text button. Hidden on the
                // first slide (nothing to go back to) and on the completion
                // slide (the wizard is already committing).
                TopBar(
                    canGoBack = step != Step.Welcome && step != Step.Completion,
                    onBack = { step = previousStep(step) },
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    when (step) {
                        Step.Welcome -> WelcomeStep(onNext = { step = Step.Liberty })
                        Step.Liberty -> LibertyStep(onNext = { step = Step.Gstore })
                        Step.Gstore -> GstoreStep(onNext = { step = Step.Settings })
                        Step.Settings -> SettingsStep(
                            name = name,
                            onNameChange = { name = it },
                            placeholder = namePlaceholder,
                            country = country,
                            onOpenPicker = { showCountryPicker = true },
                            onSubmit = { step = Step.Completion },
                            onSkip = {
                                name = TextFieldValue(text = "", selection = TextRange(0))
                                country = defaultCountry
                                step = Step.Completion
                            },
                        )
                        Step.Completion -> CompletionStep(onDone = finishOnboarding)
                    }
                }
            }
        }
    }

    if (showCountryPicker) {
        CountryPicker(
            currentCode = country,
            onSelect = { code -> country = code; showCountryPicker = false },
            onDismiss = { showCountryPicker = false },
        )
    }
}

private enum class Step { Welcome, Liberty, Gstore, Settings, Completion }

private fun previousStep(step: Step): Step = when (step) {
    Step.Welcome -> Step.Welcome
    Step.Liberty -> Step.Welcome
    Step.Gstore -> Step.Liberty
    Step.Settings -> Step.Gstore
    // Not reachable — TopBar hides the back button on the completion
    // slide since we're already about to persist and dismiss.
    Step.Completion -> Step.Settings
}

@Composable
private fun TopBar(canGoBack: Boolean, onBack: () -> Unit) {
    // Fixed-height slot so the content below sits at the same vertical
    // position on every slide, whether the back button is visible or not.
    // Extra top + left inset so the button doesn't hug the corner.
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
private fun WelcomeStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LottieMascotSlot { BatSmilingEyes(modifier = Modifier.size(120.dp)) }
        Spacer(Modifier.height(28.dp))
        StepTitle(stringResource(Res.string.first_launch_welcome_title))
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(Res.string.first_launch_welcome_amigo),
            style = MaterialTheme.typography.textBody.copy(color = AppColors.textSecondary),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(40.dp))
        PrimaryCta(
            text = stringResource(Res.string.first_launch_next),
            onClick = onNext,
        )
    }
}

@Composable
private fun LibertyStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LottieMascotSlot { BatSmilingEyes(modifier = Modifier.size(120.dp)) }
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(Res.string.first_launch_liberty_body1),
            style = MaterialTheme.typography.textBody,
            textAlign = TextAlign.Start,
            lineHeight = 24.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(Res.string.first_launch_liberty_body2),
            style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary),
            textAlign = TextAlign.Start,
            lineHeight = 22.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(40.dp))
        PrimaryCta(
            text = stringResource(Res.string.first_launch_next),
            onClick = onNext,
        )
    }
}

@Composable
private fun GstoreStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LottieMascotSlot { BatSmilingEyes(modifier = Modifier.size(120.dp)) }
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(Res.string.first_launch_gstore_intro),
            style = MaterialTheme.typography.textBody,
            textAlign = TextAlign.Start,
            lineHeight = 24.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(Res.string.first_launch_gstore_premium),
            style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary),
            textAlign = TextAlign.Start,
            lineHeight = 22.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(40.dp))
        PrimaryCta(
            text = stringResource(Res.string.first_launch_next),
            onClick = onNext,
        )
    }
}

@Composable
private fun SettingsStep(
    name: TextFieldValue,
    onNameChange: (TextFieldValue) -> Unit,
    placeholder: String,
    country: String,
    onOpenPicker: () -> Unit,
    onSubmit: () -> Unit,
    onSkip: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = stringResource(Res.string.first_launch_settings_intro),
            style = MaterialTheme.typography.textBody,
            textAlign = TextAlign.Start,
            lineHeight = 24.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(28.dp))

        // Field 1 — business name. Compact rounded field that visually
        // matches the country dropdown just below (14dp vertical inset,
        // same violet-tinted background and border) so the two inputs
        // read as siblings, not two different form patterns.
        FieldLabel(stringResource(Res.string.first_launch_settings_name_label))
        Spacer(Modifier.height(6.dp))
        CompactTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = placeholder,
        )
        Spacer(Modifier.height(20.dp))

        // Field 2 — country
        FieldLabel(stringResource(Res.string.first_launch_settings_country_label))
        Spacer(Modifier.height(6.dp))
        CountryDropdownField(
            currentCountry = country,
            placeholder = stringResource(Res.string.onboarding_issuer_country_pick),
            onClick = onOpenPicker,
        )

        Spacer(Modifier.height(36.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            PrimaryCta(
                text = stringResource(Res.string.first_launch_settings_cta),
                onClick = onSubmit,
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            TextButton(
                onClick = onSkip,
                colors = ButtonDefaults.textButtonColors(contentColor = AppColors.textLink),
            ) { Text(stringResource(Res.string.first_launch_issuer_skip)) }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.textBodySmall,
    )
}

@Composable
private fun CompactTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
) {
    val focusManager = LocalFocusManager.current
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.textBody.copy(color = AppColors.textPrimary),
        cursorBrush = SolidColor(AppColors.accent),
        // Done (not Next) so tapping the IME action just closes the keyboard —
        // Next used to fire onSubmit() and skip the user straight past the
        // Country picker to the Completion slide, which surprised users who
        // tapped it expecting to jump into the country dropdown.
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
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
private fun CompletionStep(onDone: () -> Unit) {
    // 5 s confetti burst then a 700 ms fade before the parent pops the
    // dialog. Tapping anywhere on the slide fast-forwards to the fade so
    // an eager user isn't held hostage by the celebration.
    var fadingOut by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (fadingOut) 0f else 1f,
        animationSpec = tween(durationMillis = 700),
        label = "completionFade",
    )
    LaunchedEffect(Unit) {
        delay(5000L)
        fadingOut = true
        delay(700L)
        onDone()
    }
    // A no-indication clickable across the whole slide lets the user tap
    // to fade out early. Idempotent — repeat taps do nothing extra.
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            MascotSlot { AnimatedKaomojiThanks(fontSize = 22.sp, loop = true) }
            Spacer(Modifier.height(28.dp))
            StepTitle(stringResource(Res.string.first_launch_completion_title))
        }
    }
}

// ============================================================================
// Reusable UI bits
// ============================================================================

/** Fixed-height slot for the mascot so text below doesn't shift when the
 *  frame widths vary between animation steps. Mirrors OnboardingDialog.MascotSlot. */
@Composable
private fun MascotSlot(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) { content() }
}

/** Slot for the Lottie rhino illustration used on the info slides. Bigger
 *  than the kaomoji [MascotSlot] because the Lottie needs room to breathe. */
@Composable
private fun LottieMascotSlot(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .height(120.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) { content() }
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
private fun CountryDropdownField(
    currentCountry: String?,
    placeholder: String,
    onClick: () -> Unit,
) {
    val label = currentCountry?.takeIf { it.isNotBlank() }
        ?.let { CountryCodes.displayNameOf(it) }
        ?: placeholder
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
            modifier = Modifier.padding(end = 8.dp),
            style = MaterialTheme.typography.textBody.copy(
                color = if (currentCountry != null) AppColors.textPrimary else AppColors.textSecondary,
            ),
        )
        Spacer(Modifier.fillMaxWidth().weight(1f))
        Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            contentDescription = null,
            tint = AppColors.accent,
        )
    }
}

@Composable
private fun PrimaryCta(
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
