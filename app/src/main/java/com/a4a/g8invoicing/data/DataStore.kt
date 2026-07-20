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
const val CURRENT_APP_VERSION = "1.8"

object PrefKeys {
    val HAS_SEEN_POPUP = booleanPreferencesKey("has_seen_popup")
    val LAST_SEEN_VERSION = stringPreferencesKey("last_seen_version")
    // 1.8 onboarding (multi-step wizard: Devis intro + Factur-X prep). One-shot,
    // per install. Not tied to LAST_SEEN_VERSION because we may need to re-show
    // a v1.9 wizard later without re-triggering this one.
    val HAS_SEEN_ONBOARDING_1_8 = booleanPreferencesKey("has_seen_onboarding_1_8")
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

    // Seulement pour les vraies nouvelles installations
    if (lastSeenVersion == null && !hasSeenPopup) {
        setSeenWhatsNew(context)
        // NB: HAS_SEEN_ONBOARDING_1_8 is intentionally NOT set here anymore.
        // The onboarding must run for fresh installs too (with the empty-issuer
        // path skipping the per-issuer loop) so a user quitting via the Home
        // button during Welcome can resume the wizard on next launch. The flag
        // is only flipped once the user reaches Terminer (see commit()).
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

suspend fun setSeenOnboarding18(context: Context) {
    context.dataStore.edit { prefs ->
        prefs[PrefKeys.HAS_SEEN_ONBOARDING_1_8] = true
        // Onboarding already includes a "Sauvegarder ma base de données" CTA
        // in the Factur-X intro step, so silently mark the standalone backup
        // popup as seen — no need to nag the same reminder twice.
        prefs[PrefKeys.HAS_SEEN_POPUP] = true
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