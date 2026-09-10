package io.github.raginlundf.solarcalc.dtos.allocation

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationCategoryEnum

data class AllocationPolicyResponse(
    val id: Long,
    val energyProfileUuid: String,
    val name: String,
    val priorityOrder: List<AllocationCategoryEnum>,
)
