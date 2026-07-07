package io.github.raginlundf.solarcalc.dtos.allocation

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationCategory
import kotlinx.serialization.Serializable

@Serializable
data class AllocationPolicyResponse(
    val id: Long,
    val energyProfileUuid: String,
    val name: String,
    val priorityOrder: List<AllocationCategory>,
)
