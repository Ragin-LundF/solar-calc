package io.github.raginlundf.solarcalc.domain.models.tenant

import kotlinx.serialization.Serializable

@Serializable
enum class HeatingReferenceType {
    NONE,
    OIL,
    GAS,
}
