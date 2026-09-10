package io.github.raginlundf.solarcalc.domain.services.price

import java.math.BigDecimal

/** The prices in effect for one month, after the snapshot/profile fallback chain. */
data class ResolvedPrices(
    val electricityPrice: BigDecimal?,
    val feedInTariff: BigDecimal?,
    val petrolPrice: BigDecimal?,
    val heatingReferenceCost: BigDecimal?,
)
