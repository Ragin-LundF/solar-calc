package io.github.raginlundf.solarcalc.restapi.profile

import io.github.raginlundf.logging.annotations.LogDuration
import io.github.raginlundf.solarcalc.domain.services.profile.ProfileDomainController
import io.github.raginlundf.solarcalc.dtos.profile.CreateEnergyProfileRequest
import io.github.raginlundf.solarcalc.dtos.profile.EnergyProfileResponse
import io.github.raginlundf.solarcalc.dtos.profile.UpdateEnergyProfileRequest
import io.github.raginlundf.solarcalc.domain.services.auth.SolarcalcScopes
import io.github.raginlundf.solarcalc.restapi.security.CurrentUser
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

@RestController
@RequestMapping("/api/v1/profiles")
class EnergyProfileController(
    private val profileDomainController: ProfileDomainController,
) {

    @LogDuration
    @GetMapping
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_READ}')")
    fun list(): List<EnergyProfileResponse> {
        return profileDomainController.list(username = CurrentUser.requireUsername())
    }

    @LogDuration
    @GetMapping("/{profileUuid}")
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_READ}')")
    fun get(@PathVariable profileUuid: String): EnergyProfileResponse {
        return profileDomainController.get(profileUuid = profileUuid, username = CurrentUser.requireUsername())
    }

    @LogDuration
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_WRITE}')")
    fun create(@Valid @RequestBody request: CreateEnergyProfileRequest): EnergyProfileResponse {
        return profileDomainController.create(request = request, username = CurrentUser.requireUsername())
    }

    @LogDuration
    @PutMapping("/{profileUuid}")
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_WRITE}')")
    fun update(
        @PathVariable profileUuid: String,
        @Valid @RequestBody request: UpdateEnergyProfileRequest
    ): EnergyProfileResponse {
        return profileDomainController.update(
            profileUuid = profileUuid,
            request = request,
            username = CurrentUser.requireUsername(),
        )
    }

    @LogDuration
    @DeleteMapping("/{profileUuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_WRITE}')")
    fun delete(@PathVariable profileUuid: String) {
        profileDomainController.delete(profileUuid = profileUuid, username = CurrentUser.requireUsername())
    }
}
