package com.a4a.g8invoicing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSourceInterface
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.data.auth.ActivatedModulesRepository
import com.a4a.g8invoicing.data.auth.SubscriptionRepository
import com.a4a.g8invoicing.data.models.CountryCodes
import com.a4a.g8invoicing.data.models.PersonType
import com.a4a.g8invoicing.ui.states.AddressState
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.OnboardingIssuerAnswers
import com.a4a.g8invoicing.ui.states.OnboardingStep
import com.a4a.g8invoicing.ui.states.ProductNatureAnswer
import com.a4a.g8invoicing.ui.states.VatAnswer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Backing state for the 1.8 fullscreen onboarding wizard.
 *
 * The wizard walks the user through a mix of global + per-issuer questions and
 * defers persistence until the final "Terminer" so answers can be revisited via
 * the Précédent button. See [OnboardingStep] for the sequence.
 *
 * Per-issuer country is captured explicitly here (not inferred from locale) and
 * feeds into two branches:
 * - Intra-EU sales question: only shown when the picked country is in EU
 * - Product nature question: only shown when at least one issuer has both
 *   intra-EU sales AND an EU country
 */
class OnboardingViewModel(
    private val issuerDataSource: ClientOrIssuerLocalDataSourceInterface,
    private val productDataSource: ProductLocalDataSourceInterface,
    private val activatedModules: ActivatedModulesRepository,
    private val subscriptionRepository: SubscriptionRepository,
) : ViewModel() {

    private val _issuers = MutableStateFlow<List<ClientOrIssuerState>>(emptyList())
    val issuers: StateFlow<List<ClientOrIssuerState>> = _issuers.asStateFlow()

    private val _perIssuerAnswers = MutableStateFlow<Map<Int, OnboardingIssuerAnswers>>(emptyMap())
    val perIssuerAnswers: StateFlow<Map<Int, OnboardingIssuerAnswers>> =
        _perIssuerAnswers.asStateFlow()

    // "Are all your clients in one country?" answer + the picked country. Both
    // reset when the user backtracks past the client-country steps so they can
    // change their mind.
    private val _clientCountrySameForAll = MutableStateFlow<Boolean?>(null)
    val clientCountrySameForAll: StateFlow<Boolean?> = _clientCountrySameForAll.asStateFlow()

    private val _clientCountry = MutableStateFlow<String?>(null)
    val clientCountry: StateFlow<String?> = _clientCountry.asStateFlow()

    private val _currentStep = MutableStateFlow<OnboardingStep>(OnboardingStep.Welcome)
    val currentStep: StateFlow<OnboardingStep> = _currentStep.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    // Gates the wizard against advancing past the info steps before the
    // issuers list has been fetched. Set true after the first emission from
    // `fetchAll` (even an empty list — no issuers = legitimate end state).
    private val _issuersLoaded = MutableStateFlow(false)
    val issuersLoaded: StateFlow<Boolean> = _issuersLoaded.asStateFlow()

    init {
        viewModelScope.launch {
            issuerDataSource.fetchAll(type = PersonType.ISSUER).collect { list ->
                _issuers.value = list
                _issuersLoaded.value = true
            }
        }
    }

    // ---- Answer setters -----------------------------------------------------

    fun setIssuerCountry(issuerId: Int, countryCode: String) {
        updateAnswers(issuerId) { it.copy(countryCode = countryCode.uppercase()) }
    }

    fun setVatAnswer(issuerId: Int, value: VatAnswer) {
        updateAnswers(issuerId) { it.copy(vatAnswer = value) }
    }

    fun setIntraEu(issuerId: Int, value: Boolean) {
        updateAnswers(issuerId) { it.copy(intraEuSales = value) }
    }

    fun setProductNature(issuerId: Int, value: ProductNatureAnswer) {
        updateAnswers(issuerId) { it.copy(productNature = value) }
    }

    fun setClientCountrySameForAll(value: Boolean) {
        _clientCountrySameForAll.value = value
    }

    fun setClientCountry(countryCode: String) {
        _clientCountry.value = countryCode.uppercase()
    }

    private fun updateAnswers(
        issuerId: Int,
        transform: (OnboardingIssuerAnswers) -> OnboardingIssuerAnswers,
    ) {
        val current = _perIssuerAnswers.value
        val existing = current[issuerId] ?: OnboardingIssuerAnswers(issuerId = issuerId)
        _perIssuerAnswers.value = current + (issuerId to transform(existing))
    }

    // ---- Side effects ------------------------------------------------------

    fun deleteIssuer(issuer: ClientOrIssuerState) {
        val id = issuer.id ?: return
        viewModelScope.launch {
            issuerDataSource.deleteClientOrIssuer(issuer)
            _perIssuerAnswers.value = _perIssuerAnswers.value - id
        }
    }

    /**
     * Activate the Quote category from the onboarding "Activer les devis" CTA.
     * Premium users get [MODULE_QUOTE] (unlimited); non-premium users get the
     * discovery [MODULE_QUOTE_TRIAL] (capped at 5 quote creations). This mirrors
     * what the user would see in the gStore — the same distinction, just
     * pre-toggled so they don't need to jump there manually.
     */
    fun activateQuoteModule() {
        val moduleId = if (subscriptionRepository.isPremium())
            ActivatedModulesRepository.MODULE_QUOTE
        else
            ActivatedModulesRepository.MODULE_QUOTE_TRIAL
        if (!activatedModules.isActive(moduleId)) {
            activatedModules.toggle(moduleId)
        }
    }

    // ---- Navigation ---------------------------------------------------------

    fun next() {
        _currentStep.value = computeNext(_currentStep.value)
    }

    fun previous() {
        _currentStep.value = computePrevious(_currentStep.value)
    }

    private fun computeNext(step: OnboardingStep): OnboardingStep {
        val list = _issuers.value
        return when (step) {
            OnboardingStep.Welcome -> OnboardingStep.DevisIntro
            OnboardingStep.DevisIntro -> OnboardingStep.FacturXIntro
            OnboardingStep.FacturXIntro -> OnboardingStep.Privacy
            OnboardingStep.Privacy -> {
                if (list.size > 1) OnboardingStep.IssuerCleanup
                else firstIssuerLoopStepOrTail(list)
            }
            OnboardingStep.IssuerCleanup -> firstIssuerLoopStepOrTail(list)
            is OnboardingStep.IssuerCountry -> OnboardingStep.VatExempt(step.issuerIndex)
            is OnboardingStep.VatExempt -> {
                val ans = answersFor(list.getOrNull(step.issuerIndex)?.id)
                val next = when (ans?.vatAnswer) {
                    VatAnswer.DONT_KNOW -> OnboardingStep.VatDontKnowHint(step.issuerIndex)
                    else -> stepAfterVatFor(list, step.issuerIndex)
                }
                next
            }
            is OnboardingStep.VatDontKnowHint -> stepAfterVatFor(list, step.issuerIndex)
            is OnboardingStep.IntraEu -> {
                // If EU + intra-EU=Yes → ask that issuer's product nature.
                // Otherwise advance to the next issuer (or the tail).
                val ans = answersFor(list.getOrNull(step.issuerIndex)?.id)
                if (ans?.intraEuSales == true) OnboardingStep.ProductNature(step.issuerIndex)
                else advanceIssuerOrClientCountryOrTail(list, step.issuerIndex)
            }
            is OnboardingStep.ProductNature -> {
                val ans = answersFor(list.getOrNull(step.issuerIndex)?.id)
                if (ans?.productNature == ProductNatureAnswer.MIXED)
                    OnboardingStep.MixedHint(step.issuerIndex)
                else advanceIssuerOrClientCountryOrTail(list, step.issuerIndex)
            }
            is OnboardingStep.MixedHint ->
                advanceIssuerOrClientCountryOrTail(list, step.issuerIndex)
            OnboardingStep.IssuersDone -> OnboardingStep.UnitCodeInfo
            OnboardingStep.UnitCodeInfo -> OnboardingStep.ClientCountryQuestion
            OnboardingStep.ClientCountryQuestion -> {
                if (_clientCountrySameForAll.value == true) OnboardingStep.ClientCountryPicker
                else OnboardingStep.ClientCountryDoneSkipped
            }
            OnboardingStep.ClientCountryPicker -> OnboardingStep.ClientCountryDoneApplied
            OnboardingStep.ClientCountryDoneApplied -> OnboardingStep.ThankYou
            OnboardingStep.ClientCountryDoneSkipped -> OnboardingStep.ThankYou
            OnboardingStep.ThankYou -> OnboardingStep.ThankYou
        }
    }

    private fun computePrevious(step: OnboardingStep): OnboardingStep {
        val list = _issuers.value
        return when (step) {
            OnboardingStep.Welcome -> OnboardingStep.Welcome
            OnboardingStep.DevisIntro -> OnboardingStep.Welcome
            OnboardingStep.FacturXIntro -> OnboardingStep.DevisIntro
            OnboardingStep.Privacy -> OnboardingStep.FacturXIntro
            OnboardingStep.IssuerCleanup -> OnboardingStep.Privacy
            is OnboardingStep.IssuerCountry -> {
                if (step.issuerIndex == 0) {
                    if (list.size > 1) OnboardingStep.IssuerCleanup
                    else OnboardingStep.Privacy
                } else {
                    terminalPerIssuerStepFor(list, step.issuerIndex - 1)
                }
            }
            is OnboardingStep.VatExempt -> OnboardingStep.IssuerCountry(step.issuerIndex)
            is OnboardingStep.VatDontKnowHint -> OnboardingStep.VatExempt(step.issuerIndex)
            is OnboardingStep.IntraEu -> {
                val ans = answersFor(list.getOrNull(step.issuerIndex)?.id)
                if (ans?.vatAnswer == VatAnswer.DONT_KNOW) OnboardingStep.VatDontKnowHint(step.issuerIndex)
                else OnboardingStep.VatExempt(step.issuerIndex)
            }
            is OnboardingStep.ProductNature -> OnboardingStep.IntraEu(step.issuerIndex)
            is OnboardingStep.MixedHint -> OnboardingStep.ProductNature(step.issuerIndex)
            OnboardingStep.IssuersDone -> {
                // Précédent from IssuersDone brings the user back to the
                // entreprises list (IssuerCleanup) rather than into the last
                // per-issuer question — the review affordance most users expect
                // at this point is the businesses list, not the last question.
                if (list.size > 1) OnboardingStep.IssuerCleanup
                else OnboardingStep.Privacy
            }
            OnboardingStep.UnitCodeInfo -> {
                // UnitCodeInfo is reached from IssuersDone (or Privacy on
                // fresh install when the per-issuer loop was skipped).
                if (list.isEmpty()) OnboardingStep.Privacy
                else OnboardingStep.IssuersDone
            }
            OnboardingStep.ClientCountryQuestion -> OnboardingStep.UnitCodeInfo
            OnboardingStep.ClientCountryPicker -> OnboardingStep.ClientCountryQuestion
            OnboardingStep.ClientCountryDoneApplied -> OnboardingStep.ClientCountryPicker
            OnboardingStep.ClientCountryDoneSkipped -> OnboardingStep.ClientCountryQuestion
            OnboardingStep.ThankYou -> {
                if (_clientCountrySameForAll.value == true) OnboardingStep.ClientCountryDoneApplied
                else OnboardingStep.ClientCountryDoneSkipped
            }
        }
    }

    // ---- Per-issuer branching helpers --------------------------------------

    /** After the VAT question (or its hint), decide whether to ask about
     *  intra-EU sales (issuer in EU) or move on to the next issuer / tail. */
    private fun stepAfterVatFor(list: List<ClientOrIssuerState>, idx: Int): OnboardingStep {
        val issuerCountry = answersFor(list.getOrNull(idx)?.id)?.countryCode
        return if (CountryCodes.isInEU(issuerCountry)) OnboardingStep.IntraEu(idx)
        else advanceIssuerOrClientCountryOrTail(list, idx)
    }

    /** After the terminal per-issuer step, either loop onto the next issuer
     *  or land on the [IssuersDone] confirmation. The subsequent tail
     *  (ProductNature → UnitCode → ClientCountry* → ThankYou) is walked from
     *  IssuersDone forward — [computeNext] handles the branching from there. */
    private fun advanceIssuerOrClientCountryOrTail(
        list: List<ClientOrIssuerState>,
        currentIndex: Int,
    ): OnboardingStep {
        val nextIndex = currentIndex + 1
        if (nextIndex < list.size) return OnboardingStep.IssuerCountry(nextIndex)
        // Always show the "issuers configured" confirmation once at least one
        // issuer went through the loop — even if the intra-EU tail is skipped.
        return OnboardingStep.IssuersDone
    }

    /** The step that was terminal for issuer[idx] given the answers captured
     *  so far — used when the user backs up onto the previous issuer's block.
     *  Deep to shallow: MixedHint > ProductNature > IntraEu > VatDontKnowHint >
     *  VatExempt. */
    private fun terminalPerIssuerStepFor(
        list: List<ClientOrIssuerState>,
        idx: Int,
    ): OnboardingStep {
        val ans = answersFor(list.getOrNull(idx)?.id)
        val eu = CountryCodes.isInEU(ans?.countryCode)
        return when {
            eu && ans?.intraEuSales == true && ans.productNature == ProductNatureAnswer.MIXED ->
                OnboardingStep.MixedHint(idx)
            eu && ans?.intraEuSales == true && ans.productNature != null ->
                OnboardingStep.ProductNature(idx)
            eu -> OnboardingStep.IntraEu(idx)
            ans?.vatAnswer == VatAnswer.DONT_KNOW -> OnboardingStep.VatDontKnowHint(idx)
            else -> OnboardingStep.VatExempt(idx)
        }
    }

    private fun firstIssuerLoopStepOrTail(list: List<ClientOrIssuerState>): OnboardingStep =
        if (list.isEmpty()) OnboardingStep.UnitCodeInfo
        else OnboardingStep.IssuerCountry(0)

    private fun answersFor(issuerId: Int?): OnboardingIssuerAnswers? =
        issuerId?.let { _perIssuerAnswers.value[it] }

    /** Any issuer that is both in EU (per their picked country) AND sells
     *  intra-EU. Kept for legacy logging — the per-issuer ProductNature step is
     *  gated per-issuer now, no global sweep needed. */
    private fun anyEuIntraSales(): Boolean =
        _perIssuerAnswers.value.values.any {
            it.intraEuSales == true && CountryCodes.isInEU(it.countryCode)
        }

    /** Unanimous non-MIXED product-nature answer across all EU + intra-EU
     *  issuers, or null if answers diverge / one is MIXED / no such issuer.
     *  Drives the bulk Product.type update on [commit]. */
    private fun unanimousProductNature(): ProductNatureAnswer? {
        val relevant = _perIssuerAnswers.value.values.filter {
            it.intraEuSales == true && CountryCodes.isInEU(it.countryCode)
        }
        if (relevant.isEmpty()) return null
        val distinct = relevant.mapNotNull { it.productNature }.toSet()
        val allAnswered = relevant.all { it.productNature != null }
        return if (allAnswered && distinct.size == 1 && distinct.first() != ProductNatureAnswer.MIXED)
            distinct.first()
        else null
    }

    // ---- Final commit -------------------------------------------------------

    fun commit(onDone: () -> Unit) {
        viewModelScope.launch {
            _perIssuerAnswers.value.values.forEach { ans ->
                val issuer = _issuers.value.firstOrNull { it.id == ans.issuerId } ?: return@forEach
                val newAddresses = when {
                    ans.countryCode.isNullOrBlank() -> issuer.addresses
                    !issuer.addresses.isNullOrEmpty() -> issuer.addresses!!.mapIndexed { i, addr ->
                        if (i == 0) addr.copy(countryCode = ans.countryCode) else addr
                    }
                    // Issuer had no address yet — create a bare one carrying the country.
                    else -> listOf(AddressState(countryCode = ans.countryCode))
                }
                val updated = issuer.copy(
                    vatExempt = ans.vatAnswer == VatAnswer.FRANCHISE,
                    intraEuSales = ans.intraEuSales ?: issuer.intraEuSales,
                    addresses = newAddresses,
                )
                issuerDataSource.updateClientOrIssuer(updated)
            }
            unanimousProductNature()?.toBulkType()?.let { bulk ->
                productDataSource.updateAllProductTypes(bulk)
            }
            if (_clientCountrySameForAll.value == true) {
                _clientCountry.value?.let { code ->
                    issuerDataSource.setCountryForClientsWithoutCountry(code)
                }
            }
            _isFinished.value = true
            onDone()
        }
    }
}
