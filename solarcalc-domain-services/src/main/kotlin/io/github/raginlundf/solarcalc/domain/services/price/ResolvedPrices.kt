package io.github.raginlundf.solarcalc.domain.services.price

import io.github.raginlundf.solarcalc.domain.models.profile.HeatingReferenceTypeEnum
import java.math.BigDecimal

/** The prices in effect for one month, after the timeline has been applied to the profile defaults. */
data class ResolvedPrices(
    val electricityPrice: BigDecimal?,
    val feedInTariff: BigDecimal?,
    val petrolPrice: BigDecimal?,
    /** The fuel the heating reference stood for that month. */
    val heatingReferenceType: HeatingReferenceTypeEnum,
    /** Annual reference cost for [heatingReferenceType]; null when that type is NONE. */
    val heatingReferenceCost: BigDecimal?,
)
