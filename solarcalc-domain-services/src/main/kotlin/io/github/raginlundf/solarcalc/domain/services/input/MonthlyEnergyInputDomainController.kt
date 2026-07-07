package io.github.raginlundf.solarcalc.domain.services.input

import io.github.raginlundf.solarcalc.dtos.input.MonthlyEnergyInputResponse
import io.github.raginlundf.solarcalc.dtos.input.UpsertMonthlyEnergyInputRequest

interface MonthlyEnergyInputDomainController {
    fun list(profileUuid: String, username: String): List<MonthlyEnergyInputResponse>
    fun get(profileUuid: String, inputId: Long, username: String): MonthlyEnergyInputResponse
    fun create(
        profileUuid: String,
        request: UpsertMonthlyEnergyInputRequest,
        username: String,
    ): MonthlyEnergyInputResponse
    fun update(
        profileUuid: String,
        inputId: Long,
        request: UpsertMonthlyEnergyInputRequest,
        username: String,
    ): MonthlyEnergyInputResponse
    fun delete(profileUuid: String, inputId: Long, username: String)
}
