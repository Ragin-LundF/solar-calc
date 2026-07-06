package io.github.raginlundf.solarcalc.domain.services.profile

import io.github.raginlundf.solarcalc.dtos.profile.CreateEnergyProfileRequest
import io.github.raginlundf.solarcalc.dtos.profile.EnergyProfileResponse
import io.github.raginlundf.solarcalc.dtos.profile.UpdateEnergyProfileRequest

interface ProfileDomainController {
    fun list(): List<EnergyProfileResponse>
    fun get(profileUuid: String): EnergyProfileResponse
    fun create(request: CreateEnergyProfileRequest, username: String): EnergyProfileResponse
    fun update(profileUuid: String, request: UpdateEnergyProfileRequest): EnergyProfileResponse
    fun delete(profileUuid: String)
}
