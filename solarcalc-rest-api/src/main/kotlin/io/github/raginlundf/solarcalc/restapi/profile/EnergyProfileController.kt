package io.github.raginlundf.solarcalc.restapi.profile

import io.github.raginlundf.logging.annotations.LogDuration
import io.github.raginlundf.solarcalc.domain.services.profile.ProfileDomainController
import io.github.raginlundf.solarcalc.dtos.profile.CreateEnergyProfileRequest
import io.github.raginlundf.solarcalc.dtos.profile.EnergyProfileResponse
import io.github.raginlundf.solarcalc.dtos.profile.UpdateEnergyProfileRequest
import io.github.raginlundf.solarcalc.restapi.security.SolarcalcScopes
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
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
@RequestMapping("/api/v1/profiles")
class EnergyProfileController(
    private val profileDomainController: ProfileDomainController,
) {

    @LogDuration
    @GetMapping
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_PROFILES_READ}')")
    fun list(): List<EnergyProfileResponse> {
        return profileDomainController.list()
    }

    @LogDuration
    @GetMapping("/{profileUuid}")
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_PROFILES_READ}')")
    fun get(@PathVariable profileUuid: String): EnergyProfileResponse {
        return profileDomainController.get(profileUuid = profileUuid)
    }

    @LogDuration
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_PROFILES_WRITE}')")
    fun create(@Valid @RequestBody request: CreateEnergyProfileRequest): EnergyProfileResponse {
        val username = SecurityContextHolder.getContext().authentication?.name
            ?: throw IllegalStateException("No authenticated user")
        return profileDomainController.create(request = request, username = username)
    }

    @LogDuration
    @PutMapping("/{profileUuid}")
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_PROFILES_WRITE}')")
    fun update(
        @PathVariable profileUuid: String,
        @Valid @RequestBody request: UpdateEnergyProfileRequest
    ): EnergyProfileResponse {
        return profileDomainController.update(profileUuid = profileUuid, request = request)
    }

    @LogDuration
    @DeleteMapping("/{profileUuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_PROFILES_WRITE}')")
    fun delete(@PathVariable profileUuid: String) {
        profileDomainController.delete(profileUuid = profileUuid)
    }
}
