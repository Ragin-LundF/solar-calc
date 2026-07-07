package io.github.raginlundf.solarcalc.dtos.allocation

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationCategory
import jakarta.validation.constraints.NotEmpty
import kotlinx.serialization.Serializable

@Serializable
data class CreateAllocationPolicyRequest(
    val name: String = "Default",
    @field:NotEmpty val priorityOrder: List<AllocationCategory>,
)
