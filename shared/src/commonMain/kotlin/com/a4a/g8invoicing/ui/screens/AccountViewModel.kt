package com.a4a.g8invoicing.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.auth.AuthRepository
import com.a4a.g8invoicing.data.auth.AuthResult
import com.a4a.g8invoicing.data.auth.AuthState
import com.a4a.g8invoicing.data.auth.DeleteAccountResult
import com.a4a.g8invoicing.data.auth.MagicLinkResult
import com.a4a.g8invoicing.data.auth.SubscriptionRepository
import com.a4a.g8invoicing.data.auth.SubscriptionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AccountViewModel(
    private val authRepository: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    val subscriptionState: StateFlow<SubscriptionState> = subscriptionRepository.state

    var uiState by mutableStateOf(
        AccountUiState(isLoggedIn = authRepository.isLoggedIn(), userEmail = authRepository.getUserEmail())
    )
        private set

    init {
        viewModelScope.launch {
            authRepository.authState.collectLatest { authState ->
                uiState = when (authState) {
                    is AuthState.LoggedIn -> uiState.copy(
                        isLoggedIn = true,
                        userEmail = authState.email,
                        isLoading = false
                    )
                    is AuthState.LoggedOut -> uiState.copy(
                        isLoggedIn = false,
                        userEmail = null,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun requestMagicLink(email: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null, successMessage = null)
            when (authRepository.requestMagicLink(email)) {
                is MagicLinkResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        successMessage = "magic_link_sent" // resolved in UI via stringResource
                    )
                }
                is MagicLinkResult.Error -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "magic_link_error"
                    )
                }
            }
        }
    }

    fun consumeMagicLink(token: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, consumeErrorMessage = null)
            when (val result = authRepository.consumeMagicLink(token)) {
                is AuthResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        userEmail = result.email
                    )
                }
                is AuthResult.Error -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        consumeErrorMessage = result.message
                    )
                }
            }
        }
    }

    /**
     * Force-refresh the cached subscription state from /v1/me.
     * Called when the screen comes back to the foreground (e.g. after the user returns
     * from the Stripe Customer Portal). A short delay lets the Stripe webhook land on
     * our backend before we re-query — without it, a fast user can race the webhook
     * and see stale data on the first refresh.
     */
    fun refreshSubscription() {
        viewModelScope.launch {
            delay(WEBHOOK_GRACE_MS)
            subscriptionRepository.refresh(force = true)
        }
    }

    /**
     * Resolve the URL to open for "Gérer mon abonnement".
     * Tries the authenticated per-user portal session first; falls back to the
     * public login URL if the API call fails (network, server down, no customer ID yet).
     * The callback fires on the main dispatcher — safe to call uriHandler.openUri().
     */
    fun openCustomerPortal(fallbackUrl: String, onUrl: (String) -> Unit) {
        viewModelScope.launch {
            val url = authRepository.fetchPortalSessionUrl() ?: fallbackUrl
            onUrl(url)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            uiState = uiState.copy(isLoggedIn = false, userEmail = null)
        }
    }

    /**
     * Permanently delete the account. On success the user is also logged out locally
     * (handled inside the repository, which flips authState — that cascades to a
     * SubscriptionRepository.clear() through MainCompose's LaunchedEffect).
     */
    fun deleteAccount() {
        viewModelScope.launch {
            uiState = uiState.copy(isDeleting = true, deleteErrorMessage = null)
            when (val result = authRepository.deleteAccount()) {
                is DeleteAccountResult.Success -> {
                    uiState = uiState.copy(
                        isDeleting = false,
                        isLoggedIn = false,
                        userEmail = null,
                        accountDeleted = true,
                    )
                }
                is DeleteAccountResult.Error -> {
                    uiState = uiState.copy(
                        isDeleting = false,
                        deleteErrorMessage = result.message,
                    )
                }
            }
        }
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }

    fun clearSuccess() {
        uiState = uiState.copy(successMessage = null)
    }

    fun clearConsumeError() {
        uiState = uiState.copy(consumeErrorMessage = null)
    }

    fun clearDeleteError() {
        uiState = uiState.copy(deleteErrorMessage = null)
    }

    fun clearAccountDeleted() {
        uiState = uiState.copy(accountDeleted = false)
    }
}

data class AccountUiState(
    val isLoggedIn: Boolean = false,
    val userEmail: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val consumeErrorMessage: String? = null,
    val isDeleting: Boolean = false,
    val deleteErrorMessage: String? = null,
    val accountDeleted: Boolean = false,
)

// Wait this long before refreshing /v1/me on screen resume — Stripe webhooks usually
// land in well under a second, but a too-eager refresh can race and miss the update.
private const val WEBHOOK_GRACE_MS = 1500L
