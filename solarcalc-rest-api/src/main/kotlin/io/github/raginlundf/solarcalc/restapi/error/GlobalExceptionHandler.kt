package io.github.raginlundf.solarcalc.restapi.error

import io.github.raginlundf.solarcalc.dtos.error.ApiError
import io.github.raginlundf.solarcalc.dtos.error.DuplicateInputException
import io.github.raginlundf.solarcalc.dtos.error.ResourceNotFoundException
import io.github.raginlundf.solarcalc.dtos.error.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

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

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMessageNotReadable(ex: HttpMessageNotReadableException): ResponseEntity<ApiError> {
        log.warn("Malformed request body", ex)
        val error = ApiError(code = "MALFORMED_BODY", message = "Request body could not be read")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException): ResponseEntity<ApiError> {
        val error = ApiError(code = "NOT_FOUND", message = ex.message ?: "Not found")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    /** 409 matches what MonthlyEnergyInputController already returns for a duplicate period. */
    @ExceptionHandler(DuplicateInputException::class)
    fun handleDuplicate(ex: DuplicateInputException): ResponseEntity<ApiError> {
        val error = ApiError(code = "DUPLICATE_INPUT", message = ex.message ?: "Already exists")
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error)
    }

    @ExceptionHandler(ValidationException::class)
    fun handleDomainValidation(ex: ValidationException): ResponseEntity<ApiError> {
        val error = ApiError(code = "VALIDATION_ERROR", message = ex.message ?: "Validation failed")
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error)
    }

    /** A non-numeric {priceId} and friends would otherwise surface as a 500. */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ApiError> {
        val error = ApiError(code = "INVALID_PARAMETER", message = "Invalid value for '${ex.name}'")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<ApiError> {
        log.error("Unexpected error", ex)
        val error = ApiError(code = "INTERNAL_ERROR", message = "An unexpected error occurred")
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }
}

