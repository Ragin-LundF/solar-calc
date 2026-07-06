package io.github.raginlundf.solarcalc.domain.models.repository

import io.github.raginlundf.solarcalc.domain.models.tenant.Tenant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface TenantRepository : JpaRepository<Tenant, Long>, QuerydslPredicateExecutor<Tenant>
