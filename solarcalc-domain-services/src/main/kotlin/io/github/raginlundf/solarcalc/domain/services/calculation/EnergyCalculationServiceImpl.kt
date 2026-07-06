package io.github.raginlundf.solarcalc.domain.services.calculation

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationCategory
import io.github.raginlundf.solarcalc.domain.models.calculation.CompletenessFlag
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class EnergyCalculationServiceImpl : EnergyCalculationService {

    override fun calculate(input: CalculationInput): CalculationResult {
        return computeResult(input = input)
    }

    override fun compareScenarios(
        input: CalculationInput,
        priorities: List<List<AllocationCategory>>,
    ): List<CalculationResult> {
        return priorities.map { priority -> computeResult(input = input.copy(allocationPriority = priority)) }
    }

    private fun computeResult(input: CalculationInput): CalculationResult {
        val flags = mutableSetOf<CompletenessFlag>()
        val (feedInKwh, poolKwh) = computeFeedIn(input = input, flags = flags)
        val demand = computeDemands(input = input, flags = flags)
        val allocation = computeAllocation(input = input, demand = demand, poolKwh = poolKwh)
        checkPriceFlags(input = input, flags = flags)

        val electricityPrice = input.electricityPrice
        val feedInRevenue = input.feedInTariff?.let { tariff -> (feedInKwh * tariff).scale2() }

        val householdAllocated = allocation.allocated[AllocationCategory.HOUSEHOLD]!!
        val householdGrid = allocation.gridUsage[AllocationCategory.HOUSEHOLD]!!
        val householdSavings = electricityPrice?.let { price -> (householdAllocated * price).scale2() }

        val hp = computeHeatPump(
            input = input,
            allocated = allocation.allocated,
            gridUsage = allocation.gridUsage,
            electricityPrice = electricityPrice,
        )
        val wb = computeWallbox(
            input = input,
            allocated = allocation.allocated,
            gridUsage = allocation.gridUsage,
            electricityPrice = electricityPrice,
        )

        val totalElectricitySavings = if (electricityPrice != null) {
            listOf(householdSavings, hp.elecSavings, wb.elecSavings)
                .filterNotNull()
                .fold(BigDecimal.ZERO, BigDecimal::add)
                .scale2()
        } else {
            null
        }

        if (flags.isEmpty()) {
            flags += CompletenessFlag.COMPLETE
        }

        return CalculationResult(
            period = input.period,
            allocationPriority = input.allocationPriority,
            feedInKwh = feedInKwh,
            feedInRevenue = feedInRevenue,
            selfConsumptionPoolKwh = (input.generationKwh.max(BigDecimal.ZERO) - feedInKwh).max(BigDecimal.ZERO),
            unallocatedKwh = allocation.unallocatedKwh,
            householdAllocatedKwh = householdAllocated,
            householdGridKwh = householdGrid,
            householdSavings = householdSavings,
            heatPumpAllocatedKwh = hp.allocatedKwh,
            heatPumpGridKwh = hp.gridKwh,
            heatPumpElectricitySavings = hp.elecSavings,
            heatPumpHeatingReferenceSavings = hp.heatingSavings,
            wallboxAllocatedKwh = wb.allocatedKwh,
            wallboxGridKwh = wb.gridKwh,
            wallboxElectricitySavings = wb.elecSavings,
            totalElectricitySavings = totalElectricitySavings,
            completeness = CalculationCompleteness(flags = flags),
        )
    }

    private fun computeFeedIn(
        input: CalculationInput,
        flags: MutableSet<CompletenessFlag>,
    ): Pair<BigDecimal, BigDecimal> {
        val rawFeedIn = input.feedInKwh.max(BigDecimal.ZERO)
        val generation = input.generationKwh.max(BigDecimal.ZERO)
        if (rawFeedIn > generation) {
            flags += CompletenessFlag.ALLOCATION_CAPPED_FEED_IN
        }
        val feedInKwh = rawFeedIn.min(generation)
        return feedInKwh to (generation - feedInKwh).max(BigDecimal.ZERO)
    }

    private fun computeDemands(
        input: CalculationInput,
        flags: MutableSet<CompletenessFlag>,
    ): Map<AllocationCategory, BigDecimal> {
        val heatPumpDemand = if (input.hasHeatPump) {
            input.heatPumpConsumptionKwh?.max(BigDecimal.ZERO) ?: BigDecimal.ZERO
        } else {
            BigDecimal.ZERO
        }
        val wallboxDemand = if (input.hasWallbox) {
            input.wallboxConsumptionKwh?.max(BigDecimal.ZERO) ?: BigDecimal.ZERO
        } else {
            BigDecimal.ZERO
        }
        val householdDemand = if (input.householdConsumptionKwh != null) {
            input.householdConsumptionKwh.max(BigDecimal.ZERO)
        } else {
            flags += CompletenessFlag.DERIVED_HOUSEHOLD_CONSUMPTION
            (input.consumptionKwh - heatPumpDemand - wallboxDemand).max(BigDecimal.ZERO)
        }
        return mapOf(
            AllocationCategory.HOUSEHOLD to householdDemand,
            AllocationCategory.HEAT_PUMP to heatPumpDemand,
            AllocationCategory.WALLBOX to wallboxDemand,
        )
    }

    private data class AllocationResult(
        val allocated: Map<AllocationCategory, BigDecimal>,
        val gridUsage: Map<AllocationCategory, BigDecimal>,
        val unallocatedKwh: BigDecimal,
    )

    private fun computeAllocation(
        input: CalculationInput,
        demand: Map<AllocationCategory, BigDecimal>,
        poolKwh: BigDecimal,
    ): AllocationResult {
        var remaining = poolKwh
        val allocated = mutableMapOf<AllocationCategory, BigDecimal>()
        val gridUsage = mutableMapOf<AllocationCategory, BigDecimal>()

        for (category in input.allocationPriority) {
            val categoryDemand = demand[category] ?: BigDecimal.ZERO
            val allocatedKwh = remaining.min(categoryDemand)
            allocated[category] = allocatedKwh
            gridUsage[category] = categoryDemand - allocatedKwh
            remaining -= allocatedKwh
        }

        AllocationCategory.entries.forEach { category ->
            if (!allocated.containsKey(category)) {
                allocated[category] = BigDecimal.ZERO
                gridUsage[category] = demand[category] ?: BigDecimal.ZERO
            }
        }

        return AllocationResult(
            allocated = allocated,
            gridUsage = gridUsage,
            unallocatedKwh = remaining,
        )
    }

    private fun checkPriceFlags(input: CalculationInput, flags: MutableSet<CompletenessFlag>) {
        if (input.electricityPrice == null) flags += CompletenessFlag.MISSING_ELECTRICITY_PRICE
        if (input.feedInTariff == null) flags += CompletenessFlag.MISSING_FEED_IN_TARIFF
        if (input.hasWallbox && input.petrolPrice == null) flags += CompletenessFlag.MISSING_PETROL_PRICE
        if (input.hasHeatPump && input.heatingReferenceCost == null) {
            flags += CompletenessFlag.MISSING_HEATING_REFERENCE_COST
        }
    }

    private data class HeatPumpResult(
        val allocatedKwh: BigDecimal?,
        val gridKwh: BigDecimal?,
        val elecSavings: BigDecimal?,
        val heatingSavings: BigDecimal?,
    )

    private fun computeHeatPump(
        input: CalculationInput,
        allocated: Map<AllocationCategory, BigDecimal>,
        gridUsage: Map<AllocationCategory, BigDecimal>,
        electricityPrice: BigDecimal?,
    ): HeatPumpResult {
        if (!input.hasHeatPump) {
            return HeatPumpResult(
                allocatedKwh = null,
                gridKwh = null,
                elecSavings = null,
                heatingSavings = null,
            )
        }
        val hp = allocated[AllocationCategory.HEAT_PUMP]!!
        val hpGrid = gridUsage[AllocationCategory.HEAT_PUMP]!!
        val hpElec = electricityPrice?.let { price -> (hp * price).scale2() }
        val hpGridCost = electricityPrice?.let { price -> (hpGrid * price).scale2() }
        val hpHeating = if (hpGridCost != null && input.heatingReferenceCost != null) {
            (input.heatingReferenceCost - hpGridCost).scale2()
        } else {
            null
        }
        return HeatPumpResult(
            allocatedKwh = hp,
            gridKwh = hpGrid,
            elecSavings = hpElec,
            heatingSavings = hpHeating,
        )
    }

    private data class WallboxResult(
        val allocatedKwh: BigDecimal?,
        val gridKwh: BigDecimal?,
        val elecSavings: BigDecimal?,
    )

    private fun computeWallbox(
        input: CalculationInput,
        allocated: Map<AllocationCategory, BigDecimal>,
        gridUsage: Map<AllocationCategory, BigDecimal>,
        electricityPrice: BigDecimal?
    ): WallboxResult {
        if (!input.hasWallbox) {
            return WallboxResult(
                allocatedKwh = null,
                gridKwh = null,
                elecSavings = null,
            )
        }
        val wb = allocated[AllocationCategory.WALLBOX]!!
        val wbGrid = gridUsage[AllocationCategory.WALLBOX]!!
        val wbElec = electricityPrice?.let { price -> (wb * price).scale2() }
        return WallboxResult(
            allocatedKwh = wb,
            gridKwh = wbGrid,
            elecSavings = wbElec,
        )
    }

    private fun BigDecimal.scale2(): BigDecimal {
        return setScale(2, RoundingMode.HALF_UP)
    }
}
