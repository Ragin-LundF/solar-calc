package io.github.raginlundf.solarcalc.domain.services.calculation

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationCategory
import java.math.BigDecimal

data class CalculationResult(
    val period: String,
    val allocationPriority: List<AllocationCategory>,

    // Feed-in
    val feedInKwh: BigDecimal,
    val feedInRevenue: BigDecimal?,

    // Pool
    val selfConsumptionPoolKwh: BigDecimal,
    val unallocatedKwh: BigDecimal,

    // Household
    val householdAllocatedKwh: BigDecimal,
    val householdGridKwh: BigDecimal,
    val householdSavings: BigDecimal?,

    // Heat pump (null if no heat pump)
    val heatPumpAllocatedKwh: BigDecimal?,
    val heatPumpGridKwh: BigDecimal?,
    val heatPumpElectricitySavings: BigDecimal?,
    val heatPumpHeatingReferenceSavings: BigDecimal?,

    // Wallbox (null if no wallbox)
    val wallboxAllocatedKwh: BigDecimal?,
    val wallboxGridKwh: BigDecimal?,
    val wallboxElectricitySavings: BigDecimal?,

    // Totals
    val totalElectricitySavings: BigDecimal?,

    val completeness: CalculationCompleteness,
)
