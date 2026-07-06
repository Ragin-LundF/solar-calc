package io.github.raginlundf.solarcalc.restapi.input

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal

data class MonthlyEnergyInputResponse(
    val id: Long,
    val tenantId: Long,
    val energyProfileId: Long,
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
    val evEfficiencyOverrideKwh100km: BigDecimal?,
    val iceEfficiencyOverrideL100km: BigDecimal?,
    val heatingReferenceCostOverride: BigDecimal?,
)

data class UpsertMonthlyEnergyInputRequest(
    @field:NotBlank
    @field:Pattern(regexp = "\\d{4}-\\d{2}", message = "Period must be in YYYY-MM format")
    val period: String,

    @field:PositiveOrZero val consumptionKwh: BigDecimal,
    @field:PositiveOrZero val generationKwh: BigDecimal,
    val feedInKwh: BigDecimal? = null,
    val householdConsumptionKwh: BigDecimal? = null,
    val heatPumpConsumptionKwh: BigDecimal? = null,
    val wallboxConsumptionKwh: BigDecimal? = null,
    val electricityPriceOverride: BigDecimal? = null,
    val feedInTariffOverride: BigDecimal? = null,
    val petrolPriceOverride: BigDecimal? = null,
    val evEfficiencyOverrideKwh100km: BigDecimal? = null,
    val iceEfficiencyOverrideL100km: BigDecimal? = null,
    val heatingReferenceCostOverride: BigDecimal? = null,
)
