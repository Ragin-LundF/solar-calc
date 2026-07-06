package io.github.raginlundf.solarcalc.restapi.allocation

import io.github.raginlundf.logging.annotations.LogDuration
import io.github.raginlundf.solarcalc.domain.services.allocation.AllocationPolicyDomainController
import io.github.raginlundf.solarcalc.dtos.allocation.AllocationPolicyResponse
import io.github.raginlundf.solarcalc.dtos.allocation.CreateAllocationPolicyRequest
import io.github.raginlundf.solarcalc.dtos.allocation.UpdateAllocationPolicyRequest
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

@RestController
@RequestMapping("/api/v1/profiles/{profileUuid}/allocation-policies")
class AllocationPolicyController(
    private val allocationPolicyDomainController: AllocationPolicyDomainController,
) {
    @LogDuration
    @GetMapping
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_POLICIES_READ}')")
    fun list(@PathVariable profileUuid: String): List<AllocationPolicyResponse> {
        return allocationPolicyDomainController.list(profileUuid = profileUuid)
    }

    @LogDuration
    @GetMapping("/{policyId}")
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_POLICIES_READ}')")
    fun get(
        @PathVariable profileUuid: String,
        @PathVariable policyId: Long,
    ): AllocationPolicyResponse {
        return allocationPolicyDomainController.get(profileUuid = profileUuid, policyId = policyId)
    }

    @LogDuration
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_POLICIES_WRITE}')")
    fun create(
        @PathVariable profileUuid: String,
        @Valid @RequestBody request: CreateAllocationPolicyRequest,
    ): AllocationPolicyResponse {
        return allocationPolicyDomainController.create(profileUuid = profileUuid, request = request)
    }

    @LogDuration
    @PutMapping("/{policyId}")
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_POLICIES_WRITE}')")
    fun update(
        @PathVariable profileUuid: String,
        @PathVariable policyId: Long,
        @Valid @RequestBody request: UpdateAllocationPolicyRequest,
    ): AllocationPolicyResponse {
        return allocationPolicyDomainController.update(profileUuid = profileUuid, policyId = policyId, request = request)
    }

    @LogDuration
    @DeleteMapping("/{policyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_POLICIES_WRITE}')")
    fun delete(
        @PathVariable profileUuid: String,
        @PathVariable policyId: Long,
    ) {
        allocationPolicyDomainController.delete(profileUuid = profileUuid, policyId = policyId)
    }
}
