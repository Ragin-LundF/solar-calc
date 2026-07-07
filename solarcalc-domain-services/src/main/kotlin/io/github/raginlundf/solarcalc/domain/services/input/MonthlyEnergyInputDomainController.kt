package io.github.raginlundf.solarcalc.domain.services.input

import io.github.raginlundf.solarcalc.dtos.input.MonthlyEnergyInputResponse
import io.github.raginlundf.solarcalc.dtos.input.UpsertMonthlyEnergyInputRequest

interface MonthlyEnergyInputDomainController {
    fun list(profileUuid: String, username: String): List<MonthlyEnergyInputResponse>
    fun get(profileUuid: String, inputUuid: String, username: String): MonthlyEnergyInputResponse
    fun create(
        profileUuid: String,
        request: UpsertMonthlyEnergyInputRequest,
        username: String,
    ): MonthlyEnergyInputResponse
    fun update(
        profileUuid: String,
        inputUuid: String,
        request: UpsertMonthlyEnergyInputRequest,
        username: String,
    ): MonthlyEnergyInputResponse
    fun delete(profileUuid: String, inputUuid: String, username: String)
}
