package com.a4a.g8invoicing

import com.a4a.g8invoicing.data.auth.ActivatedModulesRepository
import com.a4a.g8invoicing.data.auth.ActivatedModulesRepository.Companion.MODULE_DELIVERY_NOTE
import com.a4a.g8invoicing.data.auth.ActivatedModulesRepository.Companion.MODULE_QUOTE
import com.a4a.g8invoicing.data.auth.ActivatedModulesRepository.Companion.MODULE_QUOTE_TRIAL
import com.a4a.g8invoicing.data.auth.ActivatedModulesRepository.Companion.MODULE_WATERMARK_REMOVAL
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ActivatedModulesRepositoryTest {

    // ---- Toggling ----------------------------------------------------------

    @Test
    fun togglingPremiumModuleAsPremium_seedsEverActivated() {
        val repo = ActivatedModulesRepository(MapSettings())
        repo.toggle(MODULE_QUOTE, isPremium = true)

        assertTrue(repo.isActive(MODULE_QUOTE))
        assertTrue(repo.wasEverActivated(MODULE_QUOTE))
    }

    @Test
    fun togglingPremiumModuleAsNonPremium_doesNotSeedEverActivated() {
        // The ViewModel usually gates this call, but if it somehow slips through, the
        // repo must not mark it as "was ever premium" — that would make an offline
        // gStore poke into permanent menu visibility.
        val repo = ActivatedModulesRepository(MapSettings())
        repo.toggle(MODULE_QUOTE, isPremium = false)

        assertTrue(repo.isActive(MODULE_QUOTE))
        assertFalse(repo.wasEverActivated(MODULE_QUOTE))
    }

    @Test
    fun togglingFreeModule_neverSeedsEverActivated() {
        // Free modules are, well, free — they don't ride on the premium history at all.
        val repo = ActivatedModulesRepository(MapSettings())
        repo.toggle(MODULE_QUOTE_TRIAL, isPremium = true)

        assertTrue(repo.isActive(MODULE_QUOTE_TRIAL))
        assertFalse(repo.wasEverActivated(MODULE_QUOTE_TRIAL))
    }

    @Test
    fun togglingOff_thenOn_keepsEverActivated() {
        val repo = ActivatedModulesRepository(MapSettings())
        repo.toggle(MODULE_QUOTE, isPremium = true)
        repo.toggle(MODULE_QUOTE, isPremium = true) // OFF
        assertFalse(repo.isActive(MODULE_QUOTE))
        assertTrue(repo.wasEverActivated(MODULE_QUOTE))
    }

    @Test
    fun toggleIsIdempotentInEverActivated() {
        val repo = ActivatedModulesRepository(MapSettings())
        repo.toggle(MODULE_QUOTE, isPremium = true)
        repo.toggle(MODULE_QUOTE, isPremium = true) // OFF
        repo.toggle(MODULE_QUOTE, isPremium = true) // ON again
        assertEquals(setOf(MODULE_QUOTE), repo.everActivated.value)
    }

    // ---- clear() vs wipeAll() ---------------------------------------------

    @Test
    fun clear_dropsActivation_butKeepsEverActivated() {
        // clear() is called on logout — the whole point of everActivated is that it
        // survives so the user still sees their premium categories in the menu after
        // logout / subscription loss.
        val repo = ActivatedModulesRepository(MapSettings())
        repo.toggle(MODULE_QUOTE, isPremium = true)
        repo.clear()

        assertFalse(repo.isActive(MODULE_QUOTE))
        assertTrue(repo.wasEverActivated(MODULE_QUOTE))
    }

    @Test
    fun wipeAll_nukesBoth() {
        // wipeAll() is for account-delete — everything goes.
        val repo = ActivatedModulesRepository(MapSettings())
        repo.toggle(MODULE_QUOTE, isPremium = true)
        repo.toggle(MODULE_WATERMARK_REMOVAL, isPremium = true)
        repo.wipeAll()

        assertFalse(repo.isActive(MODULE_QUOTE))
        assertFalse(repo.isActive(MODULE_WATERMARK_REMOVAL))
        assertFalse(repo.wasEverActivated(MODULE_QUOTE))
        assertFalse(repo.wasEverActivated(MODULE_WATERMARK_REMOVAL))
    }

    // ---- Grandfather migration --------------------------------------------

    @Test
    fun grandfatherSeeding_backfillsCurrentlyActiveNonFreeModules() {
        // Simulate an install that predates the everActivated field: the user has
        // MODULE_QUOTE currently active in the old key. On first read, we should
        // migrate it into everActivated so their menu doesn't lose the category on
        // next logout.
        val settings = MapSettings()
        settings.putString("gstore_activated_modules_v1", "$MODULE_QUOTE,$MODULE_DELIVERY_NOTE")

        val repo = ActivatedModulesRepository(settings)

        assertTrue(repo.isActive(MODULE_QUOTE))
        assertTrue(repo.isActive(MODULE_DELIVERY_NOTE))
        // Non-free grandfathered
        assertTrue(repo.wasEverActivated(MODULE_QUOTE))
        // Free never enters everActivated even during migration
        assertFalse(repo.wasEverActivated(MODULE_DELIVERY_NOTE))
    }

    @Test
    fun grandfatherSeedingIsOneShot() {
        val settings = MapSettings()
        settings.putString("gstore_activated_modules_v1", MODULE_QUOTE)
        // First construction seeds.
        val first = ActivatedModulesRepository(settings)
        assertTrue(first.wasEverActivated(MODULE_QUOTE))

        // Now simulate the user turning MODULE_QUOTE off (as non-premium — no seed).
        first.toggle(MODULE_QUOTE, isPremium = false)
        assertFalse(first.isActive(MODULE_QUOTE))

        // Second construction (e.g. next app launch) must NOT re-seed anything from
        // the (now empty) active set — everActivated should already hold the QUOTE
        // entry from the first run.
        val second = ActivatedModulesRepository(settings)
        assertTrue(second.wasEverActivated(MODULE_QUOTE))
        assertFalse(second.isActive(MODULE_QUOTE))
    }

    @Test
    fun freshInstall_hasEmptyEverActivated() {
        val repo = ActivatedModulesRepository(MapSettings())
        assertEquals(emptySet(), repo.everActivated.value)
    }
}
