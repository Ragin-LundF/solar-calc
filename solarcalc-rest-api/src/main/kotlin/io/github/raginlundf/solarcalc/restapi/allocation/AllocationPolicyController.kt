package io.github.raginlundf.solarcalc.restapi.allocation

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
@RequestMapping("/api/v1/profiles/{profileId}/allocation-policies")
class AllocationPolicyController(
    private val allocationPolicyDomainController: AllocationPolicyDomainController,
) {

    @GetMapping
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_POLICIES_READ}')")
    fun list(@PathVariable profileId: Long): List<AllocationPolicyResponse> {
        return allocationPolicyDomainController.list(profileId = profileId)
    }

    @GetMapping("/{policyId}")
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_POLICIES_READ}')")
    fun get(
        @PathVariable profileId: Long,
        @PathVariable policyId: Long,
    ): AllocationPolicyResponse {
        return allocationPolicyDomainController.get(profileId = profileId, policyId = policyId)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_POLICIES_WRITE}')")
    fun create(
        @PathVariable profileId: Long,
        @Valid @RequestBody request: CreateAllocationPolicyRequest,
    ): AllocationPolicyResponse {
        return allocationPolicyDomainController.create(profileId = profileId, request = request)
    }

    @PutMapping("/{policyId}")
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_POLICIES_WRITE}')")
    fun update(
        @PathVariable profileId: Long,
        @PathVariable policyId: Long,
        @Valid @RequestBody request: UpdateAllocationPolicyRequest,
    ): AllocationPolicyResponse {
        return allocationPolicyDomainController.update(profileId = profileId, policyId = policyId, request = request)
    }

    @DeleteMapping("/{policyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_POLICIES_WRITE}')")
    fun delete(
        @PathVariable profileId: Long,
        @PathVariable policyId: Long,
    ) {
        allocationPolicyDomainController.delete(profileId = profileId, policyId = policyId)
    }
}
