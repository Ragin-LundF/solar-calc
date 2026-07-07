package io.github.raginlundf.solarcalc.dtos.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseDto(
    val token: String,
    val username: String,
    val expiresInSeconds: Long,
    val setupStep: Int = 0,
)
