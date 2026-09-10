package io.github.raginlundf.solarcalc.domain.models.repository

import io.github.raginlundf.solarcalc.domain.models.input.MonthlyEnergyInputEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface MonthlyEnergyInputRepository :
    JpaRepository<MonthlyEnergyInputEntity, Long>,
    QuerydslPredicateExecutor<MonthlyEnergyInputEntity> {

    fun findAllByEnergyProfileId(energyProfileId: Long): List<MonthlyEnergyInputEntity>

    fun findByUuidAndEnergyProfileId(uuid: String, energyProfileId: Long): MonthlyEnergyInputEntity?

    fun findByIdAndEnergyProfileId(id: Long, energyProfileId: Long): MonthlyEnergyInputEntity?

    fun findByEnergyProfileIdAndPeriod(
        energyProfileId: Long,
        period: String,
    ): MonthlyEnergyInputEntity?
}
