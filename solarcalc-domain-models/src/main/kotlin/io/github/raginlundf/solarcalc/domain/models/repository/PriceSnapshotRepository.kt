package io.github.raginlundf.solarcalc.domain.models.repository

import io.github.raginlundf.solarcalc.domain.models.price.PriceSnapshotEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface PriceSnapshotRepository :
    JpaRepository<PriceSnapshotEntity, Long>,
    QuerydslPredicateExecutor<PriceSnapshotEntity> {

    /** The profile's whole price timeline, newest start month first. */
    fun findAllByEnergyProfileIdOrderByValidFromDesc(energyProfileId: Long): List<PriceSnapshotEntity>

    fun findByIdAndEnergyProfileId(id: Long, energyProfileId: Long): PriceSnapshotEntity?

    fun findByEnergyProfileIdAndValidFrom(
        energyProfileId: Long,
        validFrom: String,
    ): PriceSnapshotEntity?
}
