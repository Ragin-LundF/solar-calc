package io.github.raginlundf.solarcalc.domain.services.summary

import io.github.raginlundf.solarcalc.dtos.summary.EnergyEfficiencyRating
import io.github.raginlundf.solarcalc.dtos.summary.SummaryResponse

interface SummaryService {

    /**
     * Computes the full savings summary. Per-month enrichment and cumulative payback use ALL months;
     * the returned month list and aggregates are limited to [rangeStart]..[rangeEnd] (inclusive, YYYY-MM).
     */
    fun summarize(
        months: List<SummaryMonthInput>,
        params: SummaryParams,
        rangeStart: String?,
        rangeEnd: String?,
        efficiency: EnergyEfficiencyRating,
    ): SummaryResponse
}
