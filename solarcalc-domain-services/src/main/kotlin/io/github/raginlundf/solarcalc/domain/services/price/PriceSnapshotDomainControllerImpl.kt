package io.github.raginlundf.solarcalc.domain.services.price

import io.github.raginlundf.solarcalc.domain.models.price.PriceSnapshotEntity
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfileEntity
import io.github.raginlundf.solarcalc.domain.models.repository.EnergyProfileRepository
import io.github.raginlundf.solarcalc.domain.models.repository.PriceSnapshotRepository
import io.github.raginlundf.solarcalc.dtos.error.DuplicateInputException
import io.github.raginlundf.solarcalc.dtos.error.ResourceNotFoundException
import io.github.raginlundf.solarcalc.dtos.error.ValidationException
import io.github.raginlundf.solarcalc.dtos.price.EffectivePricesResponse
import io.github.raginlundf.solarcalc.dtos.price.PriceSnapshotResponse
import io.github.raginlundf.solarcalc.dtos.price.UpsertPriceSnapshotRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PriceSnapshotDomainControllerImpl(
    private val profileRepository: EnergyProfileRepository,
    private val priceRepository: PriceSnapshotRepository,
    private val priceResolver: PriceResolver,
) : PriceSnapshotDomainController {

    @Transactional(readOnly = true)
    override fun list(profileUuid: String, username: String): List<PriceSnapshotResponse> {
        val profile = requireProfile(profileUuid = profileUuid, username = username)
        return priceRepository.findAllByEnergyProfileIdOrderByValidFromDesc(energyProfileId = profile.id!!)
            .map { it.toResponse(profileUuid) }
    }

    @Transactional(readOnly = true)
    override fun get(profileUuid: String, priceId: Long, username: String): PriceSnapshotResponse {
        val profile = requireProfile(profileUuid = profileUuid, username = username)
        return requireSnapshot(priceId = priceId, profileId = profile.id!!).toResponse(profileUuid)
    }

    @Transactional
    override fun create(
        profileUuid: String,
        request: UpsertPriceSnapshotRequest,
        username: String,
    ): PriceSnapshotResponse {
        val profile = requireProfile(profileUuid = profileUuid, username = username)
        requireAtLeastOnePrice(request = request)
        requireNoOtherEntryAt(validFrom = request.validFrom, profileId = profile.id!!, excludingId = null)

        val snapshot = PriceSnapshotEntity().apply {
            this.energyProfile = profile
            applyRequest(request)
        }
        return priceRepository.save(snapshot).toResponse(profileUuid)
    }

    @Transactional
    override fun update(
        profileUuid: String,
        priceId: Long,
        request: UpsertPriceSnapshotRequest,
        username: String,
    ): PriceSnapshotResponse {
        val profile = requireProfile(profileUuid = profileUuid, username = username)
        val snapshot = requireSnapshot(priceId = priceId, profileId = profile.id!!)
        requireAtLeastOnePrice(request = request)
        // applyRequest overwrites validFrom, so an edit can collide with another entry.
        requireNoOtherEntryAt(validFrom = request.validFrom, profileId = profile.id!!, excludingId = priceId)
        snapshot.applyRequest(request)
        snapshot.updatedAt = LocalDateTime.now()
        return priceRepository.save(snapshot).toResponse(profileUuid)
    }

    @Transactional
    override fun delete(profileUuid: String, priceId: Long, username: String) {
        val profile = requireProfile(profileUuid = profileUuid, username = username)
        val snapshot = requireSnapshot(priceId = priceId, profileId = profile.id!!)
        priceRepository.delete(snapshot)
    }

    @Transactional(readOnly = true)
    override fun effectivePrices(profileUuid: String, period: String, username: String): EffectivePricesResponse {
        val profile = requireProfile(profileUuid = profileUuid, username = username)
        val effective = priceResolver.resolve(profile = profile, period = period)
        return EffectivePricesResponse(
            period = period,
            electricityPrice = effective.electricityPrice,
            feedInTariff = effective.feedInTariff,
            petrolPrice = effective.petrolPrice,
            heatingReferenceCost = effective.heatingReferenceCost,
        )
    }

    /**
     * An entry with no prices at all resolves to nothing yet still occupies its start month, which
     * would block the real entry for that month behind a duplicate error.
     */
    private fun requireAtLeastOnePrice(request: UpsertPriceSnapshotRequest) {
        val allEmpty = request.electricityPrice == null &&
            request.feedInTariff == null &&
            request.petrolPrice == null &&
            request.oilReferenceCost == null &&
            request.gasReferenceCost == null
        if (allEmpty) {
            throw ValidationException(message = "A price entry must set at least one price.")
        }
    }

    /** One start month, one entry — the DB enforces this too, but a clean 409 beats a constraint error. */
    private fun requireNoOtherEntryAt(validFrom: String, profileId: Long, excludingId: Long?) {
        val existing = priceRepository.findByEnergyProfileIdAndValidFrom(
            energyProfileId = profileId,
            validFrom = validFrom,
        )
        if (existing != null && existing.id != excludingId) {
            throw DuplicateInputException(message = "A price entry starting $validFrom already exists.")
        }
    }

    private fun requireProfile(profileUuid: String, username: String): EnergyProfileEntity {
        return profileRepository.findByUuidAndUserUsername(uuid = profileUuid, userUsername = username)
            ?: throw ResourceNotFoundException("Profile $profileUuid not found")
    }

    private fun requireSnapshot(priceId: Long, profileId: Long): PriceSnapshotEntity {
        return priceRepository.findByIdAndEnergyProfileId(id = priceId, energyProfileId = profileId)
            ?: throw ResourceNotFoundException("PriceSnapshotEntity $priceId not found")
    }
}

private fun PriceSnapshotEntity.applyRequest(request: UpsertPriceSnapshotRequest) {
    validFrom = request.validFrom
    electricityPrice = request.electricityPrice
    feedInTariff = request.feedInTariff
    petrolPrice = request.petrolPrice
    oilReferenceCost = request.oilReferenceCost
    gasReferenceCost = request.gasReferenceCost
}

private fun PriceSnapshotEntity.toResponse(energyProfileUuid: String): PriceSnapshotResponse {
    return PriceSnapshotResponse(
        id = id!!,
        energyProfileUuid = energyProfileUuid,
        validFrom = validFrom,
        electricityPrice = electricityPrice,
        feedInTariff = feedInTariff,
        petrolPrice = petrolPrice,
        oilReferenceCost = oilReferenceCost,
        gasReferenceCost = gasReferenceCost,
    )
}
