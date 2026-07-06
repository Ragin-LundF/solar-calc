package io.github.raginlundf.solarcalc.domain.services.calculation

import io.github.raginlundf.solarcalc.dtos.calculation.CalculationResponse
import io.github.raginlundf.solarcalc.dtos.calculation.ScenarioComparisonRequest

interface CalculationDomainController {
    fun calculate(profileId: Long, period: String): CalculationResponse
    fun compareScenarios(profileId: Long, period: String, request: ScenarioComparisonRequest): List<CalculationResponse>
}
