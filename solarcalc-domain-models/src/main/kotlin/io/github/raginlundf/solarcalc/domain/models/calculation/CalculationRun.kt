package io.github.raginlundf.solarcalc.domain.models.calculation

import io.github.raginlundf.extensions.kotlinEquals
import io.github.raginlundf.extensions.kotlinHashCode
import io.github.raginlundf.extensions.kotlinToString
import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationPolicy
import io.github.raginlundf.solarcalc.domain.models.input.MonthlyEnergyInput
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
import java.time.LocalDateTime

/** Immutable record of a single calculation execution. */
@Entity
@Table(name = "calculation_run")
class CalculationRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "energy_profile_id", nullable = false)
    var energyProfile: EnergyProfile? = null

    @Column(name = "energy_profile_id", insertable = false, updatable = false)
    var energyProfileId: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monthly_input_id", nullable = false)
    var monthlyInput: MonthlyEnergyInput? = null

    @Column(name = "monthly_input_id", insertable = false, updatable = false)
    var monthlyInputId: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allocation_policy_id", nullable = false)
    var allocationPolicy: AllocationPolicy? = null

    @Column(name = "allocation_policy_id", insertable = false, updatable = false)
    var allocationPolicyId: Long? = null

    @Column(nullable = false, length = 7)
    var period: String = ""

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    override fun equals(other: Any?): Boolean {
        return kotlinEquals(other, arrayOf(CalculationRun::id))
    }

    override fun hashCode(): Int {
        return kotlinHashCode(arrayOf(CalculationRun::id))
    }

    override fun toString(): String {
        return kotlinToString(arrayOf(CalculationRun::id, CalculationRun::period))
    }
}
