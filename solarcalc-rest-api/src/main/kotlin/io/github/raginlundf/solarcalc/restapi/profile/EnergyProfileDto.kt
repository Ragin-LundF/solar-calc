package io.github.raginlundf.solarcalc.restapi.profile

import jakarta.validation.constraints.NotBlank
import kotlinx.serialization.Serializable

@Serializable
data class EnergyProfileResponse(
    val id: Long,
    val name: String,
)

@Serializable
data class CreateEnergyProfileRequest(
    @field:NotBlank val name: String,
)

@Serializable
data class UpdateEnergyProfileRequest(
    @field:NotBlank val name: String,
)
