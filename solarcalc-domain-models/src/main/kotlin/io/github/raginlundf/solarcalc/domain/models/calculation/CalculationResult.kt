package io.github.raginlundf.solarcalc.domain.models.calculation

import io.github.raginlundf.extensions.kotlinEquals
import io.github.raginlundf.extensions.kotlinHashCode
import io.github.raginlundf.extensions.kotlinToString
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
import java.math.BigDecimal
import java.time.LocalDateTime

/** Immutable persisted calculation output. */
@Entity
@Table(name = "calculation_result")
class CalculationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculation_run_id", nullable = false)
    var calculationRun: CalculationRun? = null

    @Column(name = "calculation_run_id", insertable = false, updatable = false)
    var calculationRunId: Long? = null

    @Column(name = "feed_in_kwh", nullable = false, precision = 12, scale = 3)
    var feedInKwh: BigDecimal = BigDecimal.ZERO

    @Column(name = "feed_in_revenue", precision = 12, scale = 2)
    var feedInRevenue: BigDecimal? = null

    @Column(name = "self_consumption_pool_kwh", nullable = false, precision = 12, scale = 3)
    var selfConsumptionPoolKwh: BigDecimal = BigDecimal.ZERO

    @Column(name = "household_allocated_kwh", precision = 12, scale = 3)
    var householdAllocatedKwh: BigDecimal? = null

    @Column(name = "household_grid_kwh", precision = 12, scale = 3)
    var householdGridKwh: BigDecimal? = null

    @Column(name = "household_savings", precision = 12, scale = 2)
    var householdSavings: BigDecimal? = null

    @Column(name = "heat_pump_allocated_kwh", precision = 12, scale = 3)
    var heatPumpAllocatedKwh: BigDecimal? = null

    @Column(name = "heat_pump_grid_kwh", precision = 12, scale = 3)
    var heatPumpGridKwh: BigDecimal? = null

    @Column(name = "heat_pump_electricity_savings", precision = 12, scale = 2)
    var heatPumpElectricitySavings: BigDecimal? = null

    @Column(name = "heat_pump_heating_reference_savings", precision = 12, scale = 2)
    var heatPumpHeatingReferenceSavings: BigDecimal? = null

    @Column(name = "wallbox_allocated_kwh", precision = 12, scale = 3)
    var wallboxAllocatedKwh: BigDecimal? = null

    @Column(name = "wallbox_grid_kwh", precision = 12, scale = 3)
    var wallboxGridKwh: BigDecimal? = null

    @Column(name = "wallbox_electricity_savings", precision = 12, scale = 2)
    var wallboxElectricitySavings: BigDecimal? = null

    @Column(name = "wallbox_petrol_savings", precision = 12, scale = 2)
    var wallboxPetrolSavings: BigDecimal? = null

    @Column(name = "unallocated_kwh", nullable = false, precision = 12, scale = 3)
    var unallocatedKwh: BigDecimal = BigDecimal.ZERO

    @Column(name = "total_electricity_savings", precision = 12, scale = 2)
    var totalElectricitySavings: BigDecimal? = null

    @Convert(converter = CompletenessFlagSetConverter::class)
    @Column(name = "completeness_flags", nullable = false, length = 500)
    var completenessFlags: Set<CompletenessFlag> = emptySet()

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    override fun equals(other: Any?): Boolean {
        return kotlinEquals(other = other, properties = arrayOf(CalculationResult::id))
    }

    override fun hashCode(): Int {
        return kotlinHashCode(properties = arrayOf(CalculationResult::id))
    }

    override fun toString(): String {
        return kotlinToString(
            properties = arrayOf(
                CalculationResult::id,
                CalculationResult::calculationRunId,
            )
        )
    }
}
