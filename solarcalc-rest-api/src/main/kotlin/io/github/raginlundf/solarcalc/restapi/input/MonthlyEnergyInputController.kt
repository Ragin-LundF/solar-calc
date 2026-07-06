package io.github.raginlundf.solarcalc.restapi.input

import io.github.raginlundf.solarcalc.domain.models.input.MonthlyEnergyInput
import io.github.raginlundf.solarcalc.domain.models.repository.EnergyProfileRepository
import io.github.raginlundf.solarcalc.domain.models.repository.MonthlyEnergyInputRepository
import io.github.raginlundf.solarcalc.domain.models.repository.TenantRepository
import io.github.raginlundf.solarcalc.restapi.error.ApiError
import io.github.raginlundf.solarcalc.restapi.error.ResourceNotFoundException
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
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/tenants/{tenantId}/profiles/{profileId}/monthly-inputs")
class MonthlyEnergyInputController(
    private val tenantRepository: TenantRepository,
    private val profileRepository: EnergyProfileRepository,
    private val inputRepository: MonthlyEnergyInputRepository,
) {

    @GetMapping
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_INPUTS_READ}')")
    fun list(@PathVariable tenantId: Long, @PathVariable profileId: Long): List<MonthlyEnergyInputResponse> {
        requireProfile(profileId = profileId)
        return inputRepository.findAllByTenantIdAndEnergyProfileId(tenantId = tenantId, energyProfileId = profileId).map { it.toResponse() }
    }

    @GetMapping("/{inputId}")
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_INPUTS_READ}')")
    fun get(
        @PathVariable tenantId: Long,
        @PathVariable profileId: Long,
        @PathVariable inputId: Long,
    ): MonthlyEnergyInputResponse {
        requireProfile(profileId = profileId)
        return inputRepository.findByIdAndTenantId(id = inputId, tenantId = tenantId)?.toResponse()
            ?: throw ResourceNotFoundException("MonthlyInput $inputId not found for tenant $tenantId")
    }

    @PostMapping
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_INPUTS_WRITE}')")
    fun create(
        @PathVariable tenantId: Long,
        @PathVariable profileId: Long,
        @Valid @RequestBody request: UpsertMonthlyEnergyInputRequest,
    ): ResponseEntity<Any> {
        val tenant = tenantRepository.findById(tenantId).orElseThrow {
            ResourceNotFoundException("Tenant $tenantId not found")
        }
        val profile = profileRepository.findById(profileId).orElseThrow {
            ResourceNotFoundException("Profile $profileId not found")
        }

        // Validate feed-in vs generation
        if (request.feedInKwh != null && request.feedInKwh > request.generationKwh) {
            return ResponseEntity.unprocessableEntity().body(
                ApiError(
                    code = "MONTHLY_INPUT_FEED_IN_EXCEEDS_GENERATION",
                    message = "Feed-in exceeds generated electricity.",
                    details = mapOf(
                        "generationKwh" to request.generationKwh.toPlainString(),
                        "feedInKwh" to request.feedInKwh.toString(),
                    ),
                ),
            )
        }

        val existing = inputRepository.findByTenantIdAndEnergyProfileIdAndPeriod(tenantId = tenantId, energyProfileId = profileId, period = request.period)
        if (existing != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiError(
                    code = "MONTHLY_INPUT_ALREADY_EXISTS",
                    message = "Input for period ${request.period} already exists.",
                ),
            )
        }

        val input = MonthlyEnergyInput().apply {
            this.tenant = tenant
            this.energyProfile = profile
            applyRequest(request)
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(inputRepository.save(input).toResponse())
    }

    @PutMapping("/{inputId}")
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_INPUTS_WRITE}')")
    fun update(
        @PathVariable tenantId: Long,
        @PathVariable profileId: Long,
        @PathVariable inputId: Long,
        @Valid @RequestBody request: UpsertMonthlyEnergyInputRequest,
    ): ResponseEntity<Any> {
        requireProfile(profileId = profileId)
        val input = inputRepository.findByIdAndTenantId(id = inputId, tenantId = tenantId)
            ?: throw ResourceNotFoundException("MonthlyInput $inputId not found for tenant $tenantId")

        if (request.feedInKwh != null && request.feedInKwh > request.generationKwh) {
            return ResponseEntity.unprocessableEntity().body(
                ApiError(
                    code = "MONTHLY_INPUT_FEED_IN_EXCEEDS_GENERATION",
                    message = "Feed-in exceeds generated electricity.",
                    details = mapOf(
                        "generationKwh" to request.generationKwh.toPlainString(),
                        "feedInKwh" to request.feedInKwh.toString(),
                    ),
                ),
            )
        }

        input.applyRequest(request)
        input.updatedAt = LocalDateTime.now()
        return ResponseEntity.ok(inputRepository.save(input).toResponse())
    }

    @DeleteMapping("/{inputId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_INPUTS_WRITE}')")
    fun delete(
        @PathVariable tenantId: Long,
        @PathVariable profileId: Long,
        @PathVariable inputId: Long,
    ) {
        requireProfile(profileId = profileId)
        val input = inputRepository.findByIdAndTenantId(id = inputId, tenantId = tenantId)
            ?: throw ResourceNotFoundException("MonthlyInput $inputId not found for tenant $tenantId")
        inputRepository.delete(input)
    }

    private fun requireProfile(profileId: Long) {
        profileRepository.findById(profileId).orElseThrow {
            ResourceNotFoundException("Profile $profileId not found")
        }
    }
}

private fun MonthlyEnergyInput.applyRequest(request: UpsertMonthlyEnergyInputRequest) {
    period = request.period
    consumptionKwh = request.consumptionKwh
    generationKwh = request.generationKwh
    feedInKwh = request.feedInKwh
    householdConsumptionKwh = request.householdConsumptionKwh
    heatPumpConsumptionKwh = request.heatPumpConsumptionKwh
    wallboxConsumptionKwh = request.wallboxConsumptionKwh
    electricityPriceOverride = request.electricityPriceOverride
    feedInTariffOverride = request.feedInTariffOverride
    petrolPriceOverride = request.petrolPriceOverride
    evEfficiencyOverrideKwh100km = request.evEfficiencyOverrideKwh100km
    iceEfficiencyOverrideL100km = request.iceEfficiencyOverrideL100km
    heatingReferenceCostOverride = request.heatingReferenceCostOverride
}

private fun MonthlyEnergyInput.toResponse(): MonthlyEnergyInputResponse {
    return MonthlyEnergyInputResponse(
        id = id!!,
        tenantId = tenantId!!,
        energyProfileId = energyProfileId!!,
        period = period,
        consumptionKwh = consumptionKwh,
        generationKwh = generationKwh,
        feedInKwh = feedInKwh,
        householdConsumptionKwh = householdConsumptionKwh,
        heatPumpConsumptionKwh = heatPumpConsumptionKwh,
        wallboxConsumptionKwh = wallboxConsumptionKwh,
        electricityPriceOverride = electricityPriceOverride,
        feedInTariffOverride = feedInTariffOverride,
        petrolPriceOverride = petrolPriceOverride,
        evEfficiencyOverrideKwh100km = evEfficiencyOverrideKwh100km,
        iceEfficiencyOverrideL100km = iceEfficiencyOverrideL100km,
        heatingReferenceCostOverride = heatingReferenceCostOverride,
    )
}
