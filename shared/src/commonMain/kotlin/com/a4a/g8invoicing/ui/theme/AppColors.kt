package com.a4a.g8invoicing.ui.theme

import androidx.compose.ui.graphics.Color

// Semantic color tokens. Every UI element (icon tint, background, border,
// button colour) references a name from here — never a raw hex or a token
// from the palette below. Text colours are baked into the Typography tokens
// so call sites just set `style = MaterialTheme.typography.textBody`.
//
// Adding a colour: pick a role name (what it's for), not a hue name.
// If no existing role fits, add a new one here rather than reaching into
// the raw palette in Color.kt.
object AppColors {

    // Surfaces / backgrounds
    val screen        = Color(0xFFF9F9F9)   // full-screen background
    val surface       = Color(0xFFFFFFFF)   // cards, form blocks, sheets
    val surfaceMuted  = Color(0xFFEEEEEE)   // list row alt, greyed panels
    val bottomBar     = Color(0xFFC1C1C1)   // navigation bar background

    // Text (referenced by Typography tokens — rarely used directly)
    val textPrimary   = Color(0xFF1A1A1A)
    val textSecondary = Color(0xFF7D7B77)
    val textDisabled  = Color(0x8A57544F)
    val textOnAccent  = Color(0xFFFFFFFF)
    val textLink      = Color(0xFF932092)

    // Icons
    val iconPrimary   = Color(0xFF57544F)   // solid dark icons (delete, edit)
    val iconSecondary = Color(0xFF7D7B77)   // chevrons, search, help
    val iconDisabled  = Color(0x3057544F)

    // Borders / dividers
    val divider       = Color(0xFFE3E3E3)
    val borderInput   = Color(0xFFE3E3E3)

    // Actions / accent
    val accent        = Color(0xFF932092)   // brand violet (buttons, links)
    val buttonActive  = Color(0xFF932092)
    val buttonDisabled= Color(0xFFC1C1C1)

    // Document statuses — already role-named, kept as-is
    val statusDraft     = Color(0xFFC1C1C1)
    val statusSent      = Color(0xFF3B97D3)
    val statusPaid      = Color(0xFF09D981)
    val statusLate      = Color(0xFFFC5A58)
    val statusReminded  = Color(0xFFF374AE)
    val statusCancelled = Color(0xFFFFF4CC)
}
