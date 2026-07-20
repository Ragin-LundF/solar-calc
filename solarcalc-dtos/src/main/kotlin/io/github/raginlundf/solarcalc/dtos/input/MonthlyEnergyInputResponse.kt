package io.github.raginlundf.solarcalc.dtos.input

import java.math.BigDecimal

data class MonthlyEnergyInputResponse(
    val id: String,
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
