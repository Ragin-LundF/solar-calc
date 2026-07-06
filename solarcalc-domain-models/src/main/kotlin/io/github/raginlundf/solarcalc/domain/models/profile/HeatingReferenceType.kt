package io.github.raginlundf.solarcalc.domain.models.profile

import kotlinx.serialization.Serializable

@Serializable
enum class HeatingReferenceType {
    NONE,
    OIL,
    GAS,
}
