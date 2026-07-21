package io.github.raginlundf.solarcalc.dtos.price

import java.math.BigDecimal

data class UpsertPriceSnapshotRequest(
    val period: String? = null,
    val electricityPrice: BigDecimal? = null,
    val feedInTariff: BigDecimal? = null,
    val petrolPrice: BigDecimal? = null,
    val oilReferenceCost: BigDecimal? = null,
    val gasReferenceCost: BigDecimal? = null,
)
