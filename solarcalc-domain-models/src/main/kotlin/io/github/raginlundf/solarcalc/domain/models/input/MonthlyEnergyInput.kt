package io.github.raginlundf.solarcalc.domain.models.input

import io.github.raginlundf.extensions.kotlinEquals
import io.github.raginlundf.extensions.kotlinHashCode
import io.github.raginlundf.extensions.kotlinToString
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfile
import io.github.raginlundf.solarcalc.domain.models.tenant.Tenant
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
@Table(name = "monthly_energy_input")
class MonthlyEnergyInput {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    var tenant: Tenant? = null

    @Column(name = "tenant_id", insertable = false, updatable = false)
    var tenantId: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "energy_profile_id", nullable = false)
    var energyProfile: EnergyProfile? = null

    @Column(name = "energy_profile_id", insertable = false, updatable = false)
    var energyProfileId: Long? = null

    /** Format: YYYY-MM (e.g. "2024-03") */
    @Column(nullable = false, length = 7)
    var period: String = ""

    /** Total site electricity consumption, or household-only if entered directly. */
    @Column(name = "consumption_kwh", nullable = false, precision = 12, scale = 3)
    var consumptionKwh: BigDecimal = BigDecimal.ZERO

    @Column(name = "generation_kwh", nullable = false, precision = 12, scale = 3)
    var generationKwh: BigDecimal = BigDecimal.ZERO

    @Column(name = "feed_in_kwh", precision = 12, scale = 3)
    var feedInKwh: BigDecimal? = null

    @Column(name = "household_consumption_kwh", precision = 12, scale = 3)
    var householdConsumptionKwh: BigDecimal? = null

    @Column(name = "heat_pump_consumption_kwh", precision = 12, scale = 3)
    var heatPumpConsumptionKwh: BigDecimal? = null

    @Column(name = "wallbox_consumption_kwh", precision = 12, scale = 3)
    var wallboxConsumptionKwh: BigDecimal? = null

    @Column(name = "electricity_price_override", precision = 12, scale = 6)
    var electricityPriceOverride: BigDecimal? = null

    @Column(name = "feed_in_tariff_override", precision = 12, scale = 6)
    var feedInTariffOverride: BigDecimal? = null

    @Column(name = "petrol_price_override", precision = 12, scale = 6)
    var petrolPriceOverride: BigDecimal? = null

    @Column(name = "ev_efficiency_override_kwh_100km", precision = 8, scale = 2)
    var evEfficiencyOverrideKwh100km: BigDecimal? = null

    @Column(name = "ice_efficiency_override_l_100km", precision = 8, scale = 2)
    var iceEfficiencyOverrideL100km: BigDecimal? = null

    @Column(name = "heating_reference_cost_override", precision = 12, scale = 2)
    var heatingReferenceCostOverride: BigDecimal? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    override fun equals(other: Any?): Boolean {
        return kotlinEquals(other, arrayOf(MonthlyEnergyInput::id))
    }

    override fun hashCode(): Int {
        return kotlinHashCode(arrayOf(MonthlyEnergyInput::id))
    }

    override fun toString(): String {
        return kotlinToString(arrayOf(MonthlyEnergyInput::id, MonthlyEnergyInput::tenantId, MonthlyEnergyInput::period))
    }
}
