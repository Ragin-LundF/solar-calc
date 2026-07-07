@file:UseSerializers(BigDecimalSerializer::class)

package io.github.raginlundf.solarcalc.dtos.price

import io.github.raginlundf.solarcalc.dtos.serialization.BigDecimalSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.math.BigDecimal

@Serializable
data class PriceSnapshotResponse(
    val id: Long,
    val energyProfileUuid: String,
    val period: String?,
    val electricityPrice: BigDecimal?,
    val feedInTariff: BigDecimal?,
    val petrolPrice: BigDecimal?,
    val oilReferenceCost: BigDecimal?,
    val gasReferenceCost: BigDecimal?,
)
