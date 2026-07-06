package io.github.raginlundf.solarcalc.domain.services.calculation

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationCategory

interface EnergyCalculationService {

    fun calculate(input: CalculationInput): CalculationResult

    fun compareScenarios(
        input: CalculationInput,
        priorities: List<List<AllocationCategory>>,
    ): List<CalculationResult>
}
