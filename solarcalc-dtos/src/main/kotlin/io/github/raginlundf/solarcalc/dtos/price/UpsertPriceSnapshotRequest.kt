package io.github.raginlundf.solarcalc.dtos.price

import io.github.raginlundf.solarcalc.domain.models.profile.HeatingReferenceTypeEnum
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal

data class UpsertPriceSnapshotRequest(
    /** First month these prices apply to; they stay in effect until a later entry supersedes them. */
    @field:NotBlank
    @field:Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "validFrom must be in YYYY-MM format")
    val validFrom: String,

    @field:PositiveOrZero val electricityPrice: BigDecimal? = null,
    @field:PositiveOrZero val feedInTariff: BigDecimal? = null,
    @field:PositiveOrZero val petrolPrice: BigDecimal? = null,
    /** The fuel the reference cost stands for from this month on; null leaves it unchanged. */
    val heatingReferenceType: HeatingReferenceTypeEnum? = null,
    @field:PositiveOrZero val oilReferenceCost: BigDecimal? = null,
    @field:PositiveOrZero val gasReferenceCost: BigDecimal? = null,
)
