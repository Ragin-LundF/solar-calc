package io.github.raginlundf.solarcalc.domain.services.profile

import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfile
import io.github.raginlundf.solarcalc.domain.models.repository.EnergyProfileRepository
import io.github.raginlundf.solarcalc.domain.models.repository.UserRepository
import io.github.raginlundf.solarcalc.dtos.error.ResourceNotFoundException
import io.github.raginlundf.solarcalc.dtos.profile.CreateEnergyProfileRequest
import io.github.raginlundf.solarcalc.dtos.profile.EnergyProfileResponse
import io.github.raginlundf.solarcalc.dtos.profile.UpdateEnergyProfileRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ProfileDomainControllerImpl(
    private val profileRepository: EnergyProfileRepository,
    private val userRepository: UserRepository,
) : ProfileDomainController {

    override fun list(username: String): List<EnergyProfileResponse> {
        return profileRepository.findAllByUserUsername(userUsername = username).map { it.toResponse() }
    }

    override fun get(profileUuid: String, username: String): EnergyProfileResponse {
        return requireOwnedProfile(profileUuid = profileUuid, username = username).toResponse()
    }

    @Transactional
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
            kmPerKwh = request.kmPerKwh
            litersPer100km = request.litersPer100km
            investCost = request.investKosten
            usableAreaSqm = request.usableAreaSqm
            heatPumpScop = request.heatPumpScop
        }
        val saved = profileRepository.save(profile)
        user.lastProfileUuid = saved.uuid
        userRepository.save(user)
        return saved.toResponse()
    }

    override fun update(
        profileUuid: String,
        request: UpdateEnergyProfileRequest,
        username: String,
    ): EnergyProfileResponse {
        val profile = requireOwnedProfile(profileUuid = profileUuid, username = username)
        profile.name = request.name
        profile.hasWallbox = request.hasWallbox
        profile.hasHeatPump = request.hasHeatPump
        profile.heatingReferenceType = request.heatingReferenceType
        profile.defaultElectricityPrice = request.defaultElectricityPrice
        profile.defaultFeedInTariff = request.defaultFeedInTariff
        profile.defaultPetrolPrice = request.defaultPetrolPrice
        profile.defaultOilReferenceCost = request.defaultOilReferenceCost
        profile.defaultGasReferenceCost = request.defaultGasReferenceCost
        profile.kmPerKwh = request.kmPerKwh
        profile.litersPer100km = request.litersPer100km
        profile.investCost = request.investKosten
        profile.usableAreaSqm = request.usableAreaSqm
        profile.heatPumpScop = request.heatPumpScop
        request.heatingMonthlyDistribution?.let { profile.heatingMonthlyDistribution = it }
        request.overviewLayout?.let { profile.overviewLayout = it }
        profile.updatedAt = LocalDateTime.now()
        return profileRepository.save(profile).toResponse()
    }

    @Transactional
    override fun delete(profileUuid: String, username: String) {
        val profile = requireOwnedProfile(profileUuid = profileUuid, username = username)
        profileRepository.delete(profile)
        profile.user?.let { user ->
            if (user.lastProfileUuid == profileUuid) {
                user.lastProfileUuid = null
                userRepository.save(user)
            }
        }
    }

    private fun requireOwnedProfile(profileUuid: String, username: String): EnergyProfile {
        return profileRepository.findByUuidAndUserUsername(uuid = profileUuid, userUsername = username)
            ?: throw ResourceNotFoundException("Profile $profileUuid not found")
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
        kmPerKwh = kmPerKwh,
        litersPer100km = litersPer100km,
        investKosten = investCost,
        usableAreaSqm = usableAreaSqm,
        heatPumpScop = heatPumpScop,
        heatingMonthlyDistribution = heatingMonthlyDistribution,
        overviewLayout = overviewLayout,
    )
}
