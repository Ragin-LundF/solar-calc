package io.github.raginlundf.solarcalc.domain.services.profile

import io.github.raginlundf.solarcalc.dtos.profile.CreateEnergyProfileRequest
import io.github.raginlundf.solarcalc.dtos.profile.EnergyProfileResponse
import io.github.raginlundf.solarcalc.dtos.profile.UpdateEnergyProfileRequest

interface ProfileDomainController {
    fun list(username: String): List<EnergyProfileResponse>
    fun get(profileUuid: String, username: String): EnergyProfileResponse
    fun create(request: CreateEnergyProfileRequest, username: String): EnergyProfileResponse
    fun update(profileUuid: String, request: UpdateEnergyProfileRequest, username: String): EnergyProfileResponse
    fun delete(profileUuid: String, username: String)
}
