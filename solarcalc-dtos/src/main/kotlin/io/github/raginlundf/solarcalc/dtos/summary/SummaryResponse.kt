@file:UseSerializers(BigDecimalSerializer::class)

package io.github.raginlundf.solarcalc.dtos.summary

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationCategory
import io.github.raginlundf.solarcalc.dtos.serialization.BigDecimalSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.math.BigDecimal

/**
 * Server-computed savings summary for a filtered date range.
 * All monetary values are in EUR, energy in kWh. See `.plan/design/README.md` for the math.
 */
@Serializable
data class SummaryResponse(
    val allocationPriority: List<AllocationCategory>,
    val months: List<MonthlySummary>,
    val aggregates: SummaryAggregates,
    val payback: PaybackProjection,
)

/** One month of enriched, per-consumer savings. Consumer values are 0 when the consumer is absent. */
@Serializable
data class MonthlySummary(
    val period: String,
    val generationKwh: BigDecimal,
    val feedInKwh: BigDecimal,
    val selfConsumedKwh: BigDecimal,
    val selfConsumptionQuotePct: BigDecimal,
    val feedInRevenue: BigDecimal,
    val householdConsumptionKwh: BigDecimal,
    val householdSolarKwh: BigDecimal,
    val householdGridKwh: BigDecimal,
    val householdCostWithoutSolar: BigDecimal,
    val householdCostWithSolar: BigDecimal,
    val householdSavings: BigDecimal,
    val heatPumpConsumptionKwh: BigDecimal,
    val heatPumpSolarKwh: BigDecimal,
    val heatPumpGridKwh: BigDecimal,
    val heatPumpCostWithSolar: BigDecimal,
    val heizPct: Int,
    val oilCost: BigDecimal,
    val heatingSavings: BigDecimal,
    val wallboxConsumptionKwh: BigDecimal,
    val wallboxSolarKwh: BigDecimal,
    val wallboxGridKwh: BigDecimal,
    val wallboxCostWithSolar: BigDecimal,
    val estimatedKm: BigDecimal,
    val gasolineEquivalentCost: BigDecimal,
    val wallboxSavingsVsGasoline: BigDecimal,
    val totalSavings: BigDecimal,
    /** Running total of totalSavings across all history up to and including this month. */
    val cumulativeSavings: BigDecimal,
)

/** Sums over the filtered range. */
@Serializable
data class SummaryAggregates(
    val generationKwh: BigDecimal,
    val selfConsumedKwh: BigDecimal,
    val feedInKwh: BigDecimal,
    val feedInRevenue: BigDecimal,
    val householdConsumptionKwh: BigDecimal,
    val householdCostWithoutSolar: BigDecimal,
    val householdCostWithSolar: BigDecimal,
    val householdSavings: BigDecimal,
    val oilCost: BigDecimal,
    val heatPumpCost: BigDecimal,
    val heatingSavings: BigDecimal,
    val wallboxConsumptionKwh: BigDecimal,
    val estimatedKm: BigDecimal,
    val gasolineEquivalentCost: BigDecimal,
    val wallboxChargingCost: BigDecimal,
    val wallboxSavingsVsGasoline: BigDecimal,
    val distributionSumPct: Int,
    val totalSavings: BigDecimal,
)

/** Cumulative payback, always computed over all history (independent of the range filter). */
@Serializable
data class PaybackProjection(
    val cumulativeSavings: BigDecimal,
    val investKosten: BigDecimal,
    val paybackPct: BigDecimal,
    val amortised: Boolean,
    /** Projected completion month (YYYY-MM), or null if amortised / not projectable. */
    val projectedPaybackPeriod: String?,
)
