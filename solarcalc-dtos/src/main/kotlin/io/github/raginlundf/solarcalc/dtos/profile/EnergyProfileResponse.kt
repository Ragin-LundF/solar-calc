package io.github.raginlundf.solarcalc.dtos.profile

import io.github.raginlundf.solarcalc.domain.models.profile.HeatingReferenceTypeEnum
import io.github.raginlundf.solarcalc.domain.models.profile.OverviewLayoutEnum
import java.math.BigDecimal

data class EnergyProfileResponse(
    val id: String,
    val name: String,
    val hasWallbox: Boolean,
    val hasHeatPump: Boolean,
    val heatingReferenceType: HeatingReferenceTypeEnum,
    val defaultElectricityPrice: BigDecimal?,
    val defaultFeedInTariff: BigDecimal?,
    val defaultPetrolPrice: BigDecimal?,
    val defaultOilReferenceCost: BigDecimal?,
    val defaultGasReferenceCost: BigDecimal?,
    val kmPerKwh: BigDecimal?,
    val litersPer100km: BigDecimal?,
    val investKosten: BigDecimal?,
    val usableAreaSqm: BigDecimal?,
    val heatPumpScop: BigDecimal?,
    val heatingMonthlyDistribution: List<Int>,
    val overviewLayout: OverviewLayoutEnum,
)
