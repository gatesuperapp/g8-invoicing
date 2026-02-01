package com.a4a.g8invoicing.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("settings")

// Récupère la version de l'app dynamiquement
fun Context.getAppVersion(): String {
    return try {
        packageManager.getPackageInfo(packageName, 0).versionName ?: "unknown"
    } catch (e: Exception) {
        "unknown"
    }
}

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

// Vérifier si on doit afficher la popup What's New
// Retourne true (pas de popup) si : version déjà vue OU nouvelle installation
// Retourne false (afficher popup) seulement si : montée en version
fun hasSeenWhatsNew(context: Context) =
    context.dataStore.data.map { prefs ->
        val lastVersion = prefs[PrefKeys.LAST_SEEN_VERSION]
        val currentVersion = context.getAppVersion()
        lastVersion == null || lastVersion == currentVersion
    }

// Marquer les nouveautés comme vues
suspend fun setSeenWhatsNew(context: Context) {
    context.dataStore.edit { prefs ->
        prefs[PrefKeys.LAST_SEEN_VERSION] = context.getAppVersion()
    }
}