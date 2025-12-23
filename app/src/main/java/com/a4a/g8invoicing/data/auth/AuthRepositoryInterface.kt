package com.a4a.g8invoicing.data.auth

interface AuthRepositoryInterface {
    suspend fun signUp(username: String, password:String): AuthResult<Unit>
    suspend fun signIn(username: String, password: String): AuthResult<Unit>
    suspend fun authenticate(): AuthResult<Unit>
}