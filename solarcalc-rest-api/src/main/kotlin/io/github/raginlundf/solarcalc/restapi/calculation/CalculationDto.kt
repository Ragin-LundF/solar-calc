package io.github.raginlundf.solarcalc.restapi.calculation

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationCategory
import io.github.raginlundf.solarcalc.domain.models.calculation.CompletenessFlag
import jakarta.validation.constraints.NotEmpty
import java.math.BigDecimal

data class CalculationResponse(
    val period: String,
    val allocationPriority: List<AllocationCategory>,
    val feedInKwh: BigDecimal,
    val feedInRevenue: BigDecimal?,
    val selfConsumptionPoolKwh: BigDecimal,
    val unallocatedKwh: BigDecimal,
    val householdAllocatedKwh: BigDecimal,
    val householdGridKwh: BigDecimal,
    val householdSavings: BigDecimal?,
    val heatPumpAllocatedKwh: BigDecimal?,
    val heatPumpGridKwh: BigDecimal?,
    val heatPumpElectricitySavings: BigDecimal?,
    val heatPumpHeatingReferenceSavings: BigDecimal?,
    val wallboxAllocatedKwh: BigDecimal?,
    val wallboxGridKwh: BigDecimal?,
    val wallboxElectricitySavings: BigDecimal?,
    val wallboxPetrolSavings: BigDecimal?,
    val totalElectricitySavings: BigDecimal?,
    val completenessFlags: Set<CompletenessFlag>,
)

data class ScenarioComparisonRequest(
    @field:NotEmpty val scenarios: List<List<AllocationCategory>>,
)
