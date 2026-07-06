package io.github.raginlundf.solarcalc.restapi.calculation

import io.github.raginlundf.solarcalc.domain.models.repository.AllocationPolicyRepository
import io.github.raginlundf.solarcalc.domain.models.repository.EnergyProfileRepository
import io.github.raginlundf.solarcalc.domain.models.repository.MonthlyEnergyInputRepository
import io.github.raginlundf.solarcalc.domain.models.repository.TenantRepository
import io.github.raginlundf.solarcalc.domain.services.calculation.CalculationInput
import io.github.raginlundf.solarcalc.domain.services.calculation.CalculationResult
import io.github.raginlundf.solarcalc.domain.services.calculation.EnergyCalculationService
import io.github.raginlundf.solarcalc.domain.services.price.PriceResolver
import io.github.raginlundf.solarcalc.restapi.error.ResourceNotFoundException
import jakarta.validation.Valid
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
    private val calculationService: EnergyCalculationService,
    private val priceResolver: PriceResolver,
) {

    @GetMapping("/{period}")
    fun calculate(
        @PathVariable tenantId: Long,
        @PathVariable profileId: Long,
        @PathVariable period: String,
    ): CalculationResponse {
        val (tenant, input, policy) = resolveContext(tenantId, profileId, period)
        val prices = priceResolver.resolve(tenant, tenantId, profileId, period)

        val calcInput = CalculationInput(
            tenantId = tenantId,
            energyProfileId = profileId,
            period = period,
            hasWallbox = tenant.hasWallbox,
            hasHeatPump = tenant.hasHeatPump,
            consumptionKwh = input.consumptionKwh,
            generationKwh = input.generationKwh,
            feedInKwh = input.feedInKwh ?: java.math.BigDecimal.ZERO,
            householdConsumptionKwh = input.householdConsumptionKwh,
            heatPumpConsumptionKwh = input.heatPumpConsumptionKwh,
            wallboxConsumptionKwh = input.wallboxConsumptionKwh,
            electricityPrice = input.electricityPriceOverride ?: prices.electricityPrice,
            feedInTariff = input.feedInTariffOverride ?: prices.feedInTariff,
            petrolPrice = input.petrolPriceOverride ?: prices.petrolPrice,
            evEfficiencyKwh100km = input.evEfficiencyOverrideKwh100km ?: prices.evEfficiencyKwh100km,
            iceEfficiencyL100km = input.iceEfficiencyOverrideL100km ?: prices.iceEfficiencyL100km,
            heatingReferenceCost = input.heatingReferenceCostOverride ?: prices.heatingReferenceCost,
            allocationPriority = policy.priorityOrder,
        )

        return calculationService.calculate(calcInput).toResponse()
    }

    @PostMapping("/{period}/compare")
    fun compareScenarios(
        @PathVariable tenantId: Long,
        @PathVariable profileId: Long,
        @PathVariable period: String,
        @Valid @RequestBody request: ScenarioComparisonRequest,
    ): List<CalculationResponse> {
        val (tenant, input, _) = resolveContext(tenantId, profileId, period)
        val prices = priceResolver.resolve(tenant, tenantId, profileId, period)

        val baseInput = CalculationInput(
            tenantId = tenantId,
            energyProfileId = profileId,
            period = period,
            hasWallbox = tenant.hasWallbox,
            hasHeatPump = tenant.hasHeatPump,
            consumptionKwh = input.consumptionKwh,
            generationKwh = input.generationKwh,
            feedInKwh = input.feedInKwh ?: java.math.BigDecimal.ZERO,
            householdConsumptionKwh = input.householdConsumptionKwh,
            heatPumpConsumptionKwh = input.heatPumpConsumptionKwh,
            wallboxConsumptionKwh = input.wallboxConsumptionKwh,
            electricityPrice = input.electricityPriceOverride ?: prices.electricityPrice,
            feedInTariff = input.feedInTariffOverride ?: prices.feedInTariff,
            petrolPrice = input.petrolPriceOverride ?: prices.petrolPrice,
            evEfficiencyKwh100km = input.evEfficiencyOverrideKwh100km ?: prices.evEfficiencyKwh100km,
            iceEfficiencyL100km = input.iceEfficiencyOverrideL100km ?: prices.iceEfficiencyL100km,
            heatingReferenceCost = input.heatingReferenceCostOverride ?: prices.heatingReferenceCost,
            allocationPriority = emptyList(),
        )

        return calculationService.compareScenarios(baseInput, request.scenarios).map { it.toResponse() }
    }

    private data class CalcContext(
        val tenant: io.github.raginlundf.solarcalc.domain.models.tenant.Tenant,
        val input: io.github.raginlundf.solarcalc.domain.models.input.MonthlyEnergyInput,
        val policy: io.github.raginlundf.solarcalc.domain.models.allocation.AllocationPolicy,
    )

    private fun resolveContext(tenantId: Long, profileId: Long, period: String): CalcContext {
        val tenant = tenantRepository.findById(tenantId).orElseThrow {
            ResourceNotFoundException("Tenant $tenantId not found")
        }
        profileRepository.findByIdAndTenantId(profileId, tenantId)
            ?: throw ResourceNotFoundException("Profile $profileId not found for tenant $tenantId")
        val input = inputRepository.findByTenantIdAndEnergyProfileIdAndPeriod(tenantId, profileId, period)
            ?: throw ResourceNotFoundException("No monthly input for period $period")
        val policy = policyRepository.findByTenantIdAndEnergyProfileIdAndIsDefaultTrue(tenantId, profileId)
            ?: throw ResourceNotFoundException("No default allocation policy for profile $profileId")
        return CalcContext(tenant = tenant, input = input, policy = policy)
    }
}

private fun CalculationResult.toResponse(): CalculationResponse {
    return CalculationResponse(
        period = period,
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
