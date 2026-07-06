package io.github.raginlundf.solarcalc.domain.services.allocation

import io.github.raginlundf.solarcalc.dtos.allocation.AllocationPolicyResponse
import io.github.raginlundf.solarcalc.dtos.allocation.CreateAllocationPolicyRequest
import io.github.raginlundf.solarcalc.dtos.allocation.UpdateAllocationPolicyRequest

interface AllocationPolicyDomainController {
    fun list(profileUuid: String): List<AllocationPolicyResponse>
    fun get(profileUuid: String, policyId: Long): AllocationPolicyResponse
    fun create(profileUuid: String, request: CreateAllocationPolicyRequest): AllocationPolicyResponse
    fun update(profileUuid: String, policyId: Long, request: UpdateAllocationPolicyRequest): AllocationPolicyResponse
    fun delete(profileUuid: String, policyId: Long)
}
