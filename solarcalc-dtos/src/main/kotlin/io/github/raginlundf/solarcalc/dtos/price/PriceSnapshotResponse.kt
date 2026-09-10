package io.github.raginlundf.solarcalc.dtos.price

import io.github.raginlundf.solarcalc.domain.models.profile.HeatingReferenceTypeEnum
import java.math.BigDecimal

data class PriceSnapshotResponse(
    val id: Long,
    val energyProfileUuid: String,
    /** First month these prices apply to (YYYY-MM). */
    val validFrom: String,
    val electricityPrice: BigDecimal?,
    val feedInTariff: BigDecimal?,
    val petrolPrice: BigDecimal?,
    val heatingReferenceType: HeatingReferenceTypeEnum?,
    val oilReferenceCost: BigDecimal?,
    val gasReferenceCost: BigDecimal?,
)
