package io.github.raginlundf.solarcalc.domain.services.summary

import io.github.raginlundf.extensions.scale2
import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationCategory
import io.github.raginlundf.solarcalc.dtos.summary.MonthlySummary
import io.github.raginlundf.solarcalc.dtos.summary.PaybackProjection
import io.github.raginlundf.solarcalc.dtos.summary.SummaryAggregates
import io.github.raginlundf.solarcalc.dtos.summary.SummaryResponse
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class SummaryServiceImpl : SummaryService {

    private companion object {
        val HUNDRED: BigDecimal = BigDecimal(100)
        const val PAYBACK_WINDOW = 6
    }

    override fun summarize(
        months: List<SummaryMonthInput>,
        params: SummaryParams,
        rangeStart: String?,
        rangeEnd: String?,
    ): SummaryResponse {
        val sorted = months.sortedBy { it.period }

        var running = BigDecimal.ZERO
        val enriched = sorted.map { month ->
            val summary = computeMonth(month = month, params = params)
            running = (running + summary.totalSavings)
            summary.copy(cumulativeSavings = running.scale2())
        }

        val filtered = enriched.filter { month ->
            (rangeStart == null || month.period >= rangeStart) && (rangeEnd == null || month.period <= rangeEnd)
        }

        return SummaryResponse(
            allocationPriority = params.allocationPriority,
            months = filtered,
            aggregates = aggregate(filtered = filtered, params = params),
            payback = projectPayback(enriched = enriched, investKosten = params.investKosten),
        )
    }

    private fun computeMonth(month: SummaryMonthInput, params: SummaryParams): MonthlySummary {
        val generation = month.generationKwh.max(BigDecimal.ZERO)
        val feedIn = month.feedInKwh.max(BigDecimal.ZERO).min(generation)
        val selfConsumed = (generation - feedIn).max(BigDecimal.ZERO)

        val heatPumpDemand = if (params.hasHeatPump) month.heatPumpKwh.max(BigDecimal.ZERO) else BigDecimal.ZERO
        val wallboxDemand = if (params.hasWallbox) month.wallboxKwh.max(BigDecimal.ZERO) else BigDecimal.ZERO
        val householdDemand = (
            month.householdKwh ?: (month.consumptionKwh - heatPumpDemand - wallboxDemand)
            ).max(BigDecimal.ZERO)

        val demand = mapOf(
            AllocationCategory.HOUSEHOLD to householdDemand,
            AllocationCategory.HEAT_PUMP to heatPumpDemand,
            AllocationCategory.WALLBOX to wallboxDemand,
        )
        val solar = allocate(demand = demand, priority = params.allocationPriority, pool = selfConsumed)

        val household = costPair(
            demand = householdDemand,
            solar = solar.getValue(AllocationCategory.HOUSEHOLD),
            gridPrice = month.gridPrice,
        )
        val heatPump = costPair(
            demand = heatPumpDemand,
            solar = solar.getValue(AllocationCategory.HEAT_PUMP),
            gridPrice = month.gridPrice,
        )
        val wallbox = costPair(
            demand = wallboxDemand,
            solar = solar.getValue(AllocationCategory.WALLBOX),
            gridPrice = month.gridPrice,
        )

        val heizPct = params.heatingDistribution.getOrElse(monthIndex(month.period)) { 0 }
        val heatingActive = params.hasHeatPump && month.heizReferenzJahr > BigDecimal.ZERO
        val oilCost = if (heatingActive) {
            month.heizReferenzJahr * BigDecimal(heizPct) / HUNDRED
        } else {
            BigDecimal.ZERO
        }
        val heatingSavings = if (heatingActive) oilCost - heatPump.costWithSolar else BigDecimal.ZERO

        val estimatedKm = wallboxDemand * params.kmPerKwh
        val gasolineEquivalentCost = estimatedKm / HUNDRED * params.litersPer100km * month.petrolPrice
        val wallboxSavingsVsGasoline = if (params.hasWallbox) gasolineEquivalentCost - wallbox.costWithSolar else BigDecimal.ZERO

        val feedInRevenue = feedIn * month.feedInTariff
        val totalSavings = household.savings + heatingSavings + wallboxSavingsVsGasoline + feedInRevenue
        val quote = if (generation > BigDecimal.ZERO) {
            selfConsumed.divide(generation, 6, RoundingMode.HALF_UP) * HUNDRED
        } else {
            BigDecimal.ZERO
        }

        return MonthlySummary(
            period = month.period,
            generationKwh = generation.scale2(),
            feedInKwh = feedIn.scale2(),
            selfConsumedKwh = selfConsumed.scale2(),
            selfConsumptionQuotePct = quote.scale2(),
            feedInRevenue = feedInRevenue.scale2(),
            householdConsumptionKwh = householdDemand.scale2(),
            householdSolarKwh = household.solar.scale2(),
            householdGridKwh = household.grid.scale2(),
            householdCostWithoutSolar = household.costWithoutSolar.scale2(),
            householdCostWithSolar = household.costWithSolar.scale2(),
            householdSavings = household.savings.scale2(),
            heatPumpConsumptionKwh = heatPumpDemand.scale2(),
            heatPumpSolarKwh = heatPump.solar.scale2(),
            heatPumpGridKwh = heatPump.grid.scale2(),
            heatPumpCostWithSolar = heatPump.costWithSolar.scale2(),
            heizPct = heizPct,
            oilCost = oilCost.scale2(),
            heatingSavings = heatingSavings.scale2(),
            wallboxConsumptionKwh = wallboxDemand.scale2(),
            wallboxSolarKwh = wallbox.solar.scale2(),
            wallboxGridKwh = wallbox.grid.scale2(),
            wallboxCostWithSolar = wallbox.costWithSolar.scale2(),
            estimatedKm = estimatedKm.scale2(),
            gasolineEquivalentCost = gasolineEquivalentCost.scale2(),
            wallboxSavingsVsGasoline = wallboxSavingsVsGasoline.scale2(),
            totalSavings = totalSavings.scale2(),
            cumulativeSavings = BigDecimal.ZERO,
        )
    }

    private data class CostPair(
        val solar: BigDecimal,
        val grid: BigDecimal,
        val costWithoutSolar: BigDecimal,
        val costWithSolar: BigDecimal,
        val savings: BigDecimal,
    )

    private fun costPair(
        demand: BigDecimal,
        solar: BigDecimal,
        gridPrice: BigDecimal,
    ): CostPair {
        val grid = (demand - solar).max(BigDecimal.ZERO)
        val costWithoutSolar = demand * gridPrice
        // Self-consumed solar is free; the real cost is only the grid top-up.
        val costWithSolar = grid * gridPrice
        return CostPair(
            solar = solar,
            grid = grid,
            costWithoutSolar = costWithoutSolar,
            costWithSolar = costWithSolar,
            savings = costWithoutSolar - costWithSolar,
        )
    }

    private fun allocate(
        demand: Map<AllocationCategory, BigDecimal>,
        priority: List<AllocationCategory>,
        pool: BigDecimal,
    ): Map<AllocationCategory, BigDecimal> {
        var remaining = pool
        val result = AllocationCategory.entries.associateWith { BigDecimal.ZERO }.toMutableMap()
        for (category in priority) {
            val allocated = remaining.min(demand[category] ?: BigDecimal.ZERO)
            result[category] = allocated
            remaining -= allocated
        }
        return result
    }

    private fun aggregate(filtered: List<MonthlySummary>, params: SummaryParams): SummaryAggregates {
        fun sum(selector: (MonthlySummary) -> BigDecimal): BigDecimal =
            filtered.fold(BigDecimal.ZERO) { acc, month -> acc + selector(month) }.scale2()

        val feedInRevenue = sum { it.feedInRevenue }
        val householdSavings = sum { it.householdSavings }
        val heatingSavings = sum { it.heatingSavings }
        val wallboxSavings = sum { it.wallboxSavingsVsGasoline }

        return SummaryAggregates(
            generationKwh = sum { it.generationKwh },
            selfConsumedKwh = sum { it.selfConsumedKwh },
            feedInKwh = sum { it.feedInKwh },
            feedInRevenue = feedInRevenue,
            householdConsumptionKwh = sum { it.householdConsumptionKwh },
            householdCostWithoutSolar = sum { it.householdCostWithoutSolar },
            householdCostWithSolar = sum { it.householdCostWithSolar },
            householdSavings = householdSavings,
            oilCost = sum { it.oilCost },
            heatPumpCost = sum { it.heatPumpCostWithSolar },
            heatingSavings = heatingSavings,
            wallboxConsumptionKwh = sum { it.wallboxConsumptionKwh },
            estimatedKm = sum { it.estimatedKm },
            gasolineEquivalentCost = sum { it.gasolineEquivalentCost },
            wallboxChargingCost = sum { it.wallboxCostWithSolar },
            wallboxSavingsVsGasoline = wallboxSavings,
            distributionSumPct = params.heatingDistribution.sum(),
            totalSavings = (feedInRevenue + householdSavings + heatingSavings + wallboxSavings).scale2(),
        )
    }

    private fun projectPayback(enriched: List<MonthlySummary>, investKosten: BigDecimal): PaybackProjection {
        val lastCum = enriched.lastOrNull()?.cumulativeSavings ?: BigDecimal.ZERO
        val amortised = investKosten > BigDecimal.ZERO && lastCum >= investKosten
        val paybackPct = if (investKosten > BigDecimal.ZERO) {
            (lastCum.divide(investKosten, 6, RoundingMode.HALF_UP) * HUNDRED).min(HUNDRED).max(BigDecimal.ZERO)
        } else {
            BigDecimal.ZERO
        }

        val projected = if (amortised || investKosten <= BigDecimal.ZERO || enriched.isEmpty()) {
            null
        } else {
            val window = enriched.takeLast(PAYBACK_WINDOW)
            val avg = window.fold(BigDecimal.ZERO) { acc, m -> acc + m.totalSavings }
                .divide(BigDecimal(window.size), 6, RoundingMode.HALF_UP)
            if (avg > BigDecimal.ZERO) {
                val remaining = investKosten - lastCum
                val monthsNeeded = remaining.divide(avg, 0, RoundingMode.CEILING).toInt()
                addMonths(period = enriched.last().period, months = monthsNeeded)
            } else {
                null
            }
        }

        return PaybackProjection(
            cumulativeSavings = lastCum.scale2(),
            investKosten = investKosten.scale2(),
            paybackPct = paybackPct.scale2(),
            amortised = amortised,
            projectedPaybackPeriod = projected,
        )
    }

    /** 0-based calendar month index (Jan=0) from a YYYY-MM period. */
    private fun monthIndex(period: String): Int = period.substringAfter('-').toInt() - 1

    private fun addMonths(period: String, months: Int): String {
        val (year, month) = period.split("-").map { it.toInt() }
        val zeroBased = (month - 1) + months
        val newYear = year + Math.floorDiv(zeroBased, 12)
        val newMonth = Math.floorMod(zeroBased, 12) + 1
        return "%04d-%02d".format(newYear, newMonth)
    }
}
