package io.github.raginlundf.solarcalc.domain.services.auth

import io.github.raginlundf.solarcalc.dtos.auth.AuthResponseDto
import io.github.raginlundf.solarcalc.dtos.auth.LoginRequestDto
import io.github.raginlundf.solarcalc.dtos.auth.RegisterRequestDto
import io.github.raginlundf.solarcalc.dtos.auth.UpdateLastProfileRequestDto
import io.github.raginlundf.solarcalc.dtos.auth.UpdateSetupStepRequestDto

interface AuthDomainController {
    fun register(request: RegisterRequestDto): AuthResponseDto
    fun login(request: LoginRequestDto): AuthResponseDto
    fun updateSetupStep(username: String, request: UpdateSetupStepRequestDto): Int
    fun updateLastProfile(username: String, request: UpdateLastProfileRequestDto)
}
