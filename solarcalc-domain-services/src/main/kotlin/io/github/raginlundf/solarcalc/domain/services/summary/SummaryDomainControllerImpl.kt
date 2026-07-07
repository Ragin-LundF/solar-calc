package io.github.raginlundf.solarcalc.domain.services.summary

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationCategory
import io.github.raginlundf.solarcalc.domain.models.profile.DEFAULT_HEATING_DISTRIBUTION
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfile
import io.github.raginlundf.solarcalc.domain.models.repository.AllocationPolicyRepository
import io.github.raginlundf.solarcalc.domain.models.repository.EnergyProfileRepository
import io.github.raginlundf.solarcalc.domain.models.repository.MonthlyEnergyInputRepository
import io.github.raginlundf.solarcalc.domain.services.price.PriceResolver
import io.github.raginlundf.solarcalc.dtos.error.ResourceNotFoundException
import io.github.raginlundf.solarcalc.dtos.summary.SummaryResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class SummaryDomainControllerImpl(
    private val profileRepository: EnergyProfileRepository,
    private val inputRepository: MonthlyEnergyInputRepository,
    private val policyRepository: AllocationPolicyRepository,
    private val priceResolver: PriceResolver,
    private val summaryService: SummaryService,
) : SummaryDomainController {

    private val periodFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

    @Transactional(readOnly = true)
    override fun summarize(
        profileUuid: String,
        username: String,
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): SummaryResponse {
        val profile = profileRepository.findByUuidAndUserUsername(uuid = profileUuid, userUsername = username)
            ?: throw ResourceNotFoundException("Profile $profileUuid not found")

        val inputs = inputRepository.findAllByEnergyProfileId(energyProfileId = profile.id!!)
        val priority = policyRepository.findAllByEnergyProfileId(energyProfileId = profile.id!!)
            .firstOrNull()?.priorityOrder
            ?: AllocationCategory.entries.toList()

        val monthInputs = inputs.map { input ->
            val prices = priceResolver.resolve(profile = profile, period = input.period)
            val gridPrice = input.electricityPriceOverride ?: prices.electricityPrice ?: BigDecimal.ZERO
            SummaryMonthInput(
                period = input.period,
                generationKwh = input.generationKwh,
                feedInKwh = input.feedInKwh ?: BigDecimal.ZERO,
                consumptionKwh = input.consumptionKwh,
                householdKwh = input.householdConsumptionKwh,
                heatPumpKwh = input.heatPumpConsumptionKwh ?: BigDecimal.ZERO,
                wallboxKwh = input.wallboxConsumptionKwh ?: BigDecimal.ZERO,
                gridPrice = gridPrice,
                feedInTariff = input.feedInTariffOverride ?: prices.feedInTariff ?: BigDecimal.ZERO,
                petrolPrice = input.petrolPriceOverride ?: prices.petrolPrice ?: BigDecimal.ZERO,
                heizReferenzJahr = input.heatingReferenceCostOverride ?: prices.heatingReferenceCost ?: BigDecimal.ZERO,
            )
        }

        return summaryService.summarize(
            months = monthInputs,
            params = params(profile = profile, priority = priority),
            rangeStart = startDate?.format(periodFormatter),
            rangeEnd = endDate?.format(periodFormatter),
        )
    }

    private fun params(profile: EnergyProfile, priority: List<AllocationCategory>): SummaryParams {
        val distribution = profile.heatingMonthlyDistribution.takeIf { it.size == 12 } ?: DEFAULT_HEATING_DISTRIBUTION
        return SummaryParams(
            allocationPriority = priority,
            hasHeatPump = profile.hasHeatPump,
            hasWallbox = profile.hasWallbox,
            kmPerKwh = profile.kmPerKwh ?: BigDecimal.ZERO,
            litersPer100km = profile.litersPer100km ?: BigDecimal.ZERO,
            investKosten = profile.investCost ?: BigDecimal.ZERO,
            heatingDistribution = distribution,
        )
    }
}
