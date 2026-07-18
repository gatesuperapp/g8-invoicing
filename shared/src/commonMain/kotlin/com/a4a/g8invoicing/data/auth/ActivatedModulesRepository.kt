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

    fun isActive(moduleId: String): Boolean = moduleId in _state.value

    fun toggle(moduleId: String) {
        val current = _state.value
        val updated = if (moduleId in current) current - moduleId else current + moduleId
        _state.value = updated
        settings.putString(KEY_ACTIVATED, updated.joinToString(","))
    }

    fun clear() {
        _state.value = emptySet()
        settings.remove(KEY_ACTIVATED)
    }

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

    companion object {
        const val MODULE_ORDERS = "orders"
        const val MODULE_FACTURX = "facturx"
        const val MODULE_PAYMENTS = "payments"
        const val MODULE_THEMES = "themes"
        // Module that removes the "Document généré avec 𝕘𝟠" footer from invoices and PDFs.
        // Default OFF (footer shown). Only togglable by premium users.
        const val MODULE_WATERMARK_REMOVAL = "watermark_removal"
        // Module that unlocks the "Devis" (quote) category and menu.
        // Default OFF. Free-tier (see FREE_MODULES below) — everyone can toggle it.
        const val MODULE_QUOTE = "quote"
        // Module that unlocks the "Bons de livraison" category and menu.
        // Default ON (see DEFAULT_ACTIVATED_MODULES). Free-tier — everyone can toggle.
        // Seeded on first load so existing users who used delivery notes before the
        // GStore card existed don't suddenly lose the category.
        const val MODULE_DELIVERY_NOTE = "delivery_note"

        // Modules available to everyone regardless of subscription status. The UI hides
        // the PREMIUM pill and the ViewModel's premium check skips these. Kept as a Set
        // so adding a future free module is one string.
        val FREE_MODULES = setOf(MODULE_QUOTE, MODULE_DELIVERY_NOTE)

        // Modules seeded into the activated set the first time the app boots after this
        // migration is deployed. Guarded by KEY_DEFAULTS_SEEDED so we don't re-add a
        // module the user has explicitly toggled off.
        val DEFAULT_ACTIVATED_MODULES = setOf(MODULE_DELIVERY_NOTE)

        private const val KEY_ACTIVATED = "gstore_activated_modules_v1"
        private const val KEY_DEFAULTS_SEEDED = "gstore_defaults_seeded_v1"
    }
}
