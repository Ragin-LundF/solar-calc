package io.github.raginlundf.solarcalc.domain.services.summary

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationCategoryEnum
import io.github.raginlundf.solarcalc.domain.models.profile.DEFAULT_HEATING_DISTRIBUTION
import io.github.raginlundf.solarcalc.dtos.summary.EnergyEfficiencyRating
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SummaryServiceImplTest {

    private val service = SummaryServiceImpl()

    /** The rating is produced upstream by EnergyEfficiencyCalculator; the summary only carries it. */
    private val notRated = EnergyEfficiencyRating(
        usableAreaSqm = null,
        heatPumpScop = null,
        heatingEnergyKwh = BigDecimal("0.00"),
        kwhPerSqmPerYear = BigDecimal("0.00"),
        energyClass = null,
        monthsConsidered = 0,
    )

    private val params = SummaryParams(
        allocationPriority = listOf(
            AllocationCategoryEnum.HOUSEHOLD,
            AllocationCategoryEnum.HEAT_PUMP,
            AllocationCategoryEnum.WALLBOX,
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
        referencePrice = BigDecimal("0.30"),
        feedInTariff = BigDecimal("0.05"),
        petrolPrice = BigDecimal("1.80"),
        heizReferenzJahr = BigDecimal("2400"),
    )

    /** A month with no solar at all, so every kWh of demand is a grid purchase. */
    private fun gridOnlyMonth(
        period: String,
        householdKwh: String,
        gridPrice: String,
        referencePrice: String?,
    ): SummaryMonthInput {
        return SummaryMonthInput(
            period = period,
            generationKwh = BigDecimal.ZERO,
            feedInKwh = BigDecimal.ZERO,
            consumptionKwh = BigDecimal(householdKwh),
            householdKwh = BigDecimal(householdKwh),
            heatPumpKwh = BigDecimal.ZERO,
            wallboxKwh = BigDecimal.ZERO,
            gridPrice = BigDecimal(gridPrice),
            referencePrice = referencePrice?.let { BigDecimal(it) },
            feedInTariff = BigDecimal.ZERO,
            petrolPrice = BigDecimal.ZERO,
            heizReferenzJahr = BigDecimal.ZERO,
        )
    }

    private fun summarize(months: List<SummaryMonthInput>) =
        service.summarize(
            months = months,
            params = params,
            rangeStart = null,
            rangeEnd = null,
            efficiency = notRated,
        )

    @Test
    fun `computes per-consumer real cost, gasoline comparison and payback projection`() {
        val result = summarize(listOf(june))
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

    @Test
    fun `rolls the three consumers up into one grid purchase per month`() {
        val month = summarize(listOf(june)).months.single()

        // Only the wallbox drew from the grid: 0 + 0 + 100 kWh at 0.30 = 30.00.
        assertEquals(expected = BigDecimal("100.00"), actual = month.gridKwh)
        assertEquals(expected = BigDecimal("0.300"), actual = month.purchasePricePerKwh)
        assertEquals(expected = BigDecimal("30.00"), actual = month.gridCost)
    }

    @Test
    fun `reports no tariff difference when the dynamic price matches the contract price`() {
        val month = summarize(listOf(june)).months.single()

        assertEquals(expected = BigDecimal("30.00"), actual = month.gridCostAtReferencePrice)
        assertEquals(expected = BigDecimal("0.00"), actual = month.dynamicTariffDelta)
    }

    @Test
    fun `reports a saving when the dynamic price undercuts the contract price`() {
        val cheap = gridOnlyMonth(
            period = "2025-01",
            householdKwh = "100",
            gridPrice = "0.30",
            referencePrice = "0.35",
        )

        val month = summarize(listOf(cheap)).months.single()

        // 100 kWh: paid 100*0.30 = 30.00, contract would have been 100*0.35 = 35.00.
        assertEquals(expected = BigDecimal("100.00"), actual = month.gridKwh)
        assertEquals(expected = BigDecimal("30.00"), actual = month.gridCost)
        assertEquals(expected = BigDecimal("35.00"), actual = month.gridCostAtReferencePrice)
        assertEquals(expected = BigDecimal("5.00"), actual = month.dynamicTariffDelta)
    }

    @Test
    fun `reports a loss when the dynamic price exceeds the contract price`() {
        val pricey = gridOnlyMonth(
            period = "2025-01",
            householdKwh = "100",
            gridPrice = "0.40",
            referencePrice = "0.25",
        )

        val month = summarize(listOf(pricey)).months.single()

        // Paid 40.00 where the contract would have cost 25.00 -> 15.00 worse off.
        assertEquals(expected = BigDecimal("40.00"), actual = month.gridCost)
        assertEquals(expected = BigDecimal("25.00"), actual = month.gridCostAtReferencePrice)
        assertEquals(expected = BigDecimal("-15.00"), actual = month.dynamicTariffDelta)
    }

    @Test
    fun `treats a missing contract price as nothing to compare against`() {
        val noContract = gridOnlyMonth(
            period = "2025-01",
            householdKwh = "100",
            gridPrice = "0.30",
            referencePrice = null,
        )

        val month = summarize(listOf(noContract)).months.single()

        assertEquals(expected = BigDecimal("30.00"), actual = month.gridCostAtReferencePrice)
        assertEquals(expected = BigDecimal("0.00"), actual = month.dynamicTariffDelta)
    }

    @Test
    fun `weights the average purchase price by kWh, not by month`() {
        val cheapSmall = gridOnlyMonth(
            period = "2025-01",
            householdKwh = "100",
            gridPrice = "0.20",
            referencePrice = "0.30",
        )
        val dearLarge = gridOnlyMonth(
            period = "2025-02",
            householdKwh = "300",
            gridPrice = "0.40",
            referencePrice = "0.30",
        )

        val aggregates = summarize(listOf(cheapSmall, dearLarge)).aggregates

        // 400 kWh for 20.00 + 120.00 = 140.00 -> 0.350/kWh. A plain average of the two prices
        // would have given 0.300, which is what this test exists to rule out.
        assertEquals(expected = BigDecimal("400.00"), actual = aggregates.gridKwh)
        assertEquals(expected = BigDecimal("140.00"), actual = aggregates.gridCost)
        assertEquals(expected = BigDecimal("0.350"), actual = aggregates.averagePurchasePricePerKwh)
        // The contract price is flat at 0.30, so 400 kWh would have cost 120.00 -> 20.00 worse off.
        assertEquals(expected = BigDecimal("120.00"), actual = aggregates.gridCostAtReferencePrice)
        assertEquals(expected = BigDecimal("0.300"), actual = aggregates.referencePricePerKwh)
        assertEquals(expected = BigDecimal("-20.00"), actual = aggregates.dynamicTariffDelta)
    }

    @Test
    fun `reports a zero average price instead of dividing when nothing was purchased`() {
        // Generation covers the whole demand, so no kWh is bought and the weighted average
        // would otherwise be a division by zero.
        val selfSufficient = june.copy(
            householdKwh = BigDecimal("100"),
            heatPumpKwh = BigDecimal.ZERO,
            wallboxKwh = BigDecimal.ZERO,
            generationKwh = BigDecimal("1000"),
            feedInKwh = BigDecimal("200"),
        )

        val result = summarize(listOf(selfSufficient))

        assertEquals(expected = BigDecimal("0.00"), actual = result.months.single().gridKwh)
        assertEquals(expected = BigDecimal("0.00"), actual = result.aggregates.gridKwh)
        assertEquals(expected = BigDecimal("0.000"), actual = result.aggregates.averagePurchasePricePerKwh)
        assertEquals(expected = BigDecimal("0.000"), actual = result.aggregates.referencePricePerKwh)
    }

    @Test
    fun `keeps the tariff difference out of total savings and payback`() {
        val cheap = gridOnlyMonth(
            period = "2025-01",
            householdKwh = "100",
            gridPrice = "0.30",
            referencePrice = "0.35",
        )

        val result = summarize(listOf(cheap))
        val month = result.months.single()

        // No solar at all, so nothing is saved even though the dynamic tariff beat the contract by 5.00.
        assertEquals(expected = BigDecimal("5.00"), actual = month.dynamicTariffDelta)
        assertEquals(expected = BigDecimal("0.00"), actual = month.totalSavings)
        assertEquals(expected = BigDecimal("0.00"), actual = result.aggregates.totalSavings)
        assertEquals(expected = BigDecimal("0.00"), actual = result.payback.cumulativeSavings)
        assertEquals(expected = BigDecimal("0.00"), actual = result.payback.paybackPct)
    }

    @Test
    fun `carries the efficiency rating through untouched`() {
        val rated = notRated.copy(monthsConsidered = 7)

        val result = service.summarize(
            months = listOf(june),
            params = params,
            rangeStart = null,
            rangeEnd = null,
            efficiency = rated,
        )

        assertEquals(expected = rated, actual = result.efficiency)
    }
}
