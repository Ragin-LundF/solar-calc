package io.github.raginlundf.solarcalc.domain.services.price

import io.github.raginlundf.solarcalc.domain.models.price.PriceSnapshot
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

    override fun list(profileId: Long): List<PriceSnapshotResponse> {
        requireProfile(profileId = profileId)
        return priceRepository.findAllByEnergyProfileId(energyProfileId = profileId).map { it.toResponse() }
    }

    override fun get(profileId: Long, priceId: Long): PriceSnapshotResponse {
        requireProfile(profileId = profileId)
        return priceRepository.findById(priceId).orElseThrow {
            ResourceNotFoundException("PriceSnapshot $priceId not found")
        }.toResponse()
    }

    override fun create(profileId: Long, request: UpsertPriceSnapshotRequest): PriceSnapshotResponse {
        val profile = profileRepository.findById(profileId).orElseThrow {
            ResourceNotFoundException("Profile $profileId not found")
        }

        val snapshot = PriceSnapshot().apply {
            this.energyProfile = profile
            applyRequest(request)
        }
        return priceRepository.save(snapshot).toResponse()
    }

    override fun update(profileId: Long, priceId: Long, request: UpsertPriceSnapshotRequest): PriceSnapshotResponse {
        requireProfile(profileId = profileId)
        val snapshot = priceRepository.findById(priceId).orElseThrow {
            ResourceNotFoundException("PriceSnapshot $priceId not found")
        }
        snapshot.applyRequest(request)
        snapshot.updatedAt = LocalDateTime.now()
        return priceRepository.save(snapshot).toResponse()
    }

    override fun delete(profileId: Long, priceId: Long) {
        requireProfile(profileId = profileId)
        val snapshot = priceRepository.findById(priceId).orElseThrow {
            ResourceNotFoundException("PriceSnapshot $priceId not found")
        }
        priceRepository.delete(snapshot)
    }

    private fun requireProfile(profileId: Long) {
        profileRepository.findById(profileId).orElseThrow {
            ResourceNotFoundException("Profile $profileId not found")
        }
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

private fun PriceSnapshot.toResponse(): PriceSnapshotResponse {
    return PriceSnapshotResponse(
        id = id!!,
        energyProfileId = energyProfileId!!,
        period = period,
        electricityPrice = electricityPrice,
        feedInTariff = feedInTariff,
        petrolPrice = petrolPrice,
        oilReferenceCost = oilReferenceCost,
        gasReferenceCost = gasReferenceCost,
    )
}
