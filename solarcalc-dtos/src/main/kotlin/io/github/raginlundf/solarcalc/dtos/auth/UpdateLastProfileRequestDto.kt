package io.github.raginlundf.solarcalc.dtos.auth

import kotlinx.serialization.Serializable

@Serializable
data class UpdateLastProfileRequestDto(
    val profileUuid: String?,
)
