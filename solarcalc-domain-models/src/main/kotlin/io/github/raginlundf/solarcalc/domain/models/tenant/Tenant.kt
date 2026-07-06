package io.github.raginlundf.solarcalc.domain.models.tenant

import io.github.raginlundf.extensions.kotlinEquals
import io.github.raginlundf.extensions.kotlinHashCode
import io.github.raginlundf.extensions.kotlinToString
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "tenant")
class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var name: String = ""

    @Column(nullable = false, length = 10)
    var locale: String = "de"

    @Column(name = "provider_label")
    var providerLabel: String? = null

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

    @Column(name = "default_ev_efficiency_kwh_100km", precision = 8, scale = 2)
    var defaultEvEfficiencyKwh100km: BigDecimal? = null

    @Column(name = "default_ice_efficiency_l_100km", precision = 8, scale = 2)
    var defaultIceEfficiencyL100km: BigDecimal? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    override fun equals(other: Any?): Boolean {
        return kotlinEquals(other, arrayOf(Tenant::id))
    }

    override fun hashCode(): Int {
        return kotlinHashCode(arrayOf(Tenant::id))
    }

    override fun toString(): String {
        return kotlinToString(arrayOf(Tenant::id, Tenant::name))
    }
}
