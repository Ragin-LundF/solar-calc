package io.github.raginlundf.solarcalc.domain.models.profile

import io.github.raginlundf.extensions.kotlinEquals
import io.github.raginlundf.extensions.kotlinHashCode
import io.github.raginlundf.extensions.kotlinToString
import io.github.raginlundf.solarcalc.domain.models.profile.HeatingReferenceType
import io.github.raginlundf.solarcalc.domain.models.user.User
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
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "energy_profile")
class EnergyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, unique = true, updatable = false, length = 36)
    var uuid: String = ""

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null

    @Column(nullable = false)
    var name: String = ""

    @Column(name = "has_wallbox", nullable = false)
    var hasWallbox: Boolean = false

    @Column(name = "has_heat_pump", nullable = false)
    var hasHeatPump: Boolean = false

    @Enumerated(EnumType.STRING)
    @Column(name = "heating_reference_type", nullable = false, length = 10)
    var heatingReferenceType: HeatingReferenceType = HeatingReferenceType.NONE

    @Column(name = "default_electricity_price", precision = 12, scale = 6)
    var defaultElectricityPrice: BigDecimal? = null

    @Column(name = "default_feed_in_tariff", precision = 12, scale = 6)
    var defaultFeedInTariff: BigDecimal? = null

    @Column(name = "default_petrol_price", precision = 12, scale = 6)
    var defaultPetrolPrice: BigDecimal? = null

    @Column(name = "default_oil_reference_cost", precision = 12, scale = 2)
    var defaultOilReferenceCost: BigDecimal? = null

    @Column(name = "default_gas_reference_cost", precision = 12, scale = 2)
    var defaultGasReferenceCost: BigDecimal? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    @PrePersist
    private fun generateUuid() {
        if (uuid.isBlank()) {
            uuid = UUID.randomUUID().toString()
        }
    }

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
