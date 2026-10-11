package com.a4a.g8invoicing.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import g8invoicing.ClientOrIssuerQueries
import g8invoicing.DeliveryNoteQueries
import g8invoicing.InvoiceQueries
import g8invoicing.ProductQueries
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("settings")

// Version actuelle de l'app (à mettre à jour à chaque release)
const val CURRENT_APP_VERSION = "2.1"

object PrefKeys {
    val HAS_SEEN_POPUP = booleanPreferencesKey("has_seen_popup")
    val LAST_SEEN_VERSION = stringPreferencesKey("last_seen_version")
    // 1.8 onboarding (multi-step wizard: Devis intro + Factur-X prep). One-shot,
    // per install. Not tied to LAST_SEEN_VERSION because we may need to re-show
    // a v1.9 wizard later without re-triggering this one.
    val HAS_SEEN_ONBOARDING_1_8 = booleanPreferencesKey("has_seen_onboarding_1_8")
    // 1.8.1 e-invoicing intro popup (PR speech-bubble flow about the Sept 2026
    // PA obligation). One-shot, gated on FR locale by the caller. Independent
    // from LAST_SEEN_VERSION because the standard WhatsNew flow shouldn't
    // fire again for users updating from 1.8 → 1.8.1.
    val HAS_SEEN_EINVOICE_INTRO = booleanPreferencesKey("has_seen_einvoice_intro")
    // 2.0 dark-mode preference. Absent = follow the system setting. Present
    // = user has explicitly picked light (false) or dark (true) via the
    // Apparence toggle in Mon Compte and that choice overrides the system.
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    // 2.0 dark-mode migration prompt — one-shot, upgraders only. Fresh
    // installs flip this at the same time as HAS_SEEN_POPUP (see
    // initializeVersionTracking).
    val HAS_SEEN_DARKMODE_MIGRATION_20 = booleanPreferencesKey("has_seen_darkmode_migration_20")
}

// Dark mode preference — emits null when the user hasn't explicitly chosen
// (fall back to isSystemInDarkTheme() at read time), or the stored boolean
// once the Apparence toggle has been used.
fun darkModePreference(context: Context) =
    context.dataStore.data.map { prefs -> prefs[PrefKeys.DARK_MODE] }

suspend fun setDarkMode(context: Context, enabled: Boolean) {
    context.dataStore.edit { prefs ->
        prefs[PrefKeys.DARK_MODE] = enabled
    }
}

// Fires for anyone whose HAS_SEEN_DARKMODE_MIGRATION_20 is not yet true.
// Can't gate on LAST_SEEN_VERSION like the other "fire on upgrade" prompts
// because the 1.9 migration wizard writes LAST_SEEN_VERSION = CURRENT_APP_VERSION
// in its markSeen block (via setSeenOnboarding18), which would mask the
// upgrade signal for users crossing 1.x → 2.0.
//
// Fresh installs still need to be excluded though — the Flow collector can
// emit before initializeVersionTracking's eager setSeenDarkModeMigration20
// write propagates. Guard by also requiring a prior-session signal
// (LAST_SEEN_VERSION tracked, or legacy HAS_SEEN_POPUP touched). A real
// fresh install has neither at boot and the modal stays silent.
fun shouldShowDarkModeMigration20(context: Context) =
    context.dataStore.data.map { prefs ->
        val hasSeen = prefs[PrefKeys.HAS_SEEN_DARKMODE_MIGRATION_20] ?: false
        if (hasSeen) return@map false
        val lastSeenVersion = prefs[PrefKeys.LAST_SEEN_VERSION]
        val hasSeenPopup = prefs[PrefKeys.HAS_SEEN_POPUP] ?: false
        lastSeenVersion != null || hasSeenPopup
    }

suspend fun setSeenDarkModeMigration20(context: Context) {
    context.dataStore.edit { prefs ->
        prefs[PrefKeys.HAS_SEEN_DARKMODE_MIGRATION_20] = true
    }
}

// Écrire le flag (pour la popup d'export DB)
suspend fun setSeenDbExportPopup(context: Context) {
    context.dataStore.edit { prefs ->
        prefs[PrefKeys.HAS_SEEN_POPUP] = true
    }
}

// QA helper — uncomment both lines to force the What's New popup on every launch
// (useful when testing the dialog without bumping CURRENT_APP_VERSION).
// private const val FORCE_SHOW_WHATS_NEW_FOR_QA = true

// Vérifier si on doit afficher les nouveautés (mise à jour uniquement, pas nouvelle installation)
fun shouldShowWhatsNew(context: Context) =
    context.dataStore.data.map { prefs ->
        val lastSeenVersion = prefs[PrefKeys.LAST_SEEN_VERSION]
        val hasSeenPopup = prefs[PrefKeys.HAS_SEEN_POPUP] ?: false

        when {
            // FORCE_SHOW_WHATS_NEW_FOR_QA -> true
            // Version déjà vue → ne pas afficher
            lastSeenVersion == CURRENT_APP_VERSION -> false
            // Version précédente existe et différente → mise à jour → afficher
            lastSeenVersion != null -> true
            // lastSeenVersion null mais a déjà utilisé l'app → mise à jour depuis ancienne version → afficher
            hasSeenPopup -> true
            // Nouvelle installation → ne pas afficher
            else -> false
        }
    }

// Marquer les nouveautés comme vues (aussi appelé à la première utilisation pour les nouvelles installations)
suspend fun setSeenWhatsNew(context: Context) {
    context.dataStore.edit { prefs ->
        prefs[PrefKeys.LAST_SEEN_VERSION] = CURRENT_APP_VERSION
    }
}

// Initialiser le suivi de version pour les nouvelles installations uniquement
// Si lastSeenVersion est null ET hasSeenPopup est false → nouvelle installation → enregistrer la version
// Si lastSeenVersion est null ET hasSeenPopup est true → mise à jour depuis ancienne version → ne rien faire
suspend fun initializeVersionTracking(context: Context) {
    val prefs = context.dataStore.data.first()
    val lastSeenVersion = prefs[PrefKeys.LAST_SEEN_VERSION]
    val hasSeenPopup = prefs[PrefKeys.HAS_SEEN_POPUP] ?: false

    // Seulement pour les vraies nouvelles installations. Les trois writes
    // partagent le même .edit {} pour éviter la race que les versions
    // précédentes voyaient : chaque .edit {} fait émettre le Flow, et le
    // moindre interval où LAST_SEEN_VERSION est écrit avant
    // HAS_SEEN_DARKMODE_MIGRATION_20 déclenche la modale "Mode sombre !"
    // (shouldShowDarkModeMigration20 interprète lastSeenVersion != null
    // comme un upgrade). Un write atomique fait émettre un seul snapshot.
    //
    // NB: HAS_SEEN_ONBOARDING_1_8 is intentionally NOT set here anymore.
    // The onboarding must run for fresh installs too (with the empty-issuer
    // path skipping the per-issuer loop) so a user quitting via the Home
    // button during Welcome can resume the wizard on next launch. The flag
    // is only flipped once the user reaches Terminer (see commit()).
    if (lastSeenVersion == null && !hasSeenPopup) {
        context.dataStore.edit { prefs ->
            prefs[PrefKeys.LAST_SEEN_VERSION] = CURRENT_APP_VERSION
            // E-invoice popup cible les upgrades depuis 1.8/antérieur :
            // pour les fresh installs on flip SEEN=true directement.
            prefs[PrefKeys.HAS_SEEN_EINVOICE_INTRO] = true
            prefs[PrefKeys.HAS_SEEN_DARKMODE_MIGRATION_20] = true
        }
    }
}

// 1.8 onboarding trigger: show when upgrading from a previous version (or a
// pre-tracking legacy install) AND the user has not completed the flow yet.
// Fresh installs are marked as seen in initializeVersionTracking → they skip.
fun shouldShowOnboarding18(context: Context) =
    context.dataStore.data.map { prefs ->
        val hasSeenOnboarding = prefs[PrefKeys.HAS_SEEN_ONBOARDING_1_8] ?: false
        if (hasSeenOnboarding) return@map false

        val lastSeenVersion = prefs[PrefKeys.LAST_SEEN_VERSION]
        val hasSeenPopup = prefs[PrefKeys.HAS_SEEN_POPUP] ?: false
        when {
            // Upgrade case: prior version tracked, and it is not 1.8.
            lastSeenVersion != null && lastSeenVersion != CURRENT_APP_VERSION -> true
            // Legacy upgrade: no version tracked but the app was used before.
            lastSeenVersion == null && hasSeenPopup -> true
            else -> false
        }
    }

// 1.8.1 e-invoicing intro popup. Fires once, only if not seen. Locale gating
// (FR only) is applied by the caller — DataStore.kt is Android-side and has
// no access to AppLocaleHolder.
fun shouldShowEInvoiceIntro(context: Context) =
    context.dataStore.data.map { prefs ->
        prefs[PrefKeys.HAS_SEEN_EINVOICE_INTRO] != true
    }

suspend fun setSeenEInvoiceIntro(context: Context) {
    context.dataStore.edit { prefs ->
        prefs[PrefKeys.HAS_SEEN_EINVOICE_INTRO] = true
    }
}

suspend fun setSeenOnboarding18(context: Context) {
    context.dataStore.edit { prefs ->
        prefs[PrefKeys.HAS_SEEN_ONBOARDING_1_8] = true
        // Onboarding already includes a "Sauvegarder ma base de données" CTA
        // in the Factur-X intro step, so silently mark the standalone backup
        // popup as seen — no need to nag the same reminder twice.
        prefs[PrefKeys.HAS_SEEN_POPUP] = true
        // Same rationale for the What's New modal: the 1.8 onboarding covers
        // every nouveauté the What's New would announce. Without this write
        // LAST_SEEN_VERSION stayed at "1.7" and the standard WhatsNew popped
        // right after the user finished the wizard.
        prefs[PrefKeys.LAST_SEEN_VERSION] = CURRENT_APP_VERSION
    }
}

// Flip HAS_SEEN_ONBOARDING_1_8 back to false so the wizard re-fires on the
// next boot. Called from MainCompose when RestoreManager reports a restore
// from a pre-1.8 backup — the restored clients have no country_code and the
// wizard's "clients tous dans le même pays ?" step is how we bulk-fix them.
suspend fun resetOnboarding18Seen(context: Context) {
    context.dataStore.edit { prefs ->
        prefs[PrefKeys.HAS_SEEN_ONBOARDING_1_8] = false
    }
}

// Backup reminder popup — surfaced once when the user has accumulated
// enough real data (>3 rows in any of the main tables) and hasn't seen the
// nudge yet. Broken since the KMP migration (f9996022cb2) moved InvoiceList
// to shared/ and dropped the wiring; restored here in MainCompose since the
// dialog itself uses Android file APIs and can't move to commonMain.
suspend fun shouldShowBackupPopupNow(
    context: Context,
    invoiceQueries: InvoiceQueries,
    deliveryNoteQueries: DeliveryNoteQueries,
    productQueries: ProductQueries,
    clientOrIssuerQueries: ClientOrIssuerQueries,
): Boolean {
    val hasSeen = context.dataStore.data.first()[PrefKeys.HAS_SEEN_POPUP] ?: false
    if (hasSeen) return false
    return invoiceQueries.countAll().executeAsOne() > 3 ||
        deliveryNoteQueries.countAll().executeAsOne() > 3 ||
        productQueries.countAll().executeAsOne() > 3 ||
        clientOrIssuerQueries.countAll().executeAsOne() > 3
}