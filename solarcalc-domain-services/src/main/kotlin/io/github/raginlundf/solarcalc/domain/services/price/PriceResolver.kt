package io.github.raginlundf.solarcalc.domain.services.price

import io.github.raginlundf.solarcalc.domain.models.price.PriceSnapshot
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfile
import io.github.raginlundf.solarcalc.domain.models.repository.PriceSnapshotRepository
import io.github.raginlundf.solarcalc.domain.models.profile.HeatingReferenceType
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class PriceResolver(
    private val priceSnapshotRepository: PriceSnapshotRepository,
) {

    fun resolve(profile: EnergyProfile, period: String): ResolvedPrices {
        val monthlyOverride = priceSnapshotRepository
            .findByEnergyProfileIdAndPeriod(
                energyProfileId = profile.id!!,
                period = period,
            )
        val profileDefault = priceSnapshotRepository
            .findByEnergyProfileIdAndPeriodIsNull(
                energyProfileId = profile.id!!,
            )

        fun <T> pick(monthly: T?, profileDefault: T?, default: T?): T? {
            return monthly ?: profileDefault ?: default
        }

        return ResolvedPrices(
            electricityPrice = pick(
                monthly = monthlyOverride?.electricityPrice,
                profileDefault = profileDefault?.electricityPrice,
                default = profile.defaultElectricityPrice,
            ),
            feedInTariff = pick(
                monthly = monthlyOverride?.feedInTariff,
                profileDefault = profileDefault?.feedInTariff,
                default = profile.defaultFeedInTariff,
            ),
            petrolPrice = pick(
                monthly = monthlyOverride?.petrolPrice,
                profileDefault = profileDefault?.petrolPrice,
                default = profile.defaultPetrolPrice,
            ),
            heatingReferenceCost = resolveHeatingReference(
                monthly = monthlyOverride,
                profileDefault = profileDefault,
                profile = profile,
            ),
        )
    }

    private fun resolveHeatingReference(
        monthly: PriceSnapshot?,
        profileDefault: PriceSnapshot?,
        profile: EnergyProfile,
    ): BigDecimal? {
        return when (profile.heatingReferenceType) {
            HeatingReferenceType.OIL ->
                monthly?.oilReferenceCost ?: profileDefault?.oilReferenceCost ?: profile.defaultOilReferenceCost
            HeatingReferenceType.GAS ->
                monthly?.gasReferenceCost ?: profileDefault?.gasReferenceCost ?: profile.defaultGasReferenceCost
            HeatingReferenceType.NONE -> null
        }
    }
}
