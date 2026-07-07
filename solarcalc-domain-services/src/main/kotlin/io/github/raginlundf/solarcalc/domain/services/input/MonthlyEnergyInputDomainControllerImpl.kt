package io.github.raginlundf.solarcalc.domain.services.input

import io.github.raginlundf.solarcalc.domain.models.input.MonthlyEnergyInput
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfile
import io.github.raginlundf.solarcalc.domain.models.repository.EnergyProfileRepository
import io.github.raginlundf.solarcalc.domain.models.repository.MonthlyEnergyInputRepository
import io.github.raginlundf.solarcalc.dtos.error.DuplicateInputException
import io.github.raginlundf.solarcalc.dtos.error.ResourceNotFoundException
import io.github.raginlundf.solarcalc.dtos.input.MonthlyEnergyInputResponse
import io.github.raginlundf.solarcalc.dtos.input.UpsertMonthlyEnergyInputRequest
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class MonthlyEnergyInputDomainControllerImpl(
    private val profileRepository: EnergyProfileRepository,
    private val inputRepository: MonthlyEnergyInputRepository,
) : MonthlyEnergyInputDomainController {

    override fun list(profileUuid: String): List<MonthlyEnergyInputResponse> {
        val profile = requireProfile(profileUuid = profileUuid)
        return inputRepository.findAllByEnergyProfileId(energyProfileId = profile.id!!)
            .map { it.toResponse(energyProfileUuid = profileUuid) }
    }

    override fun get(profileUuid: String, inputId: Long): MonthlyEnergyInputResponse {
        requireProfile(profileUuid = profileUuid)
        return inputRepository.findById(inputId).orElseThrow {
            ResourceNotFoundException(message = "MonthlyInput $inputId not found")
        }.toResponse(energyProfileUuid = profileUuid)
    }

    override fun create(profileUuid: String, request: UpsertMonthlyEnergyInputRequest): MonthlyEnergyInputResponse {
        val profile = profileRepository.findByUuid(profileUuid)
            ?: throw ResourceNotFoundException(message = "Profile $profileUuid not found")

        val existing = inputRepository.findByEnergyProfileIdAndPeriod(
            energyProfileId = profile.id!!,
            period = request.period,
        )
        if (existing != null) {
            throw DuplicateInputException(message = "Input for period ${request.period} already exists.")
        }

        val input = MonthlyEnergyInput().apply {
            this.energyProfile = profile
            applyRequest(request = request)
        }
        return inputRepository.save(input).toResponse(energyProfileUuid = profileUuid)
    }

    override fun update(
        profileUuid: String,
        inputId: Long,
        request: UpsertMonthlyEnergyInputRequest
    ): MonthlyEnergyInputResponse {
        requireProfile(profileUuid = profileUuid)
        val input = inputRepository.findById(inputId).orElseThrow {
            ResourceNotFoundException(message = "MonthlyInput $inputId not found")
        }
        input.applyRequest(request = request)
        input.updatedAt = LocalDateTime.now()
        return inputRepository.save(input).toResponse(energyProfileUuid = profileUuid)
    }

    override fun delete(profileUuid: String, inputId: Long) {
        requireProfile(profileUuid = profileUuid)
        val input = inputRepository.findById(inputId).orElseThrow {
            ResourceNotFoundException(message = "MonthlyInput $inputId not found")
        }
        inputRepository.delete(input)
    }

    private fun requireProfile(profileUuid: String): EnergyProfile {
        return profileRepository.findByUuid(uuid = profileUuid)
            ?: throw ResourceNotFoundException(message = "Profile $profileUuid not found")
    }
}

private fun MonthlyEnergyInput.applyRequest(request: UpsertMonthlyEnergyInputRequest) {
    period = request.period
    consumptionKwh = (request.householdConsumptionKwh ?: BigDecimal.ZERO)
        .add(request.heatPumpConsumptionKwh ?: BigDecimal.ZERO)
        .add(request.wallboxConsumptionKwh ?: BigDecimal.ZERO)
    generationKwh = request.generationKwh
    feedInKwh = request.feedInKwh
    householdConsumptionKwh = request.householdConsumptionKwh
    heatPumpConsumptionKwh = request.heatPumpConsumptionKwh
    wallboxConsumptionKwh = request.wallboxConsumptionKwh
    electricityPriceOverride = request.electricityPriceOverride
    feedInTariffOverride = request.feedInTariffOverride
    petrolPriceOverride = request.petrolPriceOverride
    heatingReferenceCostOverride = request.heatingReferenceCostOverride
}

private fun MonthlyEnergyInput.toResponse(energyProfileUuid: String): MonthlyEnergyInputResponse {
    return MonthlyEnergyInputResponse(
        id = id!!,
        energyProfileUuid = energyProfileUuid,
        period = period,
        consumptionKwh = consumptionKwh,
        generationKwh = generationKwh,
        feedInKwh = feedInKwh,
        householdConsumptionKwh = householdConsumptionKwh,
        heatPumpConsumptionKwh = heatPumpConsumptionKwh,
        wallboxConsumptionKwh = wallboxConsumptionKwh,
        electricityPriceOverride = electricityPriceOverride,
        feedInTariffOverride = feedInTariffOverride,
        petrolPriceOverride = petrolPriceOverride,
        heatingReferenceCostOverride = heatingReferenceCostOverride,
    )
}
