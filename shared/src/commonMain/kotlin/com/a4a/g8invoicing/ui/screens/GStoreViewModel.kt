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
        if (!isPremium()) return // Defensive: UI should already prevent this, but never trust UI alone
        activatedModules.toggle(moduleId)
    }

    /**
     * Force-refresh subscription state from /v1/account. Called on screen resume so that
     * a stale cached entry (e.g. one persisted with a null currentPeriodEndMs due to
     * the old parser bug) gets corrected without requiring a trip via the Account screen.
     */
    fun refreshSubscription() {
        viewModelScope.launch {
            subscriptionRepository.refresh(force = true)
        }
    }
}
