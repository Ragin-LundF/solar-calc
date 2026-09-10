package io.github.raginlundf.solarcalc.domain.services.price

import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfileEntity
import io.github.raginlundf.solarcalc.domain.models.repository.PriceSnapshotRepository
import org.springframework.stereotype.Service

@Service
class PriceResolver(
    private val priceSnapshotRepository: PriceSnapshotRepository,
) {

    /**
     * Loads the profile's price timeline in one query. Callers that resolve many months should hold
     * on to the result rather than rebuilding it per month.
     */
    fun timeline(profile: EnergyProfileEntity): PriceTimeline {
        val snapshots = priceSnapshotRepository.findAllByEnergyProfileIdOrderByValidFromDesc(
            energyProfileId = profile.id!!,
        )
        return PriceTimeline(profile = profile, snapshots = snapshots)
    }

    /** Convenience for a single month; prefer [timeline] when resolving a whole history. */
    fun resolve(profile: EnergyProfileEntity, period: String): ResolvedPrices {
        return timeline(profile = profile).at(period = period)
    }
}
