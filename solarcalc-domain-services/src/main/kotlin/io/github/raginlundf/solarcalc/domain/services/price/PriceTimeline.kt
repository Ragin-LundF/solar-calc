package io.github.raginlundf.solarcalc.domain.services.price

import io.github.raginlundf.solarcalc.domain.models.price.PriceSnapshotEntity
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfileEntity
import io.github.raginlundf.solarcalc.domain.models.profile.HeatingReferenceTypeEnum
import java.math.BigDecimal

/**
 * A profile's prices over time.
 *
 * Each snapshot states the prices that apply from its start month onwards, so the entries form a
 * timeline rather than a set of per-month overrides. Resolution is **per field**: for a given month
 * every price independently takes the newest snapshot that is already in effect *and* actually
 * carries that price. An entry that only records a new petrol price therefore leaves the
 * electricity price alone instead of resetting it to the profile default.
 *
 * A price nothing in the timeline covers falls back to the profile's own default, which is what the
 * Settings page edits.
 *
 * Build one per profile and reuse it across months — [at] does no I/O.
 */
class PriceTimeline(
    private val profile: EnergyProfileEntity,
    snapshots: List<PriceSnapshotEntity>,
) {

    /** Newest first, so the first match while scanning is the one in effect. */
    private val newestFirst: List<PriceSnapshotEntity> = snapshots.sortedByDescending { it.validFrom }

    /** Prices in effect for [period] (format: YYYY-MM). */
    fun at(period: String): ResolvedPrices {
        return ResolvedPrices(
            electricityPrice = resolve(period = period, default = profile.defaultElectricityPrice) {
                it.electricityPrice
            },
            feedInTariff = resolve(period = period, default = profile.defaultFeedInTariff) {
                it.feedInTariff
            },
            petrolPrice = resolve(period = period, default = profile.defaultPetrolPrice) {
                it.petrolPrice
            },
            heatingReferenceCost = resolveHeatingReference(period = period),
        )
    }

    /**
     * The newest already-effective snapshot that carries this field, else [default].
     * YYYY-MM strings compare correctly as strings, so no date parsing is needed.
     */
    private fun resolve(
        period: String,
        default: BigDecimal?,
        select: (PriceSnapshotEntity) -> BigDecimal?,
    ): BigDecimal? {
        val fromTimeline = newestFirst
            .asSequence()
            .filter { it.validFrom <= period }
            .mapNotNull(select)
            .firstOrNull()
        return fromTimeline ?: default
    }

    private fun resolveHeatingReference(period: String): BigDecimal? {
        return when (profile.heatingReferenceType) {
            HeatingReferenceTypeEnum.OIL -> resolve(
                period = period,
                default = profile.defaultOilReferenceCost,
            ) { it.oilReferenceCost }

            HeatingReferenceTypeEnum.GAS -> resolve(
                period = period,
                default = profile.defaultGasReferenceCost,
            ) { it.gasReferenceCost }

            HeatingReferenceTypeEnum.NONE -> null
        }
    }
}
