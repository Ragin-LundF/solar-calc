package io.github.raginlundf.solarcalc.domain.services.input

import io.github.raginlundf.solarcalc.domain.models.input.MonthlyEnergyInput
import io.github.raginlundf.solarcalc.domain.models.repository.EnergyProfileRepository
import io.github.raginlundf.solarcalc.domain.models.repository.MonthlyEnergyInputRepository
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

    override fun list(profileId: Long): List<MonthlyEnergyInputResponse> {
        requireProfile(profileId = profileId)
        return inputRepository.findAllByEnergyProfileId(energyProfileId = profileId).map { it.toResponse() }
    }

    override fun get(profileId: Long, inputId: Long): MonthlyEnergyInputResponse {
        requireProfile(profileId = profileId)
        return inputRepository.findById(inputId).orElseThrow {
            ResourceNotFoundException("MonthlyInput $inputId not found")
        }.toResponse()
    }

    override fun create(profileId: Long, request: UpsertMonthlyEnergyInputRequest): MonthlyEnergyInputResponse {
        val profile = profileRepository.findById(profileId).orElseThrow {
            ResourceNotFoundException("Profile $profileId not found")
        }

        val existing = inputRepository.findByEnergyProfileIdAndPeriod(
            energyProfileId = profileId,
            period = request.period,
        )
        if (existing != null) {
            throw DuplicateInputException("Input for period ${request.period} already exists.")
        }

        val input = MonthlyEnergyInput().apply {
            this.energyProfile = profile
            applyRequest(request)
        }
        return inputRepository.save(input).toResponse()
    }

    override fun update(profileId: Long, inputId: Long, request: UpsertMonthlyEnergyInputRequest): MonthlyEnergyInputResponse {
        requireProfile(profileId = profileId)
        val input = inputRepository.findById(inputId).orElseThrow {
            ResourceNotFoundException("MonthlyInput $inputId not found")
        }
        input.applyRequest(request)
        input.updatedAt = LocalDateTime.now()
        return inputRepository.save(input).toResponse()
    }

    override fun delete(profileId: Long, inputId: Long) {
        requireProfile(profileId = profileId)
        val input = inputRepository.findById(inputId).orElseThrow {
            ResourceNotFoundException("MonthlyInput $inputId not found")
        }
        inputRepository.delete(input)
    }

    private fun requireProfile(profileId: Long) {
        profileRepository.findById(profileId).orElseThrow {
            ResourceNotFoundException("Profile $profileId not found")
        }
    }
}

class DuplicateInputException(message: String) : RuntimeException(message)

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

private fun MonthlyEnergyInput.toResponse(): MonthlyEnergyInputResponse {
    return MonthlyEnergyInputResponse(
        id = id!!,
        energyProfileId = energyProfileId!!,
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
