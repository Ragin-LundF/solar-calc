package io.github.raginlundf.solarcalc.domain.services.profile

import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfile
import io.github.raginlundf.solarcalc.domain.models.repository.EnergyProfileRepository
import io.github.raginlundf.solarcalc.domain.models.repository.UserRepository
import io.github.raginlundf.solarcalc.dtos.error.ResourceNotFoundException
import io.github.raginlundf.solarcalc.dtos.profile.CreateEnergyProfileRequest
import io.github.raginlundf.solarcalc.dtos.profile.EnergyProfileResponse
import io.github.raginlundf.solarcalc.dtos.profile.UpdateEnergyProfileRequest
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ProfileDomainControllerImpl(
    private val profileRepository: EnergyProfileRepository,
    private val userRepository: UserRepository,
) : ProfileDomainController {

    override fun list(): List<EnergyProfileResponse> {
        return profileRepository.findAll().map { it.toResponse() }
    }

    override fun get(profileUuid: String): EnergyProfileResponse {
        val profile = profileRepository.findByUuid(profileUuid)
            ?: throw ResourceNotFoundException("Profile $profileUuid not found")
        return profile.toResponse()
    }

    override fun create(request: CreateEnergyProfileRequest, username: String): EnergyProfileResponse {
        val user = userRepository.findByUsername(username).orElseThrow {
            ResourceNotFoundException("User $username not found")
        }
        val profile = EnergyProfile().apply {
            this.user = user
            name = request.name
            hasWallbox = request.hasWallbox
            hasHeatPump = request.hasHeatPump
            heatingReferenceType = request.heatingReferenceType
            defaultElectricityPrice = request.defaultElectricityPrice
            defaultFeedInTariff = request.defaultFeedInTariff
            defaultPetrolPrice = request.defaultPetrolPrice
            defaultOilReferenceCost = request.defaultOilReferenceCost
            defaultGasReferenceCost = request.defaultGasReferenceCost
        }
        return profileRepository.save(profile).toResponse()
    }

    override fun update(profileUuid: String, request: UpdateEnergyProfileRequest): EnergyProfileResponse {
        val profile = profileRepository.findByUuid(profileUuid)
            ?: throw ResourceNotFoundException("Profile $profileUuid not found")
        profile.name = request.name
        profile.hasWallbox = request.hasWallbox
        profile.hasHeatPump = request.hasHeatPump
        profile.heatingReferenceType = request.heatingReferenceType
        profile.defaultElectricityPrice = request.defaultElectricityPrice
        profile.defaultFeedInTariff = request.defaultFeedInTariff
        profile.defaultPetrolPrice = request.defaultPetrolPrice
        profile.defaultOilReferenceCost = request.defaultOilReferenceCost
        profile.defaultGasReferenceCost = request.defaultGasReferenceCost
        profile.updatedAt = LocalDateTime.now()
        return profileRepository.save(profile).toResponse()
    }

    override fun delete(profileUuid: String) {
        val profile = profileRepository.findByUuid(profileUuid)
            ?: throw ResourceNotFoundException("Profile $profileUuid not found")
        profileRepository.delete(profile)
    }
}

private fun EnergyProfile.toResponse(): EnergyProfileResponse {
    return EnergyProfileResponse(
        id = uuid,
        name = name,
        hasWallbox = hasWallbox,
        hasHeatPump = hasHeatPump,
        heatingReferenceType = heatingReferenceType,
        defaultElectricityPrice = defaultElectricityPrice,
        defaultFeedInTariff = defaultFeedInTariff,
        defaultPetrolPrice = defaultPetrolPrice,
        defaultOilReferenceCost = defaultOilReferenceCost,
        defaultGasReferenceCost = defaultGasReferenceCost,
    )
}
