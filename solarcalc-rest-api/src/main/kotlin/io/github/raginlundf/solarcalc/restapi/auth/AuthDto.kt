package io.github.raginlundf.solarcalc.restapi.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
)

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
)

@Serializable
data class AuthResponse(
    val token: String,
    val username: String,
    val tenantId: Long,
    val expiresInSeconds: Long,
)

@Serializable
data class ErrorResponse(
    val error: String,
)
