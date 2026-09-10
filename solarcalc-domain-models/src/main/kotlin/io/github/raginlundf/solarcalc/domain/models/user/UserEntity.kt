package io.github.raginlundf.solarcalc.domain.models.user

import io.github.raginlundf.extensions.kotlinEquals
import io.github.raginlundf.extensions.kotlinHashCode
import io.github.raginlundf.extensions.kotlinToString
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfileEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime
import kotlin.reflect.KProperty1

@Entity
@Table(name = "solarcalc_user")
class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, unique = true, length = 100)
    var username: String = ""

    @Column(name = "password_hash", nullable = false, length = 255)
    var passwordHash: String = ""

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var profiles: MutableList<EnergyProfileEntity> = mutableListOf()

    @Column(name = "setup_step", nullable = false)
    var setupStep: Int = 0

    @Column(name = "last_profile_uuid", length = 36)
    var lastProfileUuid: String? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    override fun equals(other: Any?): Boolean {
        return kotlinEquals(other, properties)
    }

    override fun hashCode(): Int {
        return kotlinHashCode(properties)
    }

    override fun toString(): String {
        return kotlinToString(properties)
    }

    private companion object {
        /**
         * The fields that make up this entity's identity, shared by equals, hashCode and toString so the
         * three can never disagree. Excludes [profiles], a lazy collection that comparing or printing
         * would force to load, and [passwordHash], which must never reach a log line.
         */
        val properties: Array<KProperty1<UserEntity, Any?>> = arrayOf(
            UserEntity::id,
            UserEntity::username,
            UserEntity::setupStep,
            UserEntity::lastProfileUuid,
            UserEntity::createdAt,
        )
    }
}
