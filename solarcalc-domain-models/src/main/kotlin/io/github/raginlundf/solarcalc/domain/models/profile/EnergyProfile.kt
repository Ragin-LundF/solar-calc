package io.github.raginlundf.solarcalc.domain.models.profile

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
@Table(name = "energy_profile")
class EnergyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var name: String = ""

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    override fun equals(other: Any?): Boolean {
        return kotlinEquals(other, arrayOf(EnergyProfile::id))
    }

    override fun hashCode(): Int {
        return kotlinHashCode(arrayOf(EnergyProfile::id))
    }

    override fun toString(): String {
        return kotlinToString(arrayOf(EnergyProfile::id, EnergyProfile::name))
    }
}
