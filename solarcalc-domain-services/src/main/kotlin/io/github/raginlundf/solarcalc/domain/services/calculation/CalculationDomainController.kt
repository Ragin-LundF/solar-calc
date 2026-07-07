package io.github.raginlundf.solarcalc.domain.services.calculation

import io.github.raginlundf.solarcalc.dtos.calculation.CalculationResponse
import io.github.raginlundf.solarcalc.dtos.calculation.ScenarioComparisonRequest
import java.time.LocalDate

interface CalculationDomainController {
    fun calculate(
        profileUuid: String,
        username: String,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
    ): CalculationResponse

    fun compareScenarios(
        profileUuid: String,
        username: String,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        request: ScenarioComparisonRequest,
    ): List<CalculationResponse>
}
