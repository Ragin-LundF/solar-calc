package io.github.raginlundf.solarcalc.domain.services.price

import io.github.raginlundf.solarcalc.domain.models.price.PriceSnapshot
import io.github.raginlundf.solarcalc.domain.models.repository.PriceSnapshotRepository
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

    /**
     * Resolves prices for a given period using precedence:
     * monthly override -> profile default -> tenant default
     */
    fun resolve(tenant: Tenant, tenantId: Long, energyProfileId: Long, period: String): ResolvedPrices {
        val monthlyOverride = priceSnapshotRepository
            .findByTenantIdAndEnergyProfileIdAndPeriod(tenantId, energyProfileId, period)
        val profileDefault = priceSnapshotRepository
            .findByTenantIdAndEnergyProfileIdAndPeriodIsNull(tenantId, energyProfileId)

        fun <T> resolve(monthly: T?, profile: T?, tenant: T?): T? = monthly ?: profile ?: tenant

        return ResolvedPrices(
            electricityPrice = resolve(monthlyOverride?.electricityPrice, profileDefault?.electricityPrice, tenant.defaultElectricityPrice),
            feedInTariff = resolve(monthlyOverride?.feedInTariff, profileDefault?.feedInTariff, tenant.defaultFeedInTariff),
            petrolPrice = resolve(monthlyOverride?.petrolPrice, profileDefault?.petrolPrice, tenant.defaultPetrolPrice),
            evEfficiencyKwh100km = resolve(monthlyOverride?.evEfficiencyKwh100km, profileDefault?.evEfficiencyKwh100km, tenant.defaultEvEfficiencyKwh100km),
            iceEfficiencyL100km = resolve(monthlyOverride?.iceEfficiencyL100km, profileDefault?.iceEfficiencyL100km, tenant.defaultIceEfficiencyL100km),
            heatingReferenceCost = resolveHeatingReference(monthlyOverride, profileDefault, tenant),
        )
    }

    private fun resolveHeatingReference(
        monthly: PriceSnapshot?,
        profile: PriceSnapshot?,
        tenant: Tenant,
    ): BigDecimal? {
        return when (tenant.heatingReferenceType) {
            io.github.raginlundf.solarcalc.domain.models.tenant.HeatingReferenceType.OIL ->
                monthly?.oilReferenceCost ?: profile?.oilReferenceCost ?: tenant.defaultOilReferenceCost
            io.github.raginlundf.solarcalc.domain.models.tenant.HeatingReferenceType.GAS ->
                monthly?.gasReferenceCost ?: profile?.gasReferenceCost ?: tenant.defaultGasReferenceCost
            io.github.raginlundf.solarcalc.domain.models.tenant.HeatingReferenceType.NONE -> null
        }
    }
}
