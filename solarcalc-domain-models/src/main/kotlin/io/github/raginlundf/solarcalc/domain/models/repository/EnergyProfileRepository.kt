package io.github.raginlundf.solarcalc.domain.models.repository

import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfileEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface EnergyProfileRepository :
    JpaRepository<EnergyProfileEntity, Long>,
    QuerydslPredicateExecutor<EnergyProfileEntity> {

    fun findByUuid(uuid: String): EnergyProfileEntity?

    fun findAllByUserUsername(userUsername: String): List<EnergyProfileEntity>

    fun findByUuidAndUserUsername(uuid: String, userUsername: String): EnergyProfileEntity?
}
