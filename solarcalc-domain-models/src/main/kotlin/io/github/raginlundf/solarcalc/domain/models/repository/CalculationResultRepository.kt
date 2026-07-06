package io.github.raginlundf.solarcalc.domain.models.repository

import io.github.raginlundf.solarcalc.domain.models.calculation.CalculationResult
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface CalculationResultRepository :
    JpaRepository<CalculationResult, Long>,
    QuerydslPredicateExecutor<CalculationResult> {

    fun findAllByCalculationRunIdAndTenantId(calculationRunId: Long, tenantId: Long): List<CalculationResult>

    fun findByCalculationRunIdAndTenantId(calculationRunId: Long, tenantId: Long): CalculationResult?
}
