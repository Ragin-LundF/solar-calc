package io.github.raginlundf.solarcalc.domain.models.repository

import io.github.raginlundf.solarcalc.domain.models.calculation.CalculationResult
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface CalculationResultRepository :
    JpaRepository<CalculationResult, Long>,
    QuerydslPredicateExecutor<CalculationResult> {

    fun findAllByCalculationRunId(calculationRunId: Long): List<CalculationResult>

    fun findByCalculationRunId(calculationRunId: Long): CalculationResult?
}
