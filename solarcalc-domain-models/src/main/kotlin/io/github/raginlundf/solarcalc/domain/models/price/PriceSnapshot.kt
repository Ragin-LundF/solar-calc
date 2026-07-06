package io.github.raginlundf.solarcalc.domain.models.price

import io.github.raginlundf.extensions.kotlinEquals
import io.github.raginlundf.extensions.kotlinHashCode
import io.github.raginlundf.extensions.kotlinToString
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfile
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "price_snapshot")
class PriceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "energy_profile_id", nullable = false)
    var energyProfile: EnergyProfile? = null

    @Column(name = "energy_profile_id", insertable = false, updatable = false)
    var energyProfileId: Long? = null

    /** null = profile default; non-null = monthly override (format: YYYY-MM) */
    @Column(length = 7)
    var period: String? = null

    @Column(name = "electricity_price", precision = 12, scale = 6)
    var electricityPrice: BigDecimal? = null

    @Column(name = "feed_in_tariff", precision = 12, scale = 6)
    var feedInTariff: BigDecimal? = null

    @Column(name = "petrol_price", precision = 12, scale = 6)
    var petrolPrice: BigDecimal? = null

    @Column(name = "oil_reference_cost", precision = 12, scale = 2)
    var oilReferenceCost: BigDecimal? = null

    @Column(name = "gas_reference_cost", precision = 12, scale = 2)
    var gasReferenceCost: BigDecimal? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    override fun equals(other: Any?): Boolean {
        return kotlinEquals(other, arrayOf(PriceSnapshot::id))
    }

    override fun hashCode(): Int {
        return kotlinHashCode(arrayOf(PriceSnapshot::id))
    }

    override fun toString(): String {
        return kotlinToString(arrayOf(PriceSnapshot::id, PriceSnapshot::period))
    }
}
