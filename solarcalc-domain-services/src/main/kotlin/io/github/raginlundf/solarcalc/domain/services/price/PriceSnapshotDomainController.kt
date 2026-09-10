package io.github.raginlundf.solarcalc.domain.services.price

import io.github.raginlundf.solarcalc.dtos.price.EffectivePricesResponse
import io.github.raginlundf.solarcalc.dtos.price.PriceSnapshotResponse
import io.github.raginlundf.solarcalc.dtos.price.UpsertPriceSnapshotRequest

interface PriceSnapshotDomainController {
    fun list(profileUuid: String, username: String): List<PriceSnapshotResponse>
    fun get(profileUuid: String, priceId: Long, username: String): PriceSnapshotResponse
    fun create(profileUuid: String, request: UpsertPriceSnapshotRequest, username: String): PriceSnapshotResponse
    fun update(
        profileUuid: String,
        priceId: Long,
        request: UpsertPriceSnapshotRequest,
        username: String,
    ): PriceSnapshotResponse
    fun delete(profileUuid: String, priceId: Long, username: String)

    /** Prices in effect for [period] (YYYY-MM) after the timeline is applied to the profile defaults. */
    fun effectivePrices(profileUuid: String, period: String, username: String): EffectivePricesResponse
}
