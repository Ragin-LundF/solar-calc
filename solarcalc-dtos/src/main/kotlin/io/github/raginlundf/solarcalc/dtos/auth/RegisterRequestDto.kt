package io.github.raginlundf.solarcalc.dtos.auth

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    @field:NotBlank
    @field:Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    val username: String,

    // Max 72: BCrypt silently ignores bytes beyond 72, so anything longer is misleading.
    @field:NotBlank
    @field:Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
    val password: String,
)
