@file:UseSerializers(BigDecimalSerializer::class)

package io.github.raginlundf.solarcalc.dtos.price

import io.github.raginlundf.solarcalc.dtos.serialization.BigDecimalSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.math.BigDecimal

@Serializable
data class UpsertPriceSnapshotRequest(
    val period: String? = null,
    val electricityPrice: BigDecimal? = null,
    val feedInTariff: BigDecimal? = null,
    val petrolPrice: BigDecimal? = null,
    val oilReferenceCost: BigDecimal? = null,
    val gasReferenceCost: BigDecimal? = null,
)
