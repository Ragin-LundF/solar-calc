package io.github.raginlundf.solarcalc.domain.services.calculation

import io.github.raginlundf.solarcalc.domain.models.calculation.CalculationResult as CalculationResultEntity
import io.github.raginlundf.solarcalc.domain.models.calculation.CalculationRun
import io.github.raginlundf.solarcalc.domain.models.repository.AllocationPolicyRepository
import io.github.raginlundf.solarcalc.domain.models.repository.CalculationResultRepository
import io.github.raginlundf.solarcalc.domain.models.repository.CalculationRunRepository
import io.github.raginlundf.solarcalc.domain.models.repository.EnergyProfileRepository
import io.github.raginlundf.solarcalc.domain.models.repository.MonthlyEnergyInputRepository
import io.github.raginlundf.solarcalc.dtos.calculation.CalculationResponse
import io.github.raginlundf.solarcalc.dtos.calculation.ScenarioComparisonRequest
import io.github.raginlundf.solarcalc.dtos.error.ResourceNotFoundException
import io.github.raginlundf.solarcalc.domain.services.price.PriceResolver
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CalculationDomainControllerImpl(
    private val profileRepository: EnergyProfileRepository,
    private val inputRepository: MonthlyEnergyInputRepository,
    private val policyRepository: AllocationPolicyRepository,
    private val calculationRunRepository: CalculationRunRepository,
    private val calculationResultRepository: CalculationResultRepository,
    private val calculationService: EnergyCalculationService,
    private val priceResolver: PriceResolver,
) : CalculationDomainController {

    @Transactional
    override fun calculate(profileId: Long, period: String): CalculationResponse {
        val ctx = resolveContext(profileId = profileId, period = period)
        val prices = priceResolver.resolve(ctx.profile, period)

        val calcInput = CalculationInput(
            energyProfileId = profileId,
            period = period,
            hasWallbox = ctx.profile.hasWallbox,
            hasHeatPump = ctx.profile.hasHeatPump,
            consumptionKwh = ctx.input.consumptionKwh,
            generationKwh = ctx.input.generationKwh,
            feedInKwh = ctx.input.feedInKwh ?: java.math.BigDecimal.ZERO,
            householdConsumptionKwh = ctx.input.householdConsumptionKwh,
            heatPumpConsumptionKwh = ctx.input.heatPumpConsumptionKwh,
            wallboxConsumptionKwh = ctx.input.wallboxConsumptionKwh,
            electricityPrice = ctx.input.electricityPriceOverride ?: prices.electricityPrice,
            feedInTariff = ctx.input.feedInTariffOverride ?: prices.feedInTariff,
            petrolPrice = ctx.input.petrolPriceOverride ?: prices.petrolPrice,
            heatingReferenceCost = ctx.input.heatingReferenceCostOverride ?: prices.heatingReferenceCost,
            allocationPriority = ctx.policy.priorityOrder,
        )

        val result = calculationService.calculate(calcInput)

        val run = calculationRunRepository.save(CalculationRun().apply {
            this.energyProfile = ctx.profile
            this.monthlyInput = ctx.input
            this.allocationPolicy = ctx.policy
            this.period = period
        })

        calculationResultRepository.save(CalculationResultEntity().apply {
            calculationRun = run
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

    override fun compareScenarios(profileId: Long, period: String, request: ScenarioComparisonRequest): List<CalculationResponse> {
        val ctx = resolveContext(profileId = profileId, period = period)
        val prices = priceResolver.resolve(ctx.profile, period)

        val baseInput = CalculationInput(
            energyProfileId = profileId,
            period = period,
            hasWallbox = ctx.profile.hasWallbox,
            hasHeatPump = ctx.profile.hasHeatPump,
            consumptionKwh = ctx.input.consumptionKwh,
            generationKwh = ctx.input.generationKwh,
            feedInKwh = ctx.input.feedInKwh ?: java.math.BigDecimal.ZERO,
            householdConsumptionKwh = ctx.input.householdConsumptionKwh,
            heatPumpConsumptionKwh = ctx.input.heatPumpConsumptionKwh,
            wallboxConsumptionKwh = ctx.input.wallboxConsumptionKwh,
            electricityPrice = ctx.input.electricityPriceOverride ?: prices.electricityPrice,
            feedInTariff = ctx.input.feedInTariffOverride ?: prices.feedInTariff,
            petrolPrice = ctx.input.petrolPriceOverride ?: prices.petrolPrice,
            heatingReferenceCost = ctx.input.heatingReferenceCostOverride ?: prices.heatingReferenceCost,
            allocationPriority = emptyList(),
        )

        return calculationService.compareScenarios(baseInput, request.scenarios).map { it.toResponse() }
    }

    private data class CalcContext(
        val profile: io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfile,
        val input: io.github.raginlundf.solarcalc.domain.models.input.MonthlyEnergyInput,
        val policy: io.github.raginlundf.solarcalc.domain.models.allocation.AllocationPolicy,
    )

    private fun resolveContext(profileId: Long, period: String): CalcContext {
        val profile = findOrThrow("Profile $profileId not found") {
            profileRepository.findById(profileId).orElse(null)
        }
        val input = findOrThrow("No monthly input for period $period") {
            inputRepository.findByEnergyProfileIdAndPeriod(
                energyProfileId = profileId,
                period = period,
            )
        }
        val policy = findOrThrow("No default allocation policy for profile $profileId") {
            policyRepository.findByEnergyProfileIdAndIsDefaultTrue(
                energyProfileId = profileId,
            )
        }
        return CalcContext(profile = profile, input = input, policy = policy)
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
