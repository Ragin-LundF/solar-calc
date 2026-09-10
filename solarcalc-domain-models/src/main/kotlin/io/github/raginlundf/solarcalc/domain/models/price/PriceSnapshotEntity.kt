package io.github.raginlundf.solarcalc.domain.models.price

import io.github.raginlundf.extensions.kotlinEquals
import io.github.raginlundf.extensions.kotlinHashCode
import io.github.raginlundf.extensions.kotlinToString
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfileEntity
import io.github.raginlundf.solarcalc.domain.models.profile.HeatingReferenceTypeEnum
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.reflect.KProperty1

@Entity
@Table(name = "price_snapshot")
class PriceSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "energy_profile_id", nullable = false)
    var energyProfile: EnergyProfileEntity? = null

    @Column(name = "energy_profile_id", insertable = false, updatable = false)
    var energyProfileId: Long? = null

    /**
     * First month these prices apply to (format: YYYY-MM). They stay in effect until a snapshot
     * with a later [validFrom] supersedes them, so entries form a timeline and cannot overlap.
     */
    @Column(name = "valid_from", nullable = false, length = 7)
    var validFrom: String = ""

    @Column(name = "electricity_price", precision = 12, scale = 6)
    var electricityPrice: BigDecimal? = null

    @Column(name = "feed_in_tariff", precision = 12, scale = 6)
    var feedInTariff: BigDecimal? = null

    @Column(name = "petrol_price", precision = 12, scale = 6)
    var petrolPrice: BigDecimal? = null

    /**
     * Which fuel the reference cost stands for from [validFrom] onwards. Null means unchanged, so
     * the fuel carries over from an earlier entry, or from the profile when none has set one.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "heating_reference_type", length = 10)
    var heatingReferenceType: HeatingReferenceTypeEnum? = null

    @Column(name = "oil_reference_cost", precision = 12, scale = 2)
    var oilReferenceCost: BigDecimal? = null

    @Column(name = "gas_reference_cost", precision = 12, scale = 2)
    var gasReferenceCost: BigDecimal? = null

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
        val properties: Array<KProperty1<PriceSnapshotEntity, Any?>> = arrayOf(
            PriceSnapshotEntity::id,
            PriceSnapshotEntity::energyProfileId,
            PriceSnapshotEntity::validFrom,
            PriceSnapshotEntity::electricityPrice,
            PriceSnapshotEntity::feedInTariff,
            PriceSnapshotEntity::petrolPrice,
            PriceSnapshotEntity::heatingReferenceType,
            PriceSnapshotEntity::oilReferenceCost,
            PriceSnapshotEntity::gasReferenceCost,
            PriceSnapshotEntity::createdAt,
            PriceSnapshotEntity::updatedAt,
        )
    }
}
