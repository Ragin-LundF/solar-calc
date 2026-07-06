package io.github.raginlundf.solarcalc.domain.services.price

import io.github.raginlundf.solarcalc.dtos.price.PriceSnapshotResponse
import io.github.raginlundf.solarcalc.dtos.price.UpsertPriceSnapshotRequest

interface PriceSnapshotDomainController {
    fun list(profileId: Long): List<PriceSnapshotResponse>
    fun get(profileId: Long, priceId: Long): PriceSnapshotResponse
    fun create(profileId: Long, request: UpsertPriceSnapshotRequest): PriceSnapshotResponse
    fun update(profileId: Long, priceId: Long, request: UpsertPriceSnapshotRequest): PriceSnapshotResponse
    fun delete(profileId: Long, priceId: Long)
}
