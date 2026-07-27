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
    val surfaceSubtle = Color(0xFFF6F5F2)   // list container behind rows — warm, barely tinted
    val surfaceMuted  = Color(0xFFEEEEEE)   // greyed panels, alt-row backgrounds
    val bottomBar     = Color(0xFFC1C1C1)   // navigation bar background

    // Text (referenced by Typography tokens — rarely used directly)
    val textPrimary   = Color(0xFF1A1A1A)
    val textSecondary = Color(0xFF57544F)   // warm dark grey — WCAG AA on white (6.5:1)
    val textMuted     = Color(0xFF7D7B77)   // greyed-out / inactive text (cancelled invoices…)
    val textPale      = Color(0xFF9C9A96)   // discreet section headers — darker than the very-pale statusDraft so it still reads on a light background
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

    // Document statuses — shared between the tag pill (list item + AppBar dropdown)
    // and the invoice list row text (price + status label). Reminded / draft / sent
    // use muted "workflow off" hues so they don't compete with paid/late for
    // attention; paid / late are the strong signals.
    val statusDraft     = Color(0xFFB0AEAA)   // muted grey
    val statusSent      = Color(0xFF7E96B8)   // dusty blue
    val statusPaid      = Color(0xFF12B76A)   // green
    val statusLate      = Color(0xFFDC2A2A)   // red
    val statusReminded  = Color(0xFFBC8FB0)   // muted mauve
    val statusCancelled = Color(0xFFFFF4CC)   // pale yellow — kept but overridden as
                                              // transparent-fill + grey outline in
                                              // DocumentListItem (see cancelled block)
    val statusUrgent    = Color(0xFFF59E0B)   // amber — countdown text when the due
                                              // date is 5 days out or less
}
