package io.github.raginlundf.solarcalc.dtos.error

import java.io.Serial

/** A semantic rule the request body violates that bean validation cannot express on a single field. */
class ValidationException(message: String) : RuntimeException(message) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -7748219054168873001L
    }
}
