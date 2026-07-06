@file:UseSerializers(BigDecimalSerializer::class)

package io.github.raginlundf.solarcalc.dtos.input

import io.github.raginlundf.solarcalc.dtos.serialization.BigDecimalSerializer
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.PositiveOrZero
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.math.BigDecimal

@Serializable
data class MonthlyEnergyInputResponse(
    val id: Long,
    val energyProfileUuid: String,
    val period: String,
    val consumptionKwh: BigDecimal,
    val generationKwh: BigDecimal,
    val feedInKwh: BigDecimal?,
    val householdConsumptionKwh: BigDecimal?,
    val heatPumpConsumptionKwh: BigDecimal?,
    val wallboxConsumptionKwh: BigDecimal?,
    val electricityPriceOverride: BigDecimal?,
    val feedInTariffOverride: BigDecimal?,
    val petrolPriceOverride: BigDecimal?,
    val heatingReferenceCostOverride: BigDecimal?,
)

@Serializable
data class UpsertMonthlyEnergyInputRequest(
    @field:NotBlank
    @field:Pattern(regexp = "\\d{4}-\\d{2}", message = "Period must be in YYYY-MM format")
    val period: String,

    @field:PositiveOrZero val generationKwh: BigDecimal,
    val feedInKwh: BigDecimal? = null,
    val householdConsumptionKwh: BigDecimal? = null,
    val heatPumpConsumptionKwh: BigDecimal? = null,
    val wallboxConsumptionKwh: BigDecimal? = null,
    val electricityPriceOverride: BigDecimal? = null,
    val feedInTariffOverride: BigDecimal? = null,
    val petrolPriceOverride: BigDecimal? = null,
    val heatingReferenceCostOverride: BigDecimal? = null,
)
