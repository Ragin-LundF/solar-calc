package io.github.raginlundf.solarcalc.domain.services.allocation

import io.github.raginlundf.solarcalc.dtos.allocation.AllocationPolicyResponse
import io.github.raginlundf.solarcalc.dtos.allocation.CreateAllocationPolicyRequest
import io.github.raginlundf.solarcalc.dtos.allocation.UpdateAllocationPolicyRequest

interface AllocationPolicyDomainController {
    fun list(profileId: Long): List<AllocationPolicyResponse>
    fun get(profileId: Long, policyId: Long): AllocationPolicyResponse
    fun create(profileId: Long, request: CreateAllocationPolicyRequest): AllocationPolicyResponse
    fun update(profileId: Long, policyId: Long, request: UpdateAllocationPolicyRequest): AllocationPolicyResponse
    fun delete(profileId: Long, policyId: Long)
}
