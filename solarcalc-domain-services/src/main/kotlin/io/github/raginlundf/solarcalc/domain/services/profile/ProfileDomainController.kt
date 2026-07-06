package io.github.raginlundf.solarcalc.domain.services.profile

import io.github.raginlundf.solarcalc.dtos.profile.CreateEnergyProfileRequest
import io.github.raginlundf.solarcalc.dtos.profile.EnergyProfileResponse
import io.github.raginlundf.solarcalc.dtos.profile.UpdateEnergyProfileRequest

interface ProfileDomainController {
    fun list(): List<EnergyProfileResponse>
    fun get(profileId: Long): EnergyProfileResponse
    fun create(request: CreateEnergyProfileRequest): EnergyProfileResponse
    fun update(profileId: Long, request: UpdateEnergyProfileRequest): EnergyProfileResponse
    fun delete(profileId: Long)
}
