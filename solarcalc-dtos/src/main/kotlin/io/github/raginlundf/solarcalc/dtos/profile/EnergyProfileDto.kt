@file:UseSerializers(BigDecimalSerializer::class)

package io.github.raginlundf.solarcalc.dtos.profile

import io.github.raginlundf.solarcalc.domain.models.profile.HeatingReferenceType
import io.github.raginlundf.solarcalc.dtos.serialization.BigDecimalSerializer
import jakarta.validation.constraints.NotBlank
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
)

@Serializable
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
)

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
)
