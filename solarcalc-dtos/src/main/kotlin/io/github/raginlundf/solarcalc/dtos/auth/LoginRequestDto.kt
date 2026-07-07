package io.github.raginlundf.solarcalc.dtos.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String,
)
