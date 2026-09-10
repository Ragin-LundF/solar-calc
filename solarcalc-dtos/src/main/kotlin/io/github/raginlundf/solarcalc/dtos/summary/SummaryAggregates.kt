package io.github.raginlundf.solarcalc.dtos.summary

import java.math.BigDecimal

/** Sums over the filtered range. */
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
    val gridKwh: BigDecimal,
    val gridCost: BigDecimal,
    val gridCostAtReferencePrice: BigDecimal,
    val dynamicTariffDelta: BigDecimal,
    /** Weighted by kWh, not a plain average of the monthly prices. */
    val averagePurchasePricePerKwh: BigDecimal,
    /** Weighted contract price over the same range, for a like-for-like comparison. */
    val referencePricePerKwh: BigDecimal,
)
