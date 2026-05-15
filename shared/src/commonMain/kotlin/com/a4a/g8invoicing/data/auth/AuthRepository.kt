package com.a4a.g8invoicing.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for authentication state management.
 * Handles magic link auth, token refresh, and session lifecycle.
 */
class AuthRepository(
    private val tokenStorage: TokenStorage,
    private val apiClient: AuthApiClient
) {
    private val _authState = MutableStateFlow<AuthState>(
        if (tokenStorage.isLoggedIn()) AuthState.LoggedIn(tokenStorage.userEmail ?: "")
        else AuthState.LoggedOut
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun isLoggedIn(): Boolean = tokenStorage.isLoggedIn()
    fun getUserEmail(): String? = tokenStorage.userEmail
    fun getAccessToken(): String? = tokenStorage.accessToken
    fun getRefreshToken(): String? = tokenStorage.refreshToken

    /**
     * Request a magic link email. Always succeeds from the user's perspective.
     */
    suspend fun requestMagicLink(email: String): MagicLinkResult {
        return apiClient.requestMagicLink(email)
    }

    /**
     * Consume a magic link token (from deep link) and establish session.
     */
    suspend fun consumeMagicLink(token: String): AuthResult {
        return when (val result = apiClient.consumeMagicLink(token)) {
            is AuthTokenResult.Success -> {
                tokenStorage.saveTokens(
                    accessToken = result.accessToken,
                    refreshToken = result.refreshToken,
                    email = result.email ?: "",
                    userId = result.userId ?: ""
                )
                _authState.value = AuthState.LoggedIn(result.email ?: "")
                AuthResult.Success(result.email ?: "")
            }
            is AuthTokenResult.Error -> AuthResult.Error(result.message)
        }
    }

    /**
     * Refresh the access token using the refresh token.
     * Called by the HTTP interceptor on 401.
     * Returns true if refresh succeeded.
     */
    suspend fun refreshTokens(): Boolean {
        val refreshToken = tokenStorage.refreshToken ?: return false
        return when (val result = apiClient.refreshToken(refreshToken)) {
            is AuthTokenResult.Success -> {
                tokenStorage.accessToken = result.accessToken
                tokenStorage.refreshToken = result.refreshToken
                true
            }
            is AuthTokenResult.Error -> {
                // Refresh failed — session expired, force re-login
                forceLogout()
                false
            }
        }
    }

    /**
     * Fetch a one-shot Stripe Customer Portal URL bound to the authenticated user.
     * Returns null on failure — caller should fall back to the public login URL.
     */
    suspend fun fetchPortalSessionUrl(): String? {
        return when (val result = apiClient.createPortalSession()) {
            is PortalSessionResult.Success -> result.url
            is PortalSessionResult.Error -> null
        }
    }

    /**
     * Logout: revoke server session + clear local tokens.
     */
    suspend fun logout() {
        val refreshToken = tokenStorage.refreshToken
        if (refreshToken != null) {
            apiClient.logout(refreshToken)
        }
        tokenStorage.clear()
        _authState.value = AuthState.LoggedOut
    }

    /**
     * Force logout without server call (when refresh fails).
     */
    fun forceLogout() {
        tokenStorage.clear()
        _authState.value = AuthState.LoggedOut
    }
}

sealed class AuthState {
    data object LoggedOut : AuthState()
    data class LoggedIn(val email: String) : AuthState()
}

sealed class AuthResult {
    data class Success(val email: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
