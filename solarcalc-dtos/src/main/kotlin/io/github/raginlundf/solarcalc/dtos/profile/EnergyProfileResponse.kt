@file:UseSerializers(BigDecimalSerializer::class)

package io.github.raginlundf.solarcalc.dtos.profile

import io.github.raginlundf.solarcalc.domain.models.profile.HeatingReferenceType
import io.github.raginlundf.solarcalc.domain.models.profile.OverviewLayout
import io.github.raginlundf.solarcalc.dtos.serialization.BigDecimalSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.math.BigDecimal

@Serializable
data class EnergyProfileResponse(
    val id: String,
    val name: String,
    val hasWallbox: Boolean,
    val hasHeatPump: Boolean,
    val heatingReferenceType: HeatingReferenceType,
    val defaultElectricityPrice: BigDecimal?,
    val defaultFeedInTariff: BigDecimal?,
    val defaultPetrolPrice: BigDecimal?,
    val defaultOilReferenceCost: BigDecimal?,
    val defaultGasReferenceCost: BigDecimal?,
    val kmPerKwh: BigDecimal?,
    val litersPer100km: BigDecimal?,
    val investKosten: BigDecimal?,
    val heatingMonthlyDistribution: List<Int>,
    val overviewLayout: OverviewLayout,
)
