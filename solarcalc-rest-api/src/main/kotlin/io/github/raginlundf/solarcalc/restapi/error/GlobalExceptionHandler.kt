package io.github.raginlundf.solarcalc.restapi.error

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val firstField = ex.bindingResult.fieldErrors.firstOrNull()
        val error = ApiError(
            code = "VALIDATION_ERROR",
            message = firstField?.defaultMessage ?: "Validation failed",
            details = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "") },
        )
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error)
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException): ResponseEntity<ApiError> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError(code = "NOT_FOUND", message = ex.message ?: "Not found"))
    }

    @ExceptionHandler(TenantAccessDeniedException::class)
    fun handleTenantAccess(ex: TenantAccessDeniedException): ResponseEntity<ApiError> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError(code = "TENANT_ACCESS_DENIED", message = ex.message ?: "Access denied"))
    }
}
