package io.github.raginlundf.extensions

import java.math.BigDecimal
import java.math.RoundingMode

fun BigDecimal.scale2(): BigDecimal {
    return this.setScale(2, RoundingMode.HALF_UP)
}
