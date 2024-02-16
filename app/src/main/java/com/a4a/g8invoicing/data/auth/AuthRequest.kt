package com.a4a.g8invoicing.data.auth

class AuthRequestSignUp (
    val id: Int,
    val firstName: String,
    val lastName: String ="lastname",
    val email: String,
    val password: String
)

class AuthRequestSignIn (
    val email: String,
    val password: String
)

