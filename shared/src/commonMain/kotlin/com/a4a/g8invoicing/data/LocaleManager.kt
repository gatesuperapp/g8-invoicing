package com.a4a.g8invoicing.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.a4a.g8invoicing.setAppLocale
import com.russhwolf.settings.Settings

enum class AppLanguage(val code: String?, val displayName: String) {
    SYSTEM(null, "Système"),
    FRENCH("fr", "Français"),
    ENGLISH("en", "English"),
    GERMAN("de", "Deutsch");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return values().find { it.code == code } ?: SYSTEM
        }
    }
}

class LocaleManager(private val settings: Settings = Settings()) {
    companion object {
        private const val KEY_LANGUAGE = "app_language"
    }

    var currentLanguage: AppLanguage by mutableStateOf(loadSavedLanguage())
        private set

    private fun loadSavedLanguage(): AppLanguage {
        val savedCode = settings.getStringOrNull(KEY_LANGUAGE)
        return AppLanguage.fromCode(savedCode)
    }

    fun setLanguage(language: AppLanguage) {
        currentLanguage = language
        if (language == AppLanguage.SYSTEM) {
            settings.remove(KEY_LANGUAGE)
        } else {
            settings.putString(KEY_LANGUAGE, language.code!!)
        }
        setAppLocale(language.code)
    }

    fun initializeLocale() {
        setAppLocale(currentLanguage.code)
    }
}
