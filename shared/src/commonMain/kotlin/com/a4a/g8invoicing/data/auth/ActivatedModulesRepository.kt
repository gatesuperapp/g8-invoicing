package com.a4a.g8invoicing.data.auth

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tracks which premium modules the user has activated locally.
 *
 * The gStore screen lets a logged-in premium user toggle modules on/off; the toggle
 * is purely local state (Settings-backed), independent of the subscription status —
 * the subscription gates *whether* you can toggle, the activation flag gates whether
 * the corresponding feature/category surfaces in the rest of the app.
 *
 * Module IDs are stable strings (not Compose resource IDs) so saved state survives
 * locale changes.
 */
class ActivatedModulesRepository(
    private val settings: Settings,
) {
    private val _state = MutableStateFlow(loadFromCache())
    val state: StateFlow<Set<String>> = _state.asStateFlow()

    // Non-free modules that have been activated at least once while the user was premium.
    // Never cleared on logout — the navigation menu keeps surfacing those categories so
    // an ex-premium can still access the documents they created (quotes, orders, ...).
    // Only wiped on explicit account delete (see [wipeAll]).
    private val _everActivated = MutableStateFlow(loadEverActivatedFromCache())
    val everActivated: StateFlow<Set<String>> = _everActivated.asStateFlow()

    fun isActive(moduleId: String): Boolean = moduleId in _state.value

    fun wasEverActivated(moduleId: String): Boolean = moduleId in _everActivated.value

    /**
     * Toggle a module ON/OFF. When turning a non-free module ON, mark it in the
     * ever-activated set — this drives the menu-visibility fallback for ex-premium
     * users. The premium check itself lives in the ViewModel; this method assumes
     * the caller has already gated the call.
     */
    fun toggle(moduleId: String, isPremium: Boolean) {
        val current = _state.value
        val turningOn = moduleId !in current
        val updated = if (turningOn) current + moduleId else current - moduleId
        _state.value = updated
        settings.putString(KEY_ACTIVATED, updated.joinToString(","))

        if (turningOn && isPremium && moduleId !in FREE_MODULES) {
            val updatedEver = _everActivated.value + moduleId
            if (updatedEver != _everActivated.value) {
                _everActivated.value = updatedEver
                settings.putString(KEY_EVER_ACTIVATED, updatedEver.joinToString(","))
            }
        }
    }

    fun clear() {
        _state.value = emptySet()
        settings.remove(KEY_ACTIVATED)
    }

    /**
     * Nuke both the current activation and the ever-activated history. Only for
     * account-delete flows — logout must NOT call this, otherwise ex-premium users
     * lose menu access to their existing documents.
     */
    fun wipeAll() {
        _state.value = emptySet()
        _everActivated.value = emptySet()
        settings.remove(KEY_ACTIVATED)
        settings.remove(KEY_EVER_ACTIVATED)
    }

    // ---- Quote trial counter -----------------------------------------------
    //
    // Trial mode caps net-new quotes at [QUOTE_TRIAL_MAX_CLICKS]. The counter
    // is a persistent action count (create + duplicate), not a live COUNT(*)
    // on the Quote table — the user shouldn't be able to reset the cap by
    // deleting quotes. Duplicating N quotes bumps the counter by N, mirroring
    // what a create-N-times session would cost. Only relevant when the user is
    // on [MODULE_QUOTE_TRIAL] AND doesn't have premium (which grants unlimited
    // via [MODULE_QUOTE]).

    fun getQuoteTrialCount(): Int = settings.getInt(KEY_QUOTE_TRIAL_COUNT, 0)

    fun incrementQuoteTrialCount(by: Int = 1) {
        settings.putInt(KEY_QUOTE_TRIAL_COUNT, getQuoteTrialCount() + by)
    }

    /** True when the user is on the free trial module (not the premium unlimited
     *  one) and has hit the click cap. Callers should also check [isPremium]
     *  externally — a premium user's trial cap never applies. */
    fun isQuoteTrialExhausted(): Boolean = wouldExhaustQuoteTrial(1)

    /** True when performing [additionalActions] more create/duplicate would
     *  exceed the cap. Returns false when the user is not on the trial gate
     *  (either not activated, or has the paid [MODULE_QUOTE] override). */
    fun wouldExhaustQuoteTrial(additionalActions: Int): Boolean =
        isActive(MODULE_QUOTE_TRIAL) &&
            !isActive(MODULE_QUOTE) &&
            (getQuoteTrialCount() + additionalActions) > QUOTE_TRIAL_MAX_CLICKS

    private fun loadFromCache(): Set<String> {
        val raw = settings.getStringOrNull(KEY_ACTIVATED)
        val cached = raw?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
        // One-shot seed of default-on modules on first launch after this migration.
        // Existing users had no MODULE_DELIVERY_NOTE in their set but were seeing the BL
        // category unconditionally — we inject it so the category doesn't disappear from
        // their menu when the module gate goes live.
        if (settings.getStringOrNull(KEY_DEFAULTS_SEEDED) == null) {
            val seeded = cached + DEFAULT_ACTIVATED_MODULES
            settings.putString(KEY_ACTIVATED, seeded.joinToString(","))
            settings.putString(KEY_DEFAULTS_SEEDED, "1")
            return seeded
        }
        return cached
    }

    private fun loadEverActivatedFromCache(): Set<String> {
        val raw = settings.getStringOrNull(KEY_EVER_ACTIVATED)
        val stored = raw?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
        // Migration: existing installs that had non-free modules currently ON when this
        // shipped are grandfathered into the ever-activated set on first read. Without
        // this, a premium user updating the app would see their category disappear at
        // next logout because we'd have no history of the ON toggle.
        if (settings.getStringOrNull(KEY_EVER_ACTIVATED_SEEDED) == null) {
            val currentRaw = settings.getStringOrNull(KEY_ACTIVATED)
            val currentActive = currentRaw?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
            val seeded = stored + currentActive.filter { it !in FREE_MODULES }
            if (seeded.isNotEmpty()) {
                settings.putString(KEY_EVER_ACTIVATED, seeded.joinToString(","))
            }
            settings.putString(KEY_EVER_ACTIVATED_SEEDED, "1")
            return seeded
        }
        return stored
    }

    companion object {
        const val MODULE_ORDERS = "orders"
        const val MODULE_FACTURX = "facturx"
        const val MODULE_PAYMENTS = "payments"
        const val MODULE_THEMES = "themes"
        // Module that removes the "Document généré avec 𝕘𝟠" footer from invoices and PDFs.
        // Default OFF (footer shown). Only togglable by premium users.
        const val MODULE_WATERMARK_REMOVAL = "watermark_removal"
        // Module that unlocks the "Devis" (quote) category and menu WITHOUT any
        // creation cap. Premium-only in 1.8. Non-premium users see the free
        // [MODULE_QUOTE_TRIAL] card instead which is capped at 5 creations.
        const val MODULE_QUOTE = "quote"
        // Free discovery variant of MODULE_QUOTE — same category unlock, but the
        // "+ new quote" tap is capped at [QUOTE_TRIAL_MAX_CLICKS]. Never shown
        // in the gStore for premium users (they already have unlimited via
        // MODULE_QUOTE). If both modules end up active on a premium account, the
        // trial cap is skipped since MODULE_QUOTE takes precedence.
        const val MODULE_QUOTE_TRIAL = "quote_trial"
        // Module that unlocks the "Bons de livraison" category and menu.
        // Default ON (see DEFAULT_ACTIVATED_MODULES). Free-tier — everyone can toggle.
        // Seeded on first load so existing users who used delivery notes before the
        // GStore card existed don't suddenly lose the category.
        const val MODULE_DELIVERY_NOTE = "delivery_note"

        // Modules available to everyone regardless of subscription status. The UI hides
        // the PREMIUM pill and the ViewModel's premium check skips these. Kept as a Set
        // so adding a future free module is one string.
        val FREE_MODULES = setOf(MODULE_QUOTE_TRIAL, MODULE_DELIVERY_NOTE)

        // Modules seeded into the activated set the first time the app boots after this
        // migration is deployed. Guarded by KEY_DEFAULTS_SEEDED so we don't re-add a
        // module the user has explicitly toggled off.
        val DEFAULT_ACTIVATED_MODULES = setOf(MODULE_DELIVERY_NOTE)

        private const val KEY_ACTIVATED = "gstore_activated_modules_v1"
        private const val KEY_DEFAULTS_SEEDED = "gstore_defaults_seeded_v1"
        private const val KEY_QUOTE_TRIAL_COUNT = "gstore_quote_trial_count_v1"
        private const val KEY_EVER_ACTIVATED = "gstore_ever_activated_modules_v1"
        private const val KEY_EVER_ACTIVATED_SEEDED = "gstore_ever_activated_seeded_v1"

        /** Maximum number of "+ new quote" clicks a trial user can make before
         *  the exhausted modal takes over. Not user-configurable. */
        const val QUOTE_TRIAL_MAX_CLICKS = 5
    }
}
