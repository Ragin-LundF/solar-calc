package io.github.raginlundf.solarcalc.dtos.allocation

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationCategoryEnum
import jakarta.validation.constraints.NotEmpty

data class CreateAllocationPolicyRequest(
    val name: String = "Default",
    @field:NotEmpty val priorityOrder: List<AllocationCategoryEnum>,
)
