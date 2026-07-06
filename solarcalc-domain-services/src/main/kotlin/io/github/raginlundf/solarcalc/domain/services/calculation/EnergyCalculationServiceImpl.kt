package io.github.raginlundf.solarcalc.domain.services.calculation

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationCategory
import io.github.raginlundf.solarcalc.domain.models.calculation.CompletenessFlag
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class EnergyCalculationServiceImpl : EnergyCalculationService {

    override fun calculate(input: CalculationInput): CalculationResult {
        return computeResult(input)
    }

    override fun compareScenarios(
        input: CalculationInput,
        priorities: List<List<AllocationCategory>>,
    ): List<CalculationResult> {
        return priorities.map { priority -> computeResult(input.copy(allocationPriority = priority)) }
    }

    private fun computeResult(input: CalculationInput): CalculationResult {
        val flags = mutableSetOf<CompletenessFlag>()

        // ── Feed-in and pool ────────────────────────────────────────────────────
        val rawFeedIn = input.feedInKwh.max(BigDecimal.ZERO)
        val generation = input.generationKwh.max(BigDecimal.ZERO)
        val feedInCapped = rawFeedIn > generation
        val feedInKwh = rawFeedIn.min(generation)
        if (feedInCapped) {
            flags += CompletenessFlag.ALLOCATION_CAPPED_FEED_IN
        }

        var poolKwh = (generation - feedInKwh).max(BigDecimal.ZERO)

        // ── Category demand ─────────────────────────────────────────────────────
        val heatPumpDemand = if (input.hasHeatPump) input.heatPumpConsumptionKwh?.max(BigDecimal.ZERO) ?: BigDecimal.ZERO else BigDecimal.ZERO
        val wallboxDemand = if (input.hasWallbox) input.wallboxConsumptionKwh?.max(BigDecimal.ZERO) ?: BigDecimal.ZERO else BigDecimal.ZERO

        val householdDemand = if (input.householdConsumptionKwh != null) {
            input.householdConsumptionKwh.max(BigDecimal.ZERO)
        } else {
            flags += CompletenessFlag.DERIVED_HOUSEHOLD_CONSUMPTION
            (input.consumptionKwh - heatPumpDemand - wallboxDemand).max(BigDecimal.ZERO)
        }

        val demand = mapOf(
            AllocationCategory.HOUSEHOLD to householdDemand,
            AllocationCategory.HEAT_PUMP to heatPumpDemand,
            AllocationCategory.WALLBOX to wallboxDemand,
        )

        // ── Priority allocation ─────────────────────────────────────────────────
        val allocated = mutableMapOf<AllocationCategory, BigDecimal>()
        val gridUsage = mutableMapOf<AllocationCategory, BigDecimal>()

        for (category in input.allocationPriority) {
            val categoryDemand = demand[category] ?: BigDecimal.ZERO
            val allocatedKwh = poolKwh.min(categoryDemand)
            allocated[category] = allocatedKwh
            gridUsage[category] = categoryDemand - allocatedKwh
            poolKwh -= allocatedKwh
        }

        // Categories not in priority list get no allocation
        AllocationCategory.entries.forEach { category ->
            if (!allocated.containsKey(category)) {
                allocated[category] = BigDecimal.ZERO
                gridUsage[category] = demand[category] ?: BigDecimal.ZERO
            }
        }

        val unallocatedKwh = poolKwh

        // ── Price availability checks ───────────────────────────────────────────
        val electricityPrice = input.electricityPrice
        if (electricityPrice == null) {
            flags += CompletenessFlag.MISSING_ELECTRICITY_PRICE
        }
        if (input.feedInTariff == null) {
            flags += CompletenessFlag.MISSING_FEED_IN_TARIFF
        }
        if (input.hasWallbox && input.petrolPrice == null) {
            flags += CompletenessFlag.MISSING_PETROL_PRICE
        }
        if (input.hasHeatPump && input.heatingReferenceCost == null) {
            flags += CompletenessFlag.MISSING_HEATING_REFERENCE_COST
        }

        // ── Feed-in revenue ─────────────────────────────────────────────────────
        val feedInRevenue = input.feedInTariff?.let { tariff -> (feedInKwh * tariff).scale2() }

        // ── Household ──────────────────────────────────────────────────────────
        val householdAllocated = allocated[AllocationCategory.HOUSEHOLD]!!
        val householdGrid = gridUsage[AllocationCategory.HOUSEHOLD]!!
        val householdSavings = electricityPrice?.let { (householdAllocated * it).scale2() }

        // ── Heat pump ──────────────────────────────────────────────────────────
        val (heatPumpAllocated, heatPumpGrid, heatPumpElecSavings, heatPumpHeatingSavings) =
            if (input.hasHeatPump) {
                val hp = allocated[AllocationCategory.HEAT_PUMP]!!
                val hpGrid = gridUsage[AllocationCategory.HEAT_PUMP]!!
                val hpElec = electricityPrice?.let { (hp * it).scale2() }
                val hpGridCost = electricityPrice?.let { (hpGrid * it).scale2() }
                val hpHeating = if (hpGridCost != null && input.heatingReferenceCost != null) {
                    (input.heatingReferenceCost - hpGridCost).scale2()
                } else null
                listOf(hp, hpGrid, hpElec, hpHeating)
            } else {
                listOf(null, null, null, null)
            }

        // ── Wallbox ────────────────────────────────────────────────────────────
        val (wallboxAllocated, wallboxGrid, wallboxElecSavings, wallboxPetrolSavings) =
            if (input.hasWallbox) {
                val wb = allocated[AllocationCategory.WALLBOX]!!
                val wbGrid = gridUsage[AllocationCategory.WALLBOX]!!
                val wbElec = electricityPrice?.let { (wb * it).scale2() }
                val wbGridCost = electricityPrice?.let { (wbGrid * it).scale2() }
                val wbPetrol = computeWallboxPetrolSavings(
                    wallboxDemandKwh = wallboxDemand,
                    wallboxGridCost = wbGridCost,
                    petrolPrice = input.petrolPrice,
                    evEfficiency = input.evEfficiencyKwh100km,
                    iceEfficiency = input.iceEfficiencyL100km,
                )
                listOf(wb, wbGrid, wbElec, wbPetrol)
            } else {
                listOf(null, null, null, null)
            }

        // ── Totals ─────────────────────────────────────────────────────────────
        val totalElectricitySavings = if (electricityPrice != null) {
            listOf(householdSavings, heatPumpElecSavings as? BigDecimal, wallboxElecSavings as? BigDecimal)
                .filterNotNull()
                .fold(BigDecimal.ZERO, BigDecimal::add)
                .scale2()
        } else null

        if (flags.isEmpty()) {
            flags += CompletenessFlag.COMPLETE
        }

        return CalculationResult(
            period = input.period,
            allocationPriority = input.allocationPriority,
            feedInKwh = feedInKwh,
            feedInRevenue = feedInRevenue,
            selfConsumptionPoolKwh = (generation - feedInKwh).max(BigDecimal.ZERO),
            unallocatedKwh = unallocatedKwh,
            householdAllocatedKwh = householdAllocated,
            householdGridKwh = householdGrid,
            householdSavings = householdSavings,
            heatPumpAllocatedKwh = heatPumpAllocated as? BigDecimal,
            heatPumpGridKwh = heatPumpGrid as? BigDecimal,
            heatPumpElectricitySavings = heatPumpElecSavings as? BigDecimal,
            heatPumpHeatingReferenceSavings = heatPumpHeatingSavings as? BigDecimal,
            wallboxAllocatedKwh = wallboxAllocated as? BigDecimal,
            wallboxGridKwh = wallboxGrid as? BigDecimal,
            wallboxElectricitySavings = wallboxElecSavings as? BigDecimal,
            wallboxPetrolSavings = wallboxPetrolSavings as? BigDecimal,
            totalElectricitySavings = totalElectricitySavings,
            completeness = CalculationCompleteness(flags = flags),
        )
    }

    private fun computeWallboxPetrolSavings(
        wallboxDemandKwh: BigDecimal,
        wallboxGridCost: BigDecimal?,
        petrolPrice: BigDecimal?,
        evEfficiency: BigDecimal?,
        iceEfficiency: BigDecimal?,
    ): BigDecimal? {
        if (petrolPrice == null || evEfficiency == null || iceEfficiency == null || wallboxGridCost == null) {
            return null
        }
        if (evEfficiency <= BigDecimal.ZERO) {
            return null
        }
        val estimatedKm = wallboxDemandKwh.divide(evEfficiency, 6, RoundingMode.HALF_UP) * BigDecimal("100")
        val equivalentPetrolLitres = estimatedKm.divide(BigDecimal("100"), 6, RoundingMode.HALF_UP) * iceEfficiency
        val equivalentPetrolCost = (equivalentPetrolLitres * petrolPrice).scale2()
        return (equivalentPetrolCost - wallboxGridCost).scale2()
    }

    private fun BigDecimal.scale2(): BigDecimal {
        return setScale(2, RoundingMode.HALF_UP)
    }
}
