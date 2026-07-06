@file:UseSerializers(BigDecimalSerializer::class)

package io.github.raginlundf.solarcalc.restapi.calculation

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationCategory
import io.github.raginlundf.solarcalc.domain.models.calculation.CompletenessFlag
import io.github.raginlundf.solarcalc.restapi.serialization.BigDecimalSerializer
import jakarta.validation.constraints.NotEmpty
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.math.BigDecimal

@Serializable
data class CalculationResponse(
    val period: String,
    val calculationRunId: Long? = null,
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

@Serializable
data class ScenarioComparisonRequest(
    @field:NotEmpty val scenarios: List<List<AllocationCategory>>,
)
