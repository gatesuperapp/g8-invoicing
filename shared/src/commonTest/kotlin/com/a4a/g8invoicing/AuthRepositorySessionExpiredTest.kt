package com.a4a.g8invoicing

import com.a4a.g8invoicing.data.LocaleManager
import com.a4a.g8invoicing.data.auth.AuthApiClient
import com.a4a.g8invoicing.data.auth.AuthRepository
import com.a4a.g8invoicing.data.auth.TokenStorage
import com.russhwolf.settings.MapSettings
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthRepositorySessionExpiredTest {

    // The MockEngine is never actually reached: logout() calls apiClient.logout() which
    // is a no-op ("No backend logout endpoint yet"), and forceLogout() doesn't touch
    // the API at all. The engine just satisfies HttpClient construction — any inbound
    // request would 500 loudly so a regression that starts making network calls fails
    // the test rather than silently succeeding.
    private fun buildRepo(withRefreshToken: Boolean = true): AuthRepository {
        val settings = MapSettings()
        val tokenStorage = TokenStorage(settings)
        if (withRefreshToken) {
            tokenStorage.saveTokens(
                accessToken = "access-abc",
                refreshToken = "refresh-xyz",
                email = "test@example.com",
                userId = "uid-1",
            )
        }
        val mockClient = HttpClient(MockEngine { respondError(HttpStatusCode.InternalServerError) })
        val apiClient = AuthApiClient(mockClient, baseUrl = "https://invalid.example")
        val localeManager = LocaleManager(MapSettings())
        return AuthRepository(tokenStorage, apiClient, localeManager)
    }

    /**
     * Collect [AuthRepository.sessionExpired] into a channel so tests can await
     * emissions deterministically. Returns both the channel and the collector Job
     * so the test can cancel it before returning — otherwise runTest complains that
     * coroutines are still running when the test body ends.
     */
    private fun TestScope.collectSessionExpired(repo: AuthRepository): Pair<Channel<Unit>, Job> {
        val received = Channel<Unit>(capacity = Channel.UNLIMITED)
        val job = launch { repo.sessionExpired.collect { received.trySend(Unit) } }
        // Let the collector subscribe before the caller triggers anything.
        testScheduler.runCurrent()
        return received to job
    }

    @Test
    fun forceLogout_emitsSessionExpired() = runTest {
        val repo = buildRepo()
        val (received, job) = collectSessionExpired(repo)

        repo.forceLogout()
        testScheduler.runCurrent()

        assertEquals(Unit, received.tryReceive().getOrNull())
        job.cancel()
    }

    @Test
    fun voluntaryLogout_doesNotEmitSessionExpired() = runTest {
        val repo = buildRepo()
        val (received, job) = collectSessionExpired(repo)

        repo.logout()
        testScheduler.runCurrent()

        // We assert the channel is empty rather than blocking with a timeout because
        // "no emission" is a negative property — the fastest failing path is to poll.
        assertEquals(null, received.tryReceive().getOrNull())
        job.cancel()
    }

    @Test
    fun forceLogout_clearsTokensAndFlipsAuthState() = runTest {
        val repo = buildRepo()
        repo.forceLogout()

        assertEquals(false, repo.isLoggedIn())
        assertEquals(null, repo.getAccessToken())
        assertEquals(null, repo.getRefreshToken())
    }
}
