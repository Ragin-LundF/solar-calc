package io.github.raginlundf.solarcalc.domain.services.calculation

import io.github.raginlundf.solarcalc.domain.models.calculation.CompletenessFlag

data class CalculationCompleteness(
    val flags: Set<CompletenessFlag>,
) {
    val isComplete: Boolean
        get() = flags.isEmpty() || flags == setOf(CompletenessFlag.COMPLETE)
}
