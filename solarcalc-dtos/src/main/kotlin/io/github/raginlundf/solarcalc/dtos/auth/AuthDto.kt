package io.github.raginlundf.solarcalc.dtos.auth

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
    val expiresInSeconds: Long,
    val setupStep: Int = 0,
)

@Serializable
data class UpdateSetupStepRequest(
    val setupStep: Int,
)

@Serializable
data class ErrorResponse(
    val error: String,
)
