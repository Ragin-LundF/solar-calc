package io.github.raginlundf.solarcalc.restapi.profile

import jakarta.validation.constraints.NotBlank

data class EnergyProfileResponse(
    val id: Long,
    val tenantId: Long,
    val name: String,
)

data class CreateEnergyProfileRequest(
    @field:NotBlank val name: String,
)

data class UpdateEnergyProfileRequest(
    @field:NotBlank val name: String,
)
