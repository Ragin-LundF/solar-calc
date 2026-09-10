package io.github.raginlundf.solarcalc.dtos.profile

import io.github.raginlundf.solarcalc.domain.models.profile.HeatingReferenceType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal

data class CreateEnergyProfileRequest(
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
    @field:PositiveOrZero val usableAreaSqm: BigDecimal? = null,
    @field:Positive val heatPumpScop: BigDecimal? = null,
)
