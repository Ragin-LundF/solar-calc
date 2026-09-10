package io.github.raginlundf.solarcalc.domain.services.summary

import io.github.raginlundf.solarcalc.domain.models.profile.EnergyEfficiencyClassEnum
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfileEntity
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EnergyEfficiencyCalculatorTest {

    /** Inside 2026-01, so the readings below (ending 2025-12) are all complete months. */
    private val calculator = EnergyEfficiencyCalculator(
        clock = Clock.fixed(Instant.parse("2026-01-15T00:00:00Z"), ZoneOffset.UTC),
    )

    private fun profile(
        hasHeatPump: Boolean = true,
        usableAreaSqm: String? = "100",
        heatPumpScop: String? = "1",
        coversHotWater: Boolean = false,
        hotWaterSharePercent: String? = null,
    ): EnergyProfileEntity {
        return EnergyProfileEntity().apply {
            this.hasHeatPump = hasHeatPump
            this.usableAreaSqm = usableAreaSqm?.let { BigDecimal(it) }
            this.heatPumpScop = heatPumpScop?.let { BigDecimal(it) }
            this.heatPumpCoversHotWater = coversHotWater
            this.heatPumpHotWaterSharePercent = hotWaterSharePercent?.let { BigDecimal(it) }
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

    @Test
    fun `anchors on the newest complete month that carries a reading`() {
        // 2025-12 has no reading at all, so the window has to end at 2025-11.
        val endingInNovember = (0 until 12).associate { back ->
            YearMonth.parse("2025-11").minusMonths(back.toLong()).toString() to BigDecimal("500")
        }

        val rating = calculator.rate(profile = profile(), heatPumpKwhByPeriod = endingInNovember)

        assertEquals(expected = "2024-12", actual = rating.windowStart)
        assertEquals(expected = "2025-11", actual = rating.windowEnd)
        assertEquals(expected = 12, actual = rating.monthsConsidered)
        assertEquals(expected = BigDecimal("6000.00"), actual = rating.heatPumpElectricityKwh)
    }

    @Test
    fun `never ends the window on the running month`() {
        // The clock is inside 2026-01, so that month is only partially recorded and must not count.
        val intoJanuary = readings(totalKwh = "3000") + mapOf("2026-01" to BigDecimal("400"))

        val rating = calculator.rate(profile = profile(), heatPumpKwhByPeriod = intoJanuary)

        assertEquals(expected = "2025-12", actual = rating.windowEnd)
        assertEquals(expected = BigDecimal("3000.00"), actual = rating.heatPumpElectricityKwh)
    }

    @Test
    fun `ignores a future dated reading`() {
        val withFuture = readings(totalKwh = "3000") + mapOf("2027-06" to BigDecimal("99999"))

        val rating = calculator.rate(profile = profile(), heatPumpKwhByPeriod = withFuture)

        assertEquals(expected = "2025-12", actual = rating.windowEnd)
        assertEquals(expected = EnergyEfficiencyClassEnum.A_PLUS, actual = rating.energyClass)
    }

    @Test
    fun `takes the hot water share out before applying the performance factor`() {
        // 9000 kWh electricity, 20 % of it hot water: 7200 kWh heating at SCOP 3.5 = 25200 kWh heat.
        val rating = calculator.rate(
            profile = profile(
                usableAreaSqm = "140",
                heatPumpScop = "3.5",
                coversHotWater = true,
                hotWaterSharePercent = "20",
            ),
            heatPumpKwhByPeriod = readings(totalKwh = "9000"),
        )

        assertEquals(expected = BigDecimal("9000.00"), actual = rating.heatPumpElectricityKwh)
        assertEquals(expected = BigDecimal("7200.00"), actual = rating.heatingElectricityKwh)
        assertEquals(expected = BigDecimal("25200.00"), actual = rating.heatingEnergyKwh)
        assertEquals(expected = BigDecimal("20"), actual = rating.hotWaterSharePercent)
        assertEquals(expected = BigDecimal("180.00"), actual = rating.kwhPerSqmPerYear)
        assertEquals(expected = BigDecimal("51.43"), actual = rating.finalEnergyKwhPerSqmPerYear)
        assertEquals(expected = EnergyEfficiencyClassEnum.F, actual = rating.energyClass)
    }

    @Test
    fun `deducts nothing while the heat pump is heating only, even with a share stored`() {
        val rating = calculator.rate(
            profile = profile(coversHotWater = false, hotWaterSharePercent = "20"),
            heatPumpKwhByPeriod = readings(totalKwh = "3000"),
        )

        assertNull(actual = rating.hotWaterSharePercent)
        assertEquals(expected = BigDecimal("3000.00"), actual = rating.heatingElectricityKwh)
    }

    @Test
    fun `does not rate a hot water split without a share`() {
        val rating = calculator.rate(
            profile = profile(coversHotWater = true, hotWaterSharePercent = null),
            heatPumpKwhByPeriod = readings(totalKwh = "3000"),
        )

        // Rating it as if all the electricity were heating would overstate the building by the share.
        assertNull(actual = rating.energyClass)
        assertEquals(expected = 12, actual = rating.monthsConsidered)
    }

    @Test
    fun `does not rate a hot water share outside its range`() {
        listOf("-1", "100", "150").forEach { share ->
            val rating = calculator.rate(
                profile = profile(coversHotWater = true, hotWaterSharePercent = share),
                heatPumpKwhByPeriod = readings(totalKwh = "3000"),
            )

            assertNull(actual = rating.energyClass, message = "share $share must not be rated")
        }
    }

    @Test
    fun `reports the whole chain so the number can be checked against the meter`() {
        val rating = calculator.rate(
            profile = profile(usableAreaSqm = "140", heatPumpScop = "3.5"),
            heatPumpKwhByPeriod = readings(totalKwh = "4000"),
        )

        assertEquals(expected = "2025-01", actual = rating.windowStart)
        assertEquals(expected = "2025-12", actual = rating.windowEnd)
        assertEquals(expected = BigDecimal("4000.00"), actual = rating.heatPumpElectricityKwh)
        assertEquals(expected = BigDecimal("4000.00"), actual = rating.heatingElectricityKwh)
        assertEquals(expected = BigDecimal("14000.00"), actual = rating.heatingEnergyKwh)
        assertEquals(expected = BigDecimal("28.57"), actual = rating.finalEnergyKwhPerSqmPerYear)
    }

    @Test
    fun `reports no window when there is nothing to rate`() {
        val rating = calculator.rate(profile = profile(), heatPumpKwhByPeriod = emptyMap())

        assertNull(actual = rating.windowStart)
        assertNull(actual = rating.windowEnd)
    }
}
