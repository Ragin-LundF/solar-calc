package io.github.raginlundf.solarcalc.domain.models.profile

import io.github.raginlundf.extensions.kotlinEquals
import io.github.raginlundf.extensions.kotlinHashCode
import io.github.raginlundf.extensions.kotlinToString
import io.github.raginlundf.solarcalc.domain.models.user.UserEntity
import jakarta.persistence.Column
import jakarta.persistence.Convert
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
import kotlin.reflect.KProperty1

/** Seasonal heating-demand curve (Jan..Dec), reused every calendar year. Sums to 100. */
val DEFAULT_HEATING_DISTRIBUTION: List<Int> = listOf(22, 18, 12, 5, 3, 1, 1, 1, 2, 3, 13, 19)

@Entity
@Table(name = "energy_profile")
class EnergyProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, unique = true, updatable = false, length = 36)
    var uuid: String = ""

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity? = null

    @Column(nullable = false)
    var name: String = ""

    @Column(name = "has_wallbox", nullable = false)
    var hasWallbox: Boolean = false

    @Column(name = "has_heat_pump", nullable = false)
    var hasHeatPump: Boolean = false

    @Enumerated(EnumType.STRING)
    @Column(name = "heating_reference_type", nullable = false, length = 10)
    var heatingReferenceType: HeatingReferenceTypeEnum = HeatingReferenceTypeEnum.NONE

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

    /** EV efficiency in km per kWh, used for the wallbox gasoline comparison. */
    @Column(name = "km_per_kwh", precision = 8, scale = 3)
    var kmPerKwh: BigDecimal? = null

    /** Reference car consumption in liters per 100 km. */
    @Column(name = "liters_per_100km", precision = 8, scale = 3)
    var litersPer100km: BigDecimal? = null

    /** Total system investment cost, used for payback/amortization tracking. */
    @Column(name = "invest_cost", precision = 12, scale = 2)
    var investCost: BigDecimal? = null

    /** Usable living area (Wohnfläche) in m², used for the rough energy efficiency class. */
    @Column(name = "usable_area_sqm", precision = 8, scale = 2)
    var usableAreaSqm: BigDecimal? = null

    /**
     * Heat-pump seasonal performance factor (JAZ/SCOP). Converts metered heat-pump
     * electricity into delivered heat for the energy efficiency estimate.
     */
    @Column(name = "heat_pump_scop", precision = 4, scale = 2)
    var heatPumpScop: BigDecimal? = null

    /** 12 percentages (Jan..Dec) of the annual heating cost, must sum to 100. */
    @Convert(converter = IntListConverter::class)
    @Column(name = "heating_monthly_distribution", length = 100, nullable = false)
    var heatingMonthlyDistribution: List<Int> = DEFAULT_HEATING_DISTRIBUTION

    @Enumerated(EnumType.STRING)
    @Column(name = "overview_layout", nullable = false, length = 10)
    var overviewLayout: OverviewLayoutEnum = OverviewLayoutEnum.KPI

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
         * three can never disagree. Excludes the lazy [user] association, which comparing or printing
         * would force to load.
         */
        val properties: Array<KProperty1<EnergyProfileEntity, Any?>> = arrayOf(
            EnergyProfileEntity::id,
            EnergyProfileEntity::uuid,
            EnergyProfileEntity::name,
            EnergyProfileEntity::hasWallbox,
            EnergyProfileEntity::hasHeatPump,
            EnergyProfileEntity::heatingReferenceType,
            EnergyProfileEntity::defaultElectricityPrice,
            EnergyProfileEntity::defaultFeedInTariff,
            EnergyProfileEntity::defaultPetrolPrice,
            EnergyProfileEntity::defaultOilReferenceCost,
            EnergyProfileEntity::defaultGasReferenceCost,
            EnergyProfileEntity::kmPerKwh,
            EnergyProfileEntity::litersPer100km,
            EnergyProfileEntity::investCost,
            EnergyProfileEntity::usableAreaSqm,
            EnergyProfileEntity::heatPumpScop,
            EnergyProfileEntity::heatingMonthlyDistribution,
            EnergyProfileEntity::overviewLayout,
            EnergyProfileEntity::createdAt,
            EnergyProfileEntity::updatedAt,
        )
    }
}
