package io.github.raginlundf.solarcalc.domain.services.summary

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationCategoryEnum
import java.math.BigDecimal

/** Profile-level settings that drive the savings math. */
data class SummaryParams(
    val allocationPriority: List<AllocationCategoryEnum>,
    val hasHeatPump: Boolean,
    val hasWallbox: Boolean,
    val kmPerKwh: BigDecimal,
    val litersPer100km: BigDecimal,
    val investKosten: BigDecimal,
    /** 12 percentages (Jan..Dec) of the annual heating cost. */
    val heatingDistribution: List<Int>,
)
