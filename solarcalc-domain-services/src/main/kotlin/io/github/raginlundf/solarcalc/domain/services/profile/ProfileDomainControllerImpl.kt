package io.github.raginlundf.solarcalc.domain.services.profile

import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfile
import io.github.raginlundf.solarcalc.domain.models.repository.EnergyProfileRepository
import io.github.raginlundf.solarcalc.dtos.error.ResourceNotFoundException
import io.github.raginlundf.solarcalc.dtos.profile.CreateEnergyProfileRequest
import io.github.raginlundf.solarcalc.dtos.profile.EnergyProfileResponse
import io.github.raginlundf.solarcalc.dtos.profile.UpdateEnergyProfileRequest
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ProfileDomainControllerImpl(
    private val profileRepository: EnergyProfileRepository,
) : ProfileDomainController {

    override fun list(): List<EnergyProfileResponse> {
        return profileRepository.findAll().map { it.toResponse() }
    }

    override fun get(profileId: Long): EnergyProfileResponse {
        val profile = profileRepository.findById(profileId).orElseThrow {
            ResourceNotFoundException("Profile $profileId not found")
        }
        return profile.toResponse()
    }

    override fun create(request: CreateEnergyProfileRequest): EnergyProfileResponse {
        val profile = EnergyProfile().apply {
            name = request.name
            hasWallbox = request.hasWallbox
            hasHeatPump = request.hasHeatPump
            heatingReferenceType = request.heatingReferenceType
            defaultElectricityPrice = request.defaultElectricityPrice
            defaultFeedInTariff = request.defaultFeedInTariff
            defaultPetrolPrice = request.defaultPetrolPrice
            defaultOilReferenceCost = request.defaultOilReferenceCost
            defaultGasReferenceCost = request.defaultGasReferenceCost
            defaultEvEfficiencyKwh100km = request.defaultEvEfficiencyKwh100km
            defaultIceEfficiencyL100km = request.defaultIceEfficiencyL100km
        }
        return profileRepository.save(profile).toResponse()
    }

    override fun update(profileId: Long, request: UpdateEnergyProfileRequest): EnergyProfileResponse {
        val profile = profileRepository.findById(profileId).orElseThrow {
            ResourceNotFoundException("Profile $profileId not found")
        }
        profile.name = request.name
        profile.hasWallbox = request.hasWallbox
        profile.hasHeatPump = request.hasHeatPump
        profile.heatingReferenceType = request.heatingReferenceType
        profile.defaultElectricityPrice = request.defaultElectricityPrice
        profile.defaultFeedInTariff = request.defaultFeedInTariff
        profile.defaultPetrolPrice = request.defaultPetrolPrice
        profile.defaultOilReferenceCost = request.defaultOilReferenceCost
        profile.defaultGasReferenceCost = request.defaultGasReferenceCost
        profile.defaultEvEfficiencyKwh100km = request.defaultEvEfficiencyKwh100km
        profile.defaultIceEfficiencyL100km = request.defaultIceEfficiencyL100km
        profile.updatedAt = LocalDateTime.now()
        return profileRepository.save(profile).toResponse()
    }

    override fun delete(profileId: Long) {
        val profile = profileRepository.findById(profileId).orElseThrow {
            ResourceNotFoundException("Profile $profileId not found")
        }
        profileRepository.delete(profile)
    }
}

private fun EnergyProfile.toResponse(): EnergyProfileResponse {
    return EnergyProfileResponse(
        id = id!!,
        name = name,
        hasWallbox = hasWallbox,
        hasHeatPump = hasHeatPump,
        heatingReferenceType = heatingReferenceType,
        defaultElectricityPrice = defaultElectricityPrice,
        defaultFeedInTariff = defaultFeedInTariff,
        defaultPetrolPrice = defaultPetrolPrice,
        defaultOilReferenceCost = defaultOilReferenceCost,
        defaultGasReferenceCost = defaultGasReferenceCost,
        defaultEvEfficiencyKwh100km = defaultEvEfficiencyKwh100km,
        defaultIceEfficiencyL100km = defaultIceEfficiencyL100km,
    )
}
