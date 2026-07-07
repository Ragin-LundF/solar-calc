package io.github.raginlundf.extensions

import java.math.BigDecimal
import java.math.RoundingMode

fun BigDecimal.scale2(): BigDecimal {
    return setScale(2, RoundingMode.HALF_UP)
}

@Suppress("MagicNumber")
fun BigDecimal.scale3(): BigDecimal {
    return this.setScale(3, RoundingMode.HALF_UP)
}
