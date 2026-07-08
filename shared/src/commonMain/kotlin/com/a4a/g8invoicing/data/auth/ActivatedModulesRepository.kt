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
        val raw = settings.getStringOrNull(KEY_ACTIVATED) ?: return emptySet()
        return raw.split(",").filter { it.isNotBlank() }.toSet()
    }

    companion object {
        const val MODULE_ORDERS = "orders"
        const val MODULE_FACTURX = "facturx"
        const val MODULE_PAYMENTS = "payments"
        const val MODULE_THEMES = "themes"
        // Module that removes the "Document généré avec 𝕘𝟠" footer from invoices and PDFs.
        // Default OFF (footer shown). Only togglable by premium users.
        const val MODULE_WATERMARK_REMOVAL = "watermark_removal"

        private const val KEY_ACTIVATED = "gstore_activated_modules_v1"
    }
}
