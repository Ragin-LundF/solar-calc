package io.github.raginlundf.solarcalc.dtos.error

import java.io.Serial

class UsernameAlreadyExistsException(message: String) : RuntimeException(message) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -5712204818687059672L
    }
}
