@file:UseSerializers(BigDecimalSerializer::class)

package io.github.raginlundf.solarcalc.restapi.price

import io.github.raginlundf.solarcalc.restapi.serialization.BigDecimalSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.math.BigDecimal

@Serializable
data class PriceSnapshotResponse(
    val id: Long,
    val tenantId: Long,
    val energyProfileId: Long,
    /** null = profile default; non-null = monthly override */
    val period: String?,
    val electricityPrice: BigDecimal?,
    val feedInTariff: BigDecimal?,
    val petrolPrice: BigDecimal?,
    val oilReferenceCost: BigDecimal?,
    val gasReferenceCost: BigDecimal?,
    val evEfficiencyKwh100km: BigDecimal?,
    val iceEfficiencyL100km: BigDecimal?,
)

@Serializable
data class UpsertPriceSnapshotRequest(
    val period: String? = null,
    val electricityPrice: BigDecimal? = null,
    val feedInTariff: BigDecimal? = null,
    val petrolPrice: BigDecimal? = null,
    val oilReferenceCost: BigDecimal? = null,
    val gasReferenceCost: BigDecimal? = null,
    val evEfficiencyKwh100km: BigDecimal? = null,
    val iceEfficiencyL100km: BigDecimal? = null,
)
