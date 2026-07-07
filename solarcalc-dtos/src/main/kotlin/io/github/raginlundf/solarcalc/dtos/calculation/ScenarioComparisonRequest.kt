@file:UseSerializers(BigDecimalSerializer::class)

package io.github.raginlundf.solarcalc.dtos.calculation

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationCategory
import io.github.raginlundf.solarcalc.dtos.serialization.BigDecimalSerializer
import jakarta.validation.constraints.NotEmpty
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.math.BigDecimal

@Serializable
data class ScenarioComparisonRequest(
    @field:NotEmpty val scenarios: List<List<AllocationCategory>>,
)
