package io.github.raginlundf.solarcalc.domain.models.repository

import io.github.raginlundf.solarcalc.domain.models.input.MonthlyEnergyInput
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface MonthlyEnergyInputRepository :
    JpaRepository<MonthlyEnergyInput, Long>,
    QuerydslPredicateExecutor<MonthlyEnergyInput> {

    fun findAllByTenantIdAndEnergyProfileId(tenantId: Long, energyProfileId: Long): List<MonthlyEnergyInput>

    fun findByTenantIdAndEnergyProfileIdAndPeriod(
        tenantId: Long,
        energyProfileId: Long,
        period: String,
    ): MonthlyEnergyInput?

    fun findByIdAndTenantId(id: Long, tenantId: Long): MonthlyEnergyInput?
}
