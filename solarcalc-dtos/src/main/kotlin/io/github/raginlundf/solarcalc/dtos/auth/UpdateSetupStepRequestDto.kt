package io.github.raginlundf.solarcalc.dtos.auth

import kotlinx.serialization.Serializable

@Serializable
data class UpdateSetupStepRequestDto(
    val setupStep: Int,
)
