package com.a4a.g8invoicing.data.auth

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("user")
    suspend fun signUp(
        @Body request: AuthRequestSignUp
    )

    @POST("auth")
    suspend fun signIn(
        @Body request: AuthRequestSignIn
    ): TokenResponse

    @GET("authenticate")
    suspend fun authenticate(
        @Header("Authorization") token: String
    )

}