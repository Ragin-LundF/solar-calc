package io.github.raginlundf.solarcalc.dtos.input

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal

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
