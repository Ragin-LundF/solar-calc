package io.github.raginlundf.solarcalc.domain.models.user

import io.github.raginlundf.extensions.kotlinEquals
import io.github.raginlundf.extensions.kotlinHashCode
import io.github.raginlundf.extensions.kotlinToString
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "solarcalc_user")
class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, unique = true, length = 100)
    var username: String = ""

    @Column(name = "password_hash", nullable = false, length = 255)
    var passwordHash: String = ""

    @Column(name = "tenant_id", nullable = false)
    var tenantId: Long = 0

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    override fun equals(other: Any?): Boolean {
        return kotlinEquals(other, arrayOf(User::id))
    }

    override fun hashCode(): Int {
        return kotlinHashCode(arrayOf(User::id))
    }

    override fun toString(): String {
        return kotlinToString(arrayOf(User::id, User::username))
    }
}
