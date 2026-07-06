package io.github.raginlundf.solarcalc.domain.models.repository

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationPolicy
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface AllocationPolicyRepository :
    JpaRepository<AllocationPolicy, Long>,
    QuerydslPredicateExecutor<AllocationPolicy> {

    fun findAllByTenantIdAndEnergyProfileId(tenantId: Long, energyProfileId: Long): List<AllocationPolicy>

    fun findByTenantIdAndEnergyProfileIdAndIsDefaultTrue(tenantId: Long, energyProfileId: Long): AllocationPolicy?

    fun findByIdAndTenantId(id: Long, tenantId: Long): AllocationPolicy?
}
