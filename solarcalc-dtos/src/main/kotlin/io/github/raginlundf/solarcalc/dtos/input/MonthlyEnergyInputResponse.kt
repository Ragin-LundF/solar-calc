@file:UseSerializers(BigDecimalSerializer::class)

package io.github.raginlundf.solarcalc.dtos.input

import io.github.raginlundf.solarcalc.dtos.serialization.BigDecimalSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.math.BigDecimal

@Serializable
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
    val referencePrice: BigDecimal?,
    val electricityPriceOverride: BigDecimal?,
    val feedInTariffOverride: BigDecimal?,
    val petrolPriceOverride: BigDecimal?,
    val heatingReferenceCostOverride: BigDecimal?,
)
