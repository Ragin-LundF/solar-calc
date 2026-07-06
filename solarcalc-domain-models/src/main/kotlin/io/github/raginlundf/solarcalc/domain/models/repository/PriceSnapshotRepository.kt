package io.github.raginlundf.solarcalc.domain.models.repository

import io.github.raginlundf.solarcalc.domain.models.price.PriceSnapshot
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface PriceSnapshotRepository : JpaRepository<PriceSnapshot, Long>, QuerydslPredicateExecutor<PriceSnapshot> {

    /** Returns default (period=null) and all monthly overrides for the given profile. */
    fun findAllByTenantIdAndEnergyProfileId(tenantId: Long, energyProfileId: Long): List<PriceSnapshot>

    fun findByTenantIdAndEnergyProfileIdAndPeriodIsNull(tenantId: Long, energyProfileId: Long): PriceSnapshot?

    fun findByTenantIdAndEnergyProfileIdAndPeriod(
        tenantId: Long,
        energyProfileId: Long,
        period: String,
    ): PriceSnapshot?

    fun findByIdAndTenantId(id: Long, tenantId: Long): PriceSnapshot?
}
