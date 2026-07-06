package io.github.raginlundf.solarcalc.domain.services.price

import io.github.raginlundf.solarcalc.domain.models.price.PriceSnapshot
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfile
import io.github.raginlundf.solarcalc.domain.models.repository.EnergyProfileRepository
import io.github.raginlundf.solarcalc.domain.models.repository.PriceSnapshotRepository
import io.github.raginlundf.solarcalc.dtos.error.ResourceNotFoundException
import io.github.raginlundf.solarcalc.dtos.price.PriceSnapshotResponse
import io.github.raginlundf.solarcalc.dtos.price.UpsertPriceSnapshotRequest
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class PriceSnapshotDomainControllerImpl(
    private val profileRepository: EnergyProfileRepository,
    private val priceRepository: PriceSnapshotRepository,
) : PriceSnapshotDomainController {

    override fun list(profileUuid: String): List<PriceSnapshotResponse> {
        val profile = requireProfile(profileUuid = profileUuid)
        return priceRepository.findAllByEnergyProfileId(energyProfileId = profile.id!!)
            .map { it.toResponse(profileUuid) }
    }

    override fun get(profileUuid: String, priceId: Long): PriceSnapshotResponse {
        requireProfile(profileUuid = profileUuid)
        return priceRepository.findById(priceId).orElseThrow {
            ResourceNotFoundException("PriceSnapshot $priceId not found")
        }.toResponse(profileUuid)
    }

    override fun create(profileUuid: String, request: UpsertPriceSnapshotRequest): PriceSnapshotResponse {
        val profile = profileRepository.findByUuid(profileUuid)
            ?: throw ResourceNotFoundException("Profile $profileUuid not found")

        val snapshot = PriceSnapshot().apply {
            this.energyProfile = profile
            applyRequest(request)
        }
        return priceRepository.save(snapshot).toResponse(profileUuid)
    }

    override fun update(
        profileUuid: String,
        priceId: Long,
        request: UpsertPriceSnapshotRequest,
    ): PriceSnapshotResponse {
        requireProfile(profileUuid = profileUuid)
        val snapshot = priceRepository.findById(priceId).orElseThrow {
            ResourceNotFoundException("PriceSnapshot $priceId not found")
        }
        snapshot.applyRequest(request)
        snapshot.updatedAt = LocalDateTime.now()
        return priceRepository.save(snapshot).toResponse(profileUuid)
    }

    override fun delete(profileUuid: String, priceId: Long) {
        requireProfile(profileUuid = profileUuid)
        val snapshot = priceRepository.findById(priceId).orElseThrow {
            ResourceNotFoundException("PriceSnapshot $priceId not found")
        }
        priceRepository.delete(snapshot)
    }

    private fun requireProfile(profileUuid: String): EnergyProfile {
        return profileRepository.findByUuid(profileUuid)
            ?: throw ResourceNotFoundException("Profile $profileUuid not found")
    }
}

private fun PriceSnapshot.applyRequest(request: UpsertPriceSnapshotRequest) {
    period = request.period
    electricityPrice = request.electricityPrice
    feedInTariff = request.feedInTariff
    petrolPrice = request.petrolPrice
    oilReferenceCost = request.oilReferenceCost
    gasReferenceCost = request.gasReferenceCost
}

private fun PriceSnapshot.toResponse(energyProfileUuid: String): PriceSnapshotResponse {
    return PriceSnapshotResponse(
        id = id!!,
        energyProfileUuid = energyProfileUuid,
        period = period,
        electricityPrice = electricityPrice,
        feedInTariff = feedInTariff,
        petrolPrice = petrolPrice,
        oilReferenceCost = oilReferenceCost,
        gasReferenceCost = gasReferenceCost,
    )
}
