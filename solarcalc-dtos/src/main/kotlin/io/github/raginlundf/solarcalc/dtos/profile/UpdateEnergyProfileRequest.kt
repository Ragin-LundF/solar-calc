@file:UseSerializers(BigDecimalSerializer::class)

package io.github.raginlundf.solarcalc.dtos.profile

import io.github.raginlundf.solarcalc.domain.models.profile.HeatingReferenceType
import io.github.raginlundf.solarcalc.domain.models.profile.OverviewLayout
import io.github.raginlundf.solarcalc.dtos.serialization.BigDecimalSerializer
import jakarta.validation.constraints.NotBlank
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.math.BigDecimal

@Serializable
data class UpdateEnergyProfileRequest(
    @field:NotBlank val name: String,
    val hasWallbox: Boolean = false,
    val hasHeatPump: Boolean = false,
    val heatingReferenceType: HeatingReferenceType = HeatingReferenceType.NONE,
    val defaultElectricityPrice: BigDecimal? = null,
    val defaultFeedInTariff: BigDecimal? = null,
    val defaultPetrolPrice: BigDecimal? = null,
    val defaultOilReferenceCost: BigDecimal? = null,
    val defaultGasReferenceCost: BigDecimal? = null,
    val kmPerKwh: BigDecimal? = null,
    val litersPer100km: BigDecimal? = null,
    val investKosten: BigDecimal? = null,
    val heatingMonthlyDistribution: List<Int>? = null,
    val overviewLayout: OverviewLayout? = null,
)
