package com.a4a.g8invoicing.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Semantic color tokens. Every UI element (icon tint, background, border,
// button colour) references a name from here — never a raw hex or a token
// from the palette below. Text colours are baked into the Typography tokens
// so call sites just set `style = MaterialTheme.typography.textBody`.
//
// Adding a colour: pick a role name (what it's for), not a hue name.
// If no existing role fits, add a new one here rather than reaching into
// the raw palette in Color.kt.
//
// Theme-aware: call sites read `AppColors.screen` (the top-level val below)
// which resolves through LocalAppColors at composition time — so light and
// dark schemes swap automatically. Non-composable code paths that used to
// read AppColors directly must either become @Composable or take the token
// as a parameter.
data class AppColorScheme(
    // Surfaces / backgrounds
    val screen: Color,
    val surface: Color,
    val surfaceSubtle: Color,
    val surfaceMuted: Color,
    val bottomBar: Color,
    // Filled input container (TextField / search bar background). Distinct
    // from surfaceMuted so search-field visuals can evolve without dragging
    // list-row surfaces along.
    val inputSurface: Color,
    // Ripple ink used when tapping list rows / clickable surfaces. Must be
    // dark on light theme and light on dark theme, otherwise the ripple
    // washes out against the surface it's drawn on.
    val ripple: Color,
    // Switch off-state colours. Thumb + track both go softer on dark theme
    // so the "off" toggle doesn't glare white on a near-black bar.
    val switchUncheckedThumb: Color,
    val switchUncheckedTrack: Color,
    val switchUncheckedTrackLocked: Color,
    // Switch on-state track colour. Kept separate from `accent` because a
    // solid Switch fill wants a stronger violet than a text link — the soft
    // `accent` used for links reads as washed-out on a checked toggle.
    val switchCheckedTrack: Color,

    // Text (referenced by Typography tokens — rarely used directly)
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textPale: Color,
    val textDisabled: Color,
    val textOnAccent: Color,
    val textLink: Color,

    // Icons
    val iconPrimary: Color,
    val iconSecondary: Color,
    val iconDisabled: Color,

    // Borders / dividers
    val divider: Color,
    val borderInput: Color,
    val separatorForm: Color,

    // Actions / accent
    val accent: Color,
    val buttonActive: Color,
    val buttonDisabled: Color,
    val dangerAccent: Color,

    // Document statuses — kept identical across themes so status pills read
    // the same in light and dark (invoice status is a strong signal, not a
    // theme token).
    val statusDraft: Color,
    val statusSent: Color,
    val statusPaid: Color,
    val statusLate: Color,
    val statusReminded: Color,
    val statusCancelled: Color,

    // Document / paper rendering — FIXED across themes. The invoice preview
    // is a paper mock: it must stay white with dark ink in dark mode too
    // so what the user sees matches what the PDF exports. Read these tokens
    // (not `surface` / `textPrimary`) from every Composable that draws the
    // rendered document.
    val documentPaper: Color,
    val documentInk: Color,
    val documentInkSecondary: Color,
    val documentDivider: Color,

    // Theme flag. Read when a Composable needs to branch on light vs. dark
    // (e.g. render a mascot halo only on dark surfaces).
    val isDark: Boolean,
)

val LightAppColors = AppColorScheme(
    screen        = Color(0xFFF9F9F9),
    surface       = Color(0xFFFFFFFF),
    surfaceSubtle = Color(0xFFF5F5F5),
    surfaceMuted  = Color(0xFFEEEEEE),
    bottomBar     = Color(0xFFC1C1C1),
    inputSurface  = Color(0xFFF0F0F0),   // matches the legacy TopBar SearchFieldGrey
    ripple        = Color(0xFF000000),   // preserves the legacy pure-black ripple ink
    switchUncheckedThumb       = Color(0xFFFFFFFF),  // unchanged from legacy Switch defaults
    switchUncheckedTrack       = Color(0xFFE5E2E7),  // matches the gStore locked-off track
    switchUncheckedTrackLocked = Color(0xFFB8B5BC),  // matches the gStore unlocked-off track
    switchCheckedTrack         = Color(0xFF932092),  // legacy on-state violet, matches ColorVioletLink

    textPrimary   = Color(0xFF1A1A1A),
    textSecondary = Color(0xFF57544F),   // warm dark grey — WCAG AA on white (6.5:1)
    textMuted     = Color(0xFF7D7B77),
    textPale      = Color(0xFF9C9A96),
    textDisabled  = Color(0x8A57544F),
    textOnAccent  = Color(0xFFFFFFFF),
    textLink      = Color(0xFF932092),

    iconPrimary   = Color(0xFF57544F),
    iconSecondary = Color(0xFF7D7B77),
    iconDisabled  = Color(0x3057544F),

    divider       = Color(0xFFE3E3E3),
    borderInput   = Color(0xFFE3E3E3),
    separatorForm = Color(0xFFE5E5E5),

    accent        = Color(0xFF932092),
    buttonActive  = Color(0xFF932092),
    buttonDisabled= Color(0xFFC1C1C1),
    dangerAccent  = Color(0xFFB71C1C),

    statusDraft     = Color(0xFFB0AEAA),
    statusSent      = Color(0xFF7E96B8),
    statusPaid      = Color(0xFF12B76A),
    statusLate      = Color(0xFFDC2A2A),
    statusReminded  = Color(0xFFBC8FB0),
    statusCancelled = Color(0xFFFFF4CC),

    documentPaper       = Color(0xFFFFFFFF),
    documentInk         = Color(0xFF1A1A1A),
    documentInkSecondary= Color(0xFF57544F),
    documentDivider     = Color(0xFFE3E3E3),

    isDark = false,
)

// Dark palette — mirrors the light role structure. Surfaces stay near-black
// with layered greys for cards and inputs; text inverts to off-white. Accent
// / status hues keep their brand identity (violet stays violet, red stays
// red) so the app still feels like the same product, just at night.
val DarkAppColors = AppColorScheme(
    // "Violet whisper" palette (Option B) — very subtle violet cast on the
    // dark surfaces so the app feels connected to the brand without turning
    // into a stylised dark theme. All values sit around HSL(270°, ~10%),
    // layered by lightness: screen (base) < surface (card) < surfaceSubtle
    // (list row) < surfaceMuted (greyed panel).
    screen        = Color(0xFF1E1B21),
    surface       = Color(0xFF272229),
    surfaceSubtle = Color(0xFF2E2830),
    surfaceMuted  = Color(0xFF35303B),
    bottomBar     = Color(0xFF0A0A0A),   // matches existing ColorDarkBar (never swaps)
    inputSurface  = Color(0xFF2E2830),   // matches surfaceSubtle
    ripple        = Color(0xFFFFFFFF),   // keeps the master value for dark mode — Material3 applies its own alpha on top; smoothness is handled by switching unbounded list ripples to bounded (see call sites)
    switchUncheckedThumb       = Color(0xFF6E6B67),  // muted grey so an "off" toggle doesn't glare
    switchUncheckedTrack       = Color(0xFF2F2F2F),
    switchUncheckedTrackLocked = Color(0xFF3A3A3A),
    switchCheckedTrack         = Color(0xFF7A2179),  // deeper brand violet — reads solid without glaring

    textPrimary   = Color(0xFFDEDAE0),   // 87% off-white with a whisper of violet to match the surface palette; softer on the eye than pure #FFFFFF or 92%-white
    textSecondary = Color(0xFFB8B5B0),   // warm off-white for secondary reads
    textMuted     = Color(0xFF8A8783),
    textPale      = Color(0xFF6E6B67),
    textDisabled  = Color(0x8AB8B5B0),
    textOnAccent  = Color(0xFFFFFFFF),   // still white on the violet accent
    // Unified soft violet for dark mode. Used for both text links and
    // button accents so the same brand hue appears everywhere and no
    // strong `#932092` slips through to burn on the near-black surface.
    textLink      = Color(0xFFD48ED3),

    iconPrimary   = Color(0xFFB8B5B0),
    iconSecondary = Color(0xFF8A8783),
    iconDisabled  = Color(0x30B8B5B0),

    divider       = Color(0xFF2B2530),
    borderInput   = Color(0xFF3E3944),
    separatorForm = Color(0xFF3A3540),

    accent        = Color(0xFFD48ED3),   // matches textLink — one violet everywhere
    buttonActive  = Color(0xFFD48ED3),
    buttonDisabled= Color(0xFF3A3A3A),
    dangerAccent  = Color(0xFFEF5350),   // Material Red 400 — softer on dark

    // Status hues left untouched — an invoice paid is green regardless of theme.
    statusDraft     = Color(0xFFB0AEAA),
    statusSent      = Color(0xFF7E96B8),
    statusPaid      = Color(0xFF12B76A),
    statusLate      = Color(0xFFDC2A2A),
    statusReminded  = Color(0xFFBC8FB0),
    statusCancelled = Color(0xFFFFF4CC),

    // Paper stays white in dark mode — WYSIWYG with the PDF export.
    documentPaper       = Color(0xFFFFFFFF),
    documentInk         = Color(0xFF1A1A1A),
    documentInkSecondary= Color(0xFF57544F),
    documentDivider     = Color(0xFFE3E3E3),

    isDark = true,
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

// Shim so existing call sites (`AppColors.screen`, `AppColors.textPrimary`…)
// keep compiling — they now resolve through the CompositionLocal at read
// time. Only usable from a @Composable context.
val AppColors: AppColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current
