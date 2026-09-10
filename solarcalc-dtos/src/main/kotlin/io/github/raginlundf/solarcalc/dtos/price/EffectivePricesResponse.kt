package io.github.raginlundf.solarcalc.dtos.price

import io.github.raginlundf.solarcalc.domain.models.profile.HeatingReferenceTypeEnum
import java.math.BigDecimal

/**
 * The prices in effect for one month, after the timeline has been applied on top of the profile
 * defaults. Lets a client prefill an input with the value the server would actually use, instead of
 * re-implementing the resolution and risking a different answer.
 */
data class EffectivePricesResponse(
    val period: String,
    val electricityPrice: BigDecimal?,
    val feedInTariff: BigDecimal?,
    val petrolPrice: BigDecimal?,
    /** The fuel heated with that month, which may differ from the profile's current setting. */
    val heatingReferenceType: HeatingReferenceTypeEnum,
    /** Annual heating reference cost for [heatingReferenceType]; null when it is NONE. */
    val heatingReferenceCost: BigDecimal?,
)
