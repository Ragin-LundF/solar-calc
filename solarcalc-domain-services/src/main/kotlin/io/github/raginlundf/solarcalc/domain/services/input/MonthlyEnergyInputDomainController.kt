package io.github.raginlundf.solarcalc.domain.services.input

import io.github.raginlundf.solarcalc.dtos.input.MonthlyEnergyInputResponse
import io.github.raginlundf.solarcalc.dtos.input.UpsertMonthlyEnergyInputRequest

interface MonthlyEnergyInputDomainController {
    fun list(profileId: Long): List<MonthlyEnergyInputResponse>
    fun get(profileId: Long, inputId: Long): MonthlyEnergyInputResponse
    fun create(profileId: Long, request: UpsertMonthlyEnergyInputRequest): MonthlyEnergyInputResponse
    fun update(profileId: Long, inputId: Long, request: UpsertMonthlyEnergyInputRequest): MonthlyEnergyInputResponse
    fun delete(profileId: Long, inputId: Long)
}
