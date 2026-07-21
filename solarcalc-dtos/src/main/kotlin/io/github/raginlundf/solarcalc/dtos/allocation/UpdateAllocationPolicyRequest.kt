package io.github.raginlundf.solarcalc.dtos.allocation

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationCategory
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class UpdateAllocationPolicyRequest(
    @field:NotBlank val name: String,
    @field:NotEmpty val priorityOrder: List<AllocationCategory>,
)
