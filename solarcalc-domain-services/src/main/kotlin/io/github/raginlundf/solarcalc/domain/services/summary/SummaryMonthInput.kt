package io.github.raginlundf.solarcalc.domain.services.summary

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
    /**
     * The standing contract price for this month, i.e. the same resolution as [gridPrice] but
     * *without* the per-month override. Null when no contract price is configured at all, in
     * which case there is nothing to compare the dynamic tariff against.
     */
    val referencePrice: BigDecimal?,
    val feedInTariff: BigDecimal,
    val petrolPrice: BigDecimal,
    /** Annual heating reference cost (€/year), 0 when no heating reference is configured. */
    val heizReferenzJahr: BigDecimal,
)
