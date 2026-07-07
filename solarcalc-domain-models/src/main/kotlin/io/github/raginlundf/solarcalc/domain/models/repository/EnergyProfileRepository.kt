package io.github.raginlundf.solarcalc.domain.models.repository

import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface EnergyProfileRepository : JpaRepository<EnergyProfile, Long>, QuerydslPredicateExecutor<EnergyProfile> {

    fun findByUuid(uuid: String): EnergyProfile?

    fun findAllByUserUsername(userUsername: String): List<EnergyProfile>

    fun findByUuidAndUserUsername(uuid: String, userUsername: String): EnergyProfile?
}
