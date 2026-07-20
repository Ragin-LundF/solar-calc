package io.github.raginlundf.solarcalc.dtos.error


data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, String> = emptyMap(),
)
