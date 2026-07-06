package io.github.raginlundf.solarcalc.restapi.error

data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, Any> = emptyMap(),
)
