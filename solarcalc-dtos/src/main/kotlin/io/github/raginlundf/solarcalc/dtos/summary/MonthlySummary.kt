package io.github.raginlundf.solarcalc.dtos.summary

import java.math.BigDecimal

/** One month of enriched, per-consumer savings. Consumer values are 0 when the consumer is absent. */
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
    /** Total energy purchased from the grid: household + heat pump + wallbox. */
    val gridKwh: BigDecimal,
    /** Price actually paid per kWh this month (the dynamic tariff average, or the contract price). */
    val purchasePricePerKwh: BigDecimal,
    val gridCost: BigDecimal,
    /** What the same [gridKwh] would have cost at the standing contract price. */
    val gridCostAtReferencePrice: BigDecimal,
    /** gridCostAtReferencePrice - gridCost; positive means the dynamic tariff was cheaper. */
    val dynamicTariffDelta: BigDecimal,
    /** Running total of totalSavings across all history up to and including this month. */
    val cumulativeSavings: BigDecimal,
)
