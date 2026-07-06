package io.github.raginlundf.solarcalc.domain.services.calculation

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationCategory
import java.math.BigDecimal

data class CalculationInput(
    val tenantId: Long,
    val energyProfileId: Long,
    val period: String,
    val hasWallbox: Boolean,
    val hasHeatPump: Boolean,
    /** Total site consumption or household-only, depending on input mode. */
    val consumptionKwh: BigDecimal,
    val generationKwh: BigDecimal,
    val feedInKwh: BigDecimal = BigDecimal.ZERO,
    /** Explicit household kWh; if null it is derived from consumptionKwh - heatPump - wallbox. */
    val householdConsumptionKwh: BigDecimal? = null,
    val heatPumpConsumptionKwh: BigDecimal? = null,
    val wallboxConsumptionKwh: BigDecimal? = null,
    val electricityPrice: BigDecimal? = null,
    val feedInTariff: BigDecimal? = null,
    val petrolPrice: BigDecimal? = null,
    val evEfficiencyKwh100km: BigDecimal? = null,
    val iceEfficiencyL100km: BigDecimal? = null,
    val heatingReferenceCost: BigDecimal? = null,
    val allocationPriority: List<AllocationCategory>,
)
