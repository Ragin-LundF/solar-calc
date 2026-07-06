package io.github.raginlundf.solarcalc.domain.services.price

import io.github.raginlundf.solarcalc.domain.models.price.PriceSnapshot
import io.github.raginlundf.solarcalc.domain.models.repository.PriceSnapshotRepository
import io.github.raginlundf.solarcalc.domain.models.tenant.HeatingReferenceType
import io.github.raginlundf.solarcalc.domain.models.tenant.Tenant
import org.springframework.stereotype.Service
import java.math.BigDecimal

data class ResolvedPrices(
    val electricityPrice: BigDecimal?,
    val feedInTariff: BigDecimal?,
    val petrolPrice: BigDecimal?,
    val evEfficiencyKwh100km: BigDecimal?,
    val iceEfficiencyL100km: BigDecimal?,
    val heatingReferenceCost: BigDecimal?,
)

@Service
class PriceResolver(
    private val priceSnapshotRepository: PriceSnapshotRepository,
) {

    fun resolve(tenant: Tenant, tenantId: Long, energyProfileId: Long, period: String): ResolvedPrices {
        val monthlyOverride = priceSnapshotRepository
            .findByTenantIdAndEnergyProfileIdAndPeriod(
                tenantId = tenantId,
                energyProfileId = energyProfileId,
                period = period,
            )
        val profileDefault = priceSnapshotRepository
            .findByTenantIdAndEnergyProfileIdAndPeriodIsNull(
                tenantId = tenantId,
                energyProfileId = energyProfileId,
            )

        fun <T> pick(monthly: T?, profile: T?, default: T?): T? {
            return monthly ?: profile ?: default
        }

        return ResolvedPrices(
            electricityPrice = pick(
                monthly = monthlyOverride?.electricityPrice,
                profile = profileDefault?.electricityPrice,
                default = tenant.defaultElectricityPrice,
            ),
            feedInTariff = pick(
                monthly = monthlyOverride?.feedInTariff,
                profile = profileDefault?.feedInTariff,
                default = tenant.defaultFeedInTariff,
            ),
            petrolPrice = pick(
                monthly = monthlyOverride?.petrolPrice,
                profile = profileDefault?.petrolPrice,
                default = tenant.defaultPetrolPrice,
            ),
            evEfficiencyKwh100km = pick(
                monthly = monthlyOverride?.evEfficiencyKwh100km,
                profile = profileDefault?.evEfficiencyKwh100km,
                default = tenant.defaultEvEfficiencyKwh100km,
            ),
            iceEfficiencyL100km = pick(
                monthly = monthlyOverride?.iceEfficiencyL100km,
                profile = profileDefault?.iceEfficiencyL100km,
                default = tenant.defaultIceEfficiencyL100km,
            ),
            heatingReferenceCost = resolveHeatingReference(
                monthly = monthlyOverride,
                profile = profileDefault,
                tenant = tenant,
            ),
        )
    }

    private fun resolveHeatingReference(
        monthly: PriceSnapshot?,
        profile: PriceSnapshot?,
        tenant: Tenant,
    ): BigDecimal? {
        return when (tenant.heatingReferenceType) {
            HeatingReferenceType.OIL ->
                monthly?.oilReferenceCost ?: profile?.oilReferenceCost ?: tenant.defaultOilReferenceCost
            HeatingReferenceType.GAS ->
                monthly?.gasReferenceCost ?: profile?.gasReferenceCost ?: tenant.defaultGasReferenceCost
            HeatingReferenceType.NONE -> null
        }
    }
}
