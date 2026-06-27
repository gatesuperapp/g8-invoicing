package com.a4a.g8invoicing.data.auth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * API client for authentication endpoints (v1 spec).
 */
class AuthApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String = "https://api.the-gate.fr"
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * POST /v1/auth/magic-link/request — sends a magic link email. Always 204.
     */
    suspend fun requestMagicLink(email: String, locale: String? = null): MagicLinkResult {
        return try {
            val response = httpClient.post("$baseUrl/v1/auth/magic-link/request") {
                contentType(ContentType.Application.Json)
                setBody(MagicLinkRequest(email, locale))
            }
            if (response.status.isSuccess()) {
                MagicLinkResult.Success
            } else {
                MagicLinkResult.Error(tryParseError(response))
            }
        } catch (e: Exception) {
            MagicLinkResult.Error(e.message ?: "Network error")
        }
    }

    /**
     * POST /v1/auth/magic-link/consume — consumes magic link token, returns access + refresh tokens
     */
    suspend fun consumeMagicLink(token: String): AuthTokenResult {
        return try {
            val response = httpClient.post("$baseUrl/v1/auth/magic-link/consume") {
                contentType(ContentType.Application.Json)
                setBody(ConsumeRequest(token))
            }

            if (response.status == HttpStatusCode.OK) {
                val body = response.body<MagicLinkVerifyResponse>()
                AuthTokenResult.Success(
                    accessToken = body.authToken,
                    refreshToken = body.refreshToken,
                    userId = body.userId,
                    email = body.email,
                )
            } else {
                AuthTokenResult.Error(tryParseError(response))
            }
        } catch (e: Exception) {
            AuthTokenResult.Error(e.message ?: "Network error")
        }
    }

    /**
     * GET /v1/account — current user info + subscription status. Bearer token attached
     * automatically by AuthInterceptor; 401 triggers refresh + retry transparently.
     */
    suspend fun getAccount(): AccountResult {
        return try {
            val response = httpClient.get("$baseUrl/v1/account")
            if (response.status == HttpStatusCode.OK) {
                AccountResult.Success(response.body())
            } else {
                AccountResult.Error(tryParseError(response))
            }
        } catch (e: Exception) {
            AccountResult.Error(e.message ?: "Network error")
        }
    }

    /**
     * POST /v1/auth/refresh — rotates refresh token, returns new access + refresh tokens
     */
    suspend fun refreshToken(refreshToken: String): AuthTokenResult {
        return try {
            val response = httpClient.post("$baseUrl/v1/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(RefreshRequest(refreshToken))
            }

            if (response.status == HttpStatusCode.OK) {
                val body = response.body<AuthTokenResponse>()
                AuthTokenResult.Success(
                    accessToken = body.authToken,
                    refreshToken = body.refreshToken,
                    userId = null,
                    email = null,
                )
            } else {
                AuthTokenResult.Error("Token expired")
            }
        } catch (e: Exception) {
            AuthTokenResult.Error(e.message ?: "Network error")
        }
    }

    /**
     * No backend logout endpoint yet — caller clears tokens locally.
     */
    suspend fun logout(refreshToken: String) {
        // No-op: server doesn't expose a logout endpoint; AuthRepository clears local state.
    }

    /**
     * DELETE /v1/account — terminates the account: cancels the Stripe subscription
     * immediately, soft-deletes the user, revokes all refresh sessions and purges
     * personal data on the server side. Bearer token attached by AuthInterceptor.
     */
    suspend fun deleteAccount(): DeleteAccountResult {
        return try {
            val response = httpClient.delete("$baseUrl/v1/account")
            if (response.status.isSuccess()) {
                DeleteAccountResult.Success
            } else {
                DeleteAccountResult.Error(tryParseError(response))
            }
        } catch (e: Exception) {
            DeleteAccountResult.Error(e.message ?: "Network error")
        }
    }

    /**
     * POST /v1/billing/portal-session — creates a Stripe Customer Portal session bound
     * to the authenticated user's stripe_customer_id. Returns a one-shot URL the app
     * opens in a browser. Bearer token attached by AuthInterceptor.
     */
    suspend fun createPortalSession(): PortalSessionResult {
        return try {
            val response = httpClient.post("$baseUrl/v1/billing/portal-session")
            if (response.status == HttpStatusCode.OK) {
                val body = response.body<PortalSessionResponse>()
                PortalSessionResult.Success(body.url)
            } else {
                PortalSessionResult.Error(tryParseError(response))
            }
        } catch (e: Exception) {
            PortalSessionResult.Error(e.message ?: "Network error")
        }
    }

    private suspend fun tryParseError(response: HttpResponse): String {
        return try {
            val body = response.bodyAsText()
            json.decodeFromString<ErrorBody>(body).message
        } catch (_: Exception) {
            "An error occurred"
        }
    }
}

// Request DTOs
@Serializable
data class MagicLinkRequest(val email: String, val locale: String? = null)

@Serializable
data class ConsumeRequest(val token: String)

@Serializable
data class RefreshRequest(val refreshToken: String)

// Response DTOs
@Serializable
data class AuthTokenResponse(
    val authToken: String,
    val refreshToken: String,
)

@Serializable
data class MagicLinkVerifyResponse(
    val authToken: String,
    val refreshToken: String,
    val userId: String? = null,
    val email: String? = null,
)

@Serializable
data class AccountResponse(
    val email: String,
    val subscription: SubscriptionInfo? = null
)

@Serializable
data class SubscriptionInfo(
    val status: String,
    val currentPeriodEnd: String,
    val plan: String,
    val product: String? = null,  // "fly" or "fab"; null on legacy server / legacy rows
    // Mirrors Stripe's cancel_at_period_end. true when the user has clicked Cancel in
    // the Customer Portal but the period hasn't ended yet — status is still "active"
    // until Stripe sends subscription.deleted at period end.
    val cancelAtPeriodEnd: Boolean = false,
)

@Serializable
data class PortalSessionResponse(val url: String)

@Serializable
data class ErrorBody(val message: String)

// Result types
sealed class MagicLinkResult {
    data object Success : MagicLinkResult()
    data class Error(val message: String) : MagicLinkResult()
}

sealed class AuthTokenResult {
    data class Success(
        val accessToken: String,
        val refreshToken: String,
        val userId: String?,
        val email: String?
    ) : AuthTokenResult()
    data class Error(val message: String) : AuthTokenResult()
}

sealed class AccountResult {
    data class Success(val account: AccountResponse) : AccountResult()
    data class Error(val message: String) : AccountResult()
}

sealed class PortalSessionResult {
    data class Success(val url: String) : PortalSessionResult()
    data class Error(val message: String) : PortalSessionResult()
}

sealed class DeleteAccountResult {
    data object Success : DeleteAccountResult()
    data class Error(val message: String) : DeleteAccountResult()
}
