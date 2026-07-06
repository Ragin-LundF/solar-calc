package io.github.raginlundf.solarcalc.restapi.profile

import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfile
import io.github.raginlundf.solarcalc.domain.models.repository.EnergyProfileRepository
import io.github.raginlundf.solarcalc.restapi.error.ResourceNotFoundException
import io.github.raginlundf.solarcalc.restapi.security.SolarcalcScopes
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
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
@RequestMapping("/api/profiles")
class EnergyProfileController(
    private val profileRepository: EnergyProfileRepository,
) {

    @GetMapping
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_PROFILES_READ}')")
    fun list(): List<EnergyProfileResponse> {
        return profileRepository.findAll().map { it.toResponse() }
    }

    @GetMapping("/{profileId}")
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_PROFILES_READ}')")
    fun get(@PathVariable profileId: Long): EnergyProfileResponse {
        val profile = profileRepository.findById(profileId).orElseThrow {
            ResourceNotFoundException("Profile $profileId not found")
        }
        return profile.toResponse()
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_PROFILES_WRITE}')")
    fun create(
        @Valid @RequestBody request: CreateEnergyProfileRequest,
    ): EnergyProfileResponse {
        val profile = EnergyProfile().apply {
            name = request.name
        }
        return profileRepository.save(profile).toResponse()
    }

    @PutMapping("/{profileId}")
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_PROFILES_WRITE}')")
    fun update(
        @PathVariable profileId: Long,
        @Valid @RequestBody request: UpdateEnergyProfileRequest,
    ): EnergyProfileResponse {
        val profile = profileRepository.findById(profileId).orElseThrow {
            ResourceNotFoundException("Profile $profileId not found")
        }
        profile.name = request.name
        profile.updatedAt = LocalDateTime.now()
        return profileRepository.save(profile).toResponse()
    }

    @DeleteMapping("/{profileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_PROFILES_WRITE}')")
    fun delete(@PathVariable profileId: Long) {
        val profile = profileRepository.findById(profileId).orElseThrow {
            ResourceNotFoundException("Profile $profileId not found")
        }
        profileRepository.delete(profile)
    }
}

private fun EnergyProfile.toResponse(): EnergyProfileResponse {
    return EnergyProfileResponse(id = id!!, name = name)
}
