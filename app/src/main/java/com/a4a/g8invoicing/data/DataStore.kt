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

// Lire le flag (pour la popup d'export DB)
fun hasSeenPopup(context: Context) =
    context.dataStore.data.map { prefs ->
        prefs[PrefKeys.HAS_SEEN_POPUP] ?: false
    }

// Écrire le flag (pour la popup d'export DB)
suspend fun setSeenPopup(context: Context) {
    context.dataStore.edit { prefs ->
        prefs[PrefKeys.HAS_SEEN_POPUP] = true
    }
}

// Vérifier si on doit afficher les nouveautés (mise à jour uniquement, pas nouvelle installation)
fun shouldShowWhatsNew(context: Context) =
    context.dataStore.data.map { prefs ->
        val lastSeenVersion = prefs[PrefKeys.LAST_SEEN_VERSION]
        // Afficher seulement si : version précédente existe ET est différente de la version actuelle
        lastSeenVersion != null && lastSeenVersion != CURRENT_APP_VERSION
    }

// Vérifier si l'utilisateur a vu les nouveautés de la version actuelle
fun hasSeenWhatsNew(context: Context) =
    context.dataStore.data.map { prefs ->
        prefs[PrefKeys.LAST_SEEN_VERSION] == CURRENT_APP_VERSION
    }

// Marquer les nouveautés comme vues (aussi appelé à la première utilisation pour les nouvelles installations)
suspend fun setSeenWhatsNew(context: Context) {
    context.dataStore.edit { prefs ->
        prefs[PrefKeys.LAST_SEEN_VERSION] = CURRENT_APP_VERSION
    }
}

// Initialiser le suivi de version pour les nouvelles installations
// Si lastSeenVersion est null, c'est une nouvelle installation → on enregistre la version actuelle
// Ainsi, lors de la prochaine mise à jour, shouldShowWhatsNew retournera true
suspend fun initializeVersionTracking(context: Context) {
    val prefs = context.dataStore.data.first()
    if (prefs[PrefKeys.LAST_SEEN_VERSION] == null) {
        setSeenWhatsNew(context)
    }
}