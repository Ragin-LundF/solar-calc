package io.github.raginlundf.solarcalc.domain.services.summary

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationCategory
import io.github.raginlundf.solarcalc.dtos.summary.SummaryResponse
import java.math.BigDecimal

/** Raw readings + resolved prices for a single month. */
data class SummaryMonthInput(
    val period: String,
    val generationKwh: BigDecimal,
    val feedInKwh: BigDecimal,
    val consumptionKwh: BigDecimal,
    val householdKwh: BigDecimal?,
    val heatPumpKwh: BigDecimal,
    val wallboxKwh: BigDecimal,
    val gridPrice: BigDecimal,
    val referencePrice: BigDecimal,
    val feedInTariff: BigDecimal,
    val petrolPrice: BigDecimal,
    /** Annual heating reference cost (€/year), 0 when no heating reference is configured. */
    val heizReferenzJahr: BigDecimal,
)

/** Profile-level settings that drive the savings math. */
data class SummaryParams(
    val allocationPriority: List<AllocationCategory>,
    val hasHeatPump: Boolean,
    val hasWallbox: Boolean,
    val kmPerKwh: BigDecimal,
    val litersPer100km: BigDecimal,
    val investKosten: BigDecimal,
    /** 12 percentages (Jan..Dec) of the annual heating cost. */
    val heatingDistribution: List<Int>,
)

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
    ): SummaryResponse
}
