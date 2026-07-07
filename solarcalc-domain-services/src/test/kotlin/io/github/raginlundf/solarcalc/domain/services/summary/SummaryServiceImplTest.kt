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
    // Allocation: H=500, HP=200, WB=100 (pool exhausted), WB grid=100.
    private val june = SummaryMonthInput(
        period = "2025-06",
        generationKwh = BigDecimal("1000"),
        feedInKwh = BigDecimal("200"),
        consumptionKwh = BigDecimal("900"),
        householdKwh = BigDecimal("500"),
        heatPumpKwh = BigDecimal("200"),
        wallboxKwh = BigDecimal("200"),
        gridPrice = BigDecimal("0.30"),
        referencePrice = BigDecimal("0.10"),
        feedInTariff = BigDecimal("0.05"),
        petrolPrice = BigDecimal("1.80"),
        heizReferenzJahr = BigDecimal("2400"),
    )

    @Test
    fun `computes per-consumer savings, gasoline comparison and payback projection`() {
        val result = service.summarize(months = listOf(june), params = params, rangeStart = null, rangeEnd = null)
        val month = result.months.single()

        // Household: without = 500*0.30 = 150; with = 500*0.10 = 50; savings = 100.
        assertEquals(BigDecimal("100.00"), month.householdSavings)
        // Heating: June share = 1% of 2400 = 24; HP cost with solar = 200*0.10 = 20; savings = 4.
        assertEquals(BigDecimal("24.00"), month.oilCost)
        assertEquals(BigDecimal("4.00"), month.heatingSavings)
        // Wallbox: km = 100*5 = 500? No: demand 200*5 = 1000 km; gasoline = (1000/100)*8*1.80 = 144;
        // charging cost = 100*0.30 + 100*0.10 = 40; savings vs gasoline = 104.
        assertEquals(BigDecimal("1000.00"), month.estimatedKm)
        assertEquals(BigDecimal("144.00"), month.gasolineEquivalentCost)
        assertEquals(BigDecimal("104.00"), month.wallboxSavingsVsGasoline)
        // Feed-in revenue = 200*0.05 = 10. Total = 100 + 4 + 104 + 10 = 218.
        assertEquals(BigDecimal("10.00"), month.feedInRevenue)
        assertEquals(BigDecimal("218.00"), month.totalSavings)
        assertEquals(BigDecimal("218.00"), result.aggregates.totalSavings)

        // Payback: 218 of 1000 = 21.80%; remaining 782 / avg 218 = ceil 4 months from 2025-06 -> 2025-10.
        assertEquals(BigDecimal("21.80"), result.payback.paybackPct)
        assertFalse(result.payback.amortised)
        assertEquals("2025-10", result.payback.projectedPaybackPeriod)
    }
}
