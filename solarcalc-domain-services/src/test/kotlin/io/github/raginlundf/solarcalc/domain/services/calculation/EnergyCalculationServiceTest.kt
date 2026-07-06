package io.github.raginlundf.solarcalc.domain.services.calculation

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationCategory
import io.github.raginlundf.solarcalc.domain.models.calculation.CompletenessFlag
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EnergyCalculationServiceTest {

    private val service: EnergyCalculationService = EnergyCalculationServiceImpl()

    private fun baseInput(
        generation: String = "600",
        consumption: String = "500",
        feedIn: String = "0",
        hasWallbox: Boolean = false,
        hasHeatPump: Boolean = false,
        householdKwh: String? = null,
        heatPumpKwh: String? = null,
        wallboxKwh: String? = null,
        electricityPrice: String? = "0.30",
        feedInTariff: String? = "0.08",
        petrolPrice: String? = null,
        evEfficiency: String? = null,
        iceEfficiency: String? = null,
        heatingRefCost: String? = null,
        priority: List<AllocationCategory> = listOf(AllocationCategory.HOUSEHOLD),
    ): CalculationInput {
        return CalculationInput(
            tenantId = 1L,
            energyProfileId = 1L,
            period = "2024-06",
            hasWallbox = hasWallbox,
            hasHeatPump = hasHeatPump,
            consumptionKwh = BigDecimal(consumption),
            generationKwh = BigDecimal(generation),
            feedInKwh = BigDecimal(feedIn),
            householdConsumptionKwh = householdKwh?.let { BigDecimal(it) },
            heatPumpConsumptionKwh = heatPumpKwh?.let { BigDecimal(it) },
            wallboxConsumptionKwh = wallboxKwh?.let { BigDecimal(it) },
            electricityPrice = electricityPrice?.let { BigDecimal(it) },
            feedInTariff = feedInTariff?.let { BigDecimal(it) },
            petrolPrice = petrolPrice?.let { BigDecimal(it) },
            evEfficiencyKwh100km = evEfficiency?.let { BigDecimal(it) },
            iceEfficiencyL100km = iceEfficiency?.let { BigDecimal(it) },
            heatingReferenceCost = heatingRefCost?.let { BigDecimal(it) },
            allocationPriority = priority,
        )
    }

    // 1. Feed-in is deducted before all other categories
    @Test
    fun `feed-in is deducted from generation before allocation`() {
        val result = service.calculate(
            baseInput(generation = "600", feedIn = "200", householdKwh = "500"),
        )

        assertEquals(expected = BigDecimal("200"), actual = result.feedInKwh)
        assertEquals(expected = BigDecimal("400"), actual = result.selfConsumptionPoolKwh)
        assertEquals(expected = BigDecimal("400"), actual = result.householdAllocatedKwh)
        assertEquals(expected = BigDecimal("100"), actual = result.householdGridKwh)
    }

    // 2. Allocation priority WALLBOX -> HEAT_PUMP -> HOUSEHOLD
    @Test
    fun `allocation priority wallbox first - wallbox gets pool before household`() {
        val result = service.calculate(
            baseInput(
                generation = "300",
                feedIn = "0",
                hasWallbox = true,
                hasHeatPump = true,
                householdKwh = "200",
                heatPumpKwh = "100",
                wallboxKwh = "150",
                priority = listOf(
                    AllocationCategory.WALLBOX,
                    AllocationCategory.HEAT_PUMP,
                    AllocationCategory.HOUSEHOLD,
                ),
            ),
        )

        assertEquals(expected = BigDecimal("150"), actual = result.wallboxAllocatedKwh)
        assertEquals(expected = BigDecimal("100"), actual = result.heatPumpAllocatedKwh)
        assertEquals(expected = BigDecimal("50"), actual = result.householdAllocatedKwh)
        assertEquals(expected = BigDecimal("150"), actual = result.householdGridKwh)
        assertEquals(expected = BigDecimal("0"), actual = result.unallocatedKwh)
    }

    // 3. Allocation priority HOUSEHOLD -> HEAT_PUMP -> WALLBOX
    @Test
    fun `allocation priority household first - household gets pool before wallbox`() {
        val result = service.calculate(
            baseInput(
                generation = "300",
                feedIn = "0",
                hasWallbox = true,
                hasHeatPump = true,
                householdKwh = "200",
                heatPumpKwh = "100",
                wallboxKwh = "150",
                priority = listOf(
                    AllocationCategory.HOUSEHOLD,
                    AllocationCategory.HEAT_PUMP,
                    AllocationCategory.WALLBOX,
                ),
            ),
        )

        assertEquals(expected = BigDecimal("200"), actual = result.householdAllocatedKwh)
        assertEquals(expected = BigDecimal("100"), actual = result.heatPumpAllocatedKwh)
        assertEquals(expected = BigDecimal("0"), actual = result.wallboxAllocatedKwh)
        assertEquals(expected = BigDecimal("150"), actual = result.wallboxGridKwh)
    }

    // 4. Tenant without wallbox — wallbox demand is zero regardless of priority
    @Test
    fun `tenant without wallbox gets zero wallbox allocation`() {
        val result = service.calculate(
            baseInput(
                hasWallbox = false,
                wallboxKwh = "100",
                priority = listOf(AllocationCategory.HOUSEHOLD),
            ),
        )

        assertNull(actual = result.wallboxAllocatedKwh)
    }

    // 5. Tenant without heat pump — heat pump demand is zero
    @Test
    fun `tenant without heat pump gets zero heat pump allocation`() {
        val result = service.calculate(
            baseInput(
                hasHeatPump = false,
                heatPumpKwh = "100",
                priority = listOf(AllocationCategory.HOUSEHOLD),
            ),
        )

        assertNull(actual = result.heatPumpAllocatedKwh)
    }

    // 6. Missing optional prices mark partial results (MISSING_ELECTRICITY_PRICE)
    @Test
    fun `missing electricity price marks result as incomplete`() {
        val result = service.calculate(
            baseInput(electricityPrice = null, householdKwh = "400"),
        )

        assertTrue(CompletenessFlag.MISSING_ELECTRICITY_PRICE in result.completeness.flags)
        assertNull(actual = result.householdSavings)
        assertNull(actual = result.totalElectricitySavings)
    }

    // 7. Feed-in greater than generation is capped and flagged
    @Test
    fun `feed-in exceeding generation is capped and flagged`() {
        val result = service.calculate(
            baseInput(generation = "300", feedIn = "500", householdKwh = "200"),
        )

        assertEquals(expected = BigDecimal("300"), actual = result.feedInKwh)
        assertEquals(expected = BigDecimal("0"), actual = result.selfConsumptionPoolKwh)
        assertTrue(CompletenessFlag.ALLOCATION_CAPPED_FEED_IN in result.completeness.flags)
    }

    // 8. Household consumption derived from total when not entered
    @Test
    fun `household consumption is derived from total minus other categories`() {
        val result = service.calculate(
            baseInput(
                consumption = "500",
                hasHeatPump = true,
                hasWallbox = true,
                householdKwh = null,
                heatPumpKwh = "100",
                wallboxKwh = "80",
                priority = listOf(
                    AllocationCategory.HOUSEHOLD,
                    AllocationCategory.HEAT_PUMP,
                    AllocationCategory.WALLBOX,
                ),
            ),
        )

        // derived household = 500 - 100 - 80 = 320
        assertEquals(expected = BigDecimal("320"), actual = result.householdAllocatedKwh + result.householdGridKwh)
        assertTrue(CompletenessFlag.DERIVED_HOUSEHOLD_CONSUMPTION in result.completeness.flags)
    }

    // 9. Wallbox petrol comparison when all values present
    @Test
    fun `wallbox petrol savings are calculated when all values present`() {
        val result = service.calculate(
            baseInput(
                generation = "200",
                hasWallbox = true,
                householdKwh = "100",
                wallboxKwh = "100",
                electricityPrice = "0.30",
                petrolPrice = "1.80",
                evEfficiency = "20",
                iceEfficiency = "8",
                priority = listOf(AllocationCategory.WALLBOX, AllocationCategory.HOUSEHOLD),
            ),
        )

        assertNotNull(actual = result.wallboxPetrolSavings)
        // estimatedKm = 100 / 20 * 100 = 500 km
        // petrolLitres = 500 / 100 * 8 = 40 litres
        // petrolCost = 40 * 1.80 = 72.00
        // wallboxGridCost = 0 * 0.30 = 0.00
        // petrolSavings = 72.00 - 0.00 = 72.00
        assertEquals(expected = BigDecimal("72.00"), actual = result.wallboxPetrolSavings)
    }

    // 10. Heating oil comparison calculated when reference cost present
    @Test
    fun `heat pump heating reference savings calculated against oil reference`() {
        val result = service.calculate(
            baseInput(
                generation = "100",
                hasHeatPump = true,
                householdKwh = "50",
                heatPumpKwh = "100",
                electricityPrice = "0.30",
                heatingRefCost = "120.00",
                priority = listOf(AllocationCategory.HOUSEHOLD, AllocationCategory.HEAT_PUMP),
            ),
        )

        assertNotNull(actual = result.heatPumpHeatingReferenceSavings)
        // heatPumpAllocated = 50, heatPumpGrid = 50
        // heatPumpGridCost = 50 * 0.30 = 15.00
        // heatingRefSavings = 120.00 - 15.00 = 105.00
        assertEquals(expected = BigDecimal("105.00"), actual = result.heatPumpHeatingReferenceSavings)
    }

    // 11. Heating gas comparison (same logic as oil, reuses reference cost field)
    @Test
    fun `heat pump heating reference savings calculated against gas reference`() {
        val result = service.calculate(
            baseInput(
                generation = "200",
                hasHeatPump = true,
                householdKwh = "100",
                heatPumpKwh = "100",
                electricityPrice = "0.30",
                heatingRefCost = "80.00",
                priority = listOf(AllocationCategory.HEAT_PUMP, AllocationCategory.HOUSEHOLD),
            ),
        )

        // heatPumpAllocated = 100, heatPumpGrid = 0
        // heatPumpGridCost = 0.00
        // heatingRefSavings = 80.00 - 0.00 = 80.00
        assertEquals(expected = BigDecimal("80.00"), actual = result.heatPumpHeatingReferenceSavings)
    }

    // 12. Total view avoids double counting (electricity savings != petrol savings)
    @Test
    fun `total electricity savings does not include petrol savings`() {
        val result = service.calculate(
            baseInput(
                generation = "400",
                hasWallbox = true,
                householdKwh = "200",
                wallboxKwh = "100",
                electricityPrice = "0.30",
                petrolPrice = "1.80",
                evEfficiency = "20",
                iceEfficiency = "8",
                priority = listOf(AllocationCategory.HOUSEHOLD, AllocationCategory.WALLBOX),
            ),
        )

        // totalElectricitySavings should be householdSavings + wallboxElectricitySavings
        val expectedTotal = (result.householdSavings!! + result.wallboxElectricitySavings!!).setScale(2)
        assertEquals(expected = expectedTotal, actual = result.totalElectricitySavings)

        // petrolSavings is separate
        assertNotNull(actual = result.wallboxPetrolSavings)
    }
}
