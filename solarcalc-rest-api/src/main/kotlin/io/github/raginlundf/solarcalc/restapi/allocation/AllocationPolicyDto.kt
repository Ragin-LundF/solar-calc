package io.github.raginlundf.solarcalc.restapi.allocation

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationCategory
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class AllocationPolicyResponse(
    val id: Long,
    val tenantId: Long,
    val energyProfileId: Long,
    val name: String,
    val priorityOrder: List<AllocationCategory>,
    val isDefault: Boolean,
)

data class CreateAllocationPolicyRequest(
    @field:NotBlank val name: String,
    @field:NotEmpty val priorityOrder: List<AllocationCategory>,
    val isDefault: Boolean = false,
)

data class UpdateAllocationPolicyRequest(
    @field:NotBlank val name: String,
    @field:NotEmpty val priorityOrder: List<AllocationCategory>,
    val isDefault: Boolean = false,
)
