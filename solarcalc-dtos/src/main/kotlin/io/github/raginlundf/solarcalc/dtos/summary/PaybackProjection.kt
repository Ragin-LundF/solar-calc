package io.github.raginlundf.solarcalc.dtos.summary

import java.math.BigDecimal

/** Cumulative payback, always computed over all history (independent of the range filter). */
data class PaybackProjection(
    val cumulativeSavings: BigDecimal,
    val investKosten: BigDecimal,
    val paybackPct: BigDecimal,
    val amortised: Boolean,
    /** Projected completion month (YYYY-MM), or null if amortised / not projectable. */
    val projectedPaybackPeriod: String?,
)
