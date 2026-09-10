package io.github.raginlundf.solarcalc.domain.services.summary

import io.github.raginlundf.solarcalc.domain.models.profile.EnergyEfficiencyClassEnum
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfileEntity
import java.math.BigDecimal
import java.time.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EnergyEfficiencyCalculatorTest {

    private val calculator = EnergyEfficiencyCalculator()

    private fun profile(
        hasHeatPump: Boolean = true,
        usableAreaSqm: String? = "100",
        heatPumpScop: String? = "1",
    ): EnergyProfileEntity {
        return EnergyProfileEntity().apply {
            this.hasHeatPump = hasHeatPump
            this.usableAreaSqm = usableAreaSqm?.let { BigDecimal(it) }
            this.heatPumpScop = heatPumpScop?.let { BigDecimal(it) }
        }
    }

    /** [months] consecutive periods ending 2025-12, carrying [totalKwh] in the last one. */
    private fun readings(totalKwh: String, months: Int = 12): Map<String, BigDecimal> {
        val end = YearMonth.parse("2025-12")
        return (0 until months).associate { back ->
            val period = end.minusMonths(back.toLong()).toString()
            period to if (back == 0) BigDecimal(totalKwh) else BigDecimal.ZERO
        }
    }

    private fun classFor(totalKwh: String): EnergyEfficiencyClassEnum? {
        return calculator.rate(profile = profile(), heatPumpKwhByPeriod = readings(totalKwh)).energyClass
    }

    @Test
    fun `rates a full year of readings against the living area`() {
        // 4000 kWh electricity at SCOP 3.5 = 14000 kWh heat over 140 m2 = 100 kWh per m2 and year.
        val rating = calculator.rate(
            profile = profile(usableAreaSqm = "140", heatPumpScop = "3.5"),
            heatPumpKwhByPeriod = readings(totalKwh = "4000"),
        )

        assertEquals(expected = BigDecimal("14000.00"), actual = rating.heatingEnergyKwh)
        assertEquals(expected = BigDecimal("100.00"), actual = rating.kwhPerSqmPerYear)
        assertEquals(expected = EnergyEfficiencyClassEnum.C, actual = rating.energyClass)
        assertEquals(expected = 12, actual = rating.monthsConsidered)
    }

    @Test
    fun `sums every month of the window, not just the latest`() {
        val spread = (0 until 12).associate { back ->
            YearMonth.parse("2025-12").minusMonths(back.toLong()).toString() to BigDecimal("500")
        }

        val rating = calculator.rate(profile = profile(), heatPumpKwhByPeriod = spread)

        // 12 * 500 = 6000 kWh at SCOP 1 over 100 m2 = 60 kWh per m2 and year.
        assertEquals(expected = BigDecimal("6000.00"), actual = rating.heatingEnergyKwh)
        assertEquals(expected = BigDecimal("60.00"), actual = rating.kwhPerSqmPerYear)
        assertEquals(expected = EnergyEfficiencyClassEnum.B, actual = rating.energyClass)
    }

    @Test
    fun `places each band at its inclusive upper bound`() {
        assertEquals(expected = EnergyEfficiencyClassEnum.A_PLUS, actual = classFor("3000"))
        assertEquals(expected = EnergyEfficiencyClassEnum.A, actual = classFor("5000"))
        assertEquals(expected = EnergyEfficiencyClassEnum.B, actual = classFor("7500"))
        assertEquals(expected = EnergyEfficiencyClassEnum.C, actual = classFor("10000"))
        assertEquals(expected = EnergyEfficiencyClassEnum.D, actual = classFor("13000"))
        assertEquals(expected = EnergyEfficiencyClassEnum.E, actual = classFor("16000"))
        assertEquals(expected = EnergyEfficiencyClassEnum.F, actual = classFor("20000"))
        assertEquals(expected = EnergyEfficiencyClassEnum.G, actual = classFor("25000"))
    }

    @Test
    fun `moves to the next band just above each bound`() {
        assertEquals(expected = EnergyEfficiencyClassEnum.A, actual = classFor("3001"))
        assertEquals(expected = EnergyEfficiencyClassEnum.B, actual = classFor("5001"))
        assertEquals(expected = EnergyEfficiencyClassEnum.C, actual = classFor("7501"))
        assertEquals(expected = EnergyEfficiencyClassEnum.D, actual = classFor("10001"))
        assertEquals(expected = EnergyEfficiencyClassEnum.E, actual = classFor("13001"))
        assertEquals(expected = EnergyEfficiencyClassEnum.F, actual = classFor("16001"))
        assertEquals(expected = EnergyEfficiencyClassEnum.G, actual = classFor("20001"))
        assertEquals(expected = EnergyEfficiencyClassEnum.H, actual = classFor("25001"))
    }

    @Test
    fun `refuses to rate a partial year`() {
        val rating = calculator.rate(
            profile = profile(),
            heatPumpKwhByPeriod = readings(totalKwh = "3000", months = 11),
        )

        assertNull(actual = rating.energyClass)
        assertEquals(expected = 11, actual = rating.monthsConsidered)
    }

    @Test
    fun `refuses to rate a window with a gap in the middle`() {
        val withGap = readings(totalKwh = "3000").minus("2025-06")

        val rating = calculator.rate(profile = profile(), heatPumpKwhByPeriod = withGap)

        assertNull(actual = rating.energyClass)
        assertEquals(expected = 11, actual = rating.monthsConsidered)
    }

    @Test
    fun `ignores readings older than the twelve month window`() {
        val withAncientHistory = readings(totalKwh = "3000") + mapOf("2019-01" to BigDecimal("99999"))

        val rating = calculator.rate(profile = profile(), heatPumpKwhByPeriod = withAncientHistory)

        assertEquals(expected = BigDecimal("30.00"), actual = rating.kwhPerSqmPerYear)
        assertEquals(expected = EnergyEfficiencyClassEnum.A_PLUS, actual = rating.energyClass)
    }

    @Test
    fun `does not rate a profile without a heat pump`() {
        val rating = calculator.rate(
            profile = profile(hasHeatPump = false),
            heatPumpKwhByPeriod = readings(totalKwh = "3000"),
        )

        // Without a heat pump the heating energy is unknown, not zero — reporting A+ would be a lie.
        assertNull(actual = rating.energyClass)
        assertEquals(expected = BigDecimal("0.00"), actual = rating.kwhPerSqmPerYear)
    }

    @Test
    fun `does not rate without a living area`() {
        val rating = calculator.rate(
            profile = profile(usableAreaSqm = null),
            heatPumpKwhByPeriod = readings(totalKwh = "3000"),
        )

        assertNull(actual = rating.energyClass)
    }

    @Test
    fun `does not divide by a zero living area`() {
        val rating = calculator.rate(
            profile = profile(usableAreaSqm = "0"),
            heatPumpKwhByPeriod = readings(totalKwh = "3000"),
        )

        assertNull(actual = rating.energyClass)
        assertEquals(expected = BigDecimal("0.00"), actual = rating.kwhPerSqmPerYear)
    }

    @Test
    fun `does not rate without a seasonal performance factor`() {
        val rating = calculator.rate(
            profile = profile(heatPumpScop = null),
            heatPumpKwhByPeriod = readings(totalKwh = "3000"),
        )

        assertNull(actual = rating.energyClass)
    }

    @Test
    fun `does not rate a profile with no readings at all`() {
        val rating = calculator.rate(profile = profile(), heatPumpKwhByPeriod = emptyMap())

        assertNull(actual = rating.energyClass)
        assertEquals(expected = 0, actual = rating.monthsConsidered)
    }

    @Test
    fun `echoes the inputs back so a client can explain the result`() {
        val rating = calculator.rate(
            profile = profile(usableAreaSqm = "140", heatPumpScop = "3.5"),
            heatPumpKwhByPeriod = readings(totalKwh = "4000"),
        )

        assertEquals(expected = BigDecimal("140"), actual = rating.usableAreaSqm)
        assertEquals(expected = BigDecimal("3.5"), actual = rating.heatPumpScop)
    }
}
