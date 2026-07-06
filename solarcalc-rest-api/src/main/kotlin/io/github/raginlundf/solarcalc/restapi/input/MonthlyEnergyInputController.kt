package io.github.raginlundf.solarcalc.restapi.input

import io.github.raginlundf.logging.annotations.LogDuration
import io.github.raginlundf.solarcalc.domain.services.input.DuplicateInputException
import io.github.raginlundf.solarcalc.domain.services.input.MonthlyEnergyInputDomainController
import io.github.raginlundf.solarcalc.dtos.error.ApiError
import io.github.raginlundf.solarcalc.dtos.input.MonthlyEnergyInputResponse
import io.github.raginlundf.solarcalc.dtos.input.UpsertMonthlyEnergyInputRequest
import io.github.raginlundf.solarcalc.restapi.security.SolarcalcScopes
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/profiles/{profileUuid}/monthly-inputs")
class MonthlyEnergyInputController(
    private val monthlyEnergyInputDomainController: MonthlyEnergyInputDomainController,
) {

    @LogDuration
    @GetMapping
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_INPUTS_READ}')")
    fun list(@PathVariable profileUuid: String): List<MonthlyEnergyInputResponse> {
        return monthlyEnergyInputDomainController.list(profileUuid = profileUuid)
    }

    @LogDuration
    @GetMapping("/{inputId}")
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_INPUTS_READ}')")
    fun get(
        @PathVariable profileUuid: String,
        @PathVariable inputId: Long,
    ): MonthlyEnergyInputResponse {
        return monthlyEnergyInputDomainController.get(profileUuid = profileUuid, inputId = inputId)
    }

    @LogDuration
    @PostMapping
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_INPUTS_WRITE}')")
    fun create(
        @PathVariable profileUuid: String,
        @Valid @RequestBody request: UpsertMonthlyEnergyInputRequest,
    ): ResponseEntity<Any> {
        val feedInKwh = request.feedInKwh
        if (feedInKwh != null && feedInKwh > request.generationKwh) {
            return ResponseEntity.unprocessableEntity().body(
                ApiError(
                    code = "MONTHLY_INPUT_FEED_IN_EXCEEDS_GENERATION",
                    message = "Feed-in exceeds generated electricity.",
                    details = mapOf(
                        "generationKwh" to request.generationKwh.toPlainString(),
                        "feedInKwh" to feedInKwh.toPlainString(),
                    ),
                ),
            )
        }

        return try {
            val result = monthlyEnergyInputDomainController.create(
                profileUuid = profileUuid,
                request = request,
            )
            ResponseEntity.status(HttpStatus.CREATED).body(result as Any)
        } catch (e: DuplicateInputException) {
            ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiError(
                    code = "MONTHLY_INPUT_ALREADY_EXISTS",
                    message = e.message ?: "Input for period already exists.",
                ) as Any,
            )
        }
    }

    @LogDuration
    @PutMapping("/{inputId}")
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_INPUTS_WRITE}')")
    fun update(
        @PathVariable profileUuid: String,
        @PathVariable inputId: Long,
        @Valid @RequestBody request: UpsertMonthlyEnergyInputRequest,
    ): ResponseEntity<Any> {
        val feedInKwh = request.feedInKwh
        if (feedInKwh != null && feedInKwh > request.generationKwh) {
            return ResponseEntity.unprocessableEntity().body(
                ApiError(
                    code = "MONTHLY_INPUT_FEED_IN_EXCEEDS_GENERATION",
                    message = "Feed-in exceeds generated electricity.",
                    details = mapOf(
                        "generationKwh" to request.generationKwh.toPlainString(),
                        "feedInKwh" to feedInKwh.toPlainString(),
                    ),
                ),
            )
        }

        val result = monthlyEnergyInputDomainController.update(
            profileUuid = profileUuid,
            inputId = inputId,
            request = request,
        )
        return ResponseEntity.ok(result as Any)
    }

    @LogDuration
    @DeleteMapping("/{inputId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_INPUTS_WRITE}')")
    fun delete(
        @PathVariable profileUuid: String,
        @PathVariable inputId: Long,
    ) {
        monthlyEnergyInputDomainController.delete(profileUuid = profileUuid, inputId = inputId)
    }
}
