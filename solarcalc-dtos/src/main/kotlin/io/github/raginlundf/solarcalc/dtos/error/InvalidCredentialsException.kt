package io.github.raginlundf.solarcalc.dtos.error

import java.io.Serial

class InvalidCredentialsException(message: String) : RuntimeException(message) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -2170175074412722185L
    }
}
