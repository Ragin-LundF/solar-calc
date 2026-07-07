package io.github.raginlundf.solarcalc.dtos.auth

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    @field:NotBlank
    @field:Size(max = 100)
    val username: String,

    @field:NotBlank
    @field:Size(max = 72)
    val password: String,
)
