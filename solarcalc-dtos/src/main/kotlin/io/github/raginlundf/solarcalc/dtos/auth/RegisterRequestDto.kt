package io.github.raginlundf.solarcalc.dtos.auth

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    val username: String,
    val password: String,
)
