package com.a4a.g8invoicing.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("settings")

// Version actuelle de l'app (à mettre à jour à chaque release)
const val CURRENT_APP_VERSION = "1.4"

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

// Vérifier si l'utilisateur a vu les nouveautés de la version actuelle
fun hasSeenWhatsNew(context: Context) =
    context.dataStore.data.map { prefs ->
        prefs[PrefKeys.LAST_SEEN_VERSION] == CURRENT_APP_VERSION
    }

// Marquer les nouveautés comme vues
suspend fun setSeenWhatsNew(context: Context) {
    context.dataStore.edit { prefs ->
        prefs[PrefKeys.LAST_SEEN_VERSION] = CURRENT_APP_VERSION
    }
}