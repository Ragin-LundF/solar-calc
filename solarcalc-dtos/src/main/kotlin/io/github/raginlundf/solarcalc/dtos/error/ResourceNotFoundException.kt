package io.github.raginlundf.solarcalc.dtos.error

import java.io.Serial

class ResourceNotFoundException(message: String) : RuntimeException(message) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -8602512253837704585L
    }
}
