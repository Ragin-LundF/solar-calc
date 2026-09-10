package io.github.raginlundf.solarcalc.domain.services.summary

import io.github.raginlundf.extensions.scale2
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyEfficiencyClass
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfile
import io.github.raginlundf.solarcalc.dtos.summary.EnergyEfficiencyRating
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.YearMonth

/**
 * Rough German energy efficiency estimate on the Energieausweis (GEG) scale.
 *
 * Metered heat-pump electricity is multiplied by the seasonal performance factor to obtain the
 * heat actually delivered to the building, and that is rated per square metre and year. This
 * deliberately rates the *building*: the resulting figure is roughly what a gas or oil boiler
 * would have had to deliver, which is the basis the A+..H bands were drawn for. It is therefore
 * not the value an Energieausweis would print for a heat-pump house, where the final energy is
 * the (far lower) electricity itself.
 *
 * It stays an estimate in any case: it ignores the split between space heating and hot water,
 * primary energy factors, and the difference between living area and the GEG reference area.
 */
@Service
class EnergyEfficiencyCalculator {

    private companion object {
        const val MONTHS_PER_YEAR = 12

        /** Inclusive upper bound of each band in kWh/(m2*a); anything above the last one is H. */
        val CLASS_UPPER_BOUNDS: List<Pair<EnergyEfficiencyClass, BigDecimal>> = listOf(
            EnergyEfficiencyClass.A_PLUS to BigDecimal("30"),
            EnergyEfficiencyClass.A to BigDecimal("50"),
            EnergyEfficiencyClass.B to BigDecimal("75"),
            EnergyEfficiencyClass.C to BigDecimal("100"),
            EnergyEfficiencyClass.D to BigDecimal("130"),
            EnergyEfficiencyClass.E to BigDecimal("160"),
            EnergyEfficiencyClass.F to BigDecimal("200"),
            EnergyEfficiencyClass.G to BigDecimal("250"),
        )
    }

    /**
     * Rates the building from the raw heat-pump readings, keyed by YYYY-MM period.
     * Uses the 12 calendar months ending at the latest period that carries data, and only
     * produces a class when every one of those months is present — a gap would make the
     * "per year" figure meaningless.
     */
    fun rate(profile: EnergyProfile, heatPumpKwhByPeriod: Map<String, BigDecimal>): EnergyEfficiencyRating {
        val area = profile.usableAreaSqm
        val scop = profile.heatPumpScop
        val window = latestFullYearPeriods(periods = heatPumpKwhByPeriod.keys)
        val monthsConsidered = window.count { heatPumpKwhByPeriod.containsKey(it) }

        val unrateable = !profile.hasHeatPump ||
            monthsConsidered < MONTHS_PER_YEAR ||
            area == null || area.signum() <= 0 ||
            scop == null || scop.signum() <= 0
        if (unrateable) {
            return EnergyEfficiencyRating(
                usableAreaSqm = area,
                heatPumpScop = scop,
                heatingEnergyKwh = BigDecimal.ZERO.scale2(),
                kwhPerSqmPerYear = BigDecimal.ZERO.scale2(),
                energyClass = null,
                monthsConsidered = monthsConsidered,
            )
        }

        val electricity = window.fold(BigDecimal.ZERO) { acc, period ->
            acc + (heatPumpKwhByPeriod[period] ?: BigDecimal.ZERO)
        }
        val heatingEnergy = electricity * scop
        val perSqm = heatingEnergy.divide(area, 2, RoundingMode.HALF_UP)

        return EnergyEfficiencyRating(
            usableAreaSqm = area,
            heatPumpScop = scop,
            heatingEnergyKwh = heatingEnergy.scale2(),
            kwhPerSqmPerYear = perSqm,
            energyClass = classify(kwhPerSqmPerYear = perSqm),
            monthsConsidered = monthsConsidered,
        )
    }

    /** The 12 periods ending at the latest one present, newest first; empty when there is no data. */
    private fun latestFullYearPeriods(periods: Set<String>): List<String> {
        val latest = periods.maxOrNull() ?: return emptyList()
        val end = YearMonth.parse(latest)
        return (0 until MONTHS_PER_YEAR).map { back -> end.minusMonths(back.toLong()).toString() }
    }

    private fun classify(kwhPerSqmPerYear: BigDecimal): EnergyEfficiencyClass {
        val band = CLASS_UPPER_BOUNDS.firstOrNull { (_, upperBound) -> kwhPerSqmPerYear <= upperBound }
        return band?.first ?: EnergyEfficiencyClass.H
    }
}
