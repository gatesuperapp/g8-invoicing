package com.a4a.g8invoicing.data.auth

import com.russhwolf.settings.Settings

/**
 * Stores auth tokens using multiplatform-settings.
 * On Android, the app module wraps this with EncryptedSharedPreferences.
 */
class TokenStorage(private val settings: Settings = Settings()) {

    var accessToken: String?
        get() = settings.getStringOrNull(KEY_ACCESS_TOKEN)
        set(value) {
            if (value != null) settings.putString(KEY_ACCESS_TOKEN, value)
            else settings.remove(KEY_ACCESS_TOKEN)
        }

    var refreshToken: String?
        get() = settings.getStringOrNull(KEY_REFRESH_TOKEN)
        set(value) {
            if (value != null) settings.putString(KEY_REFRESH_TOKEN, value)
            else settings.remove(KEY_REFRESH_TOKEN)
        }

    var userEmail: String?
        get() = settings.getStringOrNull(KEY_USER_EMAIL)
        set(value) {
            if (value != null) settings.putString(KEY_USER_EMAIL, value)
            else settings.remove(KEY_USER_EMAIL)
        }

    var userId: String?
        get() = settings.getStringOrNull(KEY_USER_ID)
        set(value) {
            if (value != null) settings.putString(KEY_USER_ID, value)
            else settings.remove(KEY_USER_ID)
        }

    fun isLoggedIn(): Boolean = accessToken != null && refreshToken != null

    fun saveTokens(accessToken: String, refreshToken: String, email: String, userId: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.userEmail = email
        this.userId = userId
    }

    fun clear() {
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
        settings.remove(KEY_USER_EMAIL)
        settings.remove(KEY_USER_ID)
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "auth_access_token"
        private const val KEY_REFRESH_TOKEN = "auth_refresh_token"
        private const val KEY_USER_EMAIL = "auth_user_email"
        private const val KEY_USER_ID = "auth_user_id"
    }
}
