package io.github.raginlundf.solarcalc.domain.models.repository

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationPolicyEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface AllocationPolicyRepository :
    JpaRepository<AllocationPolicyEntity, Long>,
    QuerydslPredicateExecutor<AllocationPolicyEntity> {

    fun findAllByEnergyProfileId(energyProfileId: Long): List<AllocationPolicyEntity>

    fun findByIdAndEnergyProfileId(id: Long, energyProfileId: Long): AllocationPolicyEntity?
}
