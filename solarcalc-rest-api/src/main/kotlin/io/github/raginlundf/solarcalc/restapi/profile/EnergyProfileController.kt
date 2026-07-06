package io.github.raginlundf.solarcalc.restapi.profile

import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfile
import io.github.raginlundf.solarcalc.domain.models.repository.EnergyProfileRepository
import io.github.raginlundf.solarcalc.domain.models.repository.TenantRepository
import io.github.raginlundf.solarcalc.restapi.error.ResourceNotFoundException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
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
@RequestMapping("/api/tenants/{tenantId}/profiles")
class EnergyProfileController(
    private val tenantRepository: TenantRepository,
    private val profileRepository: EnergyProfileRepository,
) {

    @GetMapping
    fun list(@PathVariable tenantId: Long): List<EnergyProfileResponse> {
        requireTenantExists(tenantId)
        return profileRepository.findAllByTenantId(tenantId).map { it.toResponse() }
    }

    @GetMapping("/{profileId}")
    fun get(@PathVariable tenantId: Long, @PathVariable profileId: Long): EnergyProfileResponse {
        return profileRepository.findByIdAndTenantId(profileId, tenantId)?.toResponse()
            ?: throw ResourceNotFoundException("Profile $profileId not found for tenant $tenantId")
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable tenantId: Long,
        @Valid @RequestBody request: CreateEnergyProfileRequest,
    ): EnergyProfileResponse {
        val tenant = tenantRepository.findById(tenantId).orElseThrow {
            ResourceNotFoundException("Tenant $tenantId not found")
        }
        val profile = EnergyProfile().apply {
            this.tenant = tenant
            name = request.name
        }
        return profileRepository.save(profile).toResponse()
    }

    @PutMapping("/{profileId}")
    fun update(
        @PathVariable tenantId: Long,
        @PathVariable profileId: Long,
        @Valid @RequestBody request: UpdateEnergyProfileRequest,
    ): EnergyProfileResponse {
        val profile = profileRepository.findByIdAndTenantId(profileId, tenantId)
            ?: throw ResourceNotFoundException("Profile $profileId not found for tenant $tenantId")
        profile.name = request.name
        profile.updatedAt = LocalDateTime.now()
        return profileRepository.save(profile).toResponse()
    }

    @DeleteMapping("/{profileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable tenantId: Long, @PathVariable profileId: Long) {
        val profile = profileRepository.findByIdAndTenantId(profileId, tenantId)
            ?: throw ResourceNotFoundException("Profile $profileId not found for tenant $tenantId")
        profileRepository.delete(profile)
    }

    private fun requireTenantExists(tenantId: Long) {
        if (!tenantRepository.existsById(tenantId)) {
            throw ResourceNotFoundException("Tenant $tenantId not found")
        }
    }
}

private fun EnergyProfile.toResponse(): EnergyProfileResponse {
    return EnergyProfileResponse(id = id!!, tenantId = tenantId!!, name = name)
}
