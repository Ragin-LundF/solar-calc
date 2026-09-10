package io.github.raginlundf.solarcalc.domain.models.allocation

import io.github.raginlundf.extensions.kotlinEquals
import io.github.raginlundf.extensions.kotlinHashCode
import io.github.raginlundf.extensions.kotlinToString
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfileEntity
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
import kotlin.reflect.KProperty1

@Entity
@Table(name = "allocation_policy")
class AllocationPolicyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "energy_profile_id", nullable = false)
    var energyProfile: EnergyProfileEntity? = null

    @Column(name = "energy_profile_id", insertable = false, updatable = false)
    var energyProfileId: Long? = null

    @Column(nullable = false)
    var name: String = ""

    @Convert(converter = AllocationCategoryListConverter::class)
    @Column(name = "priority_order", nullable = false, length = 100)
    var priorityOrder: List<AllocationCategoryEnum> = emptyList()

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

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
         * three can never disagree. Excludes the lazy [energyProfile] association, which comparing would
         * force to load; [energyProfileId] identifies the owner without it.
         */
        val properties: Array<KProperty1<AllocationPolicyEntity, Any?>> = arrayOf(
            AllocationPolicyEntity::id,
            AllocationPolicyEntity::energyProfileId,
            AllocationPolicyEntity::name,
            AllocationPolicyEntity::priorityOrder,
            AllocationPolicyEntity::createdAt,
            AllocationPolicyEntity::updatedAt,
        )
    }
}
