package io.github.raginlundf.solarcalc.domain.models.repository

import io.github.raginlundf.solarcalc.domain.models.price.PriceSnapshot
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface PriceSnapshotRepository : JpaRepository<PriceSnapshot, Long>, QuerydslPredicateExecutor<PriceSnapshot> {

    /** Returns default (period=null) and all monthly overrides for the given profile. */
    fun findAllByEnergyProfileId(energyProfileId: Long): List<PriceSnapshot>

    fun findByEnergyProfileIdAndPeriodIsNull(energyProfileId: Long): PriceSnapshot?

    fun findByEnergyProfileIdAndPeriod(
        energyProfileId: Long,
        period: String,
    ): PriceSnapshot?
}
