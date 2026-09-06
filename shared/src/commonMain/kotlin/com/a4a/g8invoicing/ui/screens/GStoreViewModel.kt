package com.a4a.g8invoicing.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.auth.ActivatedModulesRepository
import com.a4a.g8invoicing.data.auth.SubscriptionRepository
import com.a4a.g8invoicing.data.auth.SubscriptionState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GStoreViewModel(
    private val subscriptionRepository: SubscriptionRepository,
    private val activatedModules: ActivatedModulesRepository,
) : ViewModel() {

    val subscriptionState: StateFlow<SubscriptionState> = subscriptionRepository.state
    val activatedState: StateFlow<Set<String>> = activatedModules.state

    fun isPremium(): Boolean = subscriptionRepository.isPremium()

    fun toggleModule(moduleId: String) {
        // Free modules bypass the premium check. Defense-in-depth: UI should already
        // prevent premium-only toggles for non-premium users via the pill + hint dialog,
        // but never trust UI alone.
        val premium = isPremium()
        if (moduleId !in ActivatedModulesRepository.FREE_MODULES && !premium) return
        activatedModules.toggle(moduleId, isPremium = premium)
    }

    /**
     * Force-refresh subscription state from /v1/account. Called on screen resume so the
     * switch state reflects the latest backend truth without waiting for the 6h cache to
     * expire.
     */
    fun refreshSubscription() {
        viewModelScope.launch {
            subscriptionRepository.refresh(force = true)
        }
    }
}
