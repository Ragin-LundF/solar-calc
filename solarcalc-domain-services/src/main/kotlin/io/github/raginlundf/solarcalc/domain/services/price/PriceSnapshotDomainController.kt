package io.github.raginlundf.solarcalc.domain.services.price

import io.github.raginlundf.solarcalc.dtos.price.PriceSnapshotResponse
import io.github.raginlundf.solarcalc.dtos.price.UpsertPriceSnapshotRequest

interface PriceSnapshotDomainController {
    fun list(profileUuid: String): List<PriceSnapshotResponse>
    fun get(profileUuid: String, priceId: Long): PriceSnapshotResponse
    fun create(profileUuid: String, request: UpsertPriceSnapshotRequest): PriceSnapshotResponse
    fun update(profileUuid: String, priceId: Long, request: UpsertPriceSnapshotRequest): PriceSnapshotResponse
    fun delete(profileUuid: String, priceId: Long)
}
