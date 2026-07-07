package io.github.raginlundf.solarcalc.domain.models.repository

import io.github.raginlundf.solarcalc.domain.models.input.MonthlyEnergyInput
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface MonthlyEnergyInputRepository :
    JpaRepository<MonthlyEnergyInput, Long>,
    QuerydslPredicateExecutor<MonthlyEnergyInput> {

    fun findAllByEnergyProfileId(energyProfileId: Long): List<MonthlyEnergyInput>

    fun findByUuidAndEnergyProfileId(uuid: String, energyProfileId: Long): MonthlyEnergyInput?

    fun findByIdAndEnergyProfileId(id: Long, energyProfileId: Long): MonthlyEnergyInput?

    fun findByEnergyProfileIdAndPeriod(
        energyProfileId: Long,
        period: String,
    ): MonthlyEnergyInput?
}
