package io.github.raginlundf.solarcalc.restapi.calculation

import io.github.raginlundf.logging.annotations.LogDuration
import io.github.raginlundf.solarcalc.domain.services.calculation.CalculationDomainController
import io.github.raginlundf.solarcalc.dtos.calculation.CalculationResponse
import io.github.raginlundf.solarcalc.dtos.calculation.ScenarioComparisonRequest
import io.github.raginlundf.solarcalc.restapi.security.SolarcalcScopes
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/profiles/{profileUuid}/calculations")
class CalculationController(
    private val calculationDomainController: CalculationDomainController,
) {

    @LogDuration
    @GetMapping
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_CALCULATIONS_READ}')")
    fun calculate(
        @PathVariable profileUuid: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
    ): CalculationResponse {
        return calculationDomainController.calculate(
            profileUuid = profileUuid,
            startDate = startDate,
            endDate = endDate,
        )
    }

    @LogDuration
    @PostMapping("/compare")
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_CALCULATIONS_READ}')")
    fun compareScenarios(
        @PathVariable profileUuid: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
        @Valid @RequestBody request: ScenarioComparisonRequest,
    ): List<CalculationResponse> {
        return calculationDomainController.compareScenarios(
            profileUuid = profileUuid,
            startDate = startDate,
            endDate = endDate,
            request = request,
        )
    }
}
