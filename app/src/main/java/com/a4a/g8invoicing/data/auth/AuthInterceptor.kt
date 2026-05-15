package com.a4a.g8invoicing.data.auth

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.concurrent.TimeUnit

class AuthInterceptor(
    private val tokenStorage: TokenStorage,
    private val baseUrl: String = "https://api.the-gate.fr"
) : Interceptor {

    private val refreshClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }
    private val refreshLock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // Auth endpoints handle their own tokens — don't attach Bearer, don't retry on 401
        val path = original.url.encodedPath
        if (path.startsWith("/v1/auth/")) {
            return chain.proceed(original)
        }

        val accessToken = tokenStorage.accessToken
        val authed = if (accessToken != null) {
            original.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            original
        }

        val response = chain.proceed(authed)

        if (response.code != 401 || tokenStorage.refreshToken == null) {
            return response
        }

        val newAccessToken = synchronized(refreshLock) {
            // Another thread may have refreshed while we waited for the lock.
            // If the access token changed, just use the new one.
            val current = tokenStorage.accessToken
            if (current != null && current != accessToken) {
                current
            } else {
                tryRefresh()
            }
        }

        if (newAccessToken == null) {
            tokenStorage.clear()
            return response
        }

        response.close()
        val retry = original.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
        return chain.proceed(retry)
    }

    private fun tryRefresh(): String? {
        val refreshToken = tokenStorage.refreshToken ?: return null
        return try {
            val payload = json.encodeToString(
                RefreshRequest.serializer(),
                RefreshRequest(refreshToken)
            )
            val body = payload.toRequestBody("application/json".toMediaType())

            val req = Request.Builder()
                .url("$baseUrl/v1/auth/refresh")
                .post(body)
                .build()

            refreshClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val bodyStr = resp.body?.string() ?: return null
                val parsed = json.decodeFromString(AuthTokenResponse.serializer(), bodyStr)
                tokenStorage.accessToken = parsed.authToken
                tokenStorage.refreshToken = parsed.refreshToken
                parsed.authToken
            }
        } catch (_: Exception) {
            null
        }
    }
}
