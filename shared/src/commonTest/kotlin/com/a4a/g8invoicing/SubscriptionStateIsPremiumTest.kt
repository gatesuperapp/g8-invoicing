package com.a4a.g8invoicing

import com.a4a.g8invoicing.data.auth.SubscriptionState
import com.a4a.g8invoicing.data.auth.isPremium
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SubscriptionStateIsPremiumTest {

    private fun known(status: String?): SubscriptionState.Known =
        SubscriptionState.Known(
            email = "test@example.com",
            status = status,
            plan = "monthly",
            product = "fly",
            currentPeriodEndMs = null,
            cancelAtPeriodEnd = false,
            fetchedAtMs = 0L,
        )

    @Test
    fun notLoggedIn_isNotPremium() {
        assertFalse(SubscriptionState.NotLoggedIn.isPremium())
    }

    @Test
    fun unknown_isNotPremium() {
        assertFalse(SubscriptionState.Unknown.isPremium())
    }

    @Test
    fun active_isPremium() {
        assertTrue(known("active").isPremium())
    }

    @Test
    fun trialing_isNotPremium() {
        assertFalse(known("trialing").isPremium())
    }

    @Test
    fun pastDue_isNotPremium() {
        assertFalse(known("past_due").isPremium())
    }

    @Test
    fun canceled_isNotPremium() {
        assertFalse(known("canceled").isPremium())
    }

    @Test
    fun unpaid_isNotPremium() {
        assertFalse(known("unpaid").isPremium())
    }

    @Test
    fun incomplete_isNotPremium() {
        assertFalse(known("incomplete").isPremium())
    }

    @Test
    fun nullStatus_isNotPremium() {
        assertFalse(known(null).isPremium())
    }
}
