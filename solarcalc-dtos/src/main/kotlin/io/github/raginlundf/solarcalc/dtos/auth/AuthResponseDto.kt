package io.github.raginlundf.solarcalc.dtos.auth


data class AuthResponseDto(
    val token: String,
    val username: String,
    val expiresInSeconds: Long,
    val setupStep: Int = 0,
    val lastProfileUuid: String? = null,
)
