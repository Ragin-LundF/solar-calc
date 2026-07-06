package io.github.raginlundf.solarcalc.domain.models.repository

import io.github.raginlundf.solarcalc.domain.models.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import java.util.Optional

interface UserRepository : JpaRepository<User, Long>, QuerydslPredicateExecutor<User> {
    fun findByUsername(username: String): Optional<User>
    fun existsByUsername(username: String): Boolean
}
