package io.github.raginlundf.solarcalc.dtos.summary

import io.github.raginlundf.solarcalc.domain.models.profile.EnergyEfficiencyClassEnum
import java.math.BigDecimal

/**
 * Rough German energy efficiency estimate for the building, always computed over the last
 * 12 calendar months of all history (independent of the range filter).
 *
 * [energyClass] is null whenever the estimate cannot be made: fewer than 12 consecutive
 * months of data, no heat pump, or a missing/zero living area or SCOP. [monthsConsidered]
 * says how many of those 12 months actually carry data, so a client can explain why.
 */
data class EnergyEfficiencyRating(
    val usableAreaSqm: BigDecimal?,
    val heatPumpScop: BigDecimal?,
    /** Heat delivered to the building over the window (heat-pump electricity x SCOP), in kWh. */
    val heatingEnergyKwh: BigDecimal,
    val kwhPerSqmPerYear: BigDecimal,
    val energyClass: EnergyEfficiencyClassEnum?,
    val monthsConsidered: Int,
)
