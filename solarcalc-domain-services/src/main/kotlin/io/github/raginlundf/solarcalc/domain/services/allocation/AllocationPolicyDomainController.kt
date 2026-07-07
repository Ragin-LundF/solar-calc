package io.github.raginlundf.solarcalc.domain.services.allocation

import io.github.raginlundf.solarcalc.dtos.allocation.AllocationPolicyResponse
import io.github.raginlundf.solarcalc.dtos.allocation.CreateAllocationPolicyRequest
import io.github.raginlundf.solarcalc.dtos.allocation.UpdateAllocationPolicyRequest

interface AllocationPolicyDomainController {
    fun list(profileUuid: String, username: String): List<AllocationPolicyResponse>
    fun get(profileUuid: String, policyId: Long, username: String): AllocationPolicyResponse
    fun create(
        profileUuid: String,
        request: CreateAllocationPolicyRequest,
        username: String,
    ): AllocationPolicyResponse
    fun update(
        profileUuid: String,
        policyId: Long,
        request: UpdateAllocationPolicyRequest,
        username: String,
    ): AllocationPolicyResponse
    fun delete(profileUuid: String, policyId: Long, username: String)
}
