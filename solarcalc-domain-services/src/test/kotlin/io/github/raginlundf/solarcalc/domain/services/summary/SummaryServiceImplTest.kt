package io.github.raginlundf.solarcalc.domain.services.summary

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationCategory
import io.github.raginlundf.solarcalc.domain.models.profile.DEFAULT_HEATING_DISTRIBUTION
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SummaryServiceImplTest {

    private val service = SummaryServiceImpl()

    private val params = SummaryParams(
        allocationPriority = listOf(
            AllocationCategory.HOUSEHOLD,
            AllocationCategory.HEAT_PUMP,
            AllocationCategory.WALLBOX,
        ),
        hasHeatPump = true,
        hasWallbox = true,
        kmPerKwh = BigDecimal("5"),
        litersPer100km = BigDecimal("8"),
        investKosten = BigDecimal("1000"),
        heatingDistribution = DEFAULT_HEATING_DISTRIBUTION,
    )

    // generation 1000, feedIn 200 -> pool 800; demand H=500, HP=200, WB=200.
    // Allocation: H=500 (fully solar), HP=200 (fully solar), WB=100 solar / 100 grid (pool exhausted).
    private val june = SummaryMonthInput(
        period = "2025-06",
        generationKwh = BigDecimal("1000"),
        feedInKwh = BigDecimal("200"),
        consumptionKwh = BigDecimal("900"),
        householdKwh = BigDecimal("500"),
        heatPumpKwh = BigDecimal("200"),
        wallboxKwh = BigDecimal("200"),
        gridPrice = BigDecimal("0.30"),
        feedInTariff = BigDecimal("0.05"),
        petrolPrice = BigDecimal("1.80"),
        heizReferenzJahr = BigDecimal("2400"),
    )

    @Test
    fun `computes per-consumer real cost, gasoline comparison and payback projection`() {
        val result = service.summarize(months = listOf(june), params = params, rangeStart = null, rangeEnd = null)
        val month = result.months.single()

        // Self-consumed solar is free. Household fully covered: real cost 0, savings = 500*0.30 = 150.
        assertEquals(BigDecimal("500.00"), month.householdSolarKwh)
        assertEquals(BigDecimal("0.00"), month.householdGridKwh)
        assertEquals(BigDecimal("0.00"), month.householdCostWithSolar)
        assertEquals(BigDecimal("150.00"), month.householdSavings)
        // Heating fully covered: HP real cost 0; June share = 1% of 2400 = 24; savings = 24.
        assertEquals(BigDecimal("0.00"), month.heatPumpCostWithSolar)
        assertEquals(BigDecimal("24.00"), month.oilCost)
        assertEquals(BigDecimal("24.00"), month.heatingSavings)
        // Wallbox: 100 kWh solar / 100 kWh grid -> real charging cost = 100*0.30 = 30.
        // km = 200*5 = 1000; gasoline = (1000/100)*8*1.80 = 144; savings vs gasoline = 144 - 30 = 114.
        assertEquals(BigDecimal("100.00"), month.wallboxSolarKwh)
        assertEquals(BigDecimal("100.00"), month.wallboxGridKwh)
        assertEquals(BigDecimal("30.00"), month.wallboxCostWithSolar)
        assertEquals(BigDecimal("1000.00"), month.estimatedKm)
        assertEquals(BigDecimal("144.00"), month.gasolineEquivalentCost)
        assertEquals(BigDecimal("114.00"), month.wallboxSavingsVsGasoline)
        // Feed-in revenue = 200*0.05 = 10. Total = 150 + 24 + 114 + 10 = 298.
        assertEquals(BigDecimal("10.00"), month.feedInRevenue)
        assertEquals(BigDecimal("298.00"), month.totalSavings)
        assertEquals(BigDecimal("298.00"), result.aggregates.totalSavings)

        // Payback: 298 of 1000 = 29.80%; remaining 702 / avg 298 = ceil 3 months from 2025-06 -> 2025-09.
        assertEquals(BigDecimal("29.80"), result.payback.paybackPct)
        assertFalse(result.payback.amortised)
        assertEquals("2025-09", result.payback.projectedPaybackPeriod)
    }
}
