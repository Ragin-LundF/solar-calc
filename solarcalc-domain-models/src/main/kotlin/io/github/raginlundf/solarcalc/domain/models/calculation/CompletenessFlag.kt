package io.github.raginlundf.solarcalc.domain.models.calculation

import kotlinx.serialization.Serializable

@Serializable
enum class CompletenessFlag {
    COMPLETE,
    MISSING_ELECTRICITY_PRICE,
    MISSING_FEED_IN_TARIFF,
    MISSING_PETROL_PRICE,
    MISSING_HEATING_REFERENCE_COST,
    ALLOCATION_CAPPED_FEED_IN,
    DERIVED_HOUSEHOLD_CONSUMPTION,
}
