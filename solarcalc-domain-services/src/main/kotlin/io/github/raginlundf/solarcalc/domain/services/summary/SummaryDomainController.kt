package io.github.raginlundf.solarcalc.domain.services.summary

import io.github.raginlundf.solarcalc.dtos.summary.SummaryResponse
import java.time.LocalDate

interface SummaryDomainController {

    fun summarize(
        profileUuid: String,
        username: String,
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): SummaryResponse
}
