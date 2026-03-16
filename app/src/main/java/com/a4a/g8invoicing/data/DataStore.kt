package com.a4a.g8invoicing.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("settings")

// Version actuelle de l'app (à mettre à jour à chaque release)
const val CURRENT_APP_VERSION = "1.6"

object PrefKeys {
    val HAS_SEEN_POPUP = booleanPreferencesKey("has_seen_popup")
    val LAST_SEEN_VERSION = stringPreferencesKey("last_seen_version")
}

// Écrire le flag (pour la popup d'export DB)
suspend fun setSeenDbExportPopup(context: Context) {
    context.dataStore.edit { prefs ->
        prefs[PrefKeys.HAS_SEEN_POPUP] = true
    }
}

// Vérifier si on doit afficher les nouveautés (mise à jour uniquement, pas nouvelle installation)
fun shouldShowWhatsNew(context: Context) =
    context.dataStore.data.map { prefs ->
        val lastSeenVersion = prefs[PrefKeys.LAST_SEEN_VERSION]
        val hasSeenPopup = prefs[PrefKeys.HAS_SEEN_POPUP] ?: false

        when {
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
    }
}