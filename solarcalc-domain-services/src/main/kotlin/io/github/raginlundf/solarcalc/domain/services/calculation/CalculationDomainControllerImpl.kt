package io.github.raginlundf.solarcalc.domain.services.calculation

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationPolicy
import io.github.raginlundf.solarcalc.domain.models.calculation.CalculationResult as CalculationResultEntity
import io.github.raginlundf.solarcalc.domain.models.calculation.CalculationRun
import io.github.raginlundf.solarcalc.domain.models.input.MonthlyEnergyInput
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfile
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    override fun calculate(
        profileUuid: String,
        username: String,
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): CalculationResponse {
        val profile = profileRepository.findByUuidAndUserUsername(uuid = profileUuid, userUsername = username)
            ?: throw ResourceNotFoundException("Profile $profileUuid not found")

        val period = resolvePeriod(profileId = profile.id!!, startDate = startDate, endDate = endDate)
        val ctx = resolveContext(profile = profile, period = period)
        val prices = priceResolver.resolve(ctx.profile, period)

        val calcInput = CalculationInput(
            energyProfileId = profile.id!!,
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
            unallocatedKwh = result.unallocatedKwh
            totalElectricitySavings = result.totalElectricitySavings
            completenessFlags = result.completeness.flags
        })

        return result.toResponse(calculationRunId = run.id)
    }

    override fun compareScenarios(
        profileUuid: String,
        username: String,
        startDate: LocalDate?,
        endDate: LocalDate?,
        request: ScenarioComparisonRequest
    ): List<CalculationResponse> {
        val profile = profileRepository.findByUuidAndUserUsername(uuid = profileUuid, userUsername = username)
            ?: throw ResourceNotFoundException(message = "Profile $profileUuid not found")

        val period = resolvePeriod(profileId = profile.id!!, startDate = startDate, endDate = endDate)
        val ctx = resolveContext(profile = profile, period = period)
        val prices = priceResolver.resolve(ctx.profile, period)

        val baseInput = CalculationInput(
            energyProfileId = profile.id!!,
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

        return calculationService.compareScenarios(
            input = baseInput,
            priorities = request.scenarios
        ).map { it.toResponse() }
    }

    private fun resolvePeriod(profileId: Long, startDate: LocalDate?, endDate: LocalDate?): String {
        val date = startDate ?: endDate
        if (date != null) {
            return date.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        }
        val inputs = inputRepository.findAllByEnergyProfileId(energyProfileId = profileId)
        val latest = inputs.maxByOrNull { it.period }
            ?: throw ResourceNotFoundException(message = "No monthly inputs found for profile $profileId")
        return latest.period
    }

    private data class CalcContext(
        val profile: EnergyProfile,
        val input: MonthlyEnergyInput,
        val policy: AllocationPolicy,
    )

    private fun resolveContext(profile: EnergyProfile, period: String): CalcContext {
        val input = findOrThrow(message = "No monthly input for period $period") {
            inputRepository.findByEnergyProfileIdAndPeriod(
                energyProfileId = profile.id!!,
                period = period,
            )
        }
        val policies = policyRepository.findAllByEnergyProfileId(energyProfileId = profile.id!!)
        val policy = findOrThrow(message = "No allocation policy for profile ${profile.uuid}") {
            policies.firstOrNull()
        }
        return CalcContext(profile = profile, input = input, policy = policy)
    }

    private inline fun <T : Any> findOrThrow(message: String, supplier: () -> T?): T {
        return supplier() ?: throw ResourceNotFoundException(message = message)
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
        totalElectricitySavings = totalElectricitySavings,
        completenessFlags = completeness.flags,
    )
}
