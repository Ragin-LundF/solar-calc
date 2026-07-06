package io.github.raginlundf.solarcalc.restapi.calculation

import io.github.raginlundf.solarcalc.domain.services.calculation.CalculationDomainController
import io.github.raginlundf.solarcalc.dtos.calculation.CalculationResponse
import io.github.raginlundf.solarcalc.dtos.calculation.ScenarioComparisonRequest
import io.github.raginlundf.solarcalc.restapi.security.SolarcalcScopes
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/profiles/{profileId}/calculations")
class CalculationController(
    private val calculationDomainController: CalculationDomainController,
) {

    @GetMapping("/{period}")
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_CALCULATIONS_READ}')")
    fun calculate(
        @PathVariable profileId: Long,
        @PathVariable period: String,
    ): CalculationResponse {
        return calculationDomainController.calculate(profileId = profileId, period = period)
    }

    @PostMapping("/{period}/compare")
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_CALCULATIONS_READ}')")
    fun compareScenarios(
        @PathVariable profileId: Long,
        @PathVariable period: String,
        @Valid @RequestBody request: ScenarioComparisonRequest,
    ): List<CalculationResponse> {
        return calculationDomainController.compareScenarios(
            profileId = profileId,
            period = period,
            request = request,
        )
    }
}
