package io.github.raginlundf.solarcalc.restapi.tenant

import io.github.raginlundf.solarcalc.domain.models.tenant.HeatingReferenceType
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal

data class TenantResponse(
    val id: Long,
    val name: String,
    val locale: String,
    val providerLabel: String?,
    val hasWallbox: Boolean,
    val hasHeatPump: Boolean,
    val heatingReferenceType: HeatingReferenceType,
    val defaultElectricityPrice: BigDecimal?,
    val defaultFeedInTariff: BigDecimal?,
    val defaultPetrolPrice: BigDecimal?,
    val defaultOilReferenceCost: BigDecimal?,
    val defaultGasReferenceCost: BigDecimal?,
    val defaultEvEfficiencyKwh100km: BigDecimal?,
    val defaultIceEfficiencyL100km: BigDecimal?,
)

data class CreateTenantRequest(
    @field:NotBlank val name: String,
    val locale: String = "de",
    val providerLabel: String? = null,
    val hasWallbox: Boolean = false,
    val hasHeatPump: Boolean = false,
    val heatingReferenceType: HeatingReferenceType = HeatingReferenceType.NONE,
    val defaultElectricityPrice: BigDecimal? = null,
    val defaultFeedInTariff: BigDecimal? = null,
    val defaultPetrolPrice: BigDecimal? = null,
    val defaultOilReferenceCost: BigDecimal? = null,
    val defaultGasReferenceCost: BigDecimal? = null,
    val defaultEvEfficiencyKwh100km: BigDecimal? = null,
    val defaultIceEfficiencyL100km: BigDecimal? = null,
)

data class UpdateTenantRequest(
    @field:NotBlank val name: String,
    val locale: String = "de",
    val providerLabel: String? = null,
    val hasWallbox: Boolean = false,
    val hasHeatPump: Boolean = false,
    val heatingReferenceType: HeatingReferenceType = HeatingReferenceType.NONE,
    val defaultElectricityPrice: BigDecimal? = null,
    val defaultFeedInTariff: BigDecimal? = null,
    val defaultPetrolPrice: BigDecimal? = null,
    val defaultOilReferenceCost: BigDecimal? = null,
    val defaultGasReferenceCost: BigDecimal? = null,
    val defaultEvEfficiencyKwh100km: BigDecimal? = null,
    val defaultIceEfficiencyL100km: BigDecimal? = null,
)
