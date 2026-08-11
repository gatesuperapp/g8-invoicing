package com.a4a.g8invoicing

import com.a4a.g8invoicing.data.auth.SubscriptionState
import com.a4a.g8invoicing.data.auth.isPremium
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

class SubscriptionStateIsPremiumTest {

    // Freeze "now" so tests don't drift with wall-clock time.
    private val nowMs = 1_800_000_000_000L // 2027-01-15 ~
    private val clock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(nowMs)
    }

    private fun known(status: String?, endMs: Long?): SubscriptionState.Known =
        SubscriptionState.Known(
            email = "test@example.com",
            status = status,
            plan = "monthly",
            product = "fly",
            currentPeriodEndMs = endMs,
            cancelAtPeriodEnd = false,
            fetchedAtMs = nowMs,
        )

    // ---- Base state -------------------------------------------------------

    @Test
    fun notLoggedIn_isNotPremium() {
        assertFalse(SubscriptionState.NotLoggedIn.isPremium(clock))
    }

    @Test
    fun unknown_isNotPremium() {
        assertFalse(SubscriptionState.Unknown.isPremium(clock))
    }

    // ---- Loosened premium whitelist ---------------------------------------

    @Test
    fun active_withFutureEnd_isPremium() {
        assertTrue(known("active", nowMs + 1_000).isPremium(clock))
    }

    @Test
    fun trialing_withFutureEnd_isPremium() {
        assertTrue(known("trialing", nowMs + 1_000).isPremium(clock))
    }

    @Test
    fun pastDue_withFutureEnd_isPremium() {
        // Stripe Smart Retries; user's card expired but Stripe will retry for ~3 weeks
        assertTrue(known("past_due", nowMs + 1_000).isPremium(clock))
    }

    // ---- Non-premium statuses ---------------------------------------------

    @Test
    fun canceled_isNotPremium() {
        assertFalse(known("canceled", nowMs + 1_000).isPremium(clock))
    }

    @Test
    fun unpaid_isNotPremium() {
        assertFalse(known("unpaid", nowMs + 1_000).isPremium(clock))
    }

    @Test
    fun incomplete_isNotPremium() {
        assertFalse(known("incomplete", nowMs + 1_000).isPremium(clock))
    }

    @Test
    fun nullStatus_isNotPremium() {
        assertFalse(known(null, nowMs + 1_000).isPremium(clock))
    }

    // ---- Period-end cutoff hard-stops premium -----------------------------

    @Test
    fun active_withPastEnd_isNotPremium() {
        assertFalse(known("active", nowMs - 1).isPremium(clock))
    }

    @Test
    fun active_atExactEnd_isNotPremium() {
        // end > now, not >=. At the exact boundary we're past the period.
        assertFalse(known("active", nowMs).isPremium(clock))
    }

    @Test
    fun active_withNullEnd_isNotPremium() {
        // Backend didn't send currentPeriodEnd — can't verify the paid window is still open.
        assertFalse(known("active", null).isPremium(clock))
    }

    @Test
    fun pastDue_pastEnd_isNotPremium() {
        // past_due grace has an end too — once currentPeriodEnd is past, we cut premium
        // regardless of Stripe's internal retry state.
        assertFalse(known("past_due", nowMs - 1).isPremium(clock))
    }
}
