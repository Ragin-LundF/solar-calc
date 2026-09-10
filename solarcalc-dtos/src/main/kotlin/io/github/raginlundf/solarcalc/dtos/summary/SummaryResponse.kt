package io.github.raginlundf.solarcalc.dtos.summary

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationCategory

/**
 * Server-computed savings summary for a filtered date range.
 * All monetary values are in EUR, energy in kWh. See `.plan/design/README.md` for the math.
 */
data class SummaryResponse(
    val allocationPriority: List<AllocationCategory>,
    val months: List<MonthlySummary>,
    val aggregates: SummaryAggregates,
    val payback: PaybackProjection,
    val efficiency: EnergyEfficiencyRating,
)
