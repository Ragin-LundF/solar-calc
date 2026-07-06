package io.github.raginlundf.solarcalc.restapi.calculation

import io.github.raginlundf.solarcalc.domain.models.calculation.CalculationResult as CalculationResultEntity
import io.github.raginlundf.solarcalc.domain.models.calculation.CalculationRun
import io.github.raginlundf.solarcalc.domain.models.repository.AllocationPolicyRepository
import io.github.raginlundf.solarcalc.domain.models.repository.CalculationResultRepository
import io.github.raginlundf.solarcalc.domain.models.repository.CalculationRunRepository
import io.github.raginlundf.solarcalc.domain.models.repository.EnergyProfileRepository
import io.github.raginlundf.solarcalc.domain.models.repository.MonthlyEnergyInputRepository
import io.github.raginlundf.solarcalc.domain.models.repository.TenantRepository
import io.github.raginlundf.solarcalc.domain.services.calculation.CalculationInput
import io.github.raginlundf.solarcalc.domain.services.calculation.CalculationResult
import io.github.raginlundf.solarcalc.domain.services.calculation.EnergyCalculationService
import io.github.raginlundf.solarcalc.domain.services.price.PriceResolver
import io.github.raginlundf.solarcalc.restapi.error.ResourceNotFoundException
import io.github.raginlundf.solarcalc.restapi.security.SolarcalcScopes
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/tenants/{tenantId}/profiles/{profileId}/calculations")
class CalculationController(
    private val tenantRepository: TenantRepository,
    private val profileRepository: EnergyProfileRepository,
    private val inputRepository: MonthlyEnergyInputRepository,
    private val policyRepository: AllocationPolicyRepository,
    private val calculationRunRepository: CalculationRunRepository,
    private val calculationResultRepository: CalculationResultRepository,
    private val calculationService: EnergyCalculationService,
    private val priceResolver: PriceResolver,
) {

    @GetMapping("/{period}")
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_CALCULATIONS_READ}')")
    @Transactional
    fun calculate(
        @PathVariable tenantId: Long,
        @PathVariable profileId: Long,
        @PathVariable period: String,
    ): CalculationResponse {
        val ctx = resolveContext(tenantId, profileId, period)
        val prices = priceResolver.resolve(ctx.tenant, tenantId, profileId, period)

        val calcInput = CalculationInput(
            tenantId = tenantId,
            energyProfileId = profileId,
            period = period,
            hasWallbox = ctx.tenant.hasWallbox,
            hasHeatPump = ctx.tenant.hasHeatPump,
            consumptionKwh = ctx.input.consumptionKwh,
            generationKwh = ctx.input.generationKwh,
            feedInKwh = ctx.input.feedInKwh ?: java.math.BigDecimal.ZERO,
            householdConsumptionKwh = ctx.input.householdConsumptionKwh,
            heatPumpConsumptionKwh = ctx.input.heatPumpConsumptionKwh,
            wallboxConsumptionKwh = ctx.input.wallboxConsumptionKwh,
            electricityPrice = ctx.input.electricityPriceOverride ?: prices.electricityPrice,
            feedInTariff = ctx.input.feedInTariffOverride ?: prices.feedInTariff,
            petrolPrice = ctx.input.petrolPriceOverride ?: prices.petrolPrice,
            evEfficiencyKwh100km = ctx.input.evEfficiencyOverrideKwh100km ?: prices.evEfficiencyKwh100km,
            iceEfficiencyL100km = ctx.input.iceEfficiencyOverrideL100km ?: prices.iceEfficiencyL100km,
            heatingReferenceCost = ctx.input.heatingReferenceCostOverride ?: prices.heatingReferenceCost,
            allocationPriority = ctx.policy.priorityOrder,
        )

        val result = calculationService.calculate(calcInput)

        val run = calculationRunRepository.save(CalculationRun().apply {
            this.tenant = ctx.tenant
            this.energyProfile = ctx.profile
            this.monthlyInput = ctx.input
            this.allocationPolicy = ctx.policy
            this.period = period
        })

        calculationResultRepository.save(CalculationResultEntity().apply {
            calculationRun = run
            this.tenant = ctx.tenant
            feedInKwh = result.feedInKwh
            feedInRevenue = result.feedInRevenue
            selfConsumptionPoolKwh = result.selfConsumptionPoolKwh
            householdAllocatedKwh = result.householdAllocatedKwh
            householdGridKwh = result.householdGridKwh
            householdSavings = result.householdSavings
            heatPumpAllocatedKwh = result.heatPumpAllocatedKwh
            heatPumpGridKwh = result.heatPumpGridKwh
            heatPumpElectricitySavings = result.heatPumpElectricitySavings
            heatPumpHeatingReferenceSavings = result.heatPumpHeatingReferenceSavings
            wallboxAllocatedKwh = result.wallboxAllocatedKwh
            wallboxGridKwh = result.wallboxGridKwh
            wallboxElectricitySavings = result.wallboxElectricitySavings
            wallboxPetrolSavings = result.wallboxPetrolSavings
            unallocatedKwh = result.unallocatedKwh
            totalElectricitySavings = result.totalElectricitySavings
            completenessFlags = result.completeness.flags
        })

        return result.toResponse(calculationRunId = run.id)
    }

    @PostMapping("/{period}/compare")
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_CALCULATIONS_READ}')")
    fun compareScenarios(
        @PathVariable tenantId: Long,
        @PathVariable profileId: Long,
        @PathVariable period: String,
        @Valid @RequestBody request: ScenarioComparisonRequest,
    ): List<CalculationResponse> {
        val ctx = resolveContext(tenantId, profileId, period)
        val prices = priceResolver.resolve(ctx.tenant, tenantId, profileId, period)

        val baseInput = CalculationInput(
            tenantId = tenantId,
            energyProfileId = profileId,
            period = period,
            hasWallbox = ctx.tenant.hasWallbox,
            hasHeatPump = ctx.tenant.hasHeatPump,
            consumptionKwh = ctx.input.consumptionKwh,
            generationKwh = ctx.input.generationKwh,
            feedInKwh = ctx.input.feedInKwh ?: java.math.BigDecimal.ZERO,
            householdConsumptionKwh = ctx.input.householdConsumptionKwh,
            heatPumpConsumptionKwh = ctx.input.heatPumpConsumptionKwh,
            wallboxConsumptionKwh = ctx.input.wallboxConsumptionKwh,
            electricityPrice = ctx.input.electricityPriceOverride ?: prices.electricityPrice,
            feedInTariff = ctx.input.feedInTariffOverride ?: prices.feedInTariff,
            petrolPrice = ctx.input.petrolPriceOverride ?: prices.petrolPrice,
            evEfficiencyKwh100km = ctx.input.evEfficiencyOverrideKwh100km ?: prices.evEfficiencyKwh100km,
            iceEfficiencyL100km = ctx.input.iceEfficiencyOverrideL100km ?: prices.iceEfficiencyL100km,
            heatingReferenceCost = ctx.input.heatingReferenceCostOverride ?: prices.heatingReferenceCost,
            allocationPriority = emptyList(),
        )

        return calculationService.compareScenarios(baseInput, request.scenarios).map { it.toResponse() }
    }

    private data class CalcContext(
        val tenant: io.github.raginlundf.solarcalc.domain.models.tenant.Tenant,
        val profile: io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfile,
        val input: io.github.raginlundf.solarcalc.domain.models.input.MonthlyEnergyInput,
        val policy: io.github.raginlundf.solarcalc.domain.models.allocation.AllocationPolicy,
    )

    private fun resolveContext(tenantId: Long, profileId: Long, period: String): CalcContext {
        val tenant = findOrThrow("Tenant $tenantId not found") {
            tenantRepository.findById(tenantId).orElse(null)
        }
        val profile = findOrThrow("Profile $profileId not found for tenant $tenantId") {
            profileRepository.findByIdAndTenantId(id = profileId, tenantId = tenantId)
        }
        val input = findOrThrow("No monthly input for period $period") {
            inputRepository.findByTenantIdAndEnergyProfileIdAndPeriod(
                tenantId = tenantId,
                energyProfileId = profileId,
                period = period,
            )
        }
        val policy = findOrThrow("No default allocation policy for profile $profileId") {
            policyRepository.findByTenantIdAndEnergyProfileIdAndIsDefaultTrue(
                tenantId = tenantId,
                energyProfileId = profileId,
            )
        }
        return CalcContext(tenant = tenant, profile = profile, input = input, policy = policy)
    }

    private inline fun <T : Any> findOrThrow(message: String, supplier: () -> T?): T {
        return supplier() ?: throw ResourceNotFoundException(message)
    }
}

private fun CalculationResult.toResponse(calculationRunId: Long? = null): CalculationResponse {
    return CalculationResponse(
        period = period,
        calculationRunId = calculationRunId,
        allocationPriority = allocationPriority,
        feedInKwh = feedInKwh,
        feedInRevenue = feedInRevenue,
        selfConsumptionPoolKwh = selfConsumptionPoolKwh,
        unallocatedKwh = unallocatedKwh,
        householdAllocatedKwh = householdAllocatedKwh,
        householdGridKwh = householdGridKwh,
        householdSavings = householdSavings,
        heatPumpAllocatedKwh = heatPumpAllocatedKwh,
        heatPumpGridKwh = heatPumpGridKwh,
        heatPumpElectricitySavings = heatPumpElectricitySavings,
        heatPumpHeatingReferenceSavings = heatPumpHeatingReferenceSavings,
        wallboxAllocatedKwh = wallboxAllocatedKwh,
        wallboxGridKwh = wallboxGridKwh,
        wallboxElectricitySavings = wallboxElectricitySavings,
        wallboxPetrolSavings = wallboxPetrolSavings,
        totalElectricitySavings = totalElectricitySavings,
        completenessFlags = completeness.flags,
    )
}
