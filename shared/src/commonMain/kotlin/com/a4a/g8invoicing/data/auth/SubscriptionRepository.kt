package com.a4a.g8invoicing.data.auth

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Tracks the user's premium-subscription status across the app, with a local cache.
 *
 * Strategy (per ACCOUNT_SUBSCRIPTION_PLAN.md):
 * - 6h fresh window: within that window, [refresh] is a no-op (we trust the cache).
 * - 7d stale tolerance: if /v1/me fails (offline, server down) but the cache is
 *   under 7 days old, we keep showing the last known state. Past 7 days, premium
 *   is presumed expired.
 *
 * The cache is persisted through [Settings] (which on Android is the encrypted
 * shared-prefs instance). Subscription status itself isn't sensitive, but reusing
 * the same store keeps the auth-related state colocated.
 *
 * Premium = subscription.status == "active" AND currentPeriodEnd > now.
 * (Trialing/past_due are intentionally NOT premium — strict per plan.)
 */
class SubscriptionRepository(
    private val authRepository: AuthRepository,
    private val authApi: AuthApiClient,
    private val settings: Settings,
    private val clock: Clock = Clock.System,
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val _state = MutableStateFlow(loadFromCache())
    val state: StateFlow<SubscriptionState> = _state.asStateFlow()

    /**
     * Refresh subscription state from /v1/me.
     *
     * Returns immediately if the cache is still fresh (< [FRESH_WINDOW_MS]).
     * On network failure, falls back to the cache while it's under [STALE_WINDOW_MS].
     * On HTTP error (e.g. user soft-deleted), clears cache and returns Free state.
     *
     * The AuthInterceptor handles 401 + refresh transparently: by the time we get
     * a response here, either the request succeeded with a valid token, or the
     * refresh token is itself dead and the user has been force-logged-out.
     */
    suspend fun refresh(force: Boolean = false): RefreshResult {
        if (!authRepository.isLoggedIn()) {
            _state.value = SubscriptionState.NotLoggedIn
            settings.remove(KEY_CACHE)
            return RefreshResult.NotLoggedIn
        }

        val cached = _state.value
        val now = clock.now().toEpochMilliseconds()

        if (!force && cached is SubscriptionState.Known && (now - cached.fetchedAtMs) < FRESH_WINDOW_MS) {
            return RefreshResult.UsedFreshCache
        }

        return when (val result = authApi.getMe()) {
            is MeResult.Success -> {
                val sub = result.me.subscription
                val newState = SubscriptionState.Known(
                    email = result.me.email,
                    status = sub?.status,
                    plan = sub?.plan,
                    product = sub?.product,
                    currentPeriodEndMs = sub?.currentPeriodEnd?.let { tryParseInstant(it) },
                    cancelAtPeriodEnd = sub?.cancelAtPeriodEnd ?: false,
                    fetchedAtMs = now,
                )
                _state.value = newState
                saveToCache(newState)
                RefreshResult.Refreshed
            }
            is MeResult.Error -> {
                if (cached is SubscriptionState.Known && (now - cached.fetchedAtMs) < STALE_WINDOW_MS) {
                    RefreshResult.UsedStaleCache
                } else {
                    _state.value = SubscriptionState.Unknown
                    RefreshResult.Failed(result.message)
                }
            }
        }
    }

    fun isPremium(): Boolean {
        val s = _state.value as? SubscriptionState.Known ?: return false
        if (s.status != "active") return false
        val end = s.currentPeriodEndMs ?: return false
        return end > clock.now().toEpochMilliseconds()
    }

    fun clear() {
        _state.value = SubscriptionState.NotLoggedIn
        settings.remove(KEY_CACHE)
    }

    private fun loadFromCache(): SubscriptionState {
        val raw = settings.getStringOrNull(KEY_CACHE) ?: return SubscriptionState.NotLoggedIn
        return runCatching { json.decodeFromString<SubscriptionState.Known>(raw) }.getOrElse {
            SubscriptionState.NotLoggedIn
        }
    }

    private fun saveToCache(state: SubscriptionState.Known) {
        settings.putString(KEY_CACHE, json.encodeToString(SubscriptionState.Known.serializer(), state))
    }

    private fun tryParseInstant(iso: String): Long? {
        // Backend sends LocalDateTime.toString() (no timezone, e.g. "2027-06-13T20:38:50.123").
        // Old code only handled full ISO Instant (with Z); that silently failed and made
        // isPremium evaluate to false even for active subs. Try Instant first (for any future
        // upgrade to timezone-aware serialization), then LocalDateTime assuming UTC.
        runCatching { return Instant.parse(iso).toEpochMilliseconds() }
        return runCatching {
            LocalDateTime.parse(iso).toInstant(TimeZone.UTC).toEpochMilliseconds()
        }.getOrNull()
    }

    companion object {
        private const val KEY_CACHE = "subscription_cache_v1"
        private const val FRESH_WINDOW_MS = 6L * 60L * 60L * 1000L          // 6 hours
        private const val STALE_WINDOW_MS = 7L * 24L * 60L * 60L * 1000L    // 7 days
    }
}

sealed class SubscriptionState {
    data object NotLoggedIn : SubscriptionState()

    /** /v1/me has never returned and we have no usable cache (rare — first launch offline). */
    data object Unknown : SubscriptionState()

    @Serializable
    data class Known(
        val email: String,
        val status: String?,
        val plan: String?,
        val product: String? = null,  // "fly" or "fab"; default for backwards-compat with cached state
        val currentPeriodEndMs: Long?,
        val cancelAtPeriodEnd: Boolean = false,
        val fetchedAtMs: Long,
    ) : SubscriptionState()
}

sealed class RefreshResult {
    data object NotLoggedIn : RefreshResult()
    data object Refreshed : RefreshResult()
    data object UsedFreshCache : RefreshResult()
    data object UsedStaleCache : RefreshResult()
    data class Failed(val reason: String) : RefreshResult()
}
