package io.github.raginlundf.solarcalc.dtos.error

import java.io.Serial

class DuplicateInputException(message: String) : RuntimeException(message) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -1300767160191871002L
    }
}
