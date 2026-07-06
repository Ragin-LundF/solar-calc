package io.github.raginlundf.solarcalc.domain.models.repository

import io.github.raginlundf.solarcalc.domain.models.calculation.CalculationRun
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface CalculationRunRepository : JpaRepository<CalculationRun, Long>, QuerydslPredicateExecutor<CalculationRun> {

    fun findAllByTenantIdAndEnergyProfileIdAndPeriod(
        tenantId: Long,
        energyProfileId: Long,
        period: String,
    ): List<CalculationRun>

    fun findAllByTenantIdAndEnergyProfileId(tenantId: Long, energyProfileId: Long): List<CalculationRun>

    fun findByIdAndTenantId(id: Long, tenantId: Long): CalculationRun?
}
