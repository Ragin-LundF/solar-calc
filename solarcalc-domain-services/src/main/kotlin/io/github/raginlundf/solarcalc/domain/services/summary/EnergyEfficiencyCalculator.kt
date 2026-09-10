package io.github.raginlundf.solarcalc.domain.services.summary

import io.github.raginlundf.extensions.scale2
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyEfficiencyClassEnum
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfileEntity
import io.github.raginlundf.solarcalc.dtos.summary.EnergyEfficiencyRating
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.YearMonth

/**
 * Rough German energy efficiency estimate on the Energieausweis (GEG) scale.
 *
 * Metered heat-pump electricity is reduced by the hot-water share, so only the space-heating part
 * is rated, and then multiplied by the seasonal performance factor to obtain the heat actually
 * delivered to the building. That is rated per square metre and year. This deliberately rates the
 * *building*: the resulting figure is roughly what a gas or oil boiler would have had to deliver,
 * which is the basis the A+..H bands were drawn for. The electricity per square metre — what an
 * Energieausweis would print for a heat-pump house — is reported alongside it, but does not
 * decide the class.
 *
 * It stays an estimate in any case: the hot-water share is a flat percentage rather than a
 * measurement, and it ignores primary energy factors and the difference between living area and
 * the GEG reference area.
 *
 * @param clock decides which calendar month is still running; the running month is never rated,
 * because it is only partially recorded.
 */
@Service
class EnergyEfficiencyCalculator(
    private val clock: Clock = Clock.systemDefaultZone(),
) {

    private companion object {
        const val MONTHS_PER_YEAR = 12

        /** Intermediate scale, wide enough that the hot-water split does not lose kWh. */
        const val WORKING_SCALE = 6

        val HUNDRED: BigDecimal = BigDecimal("100")

        /** Inclusive upper bound of each band in kWh/(m2*a); anything above the last one is H. */
        val CLASS_UPPER_BOUNDS: List<Pair<EnergyEfficiencyClassEnum, BigDecimal>> = listOf(
            EnergyEfficiencyClassEnum.A_PLUS to BigDecimal("30"),
            EnergyEfficiencyClassEnum.A to BigDecimal("50"),
            EnergyEfficiencyClassEnum.B to BigDecimal("75"),
            EnergyEfficiencyClassEnum.C to BigDecimal("100"),
            EnergyEfficiencyClassEnum.D to BigDecimal("130"),
            EnergyEfficiencyClassEnum.E to BigDecimal("160"),
            EnergyEfficiencyClassEnum.F to BigDecimal("200"),
            EnergyEfficiencyClassEnum.G to BigDecimal("250"),
        )
    }

    /**
     * Rates the building from the raw heat-pump readings, keyed by YYYY-MM period.
     *
     * The map must only contain months that actually carry a reading — a month mapped to zero is
     * indistinguishable from a month the heat pump really did not run, and would silently drag the
     * annual figure down. Uses the 12 calendar months ending at the latest complete month that
     * carries a reading, and only produces a class when every one of those months is present: a
     * gap would make the "per year" figure meaningless.
     */
    fun rate(profile: EnergyProfileEntity, heatPumpKwhByPeriod: Map<String, BigDecimal>): EnergyEfficiencyRating {
        val area = profile.usableAreaSqm
        val scop = profile.heatPumpScop
        val hotWaterShare = hotWaterSharePercent(profile = profile)
        val window = latestFullYearPeriods(periods = heatPumpKwhByPeriod.keys)
        val monthsConsidered = window.count { heatPumpKwhByPeriod.containsKey(it) }

        val unrateable = !profile.hasHeatPump ||
            monthsConsidered < MONTHS_PER_YEAR ||
            area == null || area.signum() <= 0 ||
            scop == null || scop.signum() <= 0 ||
            (profile.heatPumpCoversHotWater && hotWaterShare == null)
        if (unrateable) {
            return unrated(area = area, scop = scop, hotWaterShare = hotWaterShare, months = monthsConsidered)
        }

        val electricity = window.fold(BigDecimal.ZERO) { acc, period ->
            acc + (heatPumpKwhByPeriod[period] ?: BigDecimal.ZERO)
        }
        val heatingElectricity = electricity
            .multiply(HUNDRED - (hotWaterShare ?: BigDecimal.ZERO))
            .divide(HUNDRED, WORKING_SCALE, RoundingMode.HALF_UP)
        val heatingEnergy = heatingElectricity * scop
        val perSqm = heatingEnergy.divide(area, 2, RoundingMode.HALF_UP)

        return EnergyEfficiencyRating(
            usableAreaSqm = area,
            heatPumpScop = scop,
            hotWaterSharePercent = hotWaterShare,
            windowStart = window.last(),
            windowEnd = window.first(),
            heatPumpElectricityKwh = electricity.scale2(),
            heatingElectricityKwh = heatingElectricity.scale2(),
            heatingEnergyKwh = heatingEnergy.scale2(),
            kwhPerSqmPerYear = perSqm,
            finalEnergyKwhPerSqmPerYear = heatingElectricity.divide(area, 2, RoundingMode.HALF_UP),
            energyClass = classify(kwhPerSqmPerYear = perSqm),
            monthsConsidered = monthsConsidered,
        )
    }

    private fun unrated(
        area: BigDecimal?,
        scop: BigDecimal?,
        hotWaterShare: BigDecimal?,
        months: Int,
    ): EnergyEfficiencyRating {
        return EnergyEfficiencyRating(
            usableAreaSqm = area,
            heatPumpScop = scop,
            hotWaterSharePercent = hotWaterShare,
            windowStart = null,
            windowEnd = null,
            heatPumpElectricityKwh = BigDecimal.ZERO.scale2(),
            heatingElectricityKwh = BigDecimal.ZERO.scale2(),
            heatingEnergyKwh = BigDecimal.ZERO.scale2(),
            kwhPerSqmPerYear = BigDecimal.ZERO.scale2(),
            finalEnergyKwhPerSqmPerYear = BigDecimal.ZERO.scale2(),
            energyClass = null,
            monthsConsidered = months,
        )
    }

    /** The configured share, or null when the heat pump is heating-only or the share is unusable. */
    private fun hotWaterSharePercent(profile: EnergyProfileEntity): BigDecimal? {
        if (!profile.heatPumpCoversHotWater) {
            return null
        }
        val share = profile.heatPumpHotWaterSharePercent ?: return null
        return share.takeIf { it.signum() >= 0 && it < HUNDRED }
    }

    /**
     * The 12 periods ending at the latest complete month that carries a reading, newest first;
     * empty when there is none. The running month is skipped because it is only partly recorded,
     * and the same filter keeps a stray future-dated row from shifting the whole window.
     */
    private fun latestFullYearPeriods(periods: Set<String>): List<String> {
        val currentMonth = YearMonth.now(clock).toString()
        val latest = periods.filter { it < currentMonth }.maxOrNull() ?: return emptyList()
        val end = YearMonth.parse(latest)
        return (0 until MONTHS_PER_YEAR).map { back -> end.minusMonths(back.toLong()).toString() }
    }

    private fun classify(kwhPerSqmPerYear: BigDecimal): EnergyEfficiencyClassEnum {
        val band = CLASS_UPPER_BOUNDS.firstOrNull { (_, upperBound) -> kwhPerSqmPerYear <= upperBound }
        return band?.first ?: EnergyEfficiencyClassEnum.H
    }
}
