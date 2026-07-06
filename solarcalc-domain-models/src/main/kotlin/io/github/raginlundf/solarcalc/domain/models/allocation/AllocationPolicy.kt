package io.github.raginlundf.solarcalc.domain.models.allocation

import io.github.raginlundf.extensions.kotlinEquals
import io.github.raginlundf.extensions.kotlinHashCode
import io.github.raginlundf.extensions.kotlinToString
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfile
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "allocation_policy")
class AllocationPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "energy_profile_id", nullable = false)
    var energyProfile: EnergyProfile? = null

    @Column(name = "energy_profile_id", insertable = false, updatable = false)
    var energyProfileId: Long? = null

    @Column(nullable = false)
    var name: String = ""

    @Convert(converter = AllocationCategoryListConverter::class)
    @Column(name = "priority_order", nullable = false, length = 100)
    var priorityOrder: List<AllocationCategory> = emptyList()

    @Column(name = "is_default", nullable = false)
    var isDefault: Boolean = false

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    override fun equals(other: Any?): Boolean {
        return kotlinEquals(other, arrayOf(AllocationPolicy::id))
    }

    override fun hashCode(): Int {
        return kotlinHashCode(arrayOf(AllocationPolicy::id))
    }

    override fun toString(): String {
        return kotlinToString(arrayOf(AllocationPolicy::id, AllocationPolicy::name))
    }
}
