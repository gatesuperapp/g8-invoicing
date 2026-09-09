package com.a4a.g8invoicing.data

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Which company (issuer master) the user is currently working under —
 * drives whose clients / products / documents are surfaced in the lists
 * and which numbering counter feeds a brand-new doc.
 *
 * Persisted in Settings so the choice survives app restarts. Migration
 * 6 guarantees at least one issuer exists, so [initIfMissing] resolves
 * the fallback to the most recent one on first launch when the key is
 * still null.
 */
class CurrentCompanyRepository(private val settings: Settings = Settings()) {
    companion object {
        private const val KEY = "current_company_id"
    }

    private val _state = MutableStateFlow(loadFromSettings())
    val state: StateFlow<Long?> = _state

    val current: Long?
        get() = _state.value

    fun setCurrent(id: Long) {
        settings.putLong(KEY, id)
        _state.value = id
    }

    /**
     * Hydrate on cold start when Settings has no persisted value yet.
     * [fallback] is expected to resolve the most recently updated issuer
     * (typically ClientOrIssuerLocalDataSource.getLastInsertedIssuerId()).
     */
    fun initIfMissing(fallback: () -> Long?) {
        if (_state.value != null) return
        val resolved = fallback() ?: return
        setCurrent(resolved)
    }

    private fun loadFromSettings(): Long? =
        settings.getLongOrNull(KEY)?.takeIf { it > 0 }
}
