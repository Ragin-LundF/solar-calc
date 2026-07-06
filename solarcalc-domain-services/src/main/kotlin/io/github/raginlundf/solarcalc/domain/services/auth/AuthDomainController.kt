package io.github.raginlundf.solarcalc.domain.services.auth

import io.github.raginlundf.solarcalc.dtos.auth.AuthResponse
import io.github.raginlundf.solarcalc.dtos.auth.LoginRequest
import io.github.raginlundf.solarcalc.dtos.auth.RegisterRequest

interface AuthDomainController {
    fun register(request: RegisterRequest): AuthResponse
    fun login(request: LoginRequest): AuthResponse
}
