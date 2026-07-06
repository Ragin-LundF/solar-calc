package io.github.raginlundf.solarcalc.domain.models.repository

import io.github.raginlundf.solarcalc.domain.models.calculation.CalculationRun
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface CalculationRunRepository : JpaRepository<CalculationRun, Long>, QuerydslPredicateExecutor<CalculationRun> {

    fun findAllByEnergyProfileIdAndPeriod(
        energyProfileId: Long,
        period: String,
    ): List<CalculationRun>

    fun findAllByEnergyProfileId(energyProfileId: Long): List<CalculationRun>
}
