package io.github.raginlundf.solarcalc.restapi.summary

import io.github.raginlundf.logging.annotations.LogDuration
import io.github.raginlundf.solarcalc.domain.services.auth.SolarcalcScopes
import io.github.raginlundf.solarcalc.domain.services.summary.SummaryDomainController
import io.github.raginlundf.solarcalc.dtos.summary.SummaryResponse
import io.github.raginlundf.solarcalc.restapi.security.CurrentUser
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/profiles/{profileUuid}/summary")
class SummaryController(
    private val summaryDomainController: SummaryDomainController,
) {

    @LogDuration
    @GetMapping
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_READ}')")
    fun summarize(
        @PathVariable profileUuid: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
    ): SummaryResponse {
        return summaryDomainController.summarize(
            profileUuid = profileUuid,
            username = CurrentUser.requireUsername(),
            startDate = startDate,
            endDate = endDate,
        )
    }
}
