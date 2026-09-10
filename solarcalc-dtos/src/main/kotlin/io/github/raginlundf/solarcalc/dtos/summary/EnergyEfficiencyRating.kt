package io.github.raginlundf.solarcalc.dtos.summary

import io.github.raginlundf.solarcalc.domain.models.profile.EnergyEfficiencyClassEnum
import java.math.BigDecimal

/**
 * Rough German energy efficiency estimate for the building, always computed over the last
 * 12 complete calendar months that carry a heat-pump reading (independent of the range filter).
 *
 * The whole chain is reported so a client can show it and the user can check it against the
 * meter: [heatPumpElectricityKwh] metered, minus the [hotWaterSharePercent] that went into hot
 * water, gives [heatingElectricityKwh], times the SCOP gives [heatingEnergyKwh]. Per square metre
 * and year that is [kwhPerSqmPerYear] of delivered heat (the building envelope, which drives
 * [energyClass]) and [finalEnergyKwhPerSqmPerYear] of electricity (the final energy an
 * Energieausweis would print for a heat-pump house).
 *
 * [energyClass] is null whenever the estimate cannot be made: fewer than 12 consecutive
 * months of readings, no heat pump, a missing/zero living area or SCOP, or a hot-water split that
 * is switched on without a usable share. [monthsConsidered] says how many of those 12 months
 * actually carry a reading, so a client can explain why.
 */
data class EnergyEfficiencyRating(
    val usableAreaSqm: BigDecimal?,
    val heatPumpScop: BigDecimal?,
    /** The hot-water share actually deducted, in percent; null when the heat pump is heating-only. */
    val hotWaterSharePercent: BigDecimal?,
    /** First month of the rated window as YYYY-MM; null when there is nothing to rate. */
    val windowStart: String?,
    /** Last month of the rated window as YYYY-MM; null when there is nothing to rate. */
    val windowEnd: String?,
    /** Metered heat-pump electricity summed over the window, in kWh. */
    val heatPumpElectricityKwh: BigDecimal,
    /** The space-heating part of it, after the hot-water deduction, in kWh. */
    val heatingElectricityKwh: BigDecimal,
    /** Heat delivered for space heating over the window (heating electricity x SCOP), in kWh. */
    val heatingEnergyKwh: BigDecimal,
    /** Delivered heat per square metre and year — the envelope figure the class is drawn from. */
    val kwhPerSqmPerYear: BigDecimal,
    /** Heating electricity per square metre and year — the final energy figure. */
    val finalEnergyKwhPerSqmPerYear: BigDecimal,
    val energyClass: EnergyEfficiencyClassEnum?,
    val monthsConsidered: Int,
)
