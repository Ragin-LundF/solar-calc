package io.github.raginlundf.solarcalc.domain.models.allocation

import kotlinx.serialization.Serializable

@Serializable
enum class AllocationCategory {
    HOUSEHOLD,
    HEAT_PUMP,
    WALLBOX,
}
